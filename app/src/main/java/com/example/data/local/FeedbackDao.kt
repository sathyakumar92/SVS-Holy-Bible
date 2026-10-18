package com.example.data.local

import androidx.room.*
import com.example.data.model.AppFeedback
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackDao {

    @Query("SELECT * FROM user_feedbacks ORDER BY timestamp DESC")
    fun getAllFeedbacks(): Flow<List<AppFeedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: AppFeedback): Long

    @Delete
    suspend fun deleteFeedback(feedback: AppFeedback)

    @Query("SELECT COUNT(*) FROM user_feedbacks")
    suspend fun getFeedbackCount(): Int
}
