package com.example.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.ReadingHistory
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingCalendarScreen(
    viewModel: BibleViewModel
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"

    val calendarYear by viewModel.calendarSelectedYear.collectAsState()
    val calendarMonth by viewModel.calendarSelectedMonth.collectAsState()
    val calendarDay by viewModel.calendarSelectedDay.collectAsState()

    val stats by viewModel.readingStats.collectAsState()
    val selectedDayReadings by viewModel.selectedDayReadings.collectAsState()
    val daysWithReading by viewModel.daysWithReadingInSelectedMonth.collectAsState()
    val allHistory by viewModel.allReadingHistory.collectAsState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var selectedStatsTab by remember { mutableStateOf(0) } // 0 = Overview, 1 = Book Progress

    // Month formatting
    val monthName = remember(calendarMonth, calendarYear, isTamil) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, calendarMonth)
        cal.set(Calendar.YEAR, calendarYear)
        if (isTamil) {
            val tamilMonths = listOf("தை (ஜனவரி)", "மாசி (பிப்ரவரி)", "பங்குனி (மார்ச்)", "சித்திரை (ஏப்ரல்)", "வைகாசி (மே)", "ஆனி (ஜூன்)", "ஆடி (ஜூலை)", "ஆவணி (ஆகஸ்ட்)", "புரட்டாசி (செப்டம்பர்)", "ஐப்பசி (அக்டோபர்)", "கார்த்திகை (நவம்பர்)", "மார்கழி (டிசம்பர்)")
            val engMonths = listOf("ஜனவரி", "பிப்ரவரி", "மார்ச்", "ஏப்ரல்", "மே", "ஜூன்", "ஜூலை", "ஆகஸ்ட்", "செப்டம்பர்", "அக்டோபர்", "நவம்பர்", "டிசம்பர்")
            "${engMonths[calendarMonth]} $calendarYear"
        } else {
            SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
        }
    }

    // Days in current selected month
    val daysInMonth = remember(calendarYear, calendarMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, calendarYear)
        cal.set(Calendar.MONTH, calendarMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // First day of week offset (Sunday = 1, Monday = 2, ...)
    val firstDayOffset = remember(calendarYear, calendarMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, calendarYear)
        cal.set(Calendar.MONTH, calendarMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    }

    val todayCal = Calendar.getInstance()
    val isCurrentMonthAndYear = (todayCal.get(Calendar.YEAR) == calendarYear && todayCal.get(Calendar.MONTH) == calendarMonth)
    val todayDayNumber = todayCal.get(Calendar.DAY_OF_MONTH)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = RichGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTamil) "வாசிப்பு நாட்காட்டி & புள்ளிவிவரம்" else "Reading Calendar & Stats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearHistoryDialog = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear History", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("reading_calendar_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Streak & Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Current Streak Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔥", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stats.currentStreak} ${if (isTamil) "நாட்கள்" else "Days"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Text(
                                text = if (isTamil) "தற்போதைய தொடர்" else "Current Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryLightText
                            )
                        }
                    }

                    // Best Streak Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stats.bestStreak} ${if (isTamil) "நாட்கள்" else "Days"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                            Text(
                                text = if (isTamil) "உயர்ந்த தொடர்" else "Best Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryLightText
                            )
                        }
                    }

                    // Total Active Days Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📅", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stats.uniqueDaysActive} ${if (isTamil) "நாட்கள்" else "Days"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isTamil) "மொத்த நாட்கள்" else "Active Days",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryLightText
                            )
                        }
                    }
                }
            }

            // 2. Daily Reading Goal Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                    text = if (isTamil) "இன்றைய வாசிப்பு இலக்கு" else "Daily Reading Goal",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${stats.todayChaptersRead} / ${stats.dailyGoalChapters} ${if (isTamil) "அதிகாரங்கள் வாசிக்கப்பட்டன" else "Chapters completed today"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (stats.todayChaptersRead >= stats.dailyGoalChapters) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PrimaryRoyalBlue
                                ) {
                                    Text(
                                        text = if (isTamil) "இலக்கு எட்டப்பட்டது! 🎉" else "Goal Met! 🎉",
                                        color = BrightGoldHighlight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val progressFraction = (stats.todayChaptersRead.toFloat() / stats.dailyGoalChapters.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = RichGold,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Target Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTamil) "இலக்கை மாற்று:" else "Change Target:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1, 2, 3, 5).forEach { goal ->
                                    FilterChip(
                                        selected = stats.dailyGoalChapters == goal,
                                        onClick = { viewModel.setDailyReadingGoal(goal) },
                                        label = { Text("${goal} Ch", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryRoyalBlue,
                                            selectedLabelColor = BrightGoldHighlight
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Interactive Monthly Calendar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Month Navigation Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.previousCalendarMonth() }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = RichGold)
                            }

                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(onClick = { viewModel.nextCalendarMonth() }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = RichGold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Days of week header
                        val weekDays = if (isTamil) {
                            listOf("ஞா", "திங்", "செவ்", "புத", "வியா", "வெள்", "சனி")
                        } else {
                            listOf("S", "M", "T", "W", "T", "F", "S")
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            weekDays.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Grid Days
                        val totalCells = firstDayOffset + daysInMonth
                        val rows = (totalCells + 6) / 7

                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (c in 0 until 7) {
                                    val cellIndex = r * 7 + c
                                    val dayNum = cellIndex - firstDayOffset + 1

                                    if (dayNum in 1..daysInMonth) {
                                        val isToday = isCurrentMonthAndYear && dayNum == todayDayNumber
                                        val isSelected = dayNum == calendarDay
                                        val hasReading = dayNum in daysWithReading

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSelected -> PrimaryRoyalBlue
                                                        isToday -> PrimaryRoyalBlue.copy(alpha = 0.2f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .then(
                                                    if (isToday && !isSelected) {
                                                        Modifier.border(1.5.dp, RichGold, CircleShape)
                                                    } else Modifier
                                                )
                                                .clickable {
                                                    viewModel.selectCalendarDate(calendarYear, calendarMonth, dayNum)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected || isToday || hasReading) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> BrightGoldHighlight
                                                        isToday -> RichGold
                                                        hasReading -> MaterialTheme.colorScheme.onSurface
                                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                    }
                                                )
                                                if (hasReading) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .background(RichGold, CircleShape)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Reading Log for Selected Day
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (isTamil) "வாசிப்பு பதிவு" else "Reading Log"} — $calendarDay $monthName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RichGold
                            )
                            Text(
                                text = "${selectedDayReadings.size} ${if (isTamil) "பதிவுகள்" else "readings"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (selectedDayReadings.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isTamil) "இந்த நாளில் வாசிப்பு பதிவு ஏதுமில்லை." else "No reading recorded for this day.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val book = CanonicalBibleData.findBookById("JHN") ?: CanonicalBibleData.allBooks[0]
                                        viewModel.openReader(book, 1)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                                ) {
                                    Text(if (isTamil) "இன்று வாசிக்க ஆரம்பியுங்கள் →" else "Start Reading Today →")
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedDayReadings.forEach { log ->
                                    val timeString = remember(log.timestamp) {
                                        SimpleDateFormat("hh:mm a", Locale.US).format(Date(log.timestamp))
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "${log.bookName} ${log.chapterNumber}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = timeString,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    val book = CanonicalBibleData.findBookById(log.bookId)
                                                        ?: CanonicalBibleData.findBookByName(log.bookName)
                                                        ?: CanonicalBibleData.allBooks[0]
                                                    viewModel.openReader(book, log.chapterNumber)
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(if (isTamil) "வாசிக்க" else "Read", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Tabs for Statistics Breakdown
            item {
                TabRow(
                    selectedTabIndex = selectedStatsTab,
                    containerColor = Color.Transparent,
                    contentColor = RichGold
                ) {
                    Tab(
                        selected = selectedStatsTab == 0,
                        onClick = { selectedStatsTab = 0 },
                        text = { Text(if (isTamil) "ஒட்டுமொத்த விவரங்கள்" else "Overall Stats", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedStatsTab == 1,
                        onClick = { selectedStatsTab = 1 },
                        text = { Text(if (isTamil) "புத்தகங்களின் முன்னேற்றம்" else "Books Progress", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedStatsTab == 0) {
                // Statistics: Testament Breakdown & Weekly Chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isTamil) "ஏற்பாடுகள் வாரியான முன்னேற்றம்" else "Testament Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Old Testament Progress
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isTamil) "பழைய ஏற்பாடு (Old Testament)" else "Old Testament",
                                    fontSize = 13.sp,
                                    color = PrimaryLightText
                                )
                                Text(
                                    text = "${stats.oldTestamentChaptersRead} / 929 ${if (isTamil) "அதிகாரங்கள்" else "chs"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RichGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { stats.oldTestamentPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = RichGold,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // New Testament Progress
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isTamil) "புதிய ஏற்பாடு (New Testament)" else "New Testament",
                                    fontSize = 13.sp,
                                    color = PrimaryLightText
                                )
                                Text(
                                    text = "${stats.newTestamentChaptersRead} / 260 ${if (isTamil) "அதிகாரங்கள்" else "chs"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RichGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { stats.newTestamentPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BrightGoldHighlight,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // Weekly Reading Activity Bar Chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTamil) "கடந்த 7 நாட்கள் செயல்பாடு" else "Last 7 Days Activity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${stats.totalReadingMinutes} ${if (isTamil) "நிமிடங்கள்" else "mins read"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RichGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val maxCount = stats.weeklyDayCounts.maxOfOrNull { it.second }?.coerceAtLeast(4) ?: 4

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                stats.weeklyDayCounts.forEach { (dayLabel, count) ->
                                    val barHeight = ((count.toFloat() / maxCount) * 80).coerceAtLeast(6f)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom
                                    ) {
                                        Text(
                                            text = if (count > 0) count.toString() else "",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RichGold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height(barHeight.dp)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(if (count > 0) PrimaryRoyalBlue else MaterialTheme.colorScheme.surfaceVariant)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = dayLabel,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Book-by-Book Matrix
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${if (isTamil) "66 புத்தகங்களின் வாசிப்பு நிலை" else "66 Books Exploration Matrix"} (${stats.totalBooksExplored}/66)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            CanonicalBibleData.allBooks.forEach { book ->
                                val chaptersReadInBook = remember(allHistory, book.id) {
                                    allHistory.filter { it.bookId == book.id }
                                        .distinctBy { it.chapterNumber }
                                        .size
                                }
                                val percent = (chaptersReadInBook.toFloat() / book.chapterCount.toFloat()).coerceIn(0f, 1f)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openReader(book, 1) }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isTamil) book.tamilName else book.englishName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (chaptersReadInBook >= book.chapterCount) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("✓", color = BrightGoldHighlight, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LinearProgressIndicator(
                                            progress = { percent },
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = RichGold,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "$chaptersReadInBook / ${book.chapterCount}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (chaptersReadInBook > 0) RichGold else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for clearing history
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(if (isTamil) "வரலாற்றை அழிக்கவா?" else "Clear Reading History?") },
            text = { Text(if (isTamil) "உங்கள் அனைத்து வாசிப்பு பதிவுகளும் நாட்காட்டி விவரங்களும் மீட்டமைக்கப்படும். தொடரவா?" else "This will reset your reading history logs and streak calculations. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllReadingHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(if (isTamil) "அழி" else "Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}
