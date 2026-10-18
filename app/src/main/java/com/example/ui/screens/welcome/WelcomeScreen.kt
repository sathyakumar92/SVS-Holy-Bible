package com.example.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.drawHolyBibleLogo
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Premium SVS Holy Bible Startup Intro Animation
 * - Features the official uploaded SVS Holy Bible Logo
 * - Medium-speed reverent progression within 5.0 - 5.5 seconds
 * - Choreography:
 *   1. Sacred Ambient Glow (0.0s - 1.2s)
 *   2. Royal Blue Squircle Logo Reveal (1.2s - 2.6s)
 *   3. Open Bible & Twin Morning Stars Bloom (2.6s - 3.8s)
 *   4. Radiant Golden Cross & Light Sweep (3.8s - 4.8s)
 *   5. Bilingual Title "SVS Holy Bible • பரிசுத்த வேதாகமம்" (4.8s - 5.4s)
 * - Includes a gentle skip button for fast accessibility
 */
@Composable
fun WelcomeScreen(
    onAnimationFinished: () -> Unit
) {
    var animStage by remember { mutableIntStateOf(0) }

    // Master Animation State (Stages 0 to 4 across 5.4 seconds)
    LaunchedEffect(Unit) {
        // Stage 0: Initial aura & ambient light
        animStage = 1 // Logo container begins reveal
        delay(1200)

        // Stage 2: Open Bible & twin stars shine
        animStage = 2
        delay(1400)

        // Stage 3: Golden cross rise & light sweep
        animStage = 3
        delay(1200)

        // Stage 4: Brand typography reveal & completion
        animStage = 4
        delay(1600)

        // Finish intro and enter app
        onAnimationFinished()
    }

    // Smooth continuous animations
    val emblemScale by animateFloatAsState(
        targetValue = when (animStage) {
            0 -> 0.82f
            1 -> 0.94f
            2 -> 0.98f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "emblemScale"
    )

    val emblemAlpha by animateFloatAsState(
        targetValue = if (animStage >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "emblemAlpha"
    )

    val bookOpenProgress by animateFloatAsState(
        targetValue = if (animStage >= 2) 1.0f else 0.5f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "bookOpenProgress"
    )

    val starsShine by animateFloatAsState(
        targetValue = if (animStage >= 2) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "starsShine"
    )

    val crossRise by animateFloatAsState(
        targetValue = if (animStage >= 3) 1.0f else 0.2f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "crossRise"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (animStage >= 3) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "titleAlpha"
    )

    // Cinematic Light Sweep across the cross and logo
    val sweepTransition = rememberInfiniteTransition(label = "LightSweep")
    val sweepProgress by sweepTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepProgress"
    )

    // Breathing sacred aura
    val glowPulse by sweepTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
            .testTag("welcome_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Top Skip Button for immediate access
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, RichGold.copy(alpha = 0.3f)),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAnimationFinished() }
                    .testTag("skip_intro_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Skip • தொடரவும்",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = BrightGoldHighlight.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Skip",
                        tint = BrightGoldHighlight.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Ambient Volumetric Golden Halo
        Canvas(
            modifier = Modifier
                .size(360.dp)
                .alpha(glowPulse)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BrightGoldHighlight.copy(alpha = 0.35f * glowPulse),
                        Color(0xFF0F4DB8).copy(alpha = 0.22f * glowPulse),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width / 2f
                ),
                radius = size.width / 2f,
                center = center
            )
        }

        // Center Content: Logo Artwork + Brand Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Official SVS Holy Bible Logo Container (Uploaded Artwork)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(emblemScale)
                    .alpha(emblemAlpha),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawHolyBibleLogo(
                        scope = this,
                        openProgress = bookOpenProgress,
                        crossRise = crossRise,
                        starsShine = starsShine,
                        glowPulse = glowPulse,
                        sweepProgress = if (animStage >= 3) sweepProgress else 0f,
                        showCardBackground = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Brand Name & Sacred Motto
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(titleAlpha)
            ) {
                Text(
                    text = "SVS HOLY BIBLE",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "பரிசுத்த வேதாகமம் • Bilingual Bible",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = RichGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "“உம்முடைய வசனம் என் கால்களுக்குத் தீபமும், என் பாதைக்கு வெளிச்சமுமாயிருக்கிறது”",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Psalms 119:105 • சங்கீதம் 119:105",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrightGoldHighlight.copy(alpha = 0.7f)
                )
            }
        }
    }
}
