package com.example.ui.screens.topics

import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.TopicNoteEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Personal Bible Study Notes Tab for Topics (Requirement 1.4):
 * - Private, device-local secure storage in Room SQLite
 * - Create, Edit, and Delete personal study notes & reflections
 * - Timestamps for created and updated dates
 * - Confirmation dialog before permanent deletion
 */
@Composable
fun TopicPersonalNotesTab(
    topicId: String,
    topicTitle: String,
    notes: List<TopicNoteEntity>,
    isTamil: Boolean,
    onSaveNote: (title: String, content: String, verseRef: String) -> Unit,
    onUpdateNote: (TopicNoteEntity) -> Unit,
    onDeleteNote: (TopicNoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<TopicNoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<TopicNoteEntity?>(null) }

    // Add / Edit Dialog State
    var inputTitle by remember { mutableStateOf("") }
    var inputContent by remember { mutableStateOf("") }
    var inputVerseRef by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    // Dialog for adding or editing a note
    if (showAddNoteDialog || editingNote != null) {
        val isEditing = editingNote != null
        AlertDialog(
            onDismissRequest = {
                showAddNoteDialog = false
                editingNote = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = RichGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) (if (isTamil) "குறிப்பைத் திருத்துக" else "Edit Study Note")
                        else (if (isTamil) "புதிய ஆய்வுக் குறிப்பு" else "New Topic Study Note"),
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text(if (isTamil) "தலைப்பு / தியானக் குறிப்பு" else "Note Title / Observation") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("topic_note_title_input")
                    )

                    OutlinedTextField(
                        value = inputVerseRef,
                        onValueChange = { inputVerseRef = it },
                        label = { Text(if (isTamil) "தொடர்புடைய வசனக் குறிப்பு (விருப்பம்)" else "Associated Verse (Optional)") },
                        placeholder = { Text("e.g. John 3:16 / யோவான் 3:16") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputContent,
                        onValueChange = { inputContent = it },
                        label = { Text(if (isTamil) "உங்கள் தியானக் கருத்துகள்..." else "Your biblical reflections...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("topic_note_content_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isEditing) {
                            onUpdateNote(
                                editingNote!!.copy(
                                    title = inputTitle,
                                    content = inputContent,
                                    associatedVerseReference = inputVerseRef,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            editingNote = null
                        } else {
                            onSaveNote(inputTitle, inputContent, inputVerseRef)
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRoyalBlue,
                        contentColor = BrightGoldHighlight
                    ),
                    enabled = inputTitle.isNotBlank() || inputContent.isNotBlank(),
                    modifier = Modifier.testTag("save_topic_note_button")
                ) {
                    Text(if (isTamil) "சேமி" else "Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddNoteDialog = false
                    editingNote = null
                }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }

    // Confirmation Dialog for Note Deletion
    if (noteToDelete != null) {
        val target = noteToDelete!!
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTamil) "குறிப்பை நீக்கவா?" else "Delete Study Note?")
                }
            },
            text = {
                Text(
                    text = if (isTamil)
                        "\"${target.title.ifBlank { "இந்த குறிப்பை" }}\" நிரந்தரமாக நீக்க விரும்புகிறீர்களா? இந்த செயலை மாற்ற முடியாது."
                    else
                        "Are you sure you want to permanently delete \"${target.title.ifBlank { "this study note" }}\"? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNote(target)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isTamil) "நீக்குக" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("topic_personal_notes_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Add Note Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTamil) "தனிப்பட்ட வேத ஆய்வுக் குறிப்புகள்" else "Personal Study Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight
                        )
                        Text(
                            text = if (isTamil)
                                "உங்கள் தியானங்கள் உங்கள் சாதனத்தில் மட்டுமே பாதுகாப்பாக இருக்கும் (${notes.size} குறிப்புகள்)"
                            else
                                "Private reflections stored locally on your device (${notes.size} notes)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryLightText
                        )
                    }

                    Button(
                        onClick = {
                            inputTitle = ""
                            inputContent = ""
                            inputVerseRef = ""
                            showAddNoteDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRoyalBlue,
                            contentColor = BrightGoldHighlight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_topic_note_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTamil) "புதிய குறிப்பு" else "Add Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (notes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NoteAdd, contentDescription = null, tint = RichGold.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTamil) "இன்னும் குறிப்புகள் எதுவும் இல்லை" else "No personal notes yet for this topic",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTamil)
                                "தேவ வார்த்தையை தியானிக்கும்போது உங்கள் எண்ணங்கள் மற்றும் ஜெபக் குறிப்புகளை இங்கே சேமியுங்கள்."
                            else
                                "Tap 'Add Note' above to record your thoughts, study observations, and prayer reflections.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(notes, key = { it.id }) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_note_item_${note.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.title.ifBlank { if (isTamil) "பெயரிடப்படாத குறிப்பு" else "Untitled Reflection" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )

                            Row {
                                IconButton(
                                    onClick = {
                                        editingNote = note
                                        inputTitle = note.title
                                        inputContent = note.content
                                        inputVerseRef = note.associatedVerseReference
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { noteToDelete = note },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        if (note.associatedVerseReference.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PrimaryRoyalBlue.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "📖 ${note.associatedVerseReference}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RichGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "🕒 ${dateFormat.format(Date(note.updatedAt))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
