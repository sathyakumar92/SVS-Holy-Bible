package com.example.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.DevotionalData
import com.example.data.model.BibleBook
import com.example.data.model.ReadingHistory
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel

@Composable
fun HomeScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"
    val lastRead by viewModel.lastReadHistory.collectAsState()
    val readingStats by viewModel.readingStats.collectAsState()

    val todayVerse = DevotionalData.dailyVerses[0]
    val todayPrayer = DevotionalData.dailyPrayers[0]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Search Bar Entry Point
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { viewModel.navigateTo(AppScreen.SEARCH) }
                    .padding(horizontal = 16.dp, vertical = 13.dp)
                    .testTag("home_search_entry")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = RichGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isTamil) "வேத வசனங்கள், அதிகாரங்கள் அல்லது தலைப்புகளைத் தேடுங்கள்..." else "Search verses, chapters, or topics...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. First Tile: Welcome Message on First Open (NO Continue Reading) or Continue Reading if user has history
        item {
            if (lastRead == null) {
                WelcomeMessageCard(
                    isTamil = isTamil,
                    onStartReading = {
                        val johnBook = CanonicalBibleData.findBookById("JHN") ?: CanonicalBibleData.allBooks[0]
                        viewModel.openReader(johnBook, 1)
                    },
                    onFeedback = {
                        viewModel.navigateTo(AppScreen.FEEDBACK)
                    }
                )
            } else {
                ContinueReadingCard(
                    lastRead = lastRead,
                    isTamil = isTamil,
                    onResume = { history ->
                        val book = CanonicalBibleData.findBookById(history.bookId)
                            ?: CanonicalBibleData.allBooks[0]
                        viewModel.openReader(book, history.chapterNumber, history.verseNumber)
                    }
                )
            }
        }

        // Realtime Feedback Direct Access
        item {
            RealtimeFeedbackHomeBanner(
                isTamil = isTamil,
                onClick = { viewModel.navigateTo(AppScreen.FEEDBACK) }
            )
        }

        // 3. Daily Bible Verse Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("daily_verse_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SecondaryNavySurface
                ),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RichGold.copy(alpha = 0.35f)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryRoyalBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BrightGoldHighlight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "இன்றைய வேத வசனம்" else "Daily Bible Verse",
                                style = MaterialTheme.typography.labelLarge,
                                color = RichGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (prefs.bibleLanguage == "ta") todayVerse.referenceTa else todayVerse.referenceEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = BrightGoldHighlight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "“" + (if (prefs.bibleLanguage == "ta") todayVerse.textTa else todayVerse.textEn) + "”",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PrimaryLightText,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isTamil) todayVerse.reflectionTa else todayVerse.reflectionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryLightText
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val text = "${if (prefs.bibleLanguage == "ta") todayVerse.textTa else todayVerse.textEn}\n— ${todayVerse.referenceEn}"
                                clipboardManager.setText(AnnotatedString(text))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = SecondaryLightText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val shareText = "${if (prefs.bibleLanguage == "ta") todayVerse.textTa else todayVerse.textEn}\n— ${todayVerse.referenceEn}\n\nShared via SVS Holy Bible"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Verse"))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = SecondaryLightText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val book = CanonicalBibleData.findBookById(todayVerse.bookId)
                                    ?: CanonicalBibleData.allBooks[0]
                                viewModel.openReader(book, todayVerse.chapter, todayVerse.verse)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue,
                                contentColor = BrightGoldHighlight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTamil) "வாசிக்க" else "Read",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Reading Streak & Calendar Quick Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.navigateTo(AppScreen.READING_CALENDAR) }
                    .testTag("reading_streak_home_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RichGold.copy(alpha = 0.4f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryRoyalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isTamil) "வாசிப்பு நாட்காட்டி & தொடர்" else "Reading Calendar & Streak",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGoldHighlight
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${readingStats.currentStreak} ${if (isTamil) "நாட்கள் தொடர்" else "days streak"} • ${readingStats.todayChaptersRead}/${readingStats.dailyGoalChapters} ${if (isTamil) "இன்றைய இலக்கு" else "today's goal"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryLightText
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open Calendar",
                        tint = RichGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 4. Quick Feature Shortcuts Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = if (isTamil) "முக்கிய அம்சங்கள்" else "Quick Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FeatureShortcutChip(
                            title = if (isTamil) "ஷார்ட்ஸ்" else "Shorts",
                            icon = Icons.Default.PlayCircle,
                            color = PrimaryRoyalBlue,
                            onClick = { viewModel.navigateTo(AppScreen.SHORTS) }
                        )
                    }
                    item {
                        FeatureShortcutChip(
                            title = if (isTamil) "நாட்காட்டி" else "Calendar & Stats",
                            icon = Icons.Default.CalendarMonth,
                            color = ElevatedBlueSurface,
                            onClick = { viewModel.navigateTo(AppScreen.READING_CALENDAR) }
                        )
                    }
                    item {
                        FeatureShortcutChip(
                            title = if (isTamil) "வேத தேடல்" else "Bible Search",
                            icon = Icons.Default.Search,
                            color = ElevatedBlueSurface,
                            onClick = { viewModel.navigateTo(AppScreen.SEARCH) }
                        )
                    }
                    item {
                        FeatureShortcutChip(
                            title = if (isTamil) "ஆடியோ வேதம்" else "Audio Bible",
                            icon = Icons.Default.Headphones,
                            color = PrimaryRoyalBlue,
                            onClick = { viewModel.navigateTo(AppScreen.AUDIO_PLAYER) }
                        )
                    }
                    item {
                        FeatureShortcutChip(
                            title = if (isTamil) "வேத தலைப்புகள்" else "Bible Topics",
                            icon = Icons.Default.Label,
                            color = ElevatedBlueSurface,
                            onClick = { viewModel.navigateTo(AppScreen.TOPICS) }
                        )
                    }
                    item {
                        FeatureShortcutChip(
                            title = if (isTamil) "வேத வினாடி வினா" else "Bible Quiz",
                            icon = Icons.Default.Psychology,
                            color = PrimaryRoyalBlue,
                            onClick = { viewModel.navigateTo(AppScreen.QUIZ) }
                        )
                    }
                    item {
                        FeatureShortcutChip(
                            title = if (isTamil) "வாசிப்பு திட்டங்கள்" else "Reading Plans",
                            icon = Icons.Default.CalendarToday,
                            color = ElevatedBlueSurface,
                            onClick = { viewModel.navigateTo(AppScreen.READING_PLANS) }
                        )
                    }
                }
            }
        }

        // 5. Daily Prayer Shortcut Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.navigateTo(AppScreen.DAILY_DEVOTIONAL) }
                    .testTag("daily_prayer_home_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ElevatedBlueSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = RichGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTamil) "தினசரி ஜெபம்" else "Daily Prayer",
                            style = MaterialTheme.typography.labelMedium,
                            color = RichGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isTamil) todayPrayer.titleTa else todayPrayer.titleEn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isTamil) todayPrayer.scriptureRefTa else todayPrayer.scriptureRefEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 6. Old Testament Section (39 Books)
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isTamil) "பழைய ஏற்பாடு" else "Old Testament",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = PrimaryRoyalBlue.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isTamil) "39 புத்தகங்கள்" else "39 Books",
                                style = MaterialTheme.typography.labelSmall,
                                color = RichGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    TextButton(onClick = { viewModel.navigateTo(AppScreen.READER) }) {
                        Text(
                            text = if (isTamil) "அனைத்தும்" else "View All",
                            color = RichGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(CanonicalBibleData.oldTestamentBooks.take(12)) { book ->
                        BookCard(
                            book = book,
                            isTamil = isTamil,
                            onClick = { viewModel.openReader(book, 1) }
                        )
                    }
                }
            }
        }

        // 7. New Testament Section (27 Books)
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isTamil) "புதிய ஏற்பாடு" else "New Testament",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = PrimaryRoyalBlue.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isTamil) "27 புத்தகங்கள்" else "27 Books",
                                style = MaterialTheme.typography.labelSmall,
                                color = RichGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    TextButton(onClick = { viewModel.navigateTo(AppScreen.READER) }) {
                        Text(
                            text = if (isTamil) "அனைத்தும்" else "View All",
                            color = RichGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(CanonicalBibleData.newTestamentBooks.take(12)) { book ->
                        BookCard(
                            book = book,
                            isTamil = isTamil,
                            onClick = { viewModel.openReader(book, 1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueReadingCard(
    lastRead: ReadingHistory?,
    isTamil: Boolean,
    onResume: (ReadingHistory) -> Unit
) {
    if (lastRead != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { onResume(lastRead) }
                .testTag("continue_reading_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = ElevatedBlueSurface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryRoyalBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = BrightGoldHighlight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTamil) "தொடர்ந்து வாசிக்க" else "Continue Reading",
                        style = MaterialTheme.typography.labelSmall,
                        color = RichGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${lastRead.bookName} • ${if (isTamil) "அதிகாரம்" else "Chapter"} ${lastRead.chapterNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryLightText
                    )
                }

                Button(
                    onClick = { onResume(lastRead) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RichGold,
                        contentColor = DeepNavyBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isTamil) "தொடரவும்" else "Resume",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeMessageCard(
    isTamil: Boolean,
    onStartReading: () -> Unit,
    onFeedback: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("welcome_message_tile"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElevatedBlueSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RichGold.copy(alpha = 0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.example.ui.components.BibleLogo(
                    size = 52.dp,
                    animated = true,
                    modifier = Modifier.padding(end = 4.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTamil) "எஸ்.வி.எஸ் பரிசுத்த வேதாகமம்" else "Welcome to SVS Holy Bible",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                    Text(
                        text = if (isTamil) "நல்வரவு! தேவனுடைய வார்த்தையைத் தியானியுங்கள்" else "Your Daily Sanctuary in God's Holy Word",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryLightText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DeepNavyBackground.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isTamil)
                        "\"உம்முடைய வசனம் என் கால்களுக்குத் தீபமும், என் பாதைக்கு வெளிச்சமுமாயிருக்கிறது.\" — சங்கீதம் 119:105"
                    else
                        "\"Thy word is a lamp unto my feet, and a light unto my path.\" — Psalm 119:105",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrightGoldHighlight,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onStartReading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RichGold,
                    contentColor = DeepNavyBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_start_reading_button")
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTamil) "வாசிக்கத் தொடங்கு" else "Start Reading",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onFeedback,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_feedback_button")
            ) {
                Icon(Icons.Default.RateReview, contentDescription = null, tint = SecondaryLightText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTamil) "நேரடி கருத்து தெரிவிக்க (Realtime Feedback)" else "Share Realtime Feedback",
                    fontSize = 12.sp,
                    color = SecondaryLightText
                )
            }
        }
    }
}

@Composable
fun RealtimeFeedbackHomeBanner(
    isTamil: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("home_feedback_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RichGold.copy(alpha = 0.35f)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PrimaryRoyalBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    tint = BrightGoldHighlight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isTamil) "நேரடி பயனர் கருத்து (Realtime Feedback)" else "Realtime User Feedback",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                Text(
                    text = if (isTamil)
                        "உங்கள் மதிப்பீடு மற்றும் கருத்துகளை உடனுக்குடன் பகிருங்கள்"
                    else
                        "Share your ratings, suggestions & reports with us in real-time",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryLightText
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = RichGold
            )
        }
    }
}

@Composable
fun BookCard(
    book: BibleBook,
    isTamil: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(118.dp)
            .clickable { onClick() }
            .testTag("book_card_${book.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PrimaryRoyalBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isTamil) book.tamilAbbreviation else book.abbreviation,
                    style = MaterialTheme.typography.labelSmall,
                    color = RichGold,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isTamil) book.tamilName else book.englishName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${book.chapterCount} ${if (isTamil) "அதிகாரங்கள்" else "Chs"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FeatureShortcutChip(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = BrightGoldHighlight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryLightText,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
