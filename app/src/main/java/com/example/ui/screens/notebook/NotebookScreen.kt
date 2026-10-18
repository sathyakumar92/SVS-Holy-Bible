package com.example.ui.screens.notebook

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotebookFolder
import com.example.data.model.NotebookNote
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@Composable
fun NotebookScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var searchQuery by remember { mutableStateOf("") }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderNameEn by remember { mutableStateOf("") }
    var newFolderNameTa by remember { mutableStateOf("") }

    val folders by viewModel.advancedRepo.allFolders.collectAsState(initial = emptyList())
    val notesFlow = remember(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.advancedRepo.searchNotes(searchQuery)
        } else {
            viewModel.advancedRepo.allNotes
        }
    }
    val allNotes by notesFlow.collectAsState(initial = emptyList())

    val filteredNotes = allNotes.filter { note ->
        (selectedFolderId == null || note.folderId == selectedFolderId) &&
        (!showOnlyFavorites || note.isFavorite)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNoteEditor(null) },
                containerColor = RichGold,
                contentColor = DeepNavyBackground,
                modifier = Modifier.testTag("create_note_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("notebook_screen")
        ) {
            // Header & Search
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTamil) "வேதாகம ஆய்வு நோட்டுப்புத்தகம்" else "Bible Study Notebook",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                Text(
                    text = if (isTamil) "ஆய்வுக் குறிப்புகள், சிந்தனைகள் & வேத வசன இணைப்புகள்" else "Organized Scripture reflections, observation & prayer notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isTamil) "குறிப்புகளைத் தேடுங்கள்..." else "Search title, notes, tags...") },
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

            // Folder Chips & Create Folder Button
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFolderId == null && !showOnlyFavorites,
                        onClick = {
                            selectedFolderId = null
                            showOnlyFavorites = false
                        },
                        label = { Text(if (isTamil) "அனைத்தும்" else "All Notes") }
                    )
                }

                item {
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = {
                            showOnlyFavorites = !showOnlyFavorites
                            selectedFolderId = null
                        },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp)) },
                        label = { Text(if (isTamil) "விருப்பமானவை" else "Favorites") }
                    )
                }

                items(folders) { folder ->
                    FilterChip(
                        selected = selectedFolderId == folder.id,
                        onClick = {
                            selectedFolderId = if (selectedFolderId == folder.id) null else folder.id
                            showOnlyFavorites = false
                        },
                        label = { Text(if (isTamil) folder.nameTa else folder.nameEn) }
                    )
                }

                item {
                    AssistChip(
                        onClick = { showCreateFolderDialog = true },
                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp)) },
                        label = { Text(if (isTamil) "+ கோப்புறை" else "+ New Folder") }
                    )
                }
            }

            // Notes List
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = RichGold,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTamil) "நோட்டுப் புத்தகத்தில் குறிப்புகள் இல்லை" else "No study notes found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTamil) "வேத வசனங்களை தியானித்து புதிய குறிப்பைத் தொடங்குங்கள்." else "Create your first in-depth Bible study note with observations & prayers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.openNoteEditor(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTamil) "புதிய குறிப்பு எழுதுக" else "Create Note")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotes) { note ->
                        NotebookNoteCard(
                            note = note,
                            isTamil = isTamil,
                            onClick = { viewModel.openNoteEditor(note.id) },
                            onDelete = {
                                coroutineScope.launch {
                                    viewModel.advancedRepo.deleteNote(note.id)
                                }
                            },
                            onToggleFavorite = {
                                coroutineScope.launch {
                                    viewModel.advancedRepo.createOrUpdateNote(note.copy(isFavorite = !note.isFavorite))
                                }
                            },
                            onTogglePin = {
                                coroutineScope.launch {
                                    viewModel.advancedRepo.createOrUpdateNote(note.copy(isPinned = !note.isPinned))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text(if (isTamil) "புதிய கோப்புறை உருவாக்குக" else "Create Study Folder", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFolderNameEn,
                        onValueChange = { newFolderNameEn = it },
                        label = { Text("Folder Name (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFolderNameTa,
                        onValueChange = { newFolderNameTa = it },
                        label = { Text("கோப்புறை பெயர் (தமிழ்)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderNameEn.isNotBlank()) {
                            coroutineScope.launch {
                                viewModel.advancedRepo.createFolder(
                                    nameEn = newFolderNameEn.trim(),
                                    nameTa = if (newFolderNameTa.isNotBlank()) newFolderNameTa.trim() else newFolderNameEn.trim()
                                )
                                newFolderNameEn = ""
                                newFolderNameTa = ""
                                showCreateFolderDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                ) {
                    Text(if (isTamil) "உருவாக்கு" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun NotebookNoteCard(
    note: NotebookNote,
    isTamil: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notebook_note_${note.id}"),
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = BrightGoldHighlight,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                }

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (note.isFavorite) RichGold else SecondaryLightText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryLightText,
                maxLines = 3
            )

            if (note.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(3).forEach { tag ->
                        Surface(
                            shape = CircleShape,
                            color = PrimaryRoyalBlue.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                color = RichGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
