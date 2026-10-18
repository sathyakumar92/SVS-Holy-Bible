package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Rebuilt Bible Shorts Data Models (Section 11 of specification):
 * - Strict 9:16 portrait video metadata
 * - Exact administrator-provided title preservation
 * - Room persistence for shorts, intelligent analysis, verified Bible references, likes, comments, and saves
 */

@Entity(
    tableName = "shorts",
    indices = [
        Index(value = ["publicationStatus"]),
        Index(value = ["createdAt"])
    ]
)
data class ShortEntity(
    @PrimaryKey
    val id: String,
    val title: String,                           // Exact administrator-provided title
    val description: String = "",
    val category: String = "Sermon",
    val videoStoragePath: String,                // Path or URI to actual video file
    val videoRawResId: Int? = null,              // Optional raw resource ID for bundled media
    val thumbnailStoragePath: String? = null,
    val aspectRatio: String = "9:16",
    val durationSeconds: Int = 0,
    val fileSizeBytes: Long = 0L,
    val videoMimeType: String = "video/mp4",
    val audioAvailable: Boolean = true,
    val uploadStatus: String = "COMPLETED",      // UPLOADING, COMPLETED, FAILED
    val publicationStatus: String = "PUBLISHED", // DRAFT, PUBLISHED, ARCHIVED
    val createdByAdminId: String = "admin_developer",
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val publishedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shorts_analysis",
    indices = [
        Index(value = ["shortId"], unique = true)
    ]
)
data class ShortAnalysisEntity(
    @PrimaryKey
    val id: String,
    val shortId: String,
    val transcriptEn: String = "",
    val transcriptTa: String = "",
    val summaryEn: String = "",
    val summaryTa: String = "",
    val mainTopicEn: String = "",
    val mainTopicTa: String = "",
    val secondaryTopicsEn: String = "",          // Comma-separated
    val secondaryTopicsTa: String = "",
    val biblicalCharactersEn: String = "",
    val biblicalCharactersTa: String = "",
    val spiritualTeachingsEn: String = "",
    val spiritualTeachingsTa: String = "",
    val analysisStatus: String = "COMPLETED",    // PENDING, PROCESSING, COMPLETED, FAILED
    val processingError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shorts_bible_references",
    indices = [
        Index(value = ["shortId"]),
        Index(value = ["bookId", "chapterNumber", "verseStart"])
    ]
)
data class ShortBibleReferenceEntity(
    @PrimaryKey
    val id: String,
    val shortId: String,
    val analysisId: String,
    val bookId: String,
    val bookNameEn: String,
    val bookNameTa: String,
    val chapterNumber: Int,
    val verseStart: Int,
    val verseEnd: Int = verseStart,
    val referenceType: String,                   // "QUOTATION", "INCIDENT", "SUPPORTING", "THEMATIC"
    val relevanceScore: Float = 1.0f,
    val verseTextEn: String,
    val verseTextTa: String,
    val translationName: String = "KJV / BSI Union",
    val explanationEn: String,
    val explanationTa: String,
    val connectionEn: String,
    val connectionTa: String,
    val displayOrder: Int = 0
)

@Entity(
    tableName = "shorts_likes",
    indices = [
        Index(value = ["shortId", "userId"], unique = true)
    ]
)
data class ShortLikeEntity(
    @PrimaryKey
    val id: String,
    val shortId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shorts_comments",
    indices = [
        Index(value = ["shortId"])
    ]
)
data class ShortCommentEntity(
    @PrimaryKey
    val id: String,
    val shortId: String,
    val userId: String,
    val userName: String,
    val commentText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shorts_saves",
    indices = [
        Index(value = ["shortId", "userId"], unique = true)
    ]
)
data class ShortSaveEntity(
    @PrimaryKey
    val id: String,
    val shortId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Composite domain object for a Short with all intelligent Bible analysis & user states
 */
data class ShortWithDetails(
    val short: ShortEntity,
    val analysis: ShortAnalysisEntity?,
    val references: List<ShortBibleReferenceEntity>,
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    val commentsCount: Int = 0
)
