package com.example.ui.screens.reader

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.BibleBook
import com.example.data.model.BibleVerse
import com.example.data.model.Testament
import com.example.ui.components.BibleLanguageSwitcher
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val selectedBook by viewModel.selectedBook.collectAsState()
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val currentVerses by viewModel.currentVerses.collectAsState()
    val distinctVerses = remember(currentVerses) { currentVerses.distinctBy { it.verseNumber }.sortedBy { it.verseNumber } }
    val highlights by viewModel.currentHighlights.collectAsState()
    val selectedVerseForAction by viewModel.selectedVerseForAction.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.bibleLanguage == "ta"

    var showBookPicker by remember { mutableStateOf(false) }
    var showChapterPicker by remember { mutableStateOf(false) }
    var showAccessibilitySheet by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteTitleInput by remember { mutableStateOf("") }
    var noteContentInput by remember { mutableStateOf("") }

    val targetScrollVerse by viewModel.targetScrollVerse.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(targetScrollVerse, distinctVerses) {
        targetScrollVerse?.let { target ->
            val idx = distinctVerses.indexOfFirst { it.verseNumber == target }
            if (idx >= 0) {
                listState.animateScrollToItem(idx + 1) // +1 for Chapter Heading
            }
        }
    }

    val audioState by viewModel.audioEngine.playbackState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bible_reader_screen")
    ) {
        // Top Reader Control Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book & Chapter Selector Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showBookPicker = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("book_selector_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) selectedBook.tamilName else selectedBook.englishName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryRoyalBlue.copy(alpha = 0.2f),
                        modifier = Modifier
                            .clickable { showChapterPicker = true }
                            .padding(2.dp)
                    ) {
                        Text(
                            text = "${if (isTamil) "அதி" else "Ch"} $selectedChapter",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select",
                        tint = RichGold
                    )
                }

                // Audio, Accessibility & Language Switch Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Audio Playback Button
                    IconButton(
                        onClick = {
                            viewModel.togglePlayPauseAudio()
                        },
                        modifier = Modifier.testTag("reader_audio_button")
                    ) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Audio Play",
                            tint = if (audioState.isPlaying) BrightGoldHighlight else RichGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Reading Accessibility & Font Size Setting
                    IconButton(
                        onClick = { showAccessibilitySheet = true },
                        modifier = Modifier.testTag("reader_font_size_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Adjust Font Size & Accessibility",
                            tint = if (showAccessibilitySheet) BrightGoldHighlight else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Side-by-Side Language Switcher: EN | தமிழ்
                    BibleLanguageSwitcher(
                        selectedLanguage = if (isTamil) "ta" else "en",
                        onLanguageSelected = { lang -> viewModel.setBibleLanguage(lang) },
                        containerTestTag = "reader_language_switcher"
                    )
                }
            }
        }

        // Active Audio Banner (if playing or paused)
        if (audioState.isPlaying || audioState.isPaused) {
            Surface(
                color = ElevatedBlueSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reader_audio_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = BrightGoldHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = audioState.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryLightText,
                            maxLines = 1
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.audioEngine.skipPrevious() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.SkipPrevious, "Prev", tint = PrimaryLightText, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                viewModel.togglePlayPauseAudio()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", tint = BrightGoldHighlight, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.audioEngine.skipNext() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, "Next", tint = PrimaryLightText, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { viewModel.stopAudio() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = SecondaryLightText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Verses List with Navigation
        Box(modifier = Modifier.weight(1f)) {
            if (currentVerses.isEmpty()) {
                // Pre-indexed verified scripture notice / offline notice
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = RichGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTamil) "${selectedBook.tamilName} $selectedChapter" else "${selectedBook.englishName} Chapter $selectedChapter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTamil)
                                "இந்த அதிகாரத்தின் முழுமையான அதிகாரம் முன்மாதிரியாக ஏற்றப்பட்டுள்ளது. தொடக்க நூல்கள், யோவான், ரோமர், சங்கீதம், நீதிமொழிகள் அதிகாரங்களை வாசிக்கத் தேர்வு செய்யலாம்."
                            else
                                "Selected canonical chapter reference loaded. Central books like John, Genesis, Psalms, Proverbs, Matthew, Romans, 1 Corinthians are pre-indexed offline.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val johnBook = CanonicalBibleData.findBookById("JHN") ?: CanonicalBibleData.allBooks[0]
                                viewModel.openReader(johnBook, 3)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue,
                                contentColor = BrightGoldHighlight
                            )
                        ) {
                            Text(if (isTamil) "யோவான் 3 வாசிக்க" else "Read John 3")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp)
                ) {
                    // Chapter Heading
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isTamil) selectedBook.tamilName else selectedBook.englishName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Text(
                                text = "${if (isTamil) "அதிகாரம்" else "Chapter"} $selectedChapter",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp,
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .width(80.dp)
                            )
                        }
                    }

                    // Verses
                    items(items = distinctVerses, key = { "${it.bookId}_${it.chapterNumber}_${it.verseNumber}" }) { verse ->
                        val isHighlighted = highlights.firstOrNull { it.verseNumber == verse.verseNumber }
                        val isCurrentlySpeaking = audioState.isPlaying && audioState.currentVerseNumber == verse.verseNumber
                        val isTargetNavigated = targetScrollVerse == verse.verseNumber

                        val highlightColor = when {
                            isCurrentlySpeaking -> BrightGoldHighlight.copy(alpha = 0.25f)
                            isTargetNavigated -> RichGold.copy(alpha = 0.18f)
                            isHighlighted != null -> {
                                try {
                                    Color(android.graphics.Color.parseColor(isHighlighted.colorHex))
                                } catch (_: Exception) {
                                    HighlightGold
                                }
                            }
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(highlightColor)
                                .then(
                                    if (isTargetNavigated) Modifier.border(1.5.dp, RichGold.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable {
                                    viewModel.selectVerseForAction(verse)
                                }
                                .padding(vertical = 6.dp, horizontal = 8.dp)
                                .testTag("verse_item_${verse.verseNumber}")
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                if (prefs.showVerseNumbers) {
                                    Text(
                                        text = "${verse.verseNumber}",
                                        fontSize = (prefs.fontSizeSp - 4).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RichGold,
                                        modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                                    )
                                }
                                Text(
                                    text = verse.verseText,
                                    fontSize = prefs.fontSizeSp.sp,
                                    lineHeight = (prefs.fontSizeSp * prefs.lineSpacingMultiplier).sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Chapter Navigation Bottom Buttons
                    item {
                        OutlinedButton(
                            onClick = { viewModel.markChapterCompleted(selectedBook, selectedChapter) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RichGold)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RichGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "வாசித்து முடித்தேன் (நாட்காட்டியில் பதிவு செய்)" else "Mark Chapter Read (Log to Calendar)",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.previousChapter() },
                                enabled = selectedChapter > 1,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTamil) "முந்தைய அதிகாரம்" else "Previous Chapter")
                            }

                            Button(
                                onClick = { viewModel.nextChapter() },
                                enabled = selectedChapter < selectedBook.chapterCount,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryRoyalBlue,
                                    contentColor = BrightGoldHighlight
                                )
                            ) {
                                Text(if (isTamil) "அடுத்த அதிகாரம்" else "Next Chapter")
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for Verse Actions
    if (selectedVerseForAction != null) {
        val verse = selectedVerseForAction!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectVerseForAction(null) },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    IconButton(onClick = { viewModel.selectVerseForAction(null) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "“${verse.verseText}”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Highlight Color Palette
                Text(
                    text = if (isTamil) "வர்ணம் தீட்டுக (Highlight)" else "Highlight Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf(
                        "#59D4AF37" to "Gold",
                        "#5900BCD4" to "Cyan",
                        "#594CAF50" to "Green",
                        "#59E91E63" to "Pink"
                    ).forEach { (colorHex, name) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorHex)))
                                .border(1.dp, RichGold, CircleShape)
                                .clickable { viewModel.addHighlight(verse, colorHex) }
                        )
                    }

                    // Remove highlight icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.removeHighlight(verse) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatColorReset,
                            contentDescription = "Clear Highlight",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Action Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Bookmark
                    IconButton(onClick = {
                        viewModel.toggleBookmark(verse)
                        viewModel.selectVerseForAction(null)
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = RichGold)
                            Text(if (isTamil) "புக்மார்க்" else "Bookmark", fontSize = 10.sp)
                        }
                    }

                    // Add Note
                    IconButton(onClick = {
                        noteTitleInput = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}"
                        noteContentInput = ""
                        showNoteDialog = true
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EditNote, contentDescription = "Add Note", tint = RichGold)
                            Text(if (isTamil) "குறிப்பு" else "Note", fontSize = 10.sp)
                        }
                    }

                    // Copy
                    IconButton(onClick = {
                        val text = "${verse.verseText}\n— ${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}"
                        clipboardManager.setText(AnnotatedString(text))
                        viewModel.selectVerseForAction(null)
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = RichGold)
                            Text(if (isTamil) "நகலெடு" else "Copy", fontSize = 10.sp)
                        }
                    }

                    // Share
                    IconButton(onClick = {
                        val shareText = "${verse.verseText}\n— ${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}\n\nShared via SVS Holy Bible"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Verse"))
                        viewModel.selectVerseForAction(null)
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = RichGold)
                            Text(if (isTamil) "பகிர்" else "Share", fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add Note Dialog
    if (showNoteDialog && selectedVerseForAction != null) {
        val verse = selectedVerseForAction!!
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    text = if (isTamil) "வேத குறிப்பு எழுதுக" else "Add Bible Note",
                    color = RichGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { noteTitleInput = it },
                        label = { Text(if (isTamil) "தலைப்பு" else "Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteContentInput,
                        onValueChange = { noteContentInput = it },
                        label = { Text(if (isTamil) "தியான குறிப்பு" else "Your Reflection / Note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveNote(verse, noteTitleInput, noteContentInput)
                        showNoteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                ) {
                    Text(if (isTamil) "சேமி" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }

    // Book Picker Modal
    if (showBookPicker) {
        BookPickerSheet(
            currentBook = selectedBook,
            isTamil = isTamil,
            onBookSelected = { book ->
                showBookPicker = false
                viewModel.openReader(book, 1)
            },
            onDismiss = { showBookPicker = false }
        )
    }

    // Chapter Picker Modal
    if (showChapterPicker) {
        ChapterPickerSheet(
            book = selectedBook,
            currentChapter = selectedChapter,
            isTamil = isTamil,
            onChapterSelected = { ch ->
                showChapterPicker = false
                viewModel.openReader(selectedBook, ch)
            },
            onDismiss = { showChapterPicker = false }
        )
    }

    // Reading Accessibility & Font Size Modal
    if (showAccessibilitySheet) {
        FontSizeAdjustmentSheet(
            fontSizeSp = prefs.fontSizeSp,
            lineSpacingMultiplier = prefs.lineSpacingMultiplier,
            showVerseNumbers = prefs.showVerseNumbers,
            isTamil = isTamil,
            onFontSizeChange = { viewModel.setFontSize(it) },
            onLineSpacingChange = { viewModel.setLineSpacing(it) },
            onShowVerseNumbersChange = { viewModel.setShowVerseNumbers(it) },
            onDismiss = { showAccessibilitySheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPickerSheet(
    currentBook: BibleBook,
    isTamil: Boolean,
    onBookSelected: (BibleBook) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTestament by remember { mutableStateOf(currentBook.testament) }
    var bookSearchQuery by remember { mutableStateOf("") }

    val books = (if (selectedTestament == Testament.OLD_TESTAMENT) CanonicalBibleData.oldTestamentBooks else CanonicalBibleData.newTestamentBooks)
        .filter {
            bookSearchQuery.isEmpty() ||
            it.englishName.contains(bookSearchQuery, ignoreCase = true) ||
            it.tamilName.contains(bookSearchQuery, ignoreCase = true)
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Testament Tabs
            TabRow(
                selectedTabIndex = if (selectedTestament == Testament.OLD_TESTAMENT) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = RichGold
            ) {
                Tab(
                    selected = selectedTestament == Testament.OLD_TESTAMENT,
                    onClick = { selectedTestament = Testament.OLD_TESTAMENT },
                    text = { Text(if (isTamil) "பழைய ஏற்பாடு (39)" else "Old Testament (39)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTestament == Testament.NEW_TESTAMENT,
                    onClick = { selectedTestament = Testament.NEW_TESTAMENT },
                    text = { Text(if (isTamil) "புதிய ஏற்பாடு (27)" else "New Testament (27)", fontWeight = FontWeight.Bold) }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = bookSearchQuery,
                onValueChange = { bookSearchQuery = it },
                placeholder = { Text(if (isTamil) "புத்தகத்தைத் தேடுங்கள்..." else "Filter books...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Books Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(books) { book ->
                    val isSelected = book.id == currentBook.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PrimaryRoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, RichGold) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBookSelected(book) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isTamil) book.tamilName else book.englishName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BrightGoldHighlight else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "${book.chapterCount} ${if (isTamil) "அதி" else "ch"}",
                                fontSize = 10.sp,
                                color = if (isSelected) PrimaryLightText else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterPickerSheet(
    book: BibleBook,
    currentChapter: Int,
    isTamil: Boolean,
    onChapterSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${if (isTamil) book.tamilName else book.englishName} — ${if (isTamil) "அதிகாரத்தைத் தேர்வுசெய்க" else "Select Chapter"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RichGold,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items((1..book.chapterCount).toList()) { ch ->
                    val isSelected = ch == currentChapter
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PrimaryRoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, RichGold) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onChapterSelected(ch) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$ch",
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BrightGoldHighlight else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSizeAdjustmentSheet(
    fontSizeSp: Int,
    lineSpacingMultiplier: Float,
    showVerseNumbers: Boolean,
    isTamil: Boolean,
    onFontSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onShowVerseNumbersChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("font_size_adjustment_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryRoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = BrightGoldHighlight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isTamil) "வாசிப்பு & எழுத்து அமைப்புகள்" else "Reading & Font Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichGold
                        )
                        Text(
                            text = if (isTamil) "பார்வைக்கு ஏற்ப அளவை மாற்றவும்" else "Adjust size & spacing for accessibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(
                    onClick = {
                        onFontSizeChange(18)
                        onLineSpacingChange(1.4f)
                        onShowVerseNumbersChange(true)
                    }
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = RichGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isTamil) "மீட்டமை" else "Reset", fontSize = 12.sp, color = RichGold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Scripture Preview Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SecondaryNavySurface,
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RichGold.copy(alpha = 0.35f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTamil) "மாதிரி வாசிப்பு தோற்றம்" else "Live Reading Preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrightGoldHighlight,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryRoyalBlue.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = "$fontSizeSp sp • ${String.format(java.util.Locale.US, "%.1f", lineSpacingMultiplier)}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RichGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (showVerseNumbers) {
                            Text(
                                text = "1",
                                fontSize = (fontSizeSp - 4).coerceAtLeast(10).sp,
                                fontWeight = FontWeight.Bold,
                                color = RichGold,
                                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                            )
                        }
                        Text(
                            text = if (isTamil)
                                "ஆதியிலே வார்த்தை இருந்தது, அந்த வார்த்தை தேவனிடத்திலிருந்தது, அந்த வார்த்தை தேவனாயிருந்தது."
                            else
                                "In the beginning was the Word, and the Word was with God, and the Word was God.",
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * lineSpacingMultiplier).sp,
                            color = PrimaryLightText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font Size Slider with Steppers
            Text(
                text = if (isTamil) "எழுத்து அளவு: $fontSizeSp sp" else "Font Size: $fontSizeSp sp",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onFontSizeChange((fontSizeSp - 2).coerceAtLeast(12)) },
                    enabled = fontSizeSp > 12,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease Font Size", tint = MaterialTheme.colorScheme.onSurface)
                }

                Slider(
                    value = fontSizeSp.toFloat(),
                    onValueChange = { onFontSizeChange(it.toInt()) },
                    valueRange = 12f..32f,
                    steps = 9,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("font_size_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = RichGold,
                        activeTrackColor = PrimaryRoyalBlue,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                IconButton(
                    onClick = { onFontSizeChange((fontSizeSp + 2).coerceAtMost(32)) },
                    enabled = fontSizeSp < 32,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase Font Size", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Quick Preset Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    14 to if (isTamil) "சிறிய (14)" else "Small (14)",
                    18 to if (isTamil) "சாதாரண (18)" else "Default (18)",
                    22 to if (isTamil) "பெரிய (22)" else "Large (22)",
                    26 to if (isTamil) "மிகப்பெரிய (26)" else "X-Large (26)",
                    30 to if (isTamil) "பிரம்மாண்ட (30)" else "Huge (30)"
                ).forEach { (size, label) ->
                    val isSelected = fontSizeSp == size
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFontSizeChange(size) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryRoyalBlue,
                            selectedLabelColor = BrightGoldHighlight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Line Spacing Multiplier Selector
            Text(
                text = if (isTamil) "வரி இடைவெளி (Line Spacing)" else "Line Spacing",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    1.2f to if (isTamil) "சுருக்கம் (1.2x)" else "Compact (1.2x)",
                    1.4f to if (isTamil) "இயல்பு (1.4x)" else "Normal (1.4x)",
                    1.6f to if (isTamil) "விரிவு (1.6x)" else "Relaxed (1.6x)",
                    1.8f to if (isTamil) "விசாலமான (1.8x)" else "Spacious (1.8x)"
                ).forEach { (spacing, label) ->
                    val isSelected = kotlin.math.abs(lineSpacingMultiplier - spacing) < 0.05f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onLineSpacingChange(spacing) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryRoyalBlue,
                            selectedLabelColor = BrightGoldHighlight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Show Verse Numbers Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onShowVerseNumbersChange(!showVerseNumbers) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isTamil) "வசன எண்களைக் காட்டு" else "Show Verse Numbers",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isTamil) "வசனத்தின் தொடக்கத்தில் 1, 2, 3 எண்களைக் காண்பிக்கும்" else "Displays 1, 2, 3 numbering before each verse",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = showVerseNumbers,
                    onCheckedChange = onShowVerseNumbersChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BrightGoldHighlight,
                        checkedTrackColor = PrimaryRoyalBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Done Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("font_sheet_done_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRoyalBlue,
                    contentColor = BrightGoldHighlight
                )
            ) {
                Text(
                    text = if (isTamil) "முடிந்தது (சரி)" else "Done",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
