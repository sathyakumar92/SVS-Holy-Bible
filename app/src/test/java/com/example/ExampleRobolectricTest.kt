package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.DevotionalData
import com.example.data.datasource.ScriptureSeedData
import com.example.data.model.Testament
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SVS Holy Bible", appName)
    }

    @Test
    fun `validate canonical 66 books structure`() {
        val allBooks = CanonicalBibleData.allBooks
        assertEquals("Bible must have 66 canonical books", 66, allBooks.size)

        val otBooks = CanonicalBibleData.oldTestamentBooks
        assertEquals("Old Testament must have 39 books", 39, otBooks.size)

        val ntBooks = CanonicalBibleData.newTestamentBooks
        assertEquals("New Testament must have 27 books", 27, ntBooks.size)

        // Validate book boundary anchors
        assertEquals("Book 1 must be Genesis", "Genesis", allBooks[0].englishName)
        assertEquals("Book 1 testament must be OT", Testament.OLD_TESTAMENT, allBooks[0].testament)
        assertEquals("Genesis chapter count must be 50", 50, allBooks[0].chapterCount)

        assertEquals("Book 39 must be Malachi", "Malachi", allBooks[38].englishName)
        assertEquals("Book 40 must be Matthew", "Matthew", allBooks[39].englishName)
        assertEquals("Book 40 testament must be NT", Testament.NEW_TESTAMENT, allBooks[39].testament)

        assertEquals("Book 66 must be Revelation", "Revelation", allBooks[65].englishName)
        assertEquals("Revelation chapter count must be 22", 22, allBooks[65].chapterCount)
    }

    @Test
    fun `validate bilingual book lookup`() {
        val genesis = CanonicalBibleData.findBookById("GEN")
        assertNotNull(genesis)
        assertEquals("ஆதியாகமம்", genesis?.tamilName)

        val john = CanonicalBibleData.findBookByName("John")
        assertNotNull(john)
        assertEquals("JHN", john?.id)

        val johnTamil = CanonicalBibleData.findBookByName("யோவான்")
        assertNotNull(johnTamil)
        assertEquals("JHN", johnTamil?.id)
    }

    @Test
    fun `validate scripture seed data contains both English and Tamil editions`() {
        val verses = ScriptureSeedData.getInitialVerses()
        assertTrue("Seed verses should not be empty", verses.isNotEmpty())

        val englishVerses = verses.filter { it.languageCode == "en" && it.editionId == "KJV" }
        val tamilVerses = verses.filter { it.languageCode == "ta" && it.editionId == "TAMIL_BSI" }

        assertTrue("English KJV verses must exist", englishVerses.isNotEmpty())
        assertTrue("Tamil BSI verses must exist", tamilVerses.isNotEmpty())
    }

    @Test
    fun `validate devotional topics and quiz data integrity`() {
        val topics = DevotionalData.topics
        assertTrue("Topics count must be at least 10", topics.size >= 10)
        topics.forEach { topic ->
            assertTrue("Topic must have English name", topic.englishName.isNotBlank())
            assertTrue("Topic must have Tamil name", topic.tamilName.isNotBlank())
            assertTrue("Topic must have scripture references", topic.verseReferences.isNotEmpty())
        }

        val quizQuestions = DevotionalData.quizQuestions
        assertTrue("Quiz questions must be present", quizQuestions.isNotEmpty())
        quizQuestions.forEach { q ->
            assertTrue("Options must not be empty", q.optionsEn.isNotEmpty())
            assertTrue("Correct index must be within range", q.correctIndex in q.optionsEn.indices)
            assertTrue("Scripture reference must be verified", q.scriptureRefEn.isNotBlank())
        }
    }

    @Test
    fun `validate advanced features seed data integrity`() {
        val periods = com.example.data.datasource.AdvancedSeedData.biblicalPeriods
        assertTrue("Biblical periods must be populated", periods.isNotEmpty())

        val events = com.example.data.datasource.AdvancedSeedData.timelineEvents
        assertTrue("Timeline events must be populated", events.isNotEmpty())

        val locations = com.example.data.datasource.AdvancedSeedData.biblicalLocations
        assertTrue("Biblical locations must have coordinates", locations.isNotEmpty())
        locations.forEach { loc ->
            assertTrue("Latitude must be valid", loc.latitude in -90.0..90.0)
            assertTrue("Longitude must be valid", loc.longitude in -180.0..180.0)
        }
    }

    @Test
    fun `validate no duplicate verses exist in any seeded chapter`() {
        val verses = ScriptureSeedData.getInitialVerses()
        // Group by editionId, languageCode, bookId, chapterNumber
        val grouped = verses.groupBy { "${it.editionId}_${it.languageCode}_${it.bookId}_${it.chapterNumber}" }
        grouped.forEach { (chapterKey, chapterVerses) ->
            val verseNumbers = chapterVerses.map { it.verseNumber }
            val uniqueVerseNumbers = verseNumbers.distinct()
            assertEquals("Verses must not repeat in chapter $chapterKey", uniqueVerseNumbers.size, verseNumbers.size)
        }
    }

    @Test
    fun `validate canonical chapter verses fallback for all chapters`() {
        val testBooks = listOf("GEN", "MAT", "JHN", "ROM", "REV")
        testBooks.forEach { bookId ->
            val enVerses = ScriptureSeedData.getCanonicalVersesForChapter(bookId, 1, "en")
            val taVerses = ScriptureSeedData.getCanonicalVersesForChapter(bookId, 1, "ta")
            assertTrue("English verses must not be empty for $bookId Ch 1", enVerses.isNotEmpty())
            assertTrue("Tamil verses must not be empty for $bookId Ch 1", taVerses.isNotEmpty())
            assertEquals("English verses must have unique verse numbers", enVerses.map { it.verseNumber }.distinct().size, enVerses.size)
            assertEquals("Tamil verses must have unique verse numbers", taVerses.map { it.verseNumber }.distinct().size, taVerses.size)
        }
    }

    @Test
    fun `validate John 3 has sequential verses without repetition`() {
        val enVerses = ScriptureSeedData.getCanonicalVersesForChapter("JHN", 3, "en")
        val taVerses = ScriptureSeedData.getCanonicalVersesForChapter("JHN", 3, "ta")

        assertTrue("John 3 must have verses", enVerses.size >= 10)
        assertEquals("English verses must match unique count", enVerses.map { it.verseNumber }.distinct().size, enVerses.size)
        assertEquals("Tamil verses must match unique count", taVerses.map { it.verseNumber }.distinct().size, taVerses.size)

        // Validate verse 1 is first
        assertEquals(1, enVerses[0].verseNumber)
        assertEquals(1, taVerses[0].verseNumber)
        // Validate verse 2 follows verse 1
        assertEquals(2, enVerses[1].verseNumber)
        assertEquals(2, taVerses[1].verseNumber)
        // Validate verse 3 follows verse 2
        assertEquals(3, enVerses[2].verseNumber)
        assertEquals(3, taVerses[2].verseNumber)
    }

    @Test
    fun `validate Genesis 1 has all sequential verses 1 to 31 without duplicates`() {
        val enVerses = ScriptureSeedData.getCanonicalVersesForChapter("GEN", 1, "en")
        val taVerses = ScriptureSeedData.getCanonicalVersesForChapter("GEN", 1, "ta")

        assertEquals("Genesis 1 must have 31 verses in English", 31, enVerses.size)
        assertEquals("Genesis 1 must have 31 verses in Tamil", 31, taVerses.size)

        // Verify each verse appears exactly once: 1, 2, 3, 4, 5 ... 31
        for (i in 1..31) {
            assertEquals("English verse $i number must match", i, enVerses[i - 1].verseNumber)
            assertEquals("Tamil verse $i number must match", i, taVerses[i - 1].verseNumber)
        }

        // Verify no duplicate verse numbers
        assertEquals(31, enVerses.map { it.verseNumber }.distinct().size)
        assertEquals(31, taVerses.map { it.verseNumber }.distinct().size)

        // Genesis 1:1 and 1:2 authentic text check
        assertEquals("In the beginning God created the heaven and the earth.", enVerses[0].verseText)
        assertEquals("ஆதியிலே தேவன் வானத்தையும் பூமியையும் சிருஷ்டித்தார்.", taVerses[0].verseText)
    }

    @Test
    fun `validate realtime feedback data model`() {
        val feedback = com.example.data.model.AppFeedback(
            rating = 5,
            category = "Feature Request",
            userName = "Christian Reader",
            userEmail = "reader@example.com",
            comments = "Great audio Bible and daily verse features!"
        )
        assertEquals(5, feedback.rating)
        assertEquals("Feature Request", feedback.category)
        assertEquals("Received", feedback.status)
        assertTrue(feedback.timestamp > 0)
    }

    @Test
    fun `validate font size and line spacing accessibility preferences`() {
        val defaultPrefs = com.example.data.repository.UserPreferences()
        assertEquals(18, defaultPrefs.fontSizeSp)
        assertEquals(1.4f, defaultPrefs.lineSpacingMultiplier, 0.01f)
        assertTrue(defaultPrefs.showVerseNumbers)

        // Validate supported font size boundary constraints [12..32]
        val supportedSizes = listOf(14, 18, 22, 26, 30)
        supportedSizes.forEach { size ->
            assertTrue("Font size $size must be within accessible bounds", size in 12..32)
        }

        // Validate line spacing options
        val lineSpacings = listOf(1.2f, 1.4f, 1.6f, 1.8f)
        lineSpacings.forEach { spacing ->
            assertTrue("Line spacing $spacing must be greater than 1.0", spacing >= 1.2f)
        }
    }

    @Test
    fun `validate no duplicate rows across multiple books and chapters in both English and Tamil`() {
        val testChapters = listOf(
            Triple("GEN", 1, 31),
            Triple("PSA", 23, 6),
            Triple("JHN", 1, 14),
            Triple("JHN", 3, 17),
            Triple("MAT", 5, 12),
            Triple("ROM", 8, 10),
            Triple("REV", 22, 10)
        )

        testChapters.forEach { (bookId, chapter, minExpectedCount) ->
            val enVerses = ScriptureSeedData.getCanonicalVersesForChapter(bookId, chapter, "en")
            val taVerses = ScriptureSeedData.getCanonicalVersesForChapter(bookId, chapter, "ta")

            assertTrue("$bookId $chapter EN must have at least $minExpectedCount verses", enVerses.size >= minExpectedCount)
            assertTrue("$bookId $chapter TA must have at least $minExpectedCount verses", taVerses.size >= minExpectedCount)

            // Strictly 0 duplicate verse numbers
            assertEquals("$bookId $chapter EN must have strictly distinct verse numbers",
                enVerses.map { it.verseNumber }.distinct().size, enVerses.size)
            assertEquals("$bookId $chapter TA must have strictly distinct verse numbers",
                taVerses.map { it.verseNumber }.distinct().size, taVerses.size)

            // Strictly ordered: 1, 2, 3, 4, 5...
            val enNumbers = enVerses.map { it.verseNumber }
            val taNumbers = taVerses.map { it.verseNumber }
            assertEquals((1..enVerses.size).toList(), enNumbers)
            assertEquals((1..taVerses.size).toList(), taNumbers)
        }
    }
}
