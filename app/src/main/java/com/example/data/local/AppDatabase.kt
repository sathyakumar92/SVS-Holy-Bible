package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.datasource.AdvancedSeedData
import com.example.data.datasource.ScriptureSeedData
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BibleVerse::class,
        Bookmark::class,
        VerseHighlight::class,
        BibleNote::class,
        ReadingHistory::class,
        PlanProgress::class,
        QuizScore::class,
        // Advanced Features Entities
        NotebookFolder::class,
        NotebookNote::class,
        NoteVerseRef::class,
        SmartPlanEntity::class,
        SmartPlanAssignment::class,
        PlanDayCompletion::class,
        MemorizedVerse::class,
        PrayerEntry::class,
        PrayerUpdate::class,
        SermonDocument::class,
        SermonSection::class,
        TimelineEvent::class,
        BiblicalLocation::class,
        ListeningSession::class,
        AppFeedback::class,
        // Bible Topics & Advanced Study Entities
        TopicEntity::class,
        TopicReferenceEntity::class,
        TopicFavoriteEntity::class,
        TopicHistoryEntity::class,
        TopicSearchHistoryEntity::class,
        TopicNoteEntity::class,
        // Rebuilt Bible Shorts Entities
        ShortEntity::class,
        ShortAnalysisEntity::class,
        ShortBibleReferenceEntity::class,
        ShortLikeEntity::class,
        ShortCommentEntity::class,
        ShortSaveEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bibleDao(): BibleDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun highlightDao(): HighlightDao
    abstract fun noteDao(): NoteDao
    abstract fun historyDao(): HistoryDao
    abstract fun planDao(): PlanDao
    abstract fun quizScoreDao(): QuizScoreDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun topicDao(): TopicDao
    abstract fun shortsDao(): ShortsDao

    // Advanced Features DAOs
    abstract fun notebookDao(): NotebookDao
    abstract fun smartPlanDao(): SmartPlanDao
    abstract fun memorizationDao(): MemorizationDao
    abstract fun prayerJournalDao(): PrayerJournalDao
    abstract fun sermonDao(): SermonDao
    abstract fun timelineDao(): TimelineDao
    abstract fun listeningDao(): ListeningDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "svs_holy_bible_v5.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
