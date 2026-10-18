package com.example.ui.screens.plans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@Composable
fun SmartPlansScreen(
    viewModel: BibleViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    val plans by viewModel.advancedRepo.allSmartPlans.collectAsState(initial = emptyList())
    val selectedPlanId by viewModel.selectedSmartPlanId.collectAsState()

    val currentPlan = plans.firstOrNull { it.id == selectedPlanId } ?: plans.firstOrNull()

    val currentPlanId = currentPlan?.id
    val assignmentsFlow: kotlinx.coroutines.flow.Flow<List<SmartPlanAssignment>> = remember(currentPlanId) {
        if (currentPlanId != null) {
            viewModel.advancedRepo.getAssignmentsForPlan(currentPlanId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }
    val assignments by assignmentsFlow.collectAsState(initial = emptyList())

    val completionsFlow: kotlinx.coroutines.flow.Flow<List<PlanDayCompletion>> = remember(currentPlanId) {
        if (currentPlanId != null) {
            viewModel.advancedRepo.getCompletionsForPlan(currentPlanId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }
    val completions by completionsFlow.collectAsState(initial = emptyList())

    val completedDays = completions.filter { it.isCompleted }.map { it.dayNumber }.toSet()
    val progressPct = if (currentPlan != null && currentPlan.durationDays > 0) {
        (completedDays.size.toFloat() / currentPlan.durationDays.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Calculate streak from unique local dates in completions
    val uniqueDates = completions.map { it.localDateString }.distinct().size
    val currentStreak = uniqueDates

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.CUSTOM_PLAN_BUILDER) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (isTamil) "தனிப்பயன் திட்டம்" else "Build Custom Plan", fontWeight = FontWeight.Bold) },
                containerColor = RichGold,
                contentColor = DeepNavyBackground,
                modifier = Modifier.testTag("custom_plan_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("smart_plans_screen")
        ) {
            // Header & Plans Selector
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTamil) "ஸ்மார்ட் வேத வாசிப்பு திட்டங்கள்" else "Smart Bible Reading Plans",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RichGold
                )
                Text(
                    text = if (isTamil) "அன்றாட வாசிப்பு பகுதிகள், முன்னேற்றம் & தொடர் நாட்கள் (Streaks)" else "Track daily Scripture portions, progress percentages & reading streaks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Streak & Progress Banner Card
            if (currentPlan != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
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
                                    text = if (isTamil) currentPlan.titleTa else currentPlan.titleEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGoldHighlight
                                )
                                Text(
                                    text = "${completedDays.size} / ${currentPlan.durationDays} ${if (isTamil) "நாட்கள் முடிவடைந்தன" else "days completed"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryLightText
                                )
                            }

                            // Streak Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PrimaryRoyalBlue,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = BrightGoldHighlight, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$currentStreak ${if (isTamil) "நாட்கள்" else "Days Streak"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BrightGoldHighlight
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progressPct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = RichGold,
                            trackColor = DeepNavyBackground
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls: Start, Pause, Restart
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!currentPlan.isStarted) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.advancedRepo.startPlan(currentPlan.id)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(if (isTamil) "திட்டத்தைத் தொடங்கு" else "Start Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.advancedRepo.togglePlanPause(currentPlan.id)
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (currentPlan.isPaused) (if (isTamil) "மீண்டும் தொடர்" else "Resume") else (if (isTamil) "நிறுத்திவை" else "Pause"),
                                            fontSize = 11.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.advancedRepo.restartPlan(currentPlan.id)
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(if (isTamil) "மீண்டும் தொடங்கு" else "Restart", fontSize = 11.sp)
                                    }
                                }
                            }

                            // Plan Switcher Dropdown button
                            Text(
                                text = "${(progressPct * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }
                    }
                }
            }

            // Available Plans Tabs
            ScrollableTabRow(
                selectedTabIndex = plans.indexOfFirst { it.id == currentPlan?.id }.coerceAtLeast(0),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = RichGold,
                edgePadding = 16.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                plans.forEach { plan ->
                    Tab(
                        selected = plan.id == currentPlan?.id,
                        onClick = { viewModel.selectedSmartPlanId.value = plan.id },
                        text = {
                            Text(
                                text = if (isTamil) plan.titleTa else plan.titleEn,
                                fontWeight = if (plan.id == currentPlan?.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Assignments List
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (assignments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isTamil) "அன்றாட வாசிப்பு அட்டவணை" else "Reading Portion Schedule",
                                    fontWeight = FontWeight.Bold,
                                    color = RichGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isTamil) currentPlan?.descriptionTa ?: "" else currentPlan?.descriptionEn ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(assignments) { item ->
                        val isDone = item.dayNumber in completedDays
                        SmartAssignmentRow(
                            assignment = item,
                            isCompleted = isDone,
                            isTamil = isTamil,
                            onToggle = { done ->
                                coroutineScope.launch {
                                    viewModel.advancedRepo.toggleDayCompletion(item.planId, item.dayNumber, done)
                                }
                            },
                            onOpenBible = {
                                // Parse first book & chapter from passagesJson e.g. "Matthew 1" or "John 3"
                                val tokens = item.passagesJson.split(" ")
                                val bookName = tokens.getOrNull(0) ?: "John"
                                val chapter = tokens.getOrNull(1)?.toIntOrNull() ?: 1
                                val book = CanonicalBibleData.findBookByName(bookName) ?: CanonicalBibleData.allBooks[0]
                                viewModel.openReader(book, chapter)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmartAssignmentRow(
    assignment: SmartPlanAssignment,
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
                .padding(12.dp),
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
                    text = if (isTamil) assignment.titleTa else assignment.titleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) RichGold else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = assignment.passagesJson,
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryRoyalBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = onOpenBible) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Read passage",
                    tint = RichGold
                )
            }
        }
    }
}
