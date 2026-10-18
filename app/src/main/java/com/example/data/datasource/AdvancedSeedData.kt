package com.example.data.datasource

import com.example.data.local.AppDatabase
import com.example.data.model.*

object AdvancedSeedData {

    val biblicalPeriods: List<BiblicalPeriod> = listOf(
        BiblicalPeriod("period_1", "Creation & Patriarchs", "ஆதியாகமம் & முற்பிதாக்கள் காலம்", "~4000 - 1800 BC", "~கி.மு. 4000 - 1800", "From Creation, the Fall, the Flood, to Abraham, Isaac, Jacob, and Joseph.", "சிருஷ்டிப்பு, பெருவெள்ளம் முதல் ஆபிரகாம், ஈசாக்கு, யாக்கோபு, யோசேப்பு வரையான காலம்.", "#D4AF37"),
        BiblicalPeriod("period_2", "Exodus & Wilderness", "யாத்திராகமம் & வனாந்தர யாத்திரை", "~1446 - 1406 BC", "~கி.மு. 1446 - 1406", "Deliverance from Egypt under Moses, giving of the Law at Mount Sinai, and 40 years in the wilderness.", "மோசேயின் தலைமையில் எகிப்திலிருந்து விடுதலை, சீனாய் மலையில் நியாயப்பிரமாணம், 40 ஆண்டு வனாந்தர பயணம்.", "#173F8A"),
        BiblicalPeriod("period_3", "Conquest & Judges", "யோசுவா & நியாயாதிபதிகள் காலம்", "~1406 - 1050 BC", "~கி.மு. 1406 - 1050", "Entering the Promised Land under Joshua and the cycles of deliverance through the Judges.", "யோசுவாவின் தலைமையில் கானான் தேசத்தை சுதந்தரித்தல் மற்றும் நியாயாதிபதிகளின் காலம்.", "#2859A8"),
        BiblicalPeriod("period_4", "United Kingdom", "இஸ்ரவேலின் ஐக்கிய ராஜ்யம்", "~1050 - 930 BC", "~கி.மு. 1050 - 930", "Reigns of Saul, David, and Solomon; building of the First Temple in Jerusalem.", "சவுல், தாவீது, சாலொமோன் ஆகியோரின் ஆட்சி மற்றும் எருசலேம் தேவாலயக் கட்டுமானம்.", "#D4AF37"),
        BiblicalPeriod("period_5", "Divided Kingdom & Prophets", "பிரிந்த ராஜ்யம் & தீர்க்கதரிசிகள்", "~930 - 586 BC", "~கி.மு. 930 - 586", "Israel (North) and Judah (South); the warnings of Elijah, Isaiah, Jeremiah, and the fall of Samaria.", "இஸ்ரவேல் மற்றும் யூதா பிரிவினை; எலியா, ஏசாயா, எரேமியா தீர்க்கதரிசிகளின் ஊழியம்.", "#A88732"),
        BiblicalPeriod("period_6", "Exile & Return", "பாபிலோன் சிறையிருப்பு & திரும்புதல்", "~586 - 400 BC", "~கி.மு. 586 - 400", "Babylonian Captivity, Daniel, Esther, and the rebuilding of the walls under Ezra & Nehemiah.", "பாபிலோனிய சிறையிருப்பு, தானியேல், எஸ்றா, நெகேமியாவின் தலைமையில் எருசலேம் மதில்கள் திரும்புதல்.", "#172D4D"),
        BiblicalPeriod("period_7", "Life and Ministry of Jesus", "இயேசு கிறிஸ்துவின் ஜீவியம் & ஊழியம்", "~4 BC - 30 AD", "~கி.மு. 4 - கி.பி. 30", "Incarnation, teachings, miracles, crucifixion, resurrection, and ascension of our Lord Jesus Christ.", "நம் ஆண்டவராகிய இயேசு கிறிஸ்துவின் பிறப்பு, போதனைகள், சிலுவை மரணம், உயிர்த்தெழுதல், பரமேறுதல்.", "#F2D675"),
        BiblicalPeriod("period_8", "Early Church & Apostles", "ஆதி அப்போஸ்தல சபை & மிஷனரி பயணம்", "~30 - 95 AD", "~கி.பி. 30 - 95", "Day of Pentecost, missionary journeys of Paul, writing of the Epistles, and the Book of Revelation.", "பெந்தேகொஸ்தே நாள், பவுலின் மிஷனரி பயணங்கள், நிருபங்கள் மற்றும் வெளிப்படுத்தின விசேஷம்.", "#27865C")
    )

    val timelineEvents: List<TimelineEvent> = listOf(
        TimelineEvent(
            id = "event_call_abraham",
            periodId = "period_1",
            chronologicalOrder = 1,
            titleEn = "The Call of Abraham",
            titleTa = "ஆபிரகாமின் அழைப்பு",
            approximateDate = "~2091 BC",
            dateUncertaintyNote = "Approximate traditional biblical chronology based on Genesis genealogies.",
            summaryEn = "God commands Abram to leave Haran for Canaan, establishing the Abrahamic covenant.",
            summaryTa = "ஆபிரகாமை தன் தேசத்தை விட்டு கானானுக்கு புறப்படக் கட்டளையிட்டு நித்திய உடன்படிக்கையை ஏற்படுத்துகிறார்.",
            historicalContextEn = "Marks the foundational covenant of redemption where all nations of the earth are promised blessing.",
            historicalContextTa = "பூமியின் வம்சங்களெல்லாம் ஆசீர்வதிக்கப்படும் என்ற மீட்பின் உடன்படிக்கையின் ஆரம்பம்.",
            primaryScriptureRef = "Genesis 12:1-4",
            bookId = "GEN",
            chapter = 12,
            verse = 1,
            keyFigures = "Abraham, Sarah",
            linkedLocationId = "loc_jerusalem"
        ),
        TimelineEvent(
            id = "event_the_exodus",
            periodId = "period_2",
            chronologicalOrder = 2,
            titleEn = "The Exodus from Egypt",
            titleTa = "எகிப்திலிருந்து யாத்திராகமம்",
            approximateDate = "~1446 BC",
            dateUncertaintyNote = "Scholars propose early (~1446 BC) or late (~1260 BC) dates. The early date harmonizes with 1 Kings 6:1.",
            summaryEn = "God delivers the Israelites from slavery under Pharaoh through Moses, crossing the Red Sea.",
            summaryTa = "தேவன் மோசேயின் மூலம் இஸ்ரவேல் ஜனங்களை பார்வோனின் அடிமைத்தனத்திலிருந்து செங்கடலைக் கடந்து மீட்கிறார்.",
            historicalContextEn = "The defining redemption event of the Old Covenant symbolizing deliverance from sin.",
            historicalContextTa = "பாவ அடிமைத்தனத்திலிருந்து விடுதலைக்கு அடையாளமான பழைய உடன்படிக்கையின் மாபெரும் மீட்பு.",
            primaryScriptureRef = "Exodus 14:21-31",
            bookId = "EXO",
            chapter = 14,
            verse = 21,
            keyFigures = "Moses, Aaron, Miriam",
            linkedLocationId = "loc_sinai"
        ),
        TimelineEvent(
            id = "event_giving_law",
            periodId = "period_2",
            chronologicalOrder = 3,
            titleEn = "Giving of the Law at Mount Sinai",
            titleTa = "சீனாய் மலையில் நியாயப்பிரமாணம்",
            approximateDate = "~1446 BC",
            dateUncertaintyNote = "Traditional chronology immediately following the Exodus.",
            summaryEn = "God speaks the Ten Commandments and gives the covenant laws and Tabernacle blueprint.",
            summaryTa = "தேவன் பத்துக்கட்டளைகளையும் உடன்படிக்கை பிரமாணங்களையும் ஆசரிப்புக் கூடாரத்தின் மாதிரியையும் அருளுகிறார்.",
            historicalContextEn = "Establishment of Israel as a kingdom of priests and a holy nation under God's law.",
            historicalContextTa = "இஸ்ரவேல் தேவனுடைய பரிசுத்த ஜனமாகவும் ஆசாரிய ராஜ்யமாகவும் நிலைநாட்டப்படுதல்.",
            primaryScriptureRef = "Exodus 20:1-17",
            bookId = "EXO",
            chapter = 20,
            verse = 1,
            keyFigures = "Moses",
            linkedLocationId = "loc_sinai"
        ),
        TimelineEvent(
            id = "event_david_king",
            periodId = "period_4",
            chronologicalOrder = 4,
            titleEn = "David Anointed King of Israel",
            titleTa = "தாவீது இஸ்ரவேலின் ராஜாவாக முடிசூட்டப்படுதல்",
            approximateDate = "~1010 BC",
            dateUncertaintyNote = "Regnal years well supported by ancient Near Eastern synchronisms.",
            summaryEn = "David reigns over Judah in Hebron and subsequently unites all twelve tribes in Jerusalem.",
            summaryTa = "தாவீது எப்ரோனிலும் பின்பு எருசலேமிலும் 40 ஆண்டுகள் இஸ்ரவேலின் ராஜாவாக ஆட்சி செய்கிறார்.",
            historicalContextEn = "God establishes the Davidic Covenant, promising an everlasting throne fulfilled in Jesus the Messiah.",
            historicalContextTa = "இயேசு கிறிஸ்துவில் நிறைவேறும் நித்திய சிங்காசனத்தின் உடன்படிக்கை தாவீதுக்கு வாக்களிக்கப்படுகிறது.",
            primaryScriptureRef = "2 Samuel 5:1-5",
            bookId = "2SA",
            chapter = 5,
            verse = 1,
            keyFigures = "David, Samuel, Joab",
            linkedLocationId = "loc_jerusalem"
        ),
        TimelineEvent(
            id = "event_temple_built",
            periodId = "period_4",
            chronologicalOrder = 5,
            titleEn = "Solomon Dedicates the First Temple",
            titleTa = "சாலொமோனின் தேவாலயப் பிரதிஷ்டை",
            approximateDate = "~959 BC",
            dateUncertaintyNote = "Dated to the 11th year of Solomon's reign.",
            summaryEn = "Solomon completes and dedicates the magnificent temple of God on Mount Moriah in Jerusalem.",
            summaryTa = "சாலொமோன் எருசலேமில் மோரியா மலையில் கட்டிய கர்த்தரின் பரிசுத்த ஆலயம் பிரதிஷ்டை செய்யப்படுகிறது.",
            historicalContextEn = "The glory of the Lord visibly fills the house of God as a dwelling place among His people.",
            historicalContextTa = "கர்த்தரின் மகிமை தேவ ஆலயத்தை நிரப்புகிறது; ஜெபத்திற்குரிய ஆலயமாக மாறுகிறது.",
            primaryScriptureRef = "1 Kings 8:10-21",
            bookId = "1KI",
            chapter = 8,
            verse = 10,
            keyFigures = "Solomon",
            linkedLocationId = "loc_jerusalem"
        ),
        TimelineEvent(
            id = "event_birth_jesus",
            periodId = "period_7",
            chronologicalOrder = 6,
            titleEn = "Birth of Jesus Christ in Bethlehem",
            titleTa = "பெத்லகேமில் இயேசு கிறிஸ்துவின் பிறப்பு",
            approximateDate = "~4 BC",
            dateUncertaintyNote = "Calculated before the death of Herod the Great in 4 BC.",
            summaryEn = "The Son of God is born of the virgin Mary in Bethlehem of Judea, fulfilling Micah 5:2.",
            summaryTa = "தேவகுமாரன் கன்னி மரியாள் மூலம் பெத்லகேமில் அவதரித்து, தீர்க்கதரிசனங்களை நிறைவேற்றுகிறார்.",
            historicalContextEn = "The Word became flesh and dwelt among us; the arrival of the promised Savior of the world.",
            historicalContextTa = "வார்த்தை மாம்சமாகி உலகிற்கு இரட்சகராக தோன்றிய உன்னத பிறப்பு.",
            primaryScriptureRef = "Luke 2:1-14",
            bookId = "LUK",
            chapter = 2,
            verse = 1,
            keyFigures = "Jesus, Mary, Joseph",
            linkedLocationId = "loc_bethlehem"
        ),
        TimelineEvent(
            id = "event_ministry_galilee",
            periodId = "period_7",
            chronologicalOrder = 7,
            titleEn = "Jesus' Ministry in Galilee & Sermon on the Mount",
            titleTa = "கலிலேயாவில் ஊழியம் & மலைப்பிரசங்கம்",
            approximateDate = "~28 AD",
            dateUncertaintyNote = "Occurs during the second year of Jesus' public ministry.",
            summaryEn = "Jesus preaches the Beatitudes, heals the sick, and declares the principles of the Kingdom of God.",
            summaryTa = "இயேசு மலைப்பிரசங்கத்தை உபதேசித்து, நோய்களைக் குணமாக்கி, தேவராஜ்யத்தை அறிவிக்கிறார்.",
            historicalContextEn = "The definitive ethical and spiritual charter of the Kingdom of Heaven.",
            historicalContextTa = "பரலோக ராஜ்யத்தின் ஆவிக்குரிய வாழ்க்கை நெறிமுறைகளை வெளிப்படுத்தும் போதனை.",
            primaryScriptureRef = "Matthew 5:1-16",
            bookId = "MAT",
            chapter = 5,
            verse = 1,
            keyFigures = "Jesus, The Twelve Apostles",
            linkedLocationId = "loc_galilee"
        ),
        TimelineEvent(
            id = "event_resurrection",
            periodId = "period_7",
            chronologicalOrder = 8,
            titleEn = "Crucifixion and Resurrection of Jesus",
            titleTa = "இயேசுவின் சிலுவை மரணமும் உயிர்த்தெழுதலும்",
            approximateDate = "~30 AD",
            dateUncertaintyNote = "Typically dated to Friday, Nisan 14 in 30 AD (or 33 AD).",
            summaryEn = "Christ dies for the sins of the world at Calvary, is buried, and rises victorious on the third day.",
            summaryTa = "இயேசு கிறிஸ்து கல்வாரியில் பாவங்களுக்காக மரித்து, மூன்றாம் நாளில் மரணத்தை ஜெயித்து உயிர்த்தெழுகிறார்.",
            historicalContextEn = "The pinnacle of biblical revelation: sin and death are defeated, granting eternal life.",
            historicalContextTa = "முழு வேதாகமத்தின் உச்சக்கட்டம்: பாவமும் மரணமும் வெல்லப்பட்டு நித்திய ஜீவன் அருளப்படுதல்.",
            primaryScriptureRef = "John 19:30, Luke 24:1-7",
            bookId = "JHN",
            chapter = 19,
            verse = 30,
            keyFigures = "Jesus Christ, Mary Magdalene, Peter, John",
            linkedLocationId = "loc_jerusalem"
        ),
        TimelineEvent(
            id = "event_pentecost",
            periodId = "period_8",
            chronologicalOrder = 9,
            titleEn = "Day of Pentecost & Birth of the Church",
            titleTa = "பெந்தேகொஸ்தே நாள் & திருச்சபையின் தொடக்கம்",
            approximateDate = "~30 AD",
            dateUncertaintyNote = "Fifty days after the resurrection of Christ.",
            summaryEn = "The Holy Spirit is poured out upon the disciples in Jerusalem; 3,000 souls are baptized.",
            summaryTa = "பரிசுத்த ஆவியானவர் சீஷர்கள்மேல் ஊற்றப்படுகிறார்; பேதுருவின் பிரசங்கத்தால் மூவாயிரம் பேர் இரட்சிக்கப்படுகின்றனர்.",
            historicalContextEn = "Empowerment of the global Church for worldwide gospel witness.",
            historicalContextTa = "உலகெங்கும் நற்செய்தியை அறிவிக்க பரிசுத்த ஆவியின் வல்லமையால் திருச்சபை அனுப்பப்படுதல்.",
            primaryScriptureRef = "Acts 2:1-41",
            bookId = "ACT",
            chapter = 2,
            verse = 1,
            keyFigures = "Peter, The Apostles",
            linkedLocationId = "loc_jerusalem"
        ),
        TimelineEvent(
            id = "event_paul_journeys",
            periodId = "period_8",
            chronologicalOrder = 10,
            titleEn = "Apostle Paul's Missionary Journeys",
            titleTa = "அப்போஸ்தலன் பவுலின் மிஷனரி பயணங்கள்",
            approximateDate = "~47 - 62 AD",
            dateUncertaintyNote = "Covering three missionary tours through Asia Minor, Greece, and voyage to Rome.",
            summaryEn = "Paul plants churches throughout the Roman Empire and pens inspired apostolic epistles.",
            summaryTa = "பவுல் ஆசியா, கிரீஸ் மற்றும் ரோமப் பேரரசெங்கும் சபைகளை நிறுவி பரிசுத்த நிருபங்களை எழுதுகிறார்.",
            historicalContextEn = "Expansion of the Gospel to the Gentiles across the Mediterranean world.",
            historicalContextTa = "மத்தியதரைக் கடல் உலகெங்கும் புறஜாதிகளுக்கு சுவிசேஷம் பரவுதல்.",
            primaryScriptureRef = "Acts 13:1-4, Romans 1:16",
            bookId = "ACT",
            chapter = 13,
            verse = 1,
            keyFigures = "Paul, Barnabas, Silas, Timothy",
            linkedLocationId = "loc_antioch"
        )
    )

    val biblicalLocations: List<BiblicalLocation> = listOf(
        BiblicalLocation("loc_jerusalem", "Jerusalem", "எருசலேம்", "Jerusalem (Al-Quds)", 31.7683, 35.2137, "All Biblical Eras", "Psalm 122:6, Luke 24:47", "City of Peace, Mount Zion, capital of David, location of the Temple, and site of Christ's resurrection.", "தாவீதின் நகரம், சீயோன் மலை, தேவாலயத்தின் இருப்பிடம் மற்றும் கிறிஸ்துவின் சிலுவை & உயிர்த்தெழுதல் நிகழ்ந்த இடம்.", true),
        BiblicalLocation("loc_bethlehem", "Bethlehem", "பெத்லகேம்", "Beit Lahm", 31.7054, 35.2024, "Patriarchs, United Kingdom, Gospels", "Micah 5:2, Matthew 2:1", "City of David in Judah, birth place of Jesus Christ.", "தாவீதின் ஊர், இயேசு கிறிஸ்து மாட்டுக்கொட்டிலில் பிறந்த புனித ஸ்தலம்.", true),
        BiblicalLocation("loc_nazareth", "Nazareth", "நாசரேத்து", "En-Nasira", 32.6996, 35.3035, "Gospels", "Matthew 2:23, Luke 1:26", "Town in lower Galilee where Jesus grew in wisdom, stature, and favor.", "கலிலேயாவின் ஊர்; இயேசு கிறிஸ்து வளர்ந்த இடம்.", true),
        BiblicalLocation("loc_sinai", "Mount Sinai", "சீனாய் மலை", "Jabal Musa", 28.5392, 33.9750, "Exodus", "Exodus 19:18, Galatians 4:24", "The mountain in the desert where God met Moses and gave the Ten Commandments.", "தேவன் மோசேயோடு பேசி பத்துக்கட்டளைகளை அருளிய பரிசுத்த பர்வதம்.", true),
        BiblicalLocation("loc_galilee", "Sea of Galilee", "கலிலேயா கடல்", "Lake Kinneret", 32.8258, 35.5898, "Gospels", "Matthew 4:18, Mark 4:39", "Freshwater lake in northern Israel where Jesus called fishermen, walked on water, and calmed the storm.", "இயேசு புயலை அடக்கி, தண்ணீரிலே நடந்து, சீஷர்களை அழைத்த நன்னீர் ஏரி.", true),
        BiblicalLocation("loc_jericho", "Jericho", "எரிகோ", "Ariha", 31.8560, 35.4630, "Conquest, Gospels", "Joshua 6:20, Luke 19:1", "One of the oldest inhabited cities; walls fell before Joshua; home of Zacchaeus.", "யோசுவாவின் முன் மதில்கள் விழுந்த நகரம்; சகேயு இயேசுவை சந்தித்த இடம்.", true),
        BiblicalLocation("loc_antioch", "Antioch in Syria", "சிரியா அந்தியோகியா", "Antakya (Turkey)", 36.2021, 36.1606, "Early Church", "Acts 11:26, Acts 13:1", "Center of Gentile Christianity where disciples were first called 'Christians' and base of Paul's missions.", "சீஷர்கள் முதல்முதலாக 'கிறிஸ்தவர்கள்' என்று அழைக்கப்பட்ட இடம்; பவுலின் மிஷனரி தளம்.", true),
        BiblicalLocation("loc_babylon", "Babylon", "பாபிலோன்", "Hillah (Iraq)", 32.5363, 44.4208, "Exile", "Daniel 1:1, Psalm 137:1", "Ancient Mesopotamian empire capital on the Euphrates where Judah lived in exile for 70 years.", "யூதா 70 ஆண்டுகள் சிறையிருப்பில் இருந்த யூப்ரடீஸ் நதிக்கரையின் பண்டைய நகரம்.", true),
        BiblicalLocation("loc_damascus", "Damascus", "தமஸ்கு", "Dimashq (Syria)", 33.5138, 36.2765, "Early Church", "Acts 9:3, Galatians 1:17", "Ancient city on the road to which Saul had his life-transforming encounter with the risen Christ.", "தமஸ்கு செல்லும் வழியில் சவுல் உயிர்த்தெழுந்த கிறிஸ்துவை தரிசித்து இரட்சிக்கப்பட்ட இடம்.", true),
        BiblicalLocation("loc_rome", "Rome", "ரோமாபுரி", "Rome (Italy)", 41.9028, 12.4964, "Early Church", "Acts 28:16, Romans 1:7", "Imperial capital where Paul was imprisoned and bore witness to Christ.", "ரோமப் பேரரசின் தலைநகரம்; பவுல் சிறையிலிருந்து நற்செய்தியை பறைசாற்றிய ஸ்தலம்.", true)
    )

    suspend fun seedInitialData(database: AppDatabase) {
        // Seed timeline events & locations
        database.timelineDao().insertEvents(timelineEvents)
        database.biblicalLocationsDaoWorkaround(biblicalLocations)

        // Seed initial notebook folders
        database.notebookDao().insertFolder(NotebookFolder(nameEn = "Personal Devotions", nameTa = "தனிப்பட்ட தியானங்கள்", colorHex = "#173F8A"))
        database.notebookDao().insertFolder(NotebookFolder(nameEn = "Sunday Sermons", nameTa = "ஞாயிறு பிரசங்கங்கள்", colorHex = "#D4AF37"))
        database.notebookDao().insertFolder(NotebookFolder(nameEn = "Character Studies", nameTa = "வேதாகம நபர்கள் ஆய்வு", colorHex = "#2859A8"))
        database.notebookDao().insertFolder(NotebookFolder(nameEn = "Doctrine & Theology", nameTa = "உபதேசங்கள் & கோட்பாடுகள்", colorHex = "#27865C"))

        // Seed guided starter note
        val starterNoteId = database.notebookDao().insertNote(
            NotebookNote(
                title = "Living in the Shepherd's Care (Psalm 23)",
                content = "A profound meditation on the peace, guidance, and eternal provision found in Jesus Christ.",
                observation = "The Lord personally guides, restores, and prepares a table even in the presence of enemies.",
                interpretation = "David writes from intimate pastoral experience, knowing God's covenant loyalty (hesed) never fails.",
                application = "In times of anxiety, entrust my daily path completely into the hands of the Good Shepherd.",
                prayer = "Lord Jesus, lead me beside the still waters and restore my soul today. Amen.",
                tags = "Peace, Guidance, Psalms",
                isPinned = true,
                isFavorite = true
            )
        )
        database.notebookDao().insertVerseRef(
            NoteVerseRef(
                noteId = starterNoteId,
                translationId = "KJV",
                bookId = "PSA",
                bookName = "Psalms",
                chapterNumber = 23,
                startVerse = 1,
                endVerse = 6,
                verseText = "The LORD is my shepherd; I shall not want."
            )
        )

        // Seed pre-curated core memorization verses
        val memoryVerses = listOf(
            MemorizedVerse(
                bookId = "JHN", bookName = "John", chapterNumber = 3, verseNumber = 16,
                referenceDisplayEn = "John 3:16", referenceDisplayTa = "யோவான் 3:16",
                verseTextEn = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.",
                verseTextTa = "தேவன், தம்முடைய ஒரேபேறான குமாரனை விசுவாசிக்கிற எவனும் கெட்டுப்போகாமல் நித்தியஜீவனை அடையும்படிக்கு, அவரைத் தந்தருளி, இவ்வளவாய் உலகத்தில் அன்புகூர்ந்தார்.",
                translationId = "KJV", category = "Salvation & Love", isFavorite = true
            ),
            MemorizedVerse(
                bookId = "PSA", bookName = "Psalms", chapterNumber = 23, verseNumber = 1,
                referenceDisplayEn = "Psalm 23:1", referenceDisplayTa = "சங்கீதம் 23:1",
                verseTextEn = "The LORD is my shepherd; I shall not want.",
                verseTextTa = "கர்த்தர் என் மேய்ப்பராயிருக்கிறார்; நான் தாழ்ச்சியடையேன்.",
                translationId = "KJV", category = "Peace & Trust", isFavorite = true
            ),
            MemorizedVerse(
                bookId = "PHP", bookName = "Philippians", chapterNumber = 4, verseNumber = 13,
                referenceDisplayEn = "Philippians 4:13", referenceDisplayTa = "பிலிப்பியர் 4:13",
                verseTextEn = "I can do all things through Christ which strengtheneth me.",
                verseTextTa = "என்னைப்பலப்படுத்துகிற கிறிஸ்துவினாலே எல்லாவற்றையுஞ்செய்ய எனக்குப் பெலனுண்டு.",
                translationId = "KJV", category = "Strength & Hope", isFavorite = true
            ),
            MemorizedVerse(
                bookId = "ROM", bookName = "Romans", chapterNumber = 8, verseNumber = 28,
                referenceDisplayEn = "Romans 8:28", referenceDisplayTa = "ரோமர் 8:28",
                verseTextEn = "And we know that all things work together for good to them that love God, to them who are the called according to his purpose.",
                verseTextTa = "அன்றியும், அவருடைய தீர்மானத்தின்படி அழைக்கப்பட்டவர்களாய் தேவனிடத்தில் அன்புகூருகிறவர்களுக்குச் சகலமும் நன்மைக்கு ஏதுவாக நடக்கிறது என்று அறிந்திருக்கிறோம்.",
                translationId = "KJV", category = "God's Promises", isFavorite = true
            )
        )
        memoryVerses.forEach { database.memorizationDao().insertVerse(it) }

        // Seed starter prayer entries
        val prayer1Id = database.prayerJournalDao().insertPrayer(
            PrayerEntry(
                title = "Prayer for Family Spiritual Growth & Peace",
                description = "Asking the Lord for unity, faith, protection, and spiritual renewal across our household.",
                category = "Family",
                status = PrayerStatus.ACTIVE,
                isFavorite = true,
                attachedScriptureRef = "Joshua 24:15",
                attachedScriptureText = "As for me and my house, we will serve the LORD."
            )
        )
        database.prayerJournalDao().insertUpdate(
            PrayerUpdate(prayerId = prayer1Id, updateText = "Started daily evening family prayer. Experiencing profound peace.")
        )

        val prayer2Id = database.prayerJournalDao().insertPrayer(
            PrayerEntry(
                title = "Healing and Complete Recovery for Loved One",
                description = "Lifting up recovery from recent illness and thanking the Lord for merciful physician guidance.",
                category = "Health",
                status = PrayerStatus.ANSWERED,
                isFavorite = true,
                attachedScriptureRef = "Psalm 103:2-3",
                attachedScriptureText = "Bless the LORD, O my soul, who healeth all thy diseases.",
                answeredDateTimestamp = System.currentTimeMillis() - 86400000L,
                answeredTestimony = "The Lord answered mightily! Tests came back fully clear and strength has returned. Glory to God!"
            )
        )
        database.prayerJournalDao().insertUpdate(
            PrayerUpdate(prayerId = prayer2Id, updateText = "Doctor confirmed complete recovery. Testifying God's grace in church.")
        )

        // Seed starter Sermon & Lesson Builder template documents
        val doc1Id = database.sermonDao().insertDocument(
            SermonDocument(
                title = "Walking in Divine Peace",
                subtitleOrTheme = "Overcoming Anxiety Through God's Presence",
                docType = SermonDocType.SERMON_OUTLINE,
                status = SermonDocStatus.IN_PROGRESS,
                audience = "Sunday Morning Congregation",
                mainPassageRef = "Philippians 4:4-7",
                mainPassageText = "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God."
            )
        )
        database.sermonDao().insertSections(
            listOf(
                SermonSection(documentId = doc1Id, sectionOrder = 1, sectionTitle = "1. Introduction: The Reality of Modern Anxiety", content = "We live in an anxious world, yet Scripture offers a peace that surpasses understanding.", attachedReferences = "John 14:27"),
                SermonSection(documentId = doc1Id, sectionOrder = 2, sectionTitle = "2. Main Point 1: Rejoice in the Lord Always", content = "Joy is rooted in the unchanging character of God, not fluctuating earthly circumstances.", attachedReferences = "Philippians 4:4"),
                SermonSection(documentId = doc1Id, sectionOrder = 3, sectionTitle = "3. Main Point 2: The Antidote of Prayer with Thanksgiving", content = "Transforming worry into worship through specific prayer and grateful hearts.", attachedReferences = "Philippians 4:6"),
                SermonSection(documentId = doc1Id, sectionOrder = 4, sectionTitle = "4. Main Point 3: The Garrison of God's Peace", content = "God's supernatural peace acts as a sentinel guarding hearts and minds in Christ Jesus.", attachedReferences = "Philippians 4:7"),
                SermonSection(documentId = doc1Id, sectionOrder = 5, sectionTitle = "5. Practical Application & Closing Prayer", content = "Commit three specific worries to God today in prayer with immediate thanksgiving.", attachedReferences = "1 Peter 5:7")
            )
        )

        // Seed Smart Reading Plans
        val smartPlans = listOf(
            SmartPlanEntity(
                id = "plan_bible_one_year",
                titleEn = "Read the Bible in One Year",
                titleTa = "ஒரு வருடத்தில் முழு வேதாகமம்",
                descriptionEn = "Daily portions across the Old and New Testament to complete the entire Bible in 365 days.",
                descriptionTa = "365 நாட்களில் பழைய மற்றும் புதிய ஏற்பாட்டின் பகுதிகளை வாசித்து முழு வேதாகமத்தை நிறைவு செய்யும் திட்டம்.",
                durationDays = 365,
                category = "Full Bible"
            ),
            SmartPlanEntity(
                id = "plan_nt_90_days",
                titleEn = "New Testament in 90 Days",
                titleTa = "90 நாட்களில் புதிய ஏற்பாடு",
                descriptionEn = "Read through Matthew to Revelation in three focused months of spiritual study.",
                descriptionTa = "மத்தேயு முதல் வெளிப்படுத்தின விசேஷம் வரை மூன்று மாதங்களில் புதிய ஏற்பாட்டை வாசியுங்கள்.",
                durationDays = 90,
                category = "New Testament"
            ),
            SmartPlanEntity(
                id = "plan_30_gospels",
                titleEn = "30-Day Gospel Journey",
                titleTa = "30 நாட்கள் சுவிசேஷங்களின் பயணம்",
                descriptionEn = "Walk through the life, teachings, cross, and resurrection of Jesus Christ.",
                descriptionTa = "இயேசு கிறிஸ்துவின் உபதேசங்கள், சிலுவை மரணம் மற்றும் உயிர்த்தெழுதலை 30 நாட்களில் தியானியுங்கள்.",
                durationDays = 30,
                category = "Gospels"
            ),
            SmartPlanEntity(
                id = "plan_30_psalms",
                titleEn = "30 Days in the Psalms",
                titleTa = "30 நாட்கள் சங்கீதங்களின் ஆராதனை",
                descriptionEn = "Daily devotional immersion into praise, comfort, protection, and worship.",
                descriptionTa = "துதி, ஆறுதல், அடைக்கலம் மற்றும் ஆராதனை நிறைந்த சங்கீதங்களை நாள்தோறும் வாசியுங்கள்.",
                durationDays = 30,
                category = "Psalms"
            )
        )
        smartPlans.forEach { database.smartPlanDao().insertPlan(it) }

        // Seed 30 days of assignments for 30-Day Gospel Journey
        val gospelAssignments = (1..30).map { day ->
            val book = if (day <= 10) "Matthew" else if (day <= 18) "Mark" else if (day <= 25) "Luke" else "John"
            val ch = ((day - 1) % 8) + 1
            SmartPlanAssignment(
                planId = "plan_30_gospels",
                dayNumber = day,
                titleEn = "Day $day: $book Chapter $ch",
                titleTa = "நாள் $day: $book அதிகாரம் $ch",
                passagesJson = "$book $ch"
            )
        }
        database.smartPlanDao().insertAssignments(gospelAssignments)
    }

    private suspend fun AppDatabase.biblicalLocationsDaoWorkaround(locations: List<BiblicalLocation>) {
        timelineDao().insertLocations(locations)
    }
}
