package com.example.ui.screens.shorts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.model.ShortCommentEntity
import com.example.data.model.ShortEntity
import com.example.data.model.ShortWithDetails
import com.example.ui.theme.*
import java.io.File

/**
 * Fully Functional Full-Screen Shorts Video Player:
 * - Heart Like Button: Instant optimistic update, filled/unfilled heart, persistent Room DB storage, rapid-tap debounce
 * - Share Button: Native Android share sheet, deep link, clipboard copy with toast feedback
 * - Comment Button: Dedicated bottom sheet, user comment submission & deletion, instant count update
 * - Save / Bookmark Button: Persistent bookmarking in Room database
 * - More Options: Report dialog, copy link, and share
 * - Scripture Analysis ("Bible" button): Seamless navigation to verified biblical citations
 * - Playback: Tap-to-pause/resume, auto-pause on swipe, buffering & error handling
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortsPlayerScreen(
    shortDetails: ShortWithDetails?,
    comments: List<ShortCommentEntity>,
    allShorts: List<ShortEntity> = emptyList(),
    isTamil: Boolean,
    onBack: () -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onOpenBibleAnalysis: (String) -> Unit,
    onAddComment: (String, String) -> Unit,
    onDeleteComment: (String) -> Unit = {},
    onReportShort: (String, String) -> Unit = { _, _ -> },
    onSelectShort: (ShortEntity) -> Unit = {}
) {
    BackHandler {
        onBack()
    }

    if (shortDetails == null && allShorts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RichGold)
        }
        return
    }

    val shortsList = if (allShorts.isNotEmpty()) allShorts else listOfNotNull(shortDetails?.short)
    val initialIndex = remember(shortDetails?.short?.id) {
        val targetId = shortDetails?.short?.id
        val idx = shortsList.indexOfFirst { it.id == targetId }
        if (idx >= 0) idx else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { shortsList.size }
    )

    LaunchedEffect(shortDetails?.short?.id) {
        val targetId = shortDetails?.short?.id
        val idx = shortsList.indexOfFirst { it.id == targetId }
        if (idx >= 0 && idx != pagerState.currentPage) {
            pagerState.scrollToPage(idx)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val selected = shortsList.getOrNull(pagerState.currentPage)
        if (selected != null && selected.id != shortDetails?.short?.id) {
            onSelectShort(selected)
        }
    }

    var isMuted by remember { mutableStateOf(false) }
    var activeCommentsShortId by remember { mutableStateOf<String?>(null) }

    // Comment Sheet
    if (activeCommentsShortId != null) {
        val currentShort = shortsList.find { it.id == activeCommentsShortId } ?: shortDetails?.short
        ShortsCommentsSheet(
            shortId = activeCommentsShortId!!,
            shortTitle = currentShort?.title ?: "",
            comments = comments,
            isTamil = isTamil,
            onDismiss = { activeCommentsShortId = null },
            onSubmitComment = { text ->
                onAddComment(activeCommentsShortId!!, text)
            },
            onDeleteComment = onDeleteComment
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_player_screen")
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> shortsList[index].id }
        ) { pageIndex ->
            val pageShort = shortsList[pageIndex]
            val isCurrent = pagerState.currentPage == pageIndex
            val isDetailsMatching = shortDetails?.short?.id == pageShort.id
            val isLiked = if (isDetailsMatching) shortDetails?.isLikedByMe == true else false
            val isSaved = if (isDetailsMatching) shortDetails?.isSavedByMe == true else false
            val likesCount = if (isDetailsMatching) (shortDetails?.short?.likesCount ?: pageShort.likesCount) else pageShort.likesCount
            val commentsCount = if (isDetailsMatching) comments.size else 0

            ShortPlayerPage(
                short = pageShort,
                isCurrentPage = isCurrent,
                isMuted = isMuted,
                isLiked = isLiked,
                isSaved = isSaved,
                likesCount = likesCount,
                commentsCount = commentsCount,
                isTamil = isTamil,
                onToggleMute = { isMuted = !isMuted },
                onBack = onBack,
                onToggleLike = { onToggleLike(pageShort.id) },
                onToggleSave = { onToggleSave(pageShort.id) },
                onOpenBible = { onOpenBibleAnalysis(pageShort.id) },
                onOpenComments = { activeCommentsShortId = pageShort.id },
                onReportShort = { reason -> onReportShort(pageShort.id, reason) }
            )
        }
    }
}

@Composable
private fun ShortPlayerPage(
    short: ShortEntity,
    isCurrentPage: Boolean,
    isMuted: Boolean,
    isLiked: Boolean,
    isSaved: Boolean,
    likesCount: Int,
    commentsCount: Int,
    isTamil: Boolean,
    onToggleMute: () -> Unit,
    onBack: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onOpenBible: () -> Unit,
    onOpenComments: () -> Unit,
    onReportShort: (String) -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isVideoReady by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Optimistic local state for immediate heart like response
    var localIsLiked by remember(isLiked) { mutableStateOf(isLiked) }
    var localLikesCount by remember(likesCount) { mutableIntStateOf(likesCount) }
    var lastLikeClickTime by remember { mutableLongStateOf(0L) }

    // Optimistic local state for save / bookmark
    var localIsSaved by remember(isSaved) { mutableStateOf(isSaved) }

    // More options menu & report dialog
    var showMoreMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Report Dialog
    if (showReportDialog) {
        val reportReasons = listOf(
            if (isTamil) "பொருத்தமற்ற உள்ளடக்கம்" else "Inappropriate content",
            if (isTamil) "வேத தவறான விளக்கம்" else "Scripture inaccuracy",
            if (isTamil) "ஒலி/ஒளி தரக் குறைபாடு" else "Audio/Video quality issue",
            if (isTamil) "மற்றவை" else "Other"
        )
        var selectedReason by remember { mutableStateOf(reportReasons[0]) }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = if (isTamil) "வீடியோவைப் புகாரளி" else "Report Video",
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isTamil) "புகாரளிப்பதற்கான காரணத்தைத் தேர்ந்தெடுக்கவும்:" else "Select a reason for reporting:",
                        fontSize = 13.sp,
                        color = PrimaryLightText.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    reportReasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = RichGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = reason, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        onReportShort(selectedReason)
                        Toast.makeText(
                            context,
                            if (isTamil) "புகார் சமர்ப்பிக்கப்பட்டது. நன்றி!" else "Report submitted for review. Thank you!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue)
                ) {
                    Text(if (isTamil) "சமர்ப்பி" else "Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }

    // When page is active, ensure playback; when scrolled away, pause and reset
    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage) {
            mediaPlayerRef?.pause()
            isPlaying = false
        } else {
            mediaPlayerRef?.let { mp ->
                if (!mp.isPlaying) {
                    mp.start()
                    isPlaying = true
                }
            }
        }
    }

    // Apply mute updates
    LaunchedEffect(isMuted) {
        mediaPlayerRef?.let { mp ->
            val vol = if (isMuted) 0f else 1f
            mp.setVolume(vol, vol)
        }
    }

    DisposableEffect(short.id) {
        onDispose {
            try {
                mediaPlayerRef?.let { mp ->
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                    mp.reset()
                    mp.release()
                }
            } catch (_: Exception) {}
            mediaPlayerRef = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Strict 9:16 vertical video container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
                .align(Alignment.Center)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    mediaPlayerRef?.let { mp ->
                        if (mp.isPlaying) {
                            mp.pause()
                            isPlaying = false
                        } else {
                            mp.start()
                            isPlaying = true
                        }
                    }
                }
        ) {
            // Poster thumbnail shown immediately to eliminate blank screen
            if (!short.thumbnailStoragePath.isNullOrBlank() && File(short.thumbnailStoragePath).exists()) {
                AsyncImage(
                    model = File(short.thumbnailStoragePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    PrimaryRoyalBlue.copy(alpha = 0.85f),
                                    ElevatedBlueSurface,
                                    DeepNavyBackground
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        tint = BrightGoldHighlight.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            // AndroidView Video Surface with TextureView
            if (isCurrentPage) {
                AndroidView(
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                                    try {
                                        val mp = MediaPlayer().apply {
                                            setAudioAttributes(
                                                AudioAttributes.Builder()
                                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                                    .build()
                                            )
                                            setSurface(Surface(st))
                                            isLooping = true

                                            val rawResId = short.videoRawResId ?: if (short.videoStoragePath.startsWith("raw://")) {
                                                val resName = short.videoStoragePath.removePrefix("raw://")
                                                val id1 = ctx.resources.getIdentifier(resName, "raw", ctx.packageName)
                                                if (id1 != 0) id1 else {
                                                    val id2 = ctx.resources.getIdentifier(resName, "raw", "com.example")
                                                    if (id2 != 0) id2 else {
                                                        if (resName.contains("bible_short")) com.example.R.raw.bible_short_video else com.example.R.raw.sermon_loneliness_short
                                                    }
                                                }
                                            } else null

                                            if (rawResId != null) {
                                                try {
                                                    val afd = ctx.resources.openRawResourceFd(rawResId)
                                                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                                    afd.close()
                                                } catch (_: Exception) {
                                                    val rawUri = Uri.parse("android.resource://${ctx.packageName}/$rawResId")
                                                    setDataSource(ctx, rawUri)
                                                }
                                            } else if (short.videoStoragePath.startsWith("content://") || short.videoStoragePath.startsWith("file://")) {
                                                setDataSource(ctx, Uri.parse(short.videoStoragePath))
                                            } else {
                                                setDataSource(short.videoStoragePath)
                                            }

                                            setOnPreparedListener { preparedMp ->
                                                isVideoReady = true
                                                hasError = false
                                                val vol = if (isMuted) 0f else 1f
                                                preparedMp.setVolume(vol, vol)
                                                if (isPlaying && isCurrentPage) {
                                                    preparedMp.start()
                                                }
                                            }

                                            setOnErrorListener { _, what, extra ->
                                                hasError = true
                                                errorMessage = "Playback issue ($what, $extra)"
                                                false
                                            }

                                            prepareAsync()
                                        }
                                        mediaPlayerRef = mp
                                    } catch (e: Exception) {
                                        hasError = true
                                        errorMessage = e.message ?: "Failed to initialize playback"
                                    }
                                }

                                override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
                                override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                                    mediaPlayerRef?.setSurface(null)
                                    return true
                                }
                                override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Buffering progress
            if (!isVideoReady && !hasError && isCurrentPage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RichGold, modifier = Modifier.size(36.dp))
                }
            }

            // Error message overlay
            if (hasError && isCurrentPage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTamil) "வீடியோவை இயக்குவதில் சிக்கல்" else "Unable to Play Video",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Play/Pause indicator overlay
            if (isVideoReady && !isPlaying) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = BrightGoldHighlight, modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }

        // Top Gradient & Header (Back button + Category Badge + Mute toggle + More menu)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .statusBarsPadding()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        try {
                            mediaPlayerRef?.stop()
                            mediaPlayerRef?.release()
                            mediaPlayerRef = null
                        } catch (_: Exception) {}
                        onBack()
                    },
                    modifier = Modifier.testTag("player_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrightGoldHighlight
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrimaryRoyalBlue.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Text(
                            text = short.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrightGoldHighlight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = short.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                // Mute / Unmute Button
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.testTag("player_mute_toggle")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = BrightGoldHighlight
                    )
                }

                // More Options Menu (Copy Link, Share, Report)
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.testTag("player_more_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = BrightGoldHighlight
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isTamil) "இணைப்பை நகலெடு" else "Copy Video Link") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Short Link", "https://svsholybible.app/shorts/${short.id}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(
                                    context,
                                    if (isTamil) "இணைப்பு நகலெடுக்கப்பட்டது!" else "Link copied to clipboard!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isTamil) "பகிர்..." else "Share Video...") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                val shareText = "✝ ${short.title}\n\n" +
                                    (if (short.description.isNotBlank()) short.description + "\n\n" else "") +
                                    "Watch Shorts with verified Scripture Analysis on SVS Holy Bible App:\nhttps://svsholybible.app/shorts/${short.id}"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    putExtra(Intent.EXTRA_TITLE, short.title)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Short"))
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(if (isTamil) "வீடியோவைப் புகாரளி" else "Report Video", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMoreMenu = false
                                showReportDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Bottom Details & Social Interactions Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding()
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Exact Title, Description, and Audio Indicator
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = short.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight,
                    maxLines = 3
                )

                if (short.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = short.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTamil) "அசல் பிரசங்க ஆடியோ (Original Audio)" else "Original Sermon Audio",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = RichGold
                    )
                }
            }

            // Right Column: Exactly 5 Social Interaction Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. HEART LIKE BUTTON (Instant optimistic UI, debounced, filled heart)
                PlayerActionButton(
                    icon = if (localIsLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = "$localLikesCount",
                    tint = if (localIsLiked) Color(0xFFFF2D55) else Color.White,
                    testTag = "player_like_button",
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (now - lastLikeClickTime > 250L) {
                            lastLikeClickTime = now
                            // Instant optimistic update
                            if (localIsLiked) {
                                localIsLiked = false
                                localLikesCount = (localLikesCount - 1).coerceAtLeast(0)
                            } else {
                                localIsLiked = true
                                localLikesCount += 1
                            }
                            onToggleLike()
                        }
                    }
                )

                // 2. COMMENT BUTTON (Opens interactive comments bottom sheet)
                PlayerActionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = "$commentsCount",
                    tint = Color.White,
                    testTag = "player_comment_button",
                    onClick = onOpenComments
                )

                // 3. BIBLE BUTTON (Scripture Analysis & verified passage cross-references)
                PlayerActionButton(
                    icon = Icons.Default.AutoStories,
                    label = if (isTamil) "வேதம்" else "Bible",
                    tint = BrightGoldHighlight,
                    containerColor = PrimaryRoyalBlue,
                    testTag = "player_bible_analysis_button",
                    onClick = onOpenBible
                )

                // 4. SHARE BUTTON (Native Android Share Sheet + link copy)
                PlayerActionButton(
                    icon = Icons.Outlined.Share,
                    label = if (isTamil) "பகிர்" else "Share",
                    tint = Color.White,
                    testTag = "player_share_button",
                    onClick = {
                        val shareText = "✝ ${short.title}\n\n" +
                            (if (short.description.isNotBlank()) short.description + "\n\n" else "") +
                            "Watch Shorts with verified Scripture Analysis on SVS Holy Bible App:\nhttps://svsholybible.app/shorts/${short.id}"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            putExtra(Intent.EXTRA_TITLE, short.title)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Short"))
                    }
                )

                // 5. SAVE BUTTON (Bookmark persistence in Room database)
                PlayerActionButton(
                    icon = if (localIsSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = if (localIsSaved) (if (isTamil) "சேமிக்கப்பட்டது" else "Saved") else (if (isTamil) "சேமி" else "Save"),
                    tint = if (localIsSaved) BrightGoldHighlight else Color.White,
                    testTag = "player_save_button",
                    onClick = {
                        localIsSaved = !localIsSaved
                        onToggleSave()
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    containerColor: Color = Color.Black.copy(alpha = 0.45f),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            modifier = Modifier
                .size(46.dp)
                .testTag(testTag)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
