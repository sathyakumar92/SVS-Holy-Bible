package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Testament {
    OLD_TESTAMENT,
    NEW_TESTAMENT
}

data class BibleBook(
    val id: String,              // e.g. "GEN", "MAT", "JHN"
    val order: Int,             // 1..66
    val testament: Testament,
    val englishName: String,
    val tamilName: String,
    val chapterCount: Int,
    val abbreviation: String,
    val tamilAbbreviation: String
)

@Entity(
    tableName = "bible_verses",
    indices = [
        Index(
            value = ["editionId", "languageCode", "bookId", "chapterNumber", "verseNumber"],
            unique = true
        )
    ]
)
data class BibleVerse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val editionId: String,       // "KJV", "TAMIL_BSI"
    val languageCode: String,    // "en", "ta"
    val bookId: String,          // "GEN", "JHN"
    val bookName: String,        // "Genesis", "யோவான்"
    val chapterNumber: Int,
    val verseNumber: Int,
    val verseText: String,
    val normalizedText: String   // search optimized
)

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val reference: String,
    val verseText: String,
    val languageCode: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "verse_highlights")
data class VerseHighlight(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val colorHex: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bible_notes")
data class BibleNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val reference: String,
    val noteTitle: String,
    val noteContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_history")
data class ReadingHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "plan_progress")
data class PlanProgress(
    @PrimaryKey val progressKey: String, // "${planId}_day_${day}"
    val planId: String,
    val dayNumber: Int,
    val isCompleted: Boolean,
    val completedTimestamp: Long
)

@Entity(tableName = "quiz_scores")
data class QuizScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)
