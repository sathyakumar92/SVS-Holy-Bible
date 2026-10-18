package com.example.data.datasource

import com.example.data.model.TopicReferenceEntity
import com.example.data.model.TopicTimelineEntry

/**
 * Builds chronological narrative timeline entries for any Bible topic,
 * arranging passages across the major epochs of biblical history:
 * 1. Creation & Patriarchal Era
 * 2. Exodus, Wilderness & Covenant Law
 * 3. Kingdom of Israel & Temple Era
 * 4. Prophets, Exile & Restoration
 * 5. Incarnation, Life & Ministry of Christ (Gospels)
 * 6. Apostolic Church & Epistles
 * 7. Consummation, New Heaven & Eternity (Revelation)
 *
 * Demonstrates biblical narrative continuity, connecting Old Testament types/prophecies
 * with New Testament fulfillments.
 */
object TopicTimelineEngine {

    fun generateTimelineForTopic(
        topicId: String,
        verifiedReferences: List<TopicReferenceEntity>
    ): List<TopicTimelineEntry> {
        val curatedTimeline = getCuratedTimelineForTopic(topicId)
        if (curatedTimeline.isNotEmpty()) {
            return curatedTimeline
        }

        // Fallback: organize verified references by canonical order into timeline eras
        val sortedRefs = verifiedReferences.sortedBy { ref ->
            val bookOrder = CanonicalBibleData.allBooks.indexOfFirst { it.id == ref.bookId }
            (if (bookOrder >= 0) bookOrder else 99) * 1000 + ref.chapterNumber
        }

        return sortedRefs.mapIndexed { index, ref ->
            val book = CanonicalBibleData.findBookById(ref.bookId)
            val isOT = book?.testament == com.example.data.model.Testament.OLD_TESTAMENT
            val era = when {
                ref.bookId in listOf("GEN", "JOB") -> Era("Patriarchal Era", "பிதாக்களின் காலம்", 1)
                ref.bookId in listOf("EXO", "LEV", "NUM", "DEU", "JOS", "JDG", "RUT") -> Era("Exodus & Law", "யாத்திராகமம் & நியாயப்பிரமாணம்", 2)
                ref.bookId in listOf("1SA", "2SA", "1KI", "2KI", "1CH", "2CH", "PSA", "PRO", "ECC", "SNG") -> Era("Kingdom & Temple", "இஸ்ரவேலின் ராஜ்ய காலம்", 3)
                isOT -> Era("Prophets & Promise", "தீர்க்கதரிசன காலம்", 4)
                ref.bookId in listOf("MAT", "MRK", "LUK", "JHN") -> Era("Christ & Gospels", "கிறிஸ்துவின் சுவிசேஷ காலம்", 5)
                ref.bookId == "REV" -> Era("Eternity & Consummation", "நித்திய மகிமையின் காலம்", 7)
                else -> Era("Apostolic Church", "அப்போஸ்தலர் சபைக்காலம்", 6)
            }

            TopicTimelineEntry(
                id = "${topicId}_timeline_${index + 1}",
                eraEn = era.nameEn,
                eraTa = era.nameTa,
                eraOrder = era.order,
                bookId = ref.bookId,
                chapterNumber = ref.chapterNumber,
                verseStart = ref.verseStart,
                verseEnd = ref.verseEnd,
                referenceEn = ref.referenceEn,
                referenceTa = ref.referenceTa,
                verseTextEn = ref.verseTextEn,
                verseTextTa = ref.verseTextTa,
                eventTitleEn = if (ref.briefExplanationEn.isNotBlank()) ref.briefExplanationEn.take(40) else "${ref.bookNameEn} Passage",
                eventTitleTa = if (ref.briefExplanationTa.isNotBlank()) ref.briefExplanationTa.take(40) else "${ref.bookNameTa} பகுதி",
                narrativeExplanationEn = ref.briefExplanationEn.ifBlank { "Passage revealing God's truth in ${ref.referenceEn}." },
                narrativeExplanationTa = ref.briefExplanationTa.ifBlank { "வேதாகமத்தில் தேவனுடைய சத்தியத்தை வெளிப்படுத்தும் பகுதி." },
                testamentsConnectionEn = if (isOT) "Old Testament foundation pointing forward to Christ's revelation." else "New Testament fulfillment of God's covenant promises.",
                testamentsConnectionTa = if (isOT) "கிறிஸ்துவின் வெளிப்பாட்டை நோக்கிச் சுட்டிக்காட்டும் பழைய ஏற்பாட்டு அஸ்திபாரம்." else "தேவனுடைய உடன்படிக்கை வாக்குத்தத்தங்களின் புதிய ஏற்பாட்டு நிறைவேற்றம்."
            )
        }.distinctBy { it.referenceEn }
    }

    private data class Era(val nameEn: String, val nameTa: String, val order: Int)

    private fun getCuratedTimelineForTopic(topicId: String): List<TopicTimelineEntry> {
        return when (topicId) {
            "faith" -> listOf(
                TopicTimelineEntry(
                    id = "faith_1",
                    eraEn = "Patriarchal Era",
                    eraTa = "பிதாக்களின் காலம்",
                    eraOrder = 1,
                    bookId = "GEN",
                    chapterNumber = 15,
                    verseStart = 6,
                    verseEnd = 6,
                    referenceEn = "Genesis 15:6",
                    referenceTa = "ஆதியாகமம் 15:6",
                    verseTextEn = "And he believed in the LORD; and he counted it to him for righteousness.",
                    verseTextTa = "அவன் கர்த்தரை விசுவாசித்தான், அதை அவருக்கு நீதியாக எண்ணினார்.",
                    eventTitleEn = "Abraham's Covenant Faith",
                    eventTitleTa = "ஆபிரகாமின் உடன்படிக்கை விசுவாசம்",
                    narrativeExplanationEn = "Abraham believed God's impossible promise regarding his offspring, becoming the father of all who believe.",
                    narrativeExplanationTa = "ஆபிரகாம் தேவனுடைய வாக்குத்தத்தத்தை விசுவாசித்தார், இதனால் அவர் விசுவாசிகளுக்கு தகப்பனானார்.",
                    testamentsConnectionEn = "Directly quoted in Romans 4:3 and Galatians 3:6 to prove justification by faith in Christ.",
                    testamentsConnectionTa = "கிறிஸ்துவுக்குள் விசுவாசத்தினால் நீதிமானாக்கப்படுவதை நிரூபிக்க ரோமர் 4:3 மற்றும் கலாத்தியர் 3:6-ல் மேற்கோள் காட்டப்பட்டது."
                ),
                TopicTimelineEntry(
                    id = "faith_2",
                    eraEn = "Exodus & Law",
                    eraTa = "யாத்திராகமம் & நியாயப்பிரமாணம்",
                    eraOrder = 2,
                    bookId = "EXO",
                    chapterNumber = 14,
                    verseStart = 31,
                    verseEnd = 31,
                    referenceEn = "Exodus 14:31",
                    referenceTa = "யாத்திராகமம் 14:31",
                    verseTextEn = "And Israel saw that great work which the LORD did upon the Egyptians: and the people feared the LORD, and believed the LORD, and his servant Moses.",
                    verseTextTa = "கர்த்தர் எகிப்தியரில் செய்த அந்த மகத்தான கிரியையை இஸ்ரவேலர் கண்டார்கள்; அப்பொழுது ஜனங்கள் கர்த்தருக்குப் பயந்து, கர்த்தரிடத்திலும் அவருடைய ஊழியக்காரனாகிய மோசேயினிடத்திலும் விசுவாசம் வைத்தார்கள்.",
                    eventTitleEn = "Deliverance at the Red Sea",
                    eventTitleTa = "செங்கடல் வழியே அற்புத மீட்பு",
                    narrativeExplanationEn = "Witnessing God's mighty arm parting the waters transformed Israel's trembling fear into worshipful faith.",
                    narrativeExplanationTa = "தேவனுடைய வல்லமையைக் கண்ட இஸ்ரவேல் ஜனங்கள் பயத்தை விட்டு தேவனை விசுவாசித்தார்கள்.",
                    testamentsConnectionEn = "1 Corinthians 10:1-4 explains this crossing as a biblical baptism and spiritual type of salvation.",
                    testamentsConnectionTa = "1 கொரிந்தியர் 10:1-4-ல் இது ஞானஸ்நானத்திற்கும் இரட்சிப்பிற்கும் அடையாளமாக விளக்கப்பட்டுள்ளது."
                ),
                TopicTimelineEntry(
                    id = "faith_3",
                    eraEn = "Prophets & Promise",
                    eraTa = "தீர்க்கதரிசன காலம்",
                    eraOrder = 4,
                    bookId = "HAB",
                    chapterNumber = 2,
                    verseStart = 4,
                    verseEnd = 4,
                    referenceEn = "Habakkuk 2:4",
                    referenceTa = "ஆபகூக் 2:4",
                    verseTextEn = "Behold, his soul which is lifted up is not upright in him: but the just shall live by his faith.",
                    verseTextTa = "இதோ, அகந்தையுள்ளவன் எவனோ, அவனுடைய ஆத்துமா அவன் உடலுக்குள் நேர்மையாயிராது; நீதிமானோ தன் விசுவாசத்தினாலே பிழைப்பான்.",
                    eventTitleEn = "The Just Shall Live by Faith",
                    eventTitleTa = "நீதிமான் தன் விசுவாசத்தினால் பிழைப்பான்",
                    narrativeExplanationEn = "In a time of looming national collapse, God reveals that true life is found only through persevering trust.",
                    narrativeExplanationTa = "தேசத்தின் அழிவின் மத்தியில், மெய்யான ஜீவன் தேவனை மட்டுமே நம்பியிருக்கும் விசுவாசத்தால் வருகிறது.",
                    testamentsConnectionEn = "Formed the foundational heartbeat of apostolic theology in Romans 1:17 and Hebrews 10:38.",
                    testamentsConnectionTa = "அப்போஸ்தல உபதேசத்தின் அடித்தளமாக ரோமர் 1:17 மற்றும் எபிரெயர் 10:38-ல் உறுதிப்படுத்தப்பட்டது."
                ),
                TopicTimelineEntry(
                    id = "faith_4",
                    eraEn = "Christ & Gospels",
                    eraTa = "கிறிஸ்துவின் சுவிசேஷ காலம்",
                    eraOrder = 5,
                    bookId = "MAT",
                    chapterNumber = 8,
                    verseStart = 10,
                    verseEnd = 10,
                    referenceEn = "Matthew 8:10",
                    referenceTa = "மத்தேயு 8:10",
                    verseTextEn = "When Jesus heard it, he marvelled, and said to them that followed, Verily I say unto you, I have not found so great faith, no, not in Israel.",
                    verseTextTa = "இயேசு இதைக் கேட்டு ஆச்சரியப்பட்டு, தமக்குப் பின்செல்லுகிறவர்களை நோக்கி: இஸ்ரவேலருக்குள்ளும் நான் இப்படிப்பட்ட விசுவாசத்தைக் காணவில்லை என்று, மெய்யாகவே உங்களுக்குச் சொல்லுகிறேன்.",
                    eventTitleEn = "The Centurion's Great Faith",
                    eventTitleTa = "நூற்றுக்கு அதிபதியின் மாபெரும் விசுவாசம்",
                    narrativeExplanationEn = "A Gentile Roman commander recognized Jesus' sovereign divine authority without needing a visible sign.",
                    narrativeExplanationTa = "புறஜாதி ரோம அதிபதி இயேசுவின் அதிகாரத்தின் வார்த்தையை மட்டுமே முழுமையாக விசுவாசித்தார்.",
                    testamentsConnectionEn = "Foretold that Gentiles from East and West would sit down with Abraham in God's Kingdom.",
                    testamentsConnectionTa = "கிழக்கிலும் மேற்கிலும் இருந்து அநேகர் வந்து ஆபிரகாமோடு பரலோகத்தில் அமர்வார்கள் என்ற வாக்குத்தத்த நிறைவேற்றம்."
                ),
                TopicTimelineEntry(
                    id = "faith_5",
                    eraEn = "Apostolic Church",
                    eraTa = "அப்போஸ்தலர் சபைக்காலம்",
                    eraOrder = 6,
                    bookId = "HEB",
                    chapterNumber = 11,
                    verseStart = 1,
                    verseEnd = 6,
                    referenceEn = "Hebrews 11:1, 6",
                    referenceTa = "எபிரெயர் 11:1, 6",
                    verseTextEn = "Now faith is the substance of things hoped for, the evidence of things not seen... But without faith it is impossible to please him.",
                    verseTextTa = "விசுவாசமானது நம்பப்படுகிறவைகளின் உறுதியும், காணப்படாதவைகளின் நிச்சயமுமாயிருக்கிறது... விசுவாசமில்லாமல் தேவனுக்குப் பிரியமாயிருப்பது கூடாதகாரியம்.",
                    eventTitleEn = "The Hall of Faith & God's Pleasure",
                    eventTitleTa = "விசுவாச வீரர்களின் பட்டியல் & தேவ பிரியம்",
                    narrativeExplanationEn = "Outlines the comprehensive definition of saving faith illustrated across all generations of biblical heroes.",
                    narrativeExplanationTa = "வேதாகம வரலாற்று விசுவாச வீரர்களின் சாட்சியின் மூலம் விசுவாசத்தின் இலக்கணத்தை விவரிக்கிறது.",
                    testamentsConnectionEn = "Connects Abel, Enoch, Noah, Abraham, Moses, and David into one triumphant cloud of witnesses.",
                    testamentsConnectionTa = "ஆபேல், ஏனோக்கு, நோவா, மோசே மற்றும் தாவீதை ஒரே சாட்சிகளின் திரளாய் இணைக்கிறது."
                ),
                TopicTimelineEntry(
                    id = "faith_6",
                    eraEn = "Eternity & Consummation",
                    eraTa = "நித்திய மகிமையின் காலம்",
                    eraOrder = 7,
                    bookId = "REV",
                    chapterNumber = 14,
                    verseStart = 12,
                    verseEnd = 12,
                    referenceEn = "Revelation 14:12",
                    referenceTa = "வெளிப்படுத்தல் 14:12",
                    verseTextEn = "Here is the patience of the saints: here are they that keep the commandments of God, and the faith of Jesus.",
                    verseTextTa = "தேவனுடைய கற்பனைகளையும் இயேசுவின்மேலுள்ள விசுவாசத்தையும் காத்துக்கொள்ளுகிறவர்களாகிய பரிசுத்தவான்களுடைய பொறுமை இதிலே விளங்கும்.",
                    eventTitleEn = "Enduring Faith Victorious Forever",
                    eventTitleTa = "இயேசுவின் விசுவாசத்தில் ஜெயங்கொள்ளுதல்",
                    narrativeExplanationEn = "At the climax of history, the saints overcome all tribulation by keeping the faith of Jesus unto eternity.",
                    narrativeExplanationTa = "வரலாற்றின் முடிவில் பரிசுத்தவான்கள் இயேசுவின் விசுவாசத்தை இறுதிவரை காத்து நித்திய வெற்றி பெறுகிறார்கள்.",
                    testamentsConnectionEn = "Fulfills Genesis 3:15 where the seed of the woman completely crushes the serpent.",
                    testamentsConnectionTa = "ஸ்திரீயின் வித்து சர்ப்பத்தின் தலையை நசுக்கும் என்ற ஆதியாகமம் 3:15 வாக்குத்தத்தத்தின் இறுதி வெற்றி."
                )
            )

            "prayer" -> listOf(
                TopicTimelineEntry(
                    id = "prayer_1",
                    eraEn = "Patriarchal Era",
                    eraTa = "பிதாக்களின் காலம்",
                    eraOrder = 1,
                    bookId = "GEN",
                    chapterNumber = 18,
                    verseStart = 23,
                    verseEnd = 32,
                    referenceEn = "Genesis 18:23-32",
                    referenceTa = "ஆதியாகமம் 18:23-32",
                    verseTextEn = "And Abraham drew near, and said, Wilt thou also destroy the righteous with the wicked?",
                    verseTextTa = "அப்பொழுது ஆபிரகாம் சமீபித்து: துன்மார்க்கனோடே நீதிமானையும் அழிப்பீரோ?",
                    eventTitleEn = "Abraham Intercedes for Sodom",
                    eventTitleTa = "சோதோமுக்காக ஆபிரகாமின் பரிந்துபேசுதல்",
                    narrativeExplanationEn = "Abraham boldly drew near to God in intercession, pleading God's own righteous and merciful nature.",
                    narrativeExplanationTa = "ஆபிரகாம் தேவனுடைய நீதியையும் இரக்கத்தையும் சார்ந்துகொண்டு ஜெபத்தில் பரிந்துபேசினார்.",
                    testamentsConnectionEn = "Foreshadows Christ's perpetual intercession for us before the throne of grace (Hebrews 7:25).",
                    testamentsConnectionTa = "கிருபையின் சிங்காசனத்தில் இயேசு நமக்காக எப்போதும் பரிந்துபேசுவதற்கு நிழலாட்டம் (எபிரெயர் 7:25)."
                ),
                TopicTimelineEntry(
                    id = "prayer_2",
                    eraEn = "Kingdom & Temple",
                    eraTa = "இஸ்ரவேலின் ராஜ்ய காலம்",
                    eraOrder = 3,
                    bookId = "2CH",
                    chapterNumber = 7,
                    verseStart = 14,
                    verseEnd = 14,
                    referenceEn = "2 Chronicles 7:14",
                    referenceTa = "2 நாளாகமம் 7:14",
                    verseTextEn = "If my people, which are called by my name, shall humble themselves, and pray, and seek my face, and turn from their wicked ways; then will I hear from heaven...",
                    verseTextTa = "என் நாமம் தரிக்கப்பட்ட என் ஜனங்கள் தங்களைத் தாழ்த்தி, ஜெபம்பண்ணி, என் முகத்தைத் தேடி, தங்கள் பொல்லாத வழிகளை விட்டுத் திரும்பினால், அப்பொழுது பரலோகத்திலிருக்கிற நான் கேட்டு...",
                    eventTitleEn = "The Promise of Answered Prayer & Healing",
                    eventTitleTa = "ஜெபத்திற்குப் பதிலளிக்கும் வாக்குத்தத்தம்",
                    narrativeExplanationEn = "God's solemn covenant response at Solomon's temple dedication, linking humility and repentance to divine healing.",
                    narrativeExplanationTa = "தேவாலய பிரதிஷ்டையின் போது தேவன் அளித்த வாக்குத்தத்தம்: தாழ்மையும் ஜெபமும் தேசத்திற்கு விடுதலையைத் தரும்.",
                    testamentsConnectionEn = "James 5:16 reaffirms that the effectual fervent prayer of a righteous person availeth much.",
                    testamentsConnectionTa = "நீதிமான் செய்யும் ஊக்கமான வேண்டுதல் மிகுந்த பலனுள்ளது என்று யாக்கோபு 5:16 உறுதிப்படுத்துகிறது."
                ),
                TopicTimelineEntry(
                    id = "prayer_3",
                    eraEn = "Christ & Gospels",
                    eraTa = "கிறிஸ்துவின் சுவிசேஷ காலம்",
                    eraOrder = 5,
                    bookId = "MAT",
                    chapterNumber = 6,
                    verseStart = 9,
                    verseEnd = 13,
                    referenceEn = "Matthew 6:9-13",
                    referenceTa = "மத்தேயு 6:9-13",
                    verseTextEn = "After this manner therefore pray ye: Our Father which art in heaven, Hallowed be thy name...",
                    verseTextTa = "நீங்கள் ஜெபம்பண்ணவேண்டிய விதமாவது: பரமண்டலங்களிலிருக்கிற எங்கள் பிதாவே, உம்முடைய நாமம் பரிசுத்தப்படுவதாக...",
                    eventTitleEn = "The Lord's Model Prayer",
                    eventTitleTa = "கர்த்தர் கற்றுக்கொடுத்த மாதிரி ஜெபம்",
                    narrativeExplanationEn = "Jesus taught His disciples to approach the Almighty as a loving heavenly Father with reverent devotion and daily dependence.",
                    narrativeExplanationTa = "அன்புள்ள பரலோகப் பிதாவிடம் தினசரி விசுவாசத்தோடு சமர்ப்பித்து ஜெபிக்கும் முறையை இயேசு கற்பித்தார்.",
                    testamentsConnectionEn = "Opens access through the torn veil into the Holy of Holies for all believers.",
                    testamentsConnectionTa = "கிறிஸ்துவின் சிலுவை பலியினால் திறக்கப்பட்ட பாதையின் வழியே பிதாவை அணுகும் சிலாக்கியம்."
                ),
                TopicTimelineEntry(
                    id = "prayer_4",
                    eraEn = "Apostolic Church",
                    eraTa = "அப்போஸ்தலர் சபைக்காலம்",
                    eraOrder = 6,
                    bookId = "ACT",
                    chapterNumber = 4,
                    verseStart = 31,
                    verseEnd = 31,
                    referenceEn = "Acts 4:31",
                    referenceTa = "அப்போஸ்தலர் 4:31",
                    verseTextEn = "And when they had prayed, the place was shaken where they were assembled together; and they were all filled with the Holy Ghost, and they spake the word of God with boldness.",
                    verseTextTa = "அவர்கள் ஜெபம்பண்ணினபோது, அவர்கள் கூடியிருந்த இடம் அசைந்தது. அவர்களெல்லாரும் பரிசுத்த ஆவியினால் நிரப்பப்பட்டு, தேவவசனத்தைத் தைரியமாய்ச் சொன்னார்கள்.",
                    eventTitleEn = "Corporate Prayer Shakes the Earth",
                    eventTitleTa = "சபையின் ஐக்கிய ஜெபம் & ஆவியின் வல்லமை",
                    narrativeExplanationEn = "In the face of threats, the early church united in prayer, releasing supernatural boldness and Holy Spirit power.",
                    narrativeExplanationTa = "எதிர்ப்புகளின் மத்தியில் ஆதி சபை ஒருமனப்பட்டு ஜெபித்தபோது பூமி அசைந்தது, அப்போஸ்தலர்கள் தைரியம் பெற்றார்கள்.",
                    testamentsConnectionEn = "Revelation 8:3-4 shows the prayers of the saints ascending as sweet incense before the throne of God.",
                    testamentsConnectionTa = "பரிசுத்தவான்களின் ஜெபங்கள் தேவ சிங்காசனத்தின் முன் தூபவர்க்கமாய் எழும்புகிறது (வெளி 8:3-4)."
                )
            )

            else -> emptyList()
        }
    }
}
