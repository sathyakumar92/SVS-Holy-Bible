package com.example.ui.screens.prayer

import android.content.Intent
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
import com.example.data.model.PrayerEntry
import com.example.data.model.PrayerStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerEditorScreen(
    viewModel: BibleViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prayerId by viewModel.selectedPrayerId.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var status by remember { mutableStateOf(PrayerStatus.ACTIVE) }
    var attachedScriptureRef by remember { mutableStateOf("") }
    var answeredTestimony by remember { mutableStateOf("") }
    var newUpdateText by remember { mutableStateOf("") }

    val updates = if (prayerId != null && prayerId != 0L) {
        viewModel.advancedRepo.getUpdatesForPrayer(prayerId!!).collectAsState(initial = emptyList()).value
    } else emptyList()

    LaunchedEffect(prayerId) {
        if (prayerId != null && prayerId != 0L) {
            val existing = viewModel.advancedRepo.getPrayerById(prayerId!!)
            if (existing != null) {
                title = existing.title
                description = existing.description
                category = existing.category
                status = existing.status
                attachedScriptureRef = existing.attachedScriptureRef
                answeredTestimony = existing.answeredTestimony
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_editor_screen")
    ) {
        // App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (prayerId == null || prayerId == 0L) (if (isTamil) "புதிய ஜெப விண்ணப்பம்" else "New Prayer Request")
                           else (if (isTamil) "ஜெபக் குறிப்பு திருத்தம்" else "Edit Prayer Request"),
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
                // Share action
                IconButton(
                    onClick = {
                        val shareText = buildString {
                            appendLine("=== Prayer: $title ===")
                            appendLine(description)
                            if (status == PrayerStatus.ANSWERED && answeredTestimony.isNotBlank()) {
                                appendLine("\n[Praise Testimony]: $answeredTestimony")
                            }
                            if (attachedScriptureRef.isNotBlank()) {
                                appendLine("Scripture: $attachedScriptureRef")
                            }
                            appendLine("\nShared via SVS Holy Bible")
                        }
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Prayer"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = BrightGoldHighlight)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.advancedRepo.savePrayer(
                                PrayerEntry(
                                    id = prayerId ?: 0L,
                                    title = if (title.isBlank()) (if (isTamil) "தலைப்பற்ற ஜெபம்" else "Untitled Prayer") else title.trim(),
                                    description = description.trim(),
                                    category = category,
                                    status = status,
                                    attachedScriptureRef = attachedScriptureRef.trim(),
                                    answeredTestimony = answeredTestimony.trim(),
                                    answeredDateTimestamp = if (status == PrayerStatus.ANSWERED) System.currentTimeMillis() else null
                                )
                            )
                            viewModel.navigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RichGold, contentColor = DeepNavyBackground),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(if (isTamil) "சேமி" else "Save", fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ElevatedBlueSurface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isTamil) "ஜெப விண்ணப்ப தலைப்பு" else "Prayer Title") },
                    placeholder = { Text(if (isTamil) "எ.கா. குடும்ப சமாதானத்திற்காய்" else "e.g. Prayer for family peace") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isTamil) "ஜெப விண்ணப்ப விபரம்" else "Prayer Details & Reflection") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Category Selection
            item {
                Text(
                    text = if (isTamil) "பிரிவு (Category)" else "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Personal", "Family", "Health", "Ministry", "Gratitude").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Status Selection
            item {
                Text(
                    text = if (isTamil) "ஜெப நிலை (Status)" else "Prayer Status",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PrayerStatus.values().forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = {
                                Text(
                                    text = when (st) {
                                        PrayerStatus.ACTIVE -> if (isTamil) "செயலில்" else "Active"
                                        PrayerStatus.ONGOING -> if (isTamil) "தொடர் ஜெபம்" else "Ongoing"
                                        PrayerStatus.ANSWERED -> if (isTamil) "பதில் பெற்றது" else "Answered"
                                        PrayerStatus.ARCHIVED -> if (isTamil) "காப்பகம்" else "Archived"
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
            }

            // Answered Praise Testimony Input (shown when status is ANSWERED)
            if (status == PrayerStatus.ANSWERED) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Celebration, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTamil) "பதில் பெற்ற சாட்சி (Praise Testimony)" else "Answered Prayer Testimony",
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = answeredTestimony,
                                onValueChange = { answeredTestimony = it },
                                placeholder = { Text(if (isTamil) "தேவன் செய்த நன்மைகளையும் சாட்சியையும் இங்கு எழுதவும்..." else "Record how God answered and glorified His name...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 90.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = attachedScriptureRef,
                    onValueChange = { attachedScriptureRef = it },
                    label = { Text(if (isTamil) "இணைக்கப்பட்ட வேத வசனம்" else "Scripture Promise / Reference") },
                    placeholder = { Text("e.g. Philippians 4:6-7") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Dated Updates Timeline
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = if (isTamil) "ஜெப முன்னேற்றப் பதிவுகள் (Prayer Updates Timeline)" else "Prayer Updates Timeline (${updates.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newUpdateText,
                        onValueChange = { newUpdateText = it },
                        placeholder = { Text(if (isTamil) "இன்றைய புதுப்பிப்பைச் சேர்க்க..." else "Add dated progress update...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newUpdateText.isNotBlank()) {
                                coroutineScope.launch {
                                    val activePrayerId = prayerId ?: viewModel.advancedRepo.savePrayer(
                                        PrayerEntry(
                                            title = if (title.isBlank()) "Prayer Request" else title,
                                            description = description
                                        )
                                    )
                                    viewModel.selectedPrayerId.value = activePrayerId
                                    viewModel.advancedRepo.addPrayerUpdate(activePrayerId, newUpdateText.trim())
                                    newUpdateText = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isTamil) "+ சேர்" else "+ Add")
                    }
                }
            }

            items(updates) { upd ->
                val dateFmt = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(upd.timestamp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = dateFmt, style = MaterialTheme.typography.labelSmall, color = RichGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = upd.updateText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
