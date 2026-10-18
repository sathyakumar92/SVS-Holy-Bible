package com.example.ui.screens.search

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.BibleBook
import com.example.data.model.BibleVerse
import com.example.data.model.SearchResultItem
import com.example.data.model.TopicEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel
import com.example.util.AdvancedSearchEngine
import com.example.util.BiblicalAnswer
import com.example.util.ReferenceParseResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    val searchQuery by viewModel.searchQuery.collectAsState()
    val categorizedResults by viewModel.categorizedSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchLanguage by viewModel.searchLanguage.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()
    val wholeWord by viewModel.wholeWordMatching.collectAsState()
    val caseSensitive by viewModel.caseSensitiveSearch.collectAsState()
    val sortOrder by viewModel.searchSortOrder.collectAsState()
    val testamentFilter by viewModel.testamentFilter.collectAsState()
    val bookFilter by viewModel.bookFilter.collectAsState()
    val fromChapter by viewModel.fromChapterFilter.collectAsState()
    val toChapter by viewModel.toChapterFilter.collectAsState()
    val fromVerse by viewModel.fromVerseFilter.collectAsState()
    val toVerse by viewModel.toVerseFilter.collectAsState()
    val surroundingVerses by viewModel.surroundingVerses.collectAsState()

    val prefs by viewModel.userPreferences.collectAsState()
    val effectiveLang = searchLanguage ?: prefs.bibleLanguage
    val isTamil = effectiveLang == "ta"

    var showAdvancedFilters by remember { mutableStateOf(false) }
    var showBookDropdown by remember { mutableStateOf(false) }
    var showDirectNavigator by remember { mutableStateOf(false) }
    var noteDialogVerse by remember { mutableStateOf<BibleVerse?>(null) }
    var noteTitleInput by remember { mutableStateOf("") }
    var noteContentInput by remember { mutableStateOf("") }
    var resultCategoryTab by remember { mutableStateOf(0) } // 0: All, 1: Topics, 2: Verses, 3: Q&A Answers, 4: Chapters
    var visibleResultsLimit by remember { mutableStateOf(20) }

    val recentSearches = prefs.recentSearches
    val suggestions = if (isTamil) {
        listOf("விசுவாசம்", "ஜெபம்", "அன்பு", "பரிசுத்த ஆவி", "யோவான் 3:16", "சங்கீதம் 23", "ரோமர் 8:28", "மன்னிப்பு", "பயத்தை வெல்லுதல்", "நித்திய ஜீவன்")
    } else {
        listOf("Faith", "Prayer", "Love", "Holy Spirit", "John 3:16", "Psalm 23", "Romans 8:28", "Forgiveness", "Overcoming fear", "Eternal life")
    }

    val popularSearchChips = if (isTamil) {
        listOf("ஜெபத்தைப் பற்றி வேதாகமம் என்ன சொல்கிறது?", "விசுவாசம்", "பயம் மற்றும் சமாதானம்", "இயேசுவின் அன்பு", "சங்கீதம் 91")
    } else {
        listOf("What does the Bible say about prayer?", "How to strengthen faith?", "Overcoming fear", "John 3:16", "Fruit of the Spirit")
    }

    val selectedBookName = remember(bookFilter, isTamil) {
        if (bookFilter != null) {
            val b = CanonicalBibleData.findBookById(bookFilter!!)
            if (b != null) (if (isTamil) b.tamilName else b.englishName) else "All Books"
        } else {
            if (isTamil) "அனைத்து புத்தகங்கள்" else "All Books"
        }
    }

    val hasActiveFilters = testamentFilter != null || bookFilter != null || fromChapter > 0 || toChapter > 0 ||
            fromVerse > 0 || toVerse > 0 || searchMode != SearchMode.WORD || wholeWord || caseSensitive || sortOrder != SearchSortOrder.RELEVANCE

    // Dialog for Personal Note
    if (noteDialogVerse != null) {
        val targetVerse = noteDialogVerse!!
        AlertDialog(
            onDismissRequest = { noteDialogVerse = null },
            title = {
                Text(
                    text = if (isTamil) "வசனக் குறிப்பு சேர்க்க (${targetVerse.bookName} ${targetVerse.chapterNumber}:${targetVerse.verseNumber})"
                           else "Add Note for ${targetVerse.bookName} ${targetVerse.chapterNumber}:${targetVerse.verseNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = BrightGoldHighlight,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { noteTitleInput = it },
                        label = { Text(if (isTamil) "குறிப்பு தலைப்பு" else "Note Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteContentInput,
                        onValueChange = { noteContentInput = it },
                        label = { Text(if (isTamil) "உங்கள் சிந்தனைகள் அல்லது தியானம்..." else "Your reflection or note...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveVerseNote(targetVerse, noteTitleInput, noteContentInput)
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
                        color = BrightGoldHighlight,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (prev != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${prev.bookName} ${prev.chapterNumber}:${prev.verseNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = prev.verseText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (current != null) {
                        Surface(
                            color = PrimaryRoyalBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RichGold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${current.bookName} ${current.chapterNumber}:${current.verseNumber} (தேர்ந்தெடுக்கப்பட்ட வசனம்)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrightGoldHighlight,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = current.verseText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    if (next != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${next.bookName} ${next.chapterNumber}:${next.verseNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = next.verseText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSurroundingVerses() }) {
                    Text(if (isTamil) "மூடுக" else "Close", color = RichGold)
                }
            }
        )
    }

    // Direct Scripture Navigator Dialog
    if (showDirectNavigator) {
        DirectScriptureNavigatorDialog(
            isTamil = isTamil,
            onDismiss = { showDirectNavigator = false },
            onNavigate = { book, chapter, verse ->
                showDirectNavigator = false
                viewModel.openReader(book, chapter, verse)
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Prominent Search Bar & Mode Selector
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(PrimaryRoyalBlue.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isTamil) "மேம்பட்ட வேதாகம தேடல்" else "Advanced Bible Search Engine",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Text(
                                text = if (isTamil) "வசனங்கள், தலைப்புகள், கேள்விகள் & வசனக் குறிப்புகள்" else "Verses, Topics, Questions, and Exact References",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Direct Book/Chapter/Verse Navigator Button
                        FilledTonalButton(
                            onClick = { showDirectNavigator = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("direct_navigator_button")
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTamil) "நேரடி வழிசெலுத்தி" else "Navigator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = if (isTamil) "யோவான் 3:16, அன்பு, அல்லது கேள்வி கேட்கவும்..."
                                else "John 3:16, faith, or ask 'What does Bible say about prayer?'...",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = RichGold)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(
                                    onClick = { showAdvancedFilters = !showAdvancedFilters },
                                    modifier = Modifier.testTag("advanced_filters_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (hasActiveFilters) Icons.Filled.FilterAlt else Icons.Outlined.FilterAlt,
                                        contentDescription = "Filters",
                                        tint = if (hasActiveFilters) BrightGoldHighlight else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RichGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("advanced_search_input")
                    )

                    // Quick Search Mode Pills
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val modes = listOf(
                            Pair(SearchMode.WORD, if (isTamil) "அனைத்தும்" else "All Search"),
                            Pair(SearchMode.TOPIC, if (isTamil) "வேத தலைப்புகள்" else "Bible Topics"),
                            Pair(SearchMode.REFERENCE, if (isTamil) "வசனக் குறிப்பு" else "Exact Reference"),
                            Pair(SearchMode.EXACT_PHRASE, if (isTamil) "முழு தொடர்" else "Exact Phrase"),
                            Pair(SearchMode.SEMANTIC, if (isTamil) "கருப்பொருள்" else "Thematic / Semantic"),
                            Pair(SearchMode.QUESTION, if (isTamil) "வினா-விடை" else "Q&A Questions")
                        )
                        items(modes) { (m, label) ->
                            val isSel = searchMode == m
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.setSearchMode(m) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryRoyalBlue,
                                    selectedLabelColor = BrightGoldHighlight
                                )
                            )
                        }
                    }

                    // Popular query suggestions chips when input is empty
                    if (searchQuery.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTamil) "பரிந்துரைக்கப்பட்ட தேடல்கள்:" else "Popular Searches:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(popularSearchChips) { chip ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { viewModel.setSearchQuery(chip) }
                                ) {
                                    Text(
                                        text = chip,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Advanced Filters Expandable Drawer / Card
        if (showAdvancedFilters) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("advanced_filter_panel"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RichGold.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTamil) "தேடல் வடிகட்டிகள்" else "Advanced Search Filters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            TextButton(onClick = { viewModel.clearSearchFilters() }) {
                                Text(if (isTamil) "மீட்டமை" else "Reset All", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                        }

                        // Testament Filter
                        Text(text = if (isTamil) "ஏற்பாடு:" else "Testament:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = testamentFilter == null, onClick = { viewModel.setTestamentFilter(null) }, label = { Text("All", fontSize = 11.sp) })
                            FilterChip(selected = testamentFilter == "OT", onClick = { viewModel.setTestamentFilter("OT") }, label = { Text(if (isTamil) "பழைய ஏற்பாடு" else "Old Testament", fontSize = 11.sp) })
                            FilterChip(selected = testamentFilter == "NT", onClick = { viewModel.setTestamentFilter("NT") }, label = { Text(if (isTamil) "புதிய ஏற்பாடு" else "New Testament", fontSize = 11.sp) })
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Book Filter
                        Text(text = if (isTamil) "புத்தகம்:" else "Specific Book:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box {
                            OutlinedButton(
                                onClick = { showBookDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(selectedBookName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showBookDropdown,
                                onDismissRequest = { showBookDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (isTamil) "அனைத்து புத்தகங்கள்" else "All Books") },
                                    onClick = {
                                        viewModel.setBookFilter(null)
                                        showBookDropdown = false
                                    }
                                )
                                CanonicalBibleData.allBooks.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(if (isTamil) b.tamilName else b.englishName) },
                                        onClick = {
                                            viewModel.setBookFilter(b.id)
                                            showBookDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Language Toggle: English, Tamil, Both
                        Text(text = if (isTamil) "மொழி மொழிபெயர்ப்பு:" else "Translation Language:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = searchLanguage == null || searchLanguage == "en", onClick = { viewModel.setSearchLanguage("en") }, label = { Text("English (KJV)", fontSize = 11.sp) })
                            FilterChip(selected = searchLanguage == "ta", onClick = { viewModel.setSearchLanguage("ta") }, label = { Text("தமிழ் (BSI)", fontSize = 11.sp) })
                            FilterChip(selected = searchLanguage == "both", onClick = { viewModel.setSearchLanguage("both") }, label = { Text(if (isTamil) "இரு மொழிகளும்" else "Both Languages", fontSize = 11.sp) })
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sorting Order
                        Text(text = if (isTamil) "வரிசைப்படுத்துதல்:" else "Sort Order:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = sortOrder == SearchSortOrder.RELEVANCE, onClick = { viewModel.setSearchSortOrder(SearchSortOrder.RELEVANCE) }, label = { Text(if (isTamil) "பொருத்தம்" else "Relevance", fontSize = 11.sp) })
                            FilterChip(selected = sortOrder == SearchSortOrder.BIBLE_ORDER, onClick = { viewModel.setSearchSortOrder(SearchSortOrder.BIBLE_ORDER) }, label = { Text(if (isTamil) "வேதாகம வரிசை" else "Bible Order", fontSize = 11.sp) })
                            FilterChip(selected = sortOrder == SearchSortOrder.ALPHABETICAL, onClick = { viewModel.setSearchSortOrder(SearchSortOrder.ALPHABETICAL) }, label = { Text(if (isTamil) "அகரவரிசை" else "Alphabetical", fontSize = 11.sp) })
                        }
                    }
                }
            }
        }

        // 3. Result Category Tabs (All Results, Matching Topics, Verses, Q&A Answers, Chapters)
        if (searchQuery.isNotBlank()) {
            item {
                val totalTopics = categorizedResults.matchingTopics.size
                val totalVerses = categorizedResults.exactVerseMatches.size + categorizedResults.relatedVerses.size
                val hasQA = categorizedResults.biblicalAnswer != null
                val totalChapters = categorizedResults.relevantChapters.size

                val resultTabs = listOf(
                    Pair(0, if (isTamil) "அனைத்தும் (${categorizedResults.totalCount})" else "All (${categorizedResults.totalCount})"),
                    Pair(1, if (isTamil) "தலைப்புகள் ($totalTopics)" else "Topics ($totalTopics)"),
                    Pair(2, if (isTamil) "வசனங்கள் ($totalVerses)" else "Verses ($totalVerses)"),
                    Pair(3, if (isTamil) "பதில்கள்" else "Biblical Q&A"),
                    Pair(4, if (isTamil) "அதிகாரங்கள் ($totalChapters)" else "Chapters ($totalChapters)")
                )

                ScrollableTabRow(
                    selectedTabIndex = resultCategoryTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = RichGold,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    resultTabs.forEach { (idx, title) ->
                        Tab(
                            selected = resultCategoryTab == idx,
                            onClick = { resultCategoryTab = idx },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (resultCategoryTab == idx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (resultCategoryTab == idx) BrightGoldHighlight else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        }

        // 4. Loading Indicator
        if (isSearching) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RichGold)
                }
            }
        }

        // 5. Direct Reference Result Banner (e.g. John 3:16, Genesis 1:1, ரோமர் 8:28)
        if (categorizedResults.directReferenceResult is ReferenceParseResult.Success && (resultCategoryTab == 0 || resultCategoryTab == 2)) {
            val parsed = (categorizedResults.directReferenceResult as ReferenceParseResult.Success).reference
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("exact_reference_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RichGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = BrightGoldHighlight, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTamil) "நேரடி வேத வசனக் குறிப்பு" else "Direct Bible Reference Match",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGoldHighlight
                                )
                            }
                            Text(
                                text = "${if (isTamil) parsed.book.tamilName else parsed.book.englishName} ${parsed.chapter}:${parsed.verse ?: 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = RichGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Open in Bible Reader Action
                        Button(
                            onClick = {
                                viewModel.openReader(parsed.book, parsed.chapter, parsed.verse ?: 1)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue,
                                contentColor = BrightGoldHighlight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "${parsed.book.tamilName} ${parsed.chapter} அதிகாரத்தில் வாசிக்க"
                                else "Read in ${parsed.book.englishName} Chapter ${parsed.chapter}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 6. Biblical Q&A Answer (Natural Language Questions)
        if (categorizedResults.biblicalAnswer != null && (resultCategoryTab == 0 || resultCategoryTab == 3)) {
            val answer = categorizedResults.biblicalAnswer!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("biblical_qa_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrightGoldHighlight.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = RichGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "வேதாகம பதில் & வழிகாட்டுதல்" else "Biblical Guidance & Answer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isTamil) answer.answerTa else answer.answerEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Link to full topic study
                        Button(
                            onClick = { viewModel.openTopicDetail(answer.matchedTopicId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue,
                                contentColor = BrightGoldHighlight
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isTamil) "முழுமையான வேத தியானம் வாசிக்க" else "Read Complete Bible Topic Study")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 7. Matching Bible Topics Section
        if (categorizedResults.matchingTopics.isNotEmpty() && (resultCategoryTab == 0 || resultCategoryTab == 1)) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "பொருத்தமான வேத தலைப்புகள்" else "Matching Bible Topics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Text(
                        text = "${categorizedResults.matchingTopics.size} found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(categorizedResults.matchingTopics) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { viewModel.openTopicDetail(topic.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                text = if (isTamil) "${topic.titleTa} (${topic.titleEn})" else "${topic.titleEn} (${topic.titleTa})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isTamil) topic.shortDescriptionTa else topic.shortDescriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = RichGold, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // 8. Exact & Related Bible Verses Section
        if ((categorizedResults.exactVerseMatches.isNotEmpty() || categorizedResults.relatedVerses.isNotEmpty()) && (resultCategoryTab == 0 || resultCategoryTab == 2)) {
            val allVerses = categorizedResults.exactVerseMatches + categorizedResults.relatedVerses
            val pagedVerses = allVerses.take(visibleResultsLimit)

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "வேத வசனங்கள் (${allVerses.size})" else "Bible Verses (${allVerses.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Text(
                        text = if (allVerses.size > visibleResultsLimit) "Showing $visibleResultsLimit of ${allVerses.size}" else "${allVerses.size} verses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(pagedVerses) { searchItem ->
                VerseSearchResultCard(
                    item = searchItem,
                    query = searchQuery,
                    isTamil = isTamil,
                    onOpenInReader = {
                        val book = CanonicalBibleData.findBookById(searchItem.verse.bookId) ?: CanonicalBibleData.allBooks[0]
                        viewModel.openReader(book, searchItem.verse.chapterNumber, searchItem.verse.verseNumber)
                    },
                    onViewSurrounding = {
                        viewModel.loadSurroundingVerses(searchItem.verse)
                    },
                    onBookmark = { viewModel.toggleBookmark(searchItem.verse) },
                    onAddNote = {
                        noteDialogVerse = searchItem.verse
                        noteTitleInput = "${searchItem.verse.bookName} ${searchItem.verse.chapterNumber}:${searchItem.verse.verseNumber}"
                        noteContentInput = ""
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString("${searchItem.verse.verseText}\n— ${searchItem.verse.bookName} ${searchItem.verse.chapterNumber}:${searchItem.verse.verseNumber} (SVS Holy Bible)"))
                    },
                    onShare = {
                        val shareText = "${searchItem.verse.verseText}\n— ${searchItem.verse.bookName} ${searchItem.verse.chapterNumber}:${searchItem.verse.verseNumber}\n\nShared via SVS Holy Bible"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Scripture"))
                    }
                )
            }

            // Load More Button
            if (allVerses.size > visibleResultsLimit) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        OutlinedButton(
                            onClick = { visibleResultsLimit += 25 },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isTamil) "கூடுதல் முடிவுகளைக் காட்டு (+25)" else "Load More Results (+25)")
                        }
                    }
                }
            }
        }

        // 9. Relevant Chapters Section
        if (categorizedResults.relevantChapters.isNotEmpty() && (resultCategoryTab == 0 || resultCategoryTab == 4)) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = if (isTamil) "தொடர்புடைய அதிகாரங்கள்" else "Relevant Chapters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categorizedResults.relevantChapters) { (book, ch) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable { viewModel.openReader(book, ch, 1) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${if (isTamil) book.tamilName else book.englishName} $ch",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 10. Empty State when nothing found
        if (searchQuery.isNotBlank() && !isSearching && categorizedResults.totalCount == 0 && categorizedResults.directReferenceResult !is ReferenceParseResult.Success) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTamil) "'$searchQuery' க்குரிய முடிவுகள் கிடைக்கவில்லை" else "No results found for '$searchQuery'",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTamil) "சரியான எழுத்துக்களை சரிபார்க்கவும், அல்லது கீழே பரிந்துரைக்கப்பட்டவற்றைப் பார்க்கவும்:"
                            else "Check for spelling variations or try one of these suggested terms:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Suggested terms chips
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            categorizedResults.suggestedSearches.take(4).forEach { sug ->
                                TextButton(onClick = { viewModel.setSearchQuery(sug) }) {
                                    Text(sug, color = RichGold, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Verse Search Result Card with highlighted matching keyword, surrounding verses trigger, and reader action
 */
@Composable
fun VerseSearchResultCard(
    item: SearchResultItem,
    query: String,
    isTamil: Boolean,
    onOpenInReader: () -> Unit,
    onViewSurrounding: () -> Unit,
    onBookmark: () -> Unit,
    onAddNote: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val verse = item.verse

    // Build annotated string with highlighted matching keywords
    val annotatedVerseText = remember(verse.verseText, query) {
        buildAnnotatedString {
            val text = verse.verseText
            val cleanQ = query.trim().lowercase()

            if (cleanQ.isNotBlank() && text.lowercase().contains(cleanQ)) {
                var startIndex = 0
                while (startIndex < text.length) {
                    val idx = text.lowercase().indexOf(cleanQ, startIndex)
                    if (idx == -1) {
                        append(text.substring(startIndex))
                        break
                    }
                    if (idx > startIndex) {
                        append(text.substring(startIndex, idx))
                    }
                    val matchEnd = idx + cleanQ.length
                    pushStyle(SpanStyle(background = HighlightGold.copy(alpha = 0.45f), color = Color.White, fontWeight = FontWeight.Bold))
                    append(text.substring(idx, matchEnd))
                    pop()
                    startIndex = matchEnd
                }
            } else {
                append(text)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("verse_result_${verse.bookId}_${verse.chapterNumber}_${verse.verseNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Reference, Testament badge, and Translation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrimaryRoyalBlue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = item.testamentName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RichGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.languageLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Highlighted Verse Text
            Text(
                text = annotatedVerseText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Open in Bible, Surrounding Verses, Bookmark, Note, Copy, Share
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
                    modifier = Modifier.testTag("search_open_in_reader_${verse.bookId}_${verse.chapterNumber}_${verse.verseNumber}")
                ) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTamil) "வேதத்தில் வாசிக்க" else "Open in Bible", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Surrounding verses, Bookmark, Note, Copy, Share
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onViewSurrounding, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.UnfoldMore, contentDescription = "Surrounding Verses", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
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
 * Direct Scripture Navigator Modal Dialog: Select Book, Chapter, Verse -> Navigate straight to Reader!
 */
@Composable
fun DirectScriptureNavigatorDialog(
    isTamil: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (BibleBook, Int, Int) -> Unit
) {
    var selectedBookIndex by remember { mutableStateOf(0) }
    var selectedChapter by remember { mutableStateOf(1) }
    var selectedVerse by remember { mutableStateOf(1) }

    val currentBook = CanonicalBibleData.allBooks.getOrNull(selectedBookIndex) ?: CanonicalBibleData.allBooks[0]

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "வேத வழிசெலுத்தி (புத்தகம், அதிகாரம், வசனம்)" else "Direct Scripture Navigator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Select Book Dropdown/Picker
                Text(if (isTamil) "1. புத்தகம் தேர்வு செய்க:" else "1. Select Bible Book:", style = MaterialTheme.typography.labelMedium)
                var bookExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { bookExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isTamil) currentBook.tamilName else currentBook.englishName, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = bookExpanded,
                        onDismissRequest = { bookExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        CanonicalBibleData.allBooks.forEachIndexed { idx, b ->
                            DropdownMenuItem(
                                text = { Text(if (isTamil) "${b.tamilName} (${b.chapterCount} அதி)" else "${b.englishName} (${b.chapterCount} ch)") },
                                onClick = {
                                    selectedBookIndex = idx
                                    selectedChapter = 1
                                    selectedVerse = 1
                                    bookExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Select Chapter Number
                Text(if (isTamil) "2. அதிகாரம் ($selectedChapter / ${currentBook.chapterCount}):" else "2. Chapter ($selectedChapter / ${currentBook.chapterCount}):", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(currentBook.chapterCount) { i ->
                        val ch = i + 1
                        val isSel = selectedChapter == ch
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) PrimaryRoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedChapter = ch }
                        ) {
                            Text(
                                text = "$ch",
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) BrightGoldHighlight else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Go to Scripture Button
                Button(
                    onClick = {
                        onNavigate(currentBook, selectedChapter, selectedVerse)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRoyalBlue,
                        contentColor = BrightGoldHighlight
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTamil) "${currentBook.tamilName} $selectedChapter வாசிக்க"
                        else "Open ${currentBook.englishName} $selectedChapter",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
