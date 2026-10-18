package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.CompleteBibleTopicsData
import com.example.data.model.TopicEntity
import com.example.data.model.TopicReferenceEntity
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel
import com.example.util.AdvancedSearchEngine
import com.example.util.BibleReferenceParser
import com.example.util.ReferenceParseResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BibleTopicsAndSearchTest {

    private lateinit var application: Application
    private lateinit var viewModel: BibleViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = BibleViewModel(application)
    }

    // =========================================================================
    // 1. TOPIC DATASET INTEGRITY (ALL 34 TOPICS VERIFIED)
    // =========================================================================

    @Test
    fun testAll34TopicsPresentAndPopulated() {
        val topics = CompleteBibleTopicsData.getAllCuratedTopics()
        assertTrue("Must contain at least 34 topics, found: ${topics.size}", topics.size >= 34)

        val requiredTopics = listOf(
            "faith", "prayer", "love", "forgiveness", "salvation", "grace",
            "holy_spirit", "wisdom", "fear", "anxiety", "peace", "hope",
            "healing", "repentance", "baptism", "worship", "fasting", "obedience",
            "temptation", "sin", "eternal_life", "second_coming", "heaven", "hell",
            "family", "marriage", "parenting", "money", "work", "leadership",
            "spiritual_growth", "discipleship", "christian_living"
        )

        for (req in requiredTopics) {
            val found = topics.find { it.id == req }
            assertNotNull("Required topic '$req' must be present in curated data", found)
            assertTrue("Topic $req must have English title", found!!.titleEn.isNotBlank())
            assertTrue("Topic $req must have Tamil title", found.titleTa.isNotBlank())
            assertTrue("Topic $req must have introduction", found.introductionEn.isNotBlank())
            assertTrue("Topic $req must have detailed explanation", found.detailedExplanationEn.isNotBlank())
        }
    }

    @Test
    fun testTopicReferencesAreValidAndAuthentic() {
        val allTopics = CompleteBibleTopicsData.getAllCuratedTopics()
        for (topic in allTopics) {
            val references = CompleteBibleTopicsData.getCuratedReferencesForTopic(topic.id)
            assertTrue("Topic ${topic.id} must have verified references", references.isNotEmpty())

            for (ref in references) {
                val book = CanonicalBibleData.findBookById(ref.bookId)
                assertNotNull("Reference ${ref.referenceEn} must point to a valid bookId ${ref.bookId}", book)
                assertTrue("Chapter must be >= 1", ref.chapterNumber >= 1)
                assertTrue("Chapter must be <= book's chapterCount (${book!!.chapterCount})", ref.chapterNumber <= book.chapterCount)
                assertTrue("VerseStart must be >= 1", ref.verseStart >= 1)
                assertTrue("English verse text must not be empty", ref.verseTextEn.isNotBlank())
                assertTrue("Tamil verse text must not be empty", ref.verseTextTa.isNotBlank())
            }
        }
    }

    // =========================================================================
    // 2. BILINGUAL BIBLE REFERENCE PARSER TESTS
    // =========================================================================

    @Test
    fun testBilingualReferenceParsing() {
        // English
        val john316 = BibleReferenceParser.parse("John 3:16")
        assertTrue("John 3:16 should succeed", john316 is ReferenceParseResult.Success)
        val jRef = (john316 as ReferenceParseResult.Success).reference
        assertEquals("JHN", jRef.book.id)
        assertEquals(3, jRef.chapter)
        assertEquals(16, jRef.verse)

        // Tamil - ரோமர் 8:28
        val rom828 = BibleReferenceParser.parse("ரோமர் 8:28")
        assertTrue("ரோமர் 8:28 should succeed", rom828 is ReferenceParseResult.Success)
        val rRef = (rom828 as ReferenceParseResult.Success).reference
        assertEquals("ROM", rRef.book.id)
        assertEquals(8, rRef.chapter)
        assertEquals(28, rRef.verse)

        // Tamil - யோவான் 3:16
        val jhnTa = BibleReferenceParser.parse("யோவான் 3:16")
        assertTrue("யோவான் 3:16 should succeed", jhnTa is ReferenceParseResult.Success)
        val jtRef = (jhnTa as ReferenceParseResult.Success).reference
        assertEquals("JHN", jtRef.book.id)
        assertEquals(3, jtRef.chapter)
        assertEquals(16, jtRef.verse)

        // Verse range - மத்தேயு 6:9-13
        val mattLordPrayer = BibleReferenceParser.parse("மத்தேயு 6:9-13")
        assertTrue("மத்தேயு 6:9-13 should succeed", mattLordPrayer is ReferenceParseResult.Success)
        val mRef = (mattLordPrayer as ReferenceParseResult.Success).reference
        assertEquals("MAT", mRef.book.id)
        assertEquals(6, mRef.chapter)
        assertEquals(9, mRef.verse)
        assertEquals(13, mRef.endVerse)
    }

    // =========================================================================
    // 3. ADVANCED SEARCH ENGINE & Q&A
    // =========================================================================

    @Test
    fun testNaturalLanguageQuestionDetectionAndAnswer() {
        val qEn = "What does the Bible say about prayer?"
        assertTrue("English question must be recognized", AdvancedSearchEngine.isQuestion(qEn))

        val qTa = "ஜெபத்தைப் பற்றி வேதாகமம் என்ன சொல்கிறது?"
        assertTrue("Tamil question must be recognized", AdvancedSearchEngine.isQuestion(qTa))

        val topics = CompleteBibleTopicsData.getAllCuratedTopics()
        val prayerTopic = AdvancedSearchEngine.resolveTopicForQuestion(qEn, topics)
        assertNotNull("Should resolve to prayer topic", prayerTopic)
        assertEquals("prayer", prayerTopic!!.id)

        val answer = AdvancedSearchEngine.buildBiblicalAnswer(qEn, prayerTopic)
        assertTrue("Answer titleEn must be set", answer.titleEn.isNotBlank())
        assertTrue("Answer answerEn must not be empty", answer.answerEn.isNotBlank())
        assertTrue("Answer foundationalPassages must not be empty", answer.foundationalPassages.isNotEmpty())
    }

    // =========================================================================
    // 4. TOPIC FAVORITES & HISTORY LOGIC
    // =========================================================================

    @Test
    fun testTopicFavoritesAndHistory() = runBlocking {
        val topicId = "faith"
        assertFalse("Topic should not be favorite initially", viewModel.topicRepo.isFavorite(topicId))

        val isNowFav = viewModel.topicRepo.toggleFavorite(topicId)
        assertTrue("toggleFavorite should return true", isNowFav)
        assertTrue("Topic should now be favorite in repository", viewModel.topicRepo.isFavorite(topicId))

        // Open topic detail records view history and updates selectedTopicDetail state
        viewModel.openTopicDetail(topicId)
        for (i in 1..25) {
            org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
            if (viewModel.selectedTopicDetail.value != null) break
            kotlinx.coroutines.delay(40)
        }
        val study = viewModel.selectedTopicDetail.value
        assertNotNull("Study should be loaded", study)
        assertEquals(topicId, study!!.topic.id)
        assertEquals(AppScreen.TOPIC_DETAIL, viewModel.currentScreen.value)
    }

    // =========================================================================
    // 5. ADMIN TOPIC MANAGEMENT CRUD
    // =========================================================================

    @Test
    fun testAdminTopicCRUD() = runBlocking {
        val testTopic = TopicEntity(
            id = "custom_test_topic",
            slug = "test-topic",
            titleEn = "Covenant Faithfulness",
            titleTa = "உடன்படிக்கையின் உண்மைத்தன்மை",
            shortDescriptionEn = "God's unchanging covenant promises throughout Scripture",
            shortDescriptionTa = "வேதத்தில் காணப்படும் தேவனுடைய மாறாத வாக்குத்தத்தங்கள்",
            category = "Theology",
            categoryTa = "வேத இறையியல்",
            keywords = "covenant, promises, faithfulness",
            introductionEn = "God is faithful to all His covenants made with mankind.",
            introductionTa = "மனிதரோடு பண்ணின உடன்படிக்கைகளில் தேவன் உண்மையுள்ளவர்.",
            detailedExplanationEn = "Detailed study of Abrahamic, Mosaic, and New Covenants.",
            detailedExplanationTa = "ஆபிரகாமிய, மோசேயின் மற்றும் புதிய உடன்படிக்கைகளின் ஆழமான விளக்கம்.",
            isCustom = true
        )

        // Save custom topic
        viewModel.topicRepo.saveTopic(testTopic)
        val loaded = viewModel.topicRepo.getTopicById("custom_test_topic")
        assertNotNull("Saved admin custom topic must appear in database", loaded)
        assertEquals("Covenant Faithfulness", loaded!!.titleEn)

        // Add reference to custom topic
        val ref = TopicReferenceEntity(
            topicId = "custom_test_topic",
            bookId = "GEN",
            bookNameEn = "Genesis",
            bookNameTa = "ஆதியாகமம்",
            chapterNumber = 15,
            verseStart = 6,
            verseEnd = 6,
            referenceEn = "Genesis 15:6",
            referenceTa = "ஆதியாகமம் 15:6",
            verseTextEn = "And he believed in the LORD; and he counted it to him for righteousness.",
            verseTextTa = "அவன் கர்த்தரை விசுவாசித்தான், அதை அவருக்கு நீதியாக எண்ணினார்.",
            briefExplanationEn = "Abraham believed God's covenant promise.",
            sectionGroup = "KEY_VERSES"
        )
        viewModel.topicRepo.addTopicReference(ref)
        val study = viewModel.topicRepo.getRichTopicStudy("custom_test_topic")
        assertNotNull("Study should be populated", study)
        assertTrue("Reference should be present", study!!.allReferences.any { it.bookId == "GEN" && it.chapterNumber == 15 })

        // Verify delete
        viewModel.topicRepo.deleteTopic("custom_test_topic")
        val afterDelete = viewModel.topicRepo.getTopicById("custom_test_topic")
        assertNull("Custom topic should be removed after deletion", afterDelete)
    }

    // =========================================================================
    // 6. ENHANCEMENT 1.1: TOPIC DEEP STUDY & ORIGINAL LANGUAGE WORD STUDIES
    // =========================================================================

    @Test
    fun testBibleTopicDeepStudyOriginalLanguageWordStudies() {
        val testTopics = listOf("faith", "love", "peace", "grace", "prayer", "holy_spirit", "wisdom", "forgiveness", "salvation", "repentance")
        for (tId in testTopics) {
            val words = com.example.data.datasource.TopicDeepStudyData.getOriginalWordStudies(tId)
            assertTrue("Topic '$tId' must have scholarly original language word studies", words.isNotEmpty())
            for (w in words) {
                assertTrue("Original word text must not be blank in $tId", w.originalWord.isNotBlank())
                assertTrue("Transliteration must not be blank in $tId", w.transliteration.isNotBlank())
                assertTrue("Literal meaning (EN) must not be blank in $tId", w.literalMeaningEn.isNotBlank())
                assertTrue("Literal meaning (TA) must not be blank in $tId", w.literalMeaningTa.isNotBlank())
                assertTrue("Theological significance must be present in $tId", w.theologicalSignificanceEn.isNotBlank())
                assertTrue("Primary scripture must be cited in $tId", w.primaryScriptureEn.isNotBlank())
            }
        }
    }

    // =========================================================================
    // 7. ENHANCEMENT 1.2: BIBLE TOPIC TIMELINE NARRATIVE ORDERING
    // =========================================================================

    @Test
    fun testBibleTopicTimelineNarrativeOrdering() {
        val faithRefs = CompleteBibleTopicsData.getCuratedReferencesForTopic("faith")
        val timeline = com.example.data.datasource.TopicTimelineEngine.generateTimelineForTopic("faith", faithRefs)

        assertTrue("Faith timeline must contain multiple narrative epochs", timeline.size >= 4)

        // Verify narrative chronological order (eraOrder must be non-decreasing)
        for (i in 0 until timeline.size - 1) {
            assertTrue(
                "Timeline eras must progress in biblical chronological order (${timeline[i].eraEn} <= ${timeline[i+1].eraEn})",
                timeline[i].eraOrder <= timeline[i + 1].eraOrder
            )
        }

        // Verify every timeline entry has valid book, chapter, and non-empty texts
        for (entry in timeline) {
            val book = CanonicalBibleData.findBookById(entry.bookId)
            assertNotNull("Timeline bookId '${entry.bookId}' must exist in CanonicalBibleData", book)
            assertTrue("Chapter must be >= 1", entry.chapterNumber >= 1)
            assertTrue("VerseStart must be >= 1", entry.verseStart >= 1)
            assertTrue("English verse text must not be blank", entry.verseTextEn.isNotBlank())
            assertTrue("Tamil verse text must not be blank", entry.verseTextTa.isNotBlank())
            assertTrue("Event title must not be blank", entry.eventTitleEn.isNotBlank())
        }
    }

    // =========================================================================
    // 8. ENHANCEMENT 1.3: COMPARE BIBLE VERSES
    // =========================================================================

    @Test
    fun testVerseComparisonSelectionAndLimits() {
        val refs = CompleteBibleTopicsData.getCuratedReferencesForTopic("peace")
        assertTrue("Must have at least 2 references for peace", refs.size >= 2)

        assertEquals("Initially comparison list should be empty", 0, viewModel.comparisonVerses.value.size)

        // Toggle first verse
        viewModel.toggleVerseForComparison(refs[0])
        assertEquals(1, viewModel.comparisonVerses.value.size)
        assertEquals(refs[0].referenceEn, viewModel.comparisonVerses.value[0].referenceEn)

        // Toggle second verse
        viewModel.toggleVerseForComparison(refs[1])
        assertEquals(2, viewModel.comparisonVerses.value.size)

        // Toggle off first verse
        viewModel.toggleVerseForComparison(refs[0])
        assertEquals(1, viewModel.comparisonVerses.value.size)
        assertEquals(refs[1].referenceEn, viewModel.comparisonVerses.value[0].referenceEn)

        // Clear comparison
        viewModel.clearComparisonVerses()
        assertEquals(0, viewModel.comparisonVerses.value.size)
    }

    // =========================================================================
    // 9. ENHANCEMENT 1.4: PERSONAL BIBLE STUDY NOTES CRUD
    // =========================================================================

    @Test
    fun testPersonalBibleStudyNotesCRUD() = runBlocking {
        val topicId = "wisdom"
        val noteTitle = "Proverbs 9:10 Reflection"
        val noteContent = "The fear of the LORD is the beginning of wisdom. Godly reverent awe directs all life decisions."
        val verseRef = "Proverbs 9:10"

        // 1. Create personal study note
        viewModel.saveTopicStudyNote(topicId, noteTitle, noteContent, verseRef)
        for (i in 1..25) {
            org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
            if (viewModel.topicRepo.getNotesForTopic(topicId).first().isNotEmpty()) break
            kotlinx.coroutines.delay(40)
        }

        val notes = viewModel.topicRepo.getNotesForTopic(topicId).first()
        assertTrue("Note must appear in repository for topic $topicId", notes.isNotEmpty())
        val saved = notes.find { it.title == noteTitle }
        assertNotNull("Saved note must match title", saved)
        assertEquals(topicId, saved!!.topicId)
        assertEquals(verseRef, saved.associatedVerseReference)
        assertEquals(noteContent, saved.content)
        assertTrue("Note must have timestamp", saved.createdAt > 0)

        // 2. Update personal study note
        val updatedNote = saved.copy(title = "Updated Wisdom Reflection", content = "Updated insight on divine wisdom.")
        viewModel.updateTopicStudyNote(updatedNote)
        for (i in 1..25) {
            org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
            if (viewModel.topicRepo.getNotesForTopic(topicId).first().any { it.title == "Updated Wisdom Reflection" }) break
            kotlinx.coroutines.delay(40)
        }

        val afterUpdate = viewModel.topicRepo.getNotesForTopic(topicId).first()
        val foundUpdated = afterUpdate.find { it.id == saved.id }
        assertNotNull(foundUpdated)
        assertEquals("Updated Wisdom Reflection", foundUpdated!!.title)
        assertEquals("Updated insight on divine wisdom.", foundUpdated.content)

        // 3. Delete personal study note
        viewModel.deleteTopicStudyNote(foundUpdated)
        for (i in 1..25) {
            org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
            if (viewModel.topicRepo.getNotesForTopic(topicId).first().none { it.id == saved.id }) break
            kotlinx.coroutines.delay(40)
        }

        val afterDelete = viewModel.topicRepo.getNotesForTopic(topicId).first()
        assertFalse("Deleted note must not be present", afterDelete.any { it.id == saved.id })
    }

    // =========================================================================
    // 10. ENHANCEMENT 2.1: AI TOPIC ASSISTANT GROUNDED QUESTIONS
    // =========================================================================

    @Test
    fun testAiTopicAssistantGroundedQuestions() = runBlocking {
        val study = viewModel.topicRepo.getRichTopicStudy("faith")
        assertNotNull("Study must load for faith", study)

        // Test simple meaning question in English
        val qEn = "What does this topic mean in simple words?"
        val ansEn = com.example.util.TopicAssistantEngine.answerTopicQuestion(qEn, study!!, isTamil = false)
        assertNotNull("Answer must be generated", ansEn)
        assertEquals("assistant", ansEn.role)
        assertTrue("Answer text must not be empty", ansEn.contentEn.isNotBlank())
        assertTrue("Cited references must not be empty", ansEn.citedReferences.isNotEmpty())

        // Test Tamil question
        val qTa = "இந்த தலைப்பின் எளிய விளக்கம் என்ன?"
        val ansTa = com.example.util.TopicAssistantEngine.answerTopicQuestion(qTa, study, isTamil = true)
        assertNotNull("Tamil answer must be generated", ansTa)
        assertTrue("Tamil content must not be empty", ansTa.contentTa.isNotBlank())
    }

    // =========================================================================
    // 11. ENHANCEMENT 2.2: RELATED TOPIC DISCOVERY MAPPING
    // =========================================================================

    @Test
    fun testRelatedTopicDiscoveryMapping() {
        val allTopics = CompleteBibleTopicsData.getAllCuratedTopics()

        // Test Faith related topics
        val faithRelated = com.example.data.datasource.TopicRelationshipData.getRelatedTopics("faith", allTopics)
        assertTrue("Faith must have related topics", faithRelated.isNotEmpty())
        assertFalse("Related topics must not contain current topic itself", faithRelated.any { it.id == "faith" })
        assertEquals("Related topics must not have duplicates", faithRelated.size, faithRelated.distinctBy { it.id }.size)

        // Verify known theological connections
        val faithRelatedIds = faithRelated.map { it.id }
        assertTrue("Faith should connect to hope, prayer, or wisdom", faithRelatedIds.contains("hope") || faithRelatedIds.contains("prayer"))

        // Test Forgiveness related topics
        val forgRelated = com.example.data.datasource.TopicRelationshipData.getRelatedTopics("forgiveness", allTopics)
        val forgRelatedIds = forgRelated.map { it.id }
        assertTrue("Forgiveness should connect to grace, love, or repentance", forgRelatedIds.contains("grace") || forgRelatedIds.contains("love"))
    }
}
