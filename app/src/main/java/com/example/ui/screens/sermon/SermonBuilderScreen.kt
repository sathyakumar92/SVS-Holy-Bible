package com.example.ui.screens.sermon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.SermonDocType
import com.example.data.model.SermonDocument
import com.example.data.model.SermonSection
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@Composable
fun SermonBuilderScreen(
    viewModel: BibleViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var selectedTypeFilter by remember { mutableStateOf<SermonDocType?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val docsFlow = remember(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.advancedRepo.searchSermonDocs(searchQuery)
        } else {
            viewModel.advancedRepo.allSermonDocs
        }
    }
    val allDocs by docsFlow.collectAsState(initial = emptyList())

    val filteredDocs = allDocs.filter { doc ->
        selectedTypeFilter == null || doc.docType == selectedTypeFilter
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openSermonEditor(null) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (isTamil) "புதிய பிரசங்கம் / பாடம்" else "New Sermon / Lesson", fontWeight = FontWeight.Bold) },
                containerColor = RichGold,
                contentColor = DeepNavyBackground,
                modifier = Modifier.testTag("create_sermon_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("sermon_builder_screen")
        ) {
            // Header & Search
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTamil) "பிரசங்கக் குறிப்புகள் & வேத பாட கட்டமைப்பு" else "Sermon Notes & Bible Lesson Builder",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                Text(
                    text = if (isTamil) "பிரசங்கங்கள், வேதபாட வகுப்புகள் மற்றும் தியான கட்டமைப்புகளை உருவாக்குங்கள்" else "Structured outlines, teaching lessons & devotional sermon builder",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isTamil) "பிரசங்கங்களைத் தேடுங்கள்..." else "Search sermon title, theme, passage...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RichGold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Document Type Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text(if (isTamil) "அனைத்தும்" else "All Types") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == SermonDocType.SERMON_OUTLINE,
                        onClick = { selectedTypeFilter = SermonDocType.SERMON_OUTLINE },
                        label = { Text(if (isTamil) "பிரசங்க குறிப்பு" else "Sermon Outline") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == SermonDocType.BIBLE_LESSON,
                        onClick = { selectedTypeFilter = SermonDocType.BIBLE_LESSON },
                        label = { Text(if (isTamil) "வேத பாடம்" else "Bible Lesson") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == SermonDocType.DEVOTIONAL,
                        onClick = { selectedTypeFilter = SermonDocType.DEVOTIONAL },
                        label = { Text(if (isTamil) "தியான குறிப்பு" else "Devotional") }
                    )
                }
            }

            // Documents List
            if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = RichGold,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTamil) "பிரசங்க ஆவணங்கள் எதுவும் இல்லை" else "No sermon outlines created yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTamil) "உபதேசப் பகுதிகள், போதனை மற்றும் வசன குறிப்புகளுடன் தொடங்கவும்." else "Build structured preaching outlines, teaching points and lessons.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.openSermonEditor(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTamil) "புதிய ஆவணத்தை உருவாக்குக" else "Create Outline")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDocs) { doc ->
                        SermonDocCard(
                            doc = doc,
                            isTamil = isTamil,
                            onClick = { viewModel.openSermonEditor(doc.id) },
                            onDelete = {
                                coroutineScope.launch {
                                    viewModel.advancedRepo.deleteSermonDoc(doc.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SermonDocCard(
    doc: SermonDocument,
    isTamil: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("sermon_doc_${doc.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                    if (doc.subtitleOrTheme.isNotBlank()) {
                        Text(
                            text = doc.subtitleOrTheme,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryLightText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryRoyalBlue.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = when (doc.docType) {
                                SermonDocType.SERMON_OUTLINE -> if (isTamil) "பிரசங்கம்" else "Sermon"
                                SermonDocType.BIBLE_LESSON -> if (isTamil) "வேத பாடம்" else "Lesson"
                                SermonDocType.DEVOTIONAL -> if (isTamil) "தியானம்" else "Devotional"
                                SermonDocType.CUSTOM_OUTLINE -> if (isTamil) "கட்டமைப்பு" else "Outline"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RichGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (doc.mainPassageRef.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = doc.mainPassageRef,
                        style = MaterialTheme.typography.labelSmall,
                        color = RichGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
