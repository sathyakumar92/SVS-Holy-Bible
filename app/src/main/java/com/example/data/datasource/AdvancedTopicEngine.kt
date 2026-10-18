package com.example.data.datasource

import com.example.data.model.SearchResultItem
import com.example.data.model.TopicItem
import com.example.data.model.VerseReference
import com.example.data.repository.BibleRepository
import com.example.ui.screens.search.SearchMode
import com.example.util.BibleReferenceParser
import com.example.util.ReferenceParseResult
import java.text.Normalizer

enum class TopicIntent(val labelEn: String, val labelTa: String) {
    QUESTION("Biblical Answer & Guidance", "வேதாகம பதில் & வழிகாட்டுதல்"),
    THEMATIC_STUDY("Thematic Scripture Study", "கருப்பொருள் வேத தியானம்"),
    KEYWORD("Keyword Doctrine & Truth", "முக்கிய வேத உபதேசம்"),
    DIRECT_REFERENCE("Direct Scripture Reference", "நேரடி வசனக் குறிப்பு")
}

data class CategorizedTopicPassages(
    val directMatches: List<SearchResultItem> = emptyList(),
    val foundationalPassages: List<VerseReference> = emptyList(),
    val teachingsAndExhortations: List<VerseReference> = emptyList(),
    val promisesAndComfort: List<VerseReference> = emptyList(),
    val practicalApplications: List<VerseReference> = emptyList()
) {
    val totalCount: Int
        get() = directMatches.size + foundationalPassages.size + teachingsAndExhortations.size + promisesAndComfort.size + practicalApplications.size
}

data class TopicAnalysisResult(
    val query: String,
    val intent: TopicIntent,
    val titleEn: String,
    val titleTa: String,
    val descriptionEn: String,
    val descriptionTa: String,
    val summaryEn: String,
    val summaryTa: String,
    val subtopicsEn: List<String>,
    val subtopicsTa: List<String>,
    val passages: CategorizedTopicPassages
)

typealias BibleTopics = AdvancedTopicEngine

object AdvancedTopicEngine {

    val curatedTopics: List<TopicItem> = listOf(
        TopicItem(
            id = "love",
            englishName = "Love",
            tamilName = "அன்பு",
            descriptionEn = "God's sacrificial, unconditional love and the command to love one another.",
            descriptionTa = "தேவனின் நிபந்தனையற்ற அன்பும் ஒருவரிலொருவர் அன்புகூரும் கற்பனையும்.",
            category = "Spiritual Life",
            categoryTa = "ஆவிக்குரிய வாழ்க்கை",
            subtopics = listOf("God's Love for Us", "Loving One Another", "Love in Action", "Love vs Fear"),
            subtopicsTa = listOf("நம்மேல் தேவனுடைய அன்பு", "ஒருவரிலொருவர் அன்பு", "செயலில் அன்பு", "அன்பும் பயமின்மையும்"),
            summaryEn = "Scripture reveals that God is love (1 John 4:8) and that His love was supremely demonstrated in the gift of His Son, Jesus Christ (John 3:16). Believers are exhorted to love patiently and sacrificially as Christ loved.",
            summaryTa = "தேவன் அன்பாகவே இருக்கிறார் (1 யோவான் 4:8) என்றும், அவருடைய அன்பு தம்முடைய ஒரேபேறான குமாரனைத் தந்தருளினதினாலே உலகத்தில் விளங்கப்பண்ணப்பட்டது (யோவான் 3:16) என்றும் வேதம் சாட்சியிடுகிறது.",
            verseReferences = listOf(
                VerseReference("1JN", 4, 8, null, "1 John 4:8", "1 யோவான் 4:8",
                    "He that loveth not knoweth not God; for God is love.",
                    "அன்பில்லாதவன் தேவனை அறியான்; தேவன் அன்பாகவே இருக்கிறார்."),
                VerseReference("JHN", 3, 16, null, "John 3:16", "யோவான் 3:16",
                    "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.",
                    "தேவன், தம்முடைய ஒரேபேறான குமாரனை விசுவாசிக்கிற எவனும் கெட்டுப்போகாமல் நித்தியஜீவனை அடையும்படிக்கு, அவரைத் தந்தருளி, இவ்வளவாய் உலகத்தில் அன்புகூர்ந்தார்."),
                VerseReference("1CO", 13, 4, 8, "1 Corinthians 13:4-8", "1 கொரிந்தியர் 13:4-8",
                    "Charity suffereth long, and is kind; charity envieth not; charity vaunteth not itself, is not puffed up...",
                    "அன்பு நீடிய சாந்தமும் தயவுமுள்ளது; அன்புக்குப் பொறாமையில்லை; அன்பு தன்னைப் புகழாது, தற்பொழிவாயிராது..."),
                VerseReference("ROM", 8, 38, 39, "Romans 8:38-39", "ரோமர் 8:38-39",
                    "For I am persuaded, that neither death, nor life... shall be able to separate us from the love of God, which is in Christ Jesus our Lord.",
                    "மரணமானாலும், ஜீவனானாலும்... நம்முடைய கர்த்தராகிய கிறிஸ்து இயேசுவிலுள்ள தேவனுடைய அன்பைவிட்டு நம்மைப் பிரிக்கமாட்டாதென்று நிச்சயித்திருக்கிறேன்.")
            )
        ),
        TopicItem(
            id = "faith",
            englishName = "Faith",
            tamilName = "விசுவாசம்",
            descriptionEn = "Unshakable trust in God's promises and walking by His unseen truth.",
            descriptionTa = "தேவனுடைய வாக்குத்தத்தங்களை முழு இருதயத்தோடும் நம்பி நடத்தல்.",
            category = "Spiritual Life",
            categoryTa = "ஆவிக்குரிய வாழ்க்கை",
            subtopics = listOf("Definition of Faith", "Walking by Faith", "Faith in Difficult Times", "Faith and Works"),
            subtopicsTa = listOf("விசுவாசத்தின் வரைவிலக்கணம்", "விசுவாசத்தினால் நடத்தல்", "துன்பத்தில் விசுவாசம்", "விசுவாசமும் கிரியைகளும்"),
            summaryEn = "Faith is the confident assurance in things hoped for and conviction of things not seen (Hebrews 11:1). Believers walk by faith, not by sight, anchoring their hope on God's unchanging faithfulness.",
            summaryTa = "விசுவாசமானது நம்பப்படுகிறவைகளின் உறுதியும், காணப்படாதவைகளின் நிச்சயமுமாயிருக்கிறது (எபிரெயர் 11:1). பார்வையினாலே நடவாமல் விசுவாசத்தினாலே நடக்கிறோம்.",
            verseReferences = listOf(
                VerseReference("HEB", 11, 1, null, "Hebrews 11:1", "எபிரெயர் 11:1",
                    "Now faith is the substance of things hoped for, the evidence of things not seen.",
                    "விசுவாசமானது நம்பப்படுகிறவைகளின் உறுதியும், காணப்படாதவைகளின் நிச்சயமுமாயிருக்கிறது."),
                VerseReference("2CO", 5, 7, null, "2 Corinthians 5:7", "2 கொரிந்தியர் 5:7",
                    "For we walk by faith, not by sight.",
                    "பார்வையினாலே நடவாமல் விசுவாசத்தினாலே நடக்கிறோம்."),
                VerseReference("PRO", 3, 5, 6, "Proverbs 3:5-6", "நீதிமொழிகள் 3:5-6",
                    "Trust in the LORD with all thine heart; and lean not unto thine own understanding.",
                    "உன் சுயபுத்தியின்மேல் சாயாமல், உன் முழு இருதயத்தோடும் கர்த்தரில் நம்பிக்கையாயிரு."),
                VerseReference("ROM", 10, 17, null, "Romans 10:17", "ரோமர் 10:17",
                    "So then faith cometh by hearing, and hearing by the word of God.",
                    "ஆதலால் விசுவாசம் கேள்வியினாலே வரும், கேள்வி தேவனுடைய வசனத்தினாலே வரும்.")
            )
        ),
        TopicItem(
            id = "prayer",
            englishName = "Prayer",
            tamilName = "ஜெபம்",
            descriptionEn = "Intimate communion with God, presenting supplications and thanksgiving.",
            descriptionTa = "தேவனுடன் நெருங்கிய ஐக்கியம், விண்ணப்பங்களையும் ஸ்தோத்திரங்களையும் சமர்ப்பித்தல்.",
            category = "Spiritual Life",
            categoryTa = "ஆவிக்குரிய வாழ்க்கை",
            subtopics = listOf("The Lord's Prayer", "Praying Without Ceasing", "Praying in Faith", "Thanksgiving in Prayer"),
            subtopicsTa = listOf("கர்த்தர் கற்பித்த ஜெபம்", "இடைவிடாத ஜெபம்", "விசுவாசத்தோடு ஜெபித்தல்", "ஸ்தோத்திரத்தோடு ஜெபம்"),
            summaryEn = "Prayer is communion with our Heavenly Father. We are invited to bring our requests with thanksgiving, casting aside anxiety (Philippians 4:6), and praying without ceasing (1 Thessalonians 5:17).",
            summaryTa = "ஜெபம் என்பது பிதாவோடு உள்ள ஐக்கியமாகும். நாம் ஒன்றுக்கும் கவலைப்படாமல், எல்லாவற்றையுங்குறித்து ஸ்தோத்திரத்தோடே கூடிய ஜெபத்தினால் தேவனிடத்தில் தெரியப்படுத்த வேண்டும்.",
            verseReferences = listOf(
                VerseReference("PHP", 4, 6, 7, "Philippians 4:6-7", "பிலிப்பியர் 4:6-7",
                    "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God.",
                    "நீங்கள் ஒன்றுக்குங் கவலைப்படாமல், எல்லாவற்றையுங்குறித்து உங்கள் விண்ணப்பங்களை ஸ்தோத்திரத்தோடே கூடிய ஜெபத்தினாலும் வேண்டுதலினாலும் தேவனுக்குத் தெரியப்படுத்துங்கள்."),
                VerseReference("1TH", 5, 17, null, "1 Thessalonians 5:17", "1 தெசலோனிக்கேயர் 5:17",
                    "Pray without ceasing.",
                    "இடைவிடாமல் ஜெபம்பண்ணுங்கள்."),
                VerseReference("MAT", 6, 9, 13, "Matthew 6:9-13", "மத்தேயு 6:9-13",
                    "After this manner therefore pray ye: Our Father which art in heaven, Hallowed be thy name.",
                    "நீங்கள் ஜெபம்பண்ணவேண்டிய விதமாவது: பரமண்டலங்களிலிருக்கிற எங்கள் பிதாவே, உம்முடைய நாமம் பரிசுத்தப்படுவதாக...")
            )
        ),
        TopicItem(
            id = "forgiveness",
            englishName = "Forgiveness",
            tamilName = "மன்னிப்பு",
            descriptionEn = "Receiving God's pardon and extending grace and mercy to others.",
            descriptionTa = "தேவனின் மன்னிப்பைப் பெற்று மற்றவர்களுக்கு கிருபையோடும் மனதுருக்கத்தோடும் மன்னித்தல்.",
            category = "Christian Living",
            categoryTa = "கிறிஸ்தவ வாழ்க்கை",
            subtopics = listOf("God's Forgiveness of Our Sins", "Forgiving Others", "Confession and Cleansing", "Reconciliation"),
            subtopicsTa = listOf("தேவன் நம் பாவங்களை மன்னிப்பது", "மற்றவர்களை மன்னித்தல்", "பாவ அறிக்கை மற்றும் சுத்திகரிப்பு", "சமாதான சமரசம்"),
            summaryEn = "God is faithful and just to cleanse us from all unrighteousness when we confess our sins (1 John 1:9). As we have received Christ's pardon, we are commanded to forgive one another tenderheartedly (Ephesians 4:32).",
            summaryTa = "நம்முடைய பாவங்களை நாம் அறிக்கையிட்டால், பாவங்களை நமக்கு மன்னித்து எல்லா அநியாயத்தையுநீக்கி நம்மைச் சுத்திகரிப்பதற்கு அவர் உண்மையும் நீதியும் உள்ளவராயிருக்கிறார் (1 யோவான் 1:9). கிறிஸ்து நமக்கு மன்னித்ததுபோல நாமும் மன்னிக்க வேண்டும்.",
            verseReferences = listOf(
                VerseReference("EPH", 4, 32, null, "Ephesians 4:32", "எபேசியர் 4:32",
                    "And be ye kind one to another, tenderhearted, forgiving one another, even as God for Christ's sake hath forgiven you.",
                    "ஒருவருக்கொருவர் தயவாயும் மனதுருக்கமாயும் இருந்து, கிறிஸ்துவுக்குள் தேவன் உங்களுக்கு மன்னித்ததுபோல, நீங்களும் ஒருவருக்கொருவர் மன்னியுங்கள்."),
                VerseReference("1JN", 1, 9, null, "1 John 1:9", "1 யோவான் 1:9",
                    "If we confess our sins, he is faithful and just to forgive us our sins, and to cleanse us from all unrighteousness.",
                    "நம்முடைய பாவங்களை நாம் அறிக்கையிட்டால், பாவங்களை நமக்கு மன்னித்து எல்லா அநியாயத்தையுநீக்கி நம்மைச் சுத்திகரிப்பதற்கு அவர் உண்மையும் நீதியும் உள்ளவராயிருக்கிறார்."),
                VerseReference("PSA", 103, 2, 3, "Psalm 103:2-3", "சங்கீதம் 103:2-3",
                    "Bless the LORD, O my soul, and forget not all his benefits: Who forgiveth all thine iniquities; who healeth all thy diseases.",
                    "என் ஆத்துமாவே, கர்த்தரை ஸ்தோத்திரி; அவர் செய்த சகல உபகாரங்களையும் மறவாதே. அவர் உன் அக்கிரமங்களையெல்லாம் மன்னித்து, உன் நோய்களையெல்லாம் குணமாக்குகிறார்.")
            )
        ),
        TopicItem(
            id = "holy_spirit",
            englishName = "The Holy Spirit",
            tamilName = "பரிசுத்த ஆவி",
            descriptionEn = "The Comforter, Guide, and power sent by the Father to dwell within believers.",
            descriptionTa = "விசுவாசிகளுக்குள் வாசம் செய்து சத்தியத்திற்குள் வழிநடத்தும் தேற்றரவாளன்.",
            category = "Spiritual Life",
            categoryTa = "ஆவிக்குரிய வாழ்க்கை",
            subtopics = listOf("The Comforter and Teacher", "Power for Witnessing", "Fruit of the Spirit", "Guidance in Truth"),
            subtopicsTa = listOf("தேற்றரவாளனும் போதகரும்", "சாட்சியிடுவதற்கான வல்லமை", "ஆவியின் கனி", "சத்தியத்தில் வழிநடத்துதல்"),
            summaryEn = "Jesus promised the Holy Spirit as the Comforter who teaches all things and brings His words to remembrance (John 14:26). Through the Spirit, believers receive divine power to be witnesses (Acts 1:8).",
            summaryTa = "இயேசு கிறிஸ்து பரிசுத்த ஆவியாகிய தேற்றரவாளனை வாக்குப்பண்ணினார்; அவர் எல்லாவற்றையும் நமக்கு போதித்து கிறிஸ்துவின் வார்த்தைகளை நினைப்பூட்டுகிறார் (யோவான் 14:26). ஆவியானவர் வரும்போது நாம் பெலனடைகிறோம்.",
            verseReferences = listOf(
                VerseReference("JHN", 14, 26, null, "John 14:26", "யோவான் 14:26",
                    "But the Comforter, which is the Holy Ghost, whom the Father will send in my name, he shall teach you all things, and bring all things to your remembrance...",
                    "என் நாமத்தினாலே பிதா அனுப்பப்போகிற பரிசுத்த ஆவியாகிய தேற்றரவாளனே எல்லாவற்றையும் உங்களுக்குப் போதித்து, நான் உங்களுக்குச் சொன்ன எல்லாவற்றையும் உங்களுக்கு நினைப்பூட்டுவார்."),
                VerseReference("ACT", 1, 8, null, "Acts 1:8", "அப்போஸ்தலர் 1:8",
                    "But ye shall receive power, after that the Holy Ghost is come upon you: and ye shall be witnesses unto me...",
                    "பரிசுத்த ஆவி உங்களிடத்தில் வரும்போது நீங்கள் பெலனடைந்து, எருசலேமிலும், யூதேயா முழுவதிலும், சமாரியாவிலும்... எனக்குச் சாட்சிகளாயிருப்பீர்கள்."),
                VerseReference("MAT", 28, 19, null, "Matthew 28:19", "மத்தேயு 28:19",
                    "Go ye therefore, and teach all nations, baptizing them in the name of the Father, and of the Son, and of the Holy Ghost:",
                    "ஆகையால், நீங்கள் புறப்பட்டுப்போய், சகல ஜாதிகளையும் சீஷராக்கி, பிதா குமாரன் பரிசுத்த ஆவியின் நாமத்திலே அவர்களுக்கு ஞானஸ்நானங்கொடுத்து...")
            )
        ),
        TopicItem(
            id = "salvation",
            englishName = "Salvation & Eternal Life",
            tamilName = "இரட்சிப்பு & நித்திய ஜீவன்",
            descriptionEn = "Redemption and eternal life received freely through faith in Christ Jesus.",
            descriptionTa = "இயேசு கிறிஸ்துவில் விசுவாசம் வைப்பதன் மூலம் இலவசமாய் நாம் பெறும் மீட்பும் நித்திய ஜீவனும்.",
            category = "Spiritual Life",
            categoryTa = "ஆவிக்குரிய வாழ்க்கை",
            subtopics = listOf("Gift of God", "Confessing Jesus as Lord", "Eternal Assurance", "Only Savior"),
            subtopicsTa = listOf("தேவனுடைய இலவச ஈவு", "இயேசுவை அறிக்கையிடுதல்", "நித்திய நிச்சயம்", "ஒரே இரட்சகர்"),
            summaryEn = "The gift of God is eternal life through Jesus Christ our Lord (Romans 6:23). Whosoever believes in Him shall not perish but have everlasting life (John 3:16).",
            summaryTa = "பாவத்தின் சம்பளம் மரணம்; தேவனுடைய கிருபைவரமோ நம்முடைய கர்த்தராகிய இயேசுகிறிஸ்துவினால் உண்டான நித்தியஜீவன் (ரோமர் 6:23). அவரை விசுவாசிக்கிற எவனும் கெட்டுப்போகாமல் நித்தியஜீவனை அடைவான்.",
            verseReferences = listOf(
                VerseReference("ROM", 6, 23, null, "Romans 6:23", "ரோமர் 6:23",
                    "For the wages of sin is death; but the gift of God is eternal life through Jesus Christ our Lord.",
                    "பாவத்தின் சம்பளம் மரணம்; தேவனுடைய கிருபைவரமோ நம்முடைய கர்த்தராகிய இயேசுகிறிஸ்துவினால் உண்டான நித்தியஜீவன்."),
                VerseReference("JHN", 3, 16, null, "John 3:16", "யோவான் 3:16",
                    "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.",
                    "தேவன், தம்முடைய ஒரேபேறான குமாரனை விசுவாசிக்கிற எவனும் கெட்டுப்போகாமல் நித்தியஜீவனை அடையும்படிக்கு, அவரைத் தந்தருளி, இவ்வளவாய் உலகத்தில் அன்புகூர்ந்தார்."),
                VerseReference("ROM", 10, 9, null, "Romans 10:9", "ரோமர் 10:9",
                    "That if thou shalt confess with thy mouth the Lord Jesus, and shalt believe in thine heart that God hath raised him from the dead, thou shalt be saved.",
                    "நீ இயேசுவை ஆண்டவரென்று உன் வாயினாலே அறிக்கையிட்டு, தேவன் அவரை மரித்தோரிலிருந்து எழுப்பினாரென்று உன் இருதயத்திலே விசுவாசித்தால் இரட்சிக்கப்படுவாய்.")
            )
        ),
        TopicItem(
            id = "peace",
            englishName = "Peace & Comfort",
            tamilName = "சமாதானம் & ஆறுதல்",
            descriptionEn = "The supernatural peace of God that guards our hearts and minds.",
            descriptionTa = "எல்லா புத்திக்கும் மேலான தேவ சமாதானமும் இருதயத்தின் அமைதியும்.",
            category = "Comfort & Hope",
            categoryTa = "ஆறுதல் & நம்பிக்கை",
            subtopics = listOf("Christ's Peace Given to Us", "Perfect Peace in Trust", "Peacemakers Blessed", "Guarding the Heart"),
            subtopicsTa = listOf("கிறிஸ்து அருளும் சமாதானம்", "நம்பிக்கையினால் வரும் பூரண சமாதானம்", "சமாதானம்பண்ணுகிறவர்கள்", "இருதயத்தைக் காக்கும் சமாதானம்"),
            summaryEn = "Jesus leaves His peace with us, not as the world gives (John 14:27). He keeps in perfect peace all those whose minds are stayed upon Him (Isaiah 26:3). Peacemakers are called children of God (Matthew 5:9).",
            summaryTa = "சமாதானத்தை உங்களுக்கு வைத்துப்போகிறேன், என்னுடைய சமாதானத்தையே உங்களுக்குக் கொடுக்கிறேன் (யோவான் 14:27). உம்மையே நம்பியிருக்கிறபடியால் பூரண சமாதானத்துடன் காப்பீர் (ஏசாயா 26:3). சமாதானம்பண்ணுகிறவர்கள் தேவனுடைய புத்திரர் எனப்படுவார்கள் (மத்தேயு 5:9).",
            verseReferences = listOf(
                VerseReference("JHN", 14, 27, null, "John 14:27", "யோவான் 14:27",
                    "Peace I leave with you, my peace I give unto you: not as the world giveth, give I unto you. Let not your heart be troubled, neither let it be afraid.",
                    "சமாதானத்தை உங்களுக்கு வைத்துப்போகிறேன், என்னுடைய சமாதானத்தையே உங்களுக்குக் கொடுக்கிறேன்; உலகம் கொடுக்கிறபிரகாரம் நான் உங்களுக்குக் கொடுக்கிறதில்லை. உங்கள் இருதயம் கலங்காமலும் பயப்படாமலும் இருப்பதாக."),
                VerseReference("MAT", 5, 9, null, "Matthew 5:9", "மத்தேயு 5:9",
                    "Blessed are the peacemakers: for they shall be called the children of God.",
                    "சமாதானம்பண்ணுகிறவர்கள் பாக்கியவான்கள்; அவர்கள் தேவனுடைய புத்திரர் என்னப்படுவார்கள்."),
                VerseReference("JER", 29, 11, null, "Jeremiah 29:11", "எரேமியா 29:11",
                    "For I know the thoughts that I think toward you, saith the LORD, thoughts of peace, and not of evil, to give you an expected end.",
                    "நீங்கள் எதிர்பார்த்திருக்கும் முடிவை உங்களுக்குக் கொடுக்கும்படிக்கு நான் உங்கள்மேல் நினைத்திருக்கிற நினைவுகளை அறிவேன் என்று கர்த்தர் சொல்லுகிறார்; அவைகள் தீமைக்கல்ல, சமாதானத்துக்கேதுவான நினைவுகளே.")
            )
        ),
        TopicItem(
            id = "wisdom",
            englishName = "Wisdom & Guidance",
            tamilName = "ஞானம் & வழிநடத்துதல்",
            descriptionEn = "Divine discernment and guidance that begins with the fear of the Lord.",
            descriptionTa = "கர்த்தருக்குப் பயப்படுதலில் தொடங்கும் பரலோக ஞானமும் தெய்வீக வழிநடத்துதலும்.",
            category = "Christian Living",
            categoryTa = "கிறிஸ்தவ வாழ்க்கை",
            subtopics = listOf("Asking God for Wisdom", "Fear of the Lord", "Righteous Paths", "Pure and Peaceable"),
            subtopicsTa = listOf("தேவனிடத்தில் ஞானம் கேட்பது", "கர்த்தருக்குப் பயப்படுதல்", "நீதியின் வழிகள்", "சுத்தமும் அமைதியும்"),
            summaryEn = "If any lack wisdom, let him ask of God who gives generously without reproach (James 1:5). The fear of the LORD is the beginning of wisdom and understanding.",
            summaryTa = "உங்களில் ஒருவன் ஞானத்தில் குறைவுள்ளவனாயிருந்தால், யாவருக்கும் சம்பூரணமாய்க் கொடுக்கிற தேவனிடத்தில் கேட்கக்கடவன், அப்பொழுது அவனுக்குக் கொடுக்கப்படும் (யாக்கோபு 1:5).",
            verseReferences = listOf(
                VerseReference("JAS", 1, 5, null, "James 1:5", "யாக்கோபு 1:5",
                    "If any of you lack wisdom, let him ask of God, that giveth to all men liberally, and upbraideth not; and it shall be given him.",
                    "உங்களில் ஒருவன் ஞானத்தில் குறைவுள்ளவனாயிருந்தால், யாவருக்கும் சம்பூரணமாய்க் கொடுக்கிறவரும் ஒருவரையும் கடிந்துகொள்ளாதவருமாகிய தேவனிடத்தில் கேட்கக்கடவன், அப்பொழுது அவனுக்குக் கொடுக்கப்படும்."),
                VerseReference("PRO", 3, 5, 6, "Proverbs 3:5-6", "நீதிமொழிகள் 3:5-6",
                    "Trust in the LORD with all thine heart; and lean not unto thine own understanding. In all thy ways acknowledge him, and he shall direct thy paths.",
                    "உன் சுயபுத்தியின்மேல் சாயாமல், உன் முழு இருதயத்தோடும் கர்த்தரில் நம்பிக்கையாயிரு. உன் வழிகளிலெல்லாம் அவரை நினைத்துக்கொள்; அப்பொழுது அவர் உன் பாதைகளைச் செவ்வைப்படுத்துவார்.")
            )
        ),
        TopicItem(
            id = "fear_anxiety",
            englishName = "Overcoming Fear & Anxiety",
            tamilName = "பயம் மற்றும் கவலையை வெல்லுதல்",
            descriptionEn = "Finding victory over worry through God's power, love, and sound mind.",
            descriptionTa = "தேவன் அருளும் வல்லமை, அன்பு, மற்றும் தெளிந்த புத்தியினால் பயத்தை வெல்லுதல்.",
            category = "Comfort & Hope",
            categoryTa = "ஆறுதல் & நம்பிக்கை",
            subtopics = listOf("Spirit of Power and Love", "Cast Cares on the Lord", "Fear Not, I am With You", "Peace of Mind"),
            subtopicsTa = listOf("வல்லமையும் அன்பும்", "கவலைகளை கர்த்தர்மேல் வைத்தல்", "பயப்படாதே நான் உன்னோடு இருக்கிறேன்", "மன அமைதி"),
            summaryEn = "God has not given us a spirit of fear, but of power, love, and a sound mind (2 Timothy 1:7). He commands us to be courageous and not dismayed, for He is always with us (Joshua 1:9).",
            summaryTa = "தேவன் நமக்கு பயமுள்ள ஆவியைக் கொடாமல், பலமும் அன்பும் தெளிந்த புத்தியுமுள்ள ஆவியையே கொடுத்திருக்கிறார் (2 தீமோத்தேயு 1:7). பயப்படாதே, கலங்காதே, உன் தேவனாகிய கர்த்தர் உன்னோடு இருக்கிறார்.",
            verseReferences = listOf(
                VerseReference("2TI", 1, 7, null, "2 Timothy 1:7", "2 தீமோத்தேயு 1:7",
                    "For God hath not given us the spirit of fear; but of power, and of love, and of a sound mind.",
                    "தேவன் நமக்கு பயமுள்ள ஆவியைக் கொடாமல், பலமும் அன்பும் தெளிந்த புத்தியுமுள்ள ஆவியையே கொடுத்திருக்கிறார்."),
                VerseReference("PSA", 23, 4, null, "Psalm 23:4", "சங்கீதம் 23:4",
                    "Yea, though I walk through the valley of the shadow of death, I will fear no evil: for thou art with me; thy rod and thy staff they comfort me.",
                    "நான் மரண இருளின் பள்ளத்தாக்கிலே நடந்தாலும் பொல்லாப்புக்குப் பயப்படேன்; தேவரீர் என்னோடேகூட இருக்கிறீர்; உமது கோலும் உமது தடியும் என்னைத் தேற்றும்."),
                VerseReference("PSA", 91, 1, 2, "Psalm 91:1-2", "சங்கீதம் 91:1-2",
                    "He that dwelleth in the secret place of the most High shall abide under the shadow of the Almighty. I will say of the LORD, He is my refuge and my fortress...",
                    "உன்னதமானவரின் மறைவிலிருக்கிறவன் சர்வவல்லவருடைய நிழலில் தங்குவான். நான் கர்த்தரை நோக்கி: நீர் என் அடைக்கலம், என் கோட்டை, நான் நம்பியிருக்கிற என் தேவன் என்று சொல்லுவேன்.")
            )
        ),
        TopicItem(
            id = "marriage_family",
            englishName = "Marriage & Family",
            tamilName = "திருமணம் மற்றும் குடும்பம்",
            descriptionEn = "Biblical foundations for family life, honoring parents, and Christ-centered marriage.",
            descriptionTa = "குடும்ப வாழ்க்கை, பெற்றோரை மதித்தல், மற்றும் கிறிஸ்துவை மையமாகக் கொண்ட திருமணத்தின் வேத அஸ்திபாரம்.",
            category = "Christian Living",
            categoryTa = "கிறிஸ்தவ வாழ்க்கை",
            subtopics = listOf("Serving the Lord as a Household", "Honoring Parents", "Sacrificial Love in Marriage", "Nurturing Children"),
            subtopicsTa = listOf("குடும்பமாய் கர்த்தரைச் சேவித்தல்", "பெற்றோரை கனம்பண்ணுதல்", "திருமணத்தில் தியாக அன்பு", "பிள்ளைகளை வளர்த்தல்"),
            summaryEn = "As Joshua declared, 'As for me and my house, we will serve the LORD' (Joshua 24:15). Christian households are grounded in mutual love, honor, and dedication to God's commandments.",
            summaryTa = "நானும் என் வீட்டாருமோவென்றால், கர்த்தரையே சேவிப்போம் என்று யோசுவா உரைத்தார் (யோசுவா 24:15). குடும்பங்கள் கிறிஸ்துவின் அன்பிலும் வேத போதனையிலும் கட்டப்பட வேண்டும்.",
            verseReferences = listOf(
                VerseReference("JOS", 24, 15, null, "Joshua 24:15", "யோசுவா 24:15",
                    "And if it seem evil unto you to serve the LORD, choose you this day whom ye will serve; but as for me and my house, we will serve the LORD.",
                    "நானும் என் வீட்டாருமோவென்றால், கர்த்தரையே சேவிப்போம்."),
                VerseReference("EPH", 4, 32, null, "Ephesians 4:32", "எபேசியர் 4:32",
                    "And be ye kind one to another, tenderhearted, forgiving one another, even as God for Christ's sake hath forgiven you.",
                    "ஒருவருக்கொருவர் தயவாயும் மனதுருக்கமாயும் இருந்து, கிறிஸ்துவுக்குள் தேவன் உங்களுக்கு மன்னித்ததுபோல, நீங்களும் ஒருவருக்கொருவர் மன்னியுங்கள்.")
            )
        ),
        TopicItem(
            id = "promises",
            englishName = "God's Promises",
            tamilName = "தேவனின் வாக்குத்தத்தங்கள்",
            descriptionEn = "Unfailing covenants confirmed in Jesus Christ for strength and hope.",
            descriptionTa = "இயேசு கிறிஸ்துவில் ஆம் என்றும் ஆமென் என்றும் உறுதியாக உள்ள தேவ வாக்குத்தத்தங்கள்.",
            category = "Divine Promises",
            categoryTa = "தேவனின் வாக்குத்தத்தங்கள்",
            subtopics = listOf("Promises of Protection", "Promises of Peace", "Promises of Eternal Life", "Promises of Strength"),
            subtopicsTa = listOf("பாதுகாப்பின் வாக்குத்தத்தம்", "சமாதானத்தின் வாக்குத்தத்தம்", "நித்திய ஜீவனின் வாக்குத்தத்தம்", "புதுப்பெலனின் வாக்குத்தத்தம்"),
            summaryEn = "For all the promises of God in Him are yea, and in Him Amen (2 Corinthians 1:20). The Lord will preserve our going out and coming in for evermore (Psalm 121:8).",
            summaryTa = "தேவனுடைய வாக்குத்தத்தங்களெல்லாம் இயேசுகிறிஸ்துவுக்குள் ஆம் என்றும், அவருக்குள் ஆமென் என்றும் இருக்கிறதே (2 கொரிந்தியர் 1:20). கர்த்தர் உன் போக்கையும் உன் வரத்தையும் இதுமுதற்கொண்டு என்றென்றைக்கும் காப்பார்.",
            verseReferences = listOf(
                VerseReference("2CO", 1, 20, null, "2 Corinthians 1:20", "2 கொரிந்தியர் 1:20",
                    "For all the promises of God in him are yea, and in him Amen, unto the glory of God by us.",
                    "தேவனுடைய வாக்குத்தத்தங்களெல்லாம் இயேசுகிறிஸ்துவுக்குள் ஆம் என்றும், அவருக்குள் ஆமென் என்றும் இருக்கிறதே."),
                VerseReference("PSA", 121, 1, 8, "Psalm 121:1-8", "சங்கீதம் 121:1-8",
                    "The LORD shall preserve thy going out and thy coming in from this time forth, and even for evermore.",
                    "கர்த்தர் உன் போக்கையும் உன் வரத்தையும் இதுமுதற்கொண்டு என்றென்றைக்கும் காப்பார்."),
                VerseReference("ISA", 40, 31, null, "Isaiah 40:31", "ஏசாயா 40:31",
                    "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles...",
                    "கர்த்தருக்குக் காத்திருக்கிறவர்களோ புதுப்பெலன் அடைந்து, கழுகுகளைப்போலச் செட்டைகளை அடித்து எழும்புவார்கள்...")
            )
        ),
        TopicItem(
            id = "healing",
            englishName = "Healing & Restoration",
            tamilName = "சுகம் & ஆரோக்கியம்",
            descriptionEn = "God's restorative power for body, soul, and spirit through Christ.",
            descriptionTa = "கிறிஸ்துவின் தழும்புகளால் நாம் பெறும் சரீர, ஆத்தும, ஆவிக்குரிய சுகம் மற்றும் ஆறுதல்.",
            category = "Comfort & Hope",
            categoryTa = "ஆறுதல் & நம்பிக்கை",
            subtopics = listOf("Healed by His Stripes", "Healer of All Diseases", "Restoration of the Soul", "Praying for the Sick"),
            subtopicsTa = listOf("அவருடைய தழும்புகளால் குணம்", "சகல நோய்களையும் நீக்குபவர்", "ஆத்துமாவை தேற்றுதல்", "நோயாளிகளுக்காக ஜெபம்"),
            summaryEn = "He was wounded for our transgressions, bruised for our iniquities, and with His stripes we are healed (Isaiah 53:5). Bless the Lord who heals all our diseases (Psalm 103:3).",
            summaryTa = "அவருடைய தழும்புகளால் குணமாகிறோம் (ஏசாயா 53:5). அவர் உன் அக்கிரமங்களையெல்லாம் மன்னித்து, உன் நோய்களையெல்லாம் குணமாக்குகிறார் (சங்கீதம் 103:3).",
            verseReferences = listOf(
                VerseReference("ISA", 53, 5, null, "Isaiah 53:5", "ஏசாயா 53:5",
                    "He was wounded for our transgressions, he was bruised for our iniquities: the chastisement of our peace was upon him; and with his stripes we are healed.",
                    "நம்முடைய மீறுதல்களினிமித்தம் அவர் காயப்பட்டு, நம்முடைய அக்கிரமங்களினிமித்தம் அவர் நொறுக்கப்பட்டார்; நமக்குச் சமாதானத்தை உண்டுபண்ணும் ஆக்கினை அவர்மேல் வந்தது; அவருடைய தழும்புகளால் குணமாகிறோம்."),
                VerseReference("PSA", 103, 2, 3, "Psalm 103:2-3", "சங்கீதம் 103:2-3",
                    "Bless the LORD, O my soul, and forget not all his benefits: Who forgiveth all thine iniquities; who healeth all thy diseases.",
                    "என் ஆத்துமாவே, கர்த்தரை ஸ்தோத்திரி; அவர் செய்த சகல உபகாரங்களையும் மறவாதே. அவர் உன் அக்கிரமங்களையெல்லாம் மன்னித்து, உன் நோய்களையெல்லாம் குணமாக்குகிறார்.")
            )
        ),
        // QA Topics
        TopicItem(
            id = "qa_prayer",
            englishName = "What Does the Bible Teach About Prayer?",
            tamilName = "ஜெபத்தைக் குறித்து வேதாகமம் என்ன போதிக்கிறது?",
            descriptionEn = "Scriptural instructions on how to pray, persistence, and seeking God's will.",
            descriptionTa = "ஜெபிக்கும் விதம், விடாமுயற்சி, மற்றும் தேவ சித்தத்தை நாடுதல் குறித்த வேத போதனை.",
            category = "Q&A Study",
            categoryTa = "வேதாகம வினாவிடைகள்",
            subtopics = listOf("How to Pray (Matthew 6)", "Supplication with Thanksgiving", "Praying Without Ceasing", "Praying in Faith"),
            subtopicsTa = listOf("ஜெபிக்கும் விதம் (மத்தேயு 6)", "ஸ்தோத்திரத்தோடு வேண்டுதல்", "இடைவிடாத ஜெபம்", "விசுவாசத்தோடு ஜெபிப்பது"),
            summaryEn = "Jesus taught His disciples to address God as 'Our Father' with reverence, submitting to His will (Matthew 6:9-13). Paul exhorted believers to replace anxiety with prayer and thanksgiving (Philippians 4:6-7) and to pray without ceasing (1 Thessalonians 5:17).",
            summaryTa = "இயேசு கிறிஸ்து 'பரமண்டலங்களிலிருக்கிற எங்கள் பிதாவே' என்று ஜெபிக்கும் முறையை கற்பித்தார் (மத்தேயு 6:9-13). கவலைப்படாமல் ஸ்தோத்திரத்தோடு ஜெபிக்கவும் (பிலிப்பியர் 4:6), இடைவிடாமல் ஜெபிக்கவும் (1 தெசலோனிக்கேயர் 5:17) வேதம் கட்டளையிடுகிறது.",
            verseReferences = listOf(
                VerseReference("MAT", 6, 9, 13, "Matthew 6:9-13", "மத்தேயு 6:9-13",
                    "After this manner therefore pray ye: Our Father which art in heaven, Hallowed be thy name.",
                    "நீங்கள் ஜெபம்பண்ணவேண்டிய விதமாவது: பரமண்டலங்களிலிருக்கிற எங்கள் பிதாவே, உம்முடைய நாமம் பரிசுத்தப்படுவதாக..."),
                VerseReference("PHP", 4, 6, 7, "Philippians 4:6-7", "பிலிப்பியர் 4:6-7",
                    "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God.",
                    "நீங்கள் ஒன்றுக்குங் கவலைப்படாமல், எல்லாவற்றையுங்குறித்து உங்கள் விண்ணப்பங்களை ஸ்தோத்திரத்தோடே கூடிய ஜெபத்தினாலும் வேண்டுதலினாலும் தேவனுக்குத் தெரியப்படுத்துங்கள்."),
                VerseReference("1TH", 5, 17, null, "1 Thessalonians 5:17", "1 தெசலோனிக்கேயர் 5:17",
                    "Pray without ceasing.",
                    "இடைவிடாமல் ஜெபம்பண்ணுங்கள்.")
            )
        ),
        TopicItem(
            id = "qa_forgiveness",
            englishName = "How to Forgive Others Biblical Guide",
            tamilName = "மற்றவர்களை எவ்வாறு மன்னிப்பது? வேத வழிகாட்டி",
            descriptionEn = "Practical steps and heart attitudes for forgiving those who wrong us.",
            descriptionTa = "நம்மை காயப்படுத்தியவர்களை மன்னிப்பதற்கான வேதபூர்வமான நடைமுறை வழிகாட்டி.",
            category = "Q&A Study",
            categoryTa = "வேதாகம வினாவிடைகள்",
            subtopics = listOf("Forgive as Christ Forgave You", "Tenderhearted Compassion", "Releasing Bitterness", "Overcoming Evil with Good"),
            subtopicsTa = listOf("கிறிஸ்து மன்னித்ததுபோல மன்னியுங்கள்", "மனதுருக்கம் காட்டுதல்", "கசப்பை அகற்றுதல்", "தீமையை நன்மையால் வெல்லுதல்"),
            summaryEn = "Scripture instructs us to forgive others because God in Christ forgave us (Ephesians 4:32). True forgiveness releases grudge and bitterness, choosing grace and tenderhearted kindness.",
            summaryTa = "கிறிஸ்துவுக்குள் தேவன் நமக்கு மன்னித்ததுபோல, நாமும் ஒருவருக்கொருவர் மன்னிக்க வேண்டும் (எபேசியர் 4:32). கசப்பை அகற்றி, கிறிஸ்துவின் கிருபையினால் மற்றவர்களை மன்னிக்க வேதம் போதிக்கிறது.",
            verseReferences = listOf(
                VerseReference("EPH", 4, 32, null, "Ephesians 4:32", "எபேசியர் 4:32",
                    "And be ye kind one to another, tenderhearted, forgiving one another, even as God for Christ's sake hath forgiven you.",
                    "ஒருவருக்கொருவர் தயவாயும் மனதுருக்கமாயும் இருந்து, கிறிஸ்துவுக்குள் தேவன் உங்களுக்கு மன்னித்ததுபோல, நீங்களும் ஒருவருக்கொருவர் மன்னியுங்கள்."),
                VerseReference("ROM", 12, 1, 2, "Romans 12:1-2", "ரோமர் 12:1-2",
                    "I beseech you therefore, brethren, by the mercies of God, that ye present your bodies a living sacrifice...",
                    "நீங்கள் உங்கள் சரீரங்களைப் பரிசுத்தமும் தேவனுக்குப் பிரியமுமான ஜீவபலியாக ஒப்புக்கொடுக்கவேண்டுமென்று தேவனுடைய இரக்கங்களை முன்னிட்டு வேண்டிக்கொள்ளுகிறேன்...")
            )
        ),
        TopicItem(
            id = "qa_faith_trials",
            englishName = "Faith During Difficult Times",
            tamilName = "கடினமான காலங்களில் விசுவாசம்",
            descriptionEn = "Enduring suffering, loss, and trials with steady hope in God's faithfulness.",
            descriptionTa = "துன்பங்கள், இழப்புகள், மற்றும் சோதனைகளின் மத்தியில் தேவனுடைய உண்மைத்துவத்தில் நிலைநிற்கும் விசுவாசம்.",
            category = "Q&A Study",
            categoryTa = "வேதாகம வினாவிடைகள்",
            subtopics = listOf("Nothing Can Separate Us from God's Love", "Waiting for Renewed Strength", "Trusting God's Sovereignty", "The Shield of Faith"),
            subtopicsTa = listOf("தேவ அன்பிலிருந்து எதுவும் பிரிக்காது", "கர்த்தருக்கு காத்திருந்து புதுப்பெலன் பெறுதல்", "தேவ திட்டத்தில் நம்பிக்கை", "விசுவாசத்தின் கேடகம்"),
            summaryEn = "Even in tribulation, distress, or persecution, nothing can separate us from the love of God in Christ Jesus (Romans 8:38-39). Those who wait upon the Lord shall renew their strength (Isaiah 40:31).",
            summaryTa = "உபத்திரவமானாலும், துன்பமானாலும் நம்முடைய கர்த்தராகிய கிறிஸ்து இயேசுவிலுள்ள தேவனுடைய அன்பைவிட்டு நம்மைப் பிரிக்கமாட்டாது (ரோமர் 8:38-39). கர்த்தருக்குக் காத்திருக்கிறவர்களோ புதுப்பெலன் அடைவார்கள் (ஏசாயா 40:31).",
            verseReferences = listOf(
                VerseReference("ROM", 8, 38, 39, "Romans 8:38-39", "ரோமர் 8:38-39",
                    "For I am persuaded, that neither death, nor life... shall be able to separate us from the love of God, which is in Christ Jesus our Lord.",
                    "நம்முடைய கர்த்தராகிய கிறிஸ்து இயேசுவிலுள்ள தேவனுடைய அன்பைவிட்டு நம்மைப் பிரிக்கமாட்டாதென்று நிச்சயித்திருக்கிறேன்."),
                VerseReference("ISA", 40, 31, null, "Isaiah 40:31", "ஏசாயா 40:31",
                    "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles...",
                    "கர்த்தருக்குக் காத்திருக்கிறவர்களோ புதுப்பெலன் அடைந்து, கழுகுகளைப்போலச் செட்டைகளை அடித்து எழும்புவார்கள்..."),
                VerseReference("PSA", 23, 1, 6, "Psalm 23:1-6", "சங்கீதம் 23:1-6",
                    "The LORD is my shepherd; I shall not want. He maketh me to lie down in green pastures...",
                    "கர்த்தர் என் மேய்ப்பராயிருக்கிறார்; நான் தாழ்ச்சியடையேன். அவர் என்னைப் புல்லுள்ள இடங்களில் படுக்கப்பண்ணுகிறார்...")
            )
        )
    )

    suspend fun analyzeTopicQuery(
        rawQuery: String,
        languageCode: String,
        repository: BibleRepository
    ): TopicAnalysisResult {
        val trimmed = rawQuery.trim()
        val normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFC)
        val isTa = languageCode == "ta"

        // Step 1: Detect intent
        val isQuestion = trimmed.contains("?") ||
                trimmed.startsWith("what", ignoreCase = true) ||
                trimmed.startsWith("how", ignoreCase = true) ||
                trimmed.startsWith("why", ignoreCase = true) ||
                trimmed.startsWith("who", ignoreCase = true) ||
                trimmed.contains("teach", ignoreCase = true) ||
                trimmed.contains("say about", ignoreCase = true) ||
                trimmed.contains("எப்படி") ||
                trimmed.contains("என்ன") ||
                trimmed.contains("ஏன்") ||
                trimmed.contains("போதிக்கிறது") ||
                trimmed.contains("கூறுகிறது")

        val refCheck = BibleReferenceParser.parse(trimmed)
        val isRef = refCheck is ReferenceParseResult.Success

        val intent = when {
            isRef -> TopicIntent.DIRECT_REFERENCE
            isQuestion -> TopicIntent.QUESTION
            trimmed.split(Regex("""\s+""")).size > 2 -> TopicIntent.THEMATIC_STUDY
            else -> TopicIntent.KEYWORD
        }

        // Step 2: Look for best-matching curated topic
        val matchedCurated = curatedTopics.firstOrNull { topic ->
            topic.englishName.equals(trimmed, ignoreCase = true) ||
            topic.tamilName.equals(trimmed, ignoreCase = true) ||
            topic.id.equals(trimmed, ignoreCase = true) ||
            topic.subtopics.any { it.contains(trimmed, ignoreCase = true) } ||
            topic.subtopicsTa.any { it.contains(trimmed, ignoreCase = true) } ||
            trimmed.contains(topic.englishName, ignoreCase = true) ||
            trimmed.contains(topic.tamilName, ignoreCase = true)
        }

        // Step 3: Search real Bible verses in database
        val searchResult = repository.searchVersesAdvanced(
            query = trimmed,
            languageCode = languageCode,
            searchMode = SearchMode.ANY_WORD,
            wholeWord = false
        ).first

        val directMatches = searchResult.take(15)

        // Step 4: Extract verses and group them
        val foundational = mutableListOf<VerseReference>()
        val teachings = mutableListOf<VerseReference>()
        val promises = mutableListOf<VerseReference>()
        val practical = mutableListOf<VerseReference>()

        if (matchedCurated != null) {
            val refs = matchedCurated.verseReferences
            refs.forEachIndexed { index, ref ->
                when (index % 4) {
                    0 -> foundational.add(ref)
                    1 -> teachings.add(ref)
                    2 -> promises.add(ref)
                    3 -> practical.add(ref)
                }
            }
        }

        // Fill from direct matches if needed
        directMatches.forEachIndexed { index, item ->
            val v = item.verse
            val b = CanonicalBibleData.findBookById(v.bookId)
            val engBook = b?.englishName ?: v.bookName
            val taBook = b?.tamilName ?: v.bookName
            val vRef = VerseReference(
                bookId = v.bookId,
                chapter = v.chapterNumber,
                verse = v.verseNumber,
                endVerse = null,
                englishRef = "$engBook ${v.chapterNumber}:${v.verseNumber}",
                tamilRef = "$taBook ${v.chapterNumber}:${v.verseNumber}",
                textEn = if (v.languageCode == "en") v.verseText else "",
                textTa = if (v.languageCode == "ta") v.verseText else ""
            )
            when {
                foundational.size < 3 -> foundational.add(vRef)
                teachings.size < 3 -> teachings.add(vRef)
                promises.size < 3 -> promises.add(vRef)
                practical.size < 3 -> practical.add(vRef)
            }
        }

        val titleEn = matchedCurated?.englishName ?: trimmed.replaceFirstChar { it.uppercase() }
        val titleTa = matchedCurated?.tamilName ?: trimmed

        val descEn = matchedCurated?.descriptionEn
            ?: "Biblical study on '$titleEn', examining passages, context, and practical faith application."
        val descTa = matchedCurated?.descriptionTa
            ?: "'$titleTa' குறித்த வேதாகம போதனைகள், வசன ஆதாரங்கள் மற்றும் ஆவிக்குரிய தியானம்."

        val summaryEn = matchedCurated?.summaryEn?.takeIf { it.isNotBlank() }
            ?: "The Holy Scriptures present '$titleEn' as a key spiritual truth, instructing believers to walk faithfully according to God's Word with hope and obedience."
        val summaryTa = matchedCurated?.summaryTa?.takeIf { it.isNotBlank() }
            ?: "வேதாகமம் '$titleTa' குறித்த சத்தியங்களை விசுவாசிகளுக்குத் தெளிவுபடுத்தி, தேவ வார்த்தைக்குக் கீழ்ப்படிந்து நம்பிக்கையுடன் வாழ வழிகாட்டுகிறது."

        val subtopicsEn = matchedCurated?.subtopics ?: listOf(
            "$titleEn in the Old Testament",
            "Teachings of Jesus on $titleEn",
            "Living Out $titleEn Today",
            "Promises Concerning $titleEn"
        )
        val subtopicsTa = matchedCurated?.subtopicsTa ?: listOf(
            "பழைய ஏற்பாட்டில் $titleTa",
            "இயேசு கிறிஸ்துவின் $titleTa உபதேசம்",
            "நடைமுறை வாழ்வில் $titleTa",
            "$titleTa குறித்த தேவ வாக்குத்தத்தங்கள்"
        )

        return TopicAnalysisResult(
            query = trimmed,
            intent = intent,
            titleEn = titleEn,
            titleTa = titleTa,
            descriptionEn = descEn,
            descriptionTa = descTa,
            summaryEn = summaryEn,
            summaryTa = summaryTa,
            subtopicsEn = subtopicsEn,
            subtopicsTa = subtopicsTa,
            passages = CategorizedTopicPassages(
                directMatches = directMatches,
                foundationalPassages = foundational.distinctBy { "${it.bookId}_${it.chapter}_${it.verse}" },
                teachingsAndExhortations = teachings.distinctBy { "${it.bookId}_${it.chapter}_${it.verse}" },
                promisesAndComfort = promises.distinctBy { "${it.bookId}_${it.chapter}_${it.verse}" },
                practicalApplications = practical.distinctBy { "${it.bookId}_${it.chapter}_${it.verse}" }
            )
        )
    }
}
