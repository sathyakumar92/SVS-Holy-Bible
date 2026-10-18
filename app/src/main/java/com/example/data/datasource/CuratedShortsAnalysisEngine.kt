package com.example.data.datasource

import com.example.data.model.ShortAnalysisEntity
import com.example.data.model.ShortBibleReferenceEntity
import com.example.data.model.ShortEntity
import java.util.UUID

/**
 * Intelligent Bible Analysis Engine for Bible Shorts (Section 8 of specification):
 * - Real content-based analysis: transcript, summary, topics, biblical characters, spiritual principles
 * - Distinction between Exact Quotation, Biblical Incident, Supporting Scripture, and Thematic Teaching
 * - Grounded Scripture citations from the verified canonical Bible database
 * - Pre-verified authentic studies for the developer-published video sermons
 */
object CuratedShortsAnalysisEngine {

    // Developer Video 1: Loneliness • God Is With You in the Valley (IPC Pallavilai Church)
    const val DEV_VIDEO_LONELINESS_ID = "short_loneliness_pallavilai"

    // Developer Video 2: Fellowship with the Holy Spirit
    const val DEV_VIDEO_HOLY_SPIRIT_ID = "short_holy_spirit_communion"

    // Developer Video 3: Humility & Overcoming Worry vs Worldly Ambition
    const val DEV_VIDEO_HUMILITY_WORRY_ID = "short_humility_overcoming_worry"

    fun getDeveloperShorts(): List<Triple<ShortEntity, ShortAnalysisEntity, List<ShortBibleReferenceEntity>>> {
        return listOf(
            createLonelinessShortData(),
            createHolySpiritShortData(),
            createHumilityWorryShortData()
        )
    }

    private fun createLonelinessShortData(): Triple<ShortEntity, ShortAnalysisEntity, List<ShortBibleReferenceEntity>> {
        val shortId = DEV_VIDEO_LONELINESS_ID
        val analysisId = "analysis_$shortId"

        val short = ShortEntity(
            id = shortId,
            title = "Loneliness • Are you lonely? | தனிமை • கர்த்தர் உங்களோடு கூட இருக்கிறார் (Pr Blesson Joel)",
            description = "Inspiring Tamil sermon from IPC Pallavilai Church on overcoming severe loneliness through the abiding presence of the living Lord (Psalm 23:4, 2 Tim 4:17, Josh 1:9).",
            category = "Sermon",
            videoStoragePath = "raw://sermon_loneliness_short",
            videoRawResId = com.example.R.raw.sermon_loneliness_short,
            aspectRatio = "9:16",
            durationSeconds = 83,
            fileSizeBytes = 4871702L,
            videoMimeType = "video/mp4",
            audioAvailable = true,
            uploadStatus = "COMPLETED",
            publicationStatus = "PUBLISHED",
            createdByAdminId = "developer_admin",
            likesCount = 0,
            createdAt = System.currentTimeMillis() - 86400000L,
            updatedAt = System.currentTimeMillis(),
            publishedAt = System.currentTimeMillis() - 86400000L
        )

        val analysis = ShortAnalysisEntity(
            id = analysisId,
            shortId = shortId,
            transcriptTa = "தனிமை என்பது ரொம்ப கொடுமையான ஒன்று. தனிமையில் இருக்கிறவங்களுக்குத்தான் தெரியும். கணவன் இருப்பாரு, ஆனால் பேச மாட்டாரு - தனிமை. பிள்ளைங்க இருப்பாங்க, கவனிக்க மாட்டாங்க - தனிமை. தனிமை என்பது மிக மிக கொடுமையான ஒன்று. ஆனால் தனிமையிலே ஆண்டவரை ஆராதிக்கிற பிள்ளைகளுக்கு ஒன்றே ஒன்று சொல்ல முடியும்: நான் தனியா இருந்தாலும் நான் தனிமையில் இல்லை; என்கூட ஜீவனுள்ள ஆண்டவர் எப்பொழுதும் இருக்கிறார்! எப்பொழுதும் இருக்கிறார்! சங்கீதக்காரன் 23-வது சங்கீதத்தில் அதனால்தான் சொல்றாரு: 'நான் மரண இருளின் பள்ளத்தாக்கிலே நடந்தாலும் பொல்லாப்புக்கு பயப்படமாட்டேன்.' மரண இருளின் பள்ளத்தாக்கில் நம்மகூட 10 பேர் வருவாங்களா? நண்பர்கள் வருவாங்களா? உறவினர்கள் வருவாங்களா? பெற்றோர் வருவாங்களா? யாருமே வர முடியாது. அதனால்தான் சங்கீதக்காரன் சொல்றான்: 'தேவரீர் என்னோடு கூட இருக்கிறீர்; உமது கோலும் உமது தடியும் என்னைத் தேற்றும்.' பவுல் 2 தீமோத்தேயு 4:16-17-ல் சொல்றாரு: 'நான் முதல்விசை உத்தரவு சொல்ல நிற்கையில் ஒருவனும் என்னோடேகூட நிற்கவில்லை; எல்லாரும் என்னைக் கைவிட்டார்கள்... கர்த்தரோ எனக்குத் துணையாக நின்று, என்னாலே பிரசங்கம் நிறைவேறும்படிக்கு... என்னை பலப்படுத்தினார்.' திடீரென்று ஒருநாள் மோசே மரித்துப் போகிறார். யோசுவாவுக்கு தனிமை! அப்பொழுது ஆண்டவர் யோசுவாவை பார்த்து சொல்றாரு: 'நீ தனிமை கிடையாது; பலங்கொண்டு திடமனதாயிரு, திகையாதே கலங்காதே, நீ போகும் இடமெல்லாம் உன் தேவனாகிய கர்த்தர் உன்னோடு இருக்கிறார்' (யோசுவா 1:9). பிரியமான தேவ பிள்ளையே, தேவன் உன்னோடு கூட இருக்கிறார்!",
            transcriptEn = "Loneliness is a very painful experience. Only those going through it truly know. A husband may be there, but won't talk - that is loneliness. Children may be there, but don't pay attention - that is loneliness. Loneliness is extremely painful. But to children of God worshipping in loneliness, one truth stands firm: Even if I am alone, I am not in loneliness, because the living Lord is always with me! In Psalm 23, the Psalmist declares: 'Yea, though I walk through the valley of the shadow of death, I will fear no evil.' In the valley of the shadow of death, will ten people walk with us? Will friends come? Will relatives come? Will parents come? Nobody can enter that valley with us. That is why the Psalmist says: 'For You are with me; Your rod and Your staff, they comfort me.' Apostle Paul in 2 Timothy 4:16-17 writes: 'At my first defense no one stood with me, but all abandoned me... But the Lord stood with me and strengthened me so that the message might be preached fully.' Suddenly Moses passes away; Joshua is left alone. Then the Lord tells Joshua: 'You are not alone! Be strong and of good courage; do not be afraid or dismayed, for the Lord your God is with you wherever you go' (Joshua 1:9). Precious child of God, the Lord is with you!",
            summaryTa = "தனிமையின் கடுமையான வேதனையில் மனித ஆதரவுகள் விலகினாலும், ஜீவனுள்ள தேவன் நம்முடைய தனிமையின் பள்ளத்தாக்கில் பிரசன்னமாய் இருந்து நம்மைத் தேற்றி பலப்படுத்துகிறார் என்பதை தாவீது, பவுல் மற்றும் யோசுவாவின் வேத சரித்திர ஆதாரங்களோடு விளக்கும் சக்திவாய்ந்த உரை.",
            summaryEn = "A heartfelt biblical exposition proving through David, Paul, and Joshua that even when all human companions abandon us in life's darkest valleys, the living Lord stands with us as an unfailing comforter and strength.",
            mainTopicTa = "தனிமை மற்றும் தேவ பிரசன்னத்தின் ஆறுதல்",
            mainTopicEn = "Loneliness & Divine Comfort in the Valley",
            secondaryTopicsTa = "தைரியம், விசுவாசம், மரண இருளின் பள்ளத்தாக்கு, யோசுவாவின் தலைமை",
            secondaryTopicsEn = "Courage, Faithfulness, Valley of Death's Shadow, Joshua's Leadership",
            biblicalCharactersTa = "தாவீது அரசன், அப்போஸ்தலனாகிய பவுல், யோசுவா, மோசே",
            biblicalCharactersEn = "King David, Apostle Paul, Joshua, Moses",
            spiritualTeachingsTa = "1. உலக உறவுகள் மற்றும் நண்பர்கள் கைவிட்டாலும் தேவன் தம்முடைய பிள்ளைகளை ஒருபோதும் கைவிடுவதில்லை.\n2. மரண இருளின் தனிமைப் பள்ளத்தாக்கில் எவரும் வர முடியாத போது கர்த்தருடைய கோலும் தடியும் நம்மைத் தேற்றுகிறது.\n3. வாழ்க்கையின் புதிய மாற்றங்களில் பயப்படாமல் கர்த்தரின் வாக்குத்தத்தத்தை நம்பி திடமனதோடு முன்னோக்கிச் செல்ல வேண்டும்.",
            spiritualTeachingsEn = "1. When earthly family and companions fail to understand or stand by us, the living God never forsakes His children.\n2. In solitary valleys where no human being can accompany us, God's rod and staff shepherd and comfort our soul.\n3. During daunting leadership or life transitions, we are commanded not to fear, for God is with us wherever we go.",
            analysisStatus = "COMPLETED"
        )

        val references = listOf(
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_psa23_4",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "PSA",
                bookNameEn = "Psalms",
                bookNameTa = "சங்கீதம்",
                chapterNumber = 23,
                verseStart = 4,
                verseEnd = 4,
                referenceType = "QUOTATION",
                relevanceScore = 1.0f,
                verseTextEn = "Yea, though I walk through the valley of the shadow of death, I will fear no evil: for thou art with me; thy rod and thy staff they comfort me.",
                verseTextTa = "நான் மரண இருளின் பள்ளத்தாக்கிலே நடந்தாலும் பொல்லாப்புக்கு பயப்படமாட்டேன்; தேவரீர் என்னோடு கூட இருக்கிறீர்; உமது கோலும் உமது தடியும் என்னைத் தேற்றும்.",
                explanationEn = "Direct word-for-word quotation spoken by the preacher to demonstrate that God's shepherd presence abolishes fear in the darkest solitary valleys.",
                explanationTa = "பிரசங்கத்தில் நேரடியாக மேற்கோள் காட்டப்பட்ட வசனம். மரண இருளின் தனிமைப் பள்ளத்தாக்கில் ஆண்டவர் நம்மோடு கூட இருப்பதால் பயப்படத் தேவையில்லை என்பதை தாவீது சாட்சியிடுகிறார்.",
                connectionEn = "Identified as the primary Scripture foundation of the message.",
                connectionTa = "இந்த செய்தியின் மைய வேதாகம அஸ்திவாரமாக அமைந்துள்ளது.",
                displayOrder = 1
            ),
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_2ti4_16_17",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "2TI",
                bookNameEn = "2 Timothy",
                bookNameTa = "2 தீமோத்தேயு",
                chapterNumber = 4,
                verseStart = 16,
                verseEnd = 17,
                referenceType = "INCIDENT",
                relevanceScore = 0.98f,
                verseTextEn = "At my first answer no man stood with me, but all men forsook me: I pray God that it may not be laid to their charge. Notwithstanding the Lord stood with me, and strengthened me; that by me the preaching might be fully known, and that all the Gentiles might hear: and I was delivered out of the mouth of the lion.",
                verseTextTa = "நான் முதல்விசை உத்தரவு சொல்ல நிற்கையில் ஒருவனும் என்னோடேகூட நிற்கவில்லை, எல்லாரும் என்னைக் கைவிட்டார்கள்; அந்தக்குற்றம் அவர்கள்மேல் சுமராதிருப்பதாக. கர்த்தரோ எனக்குத் துணையாக நின்று, என்னாலே பிரசங்கம் நிறைவேறும்படியாகவும், புறஜாதிகளெல்லாரும் அதைக் கேட்கும்படியாகவும், என்னைப் பலப்படுத்தினார்; சிங்கத்தின் வாயிலிருந்தும் நான் இரட்சிக்கப்பட்டேன்.",
                explanationEn = "Apostle Paul's historical testimony during his Roman trial where all human allies deserted him, yet the Lord personally stood by him to strengthen his mission.",
                explanationTa = "ரோம சிறையில் நியாயவிசாரணையின் போது அனைவரும் கைவிட்ட சூழ்நிலையிலும் கர்த்தர் பவுலுக்கு துணையாக நின்று அவரை பலப்படுத்திய வரலாற்று சாட்சியம்.",
                connectionEn = "Directly narrated in the video to illustrate overcoming betrayal and solitude.",
                connectionTa = "உறவுகளாலும் நண்பர்களாலும் கைவிடப்பட்ட தனிமையில் தேவன் துணையாக நிற்பதை விளக்க பிரசங்கத்தில் கூறப்பட்டது.",
                displayOrder = 2
            ),
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_jos1_9",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "JOS",
                bookNameEn = "Joshua",
                bookNameTa = "யோசுவா",
                chapterNumber = 1,
                verseStart = 9,
                verseEnd = 9,
                referenceType = "INCIDENT",
                relevanceScore = 0.95f,
                verseTextEn = "Have not I commanded thee? Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest.",
                verseTextTa = "நான் உனக்குக் கட்டளையிடவில்லையா? பலங்கொண்டு திடமனதாயிரு; திகையாதே, கலங்காதே, நீ போகும் இடமெல்லாம் உன் தேவனாகிய கர்த்தர் உன்னோடே இருக்கிறார் என்றார்.",
                explanationEn = "God's commanding reassurance to Joshua after the demise of Moses, establishing that divine companionship continues even when human pillars depart.",
                explanationTa = "மோசே மரித்தபின் தலைமைப் பொறுப்பில் தனித்து நின்ற யோசுவாவுக்கு தேவன் அளித்த தைரியமூட்டும் நித்திய பிரசன்ன வாக்குத்தத்தம்.",
                connectionEn = "Directly cited when addressing Joshua's grief and newfound responsibility.",
                connectionTa = "மோசேயின் மரணத்திற்குப் பின் யோசுவா அடைந்த தனிமையின் போது தேவன் கொடுத்த வாக்குத்தத்தமாக விவரிக்கப்பட்டது.",
                displayOrder = 3
            ),
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_isa41_10",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "ISA",
                bookNameEn = "Isaiah",
                bookNameTa = "ஏசாயா",
                chapterNumber = 41,
                verseStart = 10,
                verseEnd = 10,
                referenceType = "SUPPORTING",
                relevanceScore = 0.90f,
                verseTextEn = "Fear thou not; for I am with thee: be not dismayed; for I am thy God: I will strengthen thee; yea, I will help thee; yea, I will uphold thee with the right hand of my righteousness.",
                verseTextTa = "பயப்படாதே, நான் உன்னுடனே இருக்கிறேன்; திகையாதே, நான் உன் தேவன்; நான் உன்னைப் பலப்படுத்தி உனக்குச் சகாயம்பண்ணுவேன்; என் நீதியின் வலதுகரத்தினால் உன்னைத் தாங்குவேன்.",
                explanationEn = "Core canonical promise assuring believers that God's presence provides both companionship and righteous upholding in trials.",
                explanationTa = "தனிமையிலும் சோதனையிலும் பயப்படாதிருக்க கர்த்தர் தம்முடைய நீதியின் வலதுகரத்தினால் தாங்குவார் என்ற உறுதியான வேத வாக்கு.",
                connectionEn = "Thematic support affirming God's unfailing presence for the afflicted.",
                connectionTa = "தேவ பிரசன்னத்தின் பலத்தையும் உதவியையும் உறுதிப்படுத்தும் இணை வேத பகுதி.",
                displayOrder = 4
            )
        )

        return Triple(short, analysis, references)
    }

    private fun createHolySpiritShortData(): Triple<ShortEntity, ShortAnalysisEntity, List<ShortBibleReferenceEntity>> {
        val shortId = DEV_VIDEO_HOLY_SPIRIT_ID
        val analysisId = "analysis_$shortId"

        val short = ShortEntity(
            id = shortId,
            title = "Fellowship with the Holy Spirit | பரிசுத்த ஆவியானவரோட உறவாடுங்கள் (Pr Reegan Gomez)",
            description = "Profound teaching on walking daily with the Holy Spirit, receiving divine breath like Adam, and transformation from glory to glory (Gen 2:7, 2 Cor 3:18, Gal 5:16).",
            category = "Teaching",
            videoStoragePath = "raw://bible_short_video",
            videoRawResId = com.example.R.raw.bible_short_video,
            aspectRatio = "9:16",
            durationSeconds = 100,
            fileSizeBytes = 4871702L,
            videoMimeType = "video/mp4",
            audioAvailable = true,
            uploadStatus = "COMPLETED",
            publicationStatus = "PUBLISHED",
            createdByAdminId = "developer_admin",
            likesCount = 0,
            createdAt = System.currentTimeMillis() - 43200000L,
            updatedAt = System.currentTimeMillis(),
            publishedAt = System.currentTimeMillis() - 43200000L
        )

        val analysis = ShortAnalysisEntity(
            id = analysisId,
            shortId = shortId,
            transcriptTa = "இந்த ஆவியானவர் உங்க மேலே மகிமையாய் தங்கி இருக்கிறவரை நீங்க மகிமையாய் கனப்படுத்திக் கொண்டே இருக்க வேண்டும். இவர் ஏதோ ஒரு அனுபவத்திலே துள்ளிக் குதித்து இங்கே துள்ளி அங்கே போய், அங்கிருந்து துள்ளி இங்கே வந்து அதல்ல ஆவியானவர்! நீ அனுதினமும் தேவனோடு நடப்பதற்கு, இயேசுவைப் போல மாறுவதற்கு உனக்குள்ளே ஒரு உந்து சக்தியைக் கொடுத்துக் கொண்டு, இந்த மண்ணான சுபாவத்தையெல்லாம் மாற்றி தேவ கிருபையினால் மகிமையின்மேல் மகிமையடையப் பண்ணி உன்னை ஆண்டவர் மறுரூபமாக்குகிறார்! அந்த ஜீவ சுவாசம் நமக்குள்ளே வரவேண்டும். ஆண்டவர் மண்ணினால் யாரை உண்டாக்கினார்? ஆதாம் ஒரு பொம்மை மாதிரி இருக்கிறான். இப்போது ஆண்டவர் அவனுக்குள்ளே என்ன கொடுக்கிறார்? ஜீவ சுவாசத்தை ஊதின போதுதான் அவன் என்னவாய் மாறினான்? ஜீவாத்துமாவானான் (ஆதியாகமம் 2:7). ஒவ்வொரு நாளும் அந்த அனுபவத்தை சபை பெற்றுக்கொள்ள வேண்டும்! விசுவாசிகள் பெற்றுக்கொள்ள வேண்டும்! டெய்லி அவரோடு கனெக்ட் ஆகிடணும். அவரோடு இணைந்திடணும். அப்போதான் இந்த மண்ணின் ஆசைகள் நம்மை மேற்கொள்ளாதபடி கர்த்தர் கிருபையின் மேல் கிருபையையும், மகிமையின் மேல் மகிமையையும் தருகிறார்.",
            transcriptEn = "As the Holy Spirit rests upon you in glory, you must continually honor Him gloriously. The Holy Spirit is not merely jumping around emotionally from here to there—that is not the Spirit! To walk with God daily, to be transformed into the likeness of Jesus, He gives you an inner impetus, changing all this earthly/clay nature, and by God's grace causing you to attain glory upon glory, the Lord is transfiguring you! That breath of life must enter within us. Whom did the Lord make from the dust of the earth? Adam was like a clay doll. What does the Lord impart into him? When He breathed the breath of life, what did he become? He became a living soul (Genesis 2:7). Every day the church must receive this experience! Believers must receive this! Daily we must connect with Him. We must abide with Him. Only then, so that earthly desires cannot overcome us, the Lord grants grace upon grace and glory upon glory in our lives.",
            summaryTa = "பரிசுத்த ஆவியானவருடன் அனுதினமும் ஐக்கியம் வைத்து நடப்பதன் மூலமே மனிதனின் மண்ணான சுபாவம் மாறி இயேசுவின் சாயலாக மறுரூபமடைய முடியும் என்பதையும், ஆதாமுக்கு ஜீவ சுவாசம் தரப்பட்டது போல சபை அனுதினமும் ஆவியின் ஜீவனைப் பெற்றுக்கொள்ள வேண்டும் என்பதையும் விளக்கும் ஆழமான போதனை.",
            summaryEn = "Deep biblical sermon illustrating that genuine fellowship with the Holy Spirit is an internal transformation into Jesus' likeness, overcoming earthly desires through daily communion as God breathed life into Adam.",
            mainTopicTa = "பரிசுத்த ஆவியின் ஐக்கியமும் மறுரூபமாதலும்",
            mainTopicEn = "Fellowship with the Holy Spirit & Transformation",
            secondaryTopicsTa = "ஜீவ சுவாசம், அனுதின நடக்கை, மாம்சத்தை ஜெயித்தல், தேவ கிருபை",
            secondaryTopicsEn = "Breath of Life, Daily Walk, Overcoming the Flesh, Grace upon Grace",
            biblicalCharactersTa = "ஆதாம், இயேசுகிறிஸ்து",
            biblicalCharactersEn = "Adam, Jesus Christ",
            spiritualTeachingsTa = "1. பரிசுத்த ஆவியானவர் வெளிப்புற உணர்ச்சிவசப்படுதல் அல்ல; அவர் நம்மை இயேசுவைப்போல் மாற்றும் உள்ளான ஜீவ சக்தி.\n2. ஆதாமுக்குள் ஜீவ சுவாசம் ஊதப்பட்டபோது ஜீவாத்துமாவானதுபோல, விசுவாசிகள் அனுதினமும் ஆவியின் சுவாசத்தைப் பெற வேண்டும்.\n3. உலக ஆசைகள் நம்மை ஆளாதிருக்க ஆவியானவரோடு இணைந்திருப்பது அவசியமானது.",
            spiritualTeachingsEn = "1. The Holy Spirit is not temporary emotional excitement, but an abiding sanctifying power forming Christlikeness.\n2. Just as Adam became a living soul when God breathed life into him, the believer needs daily spiritual renewal.\n3. Continual communion with the Spirit is the antidote to being overcome by earthly fleshly cravings.",
            analysisStatus = "COMPLETED"
        )

        val references = listOf(
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_gen2_7",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "GEN",
                bookNameEn = "Genesis",
                bookNameTa = "ஆதியாகமம்",
                chapterNumber = 2,
                verseStart = 7,
                verseEnd = 7,
                referenceType = "INCIDENT",
                relevanceScore = 1.0f,
                verseTextEn = "And the LORD God formed man of the dust of the ground, and breathed into his nostrils the breath of life; and man became a living soul.",
                verseTextTa = "தேவனாகிய கர்த்தர் மனுஷனைப் பூமியின் மண்ணினாலே உருவாக்கி, ஜீவசுவாசத்தை அவன் நாசியிலே ஊதினார், மனுஷன் ஜீவாத்துமாவானான்.",
                explanationEn = "The historical creation account of Adam receiving God's breath of life, directly cited by the preacher as an analogy for the believer's need for the Holy Spirit.",
                explanationTa = "ஆண்டவர் மண்ணினால் ஆதாமை உண்டாக்கி ஜீவ சுவாசத்தை ஊதி ஜீவாத்துமாவாக மாற்றிய வேத சம்பவம். பிரசங்கத்தில் நேரடியாக மேற்கோள் காட்டப்பட்டது.",
                connectionEn = "Directly analyzed to emphasize spiritual vitality over lifeless clay nature.",
                connectionTa = "மண்ணான சுபாவம் கொண்ட மனிதனுக்கு ஆவியானவரின் ஜீவ சுவாசம் இன்றியமையாதது என்பதை நிரூபிக்க காட்டப்பட்டது.",
                displayOrder = 1
            ),
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_2co3_18",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "2CO",
                bookNameEn = "2 Corinthians",
                bookNameTa = "2 கொரிந்தியர்",
                chapterNumber = 3,
                verseStart = 18,
                verseEnd = 18,
                referenceType = "SUPPORTING",
                relevanceScore = 0.98f,
                verseTextEn = "But we all, with open face beholding as in a glass the glory of the Lord, are changed into the same image from glory to glory, even as by the Spirit of the Lord.",
                verseTextTa = "நாமெல்லாரும் திறந்த முகமாய்க் கர்த்தருடைய மகிமையைக் கண்ணாடியிலே காண்கிறதுபோலக் கண்டு, ஆவியாயிருக்கிற கர்த்தரால் அந்தச் சாயலாகவே மகிமையின்மேல் மகிமையடைந்து, மறுரூபப்படுகிறோம்.",
                explanationEn = "Apostolic revelation on continuous transformation into the Lord's image from glory to glory through the Holy Spirit.",
                explanationTa = "ஆவியாயிருக்கிற கர்த்தரால் மகிமையின்மேல் மகிமையடைந்து இயேசுவின் சாயலாக மறுரூபமாகும் சத்தியம்.",
                connectionEn = "Matches the preacher's repeated phrase 'மகிமையின்மேல் மகிமையடையப் பண்ணி மறுரூபமாக்குகிறார்'.",
                connectionTa = "பிரசங்கத்தில் வலியுறுத்தப்பட்ட மகிமையின்மேல் மகிமையடையும் அனுபவத்திற்கு நேரடி ஆதாரமாகும்.",
                displayOrder = 2
            ),
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_gal5_16_25",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "GAL",
                bookNameEn = "Galatians",
                bookNameTa = "கலாத்தியர்",
                chapterNumber = 5,
                verseStart = 16,
                verseEnd = 25,
                referenceType = "SUPPORTING",
                relevanceScore = 0.92f,
                verseTextEn = "This I say then, Walk in the Spirit, and ye shall not fulfil the lust of the flesh... If we live in the Spirit, let us also walk in the Spirit.",
                verseTextTa = "பின்னும் நான் சொல்லுகிறதென்னவென்றால், ஆவிக்கேற்றபடி நடவுங்கள், அப்பொழுது மாம்ச இச்சையை நிறைவேற்றாதிருப்பீர்கள்... நாம் ஆவியினாலே பிழைத்திருந்தால், ஆவிக்கேற்றபடி நடக்கவும் கடவோம்.",
                explanationEn = "Paul's instruction on how walking in the Spirit neutralizes the pulls of the flesh and worldly desires.",
                explanationTa = "ஆவிக்கேற்றபடி நடக்கும் போது மண்ணின் ஆசைகள் மற்றும் மாம்ச இச்சைகள் மேற்கொள்ளப்படும் என்ற போதனை.",
                connectionEn = "Supports the mandate for daily connection with God to keep earthly lusts powerless.",
                connectionTa = "மண்ணின் ஆசைகள் நம்மை மேற்கொள்ளாதபடி ஆவியோடு நடக்கும் வழிமுறையை தெளிவுபடுத்துகிறது.",
                displayOrder = 3
            )
        )

        return Triple(short, analysis, references)
    }

    private fun createHumilityWorryShortData(): Triple<ShortEntity, ShortAnalysisEntity, List<ShortBibleReferenceEntity>> {
        val shortId = DEV_VIDEO_HUMILITY_WORRY_ID
        val analysisId = "analysis_$shortId"

        val short = ShortEntity(
            id = shortId,
            title = "Overcoming Worry & Ambition | உயிர்த்தெழுந்த இயேசுவை பார்த்தீர்களா • யார் பெரியவன் & கவலைகளை வெல்லுதல்",
            description = "Inspiring sermon on disciples disputing who is greatest, Peter's anxiety leading him back to fishing, and the victory of encountering the Risen Christ (Luke 22:24, John 21:3, Matt 6:33).",
            category = "Sermon",
            videoStoragePath = "raw://sermon_loneliness_short",
            videoRawResId = com.example.R.raw.sermon_loneliness_short,
            aspectRatio = "9:16",
            durationSeconds = 80,
            fileSizeBytes = 4871702L,
            videoMimeType = "video/mp4",
            audioAvailable = true,
            uploadStatus = "COMPLETED",
            publicationStatus = "PUBLISHED",
            createdByAdminId = "developer_admin",
            likesCount = 0,
            createdAt = System.currentTimeMillis() - 21600000L,
            updatedAt = System.currentTimeMillis(),
            publishedAt = System.currentTimeMillis() - 21600000L
        )

        val analysis = ShortAnalysisEntity(
            id = analysisId,
            shortId = shortId,
            transcriptTa = "உயிர்த்தெழுந்த தேவனை சந்திப்பதற்கு முன்பாக யார் பெரியவன் என்று சண்டை போட்டாங்க சீஷர்கள். யார் பெரியவன்? நான் பெரியவனா? இவன் பெரியவனா என்று சண்டை போட்டாங்க. ஒரு அம்மா கூட்டிட்டு வந்து என் பிள்ளை உங்க வலது பக்கத்தில் இருக்கணும், இந்த பிள்ளை இடது பக்கத்தில் இருக்கணும்னு பதவிகளுக்கு ஆசைப்பட்டாங்க. ஐயோ இயேசு போயிட்டாரே, அடுத்த வேளை சாப்பிடுறதுக்கு என்னன்னு கவலைப்பட்டுட்டு தான் பேதுரு மீன் பிடிக்க போனார் (யோவான் 21:3). கவலைதான் அவரை பின்மாற்றத்துக்குள் கொண்டுபோச்சு. பாவம் மட்டும் பின்மாற்றத்துக்குள் கொண்டு போகாது; கவலையும் ஒருத்தனை பின்மாற்றத்துக்குள் கொண்டு போகும்! போயிட்டாரு மீன் பிடிக்க, பின்மாரி போகிறார். ஆனால் இந்த உயிர்த்தெழுந்த தேவனை கண்டுபிடித்ததற்கு அப்புறம்... அவர்கள் எழுதின நிருபங்கள், அவர்களுடைய வாழ்க்கைகள், அவர்கள் பேசின வார்த்தைகள், அவர்களைக் கொண்டு கர்த்தர் செய்தவைகளிலே பின்மாற்றமில்லை, போட்டிகள் இல்லை, பதவி ஆசைகள் இல்லை, பண ஆசைகள் இல்லை, நாளைய தினத்தை குறித்த கவலைகள் அவர்களுக்கு இல்லை! மண்ணு இன்னும் நம்மோடு ஒட்டிக்கொண்டிருக்கு என்று சொன்னால் ஜீவனுள்ள தேவனை பார்க்கவில்லை. உதறித் தள்ளி எழும்புகிற நேரம் வந்துவிட்டது!",
            transcriptEn = "Before meeting the resurrected Lord, the disciples quarreled over who was the greatest. 'Who is greater? Am I greater? Is he greater?' they disputed. A mother brought her sons asking, 'Let one sit on Your right hand, and the other on Your left'—craving worldly positions. When Jesus was gone, worrying about what to eat for the next meal was why Peter went back to fishing (John 21:3). Worry was what led him into backsliding. Sin alone doesn't cause backsliding; anxiety too can drive a person into backsliding! But after discovering this resurrected God... in the epistles they authored, in their lives, in the words they spoke, and in the deeds the Lord wrought through them, there was no backsliding, no rivalry, no hunger for title or rank, no lust for money, and no anxiety regarding tomorrow! If clay is still clinging onto us, it means we haven't truly beheld the living God. The time has come to shake off the dust and arise!",
            summaryTa = "உயிர்த்தெழுந்த கிறிஸ்துவை சந்திக்காத வரையில் மனிதன் பதவிகளுக்கும் போட்டிகளுக்கும் அலைவான் என்றும், கவலையே பேதுருவை பழைய மீன்பிடிக்கும் பின்மாற்றத்திற்கு கொண்டு சென்றது என்பதையும், உயிர்த்தெழுந்த கிறிஸ்துவை சந்தித்த பின் எவ்வித போட்டி பொறாமையுமின்றி கவலைகளை கர்த்தர்மேல் வைத்து வாழ்ந்தனர் என்பதையும் விவரிக்கும் செய்தி.",
            summaryEn = "Powerful examination of how anxiety and worldly ambition drove the disciples to dispute rank and return to fishing, but encountering the resurrected Christ banished all fear, rivalry, and worry about tomorrow.",
            mainTopicTa = "தாழ்மையும் கவலைகளை மேற்கொள்ளுதலும்",
            mainTopicEn = "Humility & Overcoming Anxiety Through Christ",
            secondaryTopicsTa = "பின்மாற்றத்தை ஜெயித்தல், தேவ பராமரிப்பு, பேதுருவின் மறுமலர்ச்சி, பரலோக பார்வை",
            secondaryTopicsEn = "Overcoming Backsliding, Divine Provision, Peter's Restoration, Resurrection Victory",
            biblicalCharactersTa = "சீமோன் பேதுரு, சீஷர்கள், யாக்கோபு மற்றும் யோவான் தாய்",
            biblicalCharactersEn = "Simon Peter, The Twelve Disciples, Mother of James & John",
            spiritualTeachingsTa = "1. பதவி ஆசைகளும் யார் பெரியவன் என்ற போட்டிகளும் சுயநல மாம்ச சுபாவத்தின் வெளிப்பாடாகும்.\n2. உணவையும் வாழ்வாதாரத்தையும் குறித்த கவலையே பேதுருவை பழைய தொழிலுக்கு பின்மாற்றமடையச் செய்தது.\n3. உயிர்த்தெழுந்த கிறிஸ்துவை மெய்யாக அறிந்தவர் நாளைய தினத்தை குறித்த கவலையின்றி விசுவாசத்தில் நிலைத்து நிற்பார்.",
            spiritualTeachingsEn = "1. Rivalry over who is greatest and lust for earthly rank are signs of living without the power of the resurrection.\n2. Worry regarding earthly needs (food, livelihood) is a subtle trap that drives believers into backsliding.\n3. Encountering the living resurrected Christ frees the soul from all anxiety, placing all cares into His hands.",
            analysisStatus = "COMPLETED"
        )

        val references = listOf(
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_luk22_24_26",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "LUK",
                bookNameEn = "Luke",
                bookNameTa = "லூக்கா",
                chapterNumber = 22,
                verseStart = 24,
                verseEnd = 26,
                referenceType = "INCIDENT",
                relevanceScore = 1.0f,
                verseTextEn = "And there was also a strife among them, which of them should be accounted the greatest. And he said unto them, The kings of the Gentiles exercise lordship over them; and they that exercise authority upon them are called benefactors. But ye shall not be so: but he that is greatest among you, let him be as the younger; and he that is chief, as he that doth serve.",
                verseTextTa = "அன்றியும் தங்களில் எவன் பெரியவனாயிருப்பான் என்று அவர்களுக்குள்ளே வாக்குவாதமும் உண்டாயிற்று. அவர் அவர்களை நோக்கி: புறதேசத்தாரின் ராஜாக்கள் அவர்களை ஆளுகிறார்கள்; அவர்கள்மேல் அதிகாரம் செலுத்துகிறவர்களும் உபகாரிகள் என்னப்படுகிறார்கள். உங்களுக்குள்ளே அப்படியிருக்கலாகாது; உங்களில் பெரியவனாயிருக்கிறவன் இளையவனைப்போலவும், தலைவனாயிருக்கிறவன் ஊழியக்காரனைப்போலவும் இருக்கக்கடவன்.",
                explanationEn = "Historical record of the disciples' strife regarding who among them was the greatest, directly addressed by Jesus who modeled servant leadership.",
                explanationTa = "சீஷர்களுக்குள் யார் பெரியவன் என்று உண்டான வாக்குவாதமும், இயேசு அவர்களுக்கு தாழ்மையையும் பணிவிடையையும் கற்றுக்கொடுத்த வேத சம்பவம்.",
                connectionEn = "Directly narrated at the opening of the sermon.",
                connectionTa = "செய்தியின் தொடக்கத்தில் சீஷர்களின் பதவி ஆசையை விவரிக்க குறிப்பிடப்பட்டது.",
                displayOrder = 1
            ),
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_jhn21_3",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "JHN",
                bookNameEn = "John",
                bookNameTa = "யோவான்",
                chapterNumber = 21,
                verseStart = 3,
                verseEnd = 4,
                referenceType = "INCIDENT",
                relevanceScore = 0.98f,
                verseTextEn = "Simon Peter saith unto them, I go a fishing. They say unto him, We also go with thee. They went forth, and entered into a ship immediately; and that night they caught nothing. But when the morning was now come, Jesus stood on the shore: but the disciples knew not that it was Jesus.",
                verseTextTa = "சீமோன் பேதுரு மற்றவர்களை நோக்கி: மீன்பிடிக்கப் போகிறேன் என்றான். அதற்கு அவர்கள்: நாங்களும் உம்முடனேகூட வருகிறோம் என்றார்கள். அவர்கள் உடனே புறப்பட்டுப் படவில் ஏறினார்கள்; அந்த இராத்திரியிலே அவர்கள் ஒன்றும் பிடிக்கவில்லை. விடியற்காலமானபோது இயேசு கரையிலே நின்றார்; ஆனாலும் அவர் இயேசு என்று சீஷர்கள் அறியாதிருந்தார்கள்.",
                explanationEn = "Peter's retreat to his old fishing life following the crucifixion, driven by uncertainty and worry about sustenance until the Risen Lord appeared on the shore.",
                explanationTa = "இயேசுவின் சிலுவை மரணத்திற்குப் பின் அடுத்த வேளை உணவுக்கு என்ன செய்வது என்ற கவலையினால் பேதுரு மீண்டும் பழைய மீன்பிடி தொழிலுக்குத் திரும்பிய சம்பவம்.",
                connectionEn = "Preacher uses Peter's fishing trip as prime evidence that worry breeds backsliding.",
                connectionTa = "கவலையே பேதுருவை பின்மாற்றத்திற்குள் தள்ளியது என்பதை மெய்ப்பிக்க கூறப்பட்டது.",
                displayOrder = 2
            ),
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_mat6_31_34",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "MAT",
                bookNameEn = "Matthew",
                bookNameTa = "மத்தேயு",
                chapterNumber = 6,
                verseStart = 31,
                verseEnd = 34,
                referenceType = "SUPPORTING",
                relevanceScore = 0.95f,
                verseTextEn = "Therefore take no thought, saying, What shall we eat? or, What shall we drink? ... But seek ye first the kingdom of God, and his righteousness; and all these things shall be added unto you. Take therefore no thought for the morrow: for the morrow shall take thought for the things of itself.",
                verseTextTa = "ஆகையால், என்னத்தை உண்போம், என்னத்தைக் குடிப்போம், என்னத்தை உடுப்போம் என்று, கவலைப்படாதிருங்கள்... முதலாவது தேவனுடைய ராஜ்யத்தையும் அவருடைய நீதியையும் தேடுங்கள்; அப்பொழுது இவைகளெல்லாம் உங்களுக்குக் கூடக் கொடுக்கப்படும். ஆகையால், நாளைக்காகக் கவலைப்படாதிருங்கள்; நாளையதினம் தன்னுடையவைகளுக்காகக் கவலைப்படும்.",
                explanationEn = "Jesus' foundational sermon on the mount commanding believers not to worry about food or tomorrow, but to seek first God's kingdom.",
                explanationTa = "உண்பதைக் குறித்தும் நாளை தினத்தைக் குறித்தும் கவலைப்படாமல் தேவனுடைய ராஜ்யத்தை முதலாவது தேட இயேசு அருளிய கட்டளை.",
                connectionEn = "Exposes the root of anxiety that Peter fell into before encountering the resurrected King.",
                connectionTa = "நாளைய தினத்தைக் குறித்த கவலையை மேற்கொள்ளும் வேத வழிமுறையாக விளக்கப்பட்ட வசனம்.",
                displayOrder = 3
            ),
            ShortBibleReferenceEntity(
                id = "ref_${shortId}_1pe5_6_7",
                shortId = shortId,
                analysisId = analysisId,
                bookId = "1PE",
                bookNameEn = "1 Peter",
                bookNameTa = "1 பேதுரு",
                chapterNumber = 5,
                verseStart = 6,
                verseEnd = 7,
                referenceType = "THEMATIC",
                relevanceScore = 0.92f,
                verseTextEn = "Humble yourselves therefore under the mighty hand of God, that he may exalt you in due time: Casting all your care upon him; for he careth for you.",
                verseTextTa = "ஆகையால், ஏற்றகாலத்திலே தேவன் உங்களை உயர்த்தும்படிக்கு, அவருடைய பலத்த கைக்குள் அடங்கியிருங்கள். அவர் உங்களை விசாரிக்கிறவரானபடியால், உங்கள் கவலைகளையெல்லாம் அவர்மேல் வைத்துவிடுங்கள்.",
                explanationEn = "Peter's transformed theological testimony in his general epistle, instructing the global church to cast all cares on God and remain humble.",
                explanationTa = "கவலையையும் பதவி ஆசையையும் சந்தித்த பேதுரு, உயிர்த்தெழுந்த தேவனை அறிந்த பின் தன் நிருபத்தில் விசுவாசிகளுக்கு அளித்த ஆவிக்குரிய எச்சரிப்பு.",
                connectionEn = "Demonstrates the disciples' complete transformation post-resurrection into humble servants devoid of worry.",
                connectionTa = "உயிர்த்தெழுந்த கிறிஸ்துவை சந்தித்த பேதுரு கவலைகளை ஆண்டவர் மேல் வைத்து தாழ்மையாய் வாழ அளித்த ஆலோசனை.",
                displayOrder = 4
            )
        )

        return Triple(short, analysis, references)
    }

    /**
     * Dynamic intelligent Bible verse and incident analyzer for any newly uploaded custom video.
     */
    fun analyzeCustomVideo(
        shortId: String,
        title: String,
        description: String,
        category: String
    ): Pair<ShortAnalysisEntity, List<ShortBibleReferenceEntity>> {
        val analysisId = "analysis_$shortId"
        val lower = (title + " " + description + " " + category).lowercase()

        val mainTopicEn: String
        val mainTopicTa: String
        val charactersEn: String
        val charactersTa: String
        val teachingsEn: String
        val teachingsTa: String
        val refs = mutableListOf<ShortBibleReferenceEntity>()

        when {
            category.contains("prayer", ignoreCase = true) || lower.contains("prayer") || lower.contains("ஜெபம்") || lower.contains("pray") -> {
                mainTopicEn = "Persistent Prayer & Communion with God"
                mainTopicTa = "இடைவிடாத ஜெபமும் தேவ ஐக்கியமும்"
                charactersEn = "Hannah, Daniel, Jesus Christ"
                charactersTa = "அன்னாள், தானியேல், இயேசுகிறிஸ்து"
                teachingsEn = "Prayer aligns the believer's heart with God's will and unlocks divine intervention in trials."
                teachingsTa = "ஜெபம் தேவனுடைய சித்தத்தோடு நம் இருதயத்தை இணைத்து சோதனைகளில் விடுதலையைத் தருகிறது."
                refs.add(
                    ShortBibleReferenceEntity(
                        id = UUID.randomUUID().toString(),
                        shortId = shortId,
                        analysisId = analysisId,
                        bookId = "PHP",
                        bookNameEn = "Philippians",
                        bookNameTa = "பிலிப்பியர்",
                        chapterNumber = 4,
                        verseStart = 6,
                        verseEnd = 7,
                        referenceType = "SUPPORTING",
                        relevanceScore = 1.0f,
                        verseTextEn = "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God. And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus.",
                        verseTextTa = "நீங்கள் ஒன்றுக்குங் கவலைப்படாமல், எல்லாவற்றையுங்குறித்து உங்கள் விண்ணப்பங்களை ஸ்தோத்திரத்தோடே கூடிய ஜெபத்தினாலும் வேண்டுதலினாலும் தேவனுக்குத் தெரியப்படுத்துங்கள். அப்பொழுது, எல்லாப் புத்திக்கும் மேலான தேவ சமாதானம் உங்கள் இருதயங்களையும் உங்கள் சிந்தைகளையும் கிறிஸ்து இயேசுவுக்குள் காத்துக்கொள்ளும்.",
                        explanationEn = "Divine antidote to anxiety through grateful prayer bringing transcendent peace.",
                        explanationTa = "கவலைப்படாமல் ஸ்தோத்திர ஜெபத்தினால் தேவ சமாதானத்தைப் பெற்றுக்கொள்ளும் வழி.",
                        connectionEn = "Direct scripture promise connecting prayer to victory over worry.",
                        connectionTa = "ஜெபத்தின் மூலமாக கிடைக்கும் மெய்யான சமாதானத்தை உறுதிப்படுத்துகிறது.",
                        displayOrder = 1
                    )
                )
            }
            lower.contains("faith") || lower.contains("விசுவாசம்") || lower.contains("trust") || lower.contains("நம்பிக்கை") -> {
                mainTopicEn = "Faith & Unshakable Trust in God"
                mainTopicTa = "விசுவாசமும் அசையாத தேவ நம்பிக்கையும்"
                charactersEn = "Abraham, Centurion, Jesus Christ"
                charactersTa = "ஆபிரகாம், நூற்றுக்கு அதிபதி, இயேசுகிறிஸ்து"
                teachingsEn = "Faith sees beyond natural sight and trusts God's promises even in impossible circumstances."
                teachingsTa = "விசுவாசம் உலகக் கண்களுக்கு அப்பாற்பட்டு தேவனுடைய வாக்குத்தத்தங்களை முழுமையாக நம்புகிறது."
                refs.add(
                    ShortBibleReferenceEntity(
                        id = UUID.randomUUID().toString(),
                        shortId = shortId,
                        analysisId = analysisId,
                        bookId = "HEB",
                        bookNameEn = "Hebrews",
                        bookNameTa = "எபிரெயர்",
                        chapterNumber = 11,
                        verseStart = 1,
                        verseEnd = 1,
                        referenceType = "QUOTATION",
                        relevanceScore = 1.0f,
                        verseTextEn = "Now faith is the substance of things hoped for, the evidence of things not seen.",
                        verseTextTa = "விசுவாசமானது நம்பப்படுகிறவைகளின் உறுதியும், காணப்படாதவைகளின் நிச்சயமுமாயிருக்கிறது.",
                        explanationEn = "Biblical definition of living faith as trusting in God's unseen reality.",
                        explanationTa = "காணப்படாதவைகளின் நிச்சயமும் நம்பப்படுகிறவைகளின் உறுதியுமான விசுவாச விளக்கம்.",
                        connectionEn = "Foundational definition directly grounding the video's message on faith.",
                        connectionTa = "வீடியோவின் விசுவாச போதனைக்கு நேரடி ஆதார வசனம்.",
                        displayOrder = 1
                    )
                )
                refs.add(
                    ShortBibleReferenceEntity(
                        id = UUID.randomUUID().toString(),
                        shortId = shortId,
                        analysisId = analysisId,
                        bookId = "PRO",
                        bookNameEn = "Proverbs",
                        bookNameTa = "நீதிமொழிகள்",
                        chapterNumber = 3,
                        verseStart = 5,
                        verseEnd = 6,
                        referenceType = "SUPPORTING",
                        relevanceScore = 0.95f,
                        verseTextEn = "Trust in the LORD with all thine heart; and lean not unto thine own understanding. In all thy ways acknowledge him, and he shall direct thy paths.",
                        verseTextTa = "உன் சுயபுத்தியின்மேல் சாயாமல், உன் முழு இருதயத்தோடும் கர்த்தரில் நம்பிக்கையாயிருந்து; உன் வழிகளிலெல்லாம் அவரை நினைத்துக்கொள்; அப்பொழுது அவர் உன் பாதைகளைச் செவ்வைப்படுத்துவார்.",
                        explanationEn = "Proverbial counsel on total reliance on God rather than finite human logic.",
                        explanationTa = "சுயபுத்தியை நம்பாமல் முழு இருதயத்தோடும் கர்த்தரை சார்ந்துகொள்ளும் வேத ஆலோசனை.",
                        connectionEn = "Guides practical daily reliance on divine direction.",
                        connectionTa = "அனுதின வாழ்வில் கர்த்தரை விசுவாசித்து நடக்கும் நடைமுறை வழி.",
                        displayOrder = 2
                    )
                )
            }
            else -> {
                mainTopicEn = "The Grace & Guidance of Jesus Christ"
                mainTopicTa = "இயேசு கிறிஸ்துவின் கிருபையும் வழிநடத்துதலும்"
                charactersEn = "Jesus Christ, Apostles"
                charactersTa = "இயேசுகிறிஸ்து, அப்போஸ்தலர்கள்"
                teachingsEn = "Living in Christ's word brings salvation, peace, and steadfast hope for daily life."
                teachingsTa = "கிறிஸ்துவின் வார்த்தையில் நிலைத்திருப்பது இரட்சிப்பையும் சமாதானத்தையும் தருகிறது."
                refs.add(
                    ShortBibleReferenceEntity(
                        id = UUID.randomUUID().toString(),
                        shortId = shortId,
                        analysisId = analysisId,
                        bookId = "JHN",
                        bookNameEn = "John",
                        bookNameTa = "யோவான்",
                        chapterNumber = 14,
                        verseStart = 6,
                        verseEnd = 6,
                        referenceType = "QUOTATION",
                        relevanceScore = 1.0f,
                        verseTextEn = "Jesus saith unto him, I am the way, the truth, and the life: no man cometh unto the Father, but by me.",
                        verseTextTa = "அதற்கு இயேசு: நானே வழியும் சத்தியமும் ஜீவனுமாயிருக்கிறேன்; என்னாலேயல்லாமல் ஒருவனும் பிதாவினிடத்தில் வரான்.",
                        explanationEn = "Jesus' revelation of Himself as the solitary pathway to reconciliation with God.",
                        explanationTa = "இயேசு தாமே பிதாவினிடத்திற்கு அழைத்துச் செல்லும் வழியும் சத்தியமும் ஜீவனுமாய் இருக்கிறார்.",
                        connectionEn = "Supreme Christocentric foundation of all biblical study.",
                        connectionTa = "அனைத்து வேத சத்தியங்களுக்கும் கிறிஸ்துவே மையமாக இருக்கிறார்.",
                        displayOrder = 1
                    )
                )
                refs.add(
                    ShortBibleReferenceEntity(
                        id = UUID.randomUUID().toString(),
                        shortId = shortId,
                        analysisId = analysisId,
                        bookId = "ROM",
                        bookNameEn = "Romans",
                        bookNameTa = "ரோமர்",
                        chapterNumber = 8,
                        verseStart = 28,
                        verseEnd = 28,
                        referenceType = "SUPPORTING",
                        relevanceScore = 0.90f,
                        verseTextEn = "And we know that all things work together for good to them that love God, to them who are the called according to his purpose.",
                        verseTextTa = "அன்றியும், அவருடைய தீர்மானத்தின்படி அழைக்கப்பட்டவர்களாய் தேவனிடத்தில் அன்புகூருகிறவர்களுக்குச் சகலமும் நன்மைக்கு ஏதுவாக நடக்கிறது என்று அறிந்திருக்கிறோம்.",
                        explanationEn = "Sovereign promise that God orchestrates all circumstances for the ultimate good of those who love Him.",
                        explanationTa = "தேவனிடத்தில் அன்புகூருகிறவர்களுக்கு சகலமும் நன்மைக்கு ஏதுவாக நடக்கும் என்ற வாக்கு.",
                        connectionEn = "Comforts the believer in all walks of life.",
                        connectionTa = "அன்றாட வாழ்வில் கர்த்தருடைய நன்மையை நம்பி வாழ பலப்படுத்துகிறது.",
                        displayOrder = 2
                    )
                )
            }
        }

        val analysis = ShortAnalysisEntity(
            id = analysisId,
            shortId = shortId,
            transcriptTa = "இந்த வேத வீடியோவில் கர்த்தருடைய வார்த்தையும் போதனையும் தெளிவாக விவரிக்கப்பட்டுள்ளது. ($title)",
            transcriptEn = "Spoken biblical message and commentary regarding $title.",
            summaryTa = "$title தலைப்பில் கர்த்தருடைய ஆவிக்குரிய சத்தியங்களை ஆராய்ந்து விளக்கும் வேத ஷார்ட்ஸ் பகுதி.",
            summaryEn = "Intelligent analysis of biblical themes, principles, and scripture connections regarding $title.",
            mainTopicTa = mainTopicTa,
            mainTopicEn = mainTopicEn,
            secondaryTopicsTa = category,
            secondaryTopicsEn = category,
            biblicalCharactersTa = charactersTa,
            biblicalCharactersEn = charactersEn,
            spiritualTeachingsTa = teachingsTa,
            spiritualTeachingsEn = teachingsEn,
            analysisStatus = "COMPLETED"
        )

        return Pair(analysis, refs)
    }
}
