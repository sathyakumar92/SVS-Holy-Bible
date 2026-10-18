package com.example.data.repository

import com.example.data.datasource.AdvancedSeedData
import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.ScriptureSeedData
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BibleRepository(private val database: AppDatabase) {

    private val bibleDao = database.bibleDao()
    private val bookmarkDao = database.bookmarkDao()
    private val highlightDao = database.highlightDao()
    private val noteDao = database.noteDao()
    private val historyDao = database.historyDao()
    private val planDao = database.planDao()
    private val quizScoreDao = database.quizScoreDao()
    private val feedbackDao = database.feedbackDao()

    suspend fun ensureDatabaseInitialized() = withContext(Dispatchers.IO) {
        val initial = ScriptureSeedData.getInitialVerses().distinctBy {
            "${it.editionId}_${it.languageCode}_${it.bookId}_${it.chapterNumber}_${it.verseNumber}"
        }
        bibleDao.insertVerses(initial)
        val count = bibleDao.getVerseCount()
        if (count <= initial.size) {
            AdvancedSeedData.seedInitialData(database)
        }
    }

    fun getAllBooks(): List<BibleBook> = CanonicalBibleData.allBooks

    fun getOldTestamentBooks(): List<BibleBook> = CanonicalBibleData.oldTestamentBooks

    fun getNewTestamentBooks(): List<BibleBook> = CanonicalBibleData.newTestamentBooks

    fun getBookById(id: String): BibleBook? = CanonicalBibleData.findBookById(id)

    fun getVersesForChapter(bookId: String, chapter: Int, languageCode: String): Flow<List<BibleVerse>> {
        return bibleDao.getVersesForChapter(bookId, chapter, languageCode).map { dbList ->
            val distinct = dbList.distinctBy { it.verseNumber }.sortedBy { it.verseNumber }
            val canonical = ScriptureSeedData.getCanonicalVersesForChapter(bookId, chapter, languageCode)
            if (distinct.isNotEmpty() && distinct.size >= canonical.size) {
                distinct
            } else if (canonical.isNotEmpty()) {
                val merged = (distinct + canonical).distinctBy { it.verseNumber }.sortedBy { it.verseNumber }
                if (merged.size > distinct.size) {
                    CoroutineScope(Dispatchers.IO).launch {
                        bibleDao.insertVerses(merged)
                    }
                }
                merged
            } else {
                if (distinct.isEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        fetchChapterOnline(bookId, chapter)
                    }
                }
                distinct
            }
        }
    }

    suspend fun getVersesDirect(bookId: String, chapter: Int, languageCode: String): List<BibleVerse> = withContext(Dispatchers.IO) {
        val dbList = bibleDao.getVersesForChapterDirect(bookId, chapter, languageCode).distinctBy { it.verseNumber }.sortedBy { it.verseNumber }
        val canonical = ScriptureSeedData.getCanonicalVersesForChapter(bookId, chapter, languageCode)
        if (dbList.isNotEmpty() && dbList.size >= canonical.size) {
            dbList
        } else if (canonical.isNotEmpty()) {
            val merged = (dbList + canonical).distinctBy { it.verseNumber }.sortedBy { it.verseNumber }
            if (merged.size > dbList.size) {
                bibleDao.insertVerses(merged)
            }
            merged
        } else {
            val fetched = fetchChapterOnline(bookId, chapter)
            val filtered = fetched.filter { it.languageCode == languageCode }
            if (filtered.isNotEmpty()) filtered else dbList
        }
    }

    suspend fun fetchChapterOnline(bookId: String, chapter: Int): List<BibleVerse> = withContext(Dispatchers.IO) {
        val book = CanonicalBibleData.findBookById(bookId) ?: return@withContext emptyList()
        try {
            val bookNameEn = book.englishName
            val taUrlName = java.net.URLEncoder.encode(bookNameEn, "UTF-8")
            val enUrlName = bookNameEn.replace(" ", "")

            val taUrl = java.net.URL("https://raw.githubusercontent.com/aruljohn/Bible-tamil/master/$taUrlName.json")
            val enUrl = java.net.URL("https://raw.githubusercontent.com/aruljohn/Bible-kjv/master/$enUrlName.json")

            val taConn = (taUrl.openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            val enConn = (enUrl.openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }

            if (taConn.responseCode != 200 || enConn.responseCode != 200) {
                return@withContext emptyList()
            }

            val taJson = org.json.JSONObject(taConn.inputStream.bufferedReader().use { it.readText() })
            val enJson = org.json.JSONObject(enConn.inputStream.bufferedReader().use { it.readText() })

            val taChapters = taJson.getJSONArray("chapters")
            val enChapters = enJson.getJSONArray("chapters")

            var taChapterObj: org.json.JSONObject? = null
            for (i in 0 until taChapters.length()) {
                val obj = taChapters.getJSONObject(i)
                if (obj.optString("chapter") == chapter.toString()) {
                    taChapterObj = obj
                    break
                }
            }

            var enChapterObj: org.json.JSONObject? = null
            for (i in 0 until enChapters.length()) {
                val obj = enChapters.getJSONObject(i)
                if (obj.optString("chapter") == chapter.toString()) {
                    enChapterObj = obj
                    break
                }
            }

            if (taChapterObj == null || enChapterObj == null) return@withContext emptyList()

            val taVerses = taChapterObj.getJSONArray("verses")
            val enVerses = enChapterObj.getJSONArray("verses")

            val enMap = mutableMapOf<Int, String>()
            for (i in 0 until enVerses.length()) {
                val vObj = enVerses.getJSONObject(i)
                val vNum = vObj.optString("verse").toIntOrNull() ?: (i + 1)
                enMap[vNum] = vObj.optString("text")
            }

            val resultList = mutableListOf<BibleVerse>()
            for (i in 0 until taVerses.length()) {
                val vObj = taVerses.getJSONObject(i)
                val vNum = vObj.optString("verse").toIntOrNull() ?: (i + 1)
                val textTa = vObj.optString("text")
                val textEn = enMap[vNum] ?: ""

                if (textTa.isNotBlank()) {
                    resultList.add(
                        BibleVerse(
                            editionId = "TAMIL_BSI",
                            languageCode = "ta",
                            bookId = book.id,
                            bookName = book.tamilName,
                            chapterNumber = chapter,
                            verseNumber = vNum,
                            verseText = textTa,
                            normalizedText = textTa.lowercase()
                        )
                    )
                }
                if (textEn.isNotBlank()) {
                    resultList.add(
                        BibleVerse(
                            editionId = "KJV",
                            languageCode = "en",
                            bookId = book.id,
                            bookName = book.englishName,
                            chapterNumber = chapter,
                            verseNumber = vNum,
                            verseText = textEn,
                            normalizedText = textEn.lowercase()
                        )
                    )
                }
            }

            if (resultList.isNotEmpty()) {
                bibleDao.insertVerses(resultList)
            }
            resultList
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Realtime User Feedback
    fun getAllFeedbacks(): Flow<List<AppFeedback>> = feedbackDao.getAllFeedbacks()

    suspend fun submitFeedback(rating: Int, category: String, name: String, email: String, comments: String): AppFeedback = withContext(Dispatchers.IO) {
        val feedback = AppFeedback(
            rating = rating,
            category = category,
            userName = name.trim(),
            userEmail = email.trim(),
            comments = comments.trim()
        )
        val id = feedbackDao.insertFeedback(feedback)
        feedback.copy(id = id)
    }

    suspend fun deleteFeedback(feedback: AppFeedback) = withContext(Dispatchers.IO) {
        feedbackDao.deleteFeedback(feedback)
    }

    suspend fun getSingleVerse(bookId: String, chapter: Int, verse: Int, languageCode: String): BibleVerse? {
        return bibleDao.getSingleVerse(bookId, chapter, verse, languageCode)
    }

    fun searchVerses(query: String, languageCode: String, testamentFilter: String?, bookFilter: String?): Flow<List<BibleVerse>> {
        val normalized = java.text.Normalizer.normalize(query.trim().lowercase(), java.text.Normalizer.Form.NFC)
        return bibleDao.searchVerses(normalized, languageCode, testamentFilter, bookFilter).map { list ->
            list.distinctBy { "${it.bookId}_${it.chapterNumber}_${it.verseNumber}" }
        }
    }

    suspend fun searchVersesAdvanced(
        query: String,
        languageCode: String,
        searchMode: com.example.ui.screens.search.SearchMode,
        wholeWord: Boolean,
        caseSensitive: Boolean = false,
        sortOrder: com.example.ui.screens.search.SearchSortOrder = com.example.ui.screens.search.SearchSortOrder.RELEVANCE,
        testamentFilter: String? = null,
        bookFilter: String? = null,
        fromChapter: Int = 0,
        toChapter: Int = 0,
        fromVerse: Int = 0,
        toVerse: Int = 0
    ): Pair<List<com.example.data.model.SearchResultItem>, com.example.util.ReferenceParseResult> = withContext(Dispatchers.IO) {
        val rawTrimmed = query.trim()
        val normalizedQuery = java.text.Normalizer.normalize(rawTrimmed, java.text.Normalizer.Form.NFC)

        // Check if Reference Search or reference syntax
        val refResult = com.example.util.BibleReferenceParser.parse(rawTrimmed)
        val directRefResults = mutableListOf<com.example.data.model.SearchResultItem>()

        if (refResult is com.example.util.ReferenceParseResult.Success) {
            val ref = refResult.reference
            val effectiveLangs = if (languageCode == "both") listOf("en", "ta") else listOf(languageCode)
            for (lang in effectiveLangs) {
                var verses = bibleDao.getVersesForReference(
                    bookId = ref.book.id,
                    chapter = ref.chapter,
                    verseNumber = ref.verse ?: 0,
                    languageCode = lang
                )
                if (verses.isEmpty()) {
                    val canonical = ScriptureSeedData.getCanonicalVersesForChapter(ref.book.id, ref.chapter, lang)
                    if (canonical.isNotEmpty()) {
                        bibleDao.insertVerses(canonical)
                        verses = if (ref.verse != null) {
                            canonical.filter { it.verseNumber == ref.verse }
                        } else {
                            canonical
                        }
                    }
                }
                if (ref.endVerse != null && ref.verse != null) {
                    verses = verses.filter { it.verseNumber in ref.verse..ref.endVerse }
                }
                verses.forEach { v ->
                    val isOt = CanonicalBibleData.findBookById(v.bookId)?.testament == com.example.data.model.Testament.OLD_TESTAMENT
                    val tName = if (v.languageCode == "ta") (if (isOt) "பழைய ஏற்பாடு" else "புதிய ஏற்பாடு") else (if (isOt) "Old Testament" else "New Testament")
                    val lLabel = if (v.languageCode == "ta") "தமிழ் (BSI)" else "English (KJV)"
                    directRefResults.add(
                        com.example.data.model.SearchResultItem(
                            verse = v,
                            testamentName = tName,
                            languageLabel = lLabel,
                            matchType = com.example.ui.screens.search.MatchType.REFERENCE,
                            relevanceScore = 2000
                        )
                    )
                }
            }

            if (searchMode == com.example.ui.screens.search.SearchMode.REFERENCE) {
                return@withContext Pair(directRefResults, refResult)
            }
        } else if (searchMode == com.example.ui.screens.search.SearchMode.REFERENCE && refResult is com.example.util.ReferenceParseResult.Invalid) {
            return@withContext Pair(emptyList(), refResult)
        }

        if (normalizedQuery.length < 2 && directRefResults.isEmpty()) {
            return@withContext Pair(emptyList(), refResult)
        }

        // Fetch candidate verses within filter scope
        val candidates = bibleDao.getVersesForAdvancedSearch(
            languageCode = languageCode,
            testamentFilter = testamentFilter,
            bookFilter = bookFilter,
            fromChapter = fromChapter,
            toChapter = toChapter,
            fromVerse = fromVerse,
            toVerse = toVerse
        )

        val cleanQuery = normalizedQuery.replace(Regex("""[^\p{L}\p{M}\d\s]"""), " ").trim()
        val tokens = cleanQuery.split(Regex("""\s+""")).filter { it.isNotBlank() }

        val bookOrderMap = CanonicalBibleData.allBooks.associate { it.id to it.order }

        val matchedItems = candidates.mapNotNull { verse ->
            val vTextNorm = java.text.Normalizer.normalize(verse.verseText, java.text.Normalizer.Form.NFC)
            val vTextClean = vTextNorm.replace(Regex("""[^\p{L}\p{M}\d\s]"""), " ")

            val compareVerseText = if (caseSensitive && verse.languageCode == "en") vTextClean else vTextClean.lowercase()
            val compareQueryText = if (caseSensitive && verse.languageCode == "en") cleanQuery else cleanQuery.lowercase()
            val compareTokens = if (caseSensitive && verse.languageCode == "en") tokens else tokens.map { it.lowercase() }

            val isExactPhrase = compareQueryText.isNotBlank() && compareVerseText.contains(compareQueryText)
            
            // Check tokens match
            val matchedTokensCount = compareTokens.count { token ->
                if (wholeWord) {
                    val boundaryRegex = if (caseSensitive) Regex("""\b${Regex.escape(token)}\b""") else Regex("""(?i)\b${Regex.escape(token)}\b""")
                    boundaryRegex.containsMatchIn(vTextNorm) || vTextClean.split(Regex("""\s+""")).any { it.equals(token, ignoreCase = !caseSensitive) }
                } else {
                    compareVerseText.contains(token)
                }
            }

            val isAllTokens = compareTokens.isNotEmpty() && matchedTokensCount == compareTokens.size
            val isAnyToken = compareTokens.isNotEmpty() && matchedTokensCount > 0
            val isPartial = compareTokens.any { t -> t.length >= 3 && compareVerseText.contains(t) }

            val matchType: com.example.ui.screens.search.MatchType? = when {
                isExactPhrase -> com.example.ui.screens.search.MatchType.EXACT_PHRASE
                isAllTokens && wholeWord -> com.example.ui.screens.search.MatchType.EXACT_WORD
                isAllTokens -> com.example.ui.screens.search.MatchType.ALL_KEYWORDS
                isAnyToken -> com.example.ui.screens.search.MatchType.ANY_KEYWORD
                isPartial -> com.example.ui.screens.search.MatchType.PARTIAL
                else -> null
            }

            if (matchType == null) return@mapNotNull null

            // Filter by requested search mode
            val isModeMatched = when (searchMode) {
                com.example.ui.screens.search.SearchMode.EXACT_PHRASE -> matchType == com.example.ui.screens.search.MatchType.EXACT_PHRASE
                com.example.ui.screens.search.SearchMode.EXACT_WORD -> matchType == com.example.ui.screens.search.MatchType.EXACT_PHRASE || matchType == com.example.ui.screens.search.MatchType.EXACT_WORD
                com.example.ui.screens.search.SearchMode.WORD -> if (wholeWord) (matchType == com.example.ui.screens.search.MatchType.EXACT_PHRASE || matchType == com.example.ui.screens.search.MatchType.EXACT_WORD) else (matchType != com.example.ui.screens.search.MatchType.PARTIAL)
                com.example.ui.screens.search.SearchMode.ALL_WORDS -> matchType == com.example.ui.screens.search.MatchType.EXACT_PHRASE || matchType == com.example.ui.screens.search.MatchType.EXACT_WORD || matchType == com.example.ui.screens.search.MatchType.ALL_KEYWORDS
                com.example.ui.screens.search.SearchMode.ANY_WORD -> true
                com.example.ui.screens.search.SearchMode.PARTIAL -> true
                com.example.ui.screens.search.SearchMode.REFERENCE -> true
                com.example.ui.screens.search.SearchMode.TOPIC -> true
                com.example.ui.screens.search.SearchMode.SEMANTIC -> true
                com.example.ui.screens.search.SearchMode.QUESTION -> true
            }

            if (!isModeMatched) return@mapNotNull null

            // Calculate documented relevance score
            val baseScore = when (matchType) {
                com.example.ui.screens.search.MatchType.EXACT_PHRASE -> 1000 + (100 - (verse.verseText.length / 5).coerceAtMost(80))
                com.example.ui.screens.search.MatchType.EXACT_WORD -> 800 + compareTokens.size * 25
                com.example.ui.screens.search.MatchType.ALL_KEYWORDS -> 600 + compareTokens.size * 20
                com.example.ui.screens.search.MatchType.ANY_KEYWORD -> 300 * matchedTokensCount
                com.example.ui.screens.search.MatchType.PARTIAL -> 150
                com.example.ui.screens.search.MatchType.REFERENCE -> 2000
            }

            val isOt = CanonicalBibleData.findBookById(verse.bookId)?.testament == com.example.data.model.Testament.OLD_TESTAMENT
            val tName = if (verse.languageCode == "ta") (if (isOt) "பழைய ஏற்பாடு" else "புதிய ஏற்பாடு") else (if (isOt) "Old Testament" else "New Testament")
            val lLabel = if (verse.languageCode == "ta") "தமிழ் (BSI)" else "English (KJV)"

            com.example.data.model.SearchResultItem(
                verse = verse,
                testamentName = tName,
                languageLabel = lLabel,
                matchType = matchType,
                relevanceScore = baseScore
            )
        }

        // Deduplicate by composite key
        val combined = (directRefResults + matchedItems).distinctBy {
            "${it.verse.editionId}_${it.verse.bookId}_${it.verse.chapterNumber}_${it.verse.verseNumber}"
        }

        // Sort by requested order
        val sorted = when (sortOrder) {
            com.example.ui.screens.search.SearchSortOrder.RELEVANCE -> {
                combined.sortedByDescending { it.relevanceScore }
            }
            com.example.ui.screens.search.SearchSortOrder.BIBLE_ORDER -> {
                combined.sortedWith(
                    compareBy(
                        { bookOrderMap[it.verse.bookId] ?: 99 },
                        { it.verse.chapterNumber },
                        { it.verse.verseNumber }
                    )
                )
            }
            com.example.ui.screens.search.SearchSortOrder.ALPHABETICAL -> {
                combined.sortedBy { it.verse.verseText }
            }
        }

        Pair(sorted, refResult)
    }

    suspend fun getSurroundingVerses(
        bookId: String,
        chapter: Int,
        verse: Int,
        languageCode: String
    ): Triple<BibleVerse?, BibleVerse?, BibleVerse?> = withContext(Dispatchers.IO) {
        val lang = if (languageCode == "both") "en" else languageCode
        var current = bibleDao.getVerseByBookChapterVerse(bookId, chapter, verse, lang)
        if (current == null) {
            val canonical = ScriptureSeedData.getCanonicalVersesForChapter(bookId, chapter, lang)
            if (canonical.isNotEmpty()) {
                bibleDao.insertVerses(canonical)
                current = canonical.firstOrNull { it.verseNumber == verse }
            }
        }
        val prev = if (verse > 1) {
            bibleDao.getVerseByBookChapterVerse(bookId, chapter, verse - 1, lang)
        } else null
        val next = bibleDao.getVerseByBookChapterVerse(bookId, chapter, verse + 1, lang)
        Triple(prev, current, next)
    }


    // Bookmarks
    fun getAllBookmarks(): Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()

    suspend fun isBookmarked(bookId: String, chapter: Int, verse: Int): Boolean {
        return bookmarkDao.getBookmark(bookId, chapter, verse) != null
    }

    suspend fun toggleBookmark(bookId: String, chapter: Int, verse: Int, reference: String, verseText: String, languageCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            val existing = bookmarkDao.getBookmark(bookId, chapter, verse)
            if (existing != null) {
                bookmarkDao.deleteBookmark(existing)
                false
            } else {
                bookmarkDao.insertBookmark(
                    Bookmark(
                        bookId = bookId,
                        chapterNumber = chapter,
                        verseNumber = verse,
                        reference = reference,
                        verseText = verseText,
                        languageCode = languageCode
                    )
                )
                true
            }
        }
    }

    suspend fun removeBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    // Highlights
    fun getHighlightsForChapter(bookId: String, chapter: Int): Flow<List<VerseHighlight>> {
        return highlightDao.getHighlightsForChapter(bookId, chapter)
    }

    suspend fun setHighlight(bookId: String, chapter: Int, verse: Int, colorHex: String) = withContext(Dispatchers.IO) {
        highlightDao.insertHighlight(
            VerseHighlight(
                bookId = bookId,
                chapterNumber = chapter,
                verseNumber = verse,
                colorHex = colorHex
            )
        )
    }

    suspend fun removeHighlight(bookId: String, chapter: Int, verse: Int) = withContext(Dispatchers.IO) {
        highlightDao.removeHighlight(bookId, chapter, verse)
    }

    // Notes
    fun getAllNotes(): Flow<List<BibleNote>> = noteDao.getAllNotes()

    suspend fun getNoteForVerse(bookId: String, chapter: Int, verse: Int): BibleNote? = withContext(Dispatchers.IO) {
        noteDao.getNoteForVerse(bookId, chapter, verse)
    }

    suspend fun saveNote(bookId: String, chapter: Int, verse: Int, reference: String, title: String, content: String) = withContext(Dispatchers.IO) {
        val existing = noteDao.getNoteForVerse(bookId, chapter, verse)
        if (existing != null) {
            noteDao.insertOrUpdateNote(
                existing.copy(
                    noteTitle = title,
                    noteContent = content,
                    timestamp = System.currentTimeMillis()
                )
            )
        } else {
            noteDao.insertOrUpdateNote(
                BibleNote(
                    bookId = bookId,
                    chapterNumber = chapter,
                    verseNumber = verse,
                    reference = reference,
                    noteTitle = title,
                    noteContent = content
                )
            )
        }
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }

    // History
    fun getRecentHistory(): Flow<List<ReadingHistory>> = historyDao.getRecentHistory()

    fun getAllReadingHistory(): Flow<List<ReadingHistory>> = historyDao.getAllReadingHistory()

    fun getLastRead(): Flow<ReadingHistory?> = historyDao.getLastRead()

    suspend fun recordReading(bookId: String, bookName: String, chapter: Int, verse: Int = 1) = withContext(Dispatchers.IO) {
        historyDao.recordHistory(
            ReadingHistory(
                bookId = bookId,
                bookName = bookName,
                chapterNumber = chapter,
                verseNumber = verse
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }

    suspend fun seedInitialReadingHistoryIfEmpty() = withContext(Dispatchers.IO) {
        val current = historyDao.getAllReadingHistoryDirect()
        if (current.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            val sampleHistories = listOf(
                ReadingHistory(bookId = "JHN", bookName = "John", chapterNumber = 1, verseNumber = 1, timestamp = now - 2 * dayMs),
                ReadingHistory(bookId = "JHN", bookName = "John", chapterNumber = 2, verseNumber = 1, timestamp = now - 2 * dayMs + 3600000),
                ReadingHistory(bookId = "JHN", bookName = "John", chapterNumber = 3, verseNumber = 16, timestamp = now - 1 * dayMs),
                ReadingHistory(bookId = "PSA", bookName = "Psalms", chapterNumber = 23, verseNumber = 1, timestamp = now - 1 * dayMs + 7200000),
                ReadingHistory(bookId = "GEN", bookName = "Genesis", chapterNumber = 1, verseNumber = 1, timestamp = now - 1800000),
                ReadingHistory(bookId = "ROM", bookName = "Romans", chapterNumber = 8, verseNumber = 28, timestamp = now)
            )
            historyDao.recordHistoryList(sampleHistories)
        }
    }

    // Plans
    fun getPlanProgress(planId: String): Flow<List<PlanProgress>> = planDao.getPlanProgress(planId)

    suspend fun toggleDayPortionCompleted(planId: String, day: Int, completed: Boolean) = withContext(Dispatchers.IO) {
        val key = "${planId}_day_$day"
        planDao.setPortionCompleted(
            PlanProgress(
                progressKey = key,
                planId = planId,
                dayNumber = day,
                isCompleted = completed,
                completedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetPlan(planId: String) = withContext(Dispatchers.IO) {
        planDao.resetPlan(planId)
    }

    // Quiz
    fun getRecentQuizScores(): Flow<List<QuizScore>> = quizScoreDao.getRecentScores()

    suspend fun saveQuizScore(quizTitle: String, score: Int, total: Int) = withContext(Dispatchers.IO) {
        quizScoreDao.insertScore(
            QuizScore(
                quizTitle = quizTitle,
                score = score,
                totalQuestions = total
            )
        )
    }
}
