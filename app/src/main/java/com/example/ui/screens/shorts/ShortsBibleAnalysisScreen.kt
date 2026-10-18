package com.example.ui.screens.shorts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.ShortBibleReferenceEntity
import com.example.data.model.ShortWithDetails
import com.example.ui.theme.*

/**
 * Intelligent Bible Analysis Result Screen (Requirement 8.5):
 * - Displays video summary and spoken transcript
 * - Main biblical topics, characters, spiritual principles
 * - Grounded Scripture passages with English KJV and Tamil BSI verse texts
 * - Clear distinction badges: Exact Quotation, Biblical Incident, Supporting Scripture
 * - Direct Navigation: "Open in Bible" button opens reader directly at that exact chapter and verse
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsBibleAnalysisScreen(
    shortDetails: ShortWithDetails?,
    isTamil: Boolean,
    onBack: () -> Unit,
    onOpenBibleReader: (bookId: String, chapter: Int, verse: Int) -> Unit
) {
    BackHandler {
        onBack()
    }

    if (shortDetails == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepNavyBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RichGold)
        }
        return
    }

    val short = shortDetails.short
    val analysis = shortDetails.analysis
    val references = shortDetails.references

    var showTranscript by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = RichGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTamil) "வேத நுண்ணறிவு ஆய்வு" else "Intelligent Bible Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }
                        Text(
                            text = short.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryLightText.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("analysis_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SecondaryNavySurface,
                    scrolledContainerColor = SecondaryNavySurface
                )
            )
        },
        containerColor = DeepNavyBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("shorts_bible_analysis_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // A. Video Information Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                color = PrimaryRoyalBlue.copy(alpha = 0.35f)
                            ) {
                                Text(
                                    text = short.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGoldHighlight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = RichGold.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = RichGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${references.size} ${if (isTamil) "வேத ஆதாரங்கள்" else "verified scriptures"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrightGoldHighlight
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = short.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight,
                            lineHeight = 26.sp
                        )

                        if (analysis != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isTamil) analysis.summaryTa.ifBlank { analysis.summaryEn } else analysis.summaryEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryLightText,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // B. Main Biblical Topics & Characters Card
            if (analysis != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Main Topic
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Label, contentDescription = null, tint = RichGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTamil) "முக்கிய வேத தலைப்பு" else "Main Biblical Topic",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RichGold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isTamil) analysis.mainTopicTa.ifBlank { analysis.mainTopicEn } else analysis.mainTopicEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Biblical Characters
                            if (analysis.biblicalCharactersEn.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = RichGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isTamil) "வேத கதாபாத்திரங்கள்" else "Biblical Characters",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RichGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isTamil) analysis.biblicalCharactersTa.ifBlank { analysis.biblicalCharactersEn } else analysis.biblicalCharactersEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Spiritual Principles
                            if (analysis.spiritualTeachingsEn.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = RichGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isTamil) "ஆவிக்குரிய போதனைகள்" else "Spiritual Principles",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RichGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isTamil) analysis.spiritualTeachingsTa.ifBlank { analysis.spiritualTeachingsEn } else analysis.spiritualTeachingsEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                // Spoken Transcript Expandable Card
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
                                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isTamil) "வீடியோவில் பேசப்பட்ட உரை (Transcript)" else "Spoken Video Transcript",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BrightGoldHighlight
                                    )
                                }

                                TextButton(onClick = { showTranscript = !showTranscript }) {
                                    Text(
                                        text = if (showTranscript) (if (isTamil) "மறை" else "Hide") else (if (isTamil) "விரிவாக்கு" else "Expand"),
                                        color = RichGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (showTranscript) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DeepNavyBackground.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isTamil) analysis.transcriptTa.ifBlank { analysis.transcriptEn } else analysis.transcriptEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryLightText,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // C. Verified Bible Passages Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = RichGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTamil) "சரிபார்க்கப்பட்ட வேத பகுதிகள் & வசனங்கள்" else "Verified Scripture Passages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                }
            }

            // References List Cards
            if (references.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isTamil) "இந்த வீடியோவிற்கு வேத குறிப்புகள் ஆய்வில் உள்ளன." else "Bible references are being analyzed.",
                            color = PrimaryLightText.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                items(references, key = { it.id }) { ref ->
                    ScriptureReferenceCard(
                        ref = ref,
                        isTamil = isTamil,
                        onOpenBible = {
                            onOpenBibleReader(ref.bookId, ref.chapterNumber, ref.verseStart)
                        }
                    )
                }
            }

            // G. AI Commentary Disclaimer
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = RichGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTamil)
                                "✨ குறிப்பு: இந்த வேத நுண்ணறிவு ஆய்வு, வீடியோவில் பேசப்பட்ட வார்த்தைகளை அடிப்படையாகக் கொண்டு உண்மையான வேத வசனங்களோடு இணைக்கப்பட்டுள்ளது. விளக்கங்கள் வேதாகம சத்தியங்களை தெளிவாகப் புரிந்துகொள்ள அளிக்கப்பட்ட கல்விப் பகுதியாகும்."
                            else
                                "✨ Note: This intelligent Bible analysis grounds spoken dialogue into verified canonical Scripture. Commentary is designed for structured study and devotional enrichment.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScriptureReferenceCard(
    ref: ShortBibleReferenceEntity,
    isTamil: Boolean,
    onOpenBible: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scripture_ref_card_${ref.bookId}_${ref.chapterNumber}_${ref.verseStart}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Reference + Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isTamil) ref.bookNameTa else ref.bookNameEn} ${ref.chapterNumber}:${ref.verseStart}${if (ref.verseEnd > ref.verseStart) "-${ref.verseEnd}" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )

                val badgeText = when (ref.referenceType) {
                    "QUOTATION" -> if (isTamil) "நேரடி வேத மேற்கோள்" else "Exact Scripture Quote"
                    "INCIDENT" -> if (isTamil) "வேத வரலாற்று சம்பவம்" else "Biblical Incident"
                    "SUPPORTING" -> if (isTamil) "ஆதார வேத பகுதி" else "Supporting Scripture"
                    else -> if (isTamil) "கருத்து தொடர்பு" else "Thematic Connection"
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (ref.referenceType) {
                        "QUOTATION" -> PrimaryRoyalBlue
                        "INCIDENT" -> SecondaryNavySurface
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verse Text Display
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isTamil) ref.verseTextTa else ref.verseTextEn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "— ${ref.translationName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RichGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Explanation
            Text(
                text = if (isTamil) ref.explanationTa else ref.explanationEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (ref.connectionEn.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🎯 ${if (isTamil) ref.connectionTa else ref.connectionEn}",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrightGoldHighlight,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direct Navigation Button: Open in Bible
            Button(
                onClick = onOpenBible,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRoyalBlue,
                    contentColor = BrightGoldHighlight
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_in_bible_${ref.bookId}_${ref.chapterNumber}")
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTamil) "வேதத்தில் வாசிக்க (Open in Bible)" else "Read Full Chapter in Bible",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
