package com.example.data.repository

import com.example.data.datasource.CompleteBibleTopicsData
import com.example.data.datasource.TopicDeepStudyData
import com.example.data.datasource.TopicRelationshipData
import com.example.data.datasource.TopicTimelineEngine
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.*

class TopicRepository(private val database: AppDatabase) {

    private val topicDao = database.topicDao()

    // 1. All Topics Flow: combines in-memory verified curated topics + user custom created/edited topics
    fun getAllTopics(): Flow<List<TopicEntity>> {
        return topicDao.getAllCustomTopics().map { customList ->
            val customMap = customList.associateBy { it.id }
            val curated = CompleteBibleTopicsData.getAllCuratedTopics()
            val merged = mutableListOf<TopicEntity>()

            // Add curated (overridden by custom if edited)
            curated.forEach { c ->
                if (customMap.containsKey(c.id)) {
                    merged.add(customMap[c.id]!!)
                } else {
                    merged.add(c)
                }
            }

            // Add new custom topics created by user/admin
            customList.forEach { custom ->
                if (!curated.any { it.id == custom.id }) {
                    merged.add(custom)
                }
            }

            merged.sortedBy { it.titleEn }
        }
    }

    // 2. Get Single Topic by ID
    suspend fun getTopicById(topicId: String): TopicEntity? {
        val custom = topicDao.getTopicById(topicId)
        if (custom != null) return custom
        return CompleteBibleTopicsData.getAllCuratedTopics().firstOrNull { it.id == topicId }
    }

    // 3. Get References for Topic
    suspend fun getReferencesForTopic(topicId: String): List<TopicReferenceEntity> {
        val dbRefs = topicDao.getReferencesForTopicDirect(topicId)
        if (dbRefs.isNotEmpty()) return dbRefs
        return CompleteBibleTopicsData.getCuratedReferencesForTopic(topicId)
    }

    // 4. Get Rich Topic Study (complete details + grouped subsections + deep study + timeline)
    suspend fun getRichTopicStudy(topicId: String): RichTopicStudy? {
        val topic = getTopicById(topicId) ?: return null
        val refs = getReferencesForTopic(topicId)
        val isFav = topicDao.isFavoriteDirect(topicId)

        // Find curated related topics
        val allTopics = CompleteBibleTopicsData.getAllCuratedTopics()
        val related = TopicRelationshipData.getRelatedTopics(topicId, allTopics)

        // Generate scholarly original language word studies
        val wordStudies = TopicDeepStudyData.getOriginalWordStudies(topicId)

        // Generate chronological biblical narrative timeline
        val timeline = TopicTimelineEngine.generateTimelineForTopic(topicId, refs)

        // Get notes count
        val notesCount = topicDao.getTopicNotesCount(topicId)

        return RichTopicStudy(
            topic = topic,
            isFavorite = isFav,
            allReferences = refs,
            keyVerses = refs.filter { it.sectionGroup == "KEY_VERSES" },
            promises = refs.filter { it.sectionGroup == "PROMISES" },
            biblicalExamples = refs.filter { it.sectionGroup == "BIBLICAL_EXAMPLES" },
            warningsAndInstructions = refs.filter { it.sectionGroup == "WARNINGS_INSTRUCTIONS" },
            oldTestamentPassages = refs.filter { it.sectionGroup == "OLD_TESTAMENT" },
            newTestamentPassages = refs.filter { it.sectionGroup == "NEW_TESTAMENT" },
            teachings = refs.filter { it.sectionGroup == "TEACHINGS" },
            dailyLifeVerses = refs.filter { it.sectionGroup == "DAILY_LIFE" },
            relatedTopics = related,
            wordStudies = wordStudies,
            timelineEntries = timeline,
            userNotesCount = notesCount
        )
    }

    // 5. Favorites
    fun getFavoriteTopicIds(): Flow<Set<String>> {
        return topicDao.getAllFavorites().map { list -> list.map { it.topicId }.toSet() }
    }

    suspend fun toggleFavorite(topicId: String): Boolean {
        val isFav = topicDao.isFavoriteDirect(topicId)
        return if (isFav) {
            topicDao.removeFavorite(topicId)
            false
        } else {
            topicDao.addFavorite(TopicFavoriteEntity(topicId = topicId))
            true
        }
    }

    suspend fun isFavorite(topicId: String): Boolean = topicDao.isFavoriteDirect(topicId)

    // 6. History
    fun getRecentlyViewedTopics(): Flow<List<TopicEntity>> {
        return topicDao.getRecentTopicHistory().map { historyList ->
            val all = CompleteBibleTopicsData.getAllCuratedTopics().associateBy { it.id }
            val result = mutableListOf<TopicEntity>()
            val seen = mutableSetOf<String>()
            for (h in historyList) {
                if (!seen.contains(h.topicId)) {
                    val topic = all[h.topicId] ?: topicDao.getTopicById(h.topicId)
                    if (topic != null) {
                        result.add(topic)
                        seen.add(h.topicId)
                    }
                }
            }
            result
        }
    }

    suspend fun recordTopicView(topicId: String) {
        topicDao.recordTopicView(TopicHistoryEntity(topicId = topicId))
    }

    // 7. Search History
    fun getRecentSearchQueries(): Flow<List<String>> = topicDao.getRecentSearchQueries()

    suspend fun recordSearchQuery(query: String, languageCode: String) {
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            topicDao.recordSearchQuery(TopicSearchHistoryEntity(query = trimmed, languageCode = languageCode))
        }
    }

    suspend fun deleteSearchQuery(query: String) = topicDao.deleteSearchQuery(query)

    suspend fun clearSearchHistory() = topicDao.clearSearchHistory()

    // 8. Personal Topic Study Notes
    fun getNotesForTopic(topicId: String): Flow<List<TopicNoteEntity>> = topicDao.getNotesForTopic(topicId)

    fun getAllTopicNotes(): Flow<List<TopicNoteEntity>> = topicDao.getAllTopicNotes()

    suspend fun saveTopicNote(
        topicId: String,
        title: String,
        content: String,
        associatedVerseReference: String = ""
    ): Long {
        val note = TopicNoteEntity(
            topicId = topicId,
            associatedVerseReference = associatedVerseReference,
            title = title.trim(),
            content = content.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return topicDao.insertTopicNote(note)
    }

    suspend fun updateTopicNote(note: TopicNoteEntity) {
        topicDao.updateTopicNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTopicNote(note: TopicNoteEntity) {
        topicDao.deleteTopicNote(note)
    }

    suspend fun deleteTopicNoteById(noteId: Long) {
        topicDao.deleteTopicNoteById(noteId)
    }

    // 9. Admin Content Management (CRUD)
    suspend fun saveTopic(topic: TopicEntity) {
        topicDao.insertTopic(topic)
    }

    suspend fun deleteTopic(topicId: String) {
        topicDao.deleteTopic(topicId)
        topicDao.deleteReferencesForTopic(topicId)
    }

    suspend fun addTopicReference(ref: TopicReferenceEntity) {
        topicDao.insertReference(ref)
    }

    suspend fun deleteTopicReference(refId: Long) {
        topicDao.deleteReference(refId)
    }
}
