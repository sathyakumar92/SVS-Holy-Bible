package com.example.data.repository

import com.example.data.datasource.AdvancedSeedData
import com.example.data.datasource.CanonicalBibleData
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class AdvancedFeaturesRepository(private val database: AppDatabase) {

    private val notebookDao = database.notebookDao()
    private val smartPlanDao = database.smartPlanDao()
    private val memorizationDao = database.memorizationDao()
    private val prayerJournalDao = database.prayerJournalDao()
    private val sermonDao = database.sermonDao()
    private val timelineDao = database.timelineDao()
    private val listeningDao = database.listeningDao()

    // ----------------------------------------------------
    // FEATURE 1: ADVANCED NOTEBOOK
    // ----------------------------------------------------
    val allFolders: Flow<List<NotebookFolder>> = notebookDao.getAllFolders()
    val allNotes: Flow<List<NotebookNote>> = notebookDao.getAllNotes()
    val favoriteNotes: Flow<List<NotebookNote>> = notebookDao.getFavoriteNotes()

    fun getNotesByFolder(folderId: Long): Flow<List<NotebookNote>> = notebookDao.getNotesByFolder(folderId)

    fun searchNotes(query: String): Flow<List<NotebookNote>> = notebookDao.searchNotes(query)

    suspend fun getNoteById(id: Long): NotebookNote? = withContext(Dispatchers.IO) {
        notebookDao.getNoteById(id)
    }

    suspend fun createOrUpdateNote(note: NotebookNote): Long = withContext(Dispatchers.IO) {
        if (note.id == 0L) {
            notebookDao.insertNote(note)
        } else {
            notebookDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        notebookDao.deleteNote(id)
    }

    suspend fun createFolder(nameEn: String, nameTa: String, colorHex: String = "#173F8A"): Long = withContext(Dispatchers.IO) {
        notebookDao.insertFolder(NotebookFolder(nameEn = nameEn, nameTa = nameTa, colorHex = colorHex))
    }

    suspend fun safeDeleteFolder(folderId: Long) = withContext(Dispatchers.IO) {
        // Move all notes to default (null) folder to prevent accidental note loss
        notebookDao.moveNotesToDefault(folderId)
        notebookDao.deleteFolder(folderId)
    }

    fun getVerseRefsForNote(noteId: Long): Flow<List<NoteVerseRef>> = notebookDao.getRefsForNote(noteId)

    suspend fun attachVerseRef(ref: NoteVerseRef): Long = withContext(Dispatchers.IO) {
        notebookDao.insertVerseRef(ref)
    }

    suspend fun removeVerseRef(refId: Long) = withContext(Dispatchers.IO) {
        notebookDao.deleteVerseRef(refId)
    }

    // ----------------------------------------------------
    // FEATURE 2: SMART READING PLANS
    // ----------------------------------------------------
    val allSmartPlans: Flow<List<SmartPlanEntity>> = smartPlanDao.getAllPlans()

    suspend fun getSmartPlanById(id: String): SmartPlanEntity? = withContext(Dispatchers.IO) {
        smartPlanDao.getPlanById(id)
    }

    fun getAssignmentsForPlan(planId: String): Flow<List<SmartPlanAssignment>> =
        smartPlanDao.getAssignmentsForPlan(planId)

    fun getCompletionsForPlan(planId: String): Flow<List<PlanDayCompletion>> =
        smartPlanDao.getCompletionsForPlan(planId)

    suspend fun startPlan(planId: String, reminderMinutes: Int = 420) = withContext(Dispatchers.IO) {
        val plan = smartPlanDao.getPlanById(planId) ?: return@withContext
        smartPlanDao.updatePlan(
            plan.copy(
                isStarted = true,
                isPaused = false,
                isCompleted = false,
                startDateTimestamp = System.currentTimeMillis(),
                reminderTimeMinutes = reminderMinutes
            )
        )
    }

    suspend fun togglePlanPause(planId: String) = withContext(Dispatchers.IO) {
        val plan = smartPlanDao.getPlanById(planId) ?: return@withContext
        smartPlanDao.updatePlan(plan.copy(isPaused = !plan.isPaused))
    }

    suspend fun restartPlan(planId: String) = withContext(Dispatchers.IO) {
        val plan = smartPlanDao.getPlanById(planId) ?: return@withContext
        smartPlanDao.resetCompletionsForPlan(planId)
        smartPlanDao.updatePlan(
            plan.copy(
                isStarted = true,
                isPaused = false,
                isCompleted = false,
                startDateTimestamp = System.currentTimeMillis(),
                completedDateTimestamp = null
            )
        )
    }

    suspend fun toggleDayCompletion(planId: String, dayNumber: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        val key = "${planId}_day_$dayNumber"
        if (isCompleted) {
            val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            smartPlanDao.insertCompletion(
                PlanDayCompletion(
                    completionKey = key,
                    planId = planId,
                    dayNumber = dayNumber,
                    isCompleted = true,
                    completedTimestamp = System.currentTimeMillis(),
                    localDateString = dateFmt
                )
            )
        } else {
            smartPlanDao.deleteCompletion(key)
        }
    }

    suspend fun createCustomPlan(
        titleEn: String,
        titleTa: String,
        descriptionEn: String,
        durationDays: Int,
        passages: List<String>
    ): String = withContext(Dispatchers.IO) {
        val planId = "custom_plan_${System.currentTimeMillis()}"
        val entity = SmartPlanEntity(
            id = planId,
            titleEn = titleEn,
            titleTa = titleTa,
            descriptionEn = descriptionEn,
            descriptionTa = "தனிப்பயன் வாசிப்பு திட்டம்",
            durationDays = durationDays,
            category = "Custom",
            isCustom = true,
            isStarted = true,
            startDateTimestamp = System.currentTimeMillis()
        )
        smartPlanDao.insertPlan(entity)

        val assignments = (1..durationDays).map { day ->
            val passage = if (passages.isNotEmpty()) passages[(day - 1) % passages.size] else "Genesis $day"
            SmartPlanAssignment(
                planId = planId,
                dayNumber = day,
                titleEn = "Day $day: $passage",
                titleTa = "நாள் $day: $passage",
                passagesJson = passage
            )
        }
        smartPlanDao.insertAssignments(assignments)
        planId
    }

    // ----------------------------------------------------
    // FEATURE 3: VERSE MEMORIZATION & SPACED REPETITION (SM-2)
    // ----------------------------------------------------
    val allMemoryVerses: Flow<List<MemorizedVerse>> = memorizationDao.getAllVerses()
    val memorizedVerses: Flow<List<MemorizedVerse>> = memorizationDao.getMemorizedVerses()

    fun getDueVerses(): Flow<List<MemorizedVerse>> =
        memorizationDao.getDueVerses(System.currentTimeMillis())

    suspend fun addVerseToMemorization(
        bookId: String,
        bookName: String,
        chapter: Int,
        verse: Int,
        refEn: String,
        refTa: String,
        textEn: String,
        textTa: String,
        translationId: String = "KJV",
        category: String = "General"
    ): Long = withContext(Dispatchers.IO) {
        val existing = memorizationDao.getVerseByRef(bookId, chapter, verse)
        if (existing != null) {
            existing.id
        } else {
            memorizationDao.insertVerse(
                MemorizedVerse(
                    bookId = bookId,
                    bookName = bookName,
                    chapterNumber = chapter,
                    verseNumber = verse,
                    referenceDisplayEn = refEn,
                    referenceDisplayTa = refTa,
                    verseTextEn = textEn,
                    verseTextTa = textTa,
                    translationId = translationId,
                    category = category,
                    nextReviewTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun recordSrsReview(verseId: Long, rating: Int) = withContext(Dispatchers.IO) {
        // rating: 1 (Again), 2 (Hard), 3 (Good), 4 (Easy)
        val v = memorizationDao.getVerseById(verseId) ?: return@withContext
        val newReviewCount = v.reviewCount + 1
        val isCorrect = rating >= 3
        val newCorrectCount = if (isCorrect) v.correctCount + 1 else v.correctCount
        val newIncorrectCount = if (!isCorrect) v.incorrectCount + 1 else v.incorrectCount

        var newRepetitions = v.repetitions
        var newIntervalDays = v.intervalDays
        var newEase = v.easeFactor

        if (rating < 3) {
            newRepetitions = 0
            newIntervalDays = 1
        } else {
            when (newRepetitions) {
                0 -> newIntervalDays = 1
                1 -> newIntervalDays = 3
                else -> newIntervalDays = (newIntervalDays * newEase).roundToInt().coerceAtLeast(1)
            }
            newRepetitions += 1
        }

        // Adjust Ease Factor (minimum 1.3)
        newEase = (newEase + (0.1f - (5 - rating) * (0.08f + (5 - rating) * 0.02f))).coerceAtLeast(1.3f)

        val newStatus = when {
            newRepetitions >= 5 -> SrsStatus.MEMORIZED
            newRepetitions > 0 -> SrsStatus.LEARNING
            else -> SrsStatus.NEW
        }

        val nextTimestamp = System.currentTimeMillis() + (newIntervalDays * 86400000L)

        memorizationDao.updateVerse(
            v.copy(
                repetitions = newRepetitions,
                intervalDays = newIntervalDays,
                easeFactor = newEase,
                reviewCount = newReviewCount,
                correctCount = newCorrectCount,
                incorrectCount = newIncorrectCount,
                status = newStatus,
                lastReviewTimestamp = System.currentTimeMillis(),
                nextReviewTimestamp = nextTimestamp
            )
        )
    }

    suspend fun deleteMemoryVerse(id: Long) = withContext(Dispatchers.IO) {
        memorizationDao.deleteVerse(id)
    }

    // ----------------------------------------------------
    // FEATURE 4: PERSONAL PRAYER JOURNAL
    // ----------------------------------------------------
    val allPrayers: Flow<List<PrayerEntry>> = prayerJournalDao.getAllPrayers()

    fun getPrayersByStatus(status: PrayerStatus): Flow<List<PrayerEntry>> =
        prayerJournalDao.getPrayersByStatus(status)

    fun searchPrayers(query: String): Flow<List<PrayerEntry>> = prayerJournalDao.searchPrayers(query)

    suspend fun getPrayerById(id: Long): PrayerEntry? = withContext(Dispatchers.IO) {
        prayerJournalDao.getPrayerById(id)
    }

    suspend fun savePrayer(entry: PrayerEntry): Long = withContext(Dispatchers.IO) {
        if (entry.id == 0L) {
            prayerJournalDao.insertPrayer(entry)
        } else {
            prayerJournalDao.updatePrayer(entry.copy(updatedAt = System.currentTimeMillis()))
            entry.id
        }
    }

    suspend fun markPrayerAnswered(prayerId: Long, testimony: String) = withContext(Dispatchers.IO) {
        val prayer = prayerJournalDao.getPrayerById(prayerId) ?: return@withContext
        prayerJournalDao.updatePrayer(
            prayer.copy(
                status = PrayerStatus.ANSWERED,
                answeredDateTimestamp = System.currentTimeMillis(),
                answeredTestimony = testimony,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deletePrayer(id: Long) = withContext(Dispatchers.IO) {
        prayerJournalDao.deletePrayer(id)
    }

    fun getUpdatesForPrayer(prayerId: Long): Flow<List<PrayerUpdate>> =
        prayerJournalDao.getUpdatesForPrayer(prayerId)

    suspend fun addPrayerUpdate(prayerId: Long, text: String): Long = withContext(Dispatchers.IO) {
        prayerJournalDao.insertUpdate(PrayerUpdate(prayerId = prayerId, updateText = text))
    }

    // ----------------------------------------------------
    // FEATURE 5: SERMON NOTES & BIBLE LESSON BUILDER
    // ----------------------------------------------------
    val allSermonDocs: Flow<List<SermonDocument>> = sermonDao.getAllDocuments()

    fun getSermonDocsByType(type: SermonDocType): Flow<List<SermonDocument>> =
        sermonDao.getDocsByType(type)

    fun searchSermonDocs(query: String): Flow<List<SermonDocument>> = sermonDao.searchDocs(query)

    suspend fun getSermonDocById(id: Long): SermonDocument? = withContext(Dispatchers.IO) {
        sermonDao.getDocumentById(id)
    }

    suspend fun saveSermonDoc(doc: SermonDocument): Long = withContext(Dispatchers.IO) {
        if (doc.id == 0L) {
            sermonDao.insertDocument(doc)
        } else {
            sermonDao.updateDocument(doc.copy(updatedAt = System.currentTimeMillis()))
            doc.id
        }
    }

    suspend fun deleteSermonDoc(id: Long) = withContext(Dispatchers.IO) {
        sermonDao.deleteSectionsForDoc(id)
        sermonDao.deleteDocument(id)
    }

    fun getSectionsForDoc(docId: Long): Flow<List<SermonSection>> = sermonDao.getSectionsForDoc(docId)

    suspend fun saveSection(section: SermonSection): Long = withContext(Dispatchers.IO) {
        if (section.id == 0L) {
            sermonDao.insertSection(section)
        } else {
            sermonDao.updateSection(section)
            section.id
        }
    }

    suspend fun deleteSection(sectionId: Long) = withContext(Dispatchers.IO) {
        sermonDao.deleteSection(sectionId)
    }

    // ----------------------------------------------------
    // FEATURE 6: TIMELINE & BIBLICAL MAPS
    // ----------------------------------------------------
    val allTimelineEvents: Flow<List<TimelineEvent>> = timelineDao.getAllEvents()
    val allBiblicalLocations: Flow<List<BiblicalLocation>> = timelineDao.getAllLocations()

    fun getEventsByPeriod(periodId: String): Flow<List<TimelineEvent>> =
        timelineDao.getEventsByPeriod(periodId)

    fun searchTimelineEvents(query: String): Flow<List<TimelineEvent>> =
        timelineDao.searchEvents(query)

    suspend fun getTimelineEventById(id: String): TimelineEvent? = withContext(Dispatchers.IO) {
        timelineDao.getEventById(id)
    }

    suspend fun getBiblicalLocationById(id: String): BiblicalLocation? = withContext(Dispatchers.IO) {
        timelineDao.getLocationById(id)
    }

    // ----------------------------------------------------
    // FEATURE 7: IMMERSIVE LISTENING MODE
    // ----------------------------------------------------
    val recentListeningSessions: Flow<List<ListeningSession>> = listeningDao.getRecentSessions()
    val lastListeningSession: Flow<ListeningSession?> = listeningDao.getLastSession()

    suspend fun recordListeningSession(
        translationId: String,
        bookId: String,
        bookName: String,
        chapter: Int,
        verse: Int,
        posSec: Int,
        totalSec: Int
    ) = withContext(Dispatchers.IO) {
        val pct = if (totalSec > 0) (posSec.toFloat() / totalSec.toFloat()).coerceIn(0f, 1f) else 0f
        listeningDao.insertSession(
            ListeningSession(
                translationId = translationId,
                bookId = bookId,
                bookName = bookName,
                chapterNumber = chapter,
                verseNumber = verse,
                playbackPositionSeconds = posSec,
                totalDurationSeconds = totalSec,
                completedPercentage = pct
            )
        )
    }
}
