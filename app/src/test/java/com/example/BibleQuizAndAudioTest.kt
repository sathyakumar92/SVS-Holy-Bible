package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.audio.BibleAudioEngine
import com.example.data.datasource.DevotionalData
import com.example.data.model.BibleVerse
import com.example.ui.viewmodel.BibleViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BibleQuizAndAudioTest {

    private lateinit var application: Application
    private lateinit var viewModel: BibleViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = BibleViewModel(application)
    }

    // =========================================================================
    // QUIZ TESTS
    // =========================================================================

    @Test
    fun testQuizQuestionsDataIntegrity() {
        val questions = DevotionalData.quizQuestions
        assertTrue("Quiz questions must not be empty", questions.isNotEmpty())

        for (q in questions) {
            assertTrue("Question English must not be blank", q.questionEn.isNotBlank())
            assertTrue("Question Tamil must not be blank", q.questionTa.isNotBlank())
            assertEquals("English and Tamil option counts must match", q.optionsEn.size, q.optionsTa.size)
            assertTrue("Options count must be at least 2", q.optionsEn.size >= 2)
            assertTrue("Correct index must be within options range", q.correctIndex in q.optionsEn.indices)
            assertTrue("Explanation English must not be blank", q.explanationEn.isNotBlank())
            assertTrue("Scripture reference must not be blank", q.scriptureRefEn.isNotBlank())
        }
    }

    @Test
    fun testCorrectAnswerImmediateValidationAndScoreIncrement() {
        viewModel.restartQuiz()
        val question = viewModel.quizQuestions.value.first()
        val correctIndex = question.correctIndex

        assertEquals(0, viewModel.quizScore.value)
        assertFalse(viewModel.isAnswerSubmitted.value)
        assertNull(viewModel.selectedAnswerIndex.value)

        // Select correct answer
        viewModel.selectAndValidateQuizAnswer(correctIndex)

        // Immediate validation checks
        assertTrue("Answer must be immediately submitted", viewModel.isAnswerSubmitted.value)
        assertEquals("Selected index must be set", correctIndex, viewModel.selectedAnswerIndex.value)
        assertEquals("Score must increment by 1 on correct answer", 1, viewModel.quizScore.value)
    }

    @Test
    fun testWrongAnswerImmediateValidationAndScoreUnchanged() {
        viewModel.restartQuiz()
        val question = viewModel.quizQuestions.value.first()
        val wrongIndex = if (question.correctIndex == 0) 1 else 0

        assertEquals(0, viewModel.quizScore.value)

        // Select wrong answer
        viewModel.selectAndValidateQuizAnswer(wrongIndex)

        assertTrue("Answer must be immediately submitted", viewModel.isAnswerSubmitted.value)
        assertEquals("Selected index must record the user's tapped choice", wrongIndex, viewModel.selectedAnswerIndex.value)
        assertEquals("Score must NOT increment on wrong answer", 0, viewModel.quizScore.value)
    }

    @Test
    fun testAnswerLockingPreventsMultipleTapsAndRepeatedScoring() {
        viewModel.restartQuiz()
        val question = viewModel.quizQuestions.value.first()
        val correctIndex = question.correctIndex
        val wrongIndex = if (correctIndex == 0) 1 else 0

        // User taps correct answer
        viewModel.selectAndValidateQuizAnswer(correctIndex)
        assertEquals(1, viewModel.quizScore.value)

        // User attempts to tap wrong answer or correct answer again
        viewModel.selectAndValidateQuizAnswer(wrongIndex)
        viewModel.selectAndValidateQuizAnswer(correctIndex)

        // State must remain locked to first selection
        assertEquals("Selected index must remain first choice", correctIndex, viewModel.selectedAnswerIndex.value)
        assertEquals("Score must strictly remain 1 and not increment multiple times", 1, viewModel.quizScore.value)
    }

    @Test
    fun testQuestionNavigationResetsSelectionAndFeedbackState() {
        viewModel.restartQuiz()
        val firstQuestion = viewModel.quizQuestions.value.first()

        viewModel.selectAndValidateQuizAnswer(firstQuestion.correctIndex)
        assertTrue(viewModel.isAnswerSubmitted.value)
        assertNotNull(viewModel.selectedAnswerIndex.value)

        // Move to next question
        viewModel.nextQuizQuestion()

        assertEquals("Current question index must advance to 1", 1, viewModel.currentQuizIndex.value)
        assertNull("Selected answer must be reset for next question", viewModel.selectedAnswerIndex.value)
        assertFalse("Submitted state must be reset for next question", viewModel.isAnswerSubmitted.value)
    }

    @Test
    fun testQuizRestartResetsEverything() {
        viewModel.restartQuiz()
        val firstQuestion = viewModel.quizQuestions.value.first()
        viewModel.selectAndValidateQuizAnswer(firstQuestion.correctIndex)
        viewModel.nextQuizQuestion()

        // Restart
        viewModel.restartQuiz()

        assertEquals(0, viewModel.currentQuizIndex.value)
        assertNull(viewModel.selectedAnswerIndex.value)
        assertFalse(viewModel.isAnswerSubmitted.value)
        assertEquals(0, viewModel.quizScore.value)
        assertFalse(viewModel.isQuizCompleted.value)
    }

    // =========================================================================
    // AUDIO ENGINE & PLAYBACK TESTS
    // =========================================================================

    @Test
    fun testAudioEngineInitialState() {
        val audioEngine = BibleAudioEngine(application)
        val state = audioEngine.playbackState.value

        assertFalse("Should not be playing initially", state.isPlaying)
        assertFalse("Should not be paused initially", state.isPaused)
        audioEngine.release()
    }

    @Test
    fun testInstantPauseState() {
        val audioEngine = BibleAudioEngine(application)
        val dummyVerses = listOf(
            BibleVerse(1, "KJV", "en", "GEN", "Genesis", 1, 1, "In the beginning God created the heaven and the earth.", "in the beginning god created the heaven and the earth."),
            BibleVerse(2, "KJV", "en", "GEN", "Genesis", 1, 2, "And the earth was without form, and void.", "and the earth was without form, and void.")
        )

        audioEngine.playChapter(dummyVerses, 0, "en", 1.0f, "male")
        audioEngine.pause()

        val state = audioEngine.playbackState.value
        assertFalse("Must not be playing after pause", state.isPlaying)
        assertTrue("Must be marked paused immediately", state.isPaused)
        assertEquals("Status message must reflect paused state", "Paused", state.statusMessage)

        audioEngine.release()
    }

    @Test
    fun testAudioEngineStopState() {
        val audioEngine = BibleAudioEngine(application)
        val dummyVerses = listOf(
            BibleVerse(1, "KJV", "en", "GEN", "Genesis", 1, 1, "In the beginning God created the heaven and the earth.", "in the beginning god created the heaven and the earth.")
        )

        audioEngine.playChapter(dummyVerses, 0, "en", 1.0f, "male")
        audioEngine.stop()

        val state = audioEngine.playbackState.value
        assertFalse("Must not be playing after stop", state.isPlaying)
        assertFalse("Must not be paused after stop", state.isPaused)
        assertEquals("Stopped", state.statusMessage)

        audioEngine.release()
    }

    @Test
    fun testVoiceGenderSelectionUpdatesState() {
        val audioEngine = BibleAudioEngine(application)

        audioEngine.setVoiceGender("female")
        assertEquals("female", audioEngine.playbackState.value.voiceGender)

        audioEngine.setVoiceGender("male")
        assertEquals("male", audioEngine.playbackState.value.voiceGender)

        audioEngine.release()
    }

    @Test
    fun testLanguageSelectionUpdatesAudioState() {
        val audioEngine = BibleAudioEngine(application)

        audioEngine.setLanguage("ta")
        assertEquals("ta", audioEngine.playbackState.value.languageCode)

        audioEngine.setLanguage("en")
        assertEquals("en", audioEngine.playbackState.value.languageCode)

        audioEngine.release()
    }

    @Test
    fun testPlaybackSpeedUpdatesState() {
        val audioEngine = BibleAudioEngine(application)

        audioEngine.setSpeed(1.25f)
        assertEquals(1.25f, audioEngine.playbackState.value.speed, 0.01f)

        audioEngine.setSpeed(0.75f)
        assertEquals(0.75f, audioEngine.playbackState.value.speed, 0.01f)

        audioEngine.release()
    }

    @Test
    fun testDirectLanguageSwitchingPreservesBookAndChapter() {
        val initialBook = viewModel.selectedBook.value
        val initialChapter = viewModel.selectedChapter.value

        // Direct switch to Tamil
        viewModel.setBibleLanguage("ta")
        assertEquals(initialBook.id, viewModel.selectedBook.value.id)
        assertEquals(initialChapter, viewModel.selectedChapter.value)

        // Direct switch to English
        viewModel.setBibleLanguage("en")
        assertEquals(initialBook.id, viewModel.selectedBook.value.id)
        assertEquals(initialChapter, viewModel.selectedChapter.value)
    }

    @Test
    fun testRepeatedSameLanguageSelectionDoesNotAlterState() {
        viewModel.setBibleLanguage("ta")
        val state1Book = viewModel.selectedBook.value
        val state1Chapter = viewModel.selectedChapter.value

        // Tap Tamil again (no-op)
        viewModel.setBibleLanguage("ta")
        assertEquals(state1Book.id, viewModel.selectedBook.value.id)
        assertEquals(state1Chapter, viewModel.selectedChapter.value)
    }

    @Test
    fun testAudioEngineReceivesDirectLanguageChange() {
        val audioEngine = BibleAudioEngine(application)
        audioEngine.setLanguage("ta")
        assertEquals("ta", audioEngine.playbackState.value.languageCode)

        audioEngine.setLanguage("en")
        assertEquals("en", audioEngine.playbackState.value.languageCode)
        audioEngine.release()
    }

    @Test
    fun testLanguageSelectionUpdatesInterfaceAndBibleLanguage() {
        // Select Tamil directly (like tapping தமிழ் segment)
        viewModel.setBibleLanguage("ta")
        assertEquals("ta", viewModel.userPreferences.value.bibleLanguage)
        assertEquals("ta", viewModel.userPreferences.value.interfaceLanguage)

        // Select English directly (like tapping EN segment)
        viewModel.setBibleLanguage("en")
        assertEquals("en", viewModel.userPreferences.value.bibleLanguage)
        assertEquals("en", viewModel.userPreferences.value.interfaceLanguage)
    }

    @Test
    fun testVersesReflectSelectedLanguage() {
        // Start on English
        viewModel.setBibleLanguage("en")
        val enVerses = viewModel.currentVerses.value
        assertTrue("English verses should be present", enVerses.isNotEmpty())
        assertEquals("English verses must have 'en' language code", "en", enVerses.first().languageCode)
        assertTrue("English verse text must not be blank", enVerses.first().verseText.isNotBlank())

        // Switch to Tamil directly
        viewModel.setBibleLanguage("ta")
        val taVerses = viewModel.currentVerses.value
        assertTrue("Tamil verses should be present", taVerses.isNotEmpty())
        assertEquals("Tamil verses must have 'ta' language code", "ta", taVerses.first().languageCode)
        assertTrue("Tamil verse text must not be blank", taVerses.first().verseText.isNotBlank())
    }

    @Test
    fun testVoiceGenderSelectionUpdatesEngineAndState() {
        val audioEngine = BibleAudioEngine(application)
        audioEngine.setVoiceGender("female")
        assertEquals("female", audioEngine.playbackState.value.voiceGender)
        assertTrue("Selected voice name should be assigned", audioEngine.playbackState.value.selectedVoiceName != null)

        audioEngine.setVoiceGender("male")
        assertEquals("male", audioEngine.playbackState.value.voiceGender)
        assertTrue("Selected voice name should be assigned", audioEngine.playbackState.value.selectedVoiceName != null)
        audioEngine.release()
    }

    @Test
    fun testInstantPauseStopsEngineAndUpdatesState() {
        val audioEngine = BibleAudioEngine(application)
        audioEngine.pause()
        assertFalse("Engine must not be playing after pause", audioEngine.playbackState.value.isPlaying)
        assertTrue("Engine must be paused after pause", audioEngine.playbackState.value.isPaused)

        audioEngine.stop()
        assertFalse("Engine must not be playing after stop", audioEngine.playbackState.value.isPlaying)
        assertFalse("Engine must not be paused after stop", audioEngine.playbackState.value.isPaused)
        audioEngine.release()
    }

    @Test
    fun testAudioSyncPreferences() {
        val audioEngine = BibleAudioEngine(application)
        audioEngine.syncPreferences(languageCode = "ta", voiceGender = "female", speed = 1.25f)
        assertEquals("ta", audioEngine.playbackState.value.languageCode)
        assertEquals("female", audioEngine.playbackState.value.voiceGender)
        assertEquals(1.25f, audioEngine.playbackState.value.speed, 0.01f)
        audioEngine.release()
    }

    // =========================================================================
    // SHORTS & TOPICS TESTS
    // =========================================================================

    @Test
    fun testBibleShortsContainVideoWithAudio() {
        val devVideos = com.example.data.datasource.CuratedShortsAnalysisEngine.getDeveloperShorts()
        assertTrue("Developer shorts must not be empty", devVideos.isNotEmpty())

        val (short1, analysis1, refs1) = devVideos[0]
        assertNotNull("At least one short must have a raw video resource", short1.videoRawResId)
        assertEquals(
            R.raw.sermon_loneliness_short,
            short1.videoRawResId
        )
        assertTrue("Short duration must be greater than 0", short1.durationSeconds > 0)
        assertTrue("Title must not be blank", short1.title.isNotBlank())
        assertTrue("References must not be empty", refs1.isNotEmpty())
        assertTrue("Analysis transcript must not be blank", analysis1.transcriptTa.isNotBlank())
    }

    @Test
    fun testBibleTopicsTitleConsistency() {
        val topics = DevotionalData.topics
        assertTrue("Curated topics must not be empty", topics.isNotEmpty())
        for (topic in topics) {
            assertTrue("Topic name English must be valid", topic.englishName.isNotBlank())
            assertTrue("Topic name Tamil must be valid", topic.tamilName.isNotBlank())
        }
    }
}
