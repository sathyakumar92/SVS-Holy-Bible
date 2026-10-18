package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "svs_bible_settings")

data class UserPreferences(
    val themeMode: String = "dark",          // "dark", "light", "system"
    val interfaceLanguage: String = "en",    // "en", "ta"
    val bibleLanguage: String = "en",        // "en", "ta"
    val fontSizeSp: Int = 18,
    val lineSpacingMultiplier: Float = 1.4f,
    val showVerseNumbers: Boolean = true,
    val keepScreenAwake: Boolean = false,
    val audioVoiceGender: String = "male",   // "male", "female"
    val audioSpeed: Float = 1.0f,
    val autoPlayNextChapter: Boolean = true,
    val dailyVerseNotification: Boolean = true,
    val dailyPrayerNotification: Boolean = true,
    val dailyReadingGoalChapters: Int = 2,
    val hasCompletedWelcome: Boolean = false,
    val recentSearches: List<String> = emptyList()
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val INTERFACE_LANGUAGE = stringPreferencesKey("interface_language")
        val BIBLE_LANGUAGE = stringPreferencesKey("bible_language")
        val FONT_SIZE = intPreferencesKey("font_size")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val SHOW_VERSE_NUMBERS = booleanPreferencesKey("show_verse_numbers")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val AUDIO_VOICE_GENDER = stringPreferencesKey("audio_voice_gender")
        val AUDIO_SPEED = floatPreferencesKey("audio_speed")
        val AUTO_PLAY_NEXT = booleanPreferencesKey("auto_play_next")
        val DAILY_VERSE_NOTIF = booleanPreferencesKey("daily_verse_notif")
        val DAILY_PRAYER_NOTIF = booleanPreferencesKey("daily_prayer_notif")
        val DAILY_READING_GOAL = intPreferencesKey("daily_reading_goal")
        val HAS_COMPLETED_WELCOME = booleanPreferencesKey("has_completed_welcome")
        val RECENT_SEARCHES_CSV = stringPreferencesKey("recent_searches_csv")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val rawSearches = prefs[Keys.RECENT_SEARCHES_CSV] ?: ""
        val searchList = if (rawSearches.isNotBlank()) {
            rawSearches.split("|||").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
        UserPreferences(
            themeMode = prefs[Keys.THEME_MODE] ?: "dark",
            interfaceLanguage = prefs[Keys.INTERFACE_LANGUAGE] ?: "en",
            bibleLanguage = prefs[Keys.BIBLE_LANGUAGE] ?: "en",
            fontSizeSp = prefs[Keys.FONT_SIZE] ?: 18,
            lineSpacingMultiplier = prefs[Keys.LINE_SPACING] ?: 1.4f,
            showVerseNumbers = prefs[Keys.SHOW_VERSE_NUMBERS] ?: true,
            keepScreenAwake = prefs[Keys.KEEP_SCREEN_AWAKE] ?: false,
            audioVoiceGender = prefs[Keys.AUDIO_VOICE_GENDER] ?: "male",
            audioSpeed = prefs[Keys.AUDIO_SPEED] ?: 1.0f,
            autoPlayNextChapter = prefs[Keys.AUTO_PLAY_NEXT] ?: true,
            dailyVerseNotification = prefs[Keys.DAILY_VERSE_NOTIF] ?: true,
            dailyPrayerNotification = prefs[Keys.DAILY_PRAYER_NOTIF] ?: true,
            dailyReadingGoalChapters = prefs[Keys.DAILY_READING_GOAL] ?: 2,
            hasCompletedWelcome = prefs[Keys.HAS_COMPLETED_WELCOME] ?: false,
            recentSearches = searchList
        )
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setInterfaceLanguage(lang: String) {
        context.dataStore.edit { it[Keys.INTERFACE_LANGUAGE] = lang }
    }

    suspend fun setBibleLanguage(lang: String) {
        context.dataStore.edit { it[Keys.BIBLE_LANGUAGE] = lang }
    }

    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = size }
    }

    suspend fun setLineSpacing(multiplier: Float) {
        context.dataStore.edit { it[Keys.LINE_SPACING] = multiplier }
    }

    suspend fun setShowVerseNumbers(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_VERSE_NUMBERS] = show }
    }

    suspend fun setKeepScreenAwake(awake: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_AWAKE] = awake }
    }

    suspend fun setAudioVoiceGender(gender: String) {
        context.dataStore.edit { it[Keys.AUDIO_VOICE_GENDER] = gender }
    }

    suspend fun setAudioSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.AUDIO_SPEED] = speed }
    }

    suspend fun setAutoPlayNext(autoPlay: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_PLAY_NEXT] = autoPlay }
    }

    suspend fun setDailyVerseNotification(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_VERSE_NOTIF] = enabled }
    }

    suspend fun setDailyPrayerNotification(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_PRAYER_NOTIF] = enabled }
    }

    suspend fun setDailyReadingGoal(goal: Int) {
        context.dataStore.edit { it[Keys.DAILY_READING_GOAL] = goal.coerceAtLeast(1) }
    }

    suspend fun addRecentSearch(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.RECENT_SEARCHES_CSV] ?: ""
            val current = if (raw.isNotBlank()) raw.split("|||").filter { it.isNotBlank() } else emptyList()
            val updated = (listOf(clean) + current.filter { !it.equals(clean, ignoreCase = true) }).take(15)
            prefs[Keys.RECENT_SEARCHES_CSV] = updated.joinToString("|||")
        }
    }

    suspend fun removeRecentSearch(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.RECENT_SEARCHES_CSV] ?: ""
            if (raw.isNotBlank()) {
                val list = raw.split("|||").filter { it.isNotBlank() && !it.equals(clean, ignoreCase = true) }
                prefs[Keys.RECENT_SEARCHES_CSV] = list.joinToString("|||")
            }
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { prefs ->
            prefs[Keys.RECENT_SEARCHES_CSV] = ""
        }
    }

    suspend fun setHasCompletedWelcome(completed: Boolean) {
        context.dataStore.edit { it[Keys.HAS_COMPLETED_WELCOME] = completed }
    }
}
