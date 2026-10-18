package com.example.ui.screens.search

enum class SearchMode(val displayNameEn: String, val displayNameTa: String) {
    EXACT_PHRASE("Exact Phrase", "முழு தொடர்"),
    EXACT_WORD("Exact Word", "சரியான சொல்"),
    WORD("Word Search", "வார்த்தை தேடல்"), // Kept for backwards compatibility
    ALL_WORDS("All Keywords", "அனைத்து சொற்கள்"),
    ANY_WORD("Any Keyword", "ஏதேனும் ஒரு சொல்"),
    PARTIAL("Partial Match", "பகுதி சொல்"),
    REFERENCE("Bible Reference", "வசனக் குறிப்பு"),
    TOPIC("Topic Search", "வேத தலைப்பு"),
    SEMANTIC("Thematic & Semantic", "கருப்பொருள் தேடல்"),
    QUESTION("Q&A Question", "வேத வினா-விடை")
}

enum class SearchSortOrder(val displayNameEn: String, val displayNameTa: String) {
    RELEVANCE("Relevance", "பொருத்தம்"),
    BIBLE_ORDER("Bible Order", "வேதாகம வரிசை"),
    ALPHABETICAL("Alphabetical", "அகரவரிசை")
}

enum class MatchType(val displayNameEn: String, val displayNameTa: String) {
    EXACT_PHRASE("Exact Phrase", "முழு தொடர்"),
    EXACT_WORD("Exact Word", "சரியான சொல்"),
    ALL_KEYWORDS("All Keywords", "அனைத்து சொற்கள்"),
    ANY_KEYWORD("Any Keyword", "ஏதேனும் ஒரு சொல்"),
    PARTIAL("Partial Match", "பகுதி சொல்"),
    REFERENCE("Direct Reference", "நேரடி வசனம்")
}
