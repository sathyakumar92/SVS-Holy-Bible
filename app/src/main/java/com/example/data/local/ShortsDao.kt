package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortsDao {

    @Query("SELECT * FROM shorts WHERE publicationStatus = 'PUBLISHED' ORDER BY publishedAt DESC, createdAt DESC")
    fun getPublishedShorts(): Flow<List<ShortEntity>>

    @Query("SELECT * FROM shorts ORDER BY createdAt DESC")
    fun getAllShortsAdmin(): Flow<List<ShortEntity>>

    @Query("SELECT * FROM shorts WHERE id = :id LIMIT 1")
    suspend fun getShortById(id: String): ShortEntity?

    @Query("SELECT * FROM shorts_analysis WHERE shortId = :shortId LIMIT 1")
    fun getAnalysisForShort(shortId: String): Flow<ShortAnalysisEntity?>

    @Query("SELECT * FROM shorts_analysis WHERE shortId = :shortId LIMIT 1")
    suspend fun getAnalysisForShortDirect(shortId: String): ShortAnalysisEntity?

    @Query("SELECT * FROM shorts_bible_references WHERE shortId = :shortId ORDER BY displayOrder ASC")
    fun getReferencesForShort(shortId: String): Flow<List<ShortBibleReferenceEntity>>

    @Query("SELECT * FROM shorts_bible_references WHERE shortId = :shortId ORDER BY displayOrder ASC")
    suspend fun getReferencesForShortDirect(shortId: String): List<ShortBibleReferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShort(short: ShortEntity)

    @Update
    suspend fun updateShort(short: ShortEntity)

    @Delete
    suspend fun deleteShort(short: ShortEntity)

    @Query("DELETE FROM shorts WHERE id = :shortId")
    suspend fun deleteShortById(shortId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: ShortAnalysisEntity)

    @Query("DELETE FROM shorts_analysis WHERE shortId = :shortId")
    suspend fun deleteAnalysisByShortId(shortId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferences(references: List<ShortBibleReferenceEntity>)

    @Query("DELETE FROM shorts_bible_references WHERE shortId = :shortId")
    suspend fun deleteReferencesByShortId(shortId: String)

    // Likes
    @Query("SELECT COUNT(*) > 0 FROM shorts_likes WHERE shortId = :shortId AND userId = :userId")
    suspend fun isShortLiked(shortId: String, userId: String): Boolean

    @Query("SELECT COUNT(*) > 0 FROM shorts_likes WHERE shortId = :shortId AND userId = :userId")
    fun isShortLikedFlow(shortId: String, userId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM shorts_likes WHERE shortId = :shortId")
    suspend fun getLikesCountForShort(shortId: String): Int

    @Query("SELECT COUNT(*) FROM shorts_likes WHERE shortId = :shortId")
    fun getLikesCountFlow(shortId: String): Flow<Int>

    @Update
    suspend fun updateShortLikes(short: ShortEntity)

    @Query("UPDATE shorts SET likesCount = :count WHERE id = :shortId")
    suspend fun updateLikesCount(shortId: String, count: Int)

    @Query("UPDATE shorts SET thumbnailStoragePath = :thumbPath WHERE id = :shortId")
    suspend fun updateThumbnail(shortId: String, thumbPath: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: ShortLikeEntity)

    @Query("DELETE FROM shorts_likes WHERE shortId = :shortId AND userId = :userId")
    suspend fun deleteLike(shortId: String, userId: String)

    // Saves
    @Query("SELECT COUNT(*) > 0 FROM shorts_saves WHERE shortId = :shortId AND userId = :userId")
    suspend fun isShortSaved(shortId: String, userId: String): Boolean

    @Query("SELECT COUNT(*) > 0 FROM shorts_saves WHERE shortId = :shortId AND userId = :userId")
    fun isShortSavedFlow(shortId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSave(save: ShortSaveEntity)

    @Query("DELETE FROM shorts_saves WHERE shortId = :shortId AND userId = :userId")
    suspend fun deleteSave(shortId: String, userId: String)

    @Query("SELECT s.* FROM shorts s INNER JOIN shorts_saves v ON s.id = v.shortId WHERE v.userId = :userId ORDER BY v.createdAt DESC")
    fun getSavedShorts(userId: String): Flow<List<ShortEntity>>

    // Comments
    @Query("SELECT * FROM shorts_comments WHERE shortId = :shortId ORDER BY createdAt DESC")
    fun getCommentsForShort(shortId: String): Flow<List<ShortCommentEntity>>

    @Query("SELECT COUNT(*) FROM shorts_comments WHERE shortId = :shortId")
    suspend fun getCommentsCountForShort(shortId: String): Int

    @Query("SELECT COUNT(*) FROM shorts_comments WHERE shortId = :shortId")
    fun getCommentsCountForShortFlow(shortId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: ShortCommentEntity)

    @Query("DELETE FROM shorts_comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: String)

    // Clear all shorts data (for clean admin reset requirement)
    @Query("DELETE FROM shorts")
    suspend fun clearAllShorts()

    @Query("DELETE FROM shorts_analysis")
    suspend fun clearAllAnalysis()

    @Query("DELETE FROM shorts_bible_references")
    suspend fun clearAllReferences()

    @Query("DELETE FROM shorts_likes")
    suspend fun clearAllLikes()

    @Query("DELETE FROM shorts_comments")
    suspend fun clearAllComments()

    @Query("DELETE FROM shorts_saves")
    suspend fun clearAllSaves()
}
