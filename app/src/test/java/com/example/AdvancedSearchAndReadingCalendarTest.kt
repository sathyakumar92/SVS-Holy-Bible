package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.ReadingHistory
import com.example.ui.screens.search.SearchMode
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel
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
class AdvancedSearchAndReadingCalendarTest {

    private lateinit var application: Application
    private lateinit var viewModel: BibleViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = BibleViewModel(application)
    }

    // =========================================================================
    // FEATURE 1: BIBLE REFERENCE PARSER TESTS
    // =========================================================================

    @Test
    fun testParseEnglishReferences() {
        val res1 = BibleReferenceParser.parse("John 3:16")
        assertTrue("John 3:16 must be parsed successfully", res1 is ReferenceParseResult.Success)
        val ref1 = (res1 as ReferenceParseResult.Success).reference
        assertEquals("JHN", ref1.book.id)
        assertEquals(3, ref1.chapter)
        assertEquals(16, ref1.verse)

        val res2 = BibleReferenceParser.parse("Genesis 1:1")
        assertTrue("Genesis 1:1 must be parsed", res2 is ReferenceParseResult.Success)
        val ref2 = (res2 as ReferenceParseResult.Success).reference
        assertEquals("GEN", ref2.book.id)
        assertEquals(1, ref2.chapter)
        assertEquals(1, ref2.verse)

        val res3 = BibleReferenceParser.parse("Psalm 23")
        assertTrue("Psalm 23 must be parsed", res3 is ReferenceParseResult.Success)
        val ref3 = (res3 as ReferenceParseResult.Success).reference
        assertEquals("PSA", ref3.book.id)
        assertEquals(23, ref3.chapter)
        assertNull(ref3.verse)

        val res4 = BibleReferenceParser.parse("1 Cor 13:4")
        assertTrue("1 Cor 13:4 must be parsed", res4 is ReferenceParseResult.Success)
        val ref4 = (res4 as ReferenceParseResult.Success).reference
        assertEquals("1CO", ref4.book.id)
        assertEquals(13, ref4.chapter)
        assertEquals(4, ref4.verse)
    }

    @Test
    fun testParseTamilReferences() {
        val res1 = BibleReferenceParser.parse("யோவான் 3:16")
        assertTrue("யோவான் 3:16 must be parsed", res1 is ReferenceParseResult.Success)
        val ref1 = (res1 as ReferenceParseResult.Success).reference
        assertEquals("JHN", ref1.book.id)
        assertEquals(3, ref1.chapter)
        assertEquals(16, ref1.verse)

        val res2 = BibleReferenceParser.parse("ஆதியாகமம் 1:1")
        assertTrue("ஆதியாகமம் 1:1 must be parsed", res2 is ReferenceParseResult.Success)
        val ref2 = (res2 as ReferenceParseResult.Success).reference
        assertEquals("GEN", ref2.book.id)
        assertEquals(1, ref2.chapter)
        assertEquals(1, ref2.verse)

        val res3 = BibleReferenceParser.parse("சங்கீதம் 23")
        assertTrue("சங்கீதம் 23 must be parsed", res3 is ReferenceParseResult.Success)
        val ref3 = (res3 as ReferenceParseResult.Success).reference
        assertEquals("PSA", ref3.book.id)
        assertEquals(23, ref3.chapter)
    }

    @Test
    fun testInvalidReferencesHandleGracefully() {
        val resInvalidBook = BibleReferenceParser.parse("NonexistentBook 3:16")
        assertTrue("Nonexistent book must return Invalid or NotAReference", resInvalidBook is ReferenceParseResult.Invalid || resInvalidBook is ReferenceParseResult.NotAReference)

        val resChapterOutOfBounds = BibleReferenceParser.parse("Jude 5")
        assertTrue("Jude only has 1 chapter, chapter 5 must be invalid", resChapterOutOfBounds is ReferenceParseResult.Invalid)

        val resOrdinaryWords = BibleReferenceParser.parse("faith and love")
        assertEquals(ReferenceParseResult.NotAReference, resOrdinaryWords)
    }

    // =========================================================================
    // FEATURE 1: ADVANCED SEARCH FILTERS & REPOSITORY TESTS
    // =========================================================================

    @Test
    fun testSearchModesAndFiltering() = runBlocking {
        viewModel.repository.ensureDatabaseInitialized()

        // 1. Search English
        val (resultsEn, _) = viewModel.repository.searchVersesAdvanced(
            query = "beginning",
            languageCode = "en",
            searchMode = SearchMode.WORD,
            wholeWord = false,
            testamentFilter = "OT",
            bookFilter = "GEN",
            fromChapter = 1,
            toChapter = 1
        )
        assertTrue("Genesis 1:1 should match 'beginning'", resultsEn.isNotEmpty())
        assertEquals("GEN", resultsEn.first().verse.bookId)
        assertEquals("en", resultsEn.first().verse.languageCode)

        // 2. Search Tamil
        val (resultsTa, _) = viewModel.repository.searchVersesAdvanced(
            query = "ஆதியிலே",
            languageCode = "ta",
            searchMode = SearchMode.WORD,
            wholeWord = false,
            testamentFilter = "OT",
            bookFilter = "GEN",
            fromChapter = 1,
            toChapter = 1
        )
        assertTrue("Genesis 1:1 Tamil should match 'ஆதியிலே'", resultsTa.isNotEmpty())
        assertEquals("GEN", resultsTa.first().verse.bookId)
        assertEquals("ta", resultsTa.first().verse.languageCode)
    }

    @Test
    fun testExactPhraseSearch() = runBlocking {
        viewModel.repository.ensureDatabaseInitialized()

        val (results, _) = viewModel.repository.searchVersesAdvanced(
            query = "In the beginning",
            languageCode = "en",
            searchMode = SearchMode.EXACT_PHRASE,
            wholeWord = false,
            testamentFilter = null,
            bookFilter = null,
            fromChapter = 0,
            toChapter = 0
        )
        assertTrue("Exact phrase 'In the beginning' should match Genesis 1:1", results.any { it.verse.bookId == "GEN" && it.verse.chapterNumber == 1 && it.verse.verseNumber == 1 })
    }

    // =========================================================================
    // FEATURE 2: READING CALENDAR & STATISTICS TESTS
    // =========================================================================

    @Test
    fun testReadingCalendarRecordingAndStats() = runBlocking {
        viewModel.clearAllReadingHistory()
        val book = CanonicalBibleData.findBookById("GEN")!!

        // Record readings
        viewModel.repository.recordReading(book.id, book.englishName, 1)
        viewModel.repository.recordReading(book.id, book.englishName, 2)
        org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val history = viewModel.repository.getAllReadingHistory().first()
        assertTrue("At least 2 chapters should be recorded in history", history.size >= 2)
        assertEquals("GEN", history.first().bookId)
    }

    @Test
    fun testNavigationToReadingCalendarScreen() {
        viewModel.navigateTo(AppScreen.READING_CALENDAR)
        assertEquals(AppScreen.READING_CALENDAR, viewModel.currentScreen.value)

        val navigatedBack = viewModel.navigateBack()
        assertTrue("Should navigate back successfully", navigatedBack)
    }
}
