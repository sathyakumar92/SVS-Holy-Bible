package com.example.data.datasource

import com.example.data.model.BibleBook
import com.example.data.model.Testament

object CanonicalBibleData {

    val allBooks: List<BibleBook> = listOf(
        // OLD TESTAMENT (39 Books)
        BibleBook("GEN", 1, Testament.OLD_TESTAMENT, "Genesis", "ஆதியாகமம்", 50, "Gen", "ஆதி"),
        BibleBook("EXO", 2, Testament.OLD_TESTAMENT, "Exodus", "யாத்திராகமம்", 40, "Exo", "யாத்"),
        BibleBook("LEV", 3, Testament.OLD_TESTAMENT, "Leviticus", "லேவியராகமம்", 27, "Lev", "லேவி"),
        BibleBook("NUM", 4, Testament.OLD_TESTAMENT, "Numbers", "எண்ணாகமம்", 36, "Num", "எண்"),
        BibleBook("DEU", 5, Testament.OLD_TESTAMENT, "Deuteronomy", "உபாகமம்", 34, "Deu", "உபா"),
        BibleBook("JOS", 6, Testament.OLD_TESTAMENT, "Joshua", "யோசுவா", 24, "Jos", "யோசு"),
        BibleBook("JDG", 7, Testament.OLD_TESTAMENT, "Judges", "நியாயாதிபதிகள்", 21, "Jdg", "நியா"),
        BibleBook("RUT", 8, Testament.OLD_TESTAMENT, "Ruth", "ரூத்", 4, "Rut", "ரூத்"),
        BibleBook("1SA", 9, Testament.OLD_TESTAMENT, "1 Samuel", "1 சாமுவேல்", 31, "1Sa", "1சாமு"),
        BibleBook("2SA", 10, Testament.OLD_TESTAMENT, "2 Samuel", "2 சாமுவேல்", 24, "2Sa", "2சாமு"),
        BibleBook("1KI", 11, Testament.OLD_TESTAMENT, "1 Kings", "1 இராஜாக்கள்", 22, "1Ki", "1இரா"),
        BibleBook("2KI", 12, Testament.OLD_TESTAMENT, "2 Kings", "2 இராஜாக்கள்", 25, "2Ki", "2இரா"),
        BibleBook("1CH", 13, Testament.OLD_TESTAMENT, "1 Chronicles", "1 நாளாகமம்", 29, "1Ch", "1நாளா"),
        BibleBook("2CH", 14, Testament.OLD_TESTAMENT, "2 Chronicles", "2 நாளாகமம்", 36, "2Ch", "2நாளா"),
        BibleBook("EZR", 15, Testament.OLD_TESTAMENT, "Ezra", "எஸ்றா", 10, "Ezr", "எஸ்றா"),
        BibleBook("NEH", 16, Testament.OLD_TESTAMENT, "Nehemiah", "நெகேமியா", 13, "Neh", "நெகே"),
        BibleBook("EST", 17, Testament.OLD_TESTAMENT, "Esther", "எஸ்தர்", 10, "Est", "எஸ்த"),
        BibleBook("JOB", 18, Testament.OLD_TESTAMENT, "Job", "யோபு", 42, "Job", "யோபு"),
        BibleBook("PSA", 19, Testament.OLD_TESTAMENT, "Psalms", "சங்கீதம்", 150, "Psa", "சங்"),
        BibleBook("PRO", 20, Testament.OLD_TESTAMENT, "Proverbs", "நீதிமொழிகள்", 31, "Pro", "நீதி"),
        BibleBook("ECC", 21, Testament.OLD_TESTAMENT, "Ecclesiastes", "பிரசங்கி", 12, "Ecc", "பிரச"),
        BibleBook("SNG", 22, Testament.OLD_TESTAMENT, "Song of Solomon", "உன்னதப்பாட்டு", 8, "Sng", "உன்"),
        BibleBook("ISA", 23, Testament.OLD_TESTAMENT, "Isaiah", "ஏசாயா", 66, "Isa", "ஏசா"),
        BibleBook("JER", 24, Testament.OLD_TESTAMENT, "Jeremiah", "எரேமியா", 52, "Jer", "எரே"),
        BibleBook("LAM", 25, Testament.OLD_TESTAMENT, "Lamentations", "புலம்பல்", 5, "Lam", "புல"),
        BibleBook("EZK", 26, Testament.OLD_TESTAMENT, "Ezekiel", "எசேக்கியேல்", 48, "Ezk", "எசேக்"),
        BibleBook("DAN", 27, Testament.OLD_TESTAMENT, "Daniel", "தானியேல்", 12, "Dan", "தானி"),
        BibleBook("HOS", 28, Testament.OLD_TESTAMENT, "Hosea", "ஓசியா", 14, "Hos", "ஓசி"),
        BibleBook("JOL", 29, Testament.OLD_TESTAMENT, "Joel", "யோவேல்", 3, "Jol", "யோவே"),
        BibleBook("AMO", 30, Testament.OLD_TESTAMENT, "Amos", "ஆமோஸ்", 9, "Amo", "ஆமோ"),
        BibleBook("OBA", 31, Testament.OLD_TESTAMENT, "Obadiah", "ஒபதியா", 1, "Oba", "ஒப"),
        BibleBook("JON", 32, Testament.OLD_TESTAMENT, "Jonah", "யோனா", 4, "Jon", "யோனா"),
        BibleBook("MIC", 33, Testament.OLD_TESTAMENT, "Micah", "மீகா", 7, "Mic", "மீகா"),
        BibleBook("NAM", 34, Testament.OLD_TESTAMENT, "Nahum", "நாகூம்", 3, "Nam", "நாகூ"),
        BibleBook("HAB", 35, Testament.OLD_TESTAMENT, "Habakkuk", "ஆபகூக்", 3, "Hab", "ஆப"),
        BibleBook("ZEP", 36, Testament.OLD_TESTAMENT, "Zephaniah", "செப்பனியா", 3, "Zep", "செப்"),
        BibleBook("HAG", 37, Testament.OLD_TESTAMENT, "Haggai", "ஆகாய்", 2, "Hag", "ஆகாய்"),
        BibleBook("ZEC", 38, Testament.OLD_TESTAMENT, "Zechariah", "சகரியா", 14, "Zec", "சக"),
        BibleBook("MAL", 39, Testament.OLD_TESTAMENT, "Malachi", "மல்கியா", 4, "Mal", "மல்கி"),

        // NEW TESTAMENT (27 Books)
        BibleBook("MAT", 40, Testament.NEW_TESTAMENT, "Matthew", "மத்தேயு", 28, "Mat", "மத்"),
        BibleBook("MRK", 41, Testament.NEW_TESTAMENT, "Mark", "மாற்கு", 16, "Mrk", "மாற்"),
        BibleBook("LUK", 42, Testament.NEW_TESTAMENT, "Luke", "லூக்கா", 24, "Luk", "லூக்"),
        BibleBook("JHN", 43, Testament.NEW_TESTAMENT, "John", "யோவான்", 21, "Jhn", "யோவா"),
        BibleBook("ACT", 44, Testament.NEW_TESTAMENT, "Acts", "அப்போஸ்தலர்", 28, "Act", "அப்"),
        BibleBook("ROM", 45, Testament.NEW_TESTAMENT, "Romans", "ரோமர்", 16, "Rom", "ரோம"),
        BibleBook("1CO", 46, Testament.NEW_TESTAMENT, "1 Corinthians", "1 கொரிந்தியர்", 16, "1Co", "1கொரி"),
        BibleBook("2CO", 47, Testament.NEW_TESTAMENT, "2 Corinthians", "2 கொரிந்தியர்", 13, "2Co", "2கொரி"),
        BibleBook("GAL", 48, Testament.NEW_TESTAMENT, "Galatians", "கலாத்தியர்", 6, "Gal", "கலா"),
        BibleBook("EPH", 49, Testament.NEW_TESTAMENT, "Ephesians", "எபேசியர்", 6, "Eph", "எபே"),
        BibleBook("PHP", 50, Testament.NEW_TESTAMENT, "Philippians", "பிலிப்பியர்", 4, "Php", "பிலிப்"),
        BibleBook("COL", 51, Testament.NEW_TESTAMENT, "Colossians", "கொலோசெயர்", 4, "Col", "கொலோ"),
        BibleBook("1TH", 52, Testament.NEW_TESTAMENT, "1 Thessalonians", "1 தெசலோனிக்கேயர்", 5, "1Th", "1தெச"),
        BibleBook("2TH", 53, Testament.NEW_TESTAMENT, "2 Thessalonians", "2 தெசலோனிக்கேயர்", 3, "2Th", "2தெச"),
        BibleBook("1TI", 54, Testament.NEW_TESTAMENT, "1 Timothy", "1 தீமோத்தேயு", 6, "1Ti", "1தீமோ"),
        BibleBook("2TI", 55, Testament.NEW_TESTAMENT, "2 Timothy", "2 தீமோத்தேயு", 4, "2Ti", "2தீமோ"),
        BibleBook("TIT", 56, Testament.NEW_TESTAMENT, "Titus", "தீத்து", 3, "Tit", "தீத்து"),
        BibleBook("PHM", 57, Testament.NEW_TESTAMENT, "Philemon", "பிலேமோன்", 1, "Phm", "பிலே"),
        BibleBook("HEB", 58, Testament.NEW_TESTAMENT, "Hebrews", "எபிரெயர்", 13, "Heb", "எபி"),
        BibleBook("JAS", 59, Testament.NEW_TESTAMENT, "James", "யாக்கோபு", 5, "Jas", "யாக்"),
        BibleBook("1PE", 60, Testament.NEW_TESTAMENT, "1 Peter", "1 பேதுரு", 5, "1Pe", "1பேது"),
        BibleBook("2PE", 61, Testament.NEW_TESTAMENT, "2 Peter", "2 பேதுரு", 3, "2Pe", "2பேது"),
        BibleBook("1JN", 62, Testament.NEW_TESTAMENT, "1 John", "1 யோவான்", 5, "1Jn", "1யோவா"),
        BibleBook("2JN", 63, Testament.NEW_TESTAMENT, "2 John", "2 யோவான்", 1, "2Jn", "2யோவா"),
        BibleBook("3JN", 64, Testament.NEW_TESTAMENT, "3 John", "3 யோவான்", 1, "3Jn", "3யோவா"),
        BibleBook("JUD", 65, Testament.NEW_TESTAMENT, "Jude", "யூதா", 1, "Jud", "யூதா"),
        BibleBook("REV", 66, Testament.NEW_TESTAMENT, "Revelation", "வெளிப்படுத்தின விசேஷம்", 22, "Rev", "வெளி")
    )

    val oldTestamentBooks: List<BibleBook> = allBooks.filter { it.testament == Testament.OLD_TESTAMENT }
    val newTestamentBooks: List<BibleBook> = allBooks.filter { it.testament == Testament.NEW_TESTAMENT }

    fun findBookById(bookId: String): BibleBook? {
        return allBooks.firstOrNull { it.id.equals(bookId, ignoreCase = true) }
    }

    fun findBookByName(query: String): BibleBook? {
        val q = query.trim().lowercase()
        return allBooks.firstOrNull {
            it.englishName.lowercase() == q ||
            it.tamilName.lowercase() == q ||
            it.abbreviation.lowercase() == q ||
            it.tamilAbbreviation.lowercase() == q ||
            it.englishName.lowercase().startsWith(q) ||
            it.tamilName.lowercase().startsWith(q)
        }
    }
}
