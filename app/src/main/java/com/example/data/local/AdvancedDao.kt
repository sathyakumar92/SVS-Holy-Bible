package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

// ----------------------------------------------------
// FEATURE 1: NOTEBOOK DAO
// ----------------------------------------------------
@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebook_folders ORDER BY nameEn ASC")
    fun getAllFolders(): Flow<List<NotebookFolder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: NotebookFolder): Long

    @Update
    suspend fun updateFolder(folder: NotebookFolder)

    @Query("DELETE FROM notebook_folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long)

    @Query("SELECT * FROM notebook_notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NotebookNote>>

    @Query("SELECT * FROM notebook_notes WHERE folderId = :folderId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByFolder(folderId: Long): Flow<List<NotebookNote>>

    @Query("SELECT * FROM notebook_notes WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteNotes(): Flow<List<NotebookNote>>

    @Query("SELECT * FROM notebook_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NotebookNote?

    @Query("""
        SELECT * FROM notebook_notes 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%'
           OR observation LIKE '%' || :query || '%'
           OR interpretation LIKE '%' || :query || '%'
           OR application LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<NotebookNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NotebookNote): Long

    @Update
    suspend fun updateNote(note: NotebookNote)

    @Query("DELETE FROM notebook_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("UPDATE notebook_notes SET folderId = NULL WHERE folderId = :folderId")
    suspend fun moveNotesToDefault(folderId: Long)

    @Query("SELECT * FROM note_verse_refs WHERE noteId = :noteId")
    fun getRefsForNote(noteId: Long): Flow<List<NoteVerseRef>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerseRef(ref: NoteVerseRef): Long

    @Query("DELETE FROM note_verse_refs WHERE id = :id")
    suspend fun deleteVerseRef(id: Long)
}

// ----------------------------------------------------
// FEATURE 2: SMART READING PLANS DAO
// ----------------------------------------------------
@Dao
interface SmartPlanDao {
    @Query("SELECT * FROM smart_plans ORDER BY isStarted DESC, durationDays ASC")
    fun getAllPlans(): Flow<List<SmartPlanEntity>>

    @Query("SELECT * FROM smart_plans WHERE id = :id LIMIT 1")
    suspend fun getPlanById(id: String): SmartPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: SmartPlanEntity)

    @Update
    suspend fun updatePlan(plan: SmartPlanEntity)

    @Query("DELETE FROM smart_plans WHERE id = :id")
    suspend fun deletePlan(id: String)

    @Query("SELECT * FROM smart_plan_assignments WHERE planId = :planId ORDER BY dayNumber ASC")
    fun getAssignmentsForPlan(planId: String): Flow<List<SmartPlanAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<SmartPlanAssignment>)

    @Query("DELETE FROM smart_plan_assignments WHERE planId = :planId")
    suspend fun deleteAssignmentsForPlan(planId: String)

    @Query("SELECT * FROM plan_day_completions WHERE planId = :planId")
    fun getCompletionsForPlan(planId: String): Flow<List<PlanDayCompletion>>

    @Query("SELECT * FROM plan_day_completions ORDER BY completedTimestamp DESC")
    fun getAllCompletions(): Flow<List<PlanDayCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: PlanDayCompletion)

    @Query("DELETE FROM plan_day_completions WHERE completionKey = :key")
    suspend fun deleteCompletion(key: String)

    @Query("DELETE FROM plan_day_completions WHERE planId = :planId")
    suspend fun resetCompletionsForPlan(planId: String)
}

// ----------------------------------------------------
// FEATURE 3: MEMORIZATION DAO
// ----------------------------------------------------
@Dao
interface MemorizationDao {
    @Query("SELECT * FROM memorized_verses ORDER BY isFavorite DESC, nextReviewTimestamp ASC")
    fun getAllVerses(): Flow<List<MemorizedVerse>>

    @Query("SELECT * FROM memorized_verses WHERE nextReviewTimestamp <= :nowTimestamp ORDER BY nextReviewTimestamp ASC")
    fun getDueVerses(nowTimestamp: Long): Flow<List<MemorizedVerse>>

    @Query("SELECT * FROM memorized_verses WHERE status = 'MEMORIZED' ORDER BY lastReviewTimestamp DESC")
    fun getMemorizedVerses(): Flow<List<MemorizedVerse>>

    @Query("SELECT * FROM memorized_verses WHERE id = :id LIMIT 1")
    suspend fun getVerseById(id: Long): MemorizedVerse?

    @Query("SELECT * FROM memorized_verses WHERE bookId = :bookId AND chapterNumber = :chapter AND verseNumber = :verse LIMIT 1")
    suspend fun getVerseByRef(bookId: String, chapter: Int, verse: Int): MemorizedVerse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerse(verse: MemorizedVerse): Long

    @Update
    suspend fun updateVerse(verse: MemorizedVerse)

    @Query("DELETE FROM memorized_verses WHERE id = :id")
    suspend fun deleteVerse(id: Long)
}

// ----------------------------------------------------
// FEATURE 4: PRAYER JOURNAL DAO
// ----------------------------------------------------
@Dao
interface PrayerJournalDao {
    @Query("SELECT * FROM prayer_entries ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllPrayers(): Flow<List<PrayerEntry>>

    @Query("SELECT * FROM prayer_entries WHERE status = :status ORDER BY updatedAt DESC")
    fun getPrayersByStatus(status: PrayerStatus): Flow<List<PrayerEntry>>

    @Query("SELECT * FROM prayer_entries WHERE id = :id LIMIT 1")
    suspend fun getPrayerById(id: Long): PrayerEntry?

    @Query("""
        SELECT * FROM prayer_entries 
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchPrayers(query: String): Flow<List<PrayerEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(entry: PrayerEntry): Long

    @Update
    suspend fun updatePrayer(entry: PrayerEntry)

    @Query("DELETE FROM prayer_entries WHERE id = :id")
    suspend fun deletePrayer(id: Long)

    @Query("SELECT * FROM prayer_updates WHERE prayerId = :prayerId ORDER BY timestamp DESC")
    fun getUpdatesForPrayer(prayerId: Long): Flow<List<PrayerUpdate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(update: PrayerUpdate): Long

    @Query("DELETE FROM prayer_updates WHERE id = :id")
    suspend fun deleteUpdate(id: Long)
}

// ----------------------------------------------------
// FEATURE 5: SERMON BUILDER DAO
// ----------------------------------------------------
@Dao
interface SermonDao {
    @Query("SELECT * FROM sermon_documents ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllDocuments(): Flow<List<SermonDocument>>

    @Query("SELECT * FROM sermon_documents WHERE docType = :type ORDER BY updatedAt DESC")
    fun getDocsByType(type: SermonDocType): Flow<List<SermonDocument>>

    @Query("SELECT * FROM sermon_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): SermonDocument?

    @Query("""
        SELECT * FROM sermon_documents 
        WHERE title LIKE '%' || :query || '%' 
           OR subtitleOrTheme LIKE '%' || :query || '%' 
           OR mainPassageRef LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchDocs(query: String): Flow<List<SermonDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: SermonDocument): Long

    @Update
    suspend fun updateDocument(doc: SermonDocument)

    @Query("DELETE FROM sermon_documents WHERE id = :id")
    suspend fun deleteDocument(id: Long)

    @Query("SELECT * FROM sermon_sections WHERE documentId = :docId ORDER BY sectionOrder ASC")
    fun getSectionsForDoc(docId: Long): Flow<List<SermonSection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SermonSection): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SermonSection>)

    @Update
    suspend fun updateSection(section: SermonSection)

    @Query("DELETE FROM sermon_sections WHERE id = :id")
    suspend fun deleteSection(id: Long)

    @Query("DELETE FROM sermon_sections WHERE documentId = :docId")
    suspend fun deleteSectionsForDoc(docId: Long)
}

// ----------------------------------------------------
// FEATURE 6: TIMELINE & MAPS DAO
// ----------------------------------------------------
@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_events ORDER BY chronologicalOrder ASC")
    fun getAllEvents(): Flow<List<TimelineEvent>>

    @Query("SELECT * FROM timeline_events WHERE periodId = :periodId ORDER BY chronologicalOrder ASC")
    fun getEventsByPeriod(periodId: String): Flow<List<TimelineEvent>>

    @Query("SELECT * FROM timeline_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): TimelineEvent?

    @Query("""
        SELECT * FROM timeline_events 
        WHERE titleEn LIKE '%' || :query || '%' 
           OR titleTa LIKE '%' || :query || '%' 
           OR summaryEn LIKE '%' || :query || '%' 
           OR keyFigures LIKE '%' || :query || '%'
        ORDER BY chronologicalOrder ASC
    """)
    fun searchEvents(query: String): Flow<List<TimelineEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<TimelineEvent>)

    @Query("SELECT * FROM biblical_locations ORDER BY nameEn ASC")
    fun getAllLocations(): Flow<List<BiblicalLocation>>

    @Query("SELECT * FROM biblical_locations WHERE id = :id LIMIT 1")
    suspend fun getLocationById(id: String): BiblicalLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<BiblicalLocation>)
}

// ----------------------------------------------------
// FEATURE 7: LISTENING SESSIONS DAO
// ----------------------------------------------------
@Dao
interface ListeningDao {
    @Query("SELECT * FROM listening_history_sessions ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSessions(): Flow<List<ListeningSession>>

    @Query("SELECT * FROM listening_history_sessions ORDER BY timestamp DESC LIMIT 1")
    fun getLastSession(): Flow<ListeningSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ListeningSession): Long

    @Query("DELETE FROM listening_history_sessions")
    suspend fun clearHistory()
}
