package com.example.ui.screens.topics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.RichTopicStudy
import com.example.ui.theme.*

/**
 * Bible Topic Deep Study Tab (Requirement 1.1):
 * - Introduction and Theological Teachings
 * - Authentic Hebrew and Greek Word Studies (Meanings & Context)
 * - Historical & Biblical Background
 * - Key Principles & Spiritual Teachings
 * - Associated Biblical Characters
 * - Key Lessons and Practical Everyday Applications
 * - Clear distinction between Scripture and Devotional Commentary
 */
@Composable
fun TopicDeepStudyTab(
    study: RichTopicStudy,
    isTamil: Boolean,
    modifier: Modifier = Modifier
) {
    val topic = study.topic

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("topic_deep_study_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Biblical Introduction & Significance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTamil) "வேதாகம அறிமுகமும் முக்கியத்துவமும்" else "Biblical Introduction & Significance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isTamil) topic.introductionTa.ifBlank { topic.shortDescriptionTa }
                        else topic.introductionEn.ifBlank { topic.shortDescriptionEn },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // 2. Original Hebrew & Greek Biblical Word Studies
        if (study.wordStudies.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "மூல மொழி வார்த்தை ஆய்வு (எபிரெயர் & கிரேக்கம்)" else "Original Biblical Languages (Hebrew & Greek)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        study.wordStudies.forEachIndexed { index, word ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${word.originalWord} (${word.transliteration})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RichGold
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PrimaryRoyalBlue.copy(alpha = 0.3f)
                                        ) {
                                            Text(
                                                text = word.language,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BrightGoldHighlight,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "${if (isTamil) "நேரடிப் பொருள்: " else "Literal Meaning: "}${if (isTamil) word.literalMeaningTa else word.literalMeaningEn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = if (isTamil) word.theologicalSignificanceTa else word.theologicalSignificanceEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "📖 ${if (isTamil) word.primaryScriptureTa else word.primaryScriptureEn}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = RichGold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Theological Explanation & Core Principles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTamil) "வேத இறையியல் போதனைகளும் கொள்கைகளும்" else "Theological Teachings & Spiritual Principles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isTamil) topic.detailedExplanationTa.ifBlank { topic.introductionTa }
                        else topic.detailedExplanationEn.ifBlank { topic.introductionEn },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // 4. Biblical Characters Associated
        if (topic.biblicalPeopleEn.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "மாதிரியாக நின்ற வேத கதாபாத்திரங்கள்" else "Key Biblical Characters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isTamil) topic.biblicalPeopleTa.ifBlank { topic.biblicalPeopleEn } else topic.biblicalPeopleEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // 5. Practical Everyday Applications
        if (topic.practicalApplicationsEn.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryRoyalBlue.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryRoyalBlue.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = BrightGoldHighlight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "அன்றாட வாழ்வில் நடைமுறைப்படுத்த" else "Practical Everyday Applications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isTamil) topic.practicalApplicationsTa.ifBlank { topic.practicalApplicationsEn } else topic.practicalApplicationsEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        // 6. Key Lessons & Spiritual Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTamil) "ஆவிக்குரிய முக்கிய பாடங்களும் சுருக்கமும்" else "Summary of Main Teachings & Lessons",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isTamil) topic.keyLessonsTa.ifBlank { topic.keyLessonsEn } else topic.keyLessonsEn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isTamil)
                                "⚠️ குறிப்பு: மேலுள்ள விளக்கங்கள் வேதாகம சத்தியங்களை தெளிவாகப் புரிந்துகொள்ள அளிக்கப்பட்ட கல்விப் பகுதியாகும். எப்போதும் மூல வேத வசனங்களோடு ஒப்பிட்டுப் படியுங்கள்."
                            else
                                "⚠️ Notice: The explanations above provide structured biblical and scholarly context. Always compare all teachings directly with the inspired canonical Scripture.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}
