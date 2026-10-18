package com.example.ui.screens.quiz

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.BibleQuizBank
import com.example.data.model.QuizConfig
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizReviewItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun BibleQuizScreen(
    viewModel: BibleViewModel
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    val session by viewModel.quizSessionState.collectAsState()
    val showConfig by viewModel.showQuizConfigScreen.collectAsState()
    val isCompleted by viewModel.isQuizCompleted.collectAsState()
    val savedSession by viewModel.savedActiveSession.collectAsState()
    val uniqueQuestionsCount by viewModel.uniqueQuestionsSeenCount.collectAsState()

    val currentQuizIndex by viewModel.currentQuizIndex.collectAsState()
    val selectedIndex by viewModel.selectedAnswerIndex.collectAsState()
    val isSubmitted by viewModel.isAnswerSubmitted.collectAsState()
    val currentScore by viewModel.quizScore.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    var showResetHistoryDialog by remember { mutableStateOf(false) }
    var isReviewingAnswers by remember { mutableStateOf(false) }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = if (isTamil) "வினாடி வினாவிலிருந்து வெளியேறவா?" else "Leave Quiz Session?",
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
            },
            text = {
                Text(
                    text = if (isTamil)
                        "உங்கள் வினாடி வினா முன்னேற்றம் தானாகவே சேமிக்கப்படும். நீங்கள் எப்போது வேண்டுமானாலும் இதைத் தொடரலாம்."
                    else
                        "Your session progress is securely saved. You can resume this quiz at any time."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        viewModel.openQuizConfig()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                ) {
                    Text(if (isTamil) "சேமித்து வெளியேறு" else "Save & Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(if (isTamil) "தொடர்க" else "Keep Playing", color = RichGold)
                }
            },
            containerColor = SecondaryNavySurface
        )
    }

    // Reset History confirmation dialog
    if (showResetHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showResetHistoryDialog = false },
            title = {
                Text(
                    text = if (isTamil) "வரலாற்றை மீட்டமைக்கவா?" else "Reset Question History?",
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            },
            text = {
                Text(
                    text = if (isTamil)
                        "இதுவரை நீங்கள் பார்த்த அனைத்து கேள்விகளின் வரலாறும் அழிக்கப்பட்டு மீண்டும் ஆரம்பத்திலிருந்தே கேள்விகள் வரும்."
                    else
                        "This will clear your question exposure history, allowing all 200+ questions to appear again from the beginning."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetHistoryDialog = false
                        viewModel.resetQuizHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_reset_history_button")
                ) {
                    Text(if (isTamil) "மீட்டமை" else "Reset History")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetHistoryDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel", color = PrimaryLightText)
                }
            },
            containerColor = SecondaryNavySurface
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
            .testTag("bible_quiz_screen")
    ) {
        when {
            // VIEW 1: Final Quiz Results Screen
            isCompleted -> {
                QuizResultsView(
                    viewModel = viewModel,
                    isTamil = isTamil,
                    isReviewingAnswers = isReviewingAnswers,
                    onToggleReview = { isReviewingAnswers = !isReviewingAnswers },
                    onStartNew = {
                        isReviewingAnswers = false
                        viewModel.openQuizConfig()
                    },
                    onRetry = {
                        isReviewingAnswers = false
                        viewModel.retryCurrentQuiz()
                    }
                )
            }

            // VIEW 2: Quiz Configuration & Setup Screen
            showConfig -> {
                QuizConfigView(
                    viewModel = viewModel,
                    isTamil = isTamil,
                    savedSession = savedSession,
                    uniqueQuestionsCount = uniqueQuestionsCount,
                    onStartQuiz = { config ->
                        viewModel.startNewQuiz(config)
                    },
                    onResumeQuiz = {
                        viewModel.resumeUnfinishedQuiz()
                    },
                    onDiscardQuiz = {
                        viewModel.discardUnfinishedQuiz()
                    },
                    onRequestResetHistory = {
                        showResetHistoryDialog = true
                    }
                )
            }

            // VIEW 3: Active In-Quiz Question Screen
            else -> {
                val questions = session?.questions ?: emptyList()
                val sessionQuestion = questions.getOrNull(currentQuizIndex)

                // Fallback to legacy single questions if session is null
                val legacyQuestion = viewModel.quizQuestions.collectAsState().value.getOrNull(currentQuizIndex)
                val effectiveTotal = if (questions.isNotEmpty()) questions.size else viewModel.quizQuestions.collectAsState().value.size

                QuizInPlayView(
                    viewModel = viewModel,
                    isTamil = isTamil,
                    currentIndex = currentQuizIndex,
                    totalQuestions = effectiveTotal,
                    sessionQuestion = sessionQuestion,
                    legacyQuestion = legacyQuestion,
                    selectedIndex = selectedIndex,
                    isSubmitted = isSubmitted,
                    score = currentScore,
                    onSelectAnswer = { index ->
                        viewModel.selectAndValidateQuizAnswer(index)
                    },
                    onNextQuestion = {
                        viewModel.nextQuizQuestion()
                    },
                    onExitClick = {
                        showExitDialog = true
                    }
                )
            }
        }
    }
}

// =============================================================================
// SUB-VIEW 1: IN-PLAY QUIZ QUESTION SCREEN
// =============================================================================

@Composable
private fun QuizInPlayView(
    viewModel: BibleViewModel,
    isTamil: Boolean,
    currentIndex: Int,
    totalQuestions: Int,
    sessionQuestion: com.example.data.model.QuizSessionQuestion?,
    legacyQuestion: com.example.data.model.QuizQuestion?,
    selectedIndex: Int?,
    isSubmitted: Boolean,
    score: Int,
    onSelectAnswer: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onExitClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val baseQuestion = sessionQuestion?.question ?: legacyQuestion ?: return

    // Animated score with floating +1 badge
    var prevScore by remember { mutableIntStateOf(score) }
    var showPlusOne by remember { mutableStateOf(false) }

    LaunchedEffect(score) {
        if (score > prevScore) {
            showPlusOne = true
            delay(1200)
            showPlusOne = false
        }
        prevScore = score
    }

    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "quiz_score_animation"
    )

    // Smooth Progress Bar Animation (Progress based strictly on answered questions!)
    val answeredCount = if (isSubmitted) (currentIndex + 1) else currentIndex
    val targetProgress = if (totalQuestions > 0) answeredCount.toFloat() / totalQuestions.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "quiz_progress_animation"
    )

    val options = when {
        sessionQuestion != null -> if (isTamil) sessionQuestion.shuffledOptionsTa else sessionQuestion.shuffledOptionsEn
        else -> if (isTamil) baseQuestion.optionsTa else baseQuestion.optionsEn
    }

    val correctIndex = sessionQuestion?.correctShuffledIndex ?: baseQuestion.correctIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(scrollState)
        ) {
            // HEADER: Quiz Title, Exit Icon, Question Counter, and Animated Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onExitClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("quiz_exit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Quiz",
                            tint = PrimaryLightText
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTamil) "வேத வினாடி வினா" else "Interactive Bible Quiz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                }

                // ANIMATED SCORE DISPLAY WITH FLOATING +1 BADGE
                Box(contentAlignment = Alignment.CenterEnd) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(SecondaryNavySurface, RoundedCornerShape(20.dp))
                            .border(1.dp, RichGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("quiz_current_score")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = RichGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${if (isTamil) "மதிப்பெண்:" else "Score:"} $animatedScore",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight
                        )
                    }

                    // Floating +1 indicator with gold pulse
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showPlusOne,
                        enter = fadeIn(tween(200)) + slideInVertically { -20 } + scaleIn(initialScale = 0.8f),
                        exit = fadeOut(tween(400)) + slideOutVertically { -40 },
                        modifier = Modifier.offset(x = 10.dp, y = (-26).dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreen,
                            shadowElevation = 6.dp,
                            modifier = Modifier.testTag("quiz_score_plus_one")
                        ) {
                            Text(
                                text = "+1",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subheader: Question Counter & Category / Difficulty Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isTamil) "கேள்வி" else "Question"} ${currentIndex + 1} / $totalQuestions",
                    style = MaterialTheme.typography.titleSmall,
                    color = RichGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("quiz_question_counter")
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Difficulty Badge
                    val diffColor = when (baseQuestion.difficulty) {
                        QuizDifficulty.BEGINNER -> SuccessGreen
                        QuizDifficulty.INTERMEDIATE -> RichGold
                        QuizDifficulty.ADVANCED -> Color(0xFFCE93D8)
                    }
                    val diffLabel = when (baseQuestion.difficulty) {
                        QuizDifficulty.BEGINNER -> if (isTamil) "எளிது" else "Easy"
                        QuizDifficulty.INTERMEDIATE -> if (isTamil) "நடுத்தரம்" else "Medium"
                        QuizDifficulty.ADVANCED -> if (isTamil) "கடினம்" else "Hard"
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = diffColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, diffColor.copy(alpha = 0.6f)),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = diffLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = diffColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Category Pill
                    Surface(
                        shape = CircleShape,
                        color = PrimaryRoyalBlue.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, PrimaryRoyalBlue.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = baseQuestion.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = BrightGoldHighlight,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Smooth Animated Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("quiz_progress_indicator"),
                color = RichGold,
                trackColor = SecondaryNavySurface
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Question Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                border = BorderStroke(1.dp, PrimaryRoyalBlue.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isTamil) baseQuestion.questionTa else baseQuestion.questionEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryLightText,
                        lineHeight = 26.sp,
                        modifier = Modifier.testTag("quiz_question_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Four Answer Options
            options.forEachIndexed { index, optionText ->
                QuizOptionItem(
                    index = index,
                    optionText = optionText,
                    isSelected = selectedIndex == index,
                    isSubmitted = isSubmitted,
                    isCorrectOption = index == correctIndex,
                    isTamil = isTamil,
                    onSelect = {
                        if (!isSubmitted) {
                            onSelectAnswer(index)
                        }
                    }
                )
            }

            // Biblical Reference and Explanation Feedback
            if (isSubmitted) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_explanation_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                    border = BorderStroke(1.dp, RichGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = BrightGoldHighlight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "வேத ஆதாரம் & விளக்கம்:" else "Biblical Reference & Explanation:",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrightGoldHighlight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTamil) baseQuestion.explanationTa else baseQuestion.explanationEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryLightText,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DeepNavyBackground,
                            border = BorderStroke(1.dp, RichGold.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "— ${if (isTamil) baseQuestion.scriptureRefTa else baseQuestion.scriptureRefEn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = RichGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // CONTINUE BUTTON (Next Question or View Results)
        if (isSubmitted) {
            Spacer(modifier = Modifier.height(12.dp))
            val isLast = currentIndex == totalQuestions - 1
            Button(
                onClick = onNextQuestion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRoyalBlue,
                    contentColor = BrightGoldHighlight
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 6.dp)
                    .testTag("quiz_next_question_button")
            ) {
                Text(
                    text = if (isLast) {
                        if (isTamil) "முடிவுகளைக் காண்க (View Results) →" else "View Results →"
                    } else {
                        if (isTamil) "அடுத்த கேள்வி (Next Question) →" else "Next Question →"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// =============================================================================
// SUB-VIEW 2: QUIZ START & CONFIGURATION SCREEN
// =============================================================================

@Composable
private fun QuizConfigView(
    viewModel: BibleViewModel,
    isTamil: Boolean,
    savedSession: com.example.data.model.QuizSessionState?,
    uniqueQuestionsCount: Int,
    onStartQuiz: (QuizConfig) -> Unit,
    onResumeQuiz: () -> Unit,
    onDiscardQuiz: () -> Unit,
    onRequestResetHistory: () -> Unit
) {
    val scrollState = rememberScrollState()

    var selectedCount by remember { mutableIntStateOf(10) }
    var selectedDifficulty by remember { mutableStateOf("MIXED") }
    var selectedTestament by remember { mutableStateOf("BOTH") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val eligibleQuestions = remember(selectedDifficulty, selectedTestament, selectedCategory) {
        BibleQuizBank.filterQuestions(
            difficulty = selectedDifficulty,
            testament = selectedTestament,
            category = selectedCategory
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        // Hero Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
            border = BorderStroke(1.dp, RichGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(PrimaryRoyalBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = BrightGoldHighlight,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isTamil) "வேத வினாடி வினா" else "Interactive Bible Quiz",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isTamil)
                        "200-க்கும் மேற்பட்ட சரிபார்க்கப்பட்ட வேதக் கேள்விகள், தலைப்புகள் மற்றும் மறுபடியும் வராத புதிய வினாக்கள்!"
                    else
                        "200+ Verified Bible Questions across Old & New Testament with smart repeat prevention!",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryLightText,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RESUME UNFINISHED QUIZ BANNER (if active session exists)
        if (savedSession != null && !savedSession.isCompleted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resume_quiz_banner"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryRoyalBlue.copy(alpha = 0.35f)),
                border = BorderStroke(1.5.dp, RichGold)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTamil) "முடிக்கப்படாத வினாடி வினா உள்ளது!" else "Unfinished Quiz in Progress",
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DeepNavyBackground
                        ) {
                            Text(
                                text = "${savedSession.currentIndex} / ${savedSession.questions.size} answered",
                                style = MaterialTheme.typography.labelSmall,
                                color = RichGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${if (isTamil) "தற்போதைய மதிப்பெண்:" else "Current Score:"} ${savedSession.score}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryLightText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onResumeQuiz,
                            colors = ButtonDefaults.buttonColors(containerColor = RichGold, contentColor = DeepNavyBackground),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("resume_quiz_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTamil) "தொடர்க" else "Resume Quiz", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDiscardQuiz,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            modifier = Modifier.testTag("discard_quiz_button")
                        ) {
                            Text(if (isTamil) "கைவிடுக" else "Discard")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // CONFIGURATION CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
            border = BorderStroke(1.dp, PrimaryRoyalBlue.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isTamil) "வினாடி வினா அமைப்புகள்" else "Quiz Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Number of Questions: 5, 10, 20, 50
                Text(
                    text = if (isTamil) "கேள்விகளின் எண்ணிக்கை:" else "Number of Questions:",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryLightText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 20, 50).forEach { count ->
                        val isSelected = selectedCount == count
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCount = count },
                            label = { Text("$count", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RichGold,
                                selectedLabelColor = DeepNavyBackground,
                                containerColor = DeepNavyBackground,
                                labelColor = PrimaryLightText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) RichGold else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quiz_count_chip_$count")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Difficulty: Mixed, Easy, Medium, Hard
                Text(
                    text = if (isTamil) "கடினத்தன்மை:" else "Difficulty Level:",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryLightText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val difficulties = listOf(
                        "MIXED" to (if (isTamil) "கலவை" else "Mixed"),
                        "BEGINNER" to (if (isTamil) "எளிது" else "Easy"),
                        "INTERMEDIATE" to (if (isTamil) "நடுத்தரம்" else "Medium"),
                        "ADVANCED" to (if (isTamil) "கடினம்" else "Hard")
                    )
                    difficulties.forEach { (key, label) ->
                        val isSelected = selectedDifficulty == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDifficulty = key },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryRoyalBlue,
                                selectedLabelColor = BrightGoldHighlight,
                                containerColor = DeepNavyBackground,
                                labelColor = PrimaryLightText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) BrightGoldHighlight else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Testament: Both, Old Testament, New Testament
                Text(
                    text = if (isTamil) "வேதாகமப் பகுதி:" else "Testament:",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryLightText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val testaments = listOf(
                        "BOTH" to (if (isTamil) "இரண்டும்" else "Both"),
                        "OT" to (if (isTamil) "பழைய ஏற்பாடு" else "Old Testament"),
                        "NT" to (if (isTamil) "புதிய ஏற்பாடு" else "New Testament")
                    )
                    testaments.forEach { (key, label) ->
                        val isSelected = selectedTestament == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTestament = key },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryRoyalBlue,
                                selectedLabelColor = BrightGoldHighlight,
                                containerColor = DeepNavyBackground,
                                labelColor = PrimaryLightText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) BrightGoldHighlight else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Category Selector (All + 30 categories)
                Text(
                    text = if (isTamil) "தலைப்பு (30 பிரிவுகள்):" else "Category (30 Categories):",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryLightText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "ALL",
                            onClick = { selectedCategory = "ALL" },
                            label = { Text(if (isTamil) "அனைத்து தலைப்புகளும்" else "All Categories", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RichGold,
                                selectedLabelColor = DeepNavyBackground,
                                containerColor = DeepNavyBackground,
                                labelColor = PrimaryLightText
                            )
                        )
                    }
                    items(BibleQuizBank.categories) { categoryName ->
                        val isSelected = selectedCategory == categoryName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = categoryName },
                            label = { Text(categoryName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryRoyalBlue,
                                selectedLabelColor = BrightGoldHighlight,
                                containerColor = DeepNavyBackground,
                                labelColor = PrimaryLightText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Available Question Match Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "தேர்ந்தெடுத்த வினாக்களின் இருப்பு:" else "Eligible Questions in Pool:",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryLightText
                    )
                    Text(
                        text = "${eligibleQuestions.size} / ${BibleQuizBank.allQuestions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // START QUIZ BUTTON
                Button(
                    onClick = {
                        val finalCount = selectedCount.coerceAtMost(eligibleQuestions.size.coerceAtLeast(1))
                        val config = QuizConfig(
                            questionCount = finalCount,
                            difficulty = selectedDifficulty,
                            testament = selectedTestament,
                            category = selectedCategory,
                            language = if (isTamil) "ta" else "en"
                        )
                        onStartQuiz(config)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RichGold,
                        contentColor = DeepNavyBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_quiz_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTamil) "வினாடி வினாவைத் தொடங்கு (Start Quiz)" else "Start Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QUIZ HISTORY & STATS SUMMARY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
            border = BorderStroke(1.dp, PrimaryRoyalBlue.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = RichGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTamil) "கேள்வி வரலாறு & புள்ளிவிவரம்" else "Question Bank Exposure",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryLightText
                        )
                    }

                    TextButton(
                        onClick = onRequestResetHistory,
                        modifier = Modifier.testTag("reset_quiz_history_button")
                    ) {
                        Text(
                            text = if (isTamil) "மீட்டமை" else "Reset History",
                            color = RichGold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${if (isTamil) "பார்த்த கேள்விகள்:" else "Questions seen so far:"} $uniqueQuestionsCount / ${BibleQuizBank.allQuestions.size} (${((uniqueQuestionsCount.toFloat() / BibleQuizBank.allQuestions.size.toFloat()) * 100).roundToInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryLightText
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (uniqueQuestionsCount.toFloat() / BibleQuizBank.allQuestions.size.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BrightGoldHighlight,
                    trackColor = DeepNavyBackground
                )
            }
        }
    }
}

// =============================================================================
// SUB-VIEW 3: RESULTS & ANSWER REVIEW SCREEN
// =============================================================================

@Composable
private fun QuizResultsView(
    viewModel: BibleViewModel,
    isTamil: Boolean,
    isReviewingAnswers: Boolean,
    onToggleReview: () -> Unit,
    onStartNew: () -> Unit,
    onRetry: () -> Unit
) {
    val scrollState = rememberScrollState()
    val score by viewModel.quizScore.collectAsState()
    val session by viewModel.quizSessionState.collectAsState()
    val legacyQuestions by viewModel.quizQuestions.collectAsState()

    val totalCount = session?.questions?.size ?: legacyQuestions.size
    val percentage = if (totalCount > 0) (score.toFloat() / totalCount.toFloat() * 100).roundToInt() else 0
    val incorrectCount = (totalCount - score).coerceAtLeast(0)

    val timeTakenFormatted = remember(session) {
        val s = session
        if (s != null && s.completedTime != null) {
            val seconds = ((s.completedTime!! - s.startTime) / 1000).coerceAtLeast(1)
            val mins = seconds / 60
            val secs = seconds % 60
            if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        } else {
            "1m 20s"
        }
    }

    val reviewItems = remember(session, legacyQuestions) {
        viewModel.getQuizReviewItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Celebratory Trophy Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("quiz_completed_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
            border = BorderStroke(1.5.dp, RichGold)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PrimaryRoyalBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Quiz Complete Trophy",
                        tint = BrightGoldHighlight,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isTamil) "வினாடி வினா நிறைவடைந்தது!" else "Quiz Completed!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${if (isTamil) "உங்கள் மதிப்பெண்:" else "Your Score:"} $score / $totalCount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryLightText,
                    modifier = Modifier.testTag("quiz_final_score_text")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Performance Appraisal Message
                val appraisalText = when {
                    percentage >= 80 -> if (isTamil) "சிறந்த வேத அறிவு! வாழ்த்துகள்!" else "Outstanding! Excellent Biblical Knowledge!"
                    percentage >= 50 -> if (isTamil) "நன்று! மேலும் வேதத்தை வாசியுங்கள்." else "Good Job! Keep exploring God's Word."
                    else -> if (isTamil) "தொடர்ந்து வேதத்தை வாசித்து வளருங்கள்." else "Keep studying and growing in Scripture."
                }

                Text(
                    text = appraisalText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RichGold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // STATS ROW: Correct, Incorrect, Accuracy, Time Taken
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(label = if (isTamil) "சரி" else "Correct", value = "$score", color = SuccessGreen)
                    StatBox(label = if (isTamil) "தவறு" else "Incorrect", value = "$incorrectCount", color = ErrorRed)
                    StatBox(label = if (isTamil) "துல்லியம்" else "Accuracy", value = "$percentage%", color = BrightGoldHighlight)
                    StatBox(label = if (isTamil) "நேரம்" else "Time", value = timeTakenFormatted, color = PrimaryLightText)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons: Start New Quiz, Retry This Quiz
                Button(
                    onClick = onStartNew,
                    colors = ButtonDefaults.buttonColors(containerColor = RichGold, contentColor = DeepNavyBackground),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quiz_try_again_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTamil) "புதிய வினாடி வினா தொடங்க" else "Start New Quiz", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RichGold.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RichGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quiz_retry_same_button")
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTamil) "இக்கேள்விகளை மீண்டும் பயில" else "Retry Same Questions", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Review Answers Toggle Button
                TextButton(
                    onClick = onToggleReview,
                    modifier = Modifier.testTag("quiz_toggle_review_button")
                ) {
                    Icon(
                        imageVector = if (isReviewingAnswers) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = BrightGoldHighlight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isReviewingAnswers) {
                            if (isTamil) "விடைகள் மறை (Hide Review)" else "Hide Answers Review"
                        } else {
                            if (isTamil) "விடைகளைச் சரிபார்க்க (Review Answers)" else "Review Questions & Answers"
                        },
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                }
            }
        }

        // DETAILED REVIEW OF QUESTIONS & ANSWERS
        if (isReviewingAnswers) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isTamil) "கேள்விகள் & சரியான விடைகள்:" else "Questions & Verified Answers:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RichGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            reviewItems.forEach { item ->
                ReviewItemCard(item = item, isTamil = isTamil)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryLightText
        )
    }
}

@Composable
private fun ReviewItemCard(item: QuizReviewItem, isTamil: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
        border = BorderStroke(1.dp, if (item.isCorrect) SuccessGreen.copy(alpha = 0.5f) else ErrorRed.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isTamil) "கேள்வி" else "Question"} ${item.questionNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (item.isCorrect) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (item.isCorrect) (if (isTamil) "சரி" else "Correct") else (if (isTamil) "தவறு" else "Incorrect"),
                        color = if (item.isCorrect) SuccessGreen else ErrorRed,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryLightText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Options with highlighting
            item.options.forEachIndexed { idx, optText ->
                val isUserSelection = item.selectedIndex == idx
                val isCorrect = idx == item.correctIndex
                val optBg = when {
                    isCorrect -> SuccessGreen.copy(alpha = 0.2f)
                    isUserSelection && !isCorrect -> ErrorRed.copy(alpha = 0.2f)
                    else -> DeepNavyBackground
                }
                val optBorder = when {
                    isCorrect -> SuccessGreen
                    isUserSelection && !isCorrect -> ErrorRed
                    else -> Color.Transparent
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(optBg, RoundedCornerShape(8.dp))
                        .border(1.dp, optBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${('A' + idx)}. $optText",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryLightText,
                        modifier = Modifier.weight(1f)
                    )
                    if (isCorrect) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Correct Answer",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (isUserSelection) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Wrong Answer",
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${item.scriptureReference} — ${item.explanation}",
                style = MaterialTheme.typography.labelSmall,
                color = RichGold
            )
        }
    }
}

// =============================================================================
// SUB-VIEW 4: OPTION ITEM WITH POP & SHAKE ANIMATION
// =============================================================================

@Composable
private fun QuizOptionItem(
    index: Int,
    optionText: String,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isCorrectOption: Boolean,
    isTamil: Boolean,
    onSelect: () -> Unit
) {
    // Smooth pop scale animation on correct answer
    val popScale by animateFloatAsState(
        targetValue = if (isSubmitted && isSelected && isCorrectOption) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "quiz_pop_scale"
    )

    // Gentle horizontal shake animation on wrong answer
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isSubmitted, isSelected) {
        if (isSubmitted && isSelected && !isCorrectOption) {
            shakeOffset.animateTo(8f, animationSpec = tween(40))
            shakeOffset.animateTo(-8f, animationSpec = tween(40))
            shakeOffset.animateTo(5f, animationSpec = tween(40))
            shakeOffset.animateTo(-5f, animationSpec = tween(40))
            shakeOffset.animateTo(0f, animationSpec = tween(40))
        }
    }

    // Determine colors based on immediate validation state
    val containerColor = when {
        isSubmitted && isSelected && isCorrectOption -> SuccessGreen.copy(alpha = 0.25f)
        isSubmitted && isSelected && !isCorrectOption -> ErrorRed.copy(alpha = 0.25f)
        isSubmitted && isCorrectOption -> SuccessGreen.copy(alpha = 0.15f)
        isSelected -> PrimaryRoyalBlue.copy(alpha = 0.35f)
        else -> SecondaryNavySurface
    }

    val borderColor = when {
        isSubmitted && isSelected && isCorrectOption -> SuccessGreen
        isSubmitted && isSelected && !isCorrectOption -> ErrorRed
        isSubmitted && isCorrectOption -> SuccessGreen
        isSelected -> RichGold
        else -> PrimaryRoyalBlue.copy(alpha = 0.35f)
    }

    val borderWidth = if (isSubmitted && (isSelected || isCorrectOption)) 2.dp else 1.dp

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .scale(popScale)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isSubmitted) { onSelect() }
            .testTag("quiz_option_$index")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 14.dp)
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option Letter badge A, B, C, D
            val badgeBg = when {
                isSubmitted && isSelected && isCorrectOption -> SuccessGreen
                isSubmitted && isSelected && !isCorrectOption -> ErrorRed
                isSubmitted && isCorrectOption -> SuccessGreen
                isSelected -> RichGold
                else -> DeepNavyBackground
            }

            val badgeTextColor = when {
                isSubmitted && (isSelected || isCorrectOption) -> Color.White
                isSelected -> DeepNavyBackground
                else -> BrightGoldHighlight
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ('A' + index).toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = badgeTextColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Option Text
            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || (isSubmitted && isCorrectOption)) FontWeight.Bold else FontWeight.Normal,
                color = PrimaryLightText,
                modifier = Modifier.weight(1f)
            )

            // Result Feedback Badges
            if (isSubmitted) {
                Spacer(modifier = Modifier.width(8.dp))
                if (isSelected && isCorrectOption) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("quiz_badge_correct")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Correct",
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTamil) "சரி!" else "Correct!",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else if (isSelected && !isCorrectOption) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("quiz_badge_wrong")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Wrong",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTamil) "தவறு!" else "Wrong!",
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else if (isCorrectOption) {
                    // Clearly reveal the correct answer if user chose wrong option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("quiz_badge_revealed_correct")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Correct Answer",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTamil) "சரியான பதில்" else "Correct Answer",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
