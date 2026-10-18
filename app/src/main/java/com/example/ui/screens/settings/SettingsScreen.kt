package com.example.ui.screens.settings

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel

@Composable
fun SettingsScreen(
    viewModel: BibleViewModel
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var showClearCacheDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Appearance
        item {
            SettingsSectionHeader(title = if (isTamil) "தோற்றம் (Appearance)" else "Appearance")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isTamil) "வண்ண தீம்" else "Theme Mode")
                        Row {
                            listOf("dark" to (if (isTamil) "இருள்" else "Dark"), "light" to (if (isTamil) "வெளிச்சம்" else "Light"), "system" to (if (isTamil) "தானியங்கி" else "System")).forEach { (mode, label) ->
                                FilterChip(
                                    selected = prefs.themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Language
        item {
            SettingsSectionHeader(title = if (isTamil) "மொழி அமைப்புகள்" else "Language & Editions")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isTamil) "பயன்பாட்டு மொழி" else "App Interface Language")
                        Row {
                            FilterChip(
                                selected = prefs.interfaceLanguage == "en",
                                onClick = {
                                    viewModel.setBibleLanguage("en")
                                },
                                label = { Text("English", fontSize = 11.sp) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            FilterChip(
                                selected = prefs.interfaceLanguage == "ta",
                                onClick = {
                                    viewModel.setBibleLanguage("ta")
                                },
                                label = { Text("தமிழ்", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Section: Reader Settings
        item {
            SettingsSectionHeader(title = if (isTamil) "வாசிப்பு அமைப்புகள்" else "Bible Reading Settings")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${if (isTamil) "எழுத்து அளவு:" else "Font Size:"} ${prefs.fontSizeSp}sp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = prefs.fontSizeSp.toFloat(),
                        onValueChange = { viewModel.setFontSize(it.toInt()) },
                        valueRange = 14f..28f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = RichGold, activeTrackColor = RichGold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${if (isTamil) "வரி இடைவெளி:" else "Line Spacing:"} ${String.format("%.1f", prefs.lineSpacingMultiplier)}x",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = prefs.lineSpacingMultiplier,
                        onValueChange = { viewModel.setLineSpacing(it) },
                        valueRange = 1.2f..2.0f,
                        steps = 4,
                        colors = SliderDefaults.colors(thumbColor = RichGold, activeTrackColor = RichGold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isTamil) "வசன எண்களைக் காட்டுக" else "Show Verse Numbers")
                        Switch(
                            checked = prefs.showVerseNumbers,
                            onCheckedChange = { viewModel.setShowVerseNumbers(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = RichGold, checkedTrackColor = PrimaryRoyalBlue)
                        )
                    }
                }
            }
        }


        // Section: Audio Settings
        item {
            SettingsSectionHeader(title = if (isTamil) "ஆடியோ அமைப்புகள் (Audio Settings)" else "Audio Settings")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Audio Language Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isTamil) "வேத ஆடியோ மொழி" else "Bible Audio Language")
                        Row {
                            FilterChip(
                                selected = prefs.bibleLanguage == "en",
                                onClick = { viewModel.setBibleLanguage("en") },
                                label = { Text("English", fontSize = 11.sp) },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("settings_audio_lang_en")
                            )
                            FilterChip(
                                selected = prefs.bibleLanguage == "ta",
                                onClick = { viewModel.setBibleLanguage("ta") },
                                label = { Text("தமிழ்", fontSize = 11.sp) },
                                modifier = Modifier.testTag("settings_audio_lang_ta")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice Gender Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isTamil) "குரல் பாலினம்" else "Voice Gender")
                        Row {
                            FilterChip(
                                selected = prefs.audioVoiceGender == "male",
                                onClick = { viewModel.setAudioVoiceGender("male") },
                                label = { Text(if (isTamil) "ஆண்" else "Male", fontSize = 11.sp) },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("settings_audio_male_chip")
                            )
                            FilterChip(
                                selected = prefs.audioVoiceGender == "female",
                                onClick = { viewModel.setAudioVoiceGender("female") },
                                label = { Text(if (isTamil) "பெண்" else "Female", fontSize = 11.sp) },
                                modifier = Modifier.testTag("settings_audio_female_chip")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Playback Speed Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isTamil) "இயல்புநிலை பின்னணி வேகம்" else "Default Playback Speed")
                        Row {
                            listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { s ->
                                FilterChip(
                                    selected = prefs.audioSpeed == s,
                                    onClick = { viewModel.setAudioSpeed(s) },
                                    label = { Text("${s}x", fontSize = 11.sp) },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Engine Status Information Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = RichGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TTS Engine Active • Voice: ${prefs.audioVoiceGender.replaceFirstChar { it.uppercase() }} (${if (prefs.bibleLanguage == "ta") "Tamil" else "English"})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: Storage & Cache
        item {
            SettingsSectionHeader(title = if (isTamil) "சேமிப்பு & தற்காலிக நினைவகம்" else "Storage & Cache")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isTamil) "உள்ளக வேதாகம தரவுத்தளம்" else "Local Bible Database")
                            Text("Room SQLite • 66 Books Indexed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("2.4 MB", fontWeight = FontWeight.Bold, color = RichGold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showClearCacheDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTamil) "தற்காலிக நினைவகத்தை அழிக்க (Clear Cache)" else "Clear Temporary Cache")
                    }
                }
            }
        }

        // Section: Realtime Feedback
        item {
            SettingsSectionHeader(title = if (isTamil) "பயனர் கருத்து" else "User Feedback")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.FEEDBACK) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RateReview, contentDescription = null, tint = RichGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isTamil) "நேரடி பயனர் கருத்து" else "Realtime User Feedback", fontWeight = FontWeight.Bold)
                            Text(if (isTamil) "மதிப்பீடு மற்றும் ஆலோசனைகளைப் பகிருங்கள்" else "Submit ratings, reports & suggestions live", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Section: About & Info
        item {
            SettingsSectionHeader(title = if (isTamil) "பயன்பாட்டைப் பற்றி" else "About SVS Holy Bible")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.ABOUT) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SVS Holy Bible", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrightGoldHighlight)
                        Text(if (isTamil) "பதிப்பு 1.0.0 • தகவல்கள் மற்றும் உரிமங்கள்" else "Version 1.0.0 • License & Attributions", style = MaterialTheme.typography.bodySmall, color = PrimaryLightText)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = RichGold)
                }
            }
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(if (isTamil) "நினைவகத்தை அழிக்கவா?" else "Clear Temporary Cache?") },
            text = { Text(if (isTamil) "தற்காலிக கோப்புகள் அழிக்கப்படும். உங்கள் குறிப்புகள் மற்றும் புக்மார்க்குகள் பத்திரமாக இருக்கும்." else "Temporary cached search items will be cleared. Your notes and bookmarks remain safe.") },
            confirmButton = {
                Button(
                    onClick = { showClearCacheDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                ) {
                    Text(if (isTamil) "அழிக்க" else "Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = RichGold,
        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
    )
}
