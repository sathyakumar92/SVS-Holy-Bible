package com.example.util

import com.example.data.datasource.CanonicalBibleData
import com.example.data.datasource.CompleteBibleTopicsData
import com.example.data.model.BibleBook
import com.example.data.model.BibleVerse
import com.example.data.model.SearchResultItem
import com.example.data.model.TopicEntity
import com.example.data.model.TopicReferenceEntity
import java.text.Normalizer

enum class SearchIntent {
    BIBLE_REFERENCE,
    NATURAL_LANGUAGE_QUESTION,
    BIBLE_TOPIC,
    KEYWORD_PHRASE,
    BOOK_CHAPTER_NAVIGATOR
}

data class BiblicalAnswer(
    val query: String,
    val titleEn: String,
    val titleTa: String,
    val answerEn: String,
    val answerTa: String,
    val matchedTopicId: String,
    val foundationalPassages: List<TopicReferenceEntity>
)

data class CategorizedSearchResults(
    val intent: SearchIntent,
    val normalizedQuery: String,
    val matchingTopics: List<TopicEntity> = emptyList(),
    val directReferenceResult: ReferenceParseResult = ReferenceParseResult.NotAReference,
    val referenceVerses: List<BibleVerse> = emptyList(),
    val exactVerseMatches: List<SearchResultItem> = emptyList(),
    val relatedVerses: List<SearchResultItem> = emptyList(),
    val relevantChapters: List<Pair<BibleBook, Int>> = emptyList(),
    val biblicalAnswer: BiblicalAnswer? = null,
    val suggestedSearches: List<String> = emptyList(),
    val totalCount: Int = 0
)

object AdvancedSearchEngine {

    // Bilingual Synonyms Map: English <-> Tamil
    val bilingualSynonyms: Map<String, List<String>> = mapOf(
        "prayer" to listOf("ஜெபம்", "விண்ணப்பம்", "மன்றாட்டு", "supplication", "intercession", "pray"),
        "ஜெபம்" to listOf("prayer", "supplication", "intercession", "pray", "விண்ணப்பம்", "மன்றாட்டு"),
        "faith" to listOf("விசுவாசம்", "நம்பிக்கை", "trust", "belief", "believe", "assurance"),
        "விசுவாசம்" to listOf("faith", "belief", "trust", "assurance", "நம்பிக்கை"),
        "love" to listOf("அன்பு", "நேசம்", "பிரியம்", "charity", "agape", "affection"),
        "அன்பு" to listOf("love", "charity", "agape", "compassion", "நேசம்"),
        "forgiveness" to listOf("மன்னிப்பு", "பாவமன்னிப்பு", "pardon", "mercy", "cleanse"),
        "மன்னிப்பு" to listOf("forgiveness", "pardon", "mercy", "cleansing"),
        "peace" to listOf("சமாதானம்", "அமைதி", "இளைப்பாறுதல்", "shalom", "calm", "rest"),
        "சமாதானம்" to listOf("peace", "shalom", "tranquility", "rest", "அமைதி"),
        "hope" to listOf("நம்பிக்கை", "ஆதரவு", "expectation", "anchor"),
        "நம்பிக்கை" to listOf("hope", "faith", "trust", "expectation"),
        "salvation" to listOf("இரட்சிப்பு", "மீட்பு", "deliverance", "redemption", "saved"),
        "இரட்சிப்பு" to listOf("salvation", "redemption", "deliverance", "saved", "மீட்பு"),
        "wisdom" to listOf("ஞானம்", "புத்தி", "விவேகம்", "discernment", "understanding"),
        "ஞானம்" to listOf("wisdom", "understanding", "discernment", "prudence"),
        "holy spirit" to listOf("பரிசுத்த ஆவி", "ஆவியானவர்", "தேற்றரவாளன்", "comforter", "spirit of god"),
        "பரிசுத்த ஆவி" to listOf("holy spirit", "holy ghost", "comforter", "paraclete", "ஆவியானவர்"),
        "fear" to listOf("பயம்", "திகைப்பு", "கலக்கம்", "anxiety", "worry", "dread"),
        "பயம்" to listOf("fear", "anxiety", "dread", "worry", "overcoming fear", "courage"),
        "grace" to listOf("கிருபை", "தயவு", "இரக்கம்", "favor", "mercy"),
        "கிருபை" to listOf("grace", "favor", "mercy", "unmerited favor"),
        "healing" to listOf("சுகம்", "ஆரோக்கியம்", "பரிகாரி", "health", "cure", "physician"),
        "சுகம்" to listOf("healing", "health", "cure", "restoration"),
        "repentance" to listOf("மனந்திரும்புதல்", "பாவ அறிக்கை", "turn", "contrition"),
        "மனந்திரும்புதல்" to listOf("repentance", "contrition", "conversion", "turn"),
        "worship" to listOf("ஆராதனை", "துதி", "ஸ்தோத்திரம்", "தொழுகை", "praise", "adoration"),
        "ஆராதனை" to listOf("worship", "praise", "adoration", "exaltation", "துதி"),
        "eternal life" to listOf("நித்திய ஜீவன்", "அழியாமை", "everlasting life", "heaven"),
        "நித்திய ஜீவன்" to listOf("eternal life", "everlasting life", "immortality"),
        "second coming" to listOf("இரண்டாம் வருகை", "மறுவருகை", "return of christ", "parousia"),
        "இரண்டாம் வருகை" to listOf("second coming", "return of jesus", "rapture"),
        "family" to listOf("குடும்பம்", "மனைவி", "கணவன்", "பிள்ளைகள்", "marriage", "household"),
        "குடும்பம்" to listOf("family", "marriage", "household", "children"),
        "marriage" to listOf("திருமணம்", "மணவாழ்க்கை", "இல்லறம்", "wedding", "spouse"),
        "திருமணம்" to listOf("marriage", "wedding", "spouse", "husband", "wife"),
        "money" to listOf("பணம்", "செல்வம்", "பொருள்", "wealth", "riches", "finances", "tithe"),
        "பணம்" to listOf("money", "wealth", "finances", "riches", "stewardship"),
        "work" to listOf("வேலை", "உழைப்பு", "தொழில்", "labor", "diligence", "career"),
        "வேலை" to listOf("work", "labor", "diligence", "employment")
    )

    // Normalize Tamil and English text for accurate matching
    fun normalize(text: String): String {
        return Normalizer.normalize(text.trim(), Normalizer.Form.NFC)
            .lowercase()
            .replace(Regex("[.,:;!?'\"()\\[\\]\\-–—]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // Detect if the query is a natural-language question
    fun isQuestion(query: String): Boolean {
        val q = query.trim().lowercase()
        val questionKeywords = listOf(
            "what does", "how can", "how to", "why does", "what is", "where is",
            "tell me", "does the bible", "what scripture", "who is",
            "என்ன சொல்கிறது", "எப்படி", "எவ்வாறு", "ஏன்", "எவை", "எங்கே", "யார்", "உள்ளன", "கூறுகிறது"
        )
        return questionKeywords.any { q.contains(it) } || q.endsWith("?") || q.endsWith("குறித்து") || q.endsWith("பற்றி")
    }

    // Map a natural language question or query to a primary topic
    fun resolveTopicForQuestion(query: String, allTopics: List<TopicEntity>): TopicEntity? {
        val norm = normalize(query)

        // 1. Check direct keyword/synonym match
        for (topic in allTopics) {
            val titleNormEn = normalize(topic.titleEn)
            val titleNormTa = normalize(topic.titleTa)
            if (norm.contains(titleNormEn) || norm.contains(titleNormTa)) return topic

            val keywords = topic.keywords.split(",").map { normalize(it) }
            if (keywords.any { it.isNotBlank() && norm.contains(it) }) return topic
        }

        // 2. Check bilingual synonyms
        for ((term, synonyms) in bilingualSynonyms) {
            if (norm.contains(term) || synonyms.any { norm.contains(it) }) {
                val matched = allTopics.firstOrNull { t ->
                    normalize(t.id).contains(term) ||
                    normalize(t.titleEn).contains(term) ||
                    normalize(t.titleTa).contains(term)
                }
                if (matched != null) return matched
            }
        }

        return null
    }

    // Build a biblical answer for questions grounded in verified Scripture
    fun buildBiblicalAnswer(query: String, topic: TopicEntity): BiblicalAnswer {
        val refs = CompleteBibleTopicsData.getCuratedReferencesForTopic(topic.id)
        return BiblicalAnswer(
            query = query,
            titleEn = topic.titleEn,
            titleTa = topic.titleTa,
            answerEn = topic.introductionEn.ifBlank { topic.shortDescriptionEn },
            answerTa = topic.introductionTa.ifBlank { topic.shortDescriptionTa },
            matchedTopicId = topic.id,
            foundationalPassages = refs.take(3)
        )
    }

    // Suggest related searches based on input
    fun getSuggestedSearches(query: String, isTamil: Boolean): List<String> {
        val norm = normalize(query)
        val defaultEn = listOf("John 3:16", "Romans 8:28", "Psalm 23", "What does the Bible say about prayer?", "How to overcome fear", "Fruit of the Spirit", "God's promises", "Faith and works")
        val defaultTa = listOf("யோவான் 3:16", "சங்கீதம் 23", "ரோமர் 8:28", "ஜெபத்தைப் பற்றி வேதாகமம் என்ன சொல்கிறது?", "பயத்தை வெல்லுதல்", "ஆவியின் கனி", "தேவனின் வாக்குத்தத்தங்கள்", "விசுவாசம்")

        if (norm.isBlank()) return if (isTamil) defaultTa else defaultEn

        val list = mutableListOf<String>()
        // Check matching synonym expansions
        for ((term, synonyms) in bilingualSynonyms) {
            if (term.contains(norm) || norm.contains(term)) {
                list.add(term.replaceFirstChar { it.uppercase() })
                synonyms.take(2).forEach { list.add(it) }
            }
        }

        if (isTamil) {
            list.addAll(defaultTa.filter { it.contains(norm) })
            if (list.size < 4) list.addAll(defaultTa.take(4 - list.size))
        } else {
            list.addAll(defaultEn.filter { it.lowercase().contains(norm) })
            if (list.size < 4) list.addAll(defaultEn.take(4 - list.size))
        }

        return list.distinct().take(6)
    }
}
