package com.example.ui.screens.notebook

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.NoteVerseRef
import com.example.data.model.NotebookNote
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val noteId by viewModel.selectedNoteId.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var observation by remember { mutableStateOf("") }
    var interpretation by remember { mutableStateOf("") }
    var applicationText by remember { mutableStateOf("") }
    var prayerText by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Notes & Reflections, 1: Structured Study (SOAP), 2: Scripture Refs
    var showAddRefDialog by remember { mutableStateOf(false) }

    val currentNoteId = noteId
    val attachedRefsFlow = remember(currentNoteId) {
        if (currentNoteId != null && currentNoteId != 0L) {
            viewModel.advancedRepo.getVerseRefsForNote(currentNoteId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList<NoteVerseRef>())
        }
    }
    val attachedRefs by attachedRefsFlow.collectAsState(initial = emptyList())

    // Load existing note if editing
    LaunchedEffect(noteId) {
        if (noteId != null && noteId != 0L) {
            val existing = viewModel.advancedRepo.getNoteById(noteId!!)
            if (existing != null) {
                title = existing.title
                content = existing.content
                observation = existing.observation
                interpretation = existing.interpretation
                applicationText = existing.application
                prayerText = existing.prayer
                tags = existing.tags
                isPinned = existing.isPinned
                isFavorite = existing.isFavorite
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("note_editor_screen")
    ) {
        // Top Action Bar
        Surface(
            color = ElevatedBlueSurface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
                    }
                    Text(
                        text = if (noteId == null || noteId == 0L) (if (isTamil) "புதிய குறிப்பு" else "New Study Note")
                               else (if (isTamil) "குறிப்பைத் திருத்து" else "Edit Study Note"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) RichGold else SecondaryLightText
                        )
                    }

                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) RichGold else SecondaryLightText
                        )
                    }

                    // Share Note Action
                    IconButton(
                        onClick = {
                            val shareBody = buildString {
                                appendLine("=== $title ===")
                                if (content.isNotBlank()) appendLine(content)
                                if (observation.isNotBlank()) appendLine("\nObservation: $observation")
                                if (interpretation.isNotBlank()) appendLine("Interpretation: $interpretation")
                                if (applicationText.isNotBlank()) appendLine("Application: $applicationText")
                                if (prayerText.isNotBlank()) appendLine("Prayer: $prayerText")
                                appendLine("\nShared via SVS Holy Bible")
                            }
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Study Note"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = PrimaryLightText)
                    }

                    // Save Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val savedId = viewModel.advancedRepo.createOrUpdateNote(
                                    NotebookNote(
                                        id = noteId ?: 0L,
                                        title = if (title.isBlank()) (if (isTamil) "தலைப்பற்ற குறிப்பு" else "Untitled Study Note") else title.trim(),
                                        content = content.trim(),
                                        observation = observation.trim(),
                                        interpretation = interpretation.trim(),
                                        application = applicationText.trim(),
                                        prayer = prayerText.trim(),
                                        tags = tags.trim(),
                                        isPinned = isPinned,
                                        isFavorite = isFavorite
                                    )
                                )
                                viewModel.navigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RichGold, contentColor = DeepNavyBackground),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (isTamil) "சேமி" else "Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tabs: 0: General, 1: SOAP Method, 2: Scripture Refs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = RichGold
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isTamil) "பொதுவான குறிப்பு" else "Notes", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isTamil) "SOAP வேத ஆய்வு" else "SOAP Study", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if (isTamil) "வசனங்கள் (${attachedRefs.size})" else "Scripture (${attachedRefs.size})", fontWeight = FontWeight.Bold) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Note Title Input
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isTamil) "குறிப்பு தலைப்பு" else "Note Title") },
                    placeholder = { Text(if (isTamil) "எ.கா. யோவான் 3 தியானம்" else "e.g. Meditation on John 3") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text(if (isTamil) "குறிச்சொற்கள் (Tags, காற்புள்ளியால் பிரிக்கவும்)" else "Tags (comma separated)") },
                    placeholder = { Text("Faith, Peace, Grace") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            if (selectedTab == 0) {
                item {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text(if (isTamil) "முக்கிய குறிப்புகள் & தியானங்கள்" else "Study Notes & Reflections") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 280.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            } else if (selectedTab == 1) {
                // SOAP Method (Scripture, Observation, Application, Prayer)
                item {
                    Text(
                        text = if (isTamil) "SOAP முறை: கவனிப்பு, விளக்கம், பொருத்துதல், ஜெபம்" else "SOAP Method: Observation, Interpretation, Application, Prayer",
                        style = MaterialTheme.typography.labelSmall,
                        color = RichGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = observation,
                        onValueChange = { observation = it },
                        label = { Text(if (isTamil) "1. கவனிப்பு (Observation: இந்த வசனம் என்ன கூறுகிறது?)" else "1. Observation (What does the passage say?)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = interpretation,
                        onValueChange = { interpretation = it },
                        label = { Text(if (isTamil) "2. விளக்கம் (Interpretation: இதன் ஆவிக்குரிய உண்மை என்ன?)" else "2. Interpretation (What spiritual truth is revealed?)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = applicationText,
                        onValueChange = { applicationText = it },
                        label = { Text(if (isTamil) "3. பொருத்துதல் (Application: என் வாழ்வில் எப்படி நடைமுறைப்படுத்துவது?)" else "3. Application (How does this apply to my life today?)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = prayerText,
                        onValueChange = { prayerText = it },
                        label = { Text(if (isTamil) "4. ஜெபம் (Prayer: இந்த வசனத்தின்படி என் ஜெப விண்ணப்பம்)" else "4. Prayer (Personal prayer based on this scripture)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            } else {
                // Attached Scripture References Tab
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTamil) "இணைக்கப்பட்ட வேத வசனங்கள்" else "Attached Scripture References",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichGold
                        )
                        Button(
                            onClick = { showAddRefDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTamil) "+ வசனம் சேர்" else "+ Add Verse")
                        }
                    }
                }

                if (attachedRefs.isEmpty()) {
                    item {
                        Text(
                            text = if (isTamil) "வேத வாசகத்திலிருந்து அல்லது மேலேயுள்ள பட்டனைப் பயன்படுத்தி வசனங்களை இணைக்கலாம்." else "No attached Scripture references yet. Use the button above to link verses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(attachedRefs) { ref ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${ref.bookName} ${ref.chapterNumber}:${ref.startVerse}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BrightGoldHighlight
                                    )
                                    Row {
                                        TextButton(
                                            onClick = {
                                                val book = CanonicalBibleData.findBookById(ref.bookId) ?: CanonicalBibleData.allBooks[0]
                                                viewModel.openReader(book, ref.chapterNumber, ref.startVerse)
                                            }
                                        ) {
                                            Text(if (isTamil) "வாசிக்க" else "Read", color = RichGold, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    viewModel.advancedRepo.removeVerseRef(ref.id)
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                if (ref.verseText.isNotBlank()) {
                                    Text(
                                        text = "“${ref.verseText}”",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryLightText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddRefDialog) {
        var inputBookName by remember { mutableStateOf("John") }
        var inputChapter by remember { mutableStateOf("3") }
        var inputVerse by remember { mutableStateOf("16") }

        AlertDialog(
            onDismissRequest = { showAddRefDialog = false },
            title = { Text(if (isTamil) "வேத வசனத்தை இணைக்குக" else "Attach Bible Reference", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputBookName,
                        onValueChange = { inputBookName = it },
                        label = { Text("Book (e.g. John, Genesis)") },
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
                            val activeNoteId = noteId ?: viewModel.advancedRepo.createOrUpdateNote(
                                NotebookNote(
                                    title = if (title.isBlank()) "${book.englishName} $ch:$v Study" else title,
                                    content = content
                                )
                            )
                            viewModel.selectedNoteId.value = activeNoteId
                            viewModel.advancedRepo.attachVerseRef(
                                NoteVerseRef(
                                    noteId = activeNoteId,
                                    translationId = "KJV",
                                    bookId = book.id,
                                    bookName = book.englishName,
                                    chapterNumber = ch,
                                    startVerse = v,
                                    verseText = ""
                                )
                            )
                            showAddRefDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                ) {
                    Text(if (isTamil) "இணை" else "Attach")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRefDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}
