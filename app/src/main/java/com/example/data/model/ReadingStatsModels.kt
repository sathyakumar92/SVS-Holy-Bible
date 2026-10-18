package com.example.data.model

import com.example.ui.screens.search.MatchType
import com.example.ui.screens.search.SearchMode
import com.example.ui.screens.search.SearchSortOrder

data class AdvancedSearchParams(
    val query: String,
    val language: String, // "en", "ta", "both"
    val mode: SearchMode,
    val wholeWord: Boolean,
    val caseSensitive: Boolean = false,
    val sortOrder: SearchSortOrder = SearchSortOrder.RELEVANCE,
    val testamentFilter: String? = null,
    val bookFilter: String? = null,
    val fromChapter: Int = 0,
    val toChapter: Int = 0,
    val fromVerse: Int = 0,
    val toVerse: Int = 0
)

data class SearchResultItem(
    val verse: BibleVerse,
    val testamentName: String,
    val languageLabel: String,
    val matchType: MatchType,
    val relevanceScore: Int
)

data class ReadingStats(
    val totalChaptersRead: Int,
    val totalEstimatedVerses: Int,
    val totalReadingMinutes: Int,
    val uniqueDaysActive: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val oldTestamentChaptersRead: Int,
    val newTestamentChaptersRead: Int,
    val oldTestamentPercent: Float,
    val newTestamentPercent: Float,
    val totalBooksExplored: Int,
    val todayChaptersRead: Int,
    val dailyGoalChapters: Int,
    val weeklyDayCounts: List<Pair<String, Int>> // e.g. [("Mon", 2), ("Tue", 3), ...]
)
