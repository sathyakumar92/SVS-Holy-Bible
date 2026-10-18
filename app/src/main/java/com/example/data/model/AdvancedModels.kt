package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// ==========================================
// FEATURE 1: ADVANCED BIBLE STUDY NOTEBOOK
// ==========================================

@Entity(tableName = "notebook_folders")
data class NotebookFolder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameTa: String,
    val colorHex: String = "#173F8A",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notebook_notes")
data class NotebookNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long? = null,
    val title: String,
    val content: String,
    // Structured study fields
    val observation: String = "",
    val interpretation: String = "",
    val application: String = "",
    val prayer: String = "",
    val tags: String = "", // comma-separated tags
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "note_verse_refs")
data class NoteVerseRef(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val translationId: String, // "KJV", "TAMIL_BSI"
    val bookId: String,
    val bookName: String,
    val chapterNumber: Int,
    val startVerse: Int,
    val endVerse: Int? = null,
    val verseText: String = ""
)

// ==========================================
// FEATURE 2: SMART READING PLANS & PROGRESS
// ==========================================

@Entity(tableName = "smart_plans")
data class SmartPlanEntity(
    @PrimaryKey val id: String, // e.g. "plan_bible_one_year"
    val titleEn: String,
    val titleTa: String,
    val descriptionEn: String,
    val descriptionTa: String,
    val durationDays: Int,
    val category: String, // "Full Bible", "New Testament", "Gospels", "Psalms", "Custom"
    val isCustom: Boolean = false,
    val isStarted: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val startDateTimestamp: Long? = null,
    val completedDateTimestamp: Long? = null,
    val reminderTimeMinutes: Int = 420 // 7:00 AM (minutes from midnight)
)

@Entity(tableName = "smart_plan_assignments")
data class SmartPlanAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: String,
    val dayNumber: Int,
    val titleEn: String,
    val titleTa: String,
    val passagesJson: String // e.g. "GEN 1-3, MAT 1"
)

@Entity(tableName = "plan_day_completions")
data class PlanDayCompletion(
    @PrimaryKey val completionKey: String, // "${planId}_day_${day}"
    val planId: String,
    val dayNumber: Int,
    val isCompleted: Boolean,
    val completedTimestamp: Long = System.currentTimeMillis(),
    val localDateString: String // "YYYY-MM-DD" for streak tracking
)

// ==========================================
// FEATURE 3: BIBLE VERSE MEMORIZATION
// ==========================================

enum class SrsStatus {
    NEW,
    LEARNING,
    DUE_FOR_REVIEW,
    MEMORIZED
}

@Entity(tableName = "memorized_verses")
data class MemorizedVerse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val referenceDisplayEn: String,
    val referenceDisplayTa: String,
    val verseTextEn: String,
    val verseTextTa: String,
    val translationId: String, // "KJV", "TAMIL_BSI"
    val category: String = "General",
    val isFavorite: Boolean = false,
    // Spaced Repetition (SM-2 algorithm parameters)
    val status: SrsStatus = SrsStatus.NEW,
    val repetitions: Int = 0,
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val lastReviewTimestamp: Long? = null,
    val nextReviewTimestamp: Long = System.currentTimeMillis()
)

// ==========================================
// FEATURE 4: PERSONAL PRAYER JOURNAL
// ==========================================

enum class PrayerStatus {
    ACTIVE,
    ONGOING,
    ANSWERED,
    ARCHIVED
}

@Entity(tableName = "prayer_entries")
data class PrayerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String = "Personal", // "Personal", "Family", "Health", "Work & Provision", "Ministry", "Gratitude"
    val status: PrayerStatus = PrayerStatus.ACTIVE,
    val isFavorite: Boolean = false,
    val targetDateTimestamp: Long? = null,
    val attachedScriptureRef: String = "",
    val attachedScriptureText: String = "",
    // Answered testimony fields
    val answeredDateTimestamp: Long? = null,
    val answeredTestimony: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_updates")
data class PrayerUpdate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prayerId: Long,
    val updateText: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// FEATURE 5: SERMON NOTES & LESSON BUILDER
// ==========================================

enum class SermonDocType {
    SERMON_OUTLINE,
    BIBLE_LESSON,
    DEVOTIONAL,
    CUSTOM_OUTLINE
}

enum class SermonDocStatus {
    DRAFT,
    IN_PROGRESS,
    COMPLETED
}

@Entity(tableName = "sermon_documents")
data class SermonDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitleOrTheme: String = "",
    val docType: SermonDocType = SermonDocType.SERMON_OUTLINE,
    val status: SermonDocStatus = SermonDocStatus.DRAFT,
    val audience: String = "",
    val mainPassageRef: String = "",
    val mainPassageText: String = "",
    val tags: String = "",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sermon_sections")
data class SermonSection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val sectionOrder: Int,
    val sectionTitle: String,
    val content: String,
    val attachedReferences: String = "", // e.g. "JHN 3:16, PSA 23:1"
    val isCollapsed: Boolean = false
)

// ==========================================
// FEATURE 6: INTERACTIVE BIBLE TIMELINE & MAPS
// ==========================================

data class BiblicalPeriod(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val approximateEraEn: String,
    val approximateEraTa: String,
    val descriptionEn: String,
    val descriptionTa: String,
    val colorHex: String
)

@Entity(tableName = "timeline_events")
data class TimelineEvent(
    @PrimaryKey val id: String,
    val periodId: String,
    val chronologicalOrder: Int,
    val titleEn: String,
    val titleTa: String,
    val approximateDate: String, // e.g. "~2000 BC", "~30 AD"
    val dateUncertaintyNote: String,
    val summaryEn: String,
    val summaryTa: String,
    val historicalContextEn: String,
    val historicalContextTa: String,
    val primaryScriptureRef: String,
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val keyFigures: String, // e.g. "Abraham, Isaac"
    val linkedLocationId: String? = null
)

@Entity(tableName = "biblical_locations")
data class BiblicalLocation(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameTa: String,
    val modernName: String,
    val latitude: Double,
    val longitude: Double,
    val period: String,
    val scriptureReferences: String,
    val descriptionEn: String,
    val descriptionTa: String,
    val isLocationCertain: Boolean = true
)

// ==========================================
// FEATURE 7: IMMERSIVE LISTENING MODE
// ==========================================

@Entity(tableName = "listening_history_sessions")
data class ListeningSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val playbackPositionSeconds: Int,
    val totalDurationSeconds: Int,
    val completedPercentage: Float,
    val timestamp: Long = System.currentTimeMillis()
)
