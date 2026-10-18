package com.example.ui.screens.topics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CompleteBibleTopicsData
import com.example.data.model.TopicEntity
import com.example.ui.theme.*

/**
 * Related Topic Discovery Tab (Requirement 2.2):
 * - Displays curated related topics ranked by relevance
 * - Clean clickable cards with titles, category, description, and references count
 * - Direct navigation to explore related topic
 */
@Composable
fun TopicRelatedDiscoveryTab(
    currentTopic: TopicEntity,
    relatedTopics: List<TopicEntity>,
    isTamil: Boolean,
    onOpenTopic: (String) -> Unit,
    canNavigateBackToPreviousTopic: Boolean = false,
    onNavigateToPreviousTopic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("topic_related_discovery_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (canNavigateBackToPreviousTopic) {
            item {
                OutlinedButton(
                    onClick = onNavigateToPreviousTopic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("back_to_previous_topic_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RichGold.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = BrightGoldHighlight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTamil) "← முந்தைய தலைப்பிற்கு திரும்பு" else "← Return to Previous Topic",
                        color = BrightGoldHighlight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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
                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = RichGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isTamil) "தொடர்புடைய வேத தலைப்புகள்" else "Related Biblical Topics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight
                        )
                        Text(
                            text = if (isTamil)
                                "${currentTopic.titleTa} தலைப்போடு நெருங்கிய தொடர்புடைய வேத சத்தியங்கள் (${relatedTopics.size} தலைப்புகள்)"
                            else
                                "Biblical doctrines and spiritual themes connected to ${currentTopic.titleEn} (${relatedTopics.size} topics)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryLightText
                        )
                    }
                }
            }
        }

        if (relatedTopics.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isTamil) "தொடர்புடைய தலைப்புகள் எதுவும் காணப்படவில்லை." else "No related topics found for this section.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(relatedTopics, key = { it.id }) { rel ->
                val refCount = CompleteBibleTopicsData.getCuratedReferencesForTopic(rel.id).size
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTopic(rel.id) }
                        .testTag("related_topic_card_${rel.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Category & References Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PrimaryRoyalBlue.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = if (isTamil) rel.categoryTa else rel.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrightGoldHighlight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "$refCount ${if (isTamil) "வசனங்கள்" else "verses"}",
                                    fontSize = 10.sp,
                                    color = RichGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Titles
                            Text(
                                text = if (isTamil) rel.titleTa else rel.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isTamil) rel.titleEn else rel.titleTa,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Description
                            Text(
                                text = if (isTamil) rel.shortDescriptionTa else rel.shortDescriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Topic",
                            tint = RichGold
                        )
                    }
                }
            }
        }
    }
}
