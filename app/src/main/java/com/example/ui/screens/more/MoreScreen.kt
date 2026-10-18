package com.example.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel

@Composable
fun MoreScreen(
    viewModel: BibleViewModel
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("more_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isTamil) "வேதாகம கருவிகள் & பகுதிகள்" else "Spiritual Features & Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RichGold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        item {
            MoreMenuCard(
                title = if (isTamil) "ஷார்ட்ஸ் (Shorts)" else "Shorts",
                subtitle = if (isTamil) "செங்குத்து வீடியோக்கள், அசல் ஆடியோ & வேத ஆய்வு" else "Vertical videos, original audio & Scripture analysis",
                icon = Icons.Default.PlayCircle,
                onClick = { viewModel.navigateTo(AppScreen.SHORTS) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "வாசிப்பு நாட்காட்டி & புள்ளிவிவரங்கள்" else "Reading Calendar & Statistics",
                subtitle = if (isTamil) "தினசரி தொடர், நாட்காட்டி பதிவுகள் மற்றும் வேத வாசிப்பு முன்னேற்றம்" else "Interactive monthly reading calendar, streaks & book analytics",
                icon = Icons.Default.DateRange,
                onClick = { viewModel.navigateTo(AppScreen.READING_CALENDAR) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "மேம்பட்ட வேதாகம தேடல்" else "Advanced Bible Search",
                subtitle = if (isTamil) "சொல், தொடர், வசனக் குறிப்பு மற்றும் வடிகட்டிகளுடன் தேடுக" else "Word, exact phrase, reference lookup & Testament filters",
                icon = Icons.Default.Search,
                onClick = { viewModel.navigateTo(AppScreen.SEARCH) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "வேத தலைப்புகள் (Bible Topics)" else "Bible Topics",
                subtitle = if (isTamil) "34+ விசுவாசம், அன்பு, சமாதானம் தலைப்புகள்" else "Curated Scripture across 34+ core theological topics",
                icon = Icons.Default.Label,
                onClick = { viewModel.navigateTo(AppScreen.TOPICS) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "ஆடியோ வேதாகமம் (Audio Bible)" else "Audio Bible Player",
                subtitle = if (isTamil) "குரல் வழி வாசிப்பு, வேகம் & பாலினக் கட்டுப்பாடு" else "Listen to spoken Scripture with speed & voice control",
                icon = Icons.Default.Headphones,
                onClick = { viewModel.navigateTo(AppScreen.AUDIO_PLAYER) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "தினசரி தியானம் & ஜெபம்" else "Daily Verse & Prayer",
                subtitle = if (isTamil) "அன்றாட தேவ வார்த்தை மற்றும் ஜெப விண்ணப்பம்" else "Daily spiritual reflection, prayer, and scripture",
                icon = Icons.Default.Favorite,
                onClick = { viewModel.navigateTo(AppScreen.DAILY_DEVOTIONAL) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "வேத வினாடி வினா (Bible Quiz)" else "Interactive Bible Quiz",
                subtitle = if (isTamil) "கேள்விகள் மற்றும் வேத ஆதாரங்கள்" else "Test and grow your biblical knowledge with scored quizzes",
                icon = Icons.Default.Psychology,
                onClick = { viewModel.navigateTo(AppScreen.QUIZ) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "வாசிப்பு திட்டங்கள் (Reading Plans)" else "Bible Reading Plans",
                subtitle = if (isTamil) "7 நாள், 14 நாள் அட்டவணைகள்" else "Structured daily portions to read through Scripture",
                icon = Icons.Default.CalendarToday,
                onClick = { viewModel.navigateTo(AppScreen.READING_PLANS) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "என் நூலகம் (Bookmarks & Notes)" else "My Personal Library",
                subtitle = if (isTamil) "புக்மார்க்குகள், குறிப்புகள் மற்றும் வரலாறு" else "Saved bookmarks, verse notes, and reading history",
                icon = Icons.Default.Bookmark,
                onClick = { viewModel.navigateTo(AppScreen.LIBRARY) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "நேரடி பயனர் கருத்து (Realtime Feedback)" else "Realtime User Feedback",
                subtitle = if (isTamil) "உங்கள் மதிப்பீடுகள், ஆலோசனைகள் மற்றும் கருத்துகளைப் பகிருங்கள்" else "Submit ratings, reports, and suggestions in real-time",
                icon = Icons.Default.RateReview,
                onClick = { viewModel.navigateTo(AppScreen.FEEDBACK) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "அமைப்புகள் (Settings)" else "Settings",
                subtitle = if (isTamil) "தோற்றம், எழுத்து அளவு, மொழி, நினைவகம்" else "Appearance, font size, language, and cache",
                icon = Icons.Default.Settings,
                onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
            )
        }

        item {
            MoreMenuCard(
                title = if (isTamil) "பயன்பாட்டைப் பற்றி (About)" else "About SVS Holy Bible",
                subtitle = "SVS Holy Bible v1.0.0 • Authorized Translations",
                icon = Icons.Default.Info,
                onClick = { viewModel.navigateTo(AppScreen.ABOUT) }
            )
        }
    }
}

@Composable
fun MoreMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PrimaryRoyalBlue.copy(alpha = 0.25f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = RichGold, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
