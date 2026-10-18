package com.example.ui.screens.daily

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.DevotionalData
import com.example.data.model.DailyPrayer
import com.example.data.model.DailyVerse
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel

@Composable
fun DailyDevotionalScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.bibleLanguage == "ta"

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Daily Verse, 1: Daily Prayer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("daily_devotional_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = RichGold
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isTamil) "தினசரி வேத வசனம்" else "Daily Verse", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isTamil) "தினசரி ஜெபம்" else "Daily Prayer", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(DevotionalData.dailyVerses) { verse ->
                    DailyVerseFullCard(
                        verse = verse,
                        isTamil = isTamil,
                        onOpen = {
                            val book = CanonicalBibleData.findBookById(verse.bookId) ?: CanonicalBibleData.allBooks[0]
                            viewModel.openReader(book, verse.chapter, verse.verse)
                        },
                        onShare = {
                            val shareText = "${if (isTamil) verse.textTa else verse.textEn}\n— ${if (isTamil) verse.referenceTa else verse.referenceEn}\n\nShared via SVS Holy Bible"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Daily Verse"))
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(DevotionalData.dailyPrayers) { prayer ->
                    DailyPrayerFullCard(
                        prayer = prayer,
                        isTamil = isTamil,
                        onShare = {
                            val shareText = "${if (isTamil) prayer.titleTa else prayer.titleEn}\n\n${if (isTamil) prayer.contentTa else prayer.contentEn}\n\n${if (isTamil) prayer.scriptureRefTa else prayer.scriptureRefEn}\n\nShared via SVS Holy Bible"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Prayer"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DailyVerseFullCard(
    verse: DailyVerse,
    isTamil: Boolean,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTamil) verse.referenceTa else verse.referenceEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = RichGold, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“${if (isTamil) verse.textTa else verse.textEn}”",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = PrimaryLightText,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isTamil) verse.reflectionTa else verse.reflectionEn,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryLightText,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isTamil) "வேதத்தில் வாசிக்க" else "Read in Bible", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DailyPrayerFullCard(
    prayer: DailyPrayer,
    isTamil: Boolean,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTamil) prayer.titleTa else prayer.titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = RichGold, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isTamil) prayer.contentTa else prayer.contentEn,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "— ${if (isTamil) prayer.scriptureRefTa else prayer.scriptureRefEn}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryRoyalBlue
            )
        }
    }
}
