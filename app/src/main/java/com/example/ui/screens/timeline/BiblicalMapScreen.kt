package com.example.ui.screens.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.AdvancedSeedData
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.BiblicalLocation
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiblicalMapScreen(
    viewModel: BibleViewModel
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    val initialLocId by viewModel.selectedLocationId.collectAsState()
    val locations = AdvancedSeedData.biblicalLocations

    var selectedLocation by remember {
        mutableStateOf(locations.firstOrNull { it.id == initialLocId } ?: locations.first())
    }

    var isMapView by remember { mutableStateOf(true) } // true: Interactive Map, false: List View

    // Interactive canvas pan & zoom states
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("biblical_map_screen")
    ) {
        // App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (isTamil) "வேதாகம வரைபடம் (Biblical Maps)" else "Biblical Maps Explorer",
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
                }
            },
            actions = {
                IconButton(onClick = { isMapView = !isMapView }) {
                    Icon(
                        imageVector = if (isMapView) Icons.Default.List else Icons.Default.Map,
                        contentDescription = "Toggle View",
                        tint = BrightGoldHighlight
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ElevatedBlueSurface)
        )

        // Location Selector Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(locations) { loc ->
                FilterChip(
                    selected = selectedLocation.id == loc.id,
                    onClick = { selectedLocation = loc },
                    label = { Text(if (isTamil) loc.nameTa else loc.nameEn) },
                    leadingIcon = {
                        Icon(Icons.Default.Place, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }

        if (isMapView) {
            // Interactive Biblical Geography Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepNavyBackground)
                    .border(1.dp, PrimaryRoyalBlue, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.7f, 3.5f)
                            offset = Offset(offset.x + pan.x, offset.y + pan.y)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f + offset.x, h / 2f + offset.y)

                    // Draw Stylized Ancient Mediterranean & Levant Coastline Contours
                    // Water Body: Mediterranean Sea
                    drawCircle(
                        color = SecondaryNavySurface.copy(alpha = 0.5f),
                        radius = w * 0.45f * scale,
                        center = center
                    )

                    // Grid lines of biblical latitude/longitude
                    for (i in -3..3) {
                        drawLine(
                            color = PrimaryRoyalBlue.copy(alpha = 0.2f),
                            start = Offset(center.x + (i * 70f * scale), 0f),
                            end = Offset(center.x + (i * 70f * scale), h),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = PrimaryRoyalBlue.copy(alpha = 0.2f),
                            start = Offset(0f, center.y + (i * 70f * scale)),
                            end = Offset(w, center.y + (i * 70f * scale)),
                            strokeWidth = 1f
                        )
                    }

                    // Render Location Markers mapped from relative geographical coords
                    // Approximate Levant coordinate bounding box: Lat 28..42, Long 12..45
                    locations.forEach { loc ->
                        val relX = ((loc.longitude - 25.0) / 20.0).toFloat()
                        val relY = ((38.0 - loc.latitude) / 14.0).toFloat()

                        val pinX = center.x + (relX * 220f - 110f) * scale
                        val pinY = center.y + (relY * 220f - 110f) * scale

                        val isTarget = loc.id == selectedLocation.id

                        // Outer Pulse circle if selected
                        if (isTarget) {
                            drawCircle(
                                color = BrightGoldHighlight.copy(alpha = 0.35f),
                                radius = 18f * scale,
                                center = Offset(pinX, pinY)
                            )
                            drawCircle(
                                color = RichGold,
                                radius = 22f * scale,
                                center = Offset(pinX, pinY),
                                style = Stroke(width = 2f)
                            )
                        }

                        // Pin Center
                        drawCircle(
                            color = if (isTarget) BrightGoldHighlight else PrimaryRoyalBlue,
                            radius = if (isTarget) 8f * scale else 6f * scale,
                            center = Offset(pinX, pinY)
                        )
                    }
                }

                // Interactive Pan & Zoom Guidance Overlay
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DeepNavyBackground.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (isTamil) "இருவிரலால் சுருக்க/பெரிதாக்க (Pinch to Zoom)" else "Pinch to Zoom • Drag to Pan",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrightGoldHighlight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Selected Location Detail Bottom Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        Column {
                            Text(
                                text = if (isTamil) selectedLocation.nameTa else selectedLocation.nameEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                            Text(
                                text = "Modern: ${selectedLocation.modernName} • Lat ${String.format("%.2f", selectedLocation.latitude)}, Long ${String.format("%.2f", selectedLocation.longitude)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryLightText
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryRoyalBlue
                        ) {
                            Text(
                                text = selectedLocation.period,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrightGoldHighlight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isTamil) selectedLocation.descriptionTa else selectedLocation.descriptionEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryLightText,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scriptures: ${selectedLocation.scriptureReferences}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RichGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Fallback List View
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(locations) { loc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLocation = loc
                                isMapView = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTamil) loc.nameTa else loc.nameEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGoldHighlight
                                )
                                Text(
                                    text = loc.period,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RichGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isTamil) loc.descriptionTa else loc.descriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryLightText
                            )
                        }
                    }
                }
            }
        }
    }
}
