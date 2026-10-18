package com.example.ui.screens.topics

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.BibleBook
import com.example.data.model.BibleVerse
import com.example.data.model.TopicReferenceEntity
import com.example.ui.theme.*

/**
 * Bible Topic Verse Comparison Tab (Requirement 1.3):
 * - Select 2 or more passages for parallel examination
 * - Multilingual translation comparison: King James Version (KJV) & Tamil BSI Union Version
 * - Common themes and complementary teachings analysis
 * - Contextual surrounding verses toggle
 * - Direct navigation to Bible reader, copy & share actions
 */
@Composable
fun TopicVerseComparisonTab(
    allReferences: List<TopicReferenceEntity>,
    selectedVerses: List<TopicReferenceEntity>,
    isTamil: Boolean,
    onToggleVerse: (TopicReferenceEntity) -> Unit,
    onClearVerses: () -> Unit,
    onOpenReader: (BibleBook, Int, Int) -> Unit,
    onViewSurrounding: (BibleVerse) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showTranslationsParallel by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("topic_verse_comparison_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Introduction & Selector Header
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Compare, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "வேத வசன ஒப்பீடு" else "Compare Bible Verses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }

                        if (selectedVerses.isNotEmpty()) {
                            TextButton(onClick = onClearVerses) {
                                Text(if (isTamil) "அழி" else "Clear", color = RichGold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isTamil)
                            "ஒப்பிடுவதற்கு கீழே உள்ள வசனங்களில் 2 முதல் 4 வசனங்களைத் தேர்ந்தெடுக்கவும் (தேர்வு: ${selectedVerses.size}/4)."
                        else
                            "Select 2 to 4 passages from this topic below to compare side-by-side (Selected: ${selectedVerses.size}/4).",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryLightText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Selectable Verse Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allReferences) { ref ->
                            val isSelected = selectedVerses.any { it.id == ref.id || it.referenceEn == ref.referenceEn }
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggleVerse(ref) },
                                label = {
                                    Text(
                                        text = if (isTamil) ref.referenceTa else ref.referenceEn,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryRoyalBlue,
                                    selectedLabelColor = BrightGoldHighlight
                                )
                            )
                        }
                    }
                }
            }
        }

        // Comparison View Controls
        if (selectedVerses.size >= 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "தேர்ந்தெடுக்கப்பட்ட வசனங்கள் (${selectedVerses.size})" else "Selected Passages (${selectedVerses.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isTamil) "இரு மொழி ஒப்பீடு" else "Dual Translations",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = showTranslationsParallel,
                            onCheckedChange = { showTranslationsParallel = it },
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }
            }

            // Synthesis / Common Themes Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RichGold.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "ஒப்பீட்டு ஆய்வு & ஆவிக்குரிய ஒருங்கிணைப்பு" else "Comparative Synthesis & Common Themes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val synthesisEn = buildString {
                            append("• Common Foundation: All selected passages (${selectedVerses.joinToString(", ") { it.referenceEn }}) declare God's unwavering character and covenant faithfulness.\n\n")
                            append("• Complementary Perspectives: ")
                            val otCount = selectedVerses.count { CanonicalBibleData.findBookById(it.bookId)?.testament == com.example.data.model.Testament.OLD_TESTAMENT }
                            val ntCount = selectedVerses.size - otCount
                            if (otCount > 0 && ntCount > 0) {
                                append("Combines Old Testament prophetic and covenant groundwork ($otCount passages) with New Testament apostolic fulfillment in Jesus Christ ($ntCount passages).")
                            } else {
                                append("Reveals multifaceted dimensions of the doctrine across varied biblical historical settings.")
                            }
                        }

                        val synthesisTa = buildString {
                            append("• பொதுவான அஸ்திபாரம்: தேர்ந்தெடுக்கப்பட்ட அனைத்து பகுதிகளும் (${selectedVerses.joinToString(", ") { it.referenceTa }}) தேவனுடைய மாறாத இரக்கத்தையும் சத்தியத்தையும் வெளிப்படுத்துகின்றன.\n\n")
                            append("• உடன்படிக்கையின் ஒருமைப்பாடு: பழைய ஏற்பாட்டு அஸ்திவாரமும் புதிய ஏற்பாட்டு கிறிஸ்துவில் நிறைவேற்றமும் இணைந்து முழுமையான சத்தியத்தை போதிக்கின்றன.")
                        }

                        Text(
                            text = if (isTamil) synthesisTa else synthesisEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Compared Verse Cards
            items(selectedVerses, key = { it.id }) { ref ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Title & Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTamil) ref.referenceTa else ref.referenceEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
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
                                        onViewSurrounding(fakeVerse)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.UnfoldMore, contentDescription = "Surrounding Context", tint = RichGold, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = {
                                        val text = "${ref.referenceEn} (KJV): ${ref.verseTextEn}\n${ref.referenceTa} (Tamil): ${ref.verseTextTa}"
                                        clipboardManager.setText(AnnotatedString(text))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = {
                                        val text = "${ref.referenceEn} (KJV):\n${ref.verseTextEn}\n\n${ref.referenceTa} (Tamil BSI):\n${ref.verseTextTa}\n\nShared via SVS Holy Bible"
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, text)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Scripture"))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Translation 1: Authorized King James Version (KJV)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Authorized King James Version (KJV)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RichGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ref.verseTextEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // Translation 2: Tamil Bible (BSI / Union Version)
                        if (showTranslationsParallel) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryRoyalBlue.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "பரிசுத்த வேதாகமம் (Tamil Bible / BSI Union Version)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrightGoldHighlight
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ref.verseTextTa,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Brief contextual commentary
                        if (ref.briefExplanationEn.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 ${if (isTamil) ref.briefExplanationTa.ifBlank { ref.briefExplanationEn } else ref.briefExplanationEn}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Direct Reader Button
                        Button(
                            onClick = {
                                val book = CanonicalBibleData.findBookById(ref.bookId) ?: CanonicalBibleData.allBooks[0]
                                onOpenReader(book, ref.chapterNumber, ref.verseStart)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue,
                                contentColor = BrightGoldHighlight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTamil) "வாசிக்க" else "Read in Context", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Empty state instruction
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = RichGold.copy(alpha = 0.7f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTamil)
                                "ஒப்பீட்டைப் பார்க்க மேலே உள்ள வசனங்களில் குறைந்தபட்சம் 2 வசனங்களைத் தேர்ந்தெடுக்கவும்."
                            else
                                "Please select at least 2 passages from the chips above to view detailed comparative analysis.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
