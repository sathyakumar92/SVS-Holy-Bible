package com.example.ui.screens.topics

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.CompleteBibleTopicsData
import com.example.data.model.BibleVerse
import com.example.data.model.TopicEntity
import com.example.data.model.TopicReferenceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel
import com.example.util.AdvancedSearchEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.bibleLanguage == "ta"

    val allTopics by viewModel.allBibleTopics.collectAsState()
    val favoriteIds by viewModel.favoriteTopicIds.collectAsState()
    val recentViewed by viewModel.recentlyViewedTopics.collectAsState()
    val recentSearchKeywords by viewModel.recentTopicSearchKeywords.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedTestament by remember { mutableStateOf("ALL") } // "ALL", "OT", "NT"
    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Featured, 2: Popular, 3: Favorites, 4: History
    var selectedLetter by remember { mutableStateOf<String?>(null) }
    var showAdminDialog by remember { mutableStateOf(false) }

    // Filter topics
    val filteredTopics = remember(allTopics, searchQuery, selectedCategory, selectedTestament, selectedTab, selectedLetter, favoriteIds, recentViewed) {
        var list = allTopics

        // Tab filter
        when (selectedTab) {
            1 -> list = list.filter { it.isFeatured }
            2 -> list = list.filter { it.isPopular }
            3 -> list = list.filter { favoriteIds.contains(it.id) }
            4 -> {
                val recentMap = recentViewed.map { it.id }.toSet()
                list = list.filter { recentMap.contains(it.id) }
            }
        }

        // Category filter
        if (selectedCategory != "All") {
            list = list.filter { it.category == selectedCategory || it.categoryTa == selectedCategory }
        }

        // Testament filter
        if (selectedTestament != "ALL") {
            list = list.filter { it.testamentFocus == "ALL" || it.testamentFocus == selectedTestament }
        }

        // Letter filter
        if (selectedLetter != null) {
            val let = selectedLetter!!.lowercase()
            list = list.filter {
                it.titleEn.lowercase().startsWith(let) || it.titleTa.startsWith(selectedLetter!!)
            }
        }

        // Search Query filter
        if (searchQuery.isNotBlank()) {
            val qNorm = AdvancedSearchEngine.normalize(searchQuery)
            list = list.filter { t ->
                val titleEn = AdvancedSearchEngine.normalize(t.titleEn)
                val titleTa = AdvancedSearchEngine.normalize(t.titleTa)
                val descEn = AdvancedSearchEngine.normalize(t.shortDescriptionEn)
                val descTa = AdvancedSearchEngine.normalize(t.shortDescriptionTa)
                val keys = AdvancedSearchEngine.normalize(t.keywords)
                val syns = AdvancedSearchEngine.normalize(t.synonyms)

                titleEn.contains(qNorm) || titleTa.contains(qNorm) ||
                descEn.contains(qNorm) || descTa.contains(qNorm) ||
                keys.contains(qNorm) || syns.contains(qNorm) ||
                qNorm.contains(titleEn) || qNorm.contains(titleTa)
            }
        }

        list
    }

    // Admin Creation Modal Dialog
    if (showAdminDialog) {
        AdminTopicEditorDialog(
            isTamil = isTamil,
            onDismiss = { showAdminDialog = false },
            onSaveTopic = { newTopic ->
                viewModel.saveCustomTopic(newTopic)
                showAdminDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("topics_screen_root"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Header Banner & Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(PrimaryRoyalBlue.copy(alpha = 0.35f), Color.Transparent)
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
                                text = if (isTamil) "வேதாகம தலைப்புகள்" else "Bible Topics & Doctrines",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Text(
                                text = if (isTamil) "34+ கருப்பொருள் வேத தியானங்கள் & வசன ஆதாரங்கள்" else "Curated Scripture studies across 34+ biblical themes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Admin Management Button
                        OutlinedButton(
                            onClick = { showAdminDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BrightGoldHighlight
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RichGold.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("admin_manage_topics_button")
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTamil) "+ தலைப்பு சேர்க்க" else "+ Add Topic",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prominent Topic Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (it.length >= 2) viewModel.recordTopicSearch(it)
                        },
                        placeholder = {
                            Text(
                                text = if (isTamil) "தலைப்பு அல்லது முக்கிய சொல்லைத் தேடுக (எ.கா. விசுவாசம், ஜெபம்)..."
                                else "Search Bible topics (e.g. Faith, Prayer, Love, Fear)...",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = RichGold)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RichGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topic_search_bar")
                    )

                    // Recent search keywords (if available)
                    if (recentSearchKeywords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isTamil) "சமீபத்திய தேடல்கள்:" else "Recent:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(recentSearchKeywords.take(5)) { keyword ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { searchQuery = keyword }
                                    ) {
                                        Text(
                                            text = keyword,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Main Filter Tabs (All, Featured, Popular, Favorites, History)
        item {
            val tabs = if (isTamil) {
                listOf("அனைத்தும்", "முக்கியமானவை", "பிரபலமானவை", "விருப்பமானவை", "சமீபத்தில் பார்த்தவை")
            } else {
                listOf("All Topics", "Featured", "Popular", "Saved & Favorites", "Recently Viewed")
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = RichGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("topics_tab_row")
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) BrightGoldHighlight else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // 3. Testament & Category Filter Chips Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Testament Scope Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    FilterChip(
                        selected = selectedTestament == "ALL",
                        onClick = { selectedTestament = "ALL" },
                        label = { Text(if (isTamil) "முழு வேதாகமம்" else "All Testaments", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedTestament == "OT",
                        onClick = { selectedTestament = "OT" },
                        label = { Text(if (isTamil) "பழைய ஏற்பாடு" else "Old Testament", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedTestament == "NT",
                        onClick = { selectedTestament = "NT" },
                        label = { Text(if (isTamil) "புதிய ஏற்பாடு" else "New Testament", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Categories Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CompleteBibleTopicsData.categories) { (catEn, catTa) ->
                        val isSelected = (selectedCategory == catEn || selectedCategory == catTa)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = if (isSelected) "All" else catEn
                            },
                            label = {
                                Text(
                                    text = if (isTamil) catTa else catEn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryRoyalBlue,
                                selectedLabelColor = BrightGoldHighlight
                            )
                        )
                    }
                }

                // Alphabetical A-Z Picker
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedLetter == null) RichGold else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedLetter = null }
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = if (isTamil) "அ-Z" else "A-Z",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedLetter == null) Color.Black else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    val letters = listOf("A","B","C","D","E","F","G","H","J","L","M","N","O","P","R","S","T","W")
                    items(letters) { let ->
                        val isSel = selectedLetter == let
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) PrimaryRoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedLetter = if (isSel) null else let }
                                .padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = let,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) BrightGoldHighlight else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Topic Count & Clear Filter Indicator
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTopics.size} ${if (isTamil) "தலைப்புகள் கிடைக்கின்றன" else "Topics available"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (selectedCategory != "All" || selectedTestament != "ALL" || selectedLetter != null || searchQuery.isNotBlank()) {
                    TextButton(
                        onClick = {
                            selectedCategory = "All"
                            selectedTestament = "ALL"
                            selectedLetter = null
                            searchQuery = ""
                        }
                    ) {
                        Text(if (isTamil) "வடிகட்டிகளை நீக்கு" else "Clear Filters", fontSize = 12.sp, color = RichGold)
                    }
                }
            }
        }

        // 5. Empty State
        if (filteredTopics.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTamil) "தேடலுக்குரிய தலைப்புகள் கிடைக்கவில்லை" else "No topics match your filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTamil) "வேறு முக்கிய சொற்களை முயற்சிக்கவும் அல்லது அனைத்து வடிகட்டிகளையும் நீக்கவும்"
                            else "Try searching for a different keyword or reset filters to view all 34+ topics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 6. Topics List Items
        items(filteredTopics, key = { it.id }) { topic ->
            val isFav = favoriteIds.contains(topic.id)
            TopicMasterCard(
                topic = topic,
                isTamil = isTamil,
                isFavorite = isFav,
                onFavoriteToggle = { viewModel.toggleFavoriteTopic(topic.id) },
                onTopicClick = { viewModel.openTopicDetail(topic.id) }
            )
        }
    }
}

/**
 * Topic Master Card displayed in the Topic Home Screen
 */
@Composable
fun TopicMasterCard(
    topic: TopicEntity,
    isTamil: Boolean,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onTopicClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onTopicClick() }
            .testTag("topic_card_${topic.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (topic.isFeatured) RichGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Category icon pill
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PrimaryRoyalBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                topic.id == "prayer" -> Icons.Default.Favorite
                                topic.id == "faith" -> Icons.Default.Shield
                                topic.id == "love" -> Icons.Default.FavoriteBorder
                                topic.id == "holy_spirit" -> Icons.Default.AutoAwesome
                                topic.id == "wisdom" -> Icons.Default.Lightbulb
                                topic.id == "peace" -> Icons.Default.Water
                                topic.id == "worship" -> Icons.Default.MusicNote
                                topic.id == "family_marriage" -> Icons.Default.People
                                else -> Icons.Default.Bookmark
                            },
                            contentDescription = null,
                            tint = BrightGoldHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isTamil) topic.titleTa else topic.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (topic.isFeatured) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RichGold.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = if (isTamil) "சிறப்பு" else "Featured",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrightGoldHighlight,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Subtitle in secondary language
                        Text(
                            text = if (isTamil) topic.titleEn else topic.titleTa,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Favorite action
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Topic",
                        tint = if (isFavorite) RichGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Short Description
            Text(
                text = if (isTamil) topic.shortDescriptionTa else topic.shortDescriptionEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isTamil) topic.categoryTa else topic.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isTamil) "வேத தியானம் காண்க" else "View Scripture Study",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = RichGold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = RichGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog for Administrators / Leaders to add or edit topics without changing application code
 */
@Composable
fun AdminTopicEditorDialog(
    isTamil: Boolean,
    onDismiss: () -> Unit,
    onSaveTopic: (TopicEntity) -> Unit
) {
    var titleEn by remember { mutableStateOf("") }
    var titleTa by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Spiritual Life") }
    var categoryTa by remember { mutableStateOf("ஆவிக்குரிய வாழ்க்கை") }
    var shortDescEn by remember { mutableStateOf("") }
    var shortDescTa by remember { mutableStateOf("") }
    var introEn by remember { mutableStateOf("") }
    var introTa by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var isFeatured by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
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
                        text = if (isTamil) "புதிய வேத தலைப்பு சேர்க்க (Admin)" else "Add Bible Topic (Admin)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Text(
                        text = if (isTamil) "கோட் மாற்றமின்றி புதிய தலைப்புகளையும் விளக்கங்களையும் உருவாக்கவும்."
                        else "Create custom topics with full explanations without code changes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    OutlinedTextField(
                        value = titleEn,
                        onValueChange = { titleEn = it },
                        label = { Text("Topic Title (English)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = titleTa,
                        onValueChange = { titleTa = it },
                        label = { Text("தலைப்பு (தமிழ்)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (English)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = shortDescEn,
                        onValueChange = { shortDescEn = it },
                        label = { Text("Short Description (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = shortDescTa,
                        onValueChange = { shortDescTa = it },
                        label = { Text("சுருக்க விளக்கம் (தமிழ்)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = introEn,
                        onValueChange = { introEn = it },
                        label = { Text("Theological Intro & Meaning (English)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keywords,
                        onValueChange = { keywords = it },
                        label = { Text("Keywords & Synonyms (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTamil) "முக்கிய தலைப்பாகக் காட்டுக" else "Mark as Featured Topic", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                if (titleEn.isNotBlank()) {
                                    val id = "custom_" + titleEn.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                    val newEntity = TopicEntity(
                                        id = id,
                                        slug = id,
                                        titleEn = titleEn,
                                        titleTa = titleTa.ifBlank { titleEn },
                                        shortDescriptionEn = shortDescEn,
                                        shortDescriptionTa = shortDescTa.ifBlank { shortDescEn },
                                        category = category,
                                        categoryTa = categoryTa,
                                        keywords = keywords,
                                        introductionEn = introEn,
                                        introductionTa = introTa.ifBlank { introEn },
                                        status = "PUBLISHED",
                                        isFeatured = isFeatured,
                                        isCustom = true
                                    )
                                    onSaveTopic(newEntity)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue,
                                contentColor = BrightGoldHighlight
                            ),
                            enabled = titleEn.isNotBlank()
                        ) {
                            Text(if (isTamil) "சேமி" else "Save Topic")
                        }
                    }
                }
            }
        }
    }
}
