package com.example.ui.screens.plans

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
import com.example.data.datasource.CanonicalBibleData
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPlanBuilderScreen(
    viewModel: BibleViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    var titleEn by remember { mutableStateOf("") }
    var titleTa by remember { mutableStateOf("") }
    var descriptionEn by remember { mutableStateOf("") }
    var durationDaysString by remember { mutableStateOf("30") }
    var selectedBookName by remember { mutableStateOf("Psalms") }
    var chaptersPerDayString by remember { mutableStateOf("2") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("custom_plan_builder_screen")
    ) {
        // App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (isTamil) "தனிப்பயன் வாசிப்பு திட்டம்" else "Build Custom Reading Plan",
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
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
                Text(
                    text = if (isTamil) "உங்கள் சொந்த வாசிப்பு அட்டவணையை உருவாக்குங்கள்" else "Design a tailored Scripture journey tailored to your goals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    label = { Text("Plan Title (English)") },
                    placeholder = { Text("e.g. My Psalms Journey") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = titleTa,
                    onValueChange = { titleTa = it },
                    label = { Text("திட்ட தலைப்பு (தமிழ்)") },
                    placeholder = { Text("எ.கா. என் சங்கீத தியான பயணம்") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = descriptionEn,
                    onValueChange = { descriptionEn = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = durationDaysString,
                        onValueChange = { durationDaysString = it },
                        label = { Text("Duration (Days)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = chaptersPerDayString,
                        onValueChange = { chaptersPerDayString = it },
                        label = { Text("Chs / Day") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            item {
                Text(
                    text = if (isTamil) "முக்கிய புத்தகம் (Focus Book)" else "Focus Bible Book",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Psalms", "Proverbs", "John", "Romans", "Genesis").forEach { bName ->
                        FilterChip(
                            selected = selectedBookName == bName,
                            onClick = { selectedBookName = bName },
                            label = { Text(bName) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val duration = durationDaysString.toIntOrNull()?.coerceIn(1, 365) ?: 30
                        val chs = chaptersPerDayString.toIntOrNull()?.coerceIn(1, 10) ?: 1
                        val book = CanonicalBibleData.findBookByName(selectedBookName) ?: CanonicalBibleData.allBooks[0]

                        val passages = (1..duration).map { day ->
                            val startCh = ((day - 1) * chs % book.chapterCount) + 1
                            "${book.englishName} $startCh"
                        }

                        coroutineScope.launch {
                            val planId = viewModel.advancedRepo.createCustomPlan(
                                titleEn = if (titleEn.isBlank()) "${book.englishName} $duration-Day Study" else titleEn.trim(),
                                titleTa = if (titleTa.isBlank()) "${book.tamilName} $duration நாள் ஆய்வு" else titleTa.trim(),
                                descriptionEn = if (descriptionEn.isBlank()) "Personal study plan reading through ${book.englishName}" else descriptionEn.trim(),
                                durationDays = duration,
                                passages = passages
                            )
                            viewModel.selectedSmartPlanId.value = planId
                            viewModel.navigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTamil) "திட்டத்தை உருவாக்கி சேமிக்க" else "Generate & Start Plan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
