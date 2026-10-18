package com.example.ui.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.datasource.AdvancedSeedData
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.TimelineEvent
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineDetailScreen(
    viewModel: BibleViewModel
) {
    val eventId by viewModel.selectedTimelineEventId.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var event by remember { mutableStateOf<TimelineEvent?>(null) }

    LaunchedEffect(eventId) {
        if (eventId != null) {
            event = viewModel.advancedRepo.getTimelineEventById(eventId!!)
                ?: AdvancedSeedData.timelineEvents.firstOrNull { it.id == eventId }
        }
    }

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Event details not found")
        }
        return
    }

    val ev = event!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("timeline_detail_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (isTamil) ev.titleTa else ev.titleEn,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ElevatedBlueSurface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Era & Date Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (isTamil) "தோராயமான காலம்:" else "Approximate Era:"} ${ev.approximateDate}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }

                        if (ev.dateUncertaintyNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ⓘ ${ev.dateUncertaintyNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryLightText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isTamil) "சுருக்கம் (Event Summary)" else "Event Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTamil) ev.summaryTa else ev.summaryEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            // Historical & Theological Context
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isTamil) "வரலாற்று மற்றும் ஆவிக்குரிய முக்கியத்துவம்" else "Historical & Theological Significance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTamil) ev.historicalContextTa else ev.historicalContextEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            // Key Figures Involved
            if (ev.keyFigures.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isTamil) "முக்கிய நபர்கள்" else "Key Figures Involved",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ev.keyFigures,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Actions: Open Bible Passage & Open Map
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val book = CanonicalBibleData.findBookById(ev.bookId) ?: CanonicalBibleData.allBooks[0]
                            viewModel.openReader(book, ev.chapter, ev.verse)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTamil) "வேதத்தில் வாசிக்க" else "Read Passage", fontWeight = FontWeight.Bold)
                    }

                    if (ev.linkedLocationId != null) {
                        Button(
                            onClick = {
                                viewModel.openBiblicalMap(ev.linkedLocationId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RichGold, contentColor = DeepNavyBackground),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTamil) "வரைபடத்தில் காண்க" else "View on Map", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
