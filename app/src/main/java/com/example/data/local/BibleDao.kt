package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BibleVerse
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {

    @Query("SELECT * FROM bible_verses WHERE bookId = :bookId AND chapterNumber = :chapter AND languageCode = :languageCode GROUP BY verseNumber ORDER BY verseNumber ASC")
    fun getVersesForChapter(bookId: String, chapter: Int, languageCode: String): Flow<List<BibleVerse>>

    @Query("SELECT * FROM bible_verses WHERE bookId = :bookId AND chapterNumber = :chapter AND languageCode = :languageCode GROUP BY verseNumber ORDER BY verseNumber ASC")
    suspend fun getVersesForChapterDirect(bookId: String, chapter: Int, languageCode: String): List<BibleVerse>

    @Query("SELECT * FROM bible_verses WHERE bookId = :bookId AND chapterNumber = :chapter AND verseNumber = :verse AND languageCode = :languageCode LIMIT 1")
    suspend fun getSingleVerse(bookId: String, chapter: Int, verse: Int, languageCode: String): BibleVerse?

    @Query("""
        SELECT * FROM bible_verses 
        WHERE (normalizedText LIKE '%' || :query || '%' OR verseText LIKE '%' || :query || '%')
        AND (:testamentFilter IS NULL OR :testamentFilter = '' OR 
             (:testamentFilter = 'OT' AND bookId IN ('GEN','EXO','LEV','NUM','DEU','JOS','JDG','RUT','1SA','2SA','1KI','2KI','1CH','2CH','EZR','NEH','EST','JOB','PSA','PRO','ECC','SNG','ISA','JER','LAM','EZK','DAN','HOS','JOL','AMO','OBA','JON','MIC','NAM','HAB','ZEP','HAG','ZEC','MAL')) OR
             (:testamentFilter = 'NT' AND bookId IN ('MAT','MRK','LUK','JHN','ACT','ROM','1CO','2CO','GAL','EPH','PHP','COL','1TH','2TH','1TI','2TI','TIT','PHM','HEB','JAS','1PE','2PE','1JN','2JN','3JN','JUD','REV')))
        AND (:bookFilter IS NULL OR :bookFilter = '' OR bookId = :bookFilter)
        AND (:fromChapter <= 0 OR chapterNumber >= :fromChapter)
        AND (:toChapter <= 0 OR chapterNumber <= :toChapter)
        AND languageCode = :languageCode
        GROUP BY bookId, chapterNumber, verseNumber
        LIMIT 300
    """)
    fun searchVerses(
        query: String,
        languageCode: String,
        testamentFilter: String?,
        bookFilter: String?,
        fromChapter: Int = 0,
        toChapter: Int = 0
    ): Flow<List<BibleVerse>>

    @Query("""
        SELECT * FROM bible_verses 
        WHERE (:languageCode = 'both' OR languageCode = :languageCode)
        AND (:testamentFilter IS NULL OR :testamentFilter = '' OR 
             (:testamentFilter = 'OT' AND bookId IN ('GEN','EXO','LEV','NUM','DEU','JOS','JDG','RUT','1SA','2SA','1KI','2KI','1CH','2CH','EZR','NEH','EST','JOB','PSA','PRO','ECC','SNG','ISA','JER','LAM','EZK','DAN','HOS','JOL','AMO','OBA','JON','MIC','NAM','HAB','ZEP','HAG','ZEC','MAL')) OR
             (:testamentFilter = 'NT' AND bookId IN ('MAT','MRK','LUK','JHN','ACT','ROM','1CO','2CO','GAL','EPH','PHP','COL','1TH','2TH','1TI','2TI','TIT','PHM','HEB','JAS','1PE','2PE','1JN','2JN','3JN','JUD','REV')))
        AND (:bookFilter IS NULL OR :bookFilter = '' OR bookId = :bookFilter)
        AND (:fromChapter <= 0 OR chapterNumber >= :fromChapter)
        AND (:toChapter <= 0 OR chapterNumber <= :toChapter)
        AND (:fromVerse <= 0 OR verseNumber >= :fromVerse)
        AND (:toVerse <= 0 OR verseNumber <= :toVerse)
        GROUP BY editionId, languageCode, bookId, chapterNumber, verseNumber
        ORDER BY id ASC
    """)
    suspend fun getVersesForAdvancedSearch(
        languageCode: String,
        testamentFilter: String?,
        bookFilter: String?,
        fromChapter: Int,
        toChapter: Int,
        fromVerse: Int = 0,
        toVerse: Int = 0
    ): List<BibleVerse>

    @Query("SELECT * FROM bible_verses WHERE bookId = :bookId AND chapterNumber = :chapter AND verseNumber = :verse AND languageCode = :languageCode LIMIT 1")
    suspend fun getVerseByBookChapterVerse(bookId: String, chapter: Int, verse: Int, languageCode: String): BibleVerse?


    @Query("""
        SELECT * FROM bible_verses 
        WHERE bookId = :bookId 
        AND chapterNumber = :chapter 
        AND (:verseNumber <= 0 OR verseNumber = :verseNumber)
        AND languageCode = :languageCode
        ORDER BY verseNumber ASC
    """)
    suspend fun getVersesForReference(
        bookId: String,
        chapter: Int,
        verseNumber: Int,
        languageCode: String
    ): List<BibleVerse>

    @Query("SELECT * FROM bible_verses WHERE languageCode = :languageCode")
    suspend fun getAllVersesByLanguageDirect(languageCode: String): List<BibleVerse>

    @Query("SELECT COUNT(*) FROM bible_verses")
    suspend fun getVerseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleVerse>)
}
