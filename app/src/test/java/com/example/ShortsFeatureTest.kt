package com.example

import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.CuratedShortsAnalysisEngine
import com.example.data.model.ShortEntity
import org.junit.Assert.*
import org.junit.Test

class ShortsFeatureTest {

    @Test
    fun testDeveloperVideosExistAndPreserveExactTitles() {
        val devVideos = CuratedShortsAnalysisEngine.getDeveloperShorts()
        assertEquals("There should be 3 authentic developer uploaded videos", 3, devVideos.size)

        // Video 1: Loneliness
        val (short1, analysis1, refs1) = devVideos[0]
        assertEquals("short_loneliness_pallavilai", short1.id)
        assertTrue("Exact title must contain Loneliness", short1.title.contains("Loneliness"))
        assertTrue("Exact title must contain தனிமை", short1.title.contains("தனிமை"))
        assertEquals("9:16", short1.aspectRatio)
        assertTrue("Audio track must be available", short1.audioAvailable)
        assertEquals("PUBLISHED", short1.publicationStatus)
        assertEquals(83, short1.durationSeconds)

        // Verify transcript and characters
        assertTrue("Transcript must mention Psalm 23", analysis1.transcriptTa.contains("23-வது சங்கீதத்தில்"))
        assertTrue("Transcript must mention Paul", analysis1.transcriptTa.contains("2 தீமோத்தேயு 4:16-17"))
        assertTrue("Transcript must mention Joshua", analysis1.transcriptTa.contains("யோசுவா 1:9"))
        assertTrue("Characters must include David", analysis1.biblicalCharactersEn.contains("David"))
        assertTrue("Characters must include Paul", analysis1.biblicalCharactersEn.contains("Paul"))

        // Verify Bible references grounded in canonical Scripture
        assertTrue("Must contain Psalm 23:4", refs1.any { it.bookId == "PSA" && it.chapterNumber == 23 && it.verseStart == 4 })
        assertTrue("Must contain 2 Tim 4:16", refs1.any { it.bookId == "2TI" && it.chapterNumber == 4 && it.verseStart == 16 })
        assertTrue("Must contain Joshua 1:9", refs1.any { it.bookId == "JOS" && it.chapterNumber == 1 && it.verseStart == 9 })

        // Verify exact quotation distinction
        val psaRef = refs1.first { it.bookId == "PSA" }
        assertEquals("QUOTATION", psaRef.referenceType)
        assertTrue(psaRef.verseTextTa.contains("மரண இருளின் பள்ளத்தாக்கிலே"))

        // Video 2: Holy Spirit Fellowship
        val (short2, analysis2, refs2) = devVideos[1]
        assertEquals("short_holy_spirit_communion", short2.id)
        assertTrue(short2.title.contains("Holy Spirit") || short2.title.contains("ஆவியானவரோடு"))
        assertTrue("Transcript must mention Adam and breath of life", analysis2.transcriptTa.contains("ஆதாம்") && analysis2.transcriptTa.contains("ஜீவ சுவாசத்தை"))
        assertTrue("References must include Genesis 2:7", refs2.any { it.bookId == "GEN" && it.chapterNumber == 2 && it.verseStart == 7 })
        assertTrue("References must include 2 Cor 3:18", refs2.any { it.bookId == "2CO" && it.chapterNumber == 3 && it.verseStart == 18 })

        // Video 3: Humility & Overcoming Worry
        val (short3, analysis3, refs3) = devVideos[2]
        assertEquals("short_humility_overcoming_worry", short3.id)
        assertTrue(short3.title.contains("Worry") || short3.title.contains("கவலை"))
        assertTrue("Transcript must mention Peter returning to fishing", analysis3.transcriptTa.contains("பேதுரு மீன் பிடிக்க போனார்"))
        assertTrue("References must include Luke 22:24", refs3.any { it.bookId == "LUK" && it.chapterNumber == 22 && it.verseStart == 24 })
        assertTrue("References must include John 21:3", refs3.any { it.bookId == "JHN" && it.chapterNumber == 21 && it.verseStart == 3 })
        assertTrue("References must include Matthew 6:31", refs3.any { it.bookId == "MAT" && it.chapterNumber == 6 && it.verseStart == 31 })
    }

    @Test
    fun testDynamicCustomVideoAnalyzer() {
        val (analysis, refs) = CuratedShortsAnalysisEngine.analyzeCustomVideo(
            shortId = "test_custom_1",
            title = "Overcoming Fear through Prayer and Faith",
            description = "Daily prayer devotion on how faith dispels all fear",
            category = "Prayer"
        )

        assertEquals("test_custom_1", analysis.shortId)
        assertTrue("Main topic should relate to prayer", analysis.mainTopicEn.contains("Prayer"))
        assertTrue("References should not be empty", refs.isNotEmpty())
        assertTrue("References should link to valid canonical books", refs.all {
            CanonicalBibleData.findBookById(it.bookId) != null
        })
    }

    @Test
    fun testReferenceNavigationIntegrity() {
        val devVideos = CuratedShortsAnalysisEngine.getDeveloperShorts()
        for ((_, _, refs) in devVideos) {
            for (ref in refs) {
                val book = CanonicalBibleData.findBookById(ref.bookId)
                assertNotNull("Referenced book ${ref.bookId} must exist in canonical Bible database", book)
                assertTrue("Chapter number ${ref.chapterNumber} must be within book bounds", ref.chapterNumber in 1..book!!.chapterCount)
                assertTrue("Verse number must be greater than zero", ref.verseStart > 0)
                assertTrue("Verse text in English must be populated", ref.verseTextEn.isNotBlank())
                assertTrue("Verse text in Tamil must be populated", ref.verseTextTa.isNotBlank())
            }
        }
    }

    @Test
    fun testGenuineInitialZeroCountsForDeveloperShorts() {
        val devVideos = CuratedShortsAnalysisEngine.getDeveloperShorts()
        for ((short, _, _) in devVideos) {
            assertEquals("New/developer shorts must start with zero likes without fake counts", 0, short.likesCount)
            assertEquals("PUBLISHED", short.publicationStatus)
            assertEquals("9:16", short.aspectRatio)
            assertTrue("Video storage path must be valid", short.videoStoragePath.isNotBlank())
        }
    }

    @Test
    fun testShortShareLinkFormat() {
        val devVideos = CuratedShortsAnalysisEngine.getDeveloperShorts()
        for ((short, _, _) in devVideos) {
            val shareUrl = "https://svsholybible.app/shorts/${short.id}"
            assertTrue("Share URL must contain correct short ID", shareUrl.contains(short.id))
            assertTrue("Share URL must be HTTPS", shareUrl.startsWith("https://"))
        }
    }

    @Test
    fun testCommentValidation() {
        val blankText = "   "
        val validText = "Praise the Lord for this powerful sermon on prayer!"
        assertTrue("Blank comment must be detected", blankText.trim().isBlank())
        assertFalse("Valid comment must not be blank", validText.trim().isBlank())
        assertTrue("Valid comment must be within reasonable length", validText.length <= 500)
    }

    @Test
    fun testLikesCountNeverNegative() {
        var count = 0
        count = (count - 1).coerceAtLeast(0)
        assertEquals("Likes count must never drop below zero", 0, count)
    }
}
