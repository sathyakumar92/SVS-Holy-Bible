package com.example.ui.screens.timeline

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
import com.example.data.datasource.AdvancedSeedData
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.TimelineEvent
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel

@Composable
fun TimelineScreen(
    viewModel: BibleViewModel
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var selectedPeriodId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val eventsFlow = remember(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.advancedRepo.searchTimelineEvents(searchQuery)
        } else {
            viewModel.advancedRepo.allTimelineEvents
        }
    }
    val allEvents by eventsFlow.collectAsState(initial = emptyList())

    val filteredEvents = allEvents.filter { e ->
        selectedPeriodId == null || e.periodId == selectedPeriodId
    }

    val periods = AdvancedSeedData.biblicalPeriods

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("timeline_screen")
    ) {
        // Top Header
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTamil) "வேதாகம காலவரிசை (Bible Timeline)" else "Interactive Bible Timeline",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Text(
                        text = if (isTamil) "ஆதியாகமம் முதல் அப்போஸ்தலர் காலம் வரை காலவரிசை & வரைபடம்" else "Explore biblical history chronologically with interactive maps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Map View Shortcut Button
                Button(
                    onClick = { viewModel.openBiblicalMap(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTamil) "வரைபடம்" else "Map View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isTamil) "நிகழ்வுகள், நபர்கள் அல்லது வசனங்களைத் தேடுங்கள்..." else "Search events, people, scriptures...") },
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

        // Biblical Periods Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedPeriodId == null,
                    onClick = { selectedPeriodId = null },
                    label = { Text(if (isTamil) "அனைத்து காலங்கள்" else "All Eras") }
                )
            }
            items(periods) { p ->
                FilterChip(
                    selected = selectedPeriodId == p.id,
                    onClick = { selectedPeriodId = if (selectedPeriodId == p.id) null else p.id },
                    label = { Text(if (isTamil) p.titleTa else p.titleEn) }
                )
            }
        }

        // Timeline Events List
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredEvents) { event ->
                TimelineEventCard(
                    event = event,
                    isTamil = isTamil,
                    onClick = { viewModel.openTimelineDetail(event.id) },
                    onOpenBible = {
                        val book = CanonicalBibleData.findBookById(event.bookId) ?: CanonicalBibleData.allBooks[0]
                        viewModel.openReader(book, event.chapter, event.verse)
                    },
                    onOpenMap = {
                        viewModel.openBiblicalMap(event.linkedLocationId)
                    }
                )
            }
        }
    }
}

@Composable
fun TimelineEventCard(
    event: TimelineEvent,
    isTamil: Boolean,
    onClick: () -> Unit,
    onOpenBible: () -> Unit,
    onOpenMap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("timeline_event_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryRoyalBlue
                ) {
                    Text(
                        text = event.approximateDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrightGoldHighlight,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row {
                    if (event.linkedLocationId != null) {
                        IconButton(onClick = onOpenMap, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Place, contentDescription = "Map Location", tint = RichGold, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onOpenBible, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Read Scripture", tint = BrightGoldHighlight, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isTamil) event.titleTa else event.titleEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrightGoldHighlight
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isTamil) event.summaryTa else event.summaryEn,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryLightText,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${event.primaryScriptureRef}",
                    style = MaterialTheme.typography.labelSmall,
                    color = RichGold,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isTamil) "முழு விபரம் பார்க்க →" else "View Details →",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryRoyalBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
