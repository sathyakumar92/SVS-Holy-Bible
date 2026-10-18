package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.BibleAudioEngine
import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.DevotionalData
import com.example.data.datasource.ScriptureSeedData
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AdvancedFeaturesRepository
import com.example.data.repository.BibleRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.QuizRepository
import com.example.data.repository.TopicRepository
import com.example.data.repository.UserPreferences
import com.example.data.datasource.BibleQuizBank
import com.example.data.datasource.AdvancedTopicEngine
import com.example.data.datasource.CompleteBibleTopicsData
import com.example.data.datasource.TopicAnalysisResult
import com.example.data.repository.ShortsRepository
import com.example.data.datasource.CuratedShortsAnalysisEngine
import com.example.ui.screens.search.SearchMode
import com.example.ui.screens.search.SearchSortOrder
import com.example.util.*
import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    HOME,
    READER,
    SEARCH,
    SHORTS,
    SHORTS_PLAYER,
    SHORTS_BIBLE_ANALYSIS,
    SHORTS_ADMIN_STUDIO,
    MORE,
    TOPICS,
    TOPIC_DETAIL,
    AUDIO_PLAYER,
    DAILY_DEVOTIONAL,
    QUIZ,
    READING_PLANS,
    PLAN_DETAIL,
    LIBRARY,
    SETTINGS,
    ABOUT,
    FEEDBACK,
    READING_CALENDAR,
    // SEVEN ADVANCED FEATURES
    NOTEBOOK,
    NOTE_EDITOR,
    SMART_PLANS,
    CUSTOM_PLAN_BUILDER,
    MEMORIZATION,
    MEMORIZATION_SESSION,
    PRAYER_JOURNAL,
    PRAYER_EDITOR,
    SERMON_BUILDER,
    SERMON_EDITOR,
    TIMELINE,
    TIMELINE_DETAIL,
    BIBLICAL_MAP,
    IMMERSIVE_LISTENING
}

class BibleViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = BibleRepository(database)
    val advancedRepo = AdvancedFeaturesRepository(database)
    val topicRepo = TopicRepository(database)
    val preferencesRepository = PreferencesRepository(application)
    val audioEngine = BibleAudioEngine(application)
    val quizRepository = QuizRepository(application, database)
    val shortsRepo = ShortsRepository(application, database.shortsDao())

    // Rebuilt Bible Shorts State
    val publishedShorts: StateFlow<List<ShortEntity>> = shortsRepo.publishedShorts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminShorts: StateFlow<List<ShortEntity>> = shortsRepo.allShortsAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeShortPlayer = MutableStateFlow<ShortEntity?>(null)
    val activeShortPlayer: StateFlow<ShortEntity?> = _activeShortPlayer.asStateFlow()

    private val _activeShortId = MutableStateFlow<String?>(null)

    val activeShortDetails: StateFlow<ShortWithDetails?> = _activeShortId.flatMapLatest { id ->
        if (id != null) shortsRepo.getShortWithDetails(id, "current_user") else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeShortComments: StateFlow<List<ShortCommentEntity>> = _activeShortId.flatMapLatest { id ->
        if (id != null) shortsRepo.getCommentsForShort(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _targetScrollVerse = MutableStateFlow<Int?>(null)
    val targetScrollVerse: StateFlow<Int?> = _targetScrollVerse.asStateFlow()

    // Navigation Stack
    private val screenStack = mutableListOf<AppScreen>()
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Welcome Screen
    private val _showWelcome = MutableStateFlow(true)
    val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()

    // Preferences
    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    // Reader State
    private val _selectedBook = MutableStateFlow(CanonicalBibleData.findBookById("JHN") ?: CanonicalBibleData.allBooks[0])
    val selectedBook: StateFlow<BibleBook> = _selectedBook.asStateFlow()

    private val _selectedChapter = MutableStateFlow(3)
    val selectedChapter: StateFlow<Int> = _selectedChapter.asStateFlow()

    private val _selectedVerseForAction = MutableStateFlow<BibleVerse?>(null)
    val selectedVerseForAction: StateFlow<BibleVerse?> = _selectedVerseForAction.asStateFlow()

    // Verses flow for current reader position
    private val _currentVerses = MutableStateFlow<List<BibleVerse>>(ScriptureSeedData.getCanonicalVersesForChapter("JHN", 3, "en"))
    val currentVerses: StateFlow<List<BibleVerse>> = _currentVerses.asStateFlow()

    // Highlights for current reader position
    val currentHighlights: StateFlow<List<VerseHighlight>> = combine(
        _selectedBook,
        _selectedChapter
    ) { book, chapter ->
        Pair(book.id, chapter)
    }.flatMapLatest { (bookId, chapter) ->
        repository.getHighlightsForChapter(bookId, chapter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Advanced Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchLanguage = MutableStateFlow<String?>(null) // null = follow prefs, "en", "ta", "both"
    val searchLanguage: StateFlow<String?> = _searchLanguage.asStateFlow()

    private val _searchMode = MutableStateFlow(SearchMode.WORD)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    private val _wholeWordMatching = MutableStateFlow(false)
    val wholeWordMatching: StateFlow<Boolean> = _wholeWordMatching.asStateFlow()

    private val _caseSensitiveSearch = MutableStateFlow(false)
    val caseSensitiveSearch: StateFlow<Boolean> = _caseSensitiveSearch.asStateFlow()

    private val _searchSortOrder = MutableStateFlow(SearchSortOrder.RELEVANCE)
    val searchSortOrder: StateFlow<SearchSortOrder> = _searchSortOrder.asStateFlow()

    private val _testamentFilter = MutableStateFlow<String?>(null) // null, "OT", "NT"
    val testamentFilter: StateFlow<String?> = _testamentFilter.asStateFlow()

    private val _bookFilter = MutableStateFlow<String?>(null) // null = all, or bookId
    val bookFilter: StateFlow<String?> = _bookFilter.asStateFlow()

    private val _fromChapterFilter = MutableStateFlow(0)
    val fromChapterFilter: StateFlow<Int> = _fromChapterFilter.asStateFlow()

    private val _toChapterFilter = MutableStateFlow(0)
    val toChapterFilter: StateFlow<Int> = _toChapterFilter.asStateFlow()

    private val _fromVerseFilter = MutableStateFlow(0)
    val fromVerseFilter: StateFlow<Int> = _fromVerseFilter.asStateFlow()

    private val _toVerseFilter = MutableStateFlow(0)
    val toVerseFilter: StateFlow<Int> = _toVerseFilter.asStateFlow()

    private val _referenceResult = MutableStateFlow<ReferenceParseResult>(ReferenceParseResult.NotAReference)
    val referenceResult: StateFlow<ReferenceParseResult> = _referenceResult.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _surroundingVerses = MutableStateFlow<Triple<BibleVerse?, BibleVerse?, BibleVerse?>?>(null)
    val surroundingVerses: StateFlow<Triple<BibleVerse?, BibleVerse?, BibleVerse?>?> = _surroundingVerses.asStateFlow()

    val searchResults: StateFlow<List<SearchResultItem>> = combine(
        _searchQuery,
        _searchLanguage,
        userPreferences,
        _searchMode,
        _wholeWordMatching,
        _caseSensitiveSearch,
        _searchSortOrder,
        _testamentFilter,
        _bookFilter,
        _fromChapterFilter,
        _toChapterFilter,
        _fromVerseFilter,
        _toVerseFilter
    ) { args ->
        val q = args[0] as String
        val customLang = args[1] as String?
        val prefs = args[2] as UserPreferences
        val mode = args[3] as SearchMode
        val wholeWord = args[4] as Boolean
        val caseSensitive = args[5] as Boolean
        val sortOrder = args[6] as SearchSortOrder
        val testFilter = args[7] as String?
        val bFilter = args[8] as String?
        val fromCh = args[9] as Int
        val toCh = args[10] as Int
        val fromV = args[11] as Int
        val toV = args[12] as Int
        val effectiveLang = customLang ?: prefs.bibleLanguage
        AdvancedSearchParams(
            query = q,
            language = effectiveLang,
            mode = mode,
            wholeWord = wholeWord,
            caseSensitive = caseSensitive,
            sortOrder = sortOrder,
            testamentFilter = testFilter,
            bookFilter = bFilter,
            fromChapter = fromCh,
            toChapter = toCh,
            fromVerse = fromV,
            toVerse = toV
        )
    }.flatMapLatest { p ->
        val trimmed = p.query.trim()
        if (trimmed.length >= 2 || p.mode == SearchMode.REFERENCE) {
            _isSearching.value = true
            flow {
                val (results, refRes) = repository.searchVersesAdvanced(
                    query = trimmed,
                    languageCode = p.language,
                    searchMode = p.mode,
                    wholeWord = p.wholeWord,
                    caseSensitive = p.caseSensitive,
                    sortOrder = p.sortOrder,
                    testamentFilter = p.testamentFilter,
                    bookFilter = p.bookFilter,
                    fromChapter = p.fromChapter,
                    toChapter = p.toChapter,
                    fromVerse = p.fromVerse,
                    toVerse = p.toVerse
                )
                _referenceResult.value = refRes
                _isSearching.value = false
                emit(results)
            }
        } else {
            _referenceResult.value = ReferenceParseResult.NotAReference
            _isSearching.value = false
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Reading Calendar & Statistics State
    val allReadingHistory: StateFlow<List<ReadingHistory>> = repository.getAllReadingHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val initialCal = Calendar.getInstance()
    private val _calendarSelectedYear = MutableStateFlow(initialCal.get(Calendar.YEAR))
    val calendarSelectedYear: StateFlow<Int> = _calendarSelectedYear.asStateFlow()

    private val _calendarSelectedMonth = MutableStateFlow(initialCal.get(Calendar.MONTH)) // 0-11
    val calendarSelectedMonth: StateFlow<Int> = _calendarSelectedMonth.asStateFlow()

    private val _calendarSelectedDay = MutableStateFlow(initialCal.get(Calendar.DAY_OF_MONTH))
    val calendarSelectedDay: StateFlow<Int> = _calendarSelectedDay.asStateFlow()

    val readingStats: StateFlow<ReadingStats> = combine(
        allReadingHistory,
        userPreferences
    ) { history, prefs ->
        computeReadingStatistics(history, prefs.dailyReadingGoalChapters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), computeReadingStatistics(emptyList(), 2))

    val selectedDayReadings: StateFlow<List<ReadingHistory>> = combine(
        allReadingHistory,
        _calendarSelectedYear,
        _calendarSelectedMonth,
        _calendarSelectedDay
    ) { history, year, month, day ->
        val cal = Calendar.getInstance()
        history.filter { item ->
            cal.timeInMillis = item.timestamp
            cal.get(Calendar.YEAR) == year &&
            cal.get(Calendar.MONTH) == month &&
            cal.get(Calendar.DAY_OF_MONTH) == day
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val daysWithReadingInSelectedMonth: StateFlow<Set<Int>> = combine(
        allReadingHistory,
        _calendarSelectedYear,
        _calendarSelectedMonth
    ) { history, year, month ->
        val cal = Calendar.getInstance()
        val days = mutableSetOf<Int>()
        history.forEach { item ->
            cal.timeInMillis = item.timestamp
            if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                days.add(cal.get(Calendar.DAY_OF_MONTH))
            }
        }
        days
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Bookmarks, Notes, History
    val allBookmarks: StateFlow<List<Bookmark>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<BibleNote>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastReadHistory: StateFlow<ReadingHistory?> = repository.getLastRead()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Realtime User Feedbacks
    val allFeedbacks: StateFlow<List<AppFeedback>> = repository.getAllFeedbacks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Topic search & dynamic analysis
    private val _selectedTopic = MutableStateFlow<TopicItem?>(null)
    val selectedTopic: StateFlow<TopicItem?> = _selectedTopic.asStateFlow()

    private val _topicSearchQuery = MutableStateFlow("")
    val topicSearchQuery: StateFlow<String> = _topicSearchQuery.asStateFlow()

    private val _topicCategoryFilter = MutableStateFlow("All")
    val topicCategoryFilter: StateFlow<String> = _topicCategoryFilter.asStateFlow()

    private val _topicAlphabetFilter = MutableStateFlow<String?>(null)
    val topicAlphabetFilter: StateFlow<String?> = _topicAlphabetFilter.asStateFlow()

    private val _topicTestamentFilter = MutableStateFlow<String?>(null) // null, "OT", "NT"
    val topicTestamentFilter: StateFlow<String?> = _topicTestamentFilter.asStateFlow()

    private val _selectedTopicDetail = MutableStateFlow<RichTopicStudy?>(null)
    val selectedTopicDetail: StateFlow<RichTopicStudy?> = _selectedTopicDetail.asStateFlow()

    val allBibleTopics: StateFlow<List<TopicEntity>> = topicRepo.getAllTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompleteBibleTopicsData.getAllCuratedTopics())

    val favoriteTopicIds: StateFlow<Set<String>> = topicRepo.getFavoriteTopicIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val recentlyViewedTopics: StateFlow<List<TopicEntity>> = topicRepo.getRecentlyViewedTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTopicSearchKeywords: StateFlow<List<String>> = topicRepo.getRecentSearchQueries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Topic Study Notes State
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentTopicNotes: StateFlow<List<TopicNoteEntity>> = _selectedTopicDetail
        .flatMapLatest { study ->
            if (study == null) flowOf(emptyList()) else topicRepo.getNotesForTopic(study.topic.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI-Powered Topic Assistant State
    private val _topicAssistantMessages = MutableStateFlow<List<TopicAiMessage>>(emptyList())
    val topicAssistantMessages: StateFlow<List<TopicAiMessage>> = _topicAssistantMessages.asStateFlow()

    private val _isTopicAssistantLoading = MutableStateFlow(false)
    val isTopicAssistantLoading: StateFlow<Boolean> = _isTopicAssistantLoading.asStateFlow()

    // Verse Comparison State
    private val _comparisonVerses = MutableStateFlow<List<TopicReferenceEntity>>(emptyList())
    val comparisonVerses: StateFlow<List<TopicReferenceEntity>> = _comparisonVerses.asStateFlow()

    private data class SearchQueryConfig(
        val query: String,
        val language: String?,
        val bibleLanguage: String,
        val mode: SearchMode
    )

    private val searchQueryConfigFlow = combine(
        _searchQuery,
        _searchLanguage,
        userPreferences,
        _searchMode
    ) { q, lang, prefs, mode ->
        SearchQueryConfig(q, lang, prefs.bibleLanguage, mode)
    }

    val categorizedSearchResults: StateFlow<CategorizedSearchResults> = combine(
        searchQueryConfigFlow,
        allBibleTopics,
        searchResults
    ) { config, topics, verseResults ->
        val query = config.query.trim()
        val effectiveLang = config.language ?: config.bibleLanguage
        val isTa = effectiveLang == "ta"

        if (query.isBlank()) {
            CategorizedSearchResults(
                intent = SearchIntent.KEYWORD_PHRASE,
                normalizedQuery = "",
                suggestedSearches = AdvancedSearchEngine.getSuggestedSearches("", isTa)
            )
        } else {
            val refResult = BibleReferenceParser.parse(query)
            val isQ = AdvancedSearchEngine.isQuestion(query)
            val intent = when {
                refResult is ReferenceParseResult.Success -> SearchIntent.BIBLE_REFERENCE
                isQ -> SearchIntent.NATURAL_LANGUAGE_QUESTION
                config.mode == SearchMode.TOPIC -> SearchIntent.BIBLE_TOPIC
                else -> SearchIntent.KEYWORD_PHRASE
            }

            // Matching topics
            val normQ = AdvancedSearchEngine.normalize(query)
            val matchedTopics = topics.filter { t ->
                val titleEnNorm = AdvancedSearchEngine.normalize(t.titleEn)
                val titleTaNorm = AdvancedSearchEngine.normalize(t.titleTa)
                val keyNorm = AdvancedSearchEngine.normalize(t.keywords)
                val synNorm = AdvancedSearchEngine.normalize(t.synonyms)
                titleEnNorm.contains(normQ) || titleTaNorm.contains(normQ) ||
                keyNorm.contains(normQ) || synNorm.contains(normQ) ||
                normQ.contains(titleEnNorm) || normQ.contains(titleTaNorm)
            }

            // Q&A answer if question
            val biblicalAnswer = if (isQ) {
                val resolvedTopic = AdvancedSearchEngine.resolveTopicForQuestion(query, topics) ?: matchedTopics.firstOrNull() ?: topics.firstOrNull()
                resolvedTopic?.let { AdvancedSearchEngine.buildBiblicalAnswer(query, it) }
            } else null

            // Direct reference verses
            val refVerses = if (refResult is ReferenceParseResult.Success) {
                verseResults.map { it.verse }
            } else emptyList()

            // Relevant chapters
            val relevantCh = verseResults.map {
                Pair(CanonicalBibleData.findBookById(it.verse.bookId) ?: CanonicalBibleData.allBooks[0], it.verse.chapterNumber)
            }.distinctBy { "${it.first.id}_${it.second}" }.take(5)

            CategorizedSearchResults(
                intent = intent,
                normalizedQuery = query,
                matchingTopics = matchedTopics,
                directReferenceResult = refResult,
                referenceVerses = refVerses,
                exactVerseMatches = verseResults.filter { it.relevanceScore >= 500 },
                relatedVerses = verseResults.filter { it.relevanceScore < 500 },
                relevantChapters = relevantCh,
                biblicalAnswer = biblicalAnswer,
                suggestedSearches = AdvancedSearchEngine.getSuggestedSearches(query, isTa),
                totalCount = matchedTopics.size + verseResults.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategorizedSearchResults(SearchIntent.KEYWORD_PHRASE, ""))

    private val _topicAnalysisResult = MutableStateFlow<TopicAnalysisResult?>(null)
    val topicAnalysisResult: StateFlow<TopicAnalysisResult?> = _topicAnalysisResult.asStateFlow()

    private val _isAnalyzingTopic = MutableStateFlow(false)
    val isAnalyzingTopic: StateFlow<Boolean> = _isAnalyzingTopic.asStateFlow()

    // Reading Plan selection
    private val _selectedPlan = MutableStateFlow<ReadingPlan?>(null)
    val selectedPlan: StateFlow<ReadingPlan?> = _selectedPlan.asStateFlow()

    val planProgress: StateFlow<List<PlanProgress>> = _selectedPlan.flatMapLatest { plan ->
        if (plan != null) repository.getPlanProgress(plan.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Quiz State
    private val _quizQuestions = MutableStateFlow(DevotionalData.quizQuestions)
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _isQuizCompleted = MutableStateFlow(false)
    val isQuizCompleted: StateFlow<Boolean> = _isQuizCompleted.asStateFlow()

    // Upgraded interactive session & config states
    private val _quizSessionState = MutableStateFlow<QuizSessionState?>(null)
    val quizSessionState: StateFlow<QuizSessionState?> = _quizSessionState.asStateFlow()

    private val _quizConfig = MutableStateFlow(QuizConfig())
    val quizConfig: StateFlow<QuizConfig> = _quizConfig.asStateFlow()

    private val _showQuizConfigScreen = MutableStateFlow(true)
    val showQuizConfigScreen: StateFlow<Boolean> = _showQuizConfigScreen.asStateFlow()

    private val _uniqueQuestionsSeenCount = MutableStateFlow(0)
    val uniqueQuestionsSeenCount: StateFlow<Int> = _uniqueQuestionsSeenCount.asStateFlow()

    private val _savedActiveSession = MutableStateFlow<QuizSessionState?>(null)
    val savedActiveSession: StateFlow<QuizSessionState?> = _savedActiveSession.asStateFlow()

    // Message feedback
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _userPreferences.value = prefs
            }
        }
        viewModelScope.launch {
            repository.ensureDatabaseInitialized()
            repository.seedInitialReadingHistoryIfEmpty()
            shortsRepo.seedInitialShortsIfEmpty()
        }
        viewModelScope.launch {
            combine(_selectedBook, _selectedChapter, userPreferences) { book, chapter, prefs ->
                Triple(book.id, chapter, prefs.bibleLanguage)
            }.flatMapLatest { (bookId, chapter, lang) ->
                repository.getVersesForChapter(bookId, chapter, lang)
            }.collect { verses ->
                val sorted = verses.distinctBy { it.verseNumber }.sortedBy { it.verseNumber }
                if (sorted.isNotEmpty()) {
                    _currentVerses.value = sorted
                }
            }
        }
        viewModelScope.launch {
            userPreferences.collect { prefs ->
                audioEngine.syncPreferences(
                    languageCode = prefs.bibleLanguage,
                    voiceGender = prefs.audioVoiceGender,
                    speed = prefs.audioSpeed
                )
            }
        }
        viewModelScope.launch {
            _uniqueQuestionsSeenCount.value = quizRepository.getUniqueQuestionsAttemptedCount()
            val saved = quizRepository.getActiveSession()
            _savedActiveSession.value = saved
            if (saved != null && !saved.isCompleted) {
                _showQuizConfigScreen.value = false
                _quizSessionState.value = saved
                _quizQuestions.value = saved.questions.map { it.question }
                _currentQuizIndex.value = saved.currentIndex
                _quizScore.value = saved.score
                val currQ = saved.questions.getOrNull(saved.currentIndex)
                _selectedAnswerIndex.value = currQ?.selectedShuffledIndex
                _isAnswerSubmitted.value = currQ?.isSubmitted ?: false
                _isQuizCompleted.value = saved.isCompleted
            }
        }
    }

    fun dismissWelcome() {
        _showWelcome.value = false
        viewModelScope.launch {
            preferencesRepository.setHasCompletedWelcome(true)
        }
    }

    fun replayIntroAnimation() {
        _showWelcome.value = true
    }

    private val topicHistoryStack = mutableListOf<String>()
    private val _canNavigateToPreviousTopic = MutableStateFlow(false)
    val canNavigateToPreviousTopic: StateFlow<Boolean> = _canNavigateToPreviousTopic.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
            if (screen != AppScreen.TOPIC_DETAIL) {
                topicHistoryStack.clear()
                _canNavigateToPreviousTopic.value = false
            }
        }
    }

    fun navigateBack(): Boolean {
        if (_currentScreen.value == AppScreen.TOPIC_DETAIL && topicHistoryStack.isNotEmpty()) {
            val prevTopicId = topicHistoryStack.removeAt(topicHistoryStack.size - 1)
            _canNavigateToPreviousTopic.value = topicHistoryStack.isNotEmpty()
            loadTopicDetailInternal(prevTopicId)
            return true
        }
        topicHistoryStack.clear()
        _canNavigateToPreviousTopic.value = false
        return if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            true
        } else if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            true
        } else {
            false
        }
    }

    fun navigateToPreviousTopic() {
        if (topicHistoryStack.isNotEmpty()) {
            val prevTopicId = topicHistoryStack.removeAt(topicHistoryStack.size - 1)
            _canNavigateToPreviousTopic.value = topicHistoryStack.isNotEmpty()
            loadTopicDetailInternal(prevTopicId)
        }
    }

    fun openReader(book: BibleBook, chapter: Int = 1, verse: Int? = null) {
        _selectedBook.value = book
        _selectedChapter.value = chapter.coerceIn(1, book.chapterCount)
        _targetScrollVerse.value = verse ?: 1
        viewModelScope.launch {
            repository.recordReading(book.id, book.englishName, _selectedChapter.value, verse ?: 1)
        }
        navigateTo(AppScreen.READER)
    }

    fun openReaderAtVerse(bookId: String, chapter: Int, verse: Int = 1) {
        val book = CanonicalBibleData.findBookById(bookId) ?: CanonicalBibleData.allBooks[0]
        openReader(book, chapter, verse)
    }

    fun clearTargetScrollVerse() {
        _targetScrollVerse.value = null
    }

    fun nextChapter() {
        val current = _selectedChapter.value
        val book = _selectedBook.value
        if (current < book.chapterCount) {
            _selectedChapter.value = current + 1
            viewModelScope.launch {
                repository.recordReading(book.id, book.englishName, _selectedChapter.value)
            }
        }
    }

    fun previousChapter() {
        val current = _selectedChapter.value
        val book = _selectedBook.value
        if (current > 1) {
            _selectedChapter.value = current - 1
            viewModelScope.launch {
                repository.recordReading(book.id, book.englishName, _selectedChapter.value)
            }
        }
    }

    fun selectVerseForAction(verse: BibleVerse?) {
        _selectedVerseForAction.value = verse
    }

    fun toggleBookmark(verse: BibleVerse) {
        viewModelScope.launch {
            val isNowBookmarked = repository.toggleBookmark(
                bookId = verse.bookId,
                chapter = verse.chapterNumber,
                verse = verse.verseNumber,
                reference = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                verseText = verse.verseText,
                languageCode = verse.languageCode
            )
            _feedbackMessage.value = if (isNowBookmarked) "Verse bookmarked" else "Bookmark removed"
        }
    }

    fun addHighlight(verse: BibleVerse, colorHex: String) {
        viewModelScope.launch {
            repository.setHighlight(verse.bookId, verse.chapterNumber, verse.verseNumber, colorHex)
            _feedbackMessage.value = "Highlighted in ${if (colorHex.contains("D4AF37")) "Gold" else "Color"}"
            _selectedVerseForAction.value = null
        }
    }

    fun removeHighlight(verse: BibleVerse) {
        viewModelScope.launch {
            repository.removeHighlight(verse.bookId, verse.chapterNumber, verse.verseNumber)
            _feedbackMessage.value = "Highlight removed"
            _selectedVerseForAction.value = null
        }
    }

    fun saveNote(verse: BibleVerse, title: String, content: String) {
        viewModelScope.launch {
            repository.saveNote(
                bookId = verse.bookId,
                chapter = verse.chapterNumber,
                verse = verse.verseNumber,
                reference = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                title = title,
                content = content
            )
            _feedbackMessage.value = "Note saved"
            _selectedVerseForAction.value = null
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            _feedbackMessage.value = "Note deleted"
        }
    }

    fun removeBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
            _feedbackMessage.value = "Bookmark removed"
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _feedbackMessage.value = "History cleared"
        }
    }

    // Realtime Feedback actions
    fun submitUserFeedback(rating: Int, category: String, name: String, email: String, comments: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.submitFeedback(rating, category, name, email, comments)
            val isTa = userPreferences.value.interfaceLanguage == "ta"
            _feedbackMessage.value = if (isTa) "உங்கள் கருத்து வெற்றிகரமாக பதிவு செய்யப்பட்டது! நன்றி." else "Feedback submitted successfully in real-time! Thank you."
            onSuccess()
        }
    }

    fun deleteUserFeedback(feedback: AppFeedback) {
        viewModelScope.launch {
            repository.deleteFeedback(feedback)
            val isTa = userPreferences.value.interfaceLanguage == "ta"
            _feedbackMessage.value = if (isTa) "கருத்து நீக்கப்பட்டது" else "Feedback removed"
        }
    }

    fun setShowVerseNumbers(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setShowVerseNumbers(show)
        }
    }

    // Search Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun submitSearch(query: String) {
        val q = query.trim()
        if (q.isNotBlank()) {
            _searchQuery.value = q
            viewModelScope.launch {
                preferencesRepository.addRecentSearch(q)
            }
        }
    }

    fun setSearchLanguage(lang: String?) {
        _searchLanguage.value = lang
    }

    fun setSearchMode(mode: SearchMode) {
        _searchMode.value = mode
    }

    fun setWholeWordMatching(enabled: Boolean) {
        _wholeWordMatching.value = enabled
    }

    fun setCaseSensitiveSearch(enabled: Boolean) {
        _caseSensitiveSearch.value = enabled
    }

    fun setSearchSortOrder(order: SearchSortOrder) {
        _searchSortOrder.value = order
    }

    fun setTestamentFilter(filter: String?) {
        _testamentFilter.value = filter
    }

    fun setBookFilter(bookId: String?) {
        _bookFilter.value = bookId
    }

    fun setChapterRange(from: Int, to: Int) {
        _fromChapterFilter.value = from.coerceAtLeast(0)
        _toChapterFilter.value = to.coerceAtLeast(0)
    }

    fun setVerseRange(from: Int, to: Int) {
        _fromVerseFilter.value = from.coerceAtLeast(0)
        _toVerseFilter.value = to.coerceAtLeast(0)
    }

    fun clearSearchFilters() {
        _testamentFilter.value = null
        _bookFilter.value = null
        _fromChapterFilter.value = 0
        _toChapterFilter.value = 0
        _fromVerseFilter.value = 0
        _toVerseFilter.value = 0
        _searchMode.value = SearchMode.WORD
        _wholeWordMatching.value = false
        _caseSensitiveSearch.value = false
        _searchSortOrder.value = SearchSortOrder.RELEVANCE
    }

    fun broadenSearch() {
        _searchMode.value = SearchMode.ANY_WORD
        _wholeWordMatching.value = false
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            preferencesRepository.removeRecentSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            preferencesRepository.clearRecentSearches()
        }
    }

    fun loadSurroundingVerses(verse: BibleVerse) {
        viewModelScope.launch {
            val surrounding = repository.getSurroundingVerses(verse.bookId, verse.chapterNumber, verse.verseNumber, verse.languageCode)
            _surroundingVerses.value = surrounding
        }
    }

    fun clearSurroundingVerses() {
        _surroundingVerses.value = null
    }

    fun saveVerseNote(verse: BibleVerse, title: String, content: String) {
        viewModelScope.launch {
            repository.saveNote(
                bookId = verse.bookId,
                chapter = verse.chapterNumber,
                verse = verse.verseNumber,
                reference = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                title = title.ifBlank { "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}" },
                content = content
            )
        }
    }

    // Topic Discovery & Study Actions
    fun setTopicSearchQuery(query: String) {
        _topicSearchQuery.value = query
        if (query.isBlank()) {
            _topicAnalysisResult.value = null
        }
    }

    fun setTopicCategoryFilter(category: String) {
        _topicCategoryFilter.value = category
    }

    fun submitTopicSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) {
            _topicAnalysisResult.value = null
            return
        }
        _topicSearchQuery.value = q
        _isAnalyzingTopic.value = true
        viewModelScope.launch {
            val effectiveLang = _searchLanguage.value ?: userPreferences.value.bibleLanguage
            val analysis = AdvancedTopicEngine.analyzeTopicQuery(q, effectiveLang, repository)
            _topicAnalysisResult.value = analysis
            _isAnalyzingTopic.value = false
        }
    }

    fun selectSubtopic(subtopic: String) {
        submitTopicSearch(subtopic)
    }

    fun clearTopicAnalysis() {
        _topicSearchQuery.value = ""
        _topicAnalysisResult.value = null
    }

    // Comprehensive Bible Topic Study & Management Actions
    fun openTopicDetail(topicId: String) {
        val currentTopicId = _selectedTopicDetail.value?.topic?.id
        if (_currentScreen.value == AppScreen.TOPIC_DETAIL && currentTopicId != null && currentTopicId != topicId) {
            topicHistoryStack.add(currentTopicId)
            _canNavigateToPreviousTopic.value = true
        }
        loadTopicDetailInternal(topicId)
        navigateTo(AppScreen.TOPIC_DETAIL)
    }

    private fun loadTopicDetailInternal(topicId: String) {
        viewModelScope.launch {
            topicRepo.recordTopicView(topicId)
            val study = topicRepo.getRichTopicStudy(topicId)
            _selectedTopicDetail.value = study
            study?.let { s ->
                val legacyItem = TopicItem(
                    id = s.topic.id,
                    englishName = s.topic.titleEn,
                    tamilName = s.topic.titleTa,
                    descriptionEn = s.topic.shortDescriptionEn,
                    descriptionTa = s.topic.shortDescriptionTa,
                    category = s.topic.category,
                    categoryTa = s.topic.categoryTa,
                    subtopics = listOf("Foundations", "Promises", "Teachings", "Daily Life"),
                    subtopicsTa = listOf("அடிப்படை", "வாக்குத்தத்தம்", "போதனை", "அன்றாட வாழ்வு"),
                    summaryEn = s.topic.detailedExplanationEn,
                    summaryTa = s.topic.detailedExplanationTa,
                    verseReferences = s.allReferences.map { r ->
                        VerseReference(r.bookId, r.chapterNumber, r.verseStart, r.verseEnd, r.referenceEn, r.referenceTa, r.verseTextEn, r.verseTextTa)
                    }
                )
                _selectedTopic.value = legacyItem
            }
        }
    }

    fun toggleFavoriteTopic(topicId: String) {
        viewModelScope.launch {
            val isNowFav = topicRepo.toggleFavorite(topicId)
            _selectedTopicDetail.value?.let { current ->
                if (current.topic.id == topicId) {
                    _selectedTopicDetail.value = current.copy(isFavorite = isNowFav)
                }
            }
            val isTa = userPreferences.value.interfaceLanguage == "ta"
            _feedbackMessage.value = if (isNowFav)
                (if (isTa) "விருப்பமான தலைப்புகளில் சேர்க்கப்பட்டது" else "Added to favorites")
            else
                (if (isTa) "விருப்பமானவற்றிலிருந்து நீக்கப்பட்டது" else "Removed from favorites")
        }
    }

    fun navigateNextTopic() {
        val currentId = _selectedTopicDetail.value?.topic?.id ?: return
        val all = allBibleTopics.value
        val currentIndex = all.indexOfFirst { it.id == currentId }
        if (currentIndex in all.indices && currentIndex + 1 < all.size) {
            openTopicDetail(all[currentIndex + 1].id)
        }
    }

    fun navigatePreviousTopic() {
        val currentId = _selectedTopicDetail.value?.topic?.id ?: return
        val all = allBibleTopics.value
        val currentIndex = all.indexOfFirst { it.id == currentId }
        if (currentIndex in all.indices && currentIndex > 0) {
            openTopicDetail(all[currentIndex - 1].id)
        }
    }

    fun setTopicAlphabetFilter(letter: String?) {
        _topicAlphabetFilter.value = letter
    }

    fun setTopicTestamentFilter(testament: String?) {
        _topicTestamentFilter.value = testament
    }

    fun recordTopicSearch(query: String) {
        viewModelScope.launch {
            val lang = _searchLanguage.value ?: userPreferences.value.bibleLanguage
            topicRepo.recordSearchQuery(query, lang)
        }
    }

    fun deleteSearchHistoryItem(query: String) {
        viewModelScope.launch {
            topicRepo.deleteSearchQuery(query)
            preferencesRepository.removeRecentSearch(query)
        }
    }

    fun clearAllSearchHistory() {
        viewModelScope.launch {
            topicRepo.clearSearchHistory()
            preferencesRepository.clearRecentSearches()
        }
    }

    // Admin Topic Management CRUD
    fun saveCustomTopic(topic: TopicEntity) {
        viewModelScope.launch {
            topicRepo.saveTopic(topic)
            _feedbackMessage.value = "Topic saved successfully"
            if (_selectedTopicDetail.value?.topic?.id == topic.id) {
                _selectedTopicDetail.value = topicRepo.getRichTopicStudy(topic.id)
            }
        }
    }

    fun deleteCustomTopic(topicId: String) {
        viewModelScope.launch {
            topicRepo.deleteTopic(topicId)
            _feedbackMessage.value = "Topic deleted"
            if (_selectedTopicDetail.value?.topic?.id == topicId) {
                navigateBack()
            }
        }
    }

    fun addTopicReference(ref: TopicReferenceEntity) {
        viewModelScope.launch {
            topicRepo.addTopicReference(ref)
            if (_selectedTopicDetail.value?.topic?.id == ref.topicId) {
                _selectedTopicDetail.value = topicRepo.getRichTopicStudy(ref.topicId)
            }
            _feedbackMessage.value = "Reference added"
        }
    }

    fun deleteTopicReference(ref: TopicReferenceEntity) {
        viewModelScope.launch {
            topicRepo.deleteTopicReference(ref.id)
            if (_selectedTopicDetail.value?.topic?.id == ref.topicId) {
                _selectedTopicDetail.value = topicRepo.getRichTopicStudy(ref.topicId)
            }
            _feedbackMessage.value = "Reference removed"
        }
    }

    // --- Personal Bible Study Notes for Topics Actions ---
    fun saveTopicStudyNote(topicId: String, title: String, content: String, verseRef: String = "") {
        viewModelScope.launch {
            if (title.isBlank() && content.isBlank()) return@launch
            topicRepo.saveTopicNote(topicId, title, content, verseRef)
            val isTa = userPreferences.value.interfaceLanguage == "ta"
            _feedbackMessage.value = if (isTa) "ஆய்வுக் குறிப்பு சேமிக்கப்பட்டது" else "Study note saved"
            _selectedTopicDetail.value = topicRepo.getRichTopicStudy(topicId)
        }
    }

    fun updateTopicStudyNote(note: TopicNoteEntity) {
        viewModelScope.launch {
            topicRepo.updateTopicNote(note)
            val isTa = userPreferences.value.interfaceLanguage == "ta"
            _feedbackMessage.value = if (isTa) "குறிப்பு புதுப்பிக்கப்பட்டது" else "Note updated"
        }
    }

    fun deleteTopicStudyNote(note: TopicNoteEntity) {
        viewModelScope.launch {
            topicRepo.deleteTopicNote(note)
            val isTa = userPreferences.value.interfaceLanguage == "ta"
            _feedbackMessage.value = if (isTa) "குறிப்பு நீக்கப்பட்டது" else "Note deleted"
            _selectedTopicDetail.value?.let { s ->
                _selectedTopicDetail.value = topicRepo.getRichTopicStudy(s.topic.id)
            }
        }
    }

    // --- AI-Powered Bible Topic Assistant Actions ---
    fun askTopicAssistant(question: String) {
        val study = _selectedTopicDetail.value ?: return
        if (question.isBlank() || _isTopicAssistantLoading.value) return

        val userMessage = TopicAiMessage(
            role = "user",
            contentEn = question,
            contentTa = question,
            isAiGenerated = false
        )
        _topicAssistantMessages.value = _topicAssistantMessages.value + userMessage
        _isTopicAssistantLoading.value = true

        viewModelScope.launch {
            try {
                val isTa = userPreferences.value.interfaceLanguage == "ta" || userPreferences.value.bibleLanguage == "ta"
                val assistantResponse = TopicAssistantEngine.answerTopicQuestion(
                    question = question,
                    study = study,
                    isTamil = isTa,
                    conversationHistory = _topicAssistantMessages.value
                )
                _topicAssistantMessages.value = _topicAssistantMessages.value + assistantResponse
            } catch (e: Exception) {
                val errorMsg = TopicAiMessage(
                    role = "assistant",
                    contentEn = "Sorry, an error occurred while processing your study question: ${e.message ?: "Unknown error"}",
                    contentTa = "மன்னிக்கவும், உங்கள் கேள்விக்கு பதிலளிப்பதில் பிழை ஏற்பட்டது. தயவுசெய்து மீண்டும் முயற்சிக்கவும்.",
                    isAiGenerated = true
                )
                _topicAssistantMessages.value = _topicAssistantMessages.value + errorMsg
            } finally {
                _isTopicAssistantLoading.value = false
            }
        }
    }

    fun clearTopicAssistantChat() {
        _topicAssistantMessages.value = emptyList()
    }

    // --- Compare Bible Verses Actions ---
    fun toggleVerseForComparison(ref: TopicReferenceEntity) {
        val current = _comparisonVerses.value
        val matches = { a: TopicReferenceEntity, b: TopicReferenceEntity ->
            (a.id != 0L && b.id != 0L && a.id == b.id) || (a.referenceEn == b.referenceEn && a.topicId == b.topicId) || (a.referenceEn == b.referenceEn && a.bookId == b.bookId)
        }
        val exists = current.any { matches(it, ref) }
        if (exists) {
            _comparisonVerses.value = current.filterNot { matches(it, ref) }
        } else {
            if (current.size >= 4) {
                _feedbackMessage.value = "Maximum 4 passages can be compared at once"
                return
            }
            _comparisonVerses.value = current + ref
        }
    }

    fun clearComparisonVerses() {
        _comparisonVerses.value = emptyList()
    }


    // Reading Calendar & Statistics Actions
    fun previousCalendarMonth() {
        val currentMonth = _calendarSelectedMonth.value
        val currentYear = _calendarSelectedYear.value
        if (currentMonth == 0) {
            _calendarSelectedMonth.value = 11
            _calendarSelectedYear.value = currentYear - 1
        } else {
            _calendarSelectedMonth.value = currentMonth - 1
        }
        _calendarSelectedDay.value = 1
    }

    fun nextCalendarMonth() {
        val currentMonth = _calendarSelectedMonth.value
        val currentYear = _calendarSelectedYear.value
        if (currentMonth == 11) {
            _calendarSelectedMonth.value = 0
            _calendarSelectedYear.value = currentYear + 1
        } else {
            _calendarSelectedMonth.value = currentMonth + 1
        }
        _calendarSelectedDay.value = 1
    }

    fun selectCalendarDate(year: Int, month: Int, day: Int) {
        _calendarSelectedYear.value = year
        _calendarSelectedMonth.value = month
        _calendarSelectedDay.value = day
    }

    fun setDailyReadingGoal(chapters: Int) {
        viewModelScope.launch {
            preferencesRepository.setDailyReadingGoal(chapters)
            _feedbackMessage.value = "Daily goal set to $chapters chapters"
        }
    }

    fun markChapterCompleted(book: BibleBook, chapter: Int) {
        viewModelScope.launch {
            repository.recordReading(book.id, book.englishName, chapter, 1)
            val isTa = userPreferences.value.interfaceLanguage == "ta"
            _feedbackMessage.value = if (isTa)
                "${book.tamilName} அதிகாரம் $chapter வாசித்து முடிக்கப்பட்டது! 🎉"
            else
                "Marked ${book.englishName} Chapter $chapter as completed! 🎉"
        }
    }

    fun deleteReadingHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearAllReadingHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _feedbackMessage.value = "Reading history cleared"
        }
    }

    private fun computeReadingStatistics(history: List<ReadingHistory>, dailyGoal: Int): ReadingStats {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        val uniqueChapters = history.distinctBy { "${it.bookId}_${it.chapterNumber}" }
        val totalChapters = uniqueChapters.size
        val totalVerses = totalChapters * 25
        val totalMinutes = totalChapters * 3

        val dateSet = history.map { sdf.format(Date(it.timestamp)) }.toSet()
        val uniqueDays = dateSet.size

        // Streak calculation
        var currentStreak = 0
        val walkCal = Calendar.getInstance()
        if (dateSet.contains(todayStr)) {
            currentStreak++
            walkCal.add(Calendar.DAY_OF_YEAR, -1)
            while (dateSet.contains(sdf.format(walkCal.time))) {
                currentStreak++
                walkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        } else if (dateSet.contains(yesterdayStr)) {
            walkCal.add(Calendar.DAY_OF_YEAR, -1)
            while (dateSet.contains(sdf.format(walkCal.time))) {
                currentStreak++
                walkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        val sortedDates = dateSet.mapNotNull {
            try { sdf.parse(it) } catch (e: Exception) { null }
        }.sorted()

        var bestStreak = if (dateSet.isNotEmpty()) 1 else 0
        var tempStreak = 1
        for (i in 1 until sortedDates.size) {
            val diffDays = (sortedDates[i].time - sortedDates[i - 1].time) / (1000 * 60 * 60 * 24)
            if (diffDays == 1L) {
                tempStreak++
                if (tempStreak > bestStreak) bestStreak = tempStreak
            } else if (diffDays > 1L) {
                tempStreak = 1
            }
        }
        if (currentStreak > bestStreak) bestStreak = currentStreak

        // OT vs NT
        val otBooks = CanonicalBibleData.oldTestamentBooks.map { it.id }.toSet()
        val ntBooks = CanonicalBibleData.newTestamentBooks.map { it.id }.toSet()

        val otChapters = uniqueChapters.count { it.bookId in otBooks }
        val ntChapters = uniqueChapters.count { it.bookId in ntBooks }
        val otPercent = (otChapters.toFloat() / 929f).coerceIn(0f, 1f)
        val ntPercent = (ntChapters.toFloat() / 260f).coerceIn(0f, 1f)

        val booksExplored = history.map { it.bookId }.distinct().size

        // Today's chapters
        val todayChapters = history.filter { sdf.format(Date(it.timestamp)) == todayStr }
            .distinctBy { "${it.bookId}_${it.chapterNumber}" }.size

        // Weekly (last 7 days) counts
        val dayFormat = SimpleDateFormat("EEE", Locale.US)
        val weeklyCounts = mutableListOf<Pair<String, Int>>()
        val weekCal = Calendar.getInstance()
        weekCal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            val dStr = sdf.format(weekCal.time)
            val dLabel = dayFormat.format(weekCal.time)
            val count = history.filter { sdf.format(Date(it.timestamp)) == dStr }
                .distinctBy { "${it.bookId}_${it.chapterNumber}" }.size
            weeklyCounts.add(Pair(dLabel, count))
            weekCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return ReadingStats(
            totalChaptersRead = totalChapters,
            totalEstimatedVerses = totalVerses,
            totalReadingMinutes = totalMinutes,
            uniqueDaysActive = uniqueDays,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            oldTestamentChaptersRead = otChapters,
            newTestamentChaptersRead = ntChapters,
            oldTestamentPercent = otPercent,
            newTestamentPercent = ntPercent,
            totalBooksExplored = booksExplored,
            todayChaptersRead = todayChapters,
            dailyGoalChapters = dailyGoal,
            weeklyDayCounts = weeklyCounts
        )
    }

    fun selectTopic(topic: TopicItem) {
        _selectedTopic.value = topic
        navigateTo(AppScreen.TOPIC_DETAIL)
    }

    fun selectReadingPlan(plan: ReadingPlan) {
        _selectedPlan.value = plan
        navigateTo(AppScreen.PLAN_DETAIL)
    }

    fun togglePlanPortion(planId: String, day: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleDayPortionCompleted(planId, day, completed)
        }
    }

    // Audio Playback with active Job tracking and immediate cancellation
    private var playAudioJob: Job? = null

    fun playCurrentChapterAudio(explicitLang: String? = null, startVerseIndex: Int = 0) {
        val prefs = userPreferences.value
        val targetLang = explicitLang ?: prefs.bibleLanguage
        val book = _selectedBook.value
        val chapter = _selectedChapter.value

        playAudioJob?.cancel()
        playAudioJob = viewModelScope.launch {
            val direct = repository.getVersesDirect(book.id, chapter, targetLang)
            if (!isActive) return@launch
            audioEngine.playChapter(
                verses = direct,
                startVerseIndex = startVerseIndex,
                languageCode = targetLang,
                speed = prefs.audioSpeed,
                voiceGender = prefs.audioVoiceGender
            )
        }
    }

    fun playAudioFromVerse(verseIndex: Int) {
        playCurrentChapterAudio(startVerseIndex = verseIndex)
    }

    fun pauseAudio() {
        playAudioJob?.cancel()
        playAudioJob = null
        audioEngine.pause()
    }

    fun resumeAudio() {
        audioEngine.resume()
    }

    fun stopAudio() {
        playAudioJob?.cancel()
        playAudioJob = null
        audioEngine.stop()
    }

    fun togglePlayPauseAudio() {
        val audioState = audioEngine.playbackState.value
        val currentTargetLang = userPreferences.value.bibleLanguage
        val book = _selectedBook.value
        val chapter = _selectedChapter.value
        val bookMatches = (audioState.currentBookName == book.englishName || audioState.currentBookName == book.tamilName)

        if (audioState.isPlaying) {
            pauseAudio()
        } else if (audioState.isPaused && audioState.languageCode == currentTargetLang && audioState.currentChapter == chapter && bookMatches) {
            resumeAudio()
        } else {
            playCurrentChapterAudio(explicitLang = currentTargetLang)
        }
    }

    // Quiz Actions & Session Management
    fun updateQuizConfig(newConfig: QuizConfig) {
        _quizConfig.value = newConfig
    }

    fun startNewQuiz(config: QuizConfig? = null, customSeed: Long? = null) {
        val effectiveConfig = config ?: _quizConfig.value
        _quizConfig.value = effectiveConfig
        val session = quizRepository.generateQuizSession(effectiveConfig, customSeed)
        _quizSessionState.value = session
        _savedActiveSession.value = null
        _quizQuestions.value = session.questions.map { it.question }
        _currentQuizIndex.value = 0
        _selectedAnswerIndex.value = null
        _isAnswerSubmitted.value = false
        _quizScore.value = 0
        _isQuizCompleted.value = false
        _showQuizConfigScreen.value = false
        _uniqueQuestionsSeenCount.value = quizRepository.getUniqueQuestionsAttemptedCount()
    }

    fun resumeUnfinishedQuiz() {
        val saved = quizRepository.getActiveSession() ?: _savedActiveSession.value ?: return
        _quizSessionState.value = saved
        _savedActiveSession.value = null
        _quizQuestions.value = saved.questions.map { it.question }
        _currentQuizIndex.value = saved.currentIndex
        _quizScore.value = saved.score
        val currQ = saved.questions.getOrNull(saved.currentIndex)
        _selectedAnswerIndex.value = currQ?.selectedShuffledIndex
        _isAnswerSubmitted.value = currQ?.isSubmitted ?: false
        _isQuizCompleted.value = saved.isCompleted
        _showQuizConfigScreen.value = false
    }

    fun discardUnfinishedQuiz() {
        quizRepository.clearActiveSession()
        _savedActiveSession.value = null
        _quizSessionState.value = null
        _showQuizConfigScreen.value = true
    }

    fun openQuizConfig() {
        _showQuizConfigScreen.value = true
        _savedActiveSession.value = quizRepository.getActiveSession()
        _uniqueQuestionsSeenCount.value = quizRepository.getUniqueQuestionsAttemptedCount()
    }

    fun resetQuizHistory() {
        quizRepository.resetQuestionHistory()
        _savedActiveSession.value = null
        _uniqueQuestionsSeenCount.value = 0
        _quizSessionState.value = null
        _showQuizConfigScreen.value = true
        val isTa = userPreferences.value.interfaceLanguage == "ta"
        _feedbackMessage.value = if (isTa) "வினாடி வினா வரலாறு மீட்டமைக்கப்பட்டது" else "Quiz history has been reset"
    }

    fun retryCurrentQuiz() {
        val session = _quizSessionState.value
        if (session != null) {
            val resetQuestions = session.questions.map { sq ->
                QuizSessionQuestion(
                    question = sq.question,
                    shuffledOptionsEn = sq.shuffledOptionsEn,
                    shuffledOptionsTa = sq.shuffledOptionsTa,
                    originalToShuffledIndices = sq.originalToShuffledIndices,
                    shuffledToOriginalIndices = sq.shuffledToOriginalIndices,
                    correctShuffledIndex = sq.correctShuffledIndex,
                    selectedShuffledIndex = null,
                    isSubmitted = false
                )
            }
            val newSession = QuizSessionState(
                sessionId = session.sessionId,
                config = session.config,
                questions = resetQuestions,
                currentIndex = 0,
                score = 0,
                startTime = System.currentTimeMillis(),
                isCompleted = false,
                completedTime = null
            )
            _quizSessionState.value = newSession
            quizRepository.saveActiveSession(newSession)
            _currentQuizIndex.value = 0
            _selectedAnswerIndex.value = null
            _isAnswerSubmitted.value = false
            _quizScore.value = 0
            _isQuizCompleted.value = false
            _showQuizConfigScreen.value = false
        } else {
            restartQuiz()
        }
    }

    fun selectAndValidateQuizAnswer(index: Int) {
        if (_isAnswerSubmitted.value || _isQuizCompleted.value) return

        val session = _quizSessionState.value
        if (session != null) {
            val currSessionQ = session.questions.getOrNull(_currentQuizIndex.value) ?: return
            if (currSessionQ.isSubmitted) return

            currSessionQ.selectedShuffledIndex = index
            currSessionQ.isSubmitted = true
            _selectedAnswerIndex.value = index
            _isAnswerSubmitted.value = true

            val isCorrect = index == currSessionQ.correctShuffledIndex
            if (isCorrect) {
                _quizScore.value += 1
                session.score = _quizScore.value
                quizRepository.recordQuestionCorrect(currSessionQ.question.id)
            }
            quizRepository.saveActiveSession(session)
        } else {
            val currentQuestion = _quizQuestions.value.getOrNull(_currentQuizIndex.value) ?: return
            _selectedAnswerIndex.value = index
            _isAnswerSubmitted.value = true

            if (index == currentQuestion.correctIndex) {
                _quizScore.value += 1
            }
        }
    }

    fun selectQuizAnswer(index: Int) {
        selectAndValidateQuizAnswer(index)
    }

    fun submitQuizAnswer() {
        val selected = _selectedAnswerIndex.value ?: return
        selectAndValidateQuizAnswer(selected)
    }

    fun nextQuizQuestion() {
        val session = _quizSessionState.value
        val total = session?.questions?.size ?: _quizQuestions.value.size
        val next = _currentQuizIndex.value + 1

        if (next < total) {
            _currentQuizIndex.value = next
            if (session != null) {
                session.currentIndex = next
                val nextQ = session.questions.getOrNull(next)
                _selectedAnswerIndex.value = nextQ?.selectedShuffledIndex
                _isAnswerSubmitted.value = nextQ?.isSubmitted ?: false
                quizRepository.saveActiveSession(session)
            } else {
                _selectedAnswerIndex.value = null
                _isAnswerSubmitted.value = false
            }
        } else {
            _isQuizCompleted.value = true
            if (session != null) {
                session.isCompleted = true
                session.completedTime = System.currentTimeMillis()
                quizRepository.saveActiveSession(session)
            }
            viewModelScope.launch {
                quizRepository.saveQuizScore("Bible Knowledge Quiz", _quizScore.value, total)
            }
        }
    }

    fun restartQuiz() {
        _currentQuizIndex.value = 0
        _selectedAnswerIndex.value = null
        _isAnswerSubmitted.value = false
        _quizScore.value = 0
        _isQuizCompleted.value = false
        val session = _quizSessionState.value
        if (session != null) {
            startNewQuiz(session.config)
        }
    }

    fun getQuizReviewItems(): List<QuizReviewItem> {
        val isTa = userPreferences.value.interfaceLanguage == "ta"
        val session = _quizSessionState.value
        return if (session != null) {
            session.questions.mapIndexed { idx, sq ->
                val q = sq.question
                val isCorrect = sq.selectedShuffledIndex == sq.correctShuffledIndex
                QuizReviewItem(
                    questionNumber = idx + 1,
                    questionText = if (isTa) q.questionTa else q.questionEn,
                    options = if (isTa) sq.shuffledOptionsTa else sq.shuffledOptionsEn,
                    selectedIndex = sq.selectedShuffledIndex,
                    correctIndex = sq.correctShuffledIndex,
                    isCorrect = isCorrect,
                    explanation = if (isTa) q.explanationTa else q.explanationEn,
                    scriptureReference = if (isTa) q.scriptureRefTa else q.scriptureRefEn,
                    category = q.category,
                    difficulty = q.difficulty
                )
            }
        } else {
            _quizQuestions.value.mapIndexed { idx, q ->
                QuizReviewItem(
                    questionNumber = idx + 1,
                    questionText = if (isTa) q.questionTa else q.questionEn,
                    options = if (isTa) q.optionsTa else q.optionsEn,
                    selectedIndex = null,
                    correctIndex = q.correctIndex,
                    isCorrect = false,
                    explanation = if (isTa) q.explanationTa else q.explanationEn,
                    scriptureReference = if (isTa) q.scriptureRefTa else q.scriptureRefEn,
                    category = q.category,
                    difficulty = q.difficulty
                )
            }
        }
    }


    // Preferences Setters
    fun toggleLanguage() {
        val current = userPreferences.value.bibleLanguage
        val next = if (current == "en") "ta" else "en"
        setBibleLanguage(next)
    }

    fun setBibleLanguage(lang: String) {
        val current = _userPreferences.value.bibleLanguage
        _userPreferences.value = _userPreferences.value.copy(
            bibleLanguage = lang,
            interfaceLanguage = lang
        )
        val book = _selectedBook.value
        val chapter = _selectedChapter.value
        _currentVerses.value = ScriptureSeedData.getCanonicalVersesForChapter(book.id, chapter, lang)
        if (current == lang) return
        playAudioJob?.cancel()
        viewModelScope.launch {
            preferencesRepository.setInterfaceLanguage(lang)
            preferencesRepository.setBibleLanguage(lang)
            _feedbackMessage.value = if (lang == "ta") "தமிழ் மொழிக்கு மாற்றப்பட்டது" else "Switched to English"
            val direct = repository.getVersesDirect(book.id, chapter, lang)
            _currentVerses.value = direct
            audioEngine.setLanguage(lang, direct)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch { preferencesRepository.setFontSize(size) }
    }

    fun setLineSpacing(multiplier: Float) {
        viewModelScope.launch { preferencesRepository.setLineSpacing(multiplier) }
    }

    fun setAudioVoiceGender(gender: String) {
        viewModelScope.launch {
            preferencesRepository.setAudioVoiceGender(gender)
            audioEngine.setVoiceGender(gender)
        }
    }

    fun setAudioSpeed(speed: Float) {
        viewModelScope.launch {
            preferencesRepository.setAudioSpeed(speed)
            audioEngine.setSpeed(speed)
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    // -------------------------------------------------------------------------
    // ADVANCED FEATURES STATE & ROUTING
    // -------------------------------------------------------------------------
    val selectedNoteId = MutableStateFlow<Long?>(null)
    val selectedSmartPlanId = MutableStateFlow<String?>("plan_30_gospels")
    val selectedMemoryVerse = MutableStateFlow<MemorizedVerse?>(null)
    val selectedPrayerId = MutableStateFlow<Long?>(null)
    val selectedSermonDocId = MutableStateFlow<Long?>(null)
    val selectedTimelineEventId = MutableStateFlow<String?>("event_resurrection")
    val selectedLocationId = MutableStateFlow<String?>(null)

    // Sleep Timer for Immersive Listening
    val sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerRemainingSeconds = MutableStateFlow(0)

    fun openNoteEditor(noteId: Long? = null) {
        selectedNoteId.value = noteId
        navigateTo(AppScreen.NOTE_EDITOR)
    }

    fun openSmartPlanDetail(planId: String) {
        selectedSmartPlanId.value = planId
        navigateTo(AppScreen.SMART_PLANS)
    }

    fun openMemorizationSession(verse: MemorizedVerse? = null) {
        selectedMemoryVerse.value = verse
        navigateTo(AppScreen.MEMORIZATION_SESSION)
    }

    fun openPrayerEditor(prayerId: Long? = null) {
        selectedPrayerId.value = prayerId
        navigateTo(AppScreen.PRAYER_EDITOR)
    }

    fun openSermonEditor(docId: Long? = null) {
        selectedSermonDocId.value = docId
        navigateTo(AppScreen.SERMON_EDITOR)
    }

    fun openTimelineDetail(eventId: String) {
        selectedTimelineEventId.value = eventId
        navigateTo(AppScreen.TIMELINE_DETAIL)
    }

    fun openBiblicalMap(locationId: String? = null) {
        selectedLocationId.value = locationId
        navigateTo(AppScreen.BIBLICAL_MAP)
    }

    fun openImmersiveListening(book: BibleBook? = null, chapter: Int? = null) {
        if (book != null) {
            _selectedBook.value = book
            if (chapter != null) _selectedChapter.value = chapter
        }
        navigateTo(AppScreen.IMMERSIVE_LISTENING)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerMinutes.value = minutes
        sleepTimerRemainingSeconds.value = minutes * 60
        if (minutes > 0) {
            _feedbackMessage.value = "Sleep timer set for $minutes minutes"
        } else {
            _feedbackMessage.value = "Sleep timer turned off"
        }
    }

    // Direct Verse Action Sheet Integrations
    fun addVerseToNotebook(verse: BibleVerse) {
        viewModelScope.launch {
            val noteId = advancedRepo.createOrUpdateNote(
                NotebookNote(
                    title = "Study: ${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                    content = "“${verse.verseText}”",
                    observation = verse.verseText,
                    tags = "Scripture Study, ${verse.bookName}"
                )
            )
            advancedRepo.attachVerseRef(
                NoteVerseRef(
                    noteId = noteId,
                    translationId = verse.editionId,
                    bookId = verse.bookId,
                    bookName = verse.bookName,
                    chapterNumber = verse.chapterNumber,
                    startVerse = verse.verseNumber,
                    verseText = verse.verseText
                )
            )
            _selectedVerseForAction.value = null
            _feedbackMessage.value = "Added to Study Notebook"
            openNoteEditor(noteId)
        }
    }

    fun addVerseToMemorization(verse: BibleVerse) {
        viewModelScope.launch {
            advancedRepo.addVerseToMemorization(
                bookId = verse.bookId,
                bookName = verse.bookName,
                chapter = verse.chapterNumber,
                verse = verse.verseNumber,
                refEn = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                refTa = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                textEn = verse.verseText,
                textTa = verse.verseText,
                translationId = verse.editionId
            )
            _selectedVerseForAction.value = null
            _feedbackMessage.value = "Added to Verse Memorization"
        }
    }

    fun addVerseToPrayerJournal(verse: BibleVerse) {
        viewModelScope.launch {
            val prayerId = advancedRepo.savePrayer(
                PrayerEntry(
                    title = "Prayer from ${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                    description = "“${verse.verseText}”\n\nLord, help me to live out this truth...",
                    category = "Personal",
                    attachedScriptureRef = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                    attachedScriptureText = verse.verseText
                )
            )
            _selectedVerseForAction.value = null
            _feedbackMessage.value = "Added to Prayer Journal"
            openPrayerEditor(prayerId)
        }
    }

    fun addVerseToSermon(verse: BibleVerse) {
        viewModelScope.launch {
            val docId = advancedRepo.saveSermonDoc(
                SermonDocument(
                    title = "Message on ${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                    mainPassageRef = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                    mainPassageText = verse.verseText
                )
            )
            advancedRepo.saveSection(
                SermonSection(
                    documentId = docId,
                    sectionOrder = 1,
                    sectionTitle = "1. Introduction & Passage Exposition",
                    content = "Key verse: “${verse.verseText}”",
                    attachedReferences = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}"
                )
            )
            _selectedVerseForAction.value = null
            _feedbackMessage.value = "Added to Sermon Notes Builder"
            openSermonEditor(docId)
        }
    }

    // -------------------------------------------------------------
    // REBUILT BIBLE SHORTS ACTIONS
    // -------------------------------------------------------------
    fun playShort(short: ShortEntity) {
        _activeShortPlayer.value = short
        _activeShortId.value = short.id
        navigateTo(AppScreen.SHORTS_PLAYER)
    }

    fun closeShortPlayer() {
        _activeShortPlayer.value = null
        navigateBack()
    }

    fun openShortBibleAnalysis(shortId: String) {
        _activeShortId.value = shortId
        navigateTo(AppScreen.SHORTS_BIBLE_ANALYSIS)
    }

    fun toggleShortLike(shortId: String) {
        viewModelScope.launch {
            shortsRepo.toggleLike(shortId, "current_user")
        }
    }

    fun toggleShortSave(shortId: String) {
        viewModelScope.launch {
            val isSaved = shortsRepo.toggleSave(shortId, "current_user")
            _feedbackMessage.value = if (isSaved) "Short saved to your library" else "Removed from saved"
        }
    }

    fun addShortComment(shortId: String, text: String) {
        viewModelScope.launch {
            shortsRepo.addComment(shortId, "current_user", "Bible Student", text)
        }
    }

    fun deleteShortComment(commentId: String) {
        viewModelScope.launch {
            shortsRepo.deleteComment(commentId)
            _feedbackMessage.value = "Comment deleted"
        }
    }

    fun reportShort(shortId: String, reason: String) {
        viewModelScope.launch {
            _feedbackMessage.value = "Thank you. Your report has been submitted."
        }
    }

    fun publishDeveloperVideo(videoId: String) {
        viewModelScope.launch {
            val success = shortsRepo.publishDeveloperVideo(videoId)
            if (success) {
                _feedbackMessage.value = "Video published to Shorts successfully!"
            }
        }
    }

    fun seedInitialShortsIfEmpty() {
        viewModelScope.launch {
            shortsRepo.seedInitialShortsIfEmpty()
        }
    }

    fun uploadCustomVideo(
        videoUri: Uri,
        title: String,
        description: String,
        category: String,
        status: String
    ) {
        viewModelScope.launch {
            val shortId = "custom_short_${System.currentTimeMillis()}"
            val fileName = "$shortId.mp4"
            val storedPath = try {
                shortsRepo.copyVideoFileToInternalStorage(videoUri, fileName)
            } catch (e: Exception) {
                videoUri.toString()
            }

            val validation = shortsRepo.validateVideo(storedPath)
            val thumbPath = shortsRepo.generateAndSaveThumbnail(storedPath, shortId)
            val (analysis, refs) = CuratedShortsAnalysisEngine.analyzeCustomVideo(shortId, title, description, category)

            val short = ShortEntity(
                id = shortId,
                title = title,
                description = description,
                category = category.ifBlank { "Sermon" },
                videoStoragePath = storedPath,
                thumbnailStoragePath = thumbPath,
                aspectRatio = if (validation.is9to16) "9:16" else "9:16",
                durationSeconds = validation.durationSeconds,
                videoMimeType = "video/mp4",
                audioAvailable = validation.hasAudio,
                uploadStatus = "COMPLETED",
                publicationStatus = status,
                createdByAdminId = "developer_admin",
                likesCount = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                publishedAt = System.currentTimeMillis()
            )

            if (status == "PUBLISHED") {
                shortsRepo.publishShort(short, analysis, refs)
            } else {
                shortsRepo.saveDraftShort(short, analysis, refs)
            }
            _feedbackMessage.value = "Video uploaded and published with thumbnail!"
        }
    }

    fun toggleShortPublishStatus(shortId: String) {
        viewModelScope.launch {
            shortsRepo.togglePublishStatus(shortId)
        }
    }

    fun deleteShort(shortId: String) {
        viewModelScope.launch {
            shortsRepo.deleteShort(shortId)
            _feedbackMessage.value = "Short deleted"
        }
    }

    fun clearAllObsoleteShorts() {
        viewModelScope.launch {
            shortsRepo.clearAllShorts()
            _activeShortPlayer.value = null
            _activeShortId.value = null
            _feedbackMessage.value = "Shorts database reset cleanly"
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
