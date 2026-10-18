package com.example.util

import com.example.BuildConfig
import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.TopicDeepStudyData
import com.example.data.model.RichTopicStudy
import com.example.data.model.TopicAiMessage
import com.example.data.model.TopicReferenceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * AI-Powered Bible Topic Assistant Engine.
 * Supports direct Gemini REST API with strict grounding in verified Scripture data,
 * and seamless fallback to local grounded theological Q&A when offline or when
 * API keys are not yet configured in the Secrets panel.
 */
object TopicAssistantEngine {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val defaultSuggestedQuestionsEn = listOf(
        "What does this topic mean in simple words?",
        "What does the Bible teach about this topic?",
        "Which Bible characters demonstrated this principle?",
        "What other verses support this teaching?",
        "How can I apply this teaching in daily life?",
        "What is the biblical background and original language context?"
    )

    val defaultSuggestedQuestionsTa = listOf(
        "இந்த தலைப்பின் எளிய விளக்கம் என்ன?",
        "வேதாகமம் இதைப்பற்றி என்ன போதிக்கிறது?",
        "இந்த சத்தியத்தை வெளிப்படுத்திய வேத கதாபாத்திரங்கள் யார்?",
        "இதை உறுதிப்படுத்தும் பிற முக்கிய வசனங்கள் யாவை?",
        "இதை அன்றாட வாழ்க்கையில் நடைமுறைப்படுத்துவது எப்படி?",
        "மூல மொழி (எபிரெயர் / கிரேக்கம்) விளக்கம் என்ன?"
    )

    /**
     * Answers a user's question with full context of the currently selected topic study.
     */
    suspend fun answerTopicQuestion(
        question: String,
        study: RichTopicStudy,
        isTamil: Boolean,
        conversationHistory: List<TopicAiMessage> = emptyList()
    ): TopicAiMessage = withContext(Dispatchers.IO) {
        val topic = study.topic
        val apiKey = getApiKey()

        // 1. If real Gemini API key is available and configured, call Gemini API
        if (apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY") && apiKey.length > 10) {
            try {
                val geminiResponse = callGeminiApi(
                    apiKey = apiKey,
                    question = question,
                    study = study,
                    isTamil = isTamil,
                    conversationHistory = conversationHistory
                )
                if (geminiResponse.isNotBlank()) {
                    val matchingRefs = findRelevantReferences(question, study.allReferences)
                    return@withContext TopicAiMessage(
                        role = "assistant",
                        contentEn = if (isTamil) "" else geminiResponse,
                        contentTa = if (isTamil) geminiResponse else "",
                        citedReferences = matchingRefs,
                        isAiGenerated = true,
                        isOfflineGrounded = false
                    )
                }
            } catch (_: Exception) {
                // Graceful fallback to verified offline biblical knowledge
            }
        }

        // 2. Intelligent, Grounded Offline Fallback Engine
        val localAnswer = generateLocalGroundedAnswer(question, study, isTamil)
        val relevantRefs = findRelevantReferences(question, study.allReferences)

        TopicAiMessage(
            role = "assistant",
            contentEn = if (isTamil) "" else localAnswer,
            contentTa = if (isTamil) localAnswer else "",
            citedReferences = relevantRefs,
            isAiGenerated = true,
            isOfflineGrounded = true
        )
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Throwable) {
            ""
        }
    }

    private suspend fun callGeminiApi(
        apiKey: String,
        question: String,
        study: RichTopicStudy,
        isTamil: Boolean,
        conversationHistory: List<TopicAiMessage>
    ): String = withContext(Dispatchers.IO) {
        val topic = study.topic
        val wordStudies = TopicDeepStudyData.getOriginalWordStudies(topic.id)
        val wordStudyInfo = wordStudies.joinToString("; ") { "${it.originalWord} (${it.transliteration} - ${it.literalMeaningEn})" }
        val refsSample = study.allReferences.take(8).joinToString("; ") { "${it.referenceEn}: \"${it.verseTextEn}\"" }

        val systemPrompt = """
            You are a faithful, reverent, and scholarly Biblical Study Assistant for the SVS Holy Bible application.
            Current Topic Context:
            - Title: ${topic.titleEn} (Tamil: ${topic.titleTa})
            - Category: ${topic.category}
            - Introduction: ${topic.introductionEn}
            - Theological Teachings: ${topic.detailedExplanationEn}
            - Key Characters: ${topic.biblicalPeopleEn}
            - Practical Applications: ${topic.practicalApplicationsEn}
            - Original Language Terms: $wordStudyInfo
            - Verified Scripture Passages: $refsSample

            Guidelines:
            1. Clearly distinguish between Scripture quotations and your explanations/commentary.
            2. Only quote verified canonical Bible books. Do not invent verses.
            3. Answer in ${if (isTamil) "Tamil (தமிழ்)" else "English"}.
            4. Be clear, encouraging, reverent, and practical.
            5. State clearly that this is AI-assisted study commentary to be compared with the Holy Bible.
        """.trimIndent()

        val contentsArray = JSONArray()

        // Include recent conversation turns for context
        conversationHistory.takeLast(4).forEach { msg ->
            val role = if (msg.role == "user") "user" else "model"
            val text = if (isTamil && msg.contentTa.isNotBlank()) msg.contentTa else msg.contentEn
            contentsArray.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().put(JSONObject().put("text", text)))
            })
        }

        // Add current user prompt
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", "$question\n[Context: Topic '${topic.titleEn}']")))
        })

        val requestBodyJson = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 800)
            })
        }

        val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("Gemini API call failed with HTTP ${response.code}")
        }

        val responseBodyStr = response.body?.string() ?: ""
        val json = JSONObject(responseBodyStr)
        val candidates = json.optJSONArray("candidates") ?: return@withContext ""
        if (candidates.length() == 0) return@withContext ""

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return@withContext ""
        val parts = content.optJSONArray("parts") ?: return@withContext ""
        if (parts.length() == 0) return@withContext ""

        parts.getJSONObject(0).optString("text", "")
    }

    /**
     * Resilient offline generator strictly grounded in the curated topic study and verified Scripture.
     */
    private fun generateLocalGroundedAnswer(
        question: String,
        study: RichTopicStudy,
        isTamil: Boolean
    ): String {
        val qLower = question.lowercase()
        val topic = study.topic
        val wordStudies = TopicDeepStudyData.getOriginalWordStudies(topic.id)

        return when {
            // Meaning in simple words
            qLower.contains("simple words") || qLower.contains("simple") || qLower.contains("எளிய") || qLower.contains("அர்த்தம்") -> {
                if (isTamil) {
                    """
                    📌 **${topic.titleTa} என்பதன் எளிய விளக்கம்:**
                    ${topic.shortDescriptionTa.ifBlank { topic.introductionTa }}
                    
                    📖 **வேத முக்கியத்துவம்:**
                    ${topic.introductionTa}
                    
                    💡 **சுருக்கம்:** இது தேவனுடைய வார்த்தையை முழுமையாக நம்பி, நம்முடைய அன்றாட வாழ்வில் கீழ்ப்படிதலோடு நடக்கும் ஆவிக்குரிய அனுபவமாகும்.
                    """.trimIndent()
                } else {
                    """
                    📌 **Simple Meaning of ${topic.titleEn}:**
                    ${topic.shortDescriptionEn.ifBlank { topic.introductionEn }}
                    
                    📖 **Biblical Significance:**
                    ${topic.introductionEn}
                    
                    💡 **In Summary:** It is an unshakeable trust in God's promises that manifests in active, daily obedience and spiritual peace.
                    """.trimIndent()
                }
            }

            // Characters / Biblical People
            qLower.contains("character") || qLower.contains("people") || qLower.contains("person") || qLower.contains("கதாபாத்திரம்") || qLower.contains("யார்") -> {
                if (isTamil) {
                    """
                    👥 **${topic.titleTa} தலைப்பில் முன்மாதிரியான வேத கதாபாத்திரங்கள்:**
                    ${topic.biblicalPeopleTa.ifBlank { topic.biblicalPeopleEn }}
                    
                    இவர்கள் தங்கள் வாழ்க்கையில் பல்வேறு சோதனைகள் மற்றும் உபத்திரவங்களின் மத்தியிலும் தேவனுடைய சத்தியத்திற்கு உண்மையாயிருந்து வெற்றிகண்டார்கள்.
                    """.trimIndent()
                } else {
                    """
                    👥 **Biblical Characters Demonstrating ${topic.titleEn}:**
                    ${topic.biblicalPeopleEn}
                    
                    These individuals lived out this principle through major life trials, demonstrating that God is ever faithful to those who walk in His truth.
                    """.trimIndent()
                }
            }

            // Practical application / Daily life
            qLower.contains("apply") || qLower.contains("application") || qLower.contains("daily life") || qLower.contains("நடைமுறை") || qLower.contains("வாழ்க்கை") -> {
                if (isTamil) {
                    """
                    🌱 **${topic.titleTa} அன்றாட வாழ்வில் நடைமுறைப்படுத்தும் வழிகள்:**
                    ${topic.practicalApplicationsTa.ifBlank { topic.practicalApplicationsEn }}
                    
                    ✨ **முக்கிய பாடம்:**
                    ${topic.keyLessonsTa.ifBlank { topic.keyLessonsEn }}
                    """.trimIndent()
                } else {
                    """
                    🌱 **Practical Daily Applications for ${topic.titleEn}:**
                    ${topic.practicalApplicationsEn}
                    
                    ✨ **Key Spiritual Lesson:**
                    ${topic.keyLessonsEn}
                    """.trimIndent()
                }
            }

            // Hebrew / Greek original words
            qLower.contains("hebrew") || qLower.contains("greek") || qLower.contains("original") || qLower.contains("மூல மொழி") || qLower.contains("எபிரெயர்") || qLower.contains("கிரேக்கம்") -> {
                val wordsStr = wordStudies.joinToString("\n\n") { w ->
                    if (isTamil) {
                        "• **${w.language}:** ${w.originalWord} (${w.transliteration})\n  - பொருள்: ${w.literalMeaningTa}\n  - இறையியல் விளக்கம்: ${w.theologicalSignificanceTa}\n  - வேத ஆதாரம்: ${w.primaryScriptureTa}"
                    } else {
                        "• **${w.language}:** ${w.originalWord} (${w.transliteration})\n  - Literal Meaning: ${w.literalMeaningEn}\n  - Theological Insight: ${w.theologicalSignificanceEn}\n  - Scripture: ${w.primaryScriptureEn}"
                    }
                }
                if (isTamil) {
                    "🏛️ **${topic.titleTa} — மூல வேத மொழி வார்த்தை ஆய்வு:**\n\n$wordsStr"
                } else {
                    "🏛️ **${topic.titleEn} — Original Biblical Languages Word Study:**\n\n$wordsStr"
                }
            }

            // General / Teachings
            else -> {
                if (isTamil) {
                    """
                    📖 **${topic.titleTa} பற்றிய வேதாகம போதனை:**
                    ${topic.detailedExplanationTa.ifBlank { topic.introductionTa }}
                    
                    🔑 **முக்கிய வேத வசனங்கள்:**
                    ${study.allReferences.take(3).joinToString("\n") { "• ${it.referenceTa}: \"${it.verseTextTa}\"" }}
                    
                    💡 **நடைமுறை உண்மை:** ${topic.practicalApplicationsTa.ifBlank { topic.keyLessonsTa }}
                    """.trimIndent()
                } else {
                    """
                    📖 **Biblical Teaching on ${topic.titleEn}:**
                    ${topic.detailedExplanationEn.ifBlank { topic.introductionEn }}
                    
                    🔑 **Foundational Scripture Passages:**
                    ${study.allReferences.take(3).joinToString("\n") { "• ${it.referenceEn}: \"${it.verseTextEn}\"" }}
                    
                    💡 **Practical Application:** ${topic.practicalApplicationsEn.ifBlank { topic.keyLessonsEn }}
                    """.trimIndent()
                }
            }
        }
    }

    private fun findRelevantReferences(
        query: String,
        allRefs: List<TopicReferenceEntity>
    ): List<TopicReferenceEntity> {
        val q = query.lowercase()
        val matched = allRefs.filter { ref ->
            ref.referenceEn.lowercase().contains(q) ||
            ref.verseTextEn.lowercase().contains(q) ||
            ref.verseTextTa.contains(query) ||
            ref.briefExplanationEn.lowercase().contains(q) ||
            (q.contains("promise") && ref.sectionGroup == "PROMISES") ||
            (q.contains("example") && ref.sectionGroup == "BIBLICAL_EXAMPLES") ||
            (q.contains("key") && ref.sectionGroup == "KEY_VERSES")
        }
        return if (matched.isNotEmpty()) matched.take(3) else allRefs.take(2)
    }
}
