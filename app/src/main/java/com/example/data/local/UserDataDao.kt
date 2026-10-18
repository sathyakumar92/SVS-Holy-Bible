package com.example.data.local

import androidx.room.*
import com.example.data.model.Bookmark
import com.example.data.model.BibleNote
import com.example.data.model.PlanProgress
import com.example.data.model.QuizScore
import com.example.data.model.ReadingHistory
import com.example.data.model.VerseHighlight
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId AND chapterNumber = :chapter AND verseNumber = :verse LIMIT 1")
    suspend fun getBookmark(bookId: String, chapter: Int, verse: Int): Bookmark?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId AND chapterNumber = :chapter AND verseNumber = :verse")
    suspend fun deleteBookmarkByVerse(bookId: String, chapter: Int, verse: Int)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)
}

@Dao
interface HighlightDao {
    @Query("SELECT * FROM verse_highlights WHERE bookId = :bookId AND chapterNumber = :chapter")
    fun getHighlightsForChapter(bookId: String, chapter: Int): Flow<List<VerseHighlight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: VerseHighlight)

    @Query("DELETE FROM verse_highlights WHERE bookId = :bookId AND chapterNumber = :chapter AND verseNumber = :verse")
    suspend fun removeHighlight(bookId: String, chapter: Int, verse: Int)

    @Query("SELECT * FROM verse_highlights ORDER BY timestamp DESC")
    fun getAllHighlights(): Flow<List<VerseHighlight>>
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM bible_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<BibleNote>>

    @Query("SELECT * FROM bible_notes WHERE bookId = :bookId AND chapterNumber = :chapter AND verseNumber = :verse LIMIT 1")
    suspend fun getNoteForVerse(bookId: String, chapter: Int, verse: Int): BibleNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNote(note: BibleNote): Long

    @Delete
    suspend fun deleteNote(note: BibleNote)

    @Query("DELETE FROM bible_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC")
    fun getAllReadingHistory(): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC")
    suspend fun getAllReadingHistoryDirect(): List<ReadingHistory>

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT 1")
    fun getLastRead(): Flow<ReadingHistory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordHistory(history: ReadingHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordHistoryList(histories: List<ReadingHistory>)

    @Query("DELETE FROM reading_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM reading_history")
    suspend fun clearHistory()
}


@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_progress WHERE planId = :planId")
    fun getPlanProgress(planId: String): Flow<List<PlanProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPortionCompleted(progress: PlanProgress)

    @Query("DELETE FROM plan_progress WHERE planId = :planId")
    suspend fun resetPlan(planId: String)
}

@Dao
interface QuizScoreDao {
    @Query("SELECT * FROM quiz_scores ORDER BY timestamp DESC LIMIT 10")
    fun getRecentScores(): Flow<List<QuizScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: QuizScore)
}
