package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TopicEntity
import com.example.data.model.TopicFavoriteEntity
import com.example.data.model.TopicHistoryEntity
import com.example.data.model.TopicNoteEntity
import com.example.data.model.TopicReferenceEntity
import com.example.data.model.TopicSearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    // --- Topics CRUD ---
    @Query("SELECT * FROM bible_topics ORDER BY titleEn ASC")
    fun getAllCustomTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM bible_topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Query("DELETE FROM bible_topics WHERE id = :id")
    suspend fun deleteTopic(id: String)

    // --- Topic References CRUD ---
    @Query("SELECT * FROM topic_references WHERE topicId = :topicId ORDER BY relevanceRank ASC, id ASC")
    fun getReferencesForTopic(topicId: String): Flow<List<TopicReferenceEntity>>

    @Query("SELECT * FROM topic_references WHERE topicId = :topicId ORDER BY relevanceRank ASC, id ASC")
    suspend fun getReferencesForTopicDirect(topicId: String): List<TopicReferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReference(ref: TopicReferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferences(refs: List<TopicReferenceEntity>)

    @Query("DELETE FROM topic_references WHERE id = :id")
    suspend fun deleteReference(id: Long)

    @Query("DELETE FROM topic_references WHERE topicId = :topicId")
    suspend fun deleteReferencesForTopic(topicId: String)

    // --- Favorites ---
    @Query("SELECT * FROM topic_favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<TopicFavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM topic_favorites WHERE topicId = :topicId)")
    fun isFavorite(topicId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM topic_favorites WHERE topicId = :topicId)")
    suspend fun isFavoriteDirect(topicId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: TopicFavoriteEntity)

    @Query("DELETE FROM topic_favorites WHERE topicId = :topicId")
    suspend fun removeFavorite(topicId: String)

    // --- Topic History ---
    @Query("SELECT * FROM topic_history ORDER BY viewedAt DESC LIMIT 20")
    fun getRecentTopicHistory(): Flow<List<TopicHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordTopicView(history: TopicHistoryEntity)

    // --- Topic Search History ---
    @Query("SELECT DISTINCT query FROM topic_search_history ORDER BY timestamp DESC LIMIT 15")
    fun getRecentSearchQueries(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordSearchQuery(search: TopicSearchHistoryEntity)

    @Query("DELETE FROM topic_search_history WHERE query = :query")
    suspend fun deleteSearchQuery(query: String)

    @Query("DELETE FROM topic_search_history")
    suspend fun clearSearchHistory()

    // --- Personal Topic Study Notes ---
    @Query("SELECT * FROM topic_study_notes WHERE topicId = :topicId ORDER BY updatedAt DESC")
    fun getNotesForTopic(topicId: String): Flow<List<TopicNoteEntity>>

    @Query("SELECT * FROM topic_study_notes WHERE topicId = :topicId ORDER BY updatedAt DESC")
    suspend fun getNotesForTopicDirect(topicId: String): List<TopicNoteEntity>

    @Query("SELECT * FROM topic_study_notes ORDER BY updatedAt DESC")
    fun getAllTopicNotes(): Flow<List<TopicNoteEntity>>

    @Query("SELECT COUNT(*) FROM topic_study_notes WHERE topicId = :topicId")
    suspend fun getTopicNotesCount(topicId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopicNote(note: TopicNoteEntity): Long

    @Update
    suspend fun updateTopicNote(note: TopicNoteEntity)

    @Delete
    suspend fun deleteTopicNote(note: TopicNoteEntity)

    @Query("DELETE FROM topic_study_notes WHERE id = :id")
    suspend fun deleteTopicNoteById(id: Long)
}
