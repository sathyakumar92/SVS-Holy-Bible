package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.datasource.BibleQuizBank
import com.example.data.local.AppDatabase
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

class QuizRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("svs_bible_quiz_storage", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RECENT_QUESTION_IDS = "recent_question_ids_csv"
        private const val KEY_QUESTION_HISTORY_JSON = "question_history_json"
        private const val KEY_ACTIVE_SESSION_JSON = "active_session_json"
        private const val KEY_TOTAL_QUIZZES_TAKEN = "total_quizzes_taken"
        private const val RECENT_EXCLUSION_WINDOW = 60 // rolling window of questions to avoid repeating
    }

    // =========================================================================
    // QUESTION HISTORY & REPEAT PREVENTION
    // =========================================================================

    fun getRecentlyAskedQuestionIds(): Set<String> {
        val raw = prefs.getString(KEY_RECENT_QUESTION_IDS, "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    fun recordQuestionsAsked(questionIds: List<String>) {
        val currentHistory = prefs.getString(KEY_RECENT_QUESTION_IDS, "") ?: ""
        val existingList = if (currentHistory.isNotBlank()) {
            currentHistory.split(",").filter { it.isNotBlank() }.toMutableList()
        } else {
            mutableListOf()
        }

        // Add new question IDs to the end (most recent)
        for (id in questionIds) {
            existingList.remove(id) // avoid duplicate entries in recent list
            existingList.add(id)
        }

        // Cap at RECENT_EXCLUSION_WINDOW to prevent pool exhaustion while guaranteeing repeat prevention
        val trimmedList = if (existingList.size > RECENT_EXCLUSION_WINDOW) {
            existingList.takeLast(RECENT_EXCLUSION_WINDOW)
        } else {
            existingList
        }

        prefs.edit().putString(KEY_RECENT_QUESTION_IDS, trimmedList.joinToString(",")).apply()

        // Also update exposure stats
        updateQuestionExposureStats(questionIds)
    }

    private fun updateQuestionExposureStats(questionIds: List<String>) {
        try {
            val jsonStr = prefs.getString(KEY_QUESTION_HISTORY_JSON, "{}") ?: "{}"
            val jsonObj = JSONObject(jsonStr)
            val now = System.currentTimeMillis()

            for (id in questionIds) {
                val item = if (jsonObj.has(id)) {
                    jsonObj.getJSONObject(id)
                } else {
                    JSONObject().apply {
                        put("timesAsked", 0)
                        put("timesCorrect", 0)
                    }
                }
                item.put("timesAsked", item.optInt("timesAsked", 0) + 1)
                item.put("lastAskedTimestamp", now)
                jsonObj.put(id, item)
            }
            prefs.edit().putString(KEY_QUESTION_HISTORY_JSON, jsonObj.toString()).apply()
        } catch (_: Exception) {}
    }

    fun recordQuestionCorrect(questionId: String) {
        try {
            val jsonStr = prefs.getString(KEY_QUESTION_HISTORY_JSON, "{}") ?: "{}"
            val jsonObj = JSONObject(jsonStr)
            val item = if (jsonObj.has(questionId)) {
                jsonObj.getJSONObject(questionId)
            } else {
                JSONObject().apply {
                    put("timesAsked", 1)
                    put("timesCorrect", 0)
                    put("lastAskedTimestamp", System.currentTimeMillis())
                }
            }
            item.put("timesCorrect", item.optInt("timesCorrect", 0) + 1)
            jsonObj.put(questionId, item)
            prefs.edit().putString(KEY_QUESTION_HISTORY_JSON, jsonObj.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getUniqueQuestionsAttemptedCount(): Int {
        val jsonStr = prefs.getString(KEY_QUESTION_HISTORY_JSON, "{}") ?: "{}"
        return try {
            JSONObject(jsonStr).length()
        } catch (_: Exception) {
            0
        }
    }

    fun resetQuestionHistory() {
        prefs.edit()
            .remove(KEY_RECENT_QUESTION_IDS)
            .remove(KEY_QUESTION_HISTORY_JSON)
            .remove(KEY_ACTIVE_SESSION_JSON)
            .apply()
    }

    // =========================================================================
    // SMART RANDOM GENERATOR WITH REPEAT PREVENTION
    // =========================================================================

    fun generateQuizSession(
        config: QuizConfig,
        customSeed: Long? = null
    ): QuizSessionState {
        val eligibleQuestions = BibleQuizBank.filterQuestions(
            difficulty = config.difficulty,
            testament = config.testament,
            category = config.category
        )

        val targetCount = config.questionCount.coerceAtMost(eligibleQuestions.size).coerceAtLeast(1)
        val recentIds = getRecentlyAskedQuestionIds()

        // Divide into unasked recently vs recently asked
        val unaskedPool = eligibleQuestions.filter { it.id !in recentIds }
        val recentPool = eligibleQuestions.filter { it.id in recentIds }

        val rng = if (customSeed != null) Random(customSeed) else Random(System.currentTimeMillis())
        val selectedQuestions = mutableListOf<QuizQuestion>()

        if (unaskedPool.size >= targetCount) {
            // We have enough unasked questions! Shuffle and pick targetCount without replacement
            val shuffled = unaskedPool.shuffled(rng)
            selectedQuestions.addAll(shuffled.take(targetCount))
        } else {
            // Take all unasked questions
            selectedQuestions.addAll(unaskedPool)
            val needed = targetCount - unaskedPool.size
            // Fill remaining from recentPool, sorted by oldest asked if possible, then shuffled
            val shuffledRecent = recentPool.shuffled(rng)
            selectedQuestions.addAll(shuffledRecent.take(needed))
        }

        // Shuffle question sequence so order is varied
        val finalQuestionList = selectedQuestions.shuffled(rng)

        // Build QuizSessionQuestion objects with shuffled options and stable index mapping
        val sessionQuestions = finalQuestionList.map { q ->
            createSessionQuestion(q, rng)
        }

        val sessionState = QuizSessionState(
            sessionId = UUID.randomUUID().toString(),
            config = config,
            questions = sessionQuestions,
            currentIndex = 0,
            score = 0,
            startTime = System.currentTimeMillis(),
            isCompleted = false
        )

        // Persist session immediately
        saveActiveSession(sessionState)

        // Record question IDs into recent history
        recordQuestionsAsked(finalQuestionList.map { it.id })

        val totalQuizzes = prefs.getInt(KEY_TOTAL_QUIZZES_TAKEN, 0)
        prefs.edit().putInt(KEY_TOTAL_QUIZZES_TAKEN, totalQuizzes + 1).apply()

        return sessionState
    }

    private fun createSessionQuestion(question: QuizQuestion, rng: Random): QuizSessionQuestion {
        val size = question.optionsEn.size
        val indices = (0 until size).toList().shuffled(rng)

        val shuffledEn = indices.map { question.optionsEn[it] }
        val shuffledTa = indices.map { question.optionsTa.getOrElse(it) { question.optionsEn[it] } }
        val correctShuffledIndex = indices.indexOf(question.correctIndex)

        val originalToShuffled = (0 until size).map { origIdx -> indices.indexOf(origIdx) }
        val shuffledToOriginal = indices

        return QuizSessionQuestion(
            question = question,
            shuffledOptionsEn = shuffledEn,
            shuffledOptionsTa = shuffledTa,
            originalToShuffledIndices = originalToShuffled,
            shuffledToOriginalIndices = shuffledToOriginal,
            correctShuffledIndex = correctShuffledIndex,
            selectedShuffledIndex = null,
            isSubmitted = false
        )
    }

    // =========================================================================
    // ACTIVE SESSION PERSISTENCE & RESTORATION
    // =========================================================================

    fun saveActiveSession(session: QuizSessionState) {
        try {
            val root = JSONObject().apply {
                put("sessionId", session.sessionId)
                put("currentIndex", session.currentIndex)
                put("score", session.score)
                put("startTime", session.startTime)
                put("isCompleted", session.isCompleted)
                session.completedTime?.let { put("completedTime", it) }

                val configObj = JSONObject().apply {
                    put("questionCount", session.config.questionCount)
                    put("difficulty", session.config.difficulty)
                    put("testament", session.config.testament)
                    put("category", session.config.category)
                    put("language", session.config.language)
                }
                put("config", configObj)

                val questionsArray = JSONArray()
                for (sq in session.questions) {
                    val qObj = JSONObject().apply {
                        put("questionId", sq.question.id)
                        put("correctShuffledIndex", sq.correctShuffledIndex)
                        sq.selectedShuffledIndex?.let { put("selectedShuffledIndex", it) }
                        put("isSubmitted", sq.isSubmitted)

                        val origToShuf = JSONArray()
                        sq.originalToShuffledIndices.forEach { origToShuf.put(it) }
                        put("origToShuf", origToShuf)

                        val shufToOrig = JSONArray()
                        sq.shuffledToOriginalIndices.forEach { shufToOrig.put(it) }
                        put("shufToOrig", shufToOrig)
                    }
                    questionsArray.put(qObj)
                }
                put("questions", questionsArray)
            }
            prefs.edit().putString(KEY_ACTIVE_SESSION_JSON, root.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getActiveSession(): QuizSessionState? {
        val raw = prefs.getString(KEY_ACTIVE_SESSION_JSON, null) ?: return null
        return try {
            val root = JSONObject(raw)
            val sessionId = root.getString("sessionId")
            val currentIndex = root.getInt("currentIndex")
            val score = root.getInt("score")
            val startTime = root.getLong("startTime")
            val isCompleted = root.getBoolean("isCompleted")
            val completedTime = if (root.has("completedTime")) root.getLong("completedTime") else null

            val cObj = root.getJSONObject("config")
            val config = QuizConfig(
                questionCount = cObj.getInt("questionCount"),
                difficulty = cObj.getString("difficulty"),
                testament = cObj.getString("testament"),
                category = cObj.getString("category"),
                language = cObj.getString("language")
            )

            val qArray = root.getJSONArray("questions")
            val questions = mutableListOf<QuizSessionQuestion>()
            for (i in 0 until qArray.length()) {
                val qObj = qArray.getJSONObject(i)
                val qId = qObj.getString("questionId")
                val baseQuestion = BibleQuizBank.getQuestionById(qId) ?: continue

                val shufToOrig = mutableListOf<Int>()
                val shufToOrigArray = qObj.getJSONArray("shufToOrig")
                for (j in 0 until shufToOrigArray.length()) {
                    shufToOrig.add(shufToOrigArray.getInt(j))
                }

                val origToShuf = mutableListOf<Int>()
                val origToShufArray = qObj.getJSONArray("origToShuf")
                for (j in 0 until origToShufArray.length()) {
                    origToShuf.add(origToShufArray.getInt(j))
                }

                val shuffledEn = shufToOrig.map { baseQuestion.optionsEn[it] }
                val shuffledTa = shufToOrig.map { baseQuestion.optionsTa.getOrElse(it) { baseQuestion.optionsEn[it] } }
                val correctShuffledIndex = qObj.getInt("correctShuffledIndex")
                val selectedShuffledIndex = if (qObj.has("selectedShuffledIndex")) qObj.getInt("selectedShuffledIndex") else null
                val isSubmitted = qObj.getBoolean("isSubmitted")

                questions.add(
                    QuizSessionQuestion(
                        question = baseQuestion,
                        shuffledOptionsEn = shuffledEn,
                        shuffledOptionsTa = shuffledTa,
                        originalToShuffledIndices = origToShuf,
                        shuffledToOriginalIndices = shufToOrig,
                        correctShuffledIndex = correctShuffledIndex,
                        selectedShuffledIndex = selectedShuffledIndex,
                        isSubmitted = isSubmitted
                    )
                )
            }

            if (questions.isEmpty()) return null

            QuizSessionState(
                sessionId = sessionId,
                config = config,
                questions = questions,
                currentIndex = currentIndex,
                score = score,
                startTime = startTime,
                isCompleted = isCompleted,
                completedTime = completedTime
            )
        } catch (_: Exception) {
            null
        }
    }

    fun clearActiveSession() {
        prefs.edit().remove(KEY_ACTIVE_SESSION_JSON).apply()
    }

    suspend fun saveQuizScore(quizTitle: String, score: Int, total: Int) {
        try {
            database.quizScoreDao().insertScore(
                QuizScore(
                    quizTitle = quizTitle,
                    score = score,
                    totalQuestions = total,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) {}
    }
}
