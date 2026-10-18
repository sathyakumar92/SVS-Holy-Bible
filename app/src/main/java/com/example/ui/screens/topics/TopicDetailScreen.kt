package com.example.ui.screens.topics

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.BibleVerse
import com.example.data.model.RichTopicStudy
import com.example.data.model.TopicReferenceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel

/**
 * Enhanced Bible Topic Detail Screen integrating:
 * 1. Bible Topic Deep Study (Introduction, Hebrew/Greek Original Words, Principles, Characters, Lessons, Applications)
 * 2. Bible Topic Timeline (Biblical narrative sequence, OT-to-NT fulfillment links, tap to open in reader)
 * 3. Compare Bible Verses (Multi-verse comparison, dual KJV & Tamil translations, synthesis)
 * 4. Personal Bible Study Notes (Add, edit, delete, private Room persistence, timestamps)
 * 5. AI-Powered Bible Topic Assistant (Context-aware study questions, grounded citations)
 * 6. Related Topic Discovery (Curated connected doctrines, direct navigation)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.bibleLanguage == "ta" || prefs.interfaceLanguage == "ta"

    val richStudy by viewModel.selectedTopicDetail.collectAsState()
    val surroundingVerses by viewModel.surroundingVerses.collectAsState()
    val userNotes by viewModel.currentTopicNotes.collectAsState()
    val aiMessages by viewModel.topicAssistantMessages.collectAsState()
    val isAiLoading by viewModel.isTopicAssistantLoading.collectAsState()
    val comparisonVerses by viewModel.comparisonVerses.collectAsState()
    val canNavigateToPrevTopic by viewModel.canNavigateToPreviousTopic.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedSectionGroup by remember { mutableStateOf("ALL") }
    var showPrintStudyDialog by remember { mutableStateOf(false) }
    var showAddRefDialog by remember { mutableStateOf(false) }

    // Dialog for adding verse note from passage card
    var noteDialogVerse by remember { mutableStateOf<BibleVerse?>(null) }
    var noteTitleInput by remember { mutableStateOf("") }
    var noteContentInput by remember { mutableStateOf("") }

    BackHandler {
        viewModel.navigateBack()
    }

    if (richStudy == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = RichGold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(if (isTamil) "தலைப்பு ஏற்றப்படுகிறது..." else "Loading Bible Topic...")
            }
        }
        return
    }

    val study = richStudy!!
    val topic = study.topic

    // Filter references by selected section
    val displayReferences = remember(selectedSectionGroup, study) {
        when (selectedSectionGroup) {
            "KEY_VERSES" -> study.keyVerses
            "PROMISES" -> study.promises
            "BIBLICAL_EXAMPLES" -> study.biblicalExamples
            "WARNINGS_INSTRUCTIONS" -> study.warningsAndInstructions
            "OLD_TESTAMENT" -> study.oldTestamentPassages
            "NEW_TESTAMENT" -> study.newTestamentPassages
            "TEACHINGS" -> study.teachings
            "DAILY_LIFE" -> study.dailyLifeVerses
            else -> study.allReferences
        }
    }

    // Modal Dialog for Surrounding Verses
    if (surroundingVerses != null) {
        val (prev, current, next) = surroundingVerses!!
        AlertDialog(
            onDismissRequest = { viewModel.clearSurroundingVerses() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTamil) "சூழல் வசனங்கள்" else "Surrounding Verses Context",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (prev != null) {
                        Text(
                            text = "${prev.verseNumber}. ${prev.verseText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (current != null) {
                        Surface(
                            color = PrimaryRoyalBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${current.verseNumber}. ${current.verseText}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    if (next != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${next.verseNumber}. ${next.verseText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSurroundingVerses() }) {
                    Text(if (isTamil) "மூடு" else "Close")
                }
            }
        )
    }

    // Modal Dialog for Quick Verse Note
    if (noteDialogVerse != null) {
        val targetVerse = noteDialogVerse!!
        AlertDialog(
            onDismissRequest = { noteDialogVerse = null },
            title = {
                Text(
                    text = "${if (isTamil) "வசனக் குறிப்பு: " else "Verse Note: "}${targetVerse.bookName} ${targetVerse.chapterNumber}:${targetVerse.verseNumber}",
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { noteTitleInput = it },
                        label = { Text(if (isTamil) "தலைப்பு" else "Note Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteContentInput,
                        onValueChange = { noteContentInput = it },
                        label = { Text(if (isTamil) "உங்கள் தியானக் கருத்துகள்..." else "Your reflection...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val refStr = "${targetVerse.bookName} ${targetVerse.chapterNumber}:${targetVerse.verseNumber}"
                        viewModel.saveTopicStudyNote(topic.id, noteTitleInput, noteContentInput, refStr)
                        noteDialogVerse = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                ) {
                    Text(if (isTamil) "சேமி" else "Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteDialogVerse = null }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }

    // Print Friendly Study Dialog
    if (showPrintStudyDialog) {
        PrintFriendlyStudyDialog(
            study = study,
            isTamil = isTamil,
            onDismiss = { showPrintStudyDialog = false },
            onCopyStudy = { text ->
                clipboardManager.setText(AnnotatedString(text))
            },
            onShareStudy = { text ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Topic Study"))
            }
        )
    }

    // Admin Add Reference Dialog
    if (showAddRefDialog) {
        AdminAddReferenceDialog(
            topicId = topic.id,
            isTamil = isTamil,
            onDismiss = { showAddRefDialog = false },
            onAddReference = { newRef ->
                viewModel.addTopicReference(newRef)
                showAddRefDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("topic_detail_screen")
    ) {
        // Top Topic Summary Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, RichGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Category & Verified References Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryRoyalBlue.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = if (isTamil) topic.categoryTa else topic.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${study.allReferences.size} ${if (isTamil) "வேத ஆதாரங்கள்" else "passages"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RichGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title in Tamil & English
                Text(
                    text = if (isTamil) topic.titleTa else topic.titleEn,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
                Text(
                    text = if (isTamil) topic.titleEn else topic.titleTa,
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryLightText.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row: Favorite, Share, Study View, Admin Add
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.toggleFavoriteTopic(topic.id) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (study.isFavorite) RichGold else ElevatedBlueSurface,
                            contentColor = if (study.isFavorite) Color.Black else PrimaryLightText
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (study.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (study.isFavorite) (if (isTamil) "சேமிக்கப்பட்டது" else "Saved")
                            else (if (isTamil) "சேமி" else "Favorite"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val shareText = buildString {
                                append("${topic.titleEn} / ${topic.titleTa}\n\n")
                                append(if (isTamil) topic.introductionTa else topic.introductionEn)
                                append("\n\nVerified Scripture References:\n")
                                study.allReferences.take(5).forEach { ref ->
                                    append("• ${ref.referenceEn}: ${ref.verseTextEn}\n")
                                }
                                append("\nShared via SVS Holy Bible")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Topic Study"))
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = PrimaryLightText, modifier = Modifier.size(16.dp))
                    }

                    OutlinedButton(
                        onClick = { showPrintStudyDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RichGold.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Study View", tint = BrightGoldHighlight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTamil) "தியான அறிக்கை" else "Study Sheet", fontSize = 11.sp, color = BrightGoldHighlight)
                    }
                }
            }
        }

        // Feature Navigation Tabs Row (All 6 Capabilities)
        val tabTitles = listOf(
            Pair(if (isTamil) "ஆழமான ஆய்வு" else "Deep Study", Icons.Default.AutoStories),
            Pair(if (isTamil) "வசனங்கள் (${displayReferences.size})" else "Passages (${displayReferences.size})", Icons.Default.MenuBook),
            Pair(if (isTamil) "காலவரிசை" else "Timeline", Icons.Default.Timeline),
            Pair(if (isTamil) "வசன ஒப்பீடு" else "Compare", Icons.Default.Compare),
            Pair(if (isTamil) "குறிப்புகள் (${userNotes.size})" else "Notes (${userNotes.size})", Icons.Default.EditNote),
            Pair(if (isTamil) "உதவியாளர்" else "AI Assistant", Icons.Default.Psychology),
            Pair(if (isTamil) "தொடர்புடையவை" else "Related", Icons.Default.AccountTree)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BrightGoldHighlight,
            edgePadding = 12.dp,
            modifier = Modifier.fillMaxWidth().testTag("topic_feature_tabs")
        ) {
            tabTitles.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selectedTabIndex == index) RichGold else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) BrightGoldHighlight else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        // Active Tab Content Display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTabIndex) {
                // 1. Bible Topic Deep Study Tab
                0 -> TopicDeepStudyTab(
                    study = study,
                    isTamil = isTamil
                )

                // 2. Verified Scripture Passages List (Standard Passages view)
                1 -> TopicPassagesView(
                    study = study,
                    displayReferences = displayReferences,
                    selectedSectionGroup = selectedSectionGroup,
                    isTamil = isTamil,
                    onSelectSectionGroup = { selectedSectionGroup = it },
                    onShowAddRef = { showAddRefDialog = true },
                    onOpenInReader = { ref ->
                        val book = CanonicalBibleData.findBookById(ref.bookId) ?: CanonicalBibleData.allBooks[0]
                        viewModel.openReader(book, ref.chapterNumber, ref.verseStart)
                    },
                    onViewSurrounding = { ref ->
                        val fakeVerse = BibleVerse(
                            bookId = ref.bookId,
                            bookName = if (isTamil) ref.bookNameTa else ref.bookNameEn,
                            chapterNumber = ref.chapterNumber,
                            verseNumber = ref.verseStart,
                            verseText = if (isTamil) ref.verseTextTa else ref.verseTextEn,
                            normalizedText = "",
                            editionId = if (isTamil) "TAMIL_BSI" else "KJV",
                            languageCode = if (isTamil) "ta" else "en"
                        )
                        viewModel.loadSurroundingVerses(fakeVerse)
                    },
                    onBookmark = { ref ->
                        val fakeVerse = BibleVerse(
                            bookId = ref.bookId,
                            bookName = if (isTamil) ref.bookNameTa else ref.bookNameEn,
                            chapterNumber = ref.chapterNumber,
                            verseNumber = ref.verseStart,
                            verseText = if (isTamil) ref.verseTextTa else ref.verseTextEn,
                            normalizedText = "",
                            editionId = if (isTamil) "TAMIL_BSI" else "KJV",
                            languageCode = if (isTamil) "ta" else "en"
                        )
                        viewModel.toggleBookmark(fakeVerse)
                    },
                    onAddNote = { ref ->
                        noteDialogVerse = BibleVerse(
                            bookId = ref.bookId,
                            bookName = if (isTamil) ref.bookNameTa else ref.bookNameEn,
                            chapterNumber = ref.chapterNumber,
                            verseNumber = ref.verseStart,
                            verseText = if (isTamil) ref.verseTextTa else ref.verseTextEn,
                            normalizedText = "",
                            editionId = if (isTamil) "TAMIL_BSI" else "KJV",
                            languageCode = if (isTamil) "ta" else "en"
                        )
                        noteTitleInput = if (isTamil) ref.referenceTa else ref.referenceEn
                        noteContentInput = ""
                    },
                    onCopy = { ref ->
                        val copyStr = "${if (isTamil) ref.verseTextTa else ref.verseTextEn}\n— ${if (isTamil) ref.referenceTa else ref.referenceEn} (SVS Holy Bible)"
                        clipboardManager.setText(AnnotatedString(copyStr))
                    },
                    onShare = { ref ->
                        val shareStr = "${if (isTamil) ref.verseTextTa else ref.verseTextEn}\n— ${if (isTamil) ref.referenceTa else ref.referenceEn}\n\nTopic: ${topic.titleEn} (${topic.titleTa})\nShared via SVS Holy Bible"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareStr)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Scripture Passage"))
                    },
                    onNextTopic = { viewModel.navigateNextTopic() },
                    onPrevTopic = { viewModel.navigatePreviousTopic() },
                    onOpenTopic = { id -> viewModel.openTopicDetail(id) }
                )

                // 3. Bible Topic Timeline Tab
                2 -> TopicTimelineTab(
                    timelineEntries = study.timelineEntries,
                    isTamil = isTamil,
                    onOpenReader = { book, chapter, verse ->
                        viewModel.openReader(book, chapter, verse)
                    }
                )

                // 4. Compare Bible Verses Tab
                3 -> TopicVerseComparisonTab(
                    allReferences = study.allReferences,
                    selectedVerses = comparisonVerses,
                    isTamil = isTamil,
                    onToggleVerse = { ref -> viewModel.toggleVerseForComparison(ref) },
                    onClearVerses = { viewModel.clearComparisonVerses() },
                    onOpenReader = { book, chapter, verse -> viewModel.openReader(book, chapter, verse) },
                    onViewSurrounding = { verse -> viewModel.loadSurroundingVerses(verse) }
                )

                // 5. Personal Bible Study Notes Tab
                4 -> TopicPersonalNotesTab(
                    topicId = topic.id,
                    topicTitle = if (isTamil) topic.titleTa else topic.titleEn,
                    notes = userNotes,
                    isTamil = isTamil,
                    onSaveNote = { title, content, verseRef ->
                        viewModel.saveTopicStudyNote(topic.id, title, content, verseRef)
                    },
                    onUpdateNote = { note -> viewModel.updateTopicStudyNote(note) },
                    onDeleteNote = { note -> viewModel.deleteTopicStudyNote(note) }
                )

                // 6. AI-Powered Bible Topic Assistant Tab
                5 -> TopicAiAssistantTab(
                    study = study,
                    messages = aiMessages,
                    isLoading = isAiLoading,
                    isTamil = isTamil,
                    onAskQuestion = { q -> viewModel.askTopicAssistant(q) },
                    onClearChat = { viewModel.clearTopicAssistantChat() },
                    onOpenReader = { book, chapter, verse -> viewModel.openReader(book, chapter, verse) }
                )

                // 7. Related Topic Discovery Tab
                6 -> TopicRelatedDiscoveryTab(
                    currentTopic = topic,
                    relatedTopics = study.relatedTopics,
                    canNavigateBackToPreviousTopic = canNavigateToPrevTopic,
                    onNavigateToPreviousTopic = { viewModel.navigateToPreviousTopic() },
                    isTamil = isTamil,
                    onOpenTopic = { id -> viewModel.openTopicDetail(id) }
                )
            }
        }
    }
}

/**
 * Standard Scripture Passages View (Filtered cards, reader launcher, next/prev topic)
 */
@Composable
private fun TopicPassagesView(
    study: RichTopicStudy,
    displayReferences: List<TopicReferenceEntity>,
    selectedSectionGroup: String,
    isTamil: Boolean,
    onSelectSectionGroup: (String) -> Unit,
    onShowAddRef: () -> Unit,
    onOpenInReader: (TopicReferenceEntity) -> Unit,
    onViewSurrounding: (TopicReferenceEntity) -> Unit,
    onBookmark: (TopicReferenceEntity) -> Unit,
    onAddNote: (TopicReferenceEntity) -> Unit,
    onCopy: (TopicReferenceEntity) -> Unit,
    onShare: (TopicReferenceEntity) -> Unit,
    onNextTopic: () -> Unit,
    onPrevTopic: () -> Unit,
    onOpenTopic: (String) -> Unit
) {
    val topic = study.topic

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("topic_passages_view"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Subsection Filter Chips Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "வேத வசன ஆதாரங்கள் (${displayReferences.size})" else "Verified Scripture Passages (${displayReferences.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )

                    TextButton(onClick = onShowAddRef) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = RichGold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(if (isTamil) "வசனம் சேர்க்க" else "Add Passage", fontSize = 11.sp, color = RichGold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val sectionChips = listOf(
                    Pair("ALL", if (isTamil) "அனைத்தும்" else "All (${study.allReferences.size})"),
                    Pair("KEY_VERSES", if (isTamil) "முக்கிய வசனங்கள்" else "Key Verses (${study.keyVerses.size})"),
                    Pair("PROMISES", if (isTamil) "வாக்குத்தத்தங்கள்" else "Promises (${study.promises.size})"),
                    Pair("BIBLICAL_EXAMPLES", if (isTamil) "வேத மாதிரிகள்" else "Examples (${study.biblicalExamples.size})"),
                    Pair("WARNINGS_INSTRUCTIONS", if (isTamil) "கற்பனைகள்" else "Instructions (${study.warningsAndInstructions.size})"),
                    Pair("OLD_TESTAMENT", if (isTamil) "பழைய ஏற்பாடு" else "Old Testament (${study.oldTestamentPassages.size})"),
                    Pair("NEW_TESTAMENT", if (isTamil) "புதிய ஏற்பாடு" else "New Testament (${study.newTestamentPassages.size})"),
                    Pair("TEACHINGS", if (isTamil) "உபதேசங்கள்" else "Teachings (${study.teachings.size})"),
                    Pair("DAILY_LIFE", if (isTamil) "அன்றாட வாழ்வு" else "Daily Life (${study.dailyLifeVerses.size})")
                ).filter { it.first == "ALL" || (it.first == "KEY_VERSES" && study.keyVerses.isNotEmpty()) ||
                           (it.first == "PROMISES" && study.promises.isNotEmpty()) ||
                           (it.first == "BIBLICAL_EXAMPLES" && study.biblicalExamples.isNotEmpty()) ||
                           (it.first == "WARNINGS_INSTRUCTIONS" && study.warningsAndInstructions.isNotEmpty()) ||
                           (it.first == "OLD_TESTAMENT" && study.oldTestamentPassages.isNotEmpty()) ||
                           (it.first == "NEW_TESTAMENT" && study.newTestamentPassages.isNotEmpty()) ||
                           (it.first == "TEACHINGS" && study.teachings.isNotEmpty()) ||
                           (it.first == "DAILY_LIFE" && study.dailyLifeVerses.isNotEmpty())
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sectionChips) { (key, label) ->
                        FilterChip(
                            selected = selectedSectionGroup == key,
                            onClick = { onSelectSectionGroup(key) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryRoyalBlue,
                                selectedLabelColor = BrightGoldHighlight
                            )
                        )
                    }
                }
            }
        }

        // Scripture Reference Cards
        items(displayReferences, key = { it.id }) { ref ->
            TopicScripturePassageCard(
                ref = ref,
                isTamil = isTamil,
                onOpenInReader = { onOpenInReader(ref) },
                onViewSurrounding = { onViewSurrounding(ref) },
                onBookmark = { onBookmark(ref) },
                onAddNote = { onAddNote(ref) },
                onCopy = { onCopy(ref) },
                onShare = { onShare(ref) }
            )
        }

        // Bottom Prev / Next Navigation Row
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onPrevTopic,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTamil) "முந்தைய தலைப்பு" else "Previous Topic", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onNextTopic,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRoyalBlue,
                            contentColor = BrightGoldHighlight
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isTamil) "அடுத்த தலைப்பு" else "Next Topic", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                // Curated Related Topic Chips
                if (study.relatedTopics.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (isTamil) "தொடர்புடைய தலைப்புகள்:" else "Related Biblical Topics:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(study.relatedTopics) { rel ->
                            ActionChip(
                                label = if (isTamil) rel.titleTa else rel.titleEn,
                                onClick = { onOpenTopic(rel.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Topic Scripture Card displaying verified references, both languages toggle, explanation, and reader action
 */
@Composable
fun TopicScripturePassageCard(
    ref: TopicReferenceEntity,
    isTamil: Boolean,
    onOpenInReader: () -> Unit,
    onViewSurrounding: () -> Unit,
    onBookmark: () -> Unit,
    onAddNote: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    var showBothLanguages by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("topic_passage_${ref.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Reference Label & Section Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = RichGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTamil) ref.referenceTa else ref.referenceEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PrimaryRoyalBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = when (ref.sectionGroup) {
                            "KEY_VERSES" -> if (isTamil) "முக்கிய வசனம்" else "Key Verse"
                            "PROMISES" -> if (isTamil) "வாக்குத்தத்தம்" else "Promise"
                            "BIBLICAL_EXAMPLES" -> if (isTamil) "வேத மாதிரி" else "Example"
                            "WARNINGS_INSTRUCTIONS" -> if (isTamil) "கற்பனை" else "Instruction"
                            "DAILY_LIFE" -> if (isTamil) "அன்றாட வாழ்வு" else "Daily Life"
                            else -> if (isTamil) "வேத ஆதாரம்" else "Passage"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RichGold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Verse Text
            Text(
                text = if (isTamil) ref.verseTextTa else ref.verseTextEn,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            // Secondary Verse Text (if toggled)
            if (showBothLanguages) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isTamil) ref.verseTextEn else ref.verseTextTa,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp),
                        lineHeight = 18.sp
                    )
                }
            }

            // Brief Context / Explanation
            val explanation = if (isTamil) ref.briefExplanationTa else ref.briefExplanationEn
            if (explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = RichGold.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Open in Bible Reader
                Button(
                    onClick = onOpenInReader,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRoyalBlue,
                        contentColor = BrightGoldHighlight
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_reader_button_${ref.id}")
                ) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTamil) "வேதத்தில் வாசிக்க" else "Open in Bible", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onViewSurrounding, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.UnfoldMore, contentDescription = "Surrounding Verses", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { showBothLanguages = !showBothLanguages }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Translate, contentDescription = "Toggle Translation", tint = if (showBothLanguages) RichGold else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onAddNote, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.NoteAdd, contentDescription = "Note", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onBookmark, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Print-Friendly Topic Study Sheet Dialog
 */
@Composable
fun PrintFriendlyStudyDialog(
    study: RichTopicStudy,
    isTamil: Boolean,
    onDismiss: () -> Unit,
    onCopyStudy: (String) -> Unit,
    onShareStudy: (String) -> Unit
) {
    val topic = study.topic
    val studySheetText = remember(study, isTamil) {
        buildString {
            append("=========================================\n")
            append("SVS HOLY BIBLE — BIBLE TOPIC STUDY SHEET\n")
            append("=========================================\n\n")
            append("TOPIC: ${topic.titleEn} / ${topic.titleTa}\n")
            append("CATEGORY: ${topic.category} (${topic.categoryTa})\n\n")
            append("1. INTRODUCTION & MEANING:\n")
            append(if (isTamil) topic.introductionTa else topic.introductionEn)
            append("\n\n2. THEOLOGICAL EXPLANATION:\n")
            append(if (isTamil) topic.detailedExplanationTa else topic.detailedExplanationEn)
            if (study.wordStudies.isNotEmpty()) {
                append("\n\n3. ORIGINAL BIBLICAL LANGUAGES (HEBREW & GREEK):\n")
                study.wordStudies.forEach { w ->
                    append("• ${w.language}: ${w.originalWord} (${w.transliteration})\n")
                    append("  Meaning: ${if (isTamil) w.literalMeaningTa else w.literalMeaningEn}\n")
                    append("  Theological Context: ${if (isTamil) w.theologicalSignificanceTa else w.theologicalSignificanceEn}\n")
                }
            }
            if (topic.biblicalPeopleEn.isNotBlank()) {
                append("\n\n4. BIBLICAL PEOPLE ASSOCIATED:\n")
                append(if (isTamil) topic.biblicalPeopleTa else topic.biblicalPeopleEn)
            }
            if (topic.practicalApplicationsEn.isNotBlank()) {
                append("\n\n5. PRACTICAL APPLICATIONS:\n")
                append(if (isTamil) topic.practicalApplicationsTa else topic.practicalApplicationsEn)
            }
            append("\n\n6. VERIFIED SCRIPTURE REFERENCES:\n")
            study.allReferences.forEachIndexed { i, ref ->
                append("\n[${i + 1}] ${ref.referenceEn} (${ref.referenceTa}):\n")
                append("English (KJV): ${ref.verseTextEn}\n")
                append("Tamil (BSI): ${ref.verseTextTa}\n")
                if (ref.briefExplanationEn.isNotBlank()) {
                    append("Explanation: ${if (isTamil) ref.briefExplanationTa else ref.briefExplanationEn}\n")
                }
            }
            append("\n=========================================\n")
            append("Generated via SVS Holy Bible\n")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "அச்சு / தியான அறிக்கை" else "Printable Study Sheet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn {
                        item {
                            Text(
                                text = studySheetText,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { onCopyStudy(studySheetText) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTamil) "நகலெடு" else "Copy Study")
                    }

                    Button(
                        onClick = { onShareStudy(studySheetText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRoyalBlue,
                            contentColor = BrightGoldHighlight
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTamil) "பகிர்" else "Share Study")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for Admin to add verified references to a topic
 */
@Composable
fun AdminAddReferenceDialog(
    topicId: String,
    isTamil: Boolean,
    onDismiss: () -> Unit,
    onAddReference: (TopicReferenceEntity) -> Unit
) {
    var bookId by remember { mutableStateOf("JHN") }
    var chapterStr by remember { mutableStateOf("3") }
    var verseStartStr by remember { mutableStateOf("16") }
    var verseEndStr by remember { mutableStateOf("") }
    var refEn by remember { mutableStateOf("John 3:16") }
    var refTa by remember { mutableStateOf("யோவான் 3:16") }
    var textEn by remember { mutableStateOf("") }
    var textTa by remember { mutableStateOf("") }
    var explanationEn by remember { mutableStateOf("") }
    var explanationTa by remember { mutableStateOf("") }
    var sectionGroup by remember { mutableStateOf("KEY_VERSES") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                item {
                    Text(
                        text = if (isTamil) "வேத ஆதாரம் சேர்க்க (Admin)" else "Add Scripture Reference (Admin)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bookId,
                            onValueChange = { bookId = it.uppercase() },
                            label = { Text("Book ID (e.g. JHN)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = chapterStr,
                            onValueChange = { chapterStr = it },
                            label = { Text("Chapter") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = verseStartStr,
                            onValueChange = { verseStartStr = it },
                            label = { Text("Verse") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = refEn,
                        onValueChange = { refEn = it },
                        label = { Text("Reference English (e.g. John 3:16)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = refTa,
                        onValueChange = { refTa = it },
                        label = { Text("வசனக் குறிப்பு தமிழ் (எ.கா. யோவான் 3:16)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = textEn,
                        onValueChange = { textEn = it },
                        label = { Text("Verse Text (English KJV)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = textTa,
                        onValueChange = { textTa = it },
                        label = { Text("வசன உரை (தமிழ் BSI)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = explanationEn,
                        onValueChange = { explanationEn = it },
                        label = { Text("Brief Explanation (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(if (isTamil) "ரத்து" else "Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val b = CanonicalBibleData.findBookById(bookId)
                                val newRef = TopicReferenceEntity(
                                    topicId = topicId,
                                    bookId = bookId,
                                    bookNameEn = b?.englishName ?: bookId,
                                    bookNameTa = b?.tamilName ?: bookId,
                                    chapterNumber = chapterStr.toIntOrNull() ?: 1,
                                    verseStart = verseStartStr.toIntOrNull() ?: 1,
                                    verseEnd = verseEndStr.toIntOrNull(),
                                    sectionGroup = sectionGroup,
                                    referenceEn = refEn,
                                    referenceTa = refTa,
                                    verseTextEn = textEn,
                                    verseTextTa = textTa,
                                    briefExplanationEn = explanationEn,
                                    briefExplanationTa = explanationTa,
                                    isVerified = true
                                )
                                onAddReference(newRef)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue,
                                contentColor = BrightGoldHighlight
                            ),
                            enabled = refEn.isNotBlank()
                        ) {
                            Text(if (isTamil) "சேமி" else "Add Reference")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
