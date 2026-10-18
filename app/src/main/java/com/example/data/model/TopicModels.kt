package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Enhanced Bible Topic entity supporting Room persistence, admin authoring,
 * bilingual Tamil/English content, verified references, and deep theological explanations.
 */
@Entity(tableName = "bible_topics")
data class TopicEntity(
    @PrimaryKey val id: String, // e.g. "faith", "prayer", "custom_topic_123"
    val slug: String,
    val titleEn: String,
    val titleTa: String,
    val shortDescriptionEn: String,
    val shortDescriptionTa: String,
    val category: String, // e.g. "Spiritual Life", "Christian Living"
    val categoryTa: String,
    val keywords: String = "", // comma-separated keywords
    val synonyms: String = "", // comma-separated synonyms
    val testamentFocus: String = "ALL", // "ALL", "OT", "NT"
    val introductionEn: String = "",
    val introductionTa: String = "",
    val detailedExplanationEn: String = "",
    val detailedExplanationTa: String = "",
    val biblicalPeopleEn: String = "",
    val biblicalPeopleTa: String = "",
    val practicalApplicationsEn: String = "",
    val practicalApplicationsTa: String = "",
    val commonMisunderstandingsEn: String = "",
    val commonMisunderstandingsTa: String = "",
    val keyLessonsEn: String = "",
    val keyLessonsTa: String = "",
    val status: String = "PUBLISHED", // "PUBLISHED", "DRAFT", "ARCHIVED"
    val isFeatured: Boolean = false,
    val isPopular: Boolean = false,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "topic_references")
data class TopicReferenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: String,
    val bookId: String,
    val bookNameEn: String,
    val bookNameTa: String,
    val chapterNumber: Int,
    val verseStart: Int,
    val verseEnd: Int? = null,
    val sectionGroup: String = "KEY_VERSES", // "KEY_VERSES", "PROMISES", "BIBLICAL_EXAMPLES", "WARNINGS_INSTRUCTIONS", "OLD_TESTAMENT", "NEW_TESTAMENT", "TEACHINGS", "DAILY_LIFE"
    val referenceEn: String,
    val referenceTa: String,
    val verseTextEn: String,
    val verseTextTa: String,
    val briefExplanationEn: String = "",
    val briefExplanationTa: String = "",
    val biblicalContextEn: String = "",
    val biblicalContextTa: String = "",
    val whyRelatesEn: String = "",
    val whyRelatesTa: String = "",
    val crossReferences: String = "",
    val relevanceRank: Int = 1,
    val isVerified: Boolean = true
)

@Entity(tableName = "topic_favorites")
data class TopicFavoriteEntity(
    @PrimaryKey val topicId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "topic_history")
data class TopicHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: String,
    val viewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "topic_search_history")
data class TopicSearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val languageCode: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Personal Study Notes created by the user for a specific topic and/or specific verse.
 * Stored securely and privately in device Room SQLite database.
 */
@Entity(tableName = "topic_study_notes")
data class TopicNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: String,
    val associatedVerseReference: String = "", // e.g. "Hebrews 11:1" or blank for general topic reflections
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Authentic Hebrew or Greek word study with meanings and theological context
 */
data class OriginalWordStudy(
    val language: String, // "Hebrew" or "Greek"
    val originalWord: String, // e.g. "אֱמוּנָה" or "πίστις"
    val transliteration: String, // e.g. "Emunah" or "Pistis"
    val literalMeaningEn: String,
    val literalMeaningTa: String,
    val theologicalSignificanceEn: String,
    val theologicalSignificanceTa: String,
    val primaryScriptureEn: String,
    val primaryScriptureTa: String
)

/**
 * Chronological narrative entry for the Topic Timeline
 */
data class TopicTimelineEntry(
    val id: String,
    val eraEn: String, // e.g. "Creation & Patriarchs", "Exodus & Law", "Kingdom & Temple", "Prophets", "Christ & Gospels", "Apostolic Era", "Eternity & Revelation"
    val eraTa: String,
    val eraOrder: Int,
    val bookId: String,
    val chapterNumber: Int,
    val verseStart: Int,
    val verseEnd: Int? = null,
    val referenceEn: String,
    val referenceTa: String,
    val verseTextEn: String,
    val verseTextTa: String,
    val eventTitleEn: String,
    val eventTitleTa: String,
    val narrativeExplanationEn: String,
    val narrativeExplanationTa: String,
    val testamentsConnectionEn: String = "",
    val testamentsConnectionTa: String = ""
)

/**
 * Message model for the AI-Powered Bible Topic Assistant
 */
data class TopicAiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val contentEn: String,
    val contentTa: String = "",
    val citedReferences: List<TopicReferenceEntity> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = true,
    val isOfflineGrounded: Boolean = false
)

/**
 * Curated relationship between topics with reason
 */
data class TopicRelationship(
    val relatedTopicId: String,
    val connectionTypeEn: String, // e.g. "Foundational", "Complementary", "Practical Outcome"
    val connectionTypeTa: String,
    val explanationEn: String,
    val explanationTa: String,
    val rank: Int = 1
)

/**
 * Rich runtime presentation model for a topic study
 */
data class RichTopicStudy(
    val topic: TopicEntity,
    val isFavorite: Boolean = false,
    val allReferences: List<TopicReferenceEntity> = emptyList(),
    val keyVerses: List<TopicReferenceEntity> = emptyList(),
    val promises: List<TopicReferenceEntity> = emptyList(),
    val biblicalExamples: List<TopicReferenceEntity> = emptyList(),
    val warningsAndInstructions: List<TopicReferenceEntity> = emptyList(),
    val oldTestamentPassages: List<TopicReferenceEntity> = emptyList(),
    val newTestamentPassages: List<TopicReferenceEntity> = emptyList(),
    val teachings: List<TopicReferenceEntity> = emptyList(),
    val dailyLifeVerses: List<TopicReferenceEntity> = emptyList(),
    val relatedTopics: List<TopicEntity> = emptyList(),
    val wordStudies: List<OriginalWordStudy> = emptyList(),
    val timelineEntries: List<TopicTimelineEntry> = emptyList(),
    val userNotesCount: Int = 0
)
