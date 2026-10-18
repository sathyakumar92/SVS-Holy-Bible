package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SvsBottomNavigationBar
import com.example.ui.components.SvsTopAppBar
import com.example.ui.screens.about.AboutScreen
import com.example.ui.screens.audio.AudioBibleScreen
import com.example.ui.screens.daily.DailyDevotionalScreen
import com.example.ui.screens.feedback.FeedbackScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.listening.ImmersiveListeningScreen
import com.example.ui.screens.memorization.MemorizationScreen
import com.example.ui.screens.memorization.MemorizationSessionScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.notebook.NoteEditorScreen
import com.example.ui.screens.notebook.NotebookScreen
import com.example.ui.screens.plans.CustomPlanBuilderScreen
import com.example.ui.screens.plans.PlanDetailScreen
import com.example.ui.screens.plans.ReadingPlansScreen
import com.example.ui.screens.plans.SmartPlansScreen
import com.example.ui.screens.prayer.PrayerEditorScreen
import com.example.ui.screens.prayer.PrayerJournalScreen
import com.example.ui.screens.quiz.BibleQuizScreen
import com.example.ui.screens.reader.BibleReaderScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.sermon.SermonBuilderScreen
import com.example.ui.screens.sermon.SermonEditorScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.shorts.BibleShortsScreen
import com.example.ui.screens.shorts.ShortsPlayerScreen
import com.example.ui.screens.shorts.ShortsBibleAnalysisScreen
import com.example.ui.screens.shorts.ShortsAdminStudioScreen
import com.example.ui.screens.timeline.BiblicalMapScreen
import com.example.ui.screens.timeline.TimelineDetailScreen
import com.example.ui.screens.timeline.TimelineScreen
import com.example.ui.screens.topics.TopicDetailScreen
import com.example.ui.screens.topics.TopicsScreen
import com.example.ui.screens.calendar.ReadingCalendarScreen
import com.example.ui.screens.welcome.WelcomeScreen
import com.example.ui.theme.SvsHolyBibleTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel

class MainActivity : ComponentActivity() {
    private var bibleViewModel: BibleViewModel? = null

    override fun onDestroy() {
        super.onDestroy()
        bibleViewModel?.audioEngine?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: BibleViewModel = viewModel()
            bibleViewModel = viewModel
            val prefs by viewModel.userPreferences.collectAsState()
            val showWelcome by viewModel.showWelcome.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val feedbackMessage by viewModel.feedbackMessage.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (prefs.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemDark
            }

            SvsHolyBibleTheme(darkTheme = isDarkTheme) {
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(feedbackMessage) {
                    feedbackMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearFeedback()
                    }
                }

                if (showWelcome) {
                    WelcomeScreen(
                        onAnimationFinished = {
                            viewModel.dismissWelcome()
                        }
                    )
                } else {
                    // System Back Handler
                    BackHandler(enabled = currentScreen != AppScreen.HOME) {
                        viewModel.navigateBack()
                    }

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("svs_holy_bible_main_scaffold"),
                        topBar = {
                            val isSubScreen = currentScreen in listOf(
                                AppScreen.TOPIC_DETAIL, AppScreen.PLAN_DETAIL,
                                AppScreen.SETTINGS, AppScreen.ABOUT, AppScreen.FEEDBACK,
                                AppScreen.NOTE_EDITOR, AppScreen.CUSTOM_PLAN_BUILDER,
                                AppScreen.MEMORIZATION_SESSION, AppScreen.PRAYER_EDITOR,
                                AppScreen.SERMON_EDITOR, AppScreen.TIMELINE_DETAIL,
                                AppScreen.BIBLICAL_MAP, AppScreen.IMMERSIVE_LISTENING
                            )
                            if (currentScreen != AppScreen.IMMERSIVE_LISTENING &&
                                currentScreen != AppScreen.READING_CALENDAR &&
                                currentScreen != AppScreen.SHORTS &&
                                currentScreen != AppScreen.SHORTS_PLAYER &&
                                currentScreen != AppScreen.SHORTS_BIBLE_ANALYSIS &&
                                currentScreen != AppScreen.SHORTS_ADMIN_STUDIO
                            ) {
                                SvsTopAppBar(
                                    title = "SVS Holy Bible",
                                    currentLanguage = prefs.bibleLanguage,
                                    onLanguageSelected = { lang -> viewModel.setBibleLanguage(lang) },
                                    onLanguageToggle = { viewModel.toggleLanguage() },
                                    onSearchClick = { viewModel.navigateTo(AppScreen.SEARCH) },
                                    onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                                    navigationIcon = if (isSubScreen) {
                                        {
                                            IconButton(onClick = { viewModel.navigateBack() }) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowBack,
                                                    contentDescription = "Back",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    } else null
                                )
                            }
                        },
                        bottomBar = {
                            if (currentScreen != AppScreen.IMMERSIVE_LISTENING &&
                                currentScreen != AppScreen.NOTE_EDITOR &&
                                currentScreen != AppScreen.PRAYER_EDITOR &&
                                currentScreen != AppScreen.SERMON_EDITOR &&
                                currentScreen != AppScreen.SHORTS_PLAYER &&
                                currentScreen != AppScreen.SHORTS_BIBLE_ANALYSIS &&
                                currentScreen != AppScreen.SHORTS_ADMIN_STUDIO
                            ) {
                                SvsBottomNavigationBar(
                                    currentScreen = currentScreen,
                                    languageCode = prefs.interfaceLanguage,
                                    onNavigate = { screen ->
                                        viewModel.navigateTo(screen)
                                    }
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (currentScreen == AppScreen.IMMERSIVE_LISTENING || currentScreen == AppScreen.READING_CALENDAR || currentScreen == AppScreen.SHORTS_PLAYER) PaddingValues(0.dp) else innerPadding)
                        ) {
                            when (currentScreen) {
                                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                                AppScreen.READER -> BibleReaderScreen(viewModel = viewModel)
                                AppScreen.SEARCH -> SearchScreen(viewModel = viewModel)
                                AppScreen.SHORTS -> BibleShortsScreen(viewModel = viewModel)
                                AppScreen.SHORTS_PLAYER -> {
                                    val shortDetails by viewModel.activeShortDetails.collectAsState()
                                    val comments by viewModel.activeShortComments.collectAsState()
                                    val publishedShorts by viewModel.publishedShorts.collectAsState()
                                    val isTamil = prefs.interfaceLanguage == "ta" || prefs.bibleLanguage == "ta"
                                    ShortsPlayerScreen(
                                        shortDetails = shortDetails,
                                        comments = comments,
                                        allShorts = publishedShorts,
                                        isTamil = isTamil,
                                        onBack = { viewModel.closeShortPlayer() },
                                        onToggleLike = { shortId -> viewModel.toggleShortLike(shortId) },
                                        onToggleSave = { shortId -> viewModel.toggleShortSave(shortId) },
                                        onOpenBibleAnalysis = { shortId -> viewModel.openShortBibleAnalysis(shortId) },
                                        onAddComment = { shortId, text -> viewModel.addShortComment(shortId, text) },
                                        onDeleteComment = { commentId -> viewModel.deleteShortComment(commentId) },
                                        onReportShort = { shortId, reason -> viewModel.reportShort(shortId, reason) },
                                        onSelectShort = { selectedShort -> viewModel.playShort(selectedShort) }
                                    )
                                }
                                AppScreen.SHORTS_BIBLE_ANALYSIS -> {
                                    val shortDetails by viewModel.activeShortDetails.collectAsState()
                                    val isTamil = prefs.interfaceLanguage == "ta" || prefs.bibleLanguage == "ta"
                                    ShortsBibleAnalysisScreen(
                                        shortDetails = shortDetails,
                                        isTamil = isTamil,
                                        onBack = { viewModel.navigateBack() },
                                        onOpenBibleReader = { bookId, chapter, verse ->
                                            viewModel.openReaderAtVerse(bookId, chapter, verse)
                                        }
                                    )
                                }
                                AppScreen.SHORTS_ADMIN_STUDIO -> {
                                    val adminShorts by viewModel.adminShorts.collectAsState()
                                    val isTamil = prefs.interfaceLanguage == "ta" || prefs.bibleLanguage == "ta"
                                    ShortsAdminStudioScreen(
                                        adminShorts = adminShorts,
                                        isTamil = isTamil,
                                        onBack = { viewModel.navigateBack() },
                                        onPublishDeveloperVideo = { videoId: String -> viewModel.publishDeveloperVideo(videoId) },
                                        onUploadCustomVideo = { uri: android.net.Uri, title: String, desc: String, cat: String, status: String ->
                                            viewModel.uploadCustomVideo(uri, title, desc, cat, status)
                                        },
                                        onTogglePublishStatus = { id: String -> viewModel.toggleShortPublishStatus(id) },
                                        onDeleteShort = { id: String -> viewModel.deleteShort(id) },
                                        onClearAllShorts = { viewModel.clearAllObsoleteShorts() }
                                    )
                                }
                                AppScreen.MORE -> MoreScreen(viewModel = viewModel)
                                AppScreen.TOPICS -> TopicsScreen(viewModel = viewModel)
                                AppScreen.TOPIC_DETAIL -> TopicDetailScreen(viewModel = viewModel)
                                AppScreen.AUDIO_PLAYER -> AudioBibleScreen(viewModel = viewModel)
                                AppScreen.DAILY_DEVOTIONAL -> DailyDevotionalScreen(viewModel = viewModel)
                                AppScreen.QUIZ -> BibleQuizScreen(viewModel = viewModel)
                                AppScreen.READING_PLANS -> ReadingPlansScreen(viewModel = viewModel)
                                AppScreen.PLAN_DETAIL -> PlanDetailScreen(viewModel = viewModel)
                                AppScreen.LIBRARY -> LibraryScreen(viewModel = viewModel)
                                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                                AppScreen.ABOUT -> AboutScreen(viewModel = viewModel)
                                AppScreen.FEEDBACK -> FeedbackScreen(viewModel = viewModel)
                                AppScreen.READING_CALENDAR -> ReadingCalendarScreen(viewModel = viewModel)

                                // SEVEN ADVANCED FEATURES ROUTING
                                AppScreen.NOTEBOOK -> NotebookScreen(viewModel = viewModel)
                                AppScreen.NOTE_EDITOR -> NoteEditorScreen(viewModel = viewModel)
                                AppScreen.SMART_PLANS -> SmartPlansScreen(viewModel = viewModel)
                                AppScreen.CUSTOM_PLAN_BUILDER -> CustomPlanBuilderScreen(viewModel = viewModel)
                                AppScreen.MEMORIZATION -> MemorizationScreen(viewModel = viewModel)
                                AppScreen.MEMORIZATION_SESSION -> MemorizationSessionScreen(viewModel = viewModel)
                                AppScreen.PRAYER_JOURNAL -> PrayerJournalScreen(viewModel = viewModel)
                                AppScreen.PRAYER_EDITOR -> PrayerEditorScreen(viewModel = viewModel)
                                AppScreen.SERMON_BUILDER -> SermonBuilderScreen(viewModel = viewModel)
                                AppScreen.SERMON_EDITOR -> SermonEditorScreen(viewModel = viewModel)
                                AppScreen.TIMELINE -> TimelineScreen(viewModel = viewModel)
                                AppScreen.TIMELINE_DETAIL -> TimelineDetailScreen(viewModel = viewModel)
                                AppScreen.BIBLICAL_MAP -> BiblicalMapScreen(viewModel = viewModel)
                                AppScreen.IMMERSIVE_LISTENING -> ImmersiveListeningScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
