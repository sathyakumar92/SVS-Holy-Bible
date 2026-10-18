package com.example.ui.screens.shorts

import androidx.compose.animation.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel

/**
 * Rebuilt Bible Shorts Home Page (Requirement 2):
 * - Exactly two video cards per full row in a two-column grid
 * - Consistent card widths, spacing, and vertical 9:16 preview container
 * - Displays exact administrator-provided title (no automatic rewriting)
 * - Clickable card opens dedicated Shorts video player
 * - Clean empty state when no published shorts exist (no placeholder videos)
 * - Admin button for authorized video upload and publishing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleShortsScreen(
    viewModel: BibleViewModel
) {
    val publishedShorts by viewModel.publishedShorts.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta" || prefs.bibleLanguage == "ta"

    // Automatically ensure curated shorts are loaded if database was empty
    LaunchedEffect(Unit) {
        viewModel.seedInitialShortsIfEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryRoyalBlue,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = BrightGoldHighlight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isTamil) "ஷார்ட்ஸ் (Shorts)" else "Shorts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                            Text(
                                text = if (isTamil) "வீடியோக்கள் & வேத ஆய்வு" else "Videos & Scripture Analysis",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryLightText.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    // Authorized Admin Upload Button
                    FilledTonalButton(
                        onClick = { viewModel.navigateTo(AppScreen.SHORTS_ADMIN_STUDIO) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PrimaryRoyalBlue.copy(alpha = 0.5f),
                            contentColor = BrightGoldHighlight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("open_admin_studio_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Admin Upload", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTamil) "நிர்வாகி பதிவேற்றம்" else "Admin Studio",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
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
        if (publishedShorts.isEmpty()) {
            // Requirement 2: Clean empty state when no shorts published (Never show placeholder videos)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .testTag("shorts_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryRoyalBlue.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = BrightGoldHighlight,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isTamil) "வேத ஷார்ட்ஸ்கள் எதுவும் இன்னும் வெளியிடப்படவில்லை" else "No Bible Shorts Published Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isTamil)
                                "நிர்வாகி இன்னும் எந்த வீடியோக்களையும் வெளியிடவில்லை. அங்கீகரிக்கப்பட்ட நிர்வாகி மட்டுமே வீடியோக்களைப் பதிவேற்றி வெளியிட முடியும்."
                            else
                                "The administrator has not published any video shorts yet. Only authorized developer/admin uploads will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryLightText.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.SHORTS_ADMIN_STUDIO) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue,
                                contentColor = BrightGoldHighlight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_state_admin_button")
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "நிர்வாகி ஸ்டுடியோவைத் திற (Admin Upload)" else "Open Admin Studio & Publish Videos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Requirement 2: Two-Column Grid of 9:16 Cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag("shorts_two_column_grid")
            ) {
                // Header Banner
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = RichGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTamil) "ஷார்ட்ஸ் & வேத ஆய்வு" else "Shorts with Scripture Analysis",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGoldHighlight
                                )
                                Text(
                                    text = if (isTamil) "வீடியோவில் 'வேதம்' பட்டனைத் தட்டி வசனங்களை ஆராயுங்கள்" else "Tap 'Bible' in any video to see verified Scripture passages",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryLightText.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Two video cards per full row
                items(publishedShorts, key = { it.id }) { item ->
                    ShortGridCard(
                        short = item,
                        isTamil = isTamil,
                        onClick = {
                            viewModel.playShort(item)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual Short Video Card in the 2-Column Grid:
 * - Strict 9:16 vertical preview container
 * - Exact administrator-provided title (truncated cleanly to 2 lines without overlapping preview)
 * - Category badge and duration pill
 * - Likes count indicator
 */
@Composable
private fun ShortGridCard(
    short: ShortEntity,
    isTamil: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("short_card_${short.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column {
            // 9:16 Vertical Video Preview Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(DeepNavyBackground)
            ) {
                if (!short.thumbnailStoragePath.isNullOrBlank() && File(short.thumbnailStoragePath).exists()) {
                    AsyncImage(
                        model = File(short.thumbnailStoragePath),
                        contentDescription = short.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PrimaryRoyalBlue.copy(alpha = 0.85f),
                                        ElevatedBlueSurface,
                                        DeepNavyBackground
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = BrightGoldHighlight.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Semi-transparent gradient overlay to ensure top and bottom pills pop
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
                // Top Category & Verified Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = short.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = RichGold, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${short.durationSeconds}s",
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Centered Play Action Icon
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.Center)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = BrightGoldHighlight,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Likes Pill on Preview
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color(0xFFFF4081), modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${short.likesCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Below Preview: Title area (Prevents title text from overlapping the video preview)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text(
                    text = short.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = RichGold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTamil) "வேத ஆய்வு உண்டு" else "Bible Study Included",
                        fontSize = 9.sp,
                        color = PrimaryLightText.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
