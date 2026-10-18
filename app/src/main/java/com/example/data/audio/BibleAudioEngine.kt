package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.model.BibleVerse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class AudioState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isInitializing: Boolean = true,
    val currentBookName: String = "",
    val currentChapter: Int = 1,
    val currentVerseNumber: Int = 1,
    val currentVerseIndex: Int = 0,
    val totalVerses: Int = 0,
    val speed: Float = 1.0f,
    val voiceGender: String = "male",
    val languageCode: String = "en",
    val selectedVoiceName: String? = null,
    val isVoiceSupported: Boolean = true,
    val statusMessage: String = "Ready to play",
    val error: String? = null
)

class BibleAudioEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingPlayRequest: (() -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _playbackState = MutableStateFlow(AudioState())
    val playbackState: StateFlow<AudioState> = _playbackState.asStateFlow()

    private var currentPlaylist: List<BibleVerse> = emptyList()
    private var currentIndex = 0
    private var currentSpeed = 1.0f
    private var currentVoiceGender = "male"
    private var currentLangCode = "en"

    @Volatile
    private var isExplicitlyPaused = false
    private var activeUtteranceGeneration = 0L

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        mainHandler.post {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    pause()
                }
            }
        }
    }

    init {
        initializeTts()
    }

    fun syncPreferences(languageCode: String, voiceGender: String, speed: Float) {
        currentLangCode = languageCode
        currentVoiceGender = voiceGender
        currentSpeed = speed.coerceIn(0.5f, 2.0f)
        _playbackState.value = _playbackState.value.copy(
            languageCode = languageCode,
            voiceGender = voiceGender,
            speed = currentSpeed
        )
        if (isTtsInitialized && tts != null) {
            applyLanguageAndVoice(languageCode, voiceGender)
            tts?.setSpeechRate(currentSpeed)
        }
    }

    private fun initializeTts() {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            _playbackState.value = _playbackState.value.copy(
                isInitializing = false,
                statusMessage = "Speech synthesizer error: ${e.message}",
                error = e.localizedMessage
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            setupUtteranceListener()
            applyLanguageAndVoice(currentLangCode, currentVoiceGender)
            tts?.setSpeechRate(currentSpeed)

            _playbackState.value = _playbackState.value.copy(
                isInitializing = false,
                statusMessage = "Audio engine ready",
                error = null
            )
            // Execute any pending play request queued before onInit completed
            mainHandler.post {
                if (!isExplicitlyPaused) {
                    pendingPlayRequest?.invoke()
                }
                pendingPlayRequest = null
            }
        } else {
            isTtsInitialized = false
            _playbackState.value = _playbackState.value.copy(
                isInitializing = false,
                statusMessage = "Speech engine initializing, tap play to retry",
                error = "TTS engine init error code $status"
            )
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    if (isExplicitlyPaused || utteranceId == null || !utteranceId.contains("gen_${activeUtteranceGeneration}_")) {
                        try {
                            tts?.stop()
                        } catch (_: Exception) {}
                        return@post
                    }
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = true,
                        isPaused = false
                    )
                }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    // Prevent queued or obsolete utterance callbacks from advancing if user paused or stopped
                    if (isExplicitlyPaused || !_playbackState.value.isPlaying) {
                        return@post
                    }
                    if (utteranceId != null && !utteranceId.contains("gen_${activeUtteranceGeneration}_")) {
                        return@post
                    }

                    if (currentIndex < currentPlaylist.size - 1) {
                        currentIndex++
                        playCurrentVerse()
                    } else {
                        abandonAudioFocus()
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = false,
                            isPaused = false,
                            statusMessage = if (currentLangCode == "ta") "அதிகார வாசிப்பு நிறைவடைந்தது" else "Chapter playback complete"
                        )
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                handleTtsError(utteranceId)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                handleTtsError(utteranceId)
            }
        })
    }

    private fun handleTtsError(utteranceId: String?) {
        mainHandler.post {
            if (isExplicitlyPaused || !_playbackState.value.isPlaying) {
                return@post
            }
            if (utteranceId != null && !utteranceId.contains("gen_${activeUtteranceGeneration}_")) {
                return@post
            }

            if (currentIndex < currentPlaylist.size - 1) {
                currentIndex++
                playCurrentVerse()
            } else {
                abandonAudioFocus()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    isPaused = false,
                    statusMessage = "Playback completed"
                )
            }
        }
    }

    fun playChapter(
        verses: List<BibleVerse>,
        startVerseIndex: Int = 0,
        languageCode: String,
        speed: Float = 1.0f,
        voiceGender: String = "male"
    ) {
        val distinctVerses = verses.distinctBy { it.verseNumber }.sortedBy { it.verseNumber }

        if (distinctVerses.isEmpty()) {
            _playbackState.value = _playbackState.value.copy(
                statusMessage = if (languageCode == "ta") "இந்த அதிகாரத்தில் வசனங்கள் இல்லை" else "No verses available in this chapter"
            )
            return
        }

        currentLangCode = languageCode
        currentVoiceGender = voiceGender
        currentSpeed = speed.coerceIn(0.5f, 2.0f)
        currentPlaylist = distinctVerses
        currentIndex = startVerseIndex.coerceIn(0, distinctVerses.size - 1)

        if (!isTtsInitialized || tts == null) {
            isExplicitlyPaused = false
            pendingPlayRequest = {
                playChapter(distinctVerses, startVerseIndex, languageCode, speed, voiceGender)
            }
            if (tts == null) {
                initializeTts()
            }
            _playbackState.value = _playbackState.value.copy(
                isInitializing = true,
                statusMessage = if (languageCode == "ta") "ஆடியோ குரல் தொடங்கப்படுகிறது..." else "Initializing audio speech engine..."
            )
            return
        }

        isExplicitlyPaused = false
        activeUtteranceGeneration++
        requestAudioFocus()

        applyLanguageAndVoice(languageCode, voiceGender)
        tts?.setSpeechRate(currentSpeed)

        playCurrentVerse()
    }

    private fun applyLanguageAndVoice(languageCode: String, voiceGender: String) {
        val isTamil = languageCode == "ta"
        val targetLocale = if (isTamil) Locale("ta", "IN") else Locale.US

        var isSupported = true
        var langResult = tts?.setLanguage(targetLocale)

        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            if (isTamil) {
                val genericTa = Locale("ta")
                val fallbackResult = tts?.setLanguage(genericTa)
                if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    isSupported = false
                    _playbackState.value = _playbackState.value.copy(
                        isVoiceSupported = false,
                        statusMessage = "Tamil TTS voice pack not installed on this device. Install Tamil in Android Settings."
                    )
                } else {
                    isSupported = true
                }
            } else {
                tts?.setLanguage(Locale.US)
            }
        }

        // Apply distinct pitch modulation: Deeper masculine tone for Male, higher feminine tone for Female
        if (voiceGender == "female") {
            tts?.setPitch(1.22f)
        } else {
            tts?.setPitch(0.80f)
        }

        var chosenVoiceName: String? = null

        // Query available voices and select matching gender voice
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val allVoices = tts?.voices
                if (!allVoices.isNullOrEmpty()) {
                    val targetLang = if (isTamil) "ta" else "en"
                    val langVoices = allVoices.filter {
                        it.locale.language.equals(targetLang, ignoreCase = true)
                    }

                    if (langVoices.isNotEmpty()) {
                        val genderVoice = if (voiceGender == "female") {
                            langVoices.firstOrNull { voice ->
                                val name = voice.name.lowercase()
                                name.contains("female") || name.contains("fem") || name.contains("woman") ||
                                        name.contains("girl") || name.contains("-f-") || name.contains("_f_") ||
                                        name.contains("#female") || name.contains("taf") || name.contains("sfg") ||
                                        name.contains("sfd")
                            } ?: langVoices.firstOrNull { voice ->
                                val feats = voice.features
                                feats?.contains("gender=female") == true
                            } ?: if (langVoices.size > 1) langVoices.first() else null
                        } else {
                            langVoices.firstOrNull { voice ->
                                val name = voice.name.lowercase()
                                (name.contains("male") && !name.contains("female")) ||
                                        name.contains("man") || name.contains("boy") ||
                                        name.contains("-m-") || name.contains("_m_") ||
                                        name.contains("#male") || name.contains("tag") ||
                                        name.contains("tpd") || name.contains("iom") ||
                                        name.contains("iob")
                            } ?: langVoices.firstOrNull { voice ->
                                val feats = voice.features
                                feats?.contains("gender=male") == true
                            } ?: langVoices.firstOrNull { voice ->
                                val name = voice.name.lowercase()
                                !name.contains("female") && !name.contains("fem") && !name.contains("taf") && !name.contains("sfg")
                            } ?: if (langVoices.size > 1) langVoices.last() else null
                        }

                        if (genderVoice != null) {
                            tts?.voice = genderVoice
                            chosenVoiceName = genderVoice.name
                        }
                    }
                }
            } catch (_: Exception) {
                // Pitch variation remains active
            }
        }

        if (chosenVoiceName == null) {
            chosenVoiceName = if (voiceGender == "male") "Masculine Voice (${if (isTamil) "தமிழ் ஆண்" else "Male Tone"})" else "Feminine Voice (${if (isTamil) "தமிழ் பெண்" else "Female Tone"})"
        }

        _playbackState.value = _playbackState.value.copy(
            languageCode = languageCode,
            voiceGender = voiceGender,
            selectedVoiceName = chosenVoiceName,
            isVoiceSupported = isSupported
        )
    }

    private fun playCurrentVerse() {
        if (isExplicitlyPaused) return
        if (currentIndex !in currentPlaylist.indices) return
        val verse = currentPlaylist[currentIndex]

        _playbackState.value = _playbackState.value.copy(
            isPlaying = true,
            isPaused = false,
            currentBookName = verse.bookName,
            currentChapter = verse.chapterNumber,
            currentVerseNumber = verse.verseNumber,
            currentVerseIndex = currentIndex,
            totalVerses = currentPlaylist.size,
            speed = currentSpeed,
            voiceGender = currentVoiceGender,
            languageCode = currentLangCode,
            statusMessage = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}"
        )

        // Always re-assert pitch, speech rate, and language right before speaking to prevent TTS resetting
        applyLanguageAndVoice(currentLangCode, currentVoiceGender)
        tts?.setSpeechRate(currentSpeed.coerceIn(0.5f, 2.0f))

        val gen = activeUtteranceGeneration
        val utteranceId = "gen_${gen}_verse_${verse.bookId}_${verse.chapterNumber}_${verse.verseNumber}_${System.currentTimeMillis()}"

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

        // Queue flush ensures no stale speech plays
        val result = tts?.speak(verse.verseText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                statusMessage = if (currentLangCode == "ta") "ஆடியோ தயாராக உள்ளது. இயக்க அழுத்தவும்." else "Audio player ready. Tap play to listen."
            )
        }
    }

    private fun requestAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener, mainHandler)
                    .build()
                audioFocusRequest = request
                audioManager?.requestAudioFocus(request)
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }
        } catch (_: Exception) {
            // Non-critical audio focus fallback
        }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    fun pause() {
        // Immediate cancellation without delay: update UI state and stop speech instantly
        isExplicitlyPaused = true
        pendingPlayRequest = null
        activeUtteranceGeneration++
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isPaused = true,
            statusMessage = if (currentLangCode == "ta") "இடைநிறுத்தப்பட்டது" else "Paused"
        )
        try {
            tts?.stop()
        } catch (_: Exception) {}
        abandonAudioFocus()
    }

    fun resume() {
        if (_playbackState.value.isPaused) {
            isExplicitlyPaused = false
            activeUtteranceGeneration++
            requestAudioFocus()
            applyLanguageAndVoice(currentLangCode, currentVoiceGender)
            tts?.setSpeechRate(currentSpeed.coerceIn(0.5f, 2.0f))
            playCurrentVerse()
        }
    }

    fun stop() {
        isExplicitlyPaused = true
        pendingPlayRequest = null
        activeUtteranceGeneration++
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isPaused = false,
            statusMessage = if (currentLangCode == "ta") "நிறுத்தப்பட்டது" else "Stopped"
        )
        try {
            tts?.stop()
        } catch (_: Exception) {}
        abandonAudioFocus()
    }

    fun skipNext() {
        if (currentIndex < currentPlaylist.size - 1) {
            currentIndex++
            isExplicitlyPaused = false
            activeUtteranceGeneration++
            playCurrentVerse()
        }
    }

    fun skipPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            isExplicitlyPaused = false
            activeUtteranceGeneration++
            playCurrentVerse()
        }
    }

    fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(currentSpeed)
        _playbackState.value = _playbackState.value.copy(speed = currentSpeed)
    }

    fun setVoiceGender(gender: String) {
        currentVoiceGender = gender
        applyLanguageAndVoice(currentLangCode, gender)
        _playbackState.value = _playbackState.value.copy(voiceGender = gender)
        if (_playbackState.value.isPlaying) {
            activeUtteranceGeneration++
            try {
                tts?.stop()
            } catch (_: Exception) {}
            playCurrentVerse()
        }
    }

    fun setLanguage(languageCode: String, newVerses: List<BibleVerse>? = null) {
        currentLangCode = languageCode
        if (newVerses != null && newVerses.isNotEmpty()) {
            currentPlaylist = newVerses.distinctBy { it.verseNumber }.sortedBy { it.verseNumber }
            currentIndex = currentIndex.coerceIn(0, currentPlaylist.size - 1)
        }
        applyLanguageAndVoice(languageCode, currentVoiceGender)
        _playbackState.value = _playbackState.value.copy(languageCode = languageCode)
        if (_playbackState.value.isPlaying) {
            activeUtteranceGeneration++
            try {
                tts?.stop()
            } catch (_: Exception) {}
            playCurrentVerse()
        } else if (_playbackState.value.isPaused && currentPlaylist.isNotEmpty()) {
            val v = currentPlaylist.getOrNull(currentIndex)
            if (v != null) {
                _playbackState.value = _playbackState.value.copy(
                    currentBookName = v.bookName,
                    currentChapter = v.chapterNumber,
                    currentVerseNumber = v.verseNumber,
                    statusMessage = "${v.bookName} ${v.chapterNumber}:${v.verseNumber}"
                )
            }
        }
    }

    fun release() {
        isExplicitlyPaused = true
        pendingPlayRequest = null
        activeUtteranceGeneration++
        abandonAudioFocus()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isTtsInitialized = false
    }
}
