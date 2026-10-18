package com.example.ui.screens.listening

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImmersiveListeningScreen(
    viewModel: BibleViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val prefs by viewModel.userPreferences.collectAsState()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val audioState by viewModel.audioEngine.playbackState.collectAsState()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsState()
    val isTamil = prefs.bibleLanguage == "ta"

    var showSleepTimerMenu by remember { mutableStateOf(false) }

    // Pulsing aura animation during playback
    val infiniteTransition = rememberInfiniteTransition(label = "AuraPulse")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )

    // Countdown effect for active sleep timer
    var remainingSeconds by remember { mutableIntStateOf(sleepTimerMinutes * 60) }
    LaunchedEffect(sleepTimerMinutes) {
        remainingSeconds = sleepTimerMinutes * 60
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
            if (remainingSeconds == 0) {
                viewModel.audioEngine.stop()
                viewModel.setSleepTimer(0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
            .padding(16.dp)
            .testTag("immersive_listening_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = BrightGoldHighlight, modifier = Modifier.size(32.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isTamil) "முழுமையான ஆடியோ தியானம்" else "Immersive Audio Bible",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
                Text(
                    text = if (isTamil) "தூய தமிழ் வேதாகமம் (BSI)" else "Authorized King James Version (KJV)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryLightText
                )
            }

            // Sleep Timer Action
            Box {
                IconButton(onClick = { showSleepTimerMenu = true }) {
                    Icon(
                        imageVector = if (sleepTimerMinutes > 0) Icons.Default.Bedtime else Icons.Default.BedtimeOff,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepTimerMinutes > 0) RichGold else SecondaryLightText
                    )
                }

                DropdownMenu(
                    expanded = showSleepTimerMenu,
                    onDismissRequest = { showSleepTimerMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isTamil) "டைமர் முடக்கு (Off)" else "Off") },
                        onClick = { viewModel.setSleepTimer(0); showSleepTimerMenu = false }
                    )
                    listOf(5, 10, 15, 30, 45, 60).forEach { mins ->
                        DropdownMenuItem(
                            text = { Text("$mins ${if (isTamil) "நிமிடங்கள்" else "minutes"}") },
                            onClick = { viewModel.setSleepTimer(mins); showSleepTimerMenu = false }
                        )
                    }
                }
            }
        }

        // Active Sleep Timer Banner
        if (sleepTimerMinutes > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ElevatedBlueSurface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = BrightGoldHighlight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    val minLeft = remainingSeconds / 60
                    val secLeft = remainingSeconds % 60
                    Text(
                        text = "${if (isTamil) "ஸ்லீப் டைமர்:" else "Sleep Timer:"} ${String.format("%02d:%02d", minLeft, secLeft)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrightGoldHighlight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Radiant Illuminated Sacred Album Art
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(if (audioState.isPlaying) auraScale else 1.0f),
            contentAlignment = Alignment.Center
        ) {
            // Glow backdrop
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(BrightGoldHighlight.copy(alpha = 0.35f), PrimaryRoyalBlue.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
            )

            // Inner Album disc
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(CircleShape)
                    .background(SecondaryNavySurface)
                    .border(2.dp, RichGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = BrightGoldHighlight,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isTamil) selectedBook.tamilName else selectedBook.englishName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryLightText
                    )
                    Text(
                        text = "${if (isTamil) "அதிகாரம்" else "Chapter"} $selectedChapter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RichGold
                    )
                }
            }
        }

        // Real-Time Playback Progress Bar & Status
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = audioState.statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryLightText,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (audioState.totalVerses > 0) {
                val progressFraction = (audioState.currentVerseIndex + 1).toFloat() / audioState.totalVerses.toFloat()
                LinearProgressIndicator(
                    progress = { progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = RichGold,
                    trackColor = ElevatedBlueSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${if (isTamil) "வசனம்" else "Verse"} ${audioState.currentVerseNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryLightText
                    )
                    Text(
                        text = "${audioState.totalVerses} ${if (isTamil) "மொத்த வசனங்கள்" else "total verses"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryLightText
                    )
                }
            }
        }

        // Audio Transport Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousChapter() }, modifier = Modifier.size(46.dp)) {
                        Icon(Icons.Default.SkipPrevious, "Previous Chapter", tint = PrimaryLightText, modifier = Modifier.size(30.dp))
                    }

                    IconButton(onClick = { viewModel.audioEngine.skipPrevious() }, modifier = Modifier.size(46.dp)) {
                        Icon(Icons.Default.FastRewind, "Previous Verse", tint = PrimaryLightText, modifier = Modifier.size(26.dp))
                    }

                    // Large Center Play/Pause button
                    Surface(
                        shape = CircleShape,
                        color = PrimaryRoyalBlue,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.togglePlayPauseAudio()
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = BrightGoldHighlight,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.audioEngine.skipNext() }, modifier = Modifier.size(46.dp)) {
                        Icon(Icons.Default.FastForward, "Next Verse", tint = PrimaryLightText, modifier = Modifier.size(26.dp))
                    }

                    IconButton(onClick = { viewModel.nextChapter() }, modifier = Modifier.size(46.dp)) {
                        Icon(Icons.Default.SkipNext, "Next Chapter", tint = PrimaryLightText, modifier = Modifier.size(30.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Playback Speed Selection Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "வேகம்:" else "Speed:",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryLightText,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { spd ->
                        FilterChip(
                            selected = prefs.audioSpeed == spd,
                            onClick = { viewModel.setAudioSpeed(spd) },
                            label = { Text("${spd}x", fontSize = 11.sp) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
