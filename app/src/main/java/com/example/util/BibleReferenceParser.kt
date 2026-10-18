package com.example.util

import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.BibleBook

data class ParsedReference(
    val book: BibleBook,
    val chapter: Int,
    val verse: Int? = null,
    val endVerse: Int? = null,
    val formattedDisplay: String
)

sealed class ReferenceParseResult {
    data class Success(val reference: ParsedReference) : ReferenceParseResult()
    data class Invalid(val messageEn: String, val messageTa: String) : ReferenceParseResult()
    object NotAReference : ReferenceParseResult()
}

object BibleReferenceParser {

    // Regex to detect book followed by chapter and optional verse(s)
    // Matches: "John 3:16", "1 John 1:9", "Genesis 1:1", "Psalm 23", "யோவான் 3:16", "ஆதியாகமம் 1:1"
    private val REFERENCE_REGEX = Regex(
        """^([1-3I|II|III]?\s*[\p{L}\p{M}\.\s]+?)\s*(\d+)(?:[:\s](\d+)(?:[-–](\d+))?)?$""",
        RegexOption.IGNORE_CASE
    )

    fun parse(input: String): ReferenceParseResult {
        val trimmed = input.trim()
        if (trimmed.length < 2) return ReferenceParseResult.NotAReference

        val match = REFERENCE_REGEX.matchEntire(trimmed) ?: return ReferenceParseResult.NotAReference

        val rawBookName = match.groupValues[1].trim()
        val rawChapter = match.groupValues[2].toIntOrNull() ?: return ReferenceParseResult.NotAReference
        val rawVerse = match.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.toIntOrNull()
        val rawEndVerse = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.toIntOrNull()

        val book = resolveBook(rawBookName)
        if (book == null) {
            // Check if it really looks like user tried to enter a Bible book reference
            if (rawVerse != null || trimmed.contains(":") || isLikelyBookQuery(rawBookName)) {
                return ReferenceParseResult.Invalid(
                    messageEn = "Book '$rawBookName' not found. Please check spelling (e.g. John 3:16, Genesis 1:1).",
                    messageTa = "'$rawBookName' என்ற புத்தகம் கிடைக்கவில்லை. சரியான புத்தக பெயரை சோதிக்கவும் (எ.கா. யோவான் 3:16)."
                )
            }
            return ReferenceParseResult.NotAReference
        }

        if (rawChapter < 1 || rawChapter > book.chapterCount) {
            return ReferenceParseResult.Invalid(
                messageEn = "${book.englishName} has ${book.chapterCount} chapters. Chapter $rawChapter does not exist.",
                messageTa = "${book.tamilName} நூலில் ${book.chapterCount} அதிகாரங்கள் மட்டுமே உள்ளன. அதிகாரம் $rawChapter கிடைக்கவில்லை."
            )
        }

        val display = buildString {
            append("${book.englishName} $rawChapter")
            if (rawVerse != null) {
                append(":$rawVerse")
                if (rawEndVerse != null && rawEndVerse > rawVerse) {
                    append("-$rawEndVerse")
                }
            }
        }

        return ReferenceParseResult.Success(
            ParsedReference(
                book = book,
                chapter = rawChapter,
                verse = rawVerse,
                endVerse = rawEndVerse,
                formattedDisplay = display
            )
        )
    }

    private fun resolveBook(raw: String): BibleBook? {
        val clean = raw.replace(".", "").trim().lowercase()
        // Try direct find
        CanonicalBibleData.findBookByName(clean)?.let { return it }

        // Common aliases and alternate spellings
        val aliasMap = mapOf(
            "gen" to "GEN", "genesis" to "GEN", "ஆதி" to "GEN", "ஆதியாகமம்" to "GEN",
            "exo" to "EXO", "exodus" to "EXO", "யாத்" to "EXO", "யாத்திராகமம்" to "EXO",
            "lev" to "LEV", "leviticus" to "LEV", "லேவி" to "LEV", "லேவியராகமம்" to "LEV",
            "num" to "NUM", "numbers" to "NUM", "எண்" to "NUM", "எண்ணாகமம்" to "NUM",
            "deu" to "DEU", "deut" to "DEU", "deuteronomy" to "DEU", "உபா" to "DEU", "உபாகமம்" to "DEU",
            "jos" to "JOS", "josh" to "JOS", "joshua" to "JOS", "யோசு" to "JOS", "யோசுவா" to "JOS",
            "judg" to "JDG", "judges" to "JDG", "நியா" to "JDG", "நியாயாதிபதிகள்" to "JDG",
            "ruth" to "RUT", "ரூத்" to "RUT",
            "1 sam" to "1SA", "1sam" to "1SA", "1 samuel" to "1SA", "1சாமு" to "1SA", "1 சாமுவேல்" to "1SA",
            "2 sam" to "2SA", "2sam" to "2SA", "2 samuel" to "2SA", "2சாமு" to "2SA", "2 சாமுவேல்" to "2SA",
            "1 kgs" to "1KI", "1kings" to "1KI", "1 kings" to "1KI", "1இரா" to "1KI", "1 இராஜாக்கள்" to "1KI",
            "2 kgs" to "2KI", "2kings" to "2KI", "2 kings" to "2KI", "2இரா" to "2KI", "2 இராஜாக்கள்" to "2KI",
            "1 chr" to "1CH", "1 chron" to "1CH", "1 chronicles" to "1CH", "1நாளா" to "1CH", "1 நாளாகமம்" to "1CH",
            "2 chr" to "2CH", "2 chron" to "2CH", "2 chronicles" to "2CH", "2நாளா" to "2CH", "2 நாளாகமம்" to "2CH",
            "ezra" to "EZR", "எஸ்றா" to "EZR",
            "neh" to "NEH", "nehemiah" to "NEH", "நெகே" to "NEH", "நெகேமியா" to "NEH",
            "est" to "EST", "esther" to "EST", "எஸ்த" to "EST", "எஸ்தர்" to "EST",
            "job" to "JOB", "யோபு" to "JOB",
            "ps" to "PSA", "psa" to "PSA", "psalm" to "PSA", "psalms" to "PSA", "சங்" to "PSA", "சங்கீதம்" to "PSA",
            "pr" to "PRO", "prov" to "PRO", "proverbs" to "PRO", "நீதி" to "PRO", "நீதிமொழிகள்" to "PRO",
            "ecc" to "ECC", "eccl" to "ECC", "ecclesiastes" to "ECC", "பிரச" to "ECC", "பிரசங்கி" to "ECC",
            "song" to "SNG", "sos" to "SNG", "song of solomon" to "SNG", "song of songs" to "SNG", "உன்" to "SNG", "உன்னதப்பாட்டு" to "SNG",
            "isa" to "ISA", "isaiah" to "ISA", "ஏசா" to "ISA", "ஏசாயா" to "ISA",
            "jer" to "JER", "jeremiah" to "JER", "எரே" to "JER", "எரேமியா" to "JER",
            "lam" to "LAM", "lamentations" to "LAM", "புல" to "LAM", "புலம்பல்" to "LAM",
            "ezek" to "EZK", "ezekiel" to "EZK", "எசேக்" to "EZK", "எசேக்கியேல்" to "EZK",
            "dan" to "DAN", "daniel" to "DAN", "தானி" to "DAN", "தானியேல்" to "DAN",
            "hos" to "HOS", "hosea" to "HOS", "ஓசி" to "HOS", "ஓசியா" to "HOS",
            "joel" to "JOL", "யோவே" to "JOL", "யோவேல்" to "JOL",
            "amos" to "AMO", "ஆமோ" to "AMO", "ஆமோஸ்" to "AMO",
            "obad" to "OBA", "obadiah" to "OBA", "ஒப" to "OBA", "ஒபதியா" to "OBA",
            "jon" to "JON", "jonah" to "JON", "யோனா" to "JON",
            "mic" to "MIC", "micah" to "MIC", "மீகா" to "MIC",
            "nah" to "NAM", "nahum" to "NAM", "நாகூ" to "NAM", "நாகூம்" to "NAM",
            "hab" to "HAB", "habakkuk" to "HAB", "ஆப" to "HAB", "ஆபகூக்" to "HAB",
            "zeph" to "ZEP", "zephaniah" to "ZEP", "செப்" to "ZEP", "செப்பனியா" to "ZEP",
            "hag" to "HAG", "haggai" to "HAG", "ஆகாய்" to "HAG",
            "zech" to "ZEC", "zechariah" to "ZEC", "சக" to "ZEC", "சகரியா" to "ZEC",
            "mal" to "MAL", "malachi" to "MAL", "மல்கி" to "MAL", "மல்கியா" to "MAL",
            "mat" to "MAT", "matt" to "MAT", "matthew" to "MAT", "மத்" to "MAT", "மத்தேயு" to "MAT",
            "mk" to "MRK", "mrk" to "MRK", "mark" to "MRK", "மாற்" to "MRK", "மாற்கு" to "MRK",
            "lk" to "LUK", "luk" to "LUK", "luke" to "LUK", "லூக்" to "LUK", "லூக்கா" to "LUK",
            "jn" to "JHN", "jhn" to "JHN", "john" to "JHN", "யோவா" to "JHN", "யோவான்" to "JHN",
            "acts" to "ACT", "அப்" to "ACT", "அப்போஸ்தலர்" to "ACT",
            "rom" to "ROM", "romans" to "ROM", "ரோம" to "ROM", "ரோமர்" to "ROM",
            "1 cor" to "1CO", "1cor" to "1CO", "1 corinthians" to "1CO", "1கொரி" to "1CO", "1 கொரிந்தியர்" to "1CO",
            "2 cor" to "2CO", "2cor" to "2CO", "2 corinthians" to "2CO", "2கொரி" to "2CO", "2 கொரிந்தியர்" to "2CO",
            "gal" to "GAL", "galatians" to "GAL", "கலா" to "GAL", "கலாத்தியர்" to "GAL",
            "eph" to "EPH", "ephesians" to "EPH", "எபே" to "EPH", "எபேசியர்" to "EPH",
            "php" to "PHP", "phil" to "PHP", "philippians" to "PHP", "பிலிப்" to "PHP", "பிலிப்பியர்" to "PHP",
            "col" to "COL", "colossians" to "COL", "கொலோ" to "COL", "கொலோசெயர்" to "COL",
            "1 thess" to "1TH", "1thess" to "1TH", "1 thessalonians" to "1TH", "1தெச" to "1TH", "1 தெசலோனிக்கேயர்" to "1TH",
            "2 thess" to "2TH", "2thess" to "2TH", "2 thessalonians" to "2TH", "2தெச" to "2TH", "2 தெசலோனிக்கேயர்" to "2TH",
            "1 tim" to "1TI", "1tim" to "1TI", "1 timothy" to "1TI", "1தீமோ" to "1TI", "1 தீமோத்தேயு" to "1TI",
            "2 tim" to "2TI", "2tim" to "2TI", "2 timothy" to "2TI", "2தீமோ" to "2TI", "2 தீமோத்தேயு" to "2TI",
            "titus" to "TIT", "தீத்து" to "TIT",
            "phlm" to "PHM", "philemon" to "PHM", "பிலே" to "PHM", "பிலேமோன்" to "PHM",
            "heb" to "HEB", "hebrews" to "HEB", "எபி" to "HEB", "எபிரெயர்" to "HEB",
            "jas" to "JAS", "james" to "JAS", "யாக்" to "JAS", "யாக்கோபு" to "JAS",
            "1 pet" to "1PE", "1pet" to "1PE", "1 peter" to "1PE", "1பேது" to "1PE", "1 பேதுரு" to "1PE",
            "2 pet" to "2PE", "2pet" to "2PE", "2 peter" to "2PE", "2பேது" to "2PE", "2 பேதுரு" to "2PE",
            "1 jn" to "1JN", "1jn" to "1JN", "1 john" to "1JN", "1யோவா" to "1JN", "1 யோவான்" to "1JN",
            "2 jn" to "2JN", "2jn" to "2JN", "2 john" to "2JN", "2யோவா" to "2JN", "2 யோவான்" to "2JN",
            "3 jn" to "3JN", "3jn" to "3JN", "3 john" to "3JN", "3யோவா" to "3JN", "3 யோவான்" to "3JN",
            "jude" to "JUD", "யூதா" to "JUD",
            "rev" to "REV", "revelation" to "REV", "வெளி" to "REV", "வெளிப்படுத்தின விசேஷம்" to "REV"
        )

        val id = aliasMap[clean] ?: aliasMap[clean.replace(" ", "")]
        return if (id != null) CanonicalBibleData.findBookById(id) else null
    }

    private fun isLikelyBookQuery(raw: String): Boolean {
        val s = raw.lowercase()
        return s.contains("cor") || s.contains("john") || s.contains("tim") || s.contains("sam") ||
               s.contains("kings") || s.contains("chron") || s.contains("pet") || s.contains("thess") ||
               s.contains("கொரி") || s.contains("யோவா") || s.contains("சாமு") || s.contains("இரா") || s.contains("பேது")
    }
}
