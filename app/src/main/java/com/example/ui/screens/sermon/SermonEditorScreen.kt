package com.example.ui.screens.sermon

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.model.SermonDocType
import com.example.data.model.SermonDocument
import com.example.data.model.SermonSection
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SermonEditorScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val docId by viewModel.selectedSermonDocId.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var title by remember { mutableStateOf("") }
    var subtitleOrTheme by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf(SermonDocType.SERMON_OUTLINE) }
    var audience by remember { mutableStateOf("") }
    var mainPassageRef by remember { mutableStateOf("") }

    val currentDocId = docId
    val sectionsFlow = remember(currentDocId) {
        if (currentDocId != null && currentDocId != 0L) {
            viewModel.advancedRepo.getSectionsForDoc(currentDocId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList<SermonSection>())
        }
    }
    val sections by sectionsFlow.collectAsState(initial = emptyList())

    var showAddSectionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(docId) {
        if (docId != null && docId != 0L) {
            val existing = viewModel.advancedRepo.getSermonDocById(docId!!)
            if (existing != null) {
                title = existing.title
                subtitleOrTheme = existing.subtitleOrTheme
                docType = existing.docType
                audience = existing.audience
                mainPassageRef = existing.mainPassageRef
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sermon_editor_screen")
    ) {
        // App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (docId == null || docId == 0L) (if (isTamil) "புதிய பிரசங்கம்" else "New Sermon Outline")
                           else (if (isTamil) "பிரசங்கக் குறிப்பைத் திருத்து" else "Edit Outline"),
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
                }
            },
            actions = {
                // Share Outline
                IconButton(
                    onClick = {
                        val shareBody = buildString {
                            appendLine("# $title")
                            if (subtitleOrTheme.isNotBlank()) appendLine("### Theme: $subtitleOrTheme")
                            if (mainPassageRef.isNotBlank()) appendLine("**Key Passage:** $mainPassageRef\n")
                            sections.forEach { s ->
                                appendLine("## ${s.sectionTitle}")
                                appendLine(s.content)
                                if (s.attachedReferences.isNotBlank()) appendLine("*References: ${s.attachedReferences}*\n")
                            }
                            appendLine("\nShared via SVS Holy Bible")
                        }
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareBody)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Sermon Outline"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = BrightGoldHighlight)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val activeId = viewModel.advancedRepo.saveSermonDoc(
                                SermonDocument(
                                    id = docId ?: 0L,
                                    title = if (title.isBlank()) (if (isTamil) "தலைப்பற்ற பிரசங்கம்" else "Untitled Sermon") else title.trim(),
                                    subtitleOrTheme = subtitleOrTheme.trim(),
                                    docType = docType,
                                    audience = audience.trim(),
                                    mainPassageRef = mainPassageRef.trim()
                                )
                            )
                            viewModel.selectedSermonDocId.value = activeId
                            viewModel.navigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RichGold, contentColor = DeepNavyBackground),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(if (isTamil) "சேமி" else "Save", fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ElevatedBlueSurface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isTamil) "பிரசங்க தலைப்பு" else "Sermon Title / Lesson Name") },
                    placeholder = { Text("e.g. Walking in Divine Peace") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = subtitleOrTheme,
                    onValueChange = { subtitleOrTheme = it },
                    label = { Text(if (isTamil) "முக்கிய மையக்கருத்து (Theme / Subtitle)" else "Main Theme / Subtitle") },
                    placeholder = { Text("Overcoming Anxiety with Faith") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mainPassageRef,
                        onValueChange = { mainPassageRef = it },
                        label = { Text(if (isTamil) "முக்கிய வேதப்பகுதி" else "Main Passage") },
                        placeholder = { Text("Philippians 4:4-7") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = audience,
                        onValueChange = { audience = it },
                        label = { Text("Audience") },
                        placeholder = { Text("Congregation") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Outline Sections Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "பிரசங்க கட்டமைப்பு பிரிவுகள் (${sections.size})" else "Outline Sections (${sections.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Button(
                        onClick = { showAddSectionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTamil) "+ பிரிவு சேர்" else "+ Add Section")
                    }
                }
            }

            if (sections.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isTamil) "கட்டமைப்பு பிரிவுகள் இன்னும் சேர்க்கப்படவில்லை" else "No outline sections added yet",
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isTamil) "அறிமுகம், முக்கிய குறிப்புகள், விளக்கங்கள் மற்றும் நடைமுறை பொருத்துதல்களை வரிசையாக சேர்க்கவும்."
                                       else "Add Introduction, Main Points, Illustrations, and Practical Applications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryLightText
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(sections) { idx, section ->
                    SermonSectionRow(
                        section = section,
                        isTamil = isTamil,
                        onDelete = {
                            coroutineScope.launch {
                                viewModel.advancedRepo.deleteSection(section.id)
                            }
                        },
                        onOpenReference = {
                            val tokens = section.attachedReferences.split(" ")
                            val bookName = tokens.getOrNull(0) ?: "John"
                            val chapter = tokens.getOrNull(1)?.substringBefore(":")?.toIntOrNull() ?: 1
                            val book = CanonicalBibleData.findBookByName(bookName) ?: CanonicalBibleData.allBooks[0]
                            viewModel.openReader(book, chapter)
                        }
                    )
                }
            }
        }
    }

    if (showAddSectionDialog) {
        var sectionTitleInput by remember { mutableStateOf("") }
        var sectionContentInput by remember { mutableStateOf("") }
        var sectionRefsInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = { Text(if (isTamil) "புதிய பிரிவு சேர்க்க" else "Add Outline Section", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = sectionTitleInput,
                        onValueChange = { sectionTitleInput = it },
                        label = { Text("Section Title (e.g. 2. Main Point: Walking by Faith)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sectionContentInput,
                        onValueChange = { sectionContentInput = it },
                        label = { Text("Teaching Content & Illustrations") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sectionRefsInput,
                        onValueChange = { sectionRefsInput = it },
                        label = { Text("Scripture Reference (e.g. 2 Corinthians 5:7)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val activeDocId = docId ?: viewModel.advancedRepo.saveSermonDoc(
                                SermonDocument(
                                    title = if (title.isBlank()) "Sermon Outline" else title,
                                    subtitleOrTheme = subtitleOrTheme
                                )
                            )
                            viewModel.selectedSermonDocId.value = activeDocId
                            viewModel.advancedRepo.saveSection(
                                SermonSection(
                                    documentId = activeDocId,
                                    sectionOrder = sections.size + 1,
                                    sectionTitle = if (sectionTitleInput.isBlank()) "Point ${sections.size + 1}" else sectionTitleInput.trim(),
                                    content = sectionContentInput.trim(),
                                    attachedReferences = sectionRefsInput.trim()
                                )
                            )
                            showAddSectionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                ) {
                    Text(if (isTamil) "சேர்" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun SermonSectionRow(
    section: SermonSection,
    isTamil: Boolean,
    onDelete: () -> Unit,
    onOpenReference: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.sectionTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }

            if (section.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = section.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryLightText,
                    lineHeight = 22.sp
                )
            }

            if (section.attachedReferences.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ref: ${section.attachedReferences}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RichGold,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onOpenReference, contentPadding = PaddingValues(0.dp)) {
                        Text(if (isTamil) "வேதத்தில் வாசிக்க →" else "Read in Bible →", color = BrightGoldHighlight, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
