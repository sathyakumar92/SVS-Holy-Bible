package com.example.ui.screens.topics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.BibleBook
import com.example.data.model.TopicTimelineEntry
import com.example.ui.theme.*

/**
 * Bible Topic Timeline Tab (Requirement 1.2):
 * - Passages arranged in biblical narrative order
 * - Era markers and progression (Patriarchs -> Exodus -> Kingdom -> Prophets -> Christ -> Church -> Eternity)
 * - OT to NT fulfillment connections
 * - Short narrative event explanation
 * - Direct tap action to open passage in Bible Reader
 */
@Composable
fun TopicTimelineTab(
    timelineEntries: List<TopicTimelineEntry>,
    isTamil: Boolean,
    onOpenReader: (BibleBook, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (timelineEntries.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Timeline, contentDescription = null, tint = RichGold, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isTamil) "இந்த தலைப்பிற்கான காலவரிசை தயாராகிறது..." else "Timeline entries are being prepared for this topic.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("topic_timeline_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timeline, contentDescription = null, tint = RichGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isTamil) "வேதாகம வரலாற்று காலவரிசை" else "Biblical Narrative Timeline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight
                        )
                        Text(
                            text = if (isTamil)
                                "பழைய ஏற்பாட்டு அஸ்திவாரங்கள் முதல் புதிய ஏற்பாட்டு கிறிஸ்துவில் நிறைவேற்றம் வரை (${timelineEntries.size} பகுதிகள்)"
                            else
                                "From Old Testament foundations to New Testament fulfillment in Christ (${timelineEntries.size} epochs)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryLightText
                        )
                    }
                }
            }
        }

        itemsIndexed(timelineEntries, key = { _, item -> item.id }) { index, entry ->
            TimelineEntryCard(
                entry = entry,
                index = index,
                totalCount = timelineEntries.size,
                isTamil = isTamil,
                onOpenReader = onOpenReader
            )
        }
    }
}

@Composable
private fun TimelineEntryCard(
    entry: TopicTimelineEntry,
    index: Int,
    totalCount: Int,
    isTamil: Boolean,
    onOpenReader: (BibleBook, Int, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_entry_${entry.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Era Badge & Sequence Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryRoyalBlue,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RichGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isTamil) entry.eraTa else entry.eraEn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RichGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = if (isTamil) entry.referenceTa else entry.referenceEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Event Title
            Text(
                text = if (isTamil) entry.eventTitleTa else entry.eventTitleEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Narrative Explanation
            Text(
                text = if (isTamil) entry.narrativeExplanationTa else entry.narrativeExplanationEn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Verse Text Quote Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (isTamil) entry.verseTextTa else entry.verseTextEn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }

            // OT to NT Connection if present
            if (entry.testamentsConnectionEn.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryRoyalBlue.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = RichGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTamil) entry.testamentsConnectionTa else entry.testamentsConnectionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button to Open in Reader
            Button(
                onClick = {
                    val book = CanonicalBibleData.findBookById(entry.bookId) ?: CanonicalBibleData.allBooks[0]
                    onOpenReader(book, entry.chapterNumber, entry.verseStart)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRoyalBlue,
                    contentColor = BrightGoldHighlight
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTamil) "வேதத்தில் வாசிக்க (${entry.referenceTa})" else "Read in Bible (${entry.referenceEn})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
