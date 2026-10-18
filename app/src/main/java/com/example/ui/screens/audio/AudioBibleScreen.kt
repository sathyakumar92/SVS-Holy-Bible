package com.example.ui.screens.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.ui.components.BibleLanguageSwitcher
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioBibleScreen(
    viewModel: BibleViewModel
) {
    val audioState by viewModel.audioEngine.playbackState.collectAsState()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val currentVerses by viewModel.currentVerses.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.bibleLanguage == "ta"

    var showBookDialog by remember { mutableStateOf(false) }
    var showChapterDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll to currently playing verse
    LaunchedEffect(audioState.currentVerseIndex) {
        if (audioState.isPlaying && audioState.currentVerseIndex in currentVerses.indices) {
            listState.animateScrollToItem(audioState.currentVerseIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("audio_bible_screen")
    ) {
        // Top Player Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedBlueSurface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Book & Chapter Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryRoyalBlue.copy(alpha = 0.4f),
                        modifier = Modifier
                            .clickable { showBookDialog = true }
                            .testTag("audio_book_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTamil) selectedBook.tamilName else selectedBook.englishName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = RichGold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryRoyalBlue.copy(alpha = 0.4f),
                        modifier = Modifier
                            .clickable { showChapterDialog = true }
                            .testTag("audio_chapter_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (isTamil) "அதி" else "Ch"} $selectedChapter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = RichGold)
                        }
                    }

                    // Side-by-Side Language Switcher: EN | தமிழ்
                    BibleLanguageSwitcher(
                        selectedLanguage = if (isTamil) "ta" else "en",
                        onLanguageSelected = { lang -> viewModel.setBibleLanguage(lang) },
                        containerTestTag = "audio_language_toggle"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Now Playing Status Text
                Text(
                    text = audioState.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (audioState.isPlaying) BrightGoldHighlight else PrimaryLightText,
                    maxLines = 1,
                    modifier = Modifier.testTag("audio_status_message")
                )

                if (!audioState.isVoiceSupported) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isTamil) "கவனிக்க: சாதனத்தில் தமிழ் குரல் தொகுப்பு நிறுவப்படவில்லை" else "Note: Tamil voice pack missing on device",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Playback Controls Row (Skip Prev, Play/Pause, Skip Next, Stop)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.audioEngine.skipPrevious() },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("audio_skip_previous_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Verse",
                            tint = PrimaryLightText,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Instant Play / Pause Button
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(RichGold)
                            .clickable {
                                viewModel.togglePlayPauseAudio()
                            }
                            .testTag("audio_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (audioState.isPlaying) "Pause" else "Play",
                            tint = DeepNavyBackground,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = { viewModel.audioEngine.skipNext() },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("audio_skip_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Verse",
                            tint = PrimaryLightText,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = { viewModel.stopAudio() },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("audio_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Playback",
                            tint = SecondaryLightText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Gender & Speed Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Gender Toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isTamil) "குரல்:" else "Voice:",
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryLightText,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        FilterChip(
                            selected = prefs.audioVoiceGender == "male",
                            onClick = { viewModel.setAudioVoiceGender("male") },
                            label = { Text(if (isTamil) "ஆண்" else "Male", fontSize = 11.sp) },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("audio_voice_male_chip")
                        )
                        FilterChip(
                            selected = prefs.audioVoiceGender == "female",
                            onClick = { viewModel.setAudioVoiceGender("female") },
                            label = { Text(if (isTamil) "பெண்" else "Female", fontSize = 11.sp) },
                            modifier = Modifier.testTag("audio_voice_female_chip")
                        )
                    }

                    // Playback Speed Selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        listOf(0.75f, 1.0f, 1.25f).forEach { s ->
                            FilterChip(
                                selected = prefs.audioSpeed == s,
                                onClick = { viewModel.setAudioSpeed(s) },
                                label = { Text("${s}x", fontSize = 10.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Verses
        Text(
            text = if (isTamil) "வசனங்கள் (வாசிக்க & கேட்க தொடவும்)" else "Verses (Tap any verse to listen)",
            style = MaterialTheme.typography.labelMedium,
            color = RichGold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Verses List with Active Spoken Verse Highlighting
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("audio_verses_list"),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            itemsIndexed(
                items = currentVerses,
                key = { _, verse -> "audio_${verse.bookId}_${verse.chapterNumber}_${verse.verseNumber}" }
            ) { index, verse ->
                val isCurrentlyPlaying = audioState.isPlaying && audioState.currentVerseNumber == verse.verseNumber

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCurrentlyPlaying) PrimaryRoyalBlue.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                    border = if (isCurrentlyPlaying) androidx.compose.foundation.BorderStroke(1.5.dp, RichGold) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.playAudioFromVerse(index) }
                        .testTag("audio_verse_item_${verse.verseNumber}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${verse.verseNumber}",
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentlyPlaying) BrightGoldHighlight else RichGold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                        )
                        Text(
                            text = verse.verseText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrentlyPlaying) BrightGoldHighlight else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isCurrentlyPlaying) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isCurrentlyPlaying) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speaking",
                                tint = BrightGoldHighlight,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Book Picker Dialog
    if (showBookDialog) {
        AlertDialog(
            onDismissRequest = { showBookDialog = false },
            title = { Text(if (isTamil) "வேதப் புத்தகத்தைத் தேர்ந்தெடுக்கவும்" else "Select Bible Book") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(CanonicalBibleData.allBooks.size) { i ->
                        val b = CanonicalBibleData.allBooks[i]
                        Text(
                            text = if (isTamil) b.tamilName else b.englishName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openReader(b, 1)
                                    showBookDialog = false
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookDialog = false }) { Text("Close") }
            }
        )
    }

    // Chapter Picker Dialog
    if (showChapterDialog) {
        AlertDialog(
            onDismissRequest = { showChapterDialog = false },
            title = { Text(if (isTamil) "அதிகாரத்தைத் தேர்ந்தெடுக்கவும்" else "Select Chapter") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(selectedBook.chapterCount) { ch ->
                        Text(
                            text = "${if (isTamil) "அதிகாரம்" else "Chapter"} ${ch + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openReader(selectedBook, ch + 1)
                                    showChapterDialog = false
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChapterDialog = false }) { Text("Close") }
            }
        )
    }
}
