package com.example.ui.screens.library

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.BibleNote
import com.example.data.model.Bookmark
import com.example.data.model.ReadingHistory
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel

@Composable
fun LibraryScreen(
    viewModel: BibleViewModel
) {
    val bookmarks by viewModel.allBookmarks.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val history by viewModel.repository.getRecentHistory().collectAsState(initial = emptyList())
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Bookmarks, 1: Notes, 2: History

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = RichGold
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isTamil) "புக்மார்க்குகள் (${bookmarks.size})" else "Bookmarks (${bookmarks.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isTamil) "குறிப்புகள் (${notes.size})" else "Notes (${notes.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if (isTamil) "வரலாறு" else "History", fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> BookmarksList(
                bookmarks = bookmarks,
                isTamil = isTamil,
                onOpen = { b ->
                    val book = CanonicalBibleData.findBookById(b.bookId) ?: CanonicalBibleData.allBooks[0]
                    viewModel.openReader(book, b.chapterNumber, b.verseNumber)
                },
                onDelete = { b -> viewModel.removeBookmark(b) }
            )
            1 -> NotesList(
                notes = notes,
                isTamil = isTamil,
                onOpen = { n ->
                    val book = CanonicalBibleData.findBookById(n.bookId) ?: CanonicalBibleData.allBooks[0]
                    viewModel.openReader(book, n.chapterNumber, n.verseNumber)
                },
                onDelete = { n -> viewModel.deleteNote(n.id) }
            )
            2 -> HistoryList(
                history = history,
                isTamil = isTamil,
                onOpen = { h ->
                    val book = CanonicalBibleData.findBookById(h.bookId) ?: CanonicalBibleData.allBooks[0]
                    viewModel.openReader(book, h.chapterNumber, h.verseNumber)
                },
                onClear = { viewModel.clearHistory() }
            )
        }
    }
}

@Composable
fun BookmarksList(
    bookmarks: List<Bookmark>,
    isTamil: Boolean,
    onOpen: (Bookmark) -> Unit,
    onDelete: (Bookmark) -> Unit
) {
    if (bookmarks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isTamil) "சேமிக்கப்பட்ட புக்மார்க்குகள் எதுவும் இல்லை" else "No saved bookmarks yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bookmarks) { b ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(b) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = b.reference,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = b.verseText,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { onDelete(b) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesList(
    notes: List<BibleNote>,
    isTamil: Boolean,
    onOpen: (BibleNote) -> Unit,
    onDelete: (BibleNote) -> Unit
) {
    if (notes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isTamil) "எழுதப்பட்ட குறிப்புகள் எதுவும் இல்லை" else "No notes added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notes) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(note) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.noteTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Text(
                                text = note.reference,
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryRoyalBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.noteContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { onDelete(note) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryList(
    history: List<ReadingHistory>,
    isTamil: Boolean,
    onOpen: (ReadingHistory) -> Unit,
    onClear: () -> Unit
) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isTamil) "வாசிப்பு வரலாறு எதுவும் இல்லை" else "No reading history recorded yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClear) {
                    Text(if (isTamil) "வரலாற்றை அழிக்க" else "Clear History", color = MaterialTheme.colorScheme.error)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history) { h ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(h) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${h.bookName} • ${if (isTamil) "அதிகாரம்" else "Chapter"} ${h.chapterNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${if (isTamil) "வசனம்" else "Verse"} ${h.verseNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RichGold
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open")
                        }
                    }
                }
            }
        }
    }
}
