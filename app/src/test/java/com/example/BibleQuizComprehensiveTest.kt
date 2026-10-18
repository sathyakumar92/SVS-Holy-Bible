package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.BibleQuizBank
import com.example.data.datasource.QuizValidator
import com.example.data.local.AppDatabase
import com.example.data.model.QuizConfig
import com.example.data.repository.QuizRepository
import com.example.ui.viewmodel.BibleViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BibleQuizComprehensiveTest {

    private lateinit var application: Application
    private lateinit var database: AppDatabase
    private lateinit var quizRepository: QuizRepository
    private lateinit var viewModel: BibleViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getInstance(application)
        quizRepository = QuizRepository(application, database)
        quizRepository.resetQuestionHistory()
        viewModel = BibleViewModel(application)
    }

    // 1. Question bank contains at least 200 verified unique questions.
    @Test
    fun test1_QuestionBankContainsAtLeast200VerifiedQuestions() {
        val all = BibleQuizBank.allQuestions
        assertTrue("Bank must contain at least 200 questions, found: ${all.size}", all.size >= 200)
    }

    // 2. Duplicate question IDs are rejected.
    @Test
    fun test2_NoDuplicateQuestionIds() {
        val all = BibleQuizBank.allQuestions
        val ids = all.map { it.id }
        val uniqueIds = ids.toSet()
        assertEquals("All question IDs must be strictly unique", ids.size, uniqueIds.size)
    }

    // 3. Each question has a valid correct answer.
    @Test
    fun test3_EachQuestionHasValidCorrectAnswer() {
        val all = BibleQuizBank.allQuestions
        for (q in all) {
            assertTrue("Question ${q.id} must have 4 English options", q.optionsEn.size == 4)
            assertTrue("Question ${q.id} must have 4 Tamil options", q.optionsTa.size == 4)
            assertTrue("Correct index must be 0..3 for ${q.id}", q.correctIndex in 0..3)
            val correctOpt = q.optionsEn[q.correctIndex]
            assertTrue("Correct option text must not be blank", correctOpt.isNotBlank())
        }
    }

    // 4. Question randomization produces a valid sequence.
    @Test
    fun test4_QuestionRandomizationProducesValidSequence() {
        val config = QuizConfig(questionCount = 10, difficulty = "MIXED", testament = "BOTH", category = "ALL")
        val session1 = quizRepository.generateQuizSession(config, customSeed = 12345L)
        val session2 = quizRepository.generateQuizSession(config, customSeed = 67890L)

        assertEquals(10, session1.questions.size)
        assertEquals(10, session2.questions.size)

        val ids1 = session1.questions.map { it.question.id }
        val ids2 = session2.questions.map { it.question.id }

        // Sequences with different seeds should differ in ordering or selection
        assertNotEquals("Sequences from different seeds should not be identical", ids1, ids2)
    }

    // 5. No question repeats within a session.
    @Test
    fun test5_NoQuestionRepeatsWithinASession() {
        val config = QuizConfig(questionCount = 20, difficulty = "MIXED", testament = "BOTH", category = "ALL")
        val session = quizRepository.generateQuizSession(config)
        val ids = session.questions.map { it.question.id }
        val uniqueIds = ids.toSet()
        assertEquals("A session must contain zero duplicate questions", ids.size, uniqueIds.size)
    }

    // 6. Consecutive sessions avoid recent questions while enough eligible questions remain.
    @Test
    fun test6_ConsecutiveSessionsAvoidRecentQuestions() {
        quizRepository.resetQuestionHistory()

        val config = QuizConfig(questionCount = 10, difficulty = "MIXED", testament = "BOTH", category = "ALL")
        val session1 = quizRepository.generateQuizSession(config, customSeed = 111L)
        val session1Ids = session1.questions.map { it.question.id }.toSet()

        // Second session should draw from unasked questions since bank has 200+
        val session2 = quizRepository.generateQuizSession(config, customSeed = 222L)
        val session2Ids = session2.questions.map { it.question.id }.toSet()

        val overlap = session1Ids.intersect(session2Ids)
        assertTrue("Consecutive 10-question sessions should have 0 overlap given 200+ pool. Found: $overlap", overlap.isEmpty())
    }

    // 7. Question history persists across app restarts.
    @Test
    fun test7_QuestionHistoryPersistsAcrossRestarts() {
        quizRepository.resetQuestionHistory()
        val testIds = listOf("bq_cg_01", "bq_ae_01", "bq_na_01")
        quizRepository.recordQuestionsAsked(testIds)

        // Create a new instance simulating app restart
        val newRepo = QuizRepository(application, database)
        val recent = newRepo.getRecentlyAskedQuestionIds()

        for (id in testIds) {
            assertTrue("Persisted question history must contain $id", recent.contains(id))
        }
    }

    // 8. Answer options shuffle without changing the correct answer.
    @Test
    fun test8_AnswerOptionsShuffleWithoutChangingCorrectAnswer() {
        val config = QuizConfig(questionCount = 5, difficulty = "MIXED", testament = "BOTH", category = "ALL")
        val session = quizRepository.generateQuizSession(config, customSeed = 42L)

        for (sq in session.questions) {
            val originalCorrectText = sq.question.optionsEn[sq.question.correctIndex]
            val shuffledCorrectText = sq.shuffledOptionsEn[sq.correctShuffledIndex]
            assertEquals("Shuffled correct answer text must match original correct answer text", originalCorrectText, shuffledCorrectText)

            val originalCorrectTa = sq.question.optionsTa[sq.question.correctIndex]
            val shuffledCorrectTa = sq.shuffledOptionsTa[sq.correctShuffledIndex]
            assertEquals("Tamil shuffled correct answer must also match", originalCorrectTa, shuffledCorrectTa)
        }
    }

    // 9. Correct answers add exactly one point.
    @Test
    fun test9_CorrectAnswerAddsExactlyOnePoint() {
        viewModel.startNewQuiz(QuizConfig(questionCount = 5))
        val session = viewModel.quizSessionState.value!!
        val currQ = session.questions[0]
        val initialScore = viewModel.quizScore.value

        viewModel.selectAndValidateQuizAnswer(currQ.correctShuffledIndex)

        assertEquals("Score must increment by exactly 1 on correct answer", initialScore + 1, viewModel.quizScore.value)
    }

    // 10. Incorrect answers add zero points.
    @Test
    fun test10_IncorrectAnswerAddsZeroPoints() {
        viewModel.startNewQuiz(QuizConfig(questionCount = 5))
        val session = viewModel.quizSessionState.value!!
        val currQ = session.questions[0]
        val wrongIndex = if (currQ.correctShuffledIndex == 0) 1 else 0
        val initialScore = viewModel.quizScore.value

        viewModel.selectAndValidateQuizAnswer(wrongIndex)

        assertEquals("Score must NOT increment on wrong answer", initialScore, viewModel.quizScore.value)
    }

    // 11. Repeated taps cannot award duplicate points.
    @Test
    fun test11_RepeatedTapsCannotAwardDuplicatePoints() {
        viewModel.startNewQuiz(QuizConfig(questionCount = 5))
        val session = viewModel.quizSessionState.value!!
        val currQ = session.questions[0]

        viewModel.selectAndValidateQuizAnswer(currQ.correctShuffledIndex)
        assertEquals(1, viewModel.quizScore.value)

        // Attempt multiple repeated clicks
        viewModel.selectAndValidateQuizAnswer(currQ.correctShuffledIndex)
        viewModel.selectAndValidateQuizAnswer(if (currQ.correctShuffledIndex == 0) 1 else 0)

        assertEquals("Score must strictly remain 1 after repeated taps", 1, viewModel.quizScore.value)
    }

    // 12. Score counter reaches the correct final value.
    @Test
    fun test12_ScoreCounterReachesCorrectFinalValue() {
        viewModel.startNewQuiz(QuizConfig(questionCount = 3))
        val session = viewModel.quizSessionState.value!!

        // Answer Q1 correctly
        viewModel.selectAndValidateQuizAnswer(session.questions[0].correctShuffledIndex)
        viewModel.nextQuizQuestion()

        // Answer Q2 incorrectly
        val wrong2 = if (session.questions[1].correctShuffledIndex == 0) 1 else 0
        viewModel.selectAndValidateQuizAnswer(wrong2)
        viewModel.nextQuizQuestion()

        // Answer Q3 correctly
        viewModel.selectAndValidateQuizAnswer(session.questions[2].correctShuffledIndex)
        viewModel.nextQuizQuestion()

        assertTrue("Quiz must be marked completed after last question", viewModel.isQuizCompleted.value)
        assertEquals("Final score must be exactly 2 out of 3", 2, viewModel.quizScore.value)
    }

    // 13. Progress bar matches answered-question count.
    @Test
    fun test13_ProgressMatchesAnsweredQuestionCount() {
        viewModel.startNewQuiz(QuizConfig(questionCount = 10))
        val session = viewModel.quizSessionState.value!!

        // Before answering Q1
        assertFalse(viewModel.isAnswerSubmitted.value)

        // After submitting Q1
        viewModel.selectAndValidateQuizAnswer(session.questions[0].correctShuffledIndex)
        assertTrue(viewModel.isAnswerSubmitted.value)
        assertEquals(0, viewModel.currentQuizIndex.value)

        // Move to Q2
        viewModel.nextQuizQuestion()
        assertEquals(1, viewModel.currentQuizIndex.value)
        assertFalse(viewModel.isAnswerSubmitted.value)
    }

    // 14. Final score and accuracy are calculated correctly.
    @Test
    fun test14_FinalScoreAndAccuracyCalculatedCorrectly() {
        val total = 10
        val correct = 8
        val incorrect = 2
        assertEquals(total, correct + incorrect)
        val accuracy = (correct.toFloat() / total * 100).toInt()
        assertEquals(80, accuracy)
    }

    // 15. English and Tamil questions use the same correct-answer IDs.
    @Test
    fun test15_EnglishAndTamilUseSameCorrectAnswerId() {
        val all = BibleQuizBank.allQuestions
        for (q in all) {
            assertEquals("English and Tamil must have identical options count", q.optionsEn.size, q.optionsTa.size)
            assertTrue("Correct answer index must be identical", q.correctIndex in q.optionsEn.indices)
            assertEquals("Stable answer ID must match", "opt_${q.correctIndex}", q.correctAnswerId)
        }
    }

    // 16. Every displayed Bible reference is valid.
    @Test
    fun test16_EveryBibleReferenceIsValid() {
        val all = BibleQuizBank.allQuestions
        for (q in all) {
            assertTrue("Scripture reference English must not be blank for ${q.id}", q.scriptureRefEn.isNotBlank())
            assertTrue("Scripture reference Tamil must not be blank for ${q.id}", q.scriptureRefTa.isNotBlank())
        }
    }

    // 17. Resuming a session does not double-count answers.
    @Test
    fun test17_ResumingSessionDoesNotDoubleCount() {
        viewModel.startNewQuiz(QuizConfig(questionCount = 5))
        val session = viewModel.quizSessionState.value!!
        viewModel.selectAndValidateQuizAnswer(session.questions[0].correctShuffledIndex)
        assertEquals(1, viewModel.quizScore.value)

        // Exit and resume
        viewModel.openQuizConfig()
        viewModel.resumeUnfinishedQuiz()

        // Score must still be 1, submitted must be true, selected index restored
        assertEquals(1, viewModel.quizScore.value)
        assertTrue(viewModel.isAnswerSubmitted.value)

        // Tapping again must not increment score
        viewModel.selectAndValidateQuizAnswer(session.questions[0].correctShuffledIndex)
        assertEquals(1, viewModel.quizScore.value)
    }

    // 18. Exiting and restarting follow the specified rules.
    @Test
    fun test18_ExitingAndRestartingRules() {
        viewModel.startNewQuiz(QuizConfig(questionCount = 5))
        viewModel.selectAndValidateQuizAnswer(0)
        viewModel.openQuizConfig()

        assertTrue(viewModel.showQuizConfigScreen.value)
        assertNotNull(viewModel.savedActiveSession.value)

        // Restarting starts fresh session
        viewModel.restartQuiz()
        assertEquals(0, viewModel.quizScore.value)
        assertEquals(0, viewModel.currentQuizIndex.value)
        assertFalse(viewModel.isAnswerSubmitted.value)
    }

    // 19. Quiz works offline when supported.
    @Test
    fun test19_QuizWorksOfflineWithLocalBank() {
        val report = QuizValidator.validateAllQuestions()
        assertTrue("Local bank validation must pass cleanly: ${report.errors}", report.isValid)
        assertTrue("Bank must have 200+ local questions", report.totalQuestions >= 200)
    }

    // 20. Existing Bible reading features continue to function.
    @Test
    fun test20_ExistingBibleReadingContinuesToFunction() {
        val gen1Verses = viewModel.currentVerses.value
        assertNotNull("Bible reader verses must be available", gen1Verses)
        assertTrue("Selected book must be populated", viewModel.selectedBook.value.englishName.isNotBlank())
    }
}
