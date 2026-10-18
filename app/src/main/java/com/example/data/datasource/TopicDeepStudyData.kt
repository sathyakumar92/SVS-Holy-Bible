package com.example.data.datasource

import com.example.data.model.OriginalWordStudy

/**
 * Authentic biblical scholarship data for Bible Topic Deep Study:
 * Original Hebrew and Greek terms, theological meanings, transliterations, and context.
 * Verified according to Strong's Concordance, Gesenius Hebrew Lexicon, and Thayer's Greek Lexicon.
 */
object TopicDeepStudyData {

    fun getOriginalWordStudies(topicId: String): List<OriginalWordStudy> {
        return when (topicId) {
            "faith" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "אֱמוּנָה",
                    transliteration = "'Emunah",
                    literalMeaningEn = "Firmness, steadfast fidelity, moral stability, unshakeable trust",
                    literalMeaningTa = "நிலைத்தன்மை, உண்மையுள்ள தன்மை, தேவனிடத்தில் அசைக்க முடியாத நம்பிக்கை",
                    theologicalSignificanceEn = "In the Old Testament, faith is not mere intellectual assent, but active, steadfast faithfulness and reliance on God's covenant promises.",
                    theologicalSignificanceTa = "பழைய ஏற்பாட்டில் விசுவாசம் என்பது வெறும் அறிவார்ந்த ஒப்புதல் அல்ல, மாறாக தேவனுடைய உடன்படிக்கையின் மீது காட்டும் உறுதியான உண்மைத்தன்மை.",
                    primaryScriptureEn = "Habakkuk 2:4 — 'The just shall live by his faith.'",
                    primaryScriptureTa = "ஆபகூக் 2:4 — 'விசுவாசத்தினாலே நீதிமான் பிழைப்பான்.'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "πίστις",
                    transliteration = "Pistis",
                    literalMeaningEn = "Conviction of truth, wholehearted trust, surrendered allegiance to Christ",
                    literalMeaningTa = "சத்தியத்தின் மீது முழுமையான நம்பிக்கை, கிறிஸ்துவிடம் நம்மை ஒப்புக்கொடுக்கும் கீழ்ப்படிதல்",
                    theologicalSignificanceEn = "In the New Testament, pistis is saving reliance upon Jesus Christ, producing spiritual fruit and perseverance through trials.",
                    theologicalSignificanceTa = "புதிய ஏற்பாட்டில் பிஸ்திஸ் என்பது இயேசு கிறிஸ்துவை இரட்சகராக ஏற்றுக்கொள்ளும் ஜீவனுள்ள விசுவாசம்.",
                    primaryScriptureEn = "Hebrews 11:1 — 'Now faith is the substance of things hoped for, the evidence of things not seen.'",
                    primaryScriptureTa = "எபிரெயர் 11:1 — 'விசுவாசமானது நம்பப்படுகிறவைகளின் உறுதியும், காணப்படாதவைகளின் நிச்சயமுமாயிருக்கிறது.'"
                )
            )

            "love" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "חֶסֶד",
                    transliteration = "Chesed",
                    literalMeaningEn = "Covenant lovingkindness, steadfast loyal devotion, merciful goodness",
                    literalMeaningTa = "உடன்படிக்கையின் கிருபை, மாறாத பேரன்பு, இரக்கம் நிறைந்த தயவு",
                    theologicalSignificanceEn = "Describes God's enduring, committed love towards His covenant people that never fails despite human weakness.",
                    theologicalSignificanceTa = "மனித பலவீனங்களின் மத்தியிலும் ஒருபோதும் மாறாத தேவனுடைய உடன்படிக்கையின் நித்திய அன்பைக் குறிக்கிறது.",
                    primaryScriptureEn = "Psalm 136:1 — 'For His mercy endureth for ever.'",
                    primaryScriptureTa = "சங்கீதம் 136:1 — 'அவர் கிருபை என்றுமுள்ளது.'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "ἀγάπη",
                    transliteration = "Agape",
                    literalMeaningEn = "Unconditional, self-sacrificing, benevolent divine love",
                    literalMeaningTa = "நிபந்தனையற்ற, தியாகம் நிறைந்த, தெய்வீக தூய அன்பு",
                    theologicalSignificanceEn = "Highest form of love demonstrated when God gave His only begotten Son at Calvary for sinners while we were yet enemies.",
                    theologicalSignificanceTa = "நாம் பாவிகளாயிருக்கையில் கல்வாரியில் தம் ஒரேபேறான குமாரனை நமக்காக தந்த தேவனுடைய ஈடு இணையற்ற அன்பு.",
                    primaryScriptureEn = "1 John 4:8 — 'God is love.' / John 3:16",
                    primaryScriptureTa = "1 யோவான் 4:8 — 'தேவன் அன்பாகவே இருக்கிறார்.'"
                )
            )

            "peace" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "שָׁלוֹם",
                    transliteration = "Shalom",
                    literalMeaningEn = "Wholeness, completeness, sound health, prosperity, reconciliation",
                    literalMeaningTa = "முழுமை, பரிபூரண நலம், சமாதானம், தேவ சமாதானத்தின் சுகவாழ்வு",
                    theologicalSignificanceEn = "Biblical shalom is not simply the absence of conflict, but the positive presence of God's order, righteousness, and harmonious well-being.",
                    theologicalSignificanceTa = "ஷாலோம் என்பது வெறும் போரற்ற நிலை அல்ல; அது தேவனுடைய நீதி, அமைதி மற்றும் பரிபூரண ஆசீர்வாதத்தின் பிரசன்னமாகும்.",
                    primaryScriptureEn = "Isaiah 26:3 — 'Thou wilt keep him in perfect peace, whose mind is stayed on thee.'",
                    primaryScriptureTa = "ஏசாயா 26:3 — 'உம்மை உறுதியாய்ப் பற்றிக்கொண்ட மனதையுடையவன் உம்மையே நம்பியிருக்கிறபடியினால், நீர் அவனைப் பூரண சமாதானத்துடன் காத்துக்கொள்வீர்.'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "εἰρήνη",
                    transliteration = "Eirene",
                    literalMeaningEn = "Tranquility of spirit, quiet rest, state of reconciliation with God",
                    literalMeaningTa = "ஆத்தும அமைதி, தேவனுடன் ஒப்புரவாக்கப்படுதலால் வரும் தெய்வீக இளைப்பாறுதல்",
                    theologicalSignificanceEn = "Purchased by Christ's blood, guarding the hearts and minds of believers even in persecution.",
                    theologicalSignificanceTa = "கிறிஸ்துவின் இரத்தத்தினால் கிரயமாய் பெறப்பட்டு, எல்லா புத்திக்கும் மேலானதாய் இதயத்தைப் பாதுகாக்கும் சமாதானம்.",
                    primaryScriptureEn = "Philippians 4:7 — 'And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus.'",
                    primaryScriptureTa = "பிலிப்பியர் 4:7 — 'எல்லாப் புத்திக்கும் மேலான தேவ சமாதானம் உங்கள் இருதயங்களையும் உங்கள் சிந்தைகளையும் கிறிஸ்து இயேசுவுக்குள்ளாகக் காத்துக்கொள்ளும்.'"
                )
            )

            "grace" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "חֵן",
                    transliteration = "Chen",
                    literalMeaningEn = "Favor, graciousness, beauty of character freely bestowed",
                    literalMeaningTa = "தயவு, கிருபை, தகுதியற்றவனுக்கு இலவசமாக அளிக்கப்படும் தயை",
                    theologicalSignificanceEn = "Found by Noah before the flood, reflecting God's merciful disposition towards the humble.",
                    theologicalSignificanceTa = "நோவா கர்த்தரின் கண்களில் தயவு பெற்றார்; தாழ்மையுள்ளவர்களுக்கு தேவன் அளிக்கும் கிருபை.",
                    primaryScriptureEn = "Genesis 6:8 — 'But Noah found grace in the eyes of the LORD.'",
                    primaryScriptureTa = "ஆதியாகமம் 6:8 — 'நோவாவுக்கோ, கர்த்தருடைய கண்களில் கிருபை கிடைத்தது.'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "χάρις",
                    transliteration = "Charis",
                    literalMeaningEn = "Unmerited favor, empowering divine influence upon the heart",
                    literalMeaningTa = "ஈடுசெய்ய முடியாத இலவச கிருபை, இருதயத்தை பெலப்படுத்தும் தெய்வீக வல்லமை",
                    theologicalSignificanceEn = "The basis of salvation (Ephesians 2:8) and daily spiritual sufficiency (2 Corinthians 12:9).",
                    theologicalSignificanceTa = "இரட்சிப்பின் ஆணிவேர் (எபேசியர் 2:8) மற்றும் பலவீனத்தில் பரிபூரணமாய் விளங்கும் பலன்.",
                    primaryScriptureEn = "Ephesians 2:8 — 'For by grace are ye saved through faith; and that not of yourselves: it is the gift of God.'",
                    primaryScriptureTa = "எபேசியர் 2:8 — 'கிருபையினாலே விசுவாசத்தைக்கொண்டு இரட்சிக்கப்பட்டீர்கள்; இது உங்களால் உண்டானதல்ல, இது தேவனுடைய ஈவு.'"
                )
            )

            "prayer" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "תְּפִלָּה",
                    transliteration = "Tefillah",
                    literalMeaningEn = "Intercession, supplication, earnest petition, self-assessment before God",
                    literalMeaningTa = "விண்ணப்பம், மன்றாட்டு, தேவ சந்நிதானத்தில் தன்னைத் தாழ்த்தி வேண்டுதல் செய்தல்",
                    theologicalSignificanceEn = "Derived from palal (to arbitrate, judge), prayer aligns our will with God's sovereign holiness and purposes.",
                    theologicalSignificanceTa = "நம் சுய சித்தத்தை தேவனுடைய பரிசுத்த சித்தத்திற்கு ஒப்புக்கொடுத்து செய்யும் ஐக்கிய ஜெபம்.",
                    primaryScriptureEn = "Psalm 102:17 — 'He will regard the prayer of the destitute.'",
                    primaryScriptureTa = "சங்கீதம் 102:17 — 'திக்கற்றவர்களின் ஜெபத்தை அலட்சியம்பண்ணாமலும், அவர்கள் விண்ணப்பத்தைக் கேட்பார்.'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "προσευχή",
                    transliteration = "Proseuche",
                    literalMeaningEn = "Reverent prayer, addressing God directly with devotion and worship",
                    literalMeaningTa = "பக்தி நிறைந்த ஆராதனை ஜெபம், தேவனிடத்தில் நேரடியாகப் பேசுதல்",
                    theologicalSignificanceEn = "Carries the sense of pouring out one's soul in fellowship and submission before God the Father.",
                    theologicalSignificanceTa = "பரலோக பிதாவின் பாதத்தில் முழு உள்ளத்தையும் ஊற்றி செய்யும் ஐக்கிய ஜெபம்.",
                    primaryScriptureEn = "1 Thessalonians 5:17 — 'Pray without ceasing.'",
                    primaryScriptureTa = "1 தெசலோனிக்கேயர் 5:17 — 'இடைவிடாமல் ஜெபம்பண்ணுங்கள்.'"
                )
            )

            "holy_spirit" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "רוּחַ הַקֹּדֶשׁ",
                    transliteration = "Ruach HaKodesh",
                    literalMeaningEn = "Holy Breath, Divine Wind, Spirit of the Sovereign God",
                    literalMeaningTa = "பரிசுத்த ஆவி, தேவனுடைய ஜீவசுவாசம், தெய்வீகக் காற்று",
                    theologicalSignificanceEn = "The active life-giver hovering over the waters at creation, equipping prophets, judges, and kings.",
                    theologicalSignificanceTa = "சிருஷ்டிப்பின்போது அசைவாடி, தீர்க்கதரிசிகளையும் தேவ மனிதர்களையும் பெலப்படுத்திய பரிசுத்த ஆவியானவர்.",
                    primaryScriptureEn = "Genesis 1:2 / Psalm 51:11 — 'Take not thy holy spirit from me.'",
                    primaryScriptureTa = "சங்கீதம் 51:11 — 'உமது பரிசுத்த ஆவியை என்னிடத்திலிருந்து எடுத்துக்கொள்ளாதேயும்.'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "παράκλητος",
                    transliteration = "Parakletos",
                    literalMeaningEn = "One called alongside to help, Advocate, Comforter, Counselor",
                    literalMeaningTa = "உடன் நின்று சகாயம் செய்கிற தேற்றரவாளன், பரிந்துபேசுபவர், வழிகாட்டி",
                    theologicalSignificanceEn = "Christ promised the Holy Spirit as another Comforter of the exact same divine nature to indwell believers forever.",
                    theologicalSignificanceTa = "விசுவாசிகளுக்குள் என்றென்றைக்கும் வாசம்பண்ணும் தெய்வீக தேற்றரவாளன் மற்றும் சத்திய ஆவியானவர்.",
                    primaryScriptureEn = "John 14:26 — 'The Comforter, which is the Holy Ghost, whom the Father will send in my name.'",
                    primaryScriptureTa = "யோவான் 14:26 — 'என் நாமத்தினாலே பிதா அனுப்பப்போகிற பரிசுத்த ஆவியாகிய தேற்றரவாளனே எல்லாவற்றையும் உங்களுக்குப் போதித்து...'"
                )
            )

            "wisdom" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "חָכְמָה",
                    transliteration = "Chokmah",
                    literalMeaningEn = "Skill in holy living, sound moral judgment, godly discernment",
                    literalMeaningTa = "பரிசுத்தமாய் வாழும் ஞானம், மெய்யான பகுத்தறிவு, தேவ பயத்தினால் வரும் விவேகம்",
                    theologicalSignificanceEn = "Begins with the reverent fear of the LORD and directs every practical decision of daily life.",
                    theologicalSignificanceTa = "கர்த்தருக்குப் பயப்படுதலே ஞானத்தின் ஆரம்பம்; அன்றாட வாழ்க்கையை தேவ சித்தப்படி நடத்தும் கலை.",
                    primaryScriptureEn = "Proverbs 9:10 — 'The fear of the LORD is the beginning of wisdom.'",
                    primaryScriptureTa = "நீதிமொழிகள் 9:10 — 'கர்த்தருக்குப் பயப்படுதலே ஞானத்தின் ஆரம்பம்; பரிசுத்தரின் அறிவே அறிவு.'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "σοφία",
                    transliteration = "Sophia",
                    literalMeaningEn = "Heavenly wisdom, spiritual insight, practical purity from above",
                    literalMeaningTa = "பரத்திலிருந்து வரும் ஞானம், ஆவிக்குரிய விவேகம், சமாதானமும் நற்கனியும் நிறைந்த அறிவு",
                    theologicalSignificanceEn = "The wisdom from above is first pure, then peaceable, gentle, and full of mercy (James 3:17).",
                    theologicalSignificanceTa = "பரத்திலிருந்து வருகிற ஞானமோ முதலாவது சுத்தமுள்ளதாயும், பின்பு சமாதானமும் சாந்தமும் இணக்கமும் இரக்கமும் நிறைந்ததாயும் இருக்கிறது.",
                    primaryScriptureEn = "James 1:5 — 'If any of you lack wisdom, let him ask of God.'",
                    primaryScriptureTa = "யாக்கோபு 1:5 — 'உங்களில் ஒருவன் ஞானத்தில் குறைவுள்ளவனாயிருந்தால், யாவருக்கும் சம்பூரணமாய்க் கொடுக்கிற தேவனிடத்தில் கேட்கக்கடவன்.'"
                )
            )

            "forgiveness" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "סָלַח",
                    transliteration = "Salach",
                    literalMeaningEn = "To pardon, forgive, remit completely (used exclusively of God's forgiveness)",
                    literalMeaningTa = "மன்னித்தல், குற்றத்தை நீக்குதல் (தேவனுடைய தெய்வீக மன்னிப்பிற்கு மட்டுமே பயன்படுத்தப்படும் சொல்)",
                    theologicalSignificanceEn = "In the Old Testament, salach is reserved only for God; humans may be pardoned, but only God possesses the prerogative to blot out guilt.",
                    theologicalSignificanceTa = "வேதாகமத்தில் சலாக் என்பது தேவனுக்கு மட்டுமே உரிய மன்னிப்பு அதிகாரம்; அவர் மட்டுமே அக்கிரமங்களை மன்னித்து அகற்றுபவர்.",
                    primaryScriptureEn = "Psalm 103:3 — 'Who forgiveth all thine iniquities; who healeth all thy diseases.'",
                    primaryScriptureTa = "சங்கீதம் 103:3 — 'அவர் உன் அக்கிரமங்களையெல்லாம் மன்னித்து, உன் நோய்களையெல்லாம் குணமாக்கி...'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "ἄφεσις",
                    transliteration = "Aphesis",
                    literalMeaningEn = "Release from a debt, complete liberation, total dismissal of sins",
                    literalMeaningTa = "கடனிலிருந்து விடுதலை, முழுமையான பாவமன்னிப்பு, தண்டனையிலிருந்து விடுவிக்கப்படுதல்",
                    theologicalSignificanceEn = "Accomplished through the shed blood of Jesus on the Cross, wiping clean the believer's spiritual ledger.",
                    theologicalSignificanceTa = "கல்வாரி சிலுவையில் சிந்தப்பட்ட இயேசுவின் இரத்தத்தினால் விசுவாசிக்கு கிடைத்த நித்திய பாவமன்னிப்பு.",
                    primaryScriptureEn = "Ephesians 1:7 — 'In whom we have redemption through his blood, the forgiveness of sins.'",
                    primaryScriptureTa = "எபேசியர் 1:7 — 'அவருடைய இரத்தத்தினாலே பாவமன்னிப்பாகிய மீட்பு நமக்கு உண்டாயிருக்கிறது.'"
                )
            )

            "salvation" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "יְשׁוּעָה",
                    transliteration = "Yeshu'ah",
                    literalMeaningEn = "Deliverance, rescue, victory, total spiritual and physical preservation",
                    literalMeaningTa = "இரட்சிப்பு, விடுதலை, வெற்றி, தீங்கிலிருந்து மீட்கப்படும் தெய்வீக பாதுகாப்பு",
                    theologicalSignificanceEn = "The root of the name Jesus (Yeshua: 'The LORD is Salvation'), who delivers His people from their sins.",
                    theologicalSignificanceTa = "இயேசு என்ற நாமத்தின் மூலம் (யெஷுவா: 'கர்த்தரே இரட்சிப்பு'); தமது ஜனங்களை பாவங்களிலிருந்து இரட்சிப்பவர்.",
                    primaryScriptureEn = "Exodus 14:13 — 'Stand still, and see the salvation of the LORD.'",
                    primaryScriptureTa = "யாத்திராகமம் 14:13 — 'பயப்படாதிருங்கள்; நீங்கள் நின்று, இன்றைக்குக் கர்த்தர் உங்களுக்குச் செய்யும் இரட்சிப்பைப் பாருங்கள்.'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "σωτηρία",
                    transliteration = "Soteria",
                    literalMeaningEn = "Deliverance from the penalty and power of sin into eternal safety and glory",
                    literalMeaningTa = "பாவத்தின் ஆக்கினையிலிருந்தும் அதிகாரத்திலிருந்தும் பெறப்படும் நித்திய மீட்பு",
                    theologicalSignificanceEn = "Past (justification), present (sanctification), and future (glorification) work of God in Christ.",
                    theologicalSignificanceTa = "கிறிஸ்துவுக்குள் தேவனால் அருளப்பட்ட முழுமையான மற்றும் நித்தியமான இரட்சிப்பு.",
                    primaryScriptureEn = "Romans 1:16 — 'The gospel of Christ: for it is the power of God unto salvation.'",
                    primaryScriptureTa = "ரோமர் 1:16 — 'கிறிஸ்துவின் சுவிசேஷத்தைக் குறித்து நான் வெட்கப்படேன்; விசுவாசிக்கிறவனெவனோ அவனுக்கு இரட்சிப்பு உண்டாவதற்கு அது தேவபெலனாயிருக்கிறது.'"
                )
            )

            "repentance" -> listOf(
                OriginalWordStudy(
                    language = "Hebrew",
                    originalWord = "שׁוּב",
                    transliteration = "Shuv",
                    literalMeaningEn = "To turn back, return 180 degrees, restore relationship with God",
                    literalMeaningTa = "திரும்புதல், தவறான வழியை விட்டு தேவனிடம் முழு இருதயத்தோடு திரும்புதல்",
                    theologicalSignificanceEn = "A radical reorientation of life away from idols and rebellion, turning directly to the living God.",
                    theologicalSignificanceTa = "பாவ வழிகளை முற்றிலும் துறந்து, உயிருள்ள தேவனிடம் மனப்பூர்வமாய் திரும்புதல்.",
                    primaryScriptureEn = "Joel 2:12 — 'Turn ye even to me with all your heart.'",
                    primaryScriptureTa = "யோவேல் 2:12 — 'இப்பொழுதும் நீங்கள் முழு இருதயத்தோடும் என்னிடத்தில் திரும்புங்கள் என்று கர்த்தர் சொல்லுகிறார்.'"
                ),
                OriginalWordStudy(
                    language = "Greek",
                    originalWord = "μετάνοια",
                    transliteration = "Metanoia",
                    literalMeaningEn = "A transformative change of mind, heart, purpose, and moral direction",
                    literalMeaningTa = "மனந்திரும்புதல், சிந்தையிலும் இதயத்திலும் ஏற்படும் ஆழமான தெய்வீக மறுரூபம்",
                    theologicalSignificanceEn = "Not merely worldly sorrow or remorse, but godly sorrow that leads to salvation (2 Corinthians 7:10).",
                    theologicalSignificanceTa = "வெறும் மனவேதனை அல்ல; இரட்சிப்பை உண்டாக்குகிற தேவனுக்கேற்ற மனஸ்தாபம்.",
                    primaryScriptureEn = "Acts 3:19 — 'Repent ye therefore, and be converted, that your sins may be blotted out.'",
                    primaryScriptureTa = "அப்போஸ்தலர் 3:19 — 'உங்கள் பாவங்கள் நிவிர்த்தி செய்யப்படுவதற்கு நீங்கள் மனந்திரும்பி குணப்படுங்கள்.'"
                )
            )

            else -> listOf(
                OriginalWordStudy(
                    language = "Biblical Greek",
                    originalWord = "ἀλήθεια",
                    transliteration = "Aletheia",
                    literalMeaningEn = "Truth, reality, divine disclosure of spiritual verity",
                    literalMeaningTa = "சத்தியம், மெய்ம்மை, தேவனுடைய மாறாத உண்மை",
                    theologicalSignificanceEn = "Reflects the unalterable character of God's Word revealed throughout all Scripture.",
                    theologicalSignificanceTa = "வேதாகமத்தில் வெளிப்படுத்தப்பட்ட தேவனுடைய மாறாத வார்த்தையின் உண்மைத்தன்மை.",
                    primaryScriptureEn = "John 17:17 — 'Sanctify them through thy truth: thy word is truth.'",
                    primaryScriptureTa = "யோவான் 17:17 — 'உம்முடைய சத்தியத்தினாலே அவர்களைப் பரிசுத்தமாக்கும்; உம்முடைய வசனமே சத்தியம்.'"
                ),
                OriginalWordStudy(
                    language = "Biblical Hebrew",
                    originalWord = "אֱמֶת",
                    transliteration = "'Emet",
                    literalMeaningEn = "Trustworthiness, firmness, permanence, fidelity",
                    literalMeaningTa = "உண்மைத்தன்மை, உறுதி, மாறாத உடன்படிக்கையின் நம்பகத்தன்மை",
                    theologicalSignificanceEn = "The foundation of God's throne and His dealings with humanity across all generations.",
                    theologicalSignificanceTa = "தலைமுறை தலைமுறையாக தேவனுடைய ஆளுகையின் அடிப்படையாய் விளங்கும் உண்மை.",
                    primaryScriptureEn = "Psalm 119:160 — 'Thy word is true from the beginning.'",
                    primaryScriptureTa = "சங்கீதம் 119:160 — 'உம்முடைய வசனம் துவக்கமுதல் சத்தியமும், உம்முடைய நீதி நியாயமெல்லாம் நித்தியமுமானது.'"
                )
            )
        }
    }
}
