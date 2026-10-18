package com.example.ui.screens.memorization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemorizedVerse
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MemorizationSessionScreen(
    viewModel: BibleViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.bibleLanguage == "ta"

    val allVerses by viewModel.advancedRepo.allMemoryVerses.collectAsState(initial = emptyList())
    val selectedVerse by viewModel.selectedMemoryVerse.collectAsState()

    val currentVerse = selectedVerse ?: allVerses.firstOrNull()

    var activeMode by remember { mutableIntStateOf(0) } // 0: Flashcard Recall, 1: Missing Words, 2: Word Order, 3: Type the Verse

    // Flashcard State
    var isAnswerRevealed by remember { mutableStateOf(false) }

    // Missing Words State
    var showMissingWordsSolution by remember { mutableStateOf(false) }

    // Word Order State
    val fullVerseText = if (isTamil) currentVerse?.verseTextTa ?: "" else currentVerse?.verseTextEn ?: ""
    val originalWords = remember(fullVerseText) {
        fullVerseText.split(" ").filter { it.isNotBlank() }
    }
    var shuffledWords by remember(fullVerseText) {
        mutableStateOf(originalWords.shuffled())
    }
    var assembledWords by remember(fullVerseText) {
        mutableStateOf(listOf<String>())
    }

    // Type the Verse State
    var typedInput by remember { mutableStateOf("") }
    var hasCheckedTypedAnswer by remember { mutableStateOf(false) }

    if (currentVerse == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No verse available for practice")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("memorization_session_screen")
    ) {
        // App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (isTamil) currentVerse.referenceDisplayTa else currentVerse.referenceDisplayEn,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ElevatedBlueSurface)
        )

        // Mode Selector Tab Row
        ScrollableTabRow(
            selectedTabIndex = activeMode,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = RichGold,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = activeMode == 0,
                onClick = { activeMode = 0; isAnswerRevealed = false },
                text = { Text(if (isTamil) "1. ஃபிளாஷ்கார்டு" else "Flashcard", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeMode == 1,
                onClick = { activeMode = 1; showMissingWordsSolution = false },
                text = { Text(if (isTamil) "2. விடுபட்ட வார்த்தை" else "Missing Words", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeMode == 2,
                onClick = { activeMode = 2; assembledWords = emptyList(); shuffledWords = originalWords.shuffled() },
                text = { Text(if (isTamil) "3. சொல் வரிசை" else "Word Order", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeMode == 3,
                onClick = { activeMode = 3; hasCheckedTypedAnswer = false; typedInput = "" },
                text = { Text(if (isTamil) "4. டைப் செய்க" else "Type Verse", fontWeight = FontWeight.Bold) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // MODE A: FLASHCARD RECALL (with SM-2 SRS)
            // ==========================================
            if (activeMode == 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp)
                            .clickable { isAnswerRevealed = !isAnswerRevealed },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isTamil) currentVerse.referenceDisplayTa else currentVerse.referenceDisplayEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )

                            if (!isAnswerRevealed) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = RichGold,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (isTamil) "வசனத்தை மனதில் நினைத்துவிட்டு, தொட்டு விடையைப் பார்க்கவும்." else "Recall the verse in your mind, then tap card to reveal answer.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SecondaryLightText,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Text(
                                    text = "“$fullVerseText”",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryLightText,
                                    lineHeight = 26.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Text(
                                text = if (!isAnswerRevealed) (if (isTamil) "தொடுக (Tap to Reveal)" else "Tap to Reveal") else (if (isTamil) "விடை சரிபார்க்கப்பட்டது" else "Answer Revealed"),
                                style = MaterialTheme.typography.labelSmall,
                                color = RichGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isAnswerRevealed) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isTamil) "உங்கள் நினைவாற்றலை மதிப்பீடு செய்யுங்கள் (Rate Difficulty):" else "Rate Recall Difficulty (SM-2 Spaced Repetition):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // 4 Difficulty Rating Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.advancedRepo.recordSrsReview(currentVerse.id, 1)
                                            viewModel.navigateBack()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isTamil) "மறந்தது" else "Again", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.advancedRepo.recordSrsReview(currentVerse.id, 2)
                                            viewModel.navigateBack()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarningGold),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isTamil) "கடினம்" else "Hard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.advancedRepo.recordSrsReview(currentVerse.id, 3)
                                            viewModel.navigateBack()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isTamil) "நல்லது" else "Good", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.advancedRepo.recordSrsReview(currentVerse.id, 4)
                                            viewModel.navigateBack()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isTamil) "எளிது" else "Easy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // MODE B: MISSING WORDS CHALLENGE
            // ==========================================
            else if (activeMode == 1) {
                item {
                    val wordsWithBlanks = remember(fullVerseText) {
                        originalWords.mapIndexed { index, word ->
                            if (index % 3 == 1 && word.length > 2) "_____" else word
                        }.joinToString(" ")
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isTamil) "விடுபட்ட வார்த்தைகளை நிரப்புக" else "Fill in the Blanks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (!showMissingWordsSolution) wordsWithBlanks else fullVerseText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryLightText,
                                lineHeight = 28.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showMissingWordsSolution = !showMissingWordsSolution },
                                colors = ButtonDefaults.buttonColors(containerColor = RichGold, contentColor = DeepNavyBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (!showMissingWordsSolution) (if (isTamil) "விடையைக் காட்டு" else "Reveal Missing Words") else (if (isTamil) "மறைக்க" else "Hide Answer"),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // MODE C: WORD ORDER CHALLENGE
            // ==========================================
            else if (activeMode == 2) {
                item {
                    Text(
                        text = if (isTamil) "வார்த்தைகளை சரியான வரிசையில் அடுக்கவும்:" else "Tap words in the correct scriptural sequence:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Assembled Box
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (assembledWords.isEmpty()) (if (isTamil) "கீழேயுள்ள வார்த்தைகளை வரிசையாகத் தொடுங்கள்..." else "Tap words below to arrange...")
                                       else assembledWords.joinToString(" "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (assembledWords.isEmpty()) SecondaryLightText else PrimaryLightText,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                // Shuffled Word Chips
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        shuffledWords.forEachIndexed { idx, word ->
                            SuggestionChip(
                                onClick = {
                                    assembledWords = assembledWords + word
                                    shuffledWords = shuffledWords.toMutableList().also { it.removeAt(idx) }
                                },
                                label = { Text(word, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                item {
                    val isCompleteMatch = assembledWords == originalWords
                    if (isCompleteMatch) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreen.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTamil) "அற்புதம்! வசனம் சரியாக வரிசைப்படுத்தப்பட்டது!" else "Perfect! Word sequence matches Scripture exactly!",
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    } else if (shuffledWords.isEmpty()) {
                        Button(
                            onClick = {
                                assembledWords = emptyList()
                                shuffledWords = originalWords.shuffled()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                        ) {
                            Text(if (isTamil) "மீண்டும் முயற்சிக்க" else "Try Again")
                        }
                    }
                }
            }

            // ==========================================
            // MODE D: TYPE THE VERSE
            // ==========================================
            else if (activeMode == 3) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isTamil) "நினைவிலிருந்து வசனத்தை தட்டச்சு செய்க:" else "Type the recalled verse from memory:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = typedInput,
                                onValueChange = { typedInput = it },
                                placeholder = { Text(if (isTamil) "இங்கு தட்டச்சு செய்யவும்..." else "Type here...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 140.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { hasCheckedTypedAnswer = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isTamil) "சரிபார்க்க" else "Check Accuracy", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (hasCheckedTypedAnswer) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isTamil) "உண்மையான வேத வசனம்:" else "Actual Scripture Text:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGoldHighlight
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = fullVerseText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PrimaryLightText,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
