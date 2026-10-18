package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Official SVS Holy Bible Logo & Icon Component
 * Faithfully matches the user's official uploaded logo design:
 * - Rounded square Royal Blue container with rich gradient
 * - Open pristine white Holy Bible with dual curved pages and blue scripture lines
 * - Radiant Golden Latin Cross with 3D faceted bevels and tapered spine base
 * - Twin 4-point golden celestial morning stars on the left and right
 */
@Composable
fun BibleLogo(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    animated: Boolean = true,
    showContainer: Boolean = true
) {
    if (!animated) {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawHolyBibleLogo(
                    scope = this,
                    openProgress = 1f,
                    crossRise = 1f,
                    starsShine = 1f,
                    glowPulse = 0.8f,
                    showCardBackground = showContainer
                )
            }
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "LogoGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraPulse"
    )

    val bookOpenProgress by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BookOpen"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawHolyBibleLogo(
                scope = this,
                openProgress = bookOpenProgress,
                crossRise = 1f,
                starsShine = glowPulse,
                glowPulse = glowPulse,
                showCardBackground = showContainer
            )
        }
    }
}

/**
 * Shared drawing engine for the SVS Holy Bible official logo:
 * - Precisely implements the user's uploaded logo artwork
 * - Supports animated progress parameters for splash and intro sequences
 */
fun drawHolyBibleLogo(
    scope: DrawScope,
    openProgress: Float = 1f,
    crossRise: Float = 1f,
    starsShine: Float = 1f,
    glowPulse: Float = 1f,
    sweepProgress: Float = 0f,
    showCardBackground: Boolean = true
) {
    with(scope) {
        val w = size.width
        val h = size.height

        // 1. Royal Blue Squircle Card Container (Matches uploaded icon)
        if (showCardBackground) {
            val cornerRadius = w * 0.24f
            val cardPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = w * 0.04f,
                        top = h * 0.04f,
                        right = w * 0.96f,
                        bottom = h * 0.96f,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }

            // Outer Soft Ambient Shadow
            drawPath(
                path = cardPath,
                color = Color.Black.copy(alpha = 0.35f)
            )

            // Deep Royal Blue Gradient Background
            drawPath(
                path = cardPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F4DB8), // Vibrant royal blue top
                        Color(0xFF0B389C), // Medium royal blue center
                        Color(0xFF052166)  // Rich deep blue bottom
                    ),
                    startY = 0f,
                    endY = h
                )
            )
        }

        // 2. Twin Radiant 4-Point Golden Morning Stars (Sparkles on Left & Right)
        val starAlpha = starsShine.coerceIn(0f, 1f)
        if (starAlpha > 0.05f) {
            val starSize = w * 0.12f * starAlpha

            // Left Star (Positioned at 30% width, 36% height)
            drawFourPointStar(
                center = Offset(w * 0.30f, h * 0.365f),
                size = starSize,
                colorLight = Color(0xFFFFE066),
                colorDark = Color(0xFFF59E0B)
            )

            // Right Star (Positioned at 70% width, 36% height)
            drawFourPointStar(
                center = Offset(w * 0.70f, h * 0.365f),
                size = starSize,
                colorLight = Color(0xFFFFE066),
                colorDark = Color(0xFFF59E0B)
            )
        }

        // 3. Open Holy Bible Base Underlay / Spine Shadow (Deep Navy Blue #0D44A8)
        val spineSpread = openProgress.coerceIn(0.5f, 1.1f)
        val underlayPath = Path().apply {
            moveTo(w * 0.17f, h * 0.52f)
            cubicTo(w * 0.32f, h * 0.46f, w * 0.44f, h * 0.48f, w * 0.50f, h * 0.53f)
            cubicTo(w * 0.56f, h * 0.48f, w * 0.68f, h * 0.46f, w * 0.83f, h * 0.52f)
            lineTo(w * 0.83f, h * 0.78f)
            cubicTo(w * 0.68f, h * 0.72f, w * 0.56f, h * 0.73f, w * 0.50f, h * 0.80f)
            cubicTo(w * 0.44f, h * 0.73f, w * 0.32f, h * 0.72f, w * 0.17f, h * 0.78f)
            close()
        }
        drawPath(underlayPath, color = Color(0xFF093180))

        // 4. Open Bible Pages (Left and Right - Pure White with subtle page curve)
        // Left Page
        val leftPage = Path().apply {
            moveTo(w * 0.18f, h * (0.52f - (spineSpread - 1f) * 0.03f))
            cubicTo(
                w * 0.32f, h * (0.47f - (spineSpread - 1f) * 0.02f),
                w * 0.43f, h * 0.48f,
                w * 0.495f, h * 0.525f
            )
            lineTo(w * 0.495f, h * 0.765f)
            cubicTo(
                w * 0.43f, h * 0.72f,
                w * 0.32f, h * 0.70f,
                w * 0.18f, h * 0.755f
            )
            close()
        }
        drawPath(leftPage, color = Color.White)

        // Right Page
        val rightPage = Path().apply {
            moveTo(w * 0.505f, h * 0.525f)
            cubicTo(
                w * 0.57f, h * 0.48f,
                w * 0.68f, h * (0.47f - (spineSpread - 1f) * 0.02f),
                w * 0.82f, h * (0.52f - (spineSpread - 1f) * 0.03f)
            )
            lineTo(w * 0.82f, h * 0.755f)
            cubicTo(
                w * 0.68f, h * 0.70f,
                w * 0.57f, h * 0.72f,
                w * 0.505f, h * 0.765f
            )
            close()
        }
        drawPath(rightPage, color = Color.White)

        // 5. Scripture Text Line Markings (Four distinct royal blue rounded lines per page)
        val lineStroke = w * 0.032f
        val lineColor = Color(0xFF1451C2)

        // Left Page: 4 Horizontal Scripture Lines
        drawLine(lineColor, Offset(w * 0.24f, h * 0.565f), Offset(w * 0.435f, h * 0.545f), strokeWidth = lineStroke, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(w * 0.24f, h * 0.615f), Offset(w * 0.435f, h * 0.595f), strokeWidth = lineStroke, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(w * 0.24f, h * 0.665f), Offset(w * 0.435f, h * 0.645f), strokeWidth = lineStroke, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(w * 0.24f, h * 0.715f), Offset(w * 0.415f, h * 0.695f), strokeWidth = lineStroke, cap = StrokeCap.Round)

        // Right Page: 4 Horizontal Scripture Lines
        drawLine(lineColor, Offset(w * 0.565f, h * 0.545f), Offset(w * 0.76f, h * 0.565f), strokeWidth = lineStroke, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(w * 0.565f, h * 0.595f), Offset(w * 0.76f, h * 0.615f), strokeWidth = lineStroke, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(w * 0.565f, h * 0.645f), Offset(w * 0.76f, h * 0.665f), strokeWidth = lineStroke, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(w * 0.585f, h * 0.695f), Offset(w * 0.76f, h * 0.715f), strokeWidth = lineStroke, cap = StrokeCap.Round)

        // 6. Radiant Golden Latin Cross (Standing in the center spine with 3D faceted bevels)
        val riseOffset = (1f - crossRise.coerceIn(0f, 1f)) * (h * 0.12f)
        val crossAlpha = crossRise.coerceIn(0f, 1f)

        if (crossAlpha > 0.05f) {
            val topTipY = h * 0.215f + riseOffset
            val beamY = h * 0.335f + riseOffset
            val bottomTipY = h * 0.815f + riseOffset

            val leftArmTipX = w * 0.39f
            val rightArmTipX = w * 0.61f
            val centerX = w * 0.50f
            val shaftHalfW = w * 0.026f
            val beamHalfH = h * 0.026f

            // Left / Top Half (Brilliant Gold Highlight facet)
            val leftFacetPath = Path().apply {
                moveTo(centerX, topTipY)
                lineTo(centerX - shaftHalfW * 1.5f, topTipY + h * 0.035f)
                lineTo(centerX - shaftHalfW, topTipY + h * 0.045f)
                lineTo(centerX - shaftHalfW, beamY - beamHalfH)
                lineTo(leftArmTipX + w * 0.035f, beamY - beamHalfH)
                lineTo(leftArmTipX, beamY)
                lineTo(leftArmTipX + w * 0.035f, beamY + beamHalfH)
                lineTo(centerX - shaftHalfW, beamY + beamHalfH)
                lineTo(centerX - shaftHalfW, bottomTipY - h * 0.05f)
                lineTo(centerX, bottomTipY)
                close()
            }
            drawPath(
                path = leftFacetPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF275), // Light warm gold
                        Color(0xFFFFDF40), // Brilliant gold
                        Color(0xFFFBBF24)  // Amber gold
                    ),
                    startY = topTipY,
                    endY = bottomTipY
                )
            )

            // Right / Bottom Half (Rich Gold 3D Shadow facet)
            val rightFacetPath = Path().apply {
                moveTo(centerX, topTipY)
                lineTo(centerX + shaftHalfW * 1.5f, topTipY + h * 0.035f)
                lineTo(centerX + shaftHalfW, topTipY + h * 0.045f)
                lineTo(centerX + shaftHalfW, beamY - beamHalfH)
                lineTo(rightArmTipX - w * 0.035f, beamY - beamHalfH)
                lineTo(rightArmTipX, beamY)
                lineTo(rightArmTipX - w * 0.035f, beamY + beamHalfH)
                lineTo(centerX + shaftHalfW, beamY + beamHalfH)
                lineTo(centerX + shaftHalfW, bottomTipY - h * 0.05f)
                lineTo(centerX, bottomTipY)
                close()
            }
            drawPath(
                path = rightFacetPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE5A100), // Rich amber gold
                        Color(0xFFC78300), // Deep golden bronze
                        Color(0xFFA16600)  // Deep shadow gold
                    ),
                    startY = topTipY,
                    endY = bottomTipY
                )
            )

            // Center Ridge Line Highlight
            drawLine(
                color = Color(0xFFFFFBEB).copy(alpha = 0.85f * glowPulse),
                start = Offset(centerX, topTipY),
                end = Offset(centerX, bottomTipY),
                strokeWidth = w * 0.008f
            )
            drawLine(
                color = Color(0xFFFFFBEB).copy(alpha = 0.85f * glowPulse),
                start = Offset(leftArmTipX, beamY),
                end = Offset(rightArmTipX, beamY),
                strokeWidth = w * 0.008f
            )
        }

        // 7. Dynamic Cinematic Light Sweep across emblem
        if (sweepProgress in 0.01f..0.99f) {
            val sweepX = w * (sweepProgress * 1.6f - 0.3f)
            val sweepBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                start = Offset(sweepX - w * 0.15f, 0f),
                end = Offset(sweepX + w * 0.15f, h)
            )
            drawRect(
                brush = sweepBrush,
                topLeft = Offset(0f, 0f),
                size = size
            )
        }
    }
}

/**
 * Helper to draw a radiant 4-pointed golden celestial star with concave sides
 */
private fun DrawScope.drawFourPointStar(
    center: Offset,
    size: Float,
    colorLight: Color,
    colorDark: Color
) {
    val r = size / 2f
    val inner = r * 0.24f

    // 4-point star path
    val path = Path().apply {
        moveTo(center.x, center.y - r) // Top
        quadraticTo(center.x, center.y, center.x + r, center.y) // Right
        quadraticTo(center.x, center.y, center.x, center.y + r) // Bottom
        quadraticTo(center.x, center.y, center.x - r, center.y) // Left
        quadraticTo(center.x, center.y, center.x, center.y - r) // Back to top
        close()
    }

    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(colorLight, colorDark),
            center = center,
            radius = r
        )
    )

    // Center bright diamond highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        center = center,
        radius = inner * 0.7f
    )
}
