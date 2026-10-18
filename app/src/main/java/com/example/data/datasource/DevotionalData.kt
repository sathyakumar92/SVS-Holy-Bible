package com.example.data.datasource

import com.example.data.model.*

object DevotionalData {

    val topics: List<TopicItem> = AdvancedTopicEngine.curatedTopics


    val dailyVerses: List<DailyVerse> = listOf(
        DailyVerse(
            dateString = "today",
            referenceEn = "John 3:16",
            referenceTa = "யோவான் 3:16",
            bookId = "JHN",
            chapter = 3,
            verse = 16,
            textEn = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.",
            textTa = "தேவன், தம்முடைய ஒரேபேறான குமாரனை விசுவாசிக்கிற எவனும் கெட்டுப்போகாமல் நித்தியஜீவனை அடையும்படிக்கு, அவரைத் தந்தருளி, இவ்வளவாய் உலகத்தில் அன்புகூர்ந்தார்.",
            reflectionEn = "God's immense love for humanity is demonstrated in the ultimate sacrifice of Christ. Meditate on the everlasting life given freely through faith today.",
            reflectionTa = "மனிதகுலத்தின் மீதான தேவனுடைய எல்லையற்ற அன்பு கிறிஸ்துவின் தியாகத்தில் வெளிப்பட்டது. விசுவாசத்தின் மூலம் நாம் பெறும் நித்திய ஜீவனை இன்று தியானியுங்கள்."
        ),
        DailyVerse(
            dateString = "yesterday",
            referenceEn = "Psalm 23:1",
            referenceTa = "சங்கீதம் 23:1",
            bookId = "PSA",
            chapter = 23,
            verse = 1,
            textEn = "The LORD is my shepherd; I shall not want.",
            textTa = "கர்த்தர் என் மேய்ப்பராயிருக்கிறார்; நான் தாழ்ச்சியடையேன்.",
            reflectionEn = "When the Lord leads your life, every true spiritual need is abundantly supplied. Rest in His faithful guidance.",
            reflectionTa = "கர்த்தர் உங்கள் வாழ்க்கையை வழிநடத்தும்போது, உங்கள் தேவைகள் யாவும் நிறைவாக்கப்படும். அவருடைய உண்மையுள்ள வழிநடத்துதலில் இளைப்பாறுங்கள்."
        ),
        DailyVerse(
            dateString = "day_before",
            referenceEn = "Philippians 4:13",
            referenceTa = "பிலிப்பியர் 4:13",
            bookId = "PHP",
            chapter = 4,
            verse = 13,
            textEn = "I can do all things through Christ which strengtheneth me.",
            textTa = "என்னைப்பலப்படுத்துகிற கிறிஸ்துவினாலே எல்லாவற்றையுஞ்செய்ய எனக்குப் பெலனுண்டு.",
            reflectionEn = "Your true capability does not rely on earthly might, but on the unfailing strength of Christ dwelling in you.",
            reflectionTa = "உங்கள் பலன் உங்கள் சொந்த திறமையில் அல்ல, உங்களில் வாசம் செய்யும் கிறிஸ்துவின் வல்லமையிலேயே தங்கியிருக்கிறது."
        )
    )

    val dailyPrayers: List<DailyPrayer> = listOf(
        DailyPrayer(
            dateString = "today",
            titleEn = "A Prayer for Divine Peace and Guidance",
            titleTa = "தெய்வீக சமாதானத்திற்கும் வழிநடத்துதலுக்குமான ஜெபம்",
            contentEn = "Heavenly Father, we come humbly before Your throne of grace this morning. Quiet our anxious hearts with Your transcendent peace. Fill our steps with Your wisdom and guard our minds against doubt and fear. May our thoughts, words, and actions today bring honor to Your Holy Name, through Jesus Christ our Lord. Amen.",
            contentTa = "பரலோகப் பிதாவே, உமது கிருபையின் சிங்காசனத்தின் முன் தாழ்மையுடன் வருகிறோம். உமது சமாதானத்தினால் எங்கள் இருதயங்களை அமைதிப்படுத்தும். உமது ஞானத்தினால் எங்கள் அடிகளை வழிநடத்தும். இன்றைய எங்கள் சிந்தனைகள், வார்த்தைகள் மற்றும் செயல்கள் அனைத்தும் உமது நாமத்தை மகிமைப்படுத்துவதாக, ஆமென்.",
            scriptureRefEn = "Philippians 4:7 & Psalm 119:105",
            scriptureRefTa = "பிலிப்பியர் 4:7 & சங்கீதம் 119:105"
        ),
        DailyPrayer(
            dateString = "yesterday",
            titleEn = "A Prayer for Spiritual Strength & Protection",
            titleTa = "ஆவிக்குரிய பெலன் மற்றும் பாதுகாப்பிற்கான ஜெபம்",
            contentEn = "Almighty God, You are our shield, our high tower, and our ever-present help. We thank You for Your unfailing faithfulness. Encamp Your holy angels around our families and strengthen us to stand firm in faith. Amen.",
            contentTa = "சர்வவல்லமையுள்ள தேவனே, நீரே எங்கள் கேடகமும் எங்கள் அடைக்கலமுமாய் இருக்கிறீர். உமது உண்மைக்காக நன்றி. எங்கள் குடும்பங்களைச் சுற்றிலும் உமது தூதர்களை நிறுத்தி, விசுவாசத்தில் உறுதியாய் நிற்க பெலன் தாரும். ஆமென்.",
            scriptureRefEn = "Psalm 91:1-2",
            scriptureRefTa = "சங்கீதம் 91:1-2"
        )
    )

    val quizQuestions: List<QuizQuestion> get() = BibleQuizBank.allQuestions


    val readingPlans: List<ReadingPlan> = listOf(
        ReadingPlan(
            id = "plan_7_faith",
            titleEn = "7 Days: Foundations of Faith",
            titleTa = "7 நாட்கள்: விசுவாசத்தின் அஸ்திபாரம்",
            durationDays = 7,
            descriptionEn = "Discover how God builds unshakeable faith in everyday life.",
            descriptionTa = "அன்றாட வாழ்வில் அசைக்க முடியாத விசுவாசத்தை தேவன் எவ்வாறு உருவாக்குகிறார் என்பதை அறிந்து கொள்ளுங்கள்.",
            dailyPortions = listOf(
                PlanPortion(1, "The Call of Faith", "விசுவாசத்தின் அழைப்பு", "Genesis 12:1-4", "ஆதியாகமம் 12:1-4", "GEN", 12, "By faith Abraham obeyed...", "விசுவாசத்தினாலே ஆபிரகாம் கீழ்ப்படிந்து..."),
                PlanPortion(2, "Faith Defined", "விசுவாசம் என்றால் என்ன?", "Hebrews 11:1-6", "எபிரெயர் 11:1-6", "HEB", 11, "Now faith is the substance...", "விசுவாசமானது உறுதியாயிருக்கிறது..."),
                PlanPortion(3, "Walking by Faith", "விசுவாசத்தினால் நடப்பது", "2 Corinthians 5:1-10", "2 கொரிந்தியர் 5:1-10", "2CO", 5, "We walk by faith, not by sight", "பார்வையினாலே நடவாமல் விசுவாசத்தினாலே நடக்கிறோம்"),
                PlanPortion(4, "Shield of Faith", "விசுவாசத்தின் கேடகம்", "Ephesians 6:10-18", "எபேசியர் 6:10-18", "EPH", 6, "Above all, taking the shield of faith", "விசுவாசமென்னும் கேடகத்தைப் பிடித்துக்கொண்டவர்களாய்..."),
                PlanPortion(5, "Faith that Moves Mountains", "மலையை பெயர்க்கும் விசுவாசம்", "Matthew 17:14-21", "மத்தேயு 17:14-21", "MAT", 17, "If ye have faith as a grain of mustard seed...", "கடுகுவிதையளவு விசுவாசம் உங்களுக்கு இருந்தால்..."),
                PlanPortion(6, "Faith and Works", "விசுவாசமும் கிரியைகளும்", "James 2:14-26", "யாக்கோபு 2:14-26", "JAS", 2, "Faith without works is dead", "கிரியைகளில்லாத விசுவாசம் செத்ததாயிருக்கிறது"),
                PlanPortion(7, "The Victory of Faith", "விசுவாசத்தின் வெற்றி", "1 John 5:1-5", "1 யோவான் 5:1-5", "1JN", 5, "This is the victory that overcometh the world, even our faith", "உலகத்தை ஜெயிக்கிற ஜெயம் நம்முடைய விசுவாசமே")
            )
        ),
        ReadingPlan(
            id = "plan_14_peace",
            titleEn = "14 Days: Rest & Divine Peace",
            titleTa = "14 நாட்கள்: தெய்வீக சமாதானம்",
            durationDays = 14,
            descriptionEn = "Overcome fear and anxiety with the peace of Jesus Christ.",
            descriptionTa = "இயேசு கிறிஸ்துவின் சமாதானத்தினால் பயத்தையும் கவலையையும் ஜெயியுங்கள்.",
            dailyPortions = listOf(
                PlanPortion(1, "The Prince of Peace", "சமாதானப் பிரபு", "Isaiah 9:6-7", "ஏசாயா 9:6-7", "ISA", 9, "His name shall be called Prince of Peace", "அவர் நாமம் சமாதானப் பிரபு என்னப்படும்"),
                PlanPortion(2, "Peace in the Storm", "புயலில் சமாதானம்", "Mark 4:35-41", "மாற்கு 4:35-41", "MRK", 4, "Peace, be still", "அமைதலாயிரு, இரையாதே என்றார்")
            )
        )
    )
}
