package com.example.ui.screens.memorization

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.MemorizedVerse
import com.example.data.model.SrsStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@Composable
fun MemorizationScreen(
    viewModel: BibleViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.bibleLanguage == "ta"

    val allVerses by viewModel.advancedRepo.allMemoryVerses.collectAsState(initial = emptyList())
    val dueVerses by viewModel.advancedRepo.getDueVerses().collectAsState(initial = emptyList())
    val memorizedVerses by viewModel.advancedRepo.memorizedVerses.collectAsState(initial = emptyList())

    var showAddVerseDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddVerseDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (isTamil) "வசனம் சேர்க்க" else "Add Memory Verse", fontWeight = FontWeight.Bold) },
                containerColor = RichGold,
                contentColor = DeepNavyBackground,
                modifier = Modifier.testTag("add_memory_verse_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("memorization_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = if (isTamil) "வேத வசன மனன பயிற்சி (Memorization)" else "Bible Verse Memorization",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Text(
                        text = if (isTamil) "இடைவெளி மீள்பார்வை (Spaced Repetition) & நினைவாற்றல் பயிற்சிகள்" else "Master God's Word through Spaced Repetition (SRS) & interactive recall",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // SRS Learning Stats Dashboard Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTamil) "இன்றைய பயிற்சி புள்ளிவிவரம்" else "Spaced Repetition Stats",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                            Surface(
                                shape = CircleShape,
                                color = PrimaryRoyalBlue
                            ) {
                                Text(
                                    text = "${dueVerses.size} ${if (isTamil) "இன்று மறுபார்வைக்கு" else "Due Today"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrightGoldHighlight,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid (Total, Due, Learning, Memorized)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatBox(label = if (isTamil) "மொத்தம்" else "Total", value = "${allVerses.size}")
                            StatBox(label = if (isTamil) "மறுபார்வை" else "Due", value = "${dueVerses.size}")
                            StatBox(label = if (isTamil) "கற்றல்" else "Learning", value = "${allVerses.count { it.status == SrsStatus.LEARNING }}")
                            StatBox(label = if (isTamil) "மனனம்" else "Mastered", value = "${memorizedVerses.size}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Study Session Action Button
                        Button(
                            onClick = {
                                val targetVerse = dueVerses.firstOrNull() ?: allVerses.firstOrNull()
                                viewModel.openMemorizationSession(targetVerse)
                            },
                            enabled = allVerses.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = RichGold, contentColor = DeepNavyBackground),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (dueVerses.isNotEmpty())
                                    (if (isTamil) "இன்றைய மனன பயிற்சியைத் தொடங்கு (${dueVerses.size} வசனங்கள்)" else "Start Review Session (${dueVerses.size} Due)")
                                else
                                    (if (isTamil) "அனைத்து வசனங்களையும் பயிற்சி செய்க" else "Practice All Verses"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Section: Verses in Learning
            item {
                Text(
                    text = if (isTamil) "என் மனன வசனங்கள் (${allVerses.size})" else "My Memory Verses (${allVerses.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (allVerses.isEmpty()) {
                item {
                    Text(
                        text = if (isTamil) "மனனம் செய்வதற்கு வசனங்கள் எதுவும் சேர்க்கப்படவில்லை." else "No memory verses added yet. Tap '+ Add Memory Verse' or select a verse in the Bible reader.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(allVerses) { item ->
                    MemoryVerseCard(
                        verse = item,
                        isTamil = isTamil,
                        onPractice = { viewModel.openMemorizationSession(item) },
                        onDelete = {
                            coroutineScope.launch {
                                viewModel.advancedRepo.deleteMemoryVerse(item.id)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddVerseDialog) {
        var inputBookName by remember { mutableStateOf("John") }
        var inputChapter by remember { mutableStateOf("3") }
        var inputVerse by remember { mutableStateOf("16") }

        AlertDialog(
            onDismissRequest = { showAddVerseDialog = false },
            title = { Text(if (isTamil) "மனன வசனம் சேர்க்க" else "Add Verse to Memorize", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputBookName,
                        onValueChange = { inputBookName = it },
                        label = { Text("Book (e.g. John, Psalms)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputChapter,
                            onValueChange = { inputChapter = it },
                            label = { Text("Chapter") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = inputVerse,
                            onValueChange = { inputVerse = it },
                            label = { Text("Verse") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val book = CanonicalBibleData.findBookByName(inputBookName) ?: CanonicalBibleData.allBooks[0]
                        val ch = inputChapter.toIntOrNull() ?: 1
                        val v = inputVerse.toIntOrNull() ?: 1

                        coroutineScope.launch {
                            val verseEntity = viewModel.repository.getSingleVerse(book.id, ch, v, "en")
                            val textEn = verseEntity?.verseText ?: "For God so loved the world, that he gave his only begotten Son..."
                            val textTa = "தேவன், தம்முடைய ஒரேபேறான குமாரனை விசுவாசிக்கிற எவனும் கெட்டுப்போகாமல்..."

                            viewModel.advancedRepo.addVerseToMemorization(
                                bookId = book.id,
                                bookName = book.englishName,
                                chapter = ch,
                                verse = v,
                                refEn = "${book.englishName} $ch:$v",
                                refTa = "${book.tamilName} $ch:$v",
                                textEn = textEn,
                                textTa = textTa
                            )
                            showAddVerseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                ) {
                    Text(if (isTamil) "சேர்" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVerseDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BrightGoldHighlight)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SecondaryLightText)
    }
}

@Composable
fun MemoryVerseCard(
    verse: MemorizedVerse,
    isTamil: Boolean,
    onPractice: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPractice() }
            .testTag("memory_verse_${verse.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTamil) verse.referenceDisplayTa else verse.referenceDisplayEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (verse.status == SrsStatus.MEMORIZED) SuccessGreen.copy(alpha = 0.2f) else PrimaryRoyalBlue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (verse.status == SrsStatus.MEMORIZED) (if (isTamil) "நினைவிலுள்ளது" else "Memorized")
                                   else (if (isTamil) "கற்றல்" else "Interval: ${verse.intervalDays}d"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (verse.status == SrsStatus.MEMORIZED) SuccessGreen else BrightGoldHighlight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "“${if (isTamil) verse.verseTextTa else verse.verseTextEn}”",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onPractice,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isTamil) "பயிற்சி செய்" else "Practice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
