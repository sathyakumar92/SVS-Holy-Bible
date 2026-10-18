package com.example.ui.screens.prayer

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
import com.example.data.model.PrayerEntry
import com.example.data.model.PrayerStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@Composable
fun PrayerJournalScreen(
    viewModel: BibleViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var selectedStatusTab by remember { mutableIntStateOf(0) } // 0: All, 1: Active, 2: Answered, 3: Archived
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val prayersFlow = remember(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.advancedRepo.searchPrayers(searchQuery)
        } else {
            viewModel.advancedRepo.allPrayers
        }
    }
    val allPrayers by prayersFlow.collectAsState(initial = emptyList())

    val filteredPrayers = allPrayers.filter { p ->
        val statusMatches = when (selectedStatusTab) {
            1 -> p.status == PrayerStatus.ACTIVE || p.status == PrayerStatus.ONGOING
            2 -> p.status == PrayerStatus.ANSWERED
            3 -> p.status == PrayerStatus.ARCHIVED
            else -> true
        }
        val categoryMatches = selectedCategory == null || p.category == selectedCategory
        statusMatches && categoryMatches
    }

    val categories = listOf("Personal", "Family", "Health", "Work & Provision", "Ministry", "Gratitude")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openPrayerEditor(null) },
                containerColor = RichGold,
                contentColor = DeepNavyBackground,
                modifier = Modifier.testTag("create_prayer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Prayer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("prayer_journal_screen")
        ) {
            // Header & Search
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTamil) "தனிப்பட்ட ஜெபக் குறிப்பேடு (Prayer Journal)" else "Personal Prayer Journal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                Text(
                    text = if (isTamil) "விண்ணப்பங்கள், பதில் பெற்ற ஜெப சாட்சிகள் & தொடர் ஜெபங்கள்" else "Track ongoing prayer petitions, answered praise testimonies & updates",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isTamil) "ஜெபங்களைத் தேடுங்கள்..." else "Search prayer petitions...") },
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

            // Status Tab Row
            TabRow(
                selectedTabIndex = selectedStatusTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = RichGold
            ) {
                listOf(
                    (if (isTamil) "அனைத்தும்" else "All"),
                    (if (isTamil) "செயலில்" else "Active"),
                    (if (isTamil) "பதில் பெற்றது" else "Answered"),
                    (if (isTamil) "காப்பகம்" else "Archived")
                ).forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedStatusTab == idx,
                        onClick = { selectedStatusTab = idx },
                        text = { Text(label, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text(if (isTamil) "அனைத்து பிரிவுகள்" else "All Categories") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat) }
                    )
                }
            }

            // Prayers List
            if (filteredPrayers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = RichGold,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTamil) "ஜெபப் பதிவுகள் எதுவும் இல்லை" else "No prayer petitions found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTamil) "உங்கள் விண்ணப்பங்களை எழுதி தேவ சமூகத்தில் சமர்ப்பியுங்கள்." else "Bring your requests with thanksgiving unto God's throne of grace.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.openPrayerEditor(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTamil) "புதிய ஜெபத்தை சேர்க்க" else "Add Prayer Request")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPrayers) { prayer ->
                        PrayerCard(
                            prayer = prayer,
                            isTamil = isTamil,
                            onClick = { viewModel.openPrayerEditor(prayer.id) },
                            onDelete = {
                                coroutineScope.launch {
                                    viewModel.advancedRepo.deletePrayer(prayer.id)
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
fun PrayerCard(
    prayer: PrayerEntry,
    isTamil: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isAnswered = prayer.status == PrayerStatus.ANSWERED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("prayer_item_${prayer.id}"),
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
                Text(
                    text = prayer.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAnswered) BrightGoldHighlight else PrimaryLightText
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (prayer.status) {
                            PrayerStatus.ANSWERED -> SuccessGreen.copy(alpha = 0.25f)
                            PrayerStatus.ACTIVE -> PrimaryRoyalBlue.copy(alpha = 0.35f)
                            PrayerStatus.ONGOING -> WarningGold.copy(alpha = 0.25f)
                            PrayerStatus.ARCHIVED -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = when (prayer.status) {
                                PrayerStatus.ANSWERED -> if (isTamil) "பதில் பெற்றது" else "Answered"
                                PrayerStatus.ACTIVE -> if (isTamil) "செயலில்" else "Active"
                                PrayerStatus.ONGOING -> if (isTamil) "தொடர் ஜெபம்" else "Ongoing"
                                PrayerStatus.ARCHIVED -> if (isTamil) "காப்பகம்" else "Archived"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (prayer.status) {
                                PrayerStatus.ANSWERED -> SuccessGreen
                                PrayerStatus.ACTIVE -> BrightGoldHighlight
                                PrayerStatus.ONGOING -> WarningGold
                                PrayerStatus.ARCHIVED -> SecondaryLightText
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = prayer.description,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryLightText,
                maxLines = 3
            )

            if (isAnswered && prayer.answeredTestimony.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTamil) "பதில் பெற்ற சாட்சி (Praise Testimony):" else "Praise Testimony:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                        Text(
                            text = prayer.answeredTestimony,
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryLightText
                        )
                    }
                }
            }

            if (prayer.attachedScriptureRef.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = RichGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = prayer.attachedScriptureRef,
                        style = MaterialTheme.typography.labelSmall,
                        color = RichGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
