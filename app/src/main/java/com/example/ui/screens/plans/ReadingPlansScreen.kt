package com.example.ui.screens.plans

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.DevotionalData
import com.example.data.model.PlanPortion
import com.example.data.model.ReadingPlan
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel

@Composable
fun ReadingPlansScreen(
    viewModel: BibleViewModel
) {
    val plans = DevotionalData.readingPlans
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reading_plans_screen")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isTamil) "வேத வாசிப்பு திட்டங்கள்" else "Bible Reading Plans",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RichGold
            )
            Text(
                text = if (isTamil) "ஒழுங்கான வேத வாசிப்பிற்கு திட்டங்களைத் தேர்ந்தெடுங்கள்" else "Structured spiritual journeys to build daily Bible reading habits",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(plans) { plan ->
                PlanOverviewCard(
                    plan = plan,
                    isTamil = isTamil,
                    onSelect = { viewModel.selectReadingPlan(plan) }
                )
            }
        }
    }
}

@Composable
fun PlanOverviewCard(
    plan: ReadingPlan,
    isTamil: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTamil) plan.titleTa else plan.titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryRoyalBlue
                ) {
                    Text(
                        text = "${plan.durationDays} ${if (isTamil) "நாட்கள்" else "Days"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RichGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isTamil) plan.descriptionTa else plan.descriptionEn,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryLightText
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = RichGold, contentColor = DeepNavyBackground),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isTamil) "திட்டத்தைத் தொடங்கு" else "Start Plan", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun PlanDetailScreen(
    viewModel: BibleViewModel
) {
    val plan by viewModel.selectedPlan.collectAsState()
    val progress by viewModel.planProgress.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.bibleLanguage == "ta"

    if (plan == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No plan selected")
        }
        return
    }

    val currentPlan = plan!!
    val completedDays = progress.filter { it.isCompleted }.map { it.dayNumber }.toSet()
    val progressPercentage = if (currentPlan.dailyPortions.isNotEmpty()) completedDays.size.toFloat() / currentPlan.dailyPortions.size.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("plan_detail_screen")
    ) {
        // Plan Header
        Surface(
            color = ElevatedBlueSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isTamil) currentPlan.titleTa else currentPlan.titleEn,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight
                        )
                        Text(
                            text = "${completedDays.size} / ${currentPlan.dailyPortions.size} ${if (isTamil) "நாட்கள் முடிவடைந்தது" else "days completed"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryLightText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = RichGold,
                    trackColor = DeepNavyBackground
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(currentPlan.dailyPortions) { portion ->
                val isDone = portion.dayNumber in completedDays
                PortionItemCard(
                    portion = portion,
                    isCompleted = isDone,
                    isTamil = isTamil,
                    onToggle = { done ->
                        viewModel.togglePlanPortion(currentPlan.id, portion.dayNumber, done)
                    },
                    onOpenBible = {
                        val book = CanonicalBibleData.findBookById(portion.bookId) ?: CanonicalBibleData.allBooks[0]
                        viewModel.openReader(book, portion.chapter)
                    }
                )
            }
        }
    }
}

@Composable
fun PortionItemCard(
    portion: PlanPortion,
    isCompleted: Boolean,
    isTamil: Boolean,
    onToggle: (Boolean) -> Unit,
    onOpenBible: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggle(it) },
                colors = CheckboxDefaults.colors(checkedColor = RichGold, checkmarkColor = DeepNavyBackground)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${if (isTamil) "நாள்" else "Day"} ${portion.dayNumber}: ${if (isTamil) portion.titleTa else portion.titleEn}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) RichGold else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isTamil) portion.readingRefTa else portion.readingRefEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryRoyalBlue,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "“${if (isTamil) portion.keyVerseTa else portion.keyVerseEn}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onOpenBible) {
                Icon(Icons.Default.MenuBook, contentDescription = "Read", tint = RichGold)
            }
        }
    }
}
