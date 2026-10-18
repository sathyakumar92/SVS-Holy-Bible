package com.example.data.model

data class TopicItem(
    val id: String,
    val englishName: String,
    val tamilName: String,
    val descriptionEn: String,
    val descriptionTa: String,
    val verseReferences: List<VerseReference>,
    val category: String = "Spiritual Life",
    val categoryTa: String = "ஆவிக்குரிய வாழ்க்கை",
    val subtopics: List<String> = emptyList(),
    val subtopicsTa: List<String> = emptyList(),
    val summaryEn: String = "",
    val summaryTa: String = ""
)


data class VerseReference(
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val endVerse: Int? = null,
    val englishRef: String,
    val tamilRef: String,
    val textEn: String,
    val textTa: String
)

data class DailyVerse(
    val dateString: String,
    val referenceEn: String,
    val referenceTa: String,
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val textEn: String,
    val textTa: String,
    val reflectionEn: String,
    val reflectionTa: String
)

data class DailyPrayer(
    val dateString: String,
    val titleEn: String,
    val titleTa: String,
    val contentEn: String,
    val contentTa: String,
    val scriptureRefEn: String,
    val scriptureRefTa: String
)

enum class QuizDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class QuizType {
    MULTIPLE_CHOICE,
    TRUE_OR_FALSE,
    FILL_IN_BLANK
}

data class QuizQuestion(
    val id: String,
    val difficulty: QuizDifficulty,
    val type: QuizType = QuizType.MULTIPLE_CHOICE,
    val category: String, // 30 structured categories
    val questionEn: String,
    val questionTa: String,
    val optionsEn: List<String>,
    val optionsTa: List<String>,
    val correctIndex: Int,
    val explanationEn: String,
    val explanationTa: String,
    val scriptureRefEn: String,
    val scriptureRefTa: String,
    // Upgraded standard properties with backward-compatible defaults:
    val questionId: String = id,
    val questionTextEnglish: String = questionEn,
    val questionTextTamil: String = questionTa,
    val optionsEnglish: List<String> = optionsEn,
    val optionsTamil: List<String> = optionsTa,
    val correctAnswerId: String = "opt_$correctIndex",
    val explanationEnglish: String = explanationEn,
    val explanationTamil: String = explanationTa,
    val bibleReference: String = scriptureRefEn,
    val bibleReferenceTa: String = scriptureRefTa,
    val testament: String = "Old Testament",
    val bookId: String = "",
    val verificationStatus: String = "VERIFIED",
    val activeStatus: Boolean = true
)

data class QuizSessionQuestion(
    val question: QuizQuestion,
    val shuffledOptionsEn: List<String>,
    val shuffledOptionsTa: List<String>,
    val originalToShuffledIndices: List<Int>, // maps original index -> shuffled index
    val shuffledToOriginalIndices: List<Int>, // maps shuffled index -> original index
    val correctShuffledIndex: Int,
    var selectedShuffledIndex: Int? = null,
    var isSubmitted: Boolean = false
)

data class QuizConfig(
    val questionCount: Int = 10,
    val difficulty: String = "MIXED", // "MIXED", "BEGINNER", "INTERMEDIATE", "ADVANCED"
    val testament: String = "BOTH", // "BOTH", "OT", "NT"
    val category: String = "ALL", // "ALL" or specific category name
    val language: String = "en"
)

data class QuizSessionState(
    val sessionId: String,
    val config: QuizConfig,
    val questions: List<QuizSessionQuestion>,
    var currentIndex: Int = 0,
    var score: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    var isCompleted: Boolean = false,
    var completedTime: Long? = null
)

data class QuestionHistoryEntry(
    val questionId: String,
    val lastAskedTimestamp: Long,
    val timesAsked: Int = 1,
    val timesCorrect: Int = 0
)

data class QuizReviewItem(
    val questionNumber: Int,
    val questionText: String,
    val options: List<String>,
    val selectedIndex: Int?,
    val correctIndex: Int,
    val isCorrect: Boolean,
    val explanation: String,
    val scriptureReference: String,
    val category: String,
    val difficulty: QuizDifficulty
)

data class ReadingPlan(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val durationDays: Int,
    val descriptionEn: String,
    val descriptionTa: String,
    val dailyPortions: List<PlanPortion>
)

data class PlanPortion(
    val dayNumber: Int,
    val titleEn: String,
    val titleTa: String,
    val readingRefEn: String,
    val readingRefTa: String,
    val bookId: String,
    val chapter: Int,
    val keyVerseEn: String,
    val keyVerseTa: String
)
