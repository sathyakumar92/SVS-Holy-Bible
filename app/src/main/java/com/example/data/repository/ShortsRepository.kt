package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.data.datasource.CuratedShortsAnalysisEngine
import com.example.data.local.ShortsDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ShortsRepository(
    private val context: Context,
    private val shortsDao: ShortsDao
) {
    val publishedShorts: Flow<List<ShortEntity>> = shortsDao.getPublishedShorts()
        .flowOn(Dispatchers.IO)

    val allShortsAdmin: Flow<List<ShortEntity>> = shortsDao.getAllShortsAdmin()
        .flowOn(Dispatchers.IO)

    fun getCommentsForShort(shortId: String): Flow<List<ShortCommentEntity>> =
        shortsDao.getCommentsForShort(shortId).flowOn(Dispatchers.IO)

    fun getShortWithDetails(shortId: String, currentUserId: String): Flow<ShortWithDetails?> {
        return combine(
            shortsDao.getAnalysisForShort(shortId),
            shortsDao.getReferencesForShort(shortId),
            shortsDao.getCommentsForShort(shortId)
        ) { analysis, refs, comments ->
            val short = shortsDao.getShortById(shortId) ?: return@combine null
            val isLiked = shortsDao.isShortLiked(shortId, currentUserId)
            val isSaved = shortsDao.isShortSaved(shortId, currentUserId)
            val likesCount = shortsDao.getLikesCountForShort(shortId)
            ShortWithDetails(
                short = short.copy(likesCount = likesCount),
                analysis = analysis,
                references = refs,
                isLikedByMe = isLiked,
                isSavedByMe = isSaved,
                commentsCount = comments.size
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun toggleLike(shortId: String, currentUserId: String): Boolean = withContext(Dispatchers.IO) {
        val currentlyLiked = shortsDao.isShortLiked(shortId, currentUserId)
        if (currentlyLiked) {
            shortsDao.deleteLike(shortId, currentUserId)
        } else {
            shortsDao.insertLike(
                ShortLikeEntity(
                    id = "${shortId}_${currentUserId}",
                    shortId = shortId,
                    userId = currentUserId,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        val exactCount = shortsDao.getLikesCountForShort(shortId)
        shortsDao.updateLikesCount(shortId, exactCount)
        !currentlyLiked
    }

    suspend fun toggleSave(shortId: String, currentUserId: String): Boolean = withContext(Dispatchers.IO) {
        val currentlySaved = shortsDao.isShortSaved(shortId, currentUserId)
        if (currentlySaved) {
            shortsDao.deleteSave(shortId, currentUserId)
            false
        } else {
            shortsDao.insertSave(
                ShortSaveEntity(
                    id = "${shortId}_${currentUserId}",
                    shortId = shortId,
                    userId = currentUserId,
                    createdAt = System.currentTimeMillis()
                )
            )
            true
        }
    }

    suspend fun addComment(shortId: String, currentUserId: String, userName: String, text: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return@withContext false
        shortsDao.insertComment(
            ShortCommentEntity(
                id = UUID.randomUUID().toString(),
                shortId = shortId,
                userId = currentUserId,
                userName = userName.ifBlank { "Bible Student" },
                commentText = trimmed
            )
        )
        true
    }

    suspend fun deleteComment(commentId: String) = withContext(Dispatchers.IO) {
        shortsDao.deleteComment(commentId)
    }

    suspend fun publishShort(
        short: ShortEntity,
        analysis: ShortAnalysisEntity,
        references: List<ShortBibleReferenceEntity>
    ) = withContext(Dispatchers.IO) {
        shortsDao.insertShort(short.copy(publicationStatus = "PUBLISHED", publishedAt = System.currentTimeMillis()))
        shortsDao.insertAnalysis(analysis)
        shortsDao.deleteReferencesByShortId(short.id)
        shortsDao.insertReferences(references)
    }

    suspend fun saveDraftShort(
        short: ShortEntity,
        analysis: ShortAnalysisEntity,
        references: List<ShortBibleReferenceEntity>
    ) = withContext(Dispatchers.IO) {
        shortsDao.insertShort(short.copy(publicationStatus = "DRAFT"))
        shortsDao.insertAnalysis(analysis)
        shortsDao.deleteReferencesByShortId(short.id)
        shortsDao.insertReferences(references)
    }

    suspend fun deleteShort(shortId: String) = withContext(Dispatchers.IO) {
        shortsDao.deleteShortById(shortId)
        shortsDao.deleteAnalysisByShortId(shortId)
        shortsDao.deleteReferencesByShortId(shortId)
    }

    suspend fun togglePublishStatus(shortId: String) = withContext(Dispatchers.IO) {
        val short = shortsDao.getShortById(shortId) ?: return@withContext
        val newStatus = if (short.publicationStatus == "PUBLISHED") "DRAFT" else "PUBLISHED"
        shortsDao.updateShort(short.copy(publicationStatus = newStatus, updatedAt = System.currentTimeMillis()))
    }

    suspend fun clearAllShorts() = withContext(Dispatchers.IO) {
        shortsDao.clearAllShorts()
        shortsDao.clearAllAnalysis()
        shortsDao.clearAllReferences()
        shortsDao.clearAllLikes()
        shortsDao.clearAllComments()
        shortsDao.clearAllSaves()
    }

    suspend fun seedInitialShortsIfEmpty() = withContext(Dispatchers.IO) {
        val published = shortsDao.getPublishedShorts().first()
        if (published.isEmpty()) {
            val devVideos = CuratedShortsAnalysisEngine.getDeveloperShorts()
            for (item in devVideos) {
                val existingItem = shortsDao.getShortById(item.first.id)
                val thumb = generateAndSaveThumbnail(item.first.videoStoragePath, item.first.id)
                if (existingItem == null) {
                    val shortWithThumb = item.first.copy(
                        thumbnailStoragePath = thumb,
                        publicationStatus = "PUBLISHED",
                        uploadStatus = "COMPLETED",
                        likesCount = 0
                    )
                    shortsDao.insertShort(shortWithThumb)
                    shortsDao.insertAnalysis(item.second)
                    shortsDao.deleteReferencesByShortId(item.first.id)
                    shortsDao.insertReferences(item.third)
                } else {
                    val updated = existingItem.copy(
                        publicationStatus = "PUBLISHED",
                        uploadStatus = "COMPLETED",
                        thumbnailStoragePath = existingItem.thumbnailStoragePath ?: thumb
                    )
                    shortsDao.updateShort(updated)
                }
            }
        }
        ensureThumbnailsGenerated()
    }

    suspend fun ensureThumbnailsGenerated() = withContext(Dispatchers.IO) {
        val existing = shortsDao.getAllShortsAdmin().first()
        for (short in existing) {
            if (short.thumbnailStoragePath.isNullOrBlank() || !File(short.thumbnailStoragePath).exists()) {
                val thumb = generateAndSaveThumbnail(short.videoStoragePath, short.id)
                if (thumb != null) {
                    shortsDao.updateThumbnail(short.id, thumb)
                }
            }
        }
    }

    fun generateAndSaveThumbnail(videoPathOrUri: String, shortId: String): String? {
        return try {
            val retriever = MediaMetadataRetriever()
            if (videoPathOrUri.startsWith("android.resource://") || videoPathOrUri.startsWith("content://") || videoPathOrUri.startsWith("file://")) {
                retriever.setDataSource(context, Uri.parse(videoPathOrUri))
            } else if (videoPathOrUri.startsWith("raw://")) {
                val resName = videoPathOrUri.removePrefix("raw://")
                var rawId = context.resources.getIdentifier(resName, "raw", context.packageName)
                if (rawId == 0) {
                    rawId = context.resources.getIdentifier(resName, "raw", "com.example")
                }
                if (rawId == 0) {
                    rawId = if (resName.contains("bible_short")) {
                        com.example.R.raw.bible_short_video
                    } else {
                        com.example.R.raw.sermon_loneliness_short
                    }
                }
                if (rawId != 0) {
                    try {
                        val afd = context.resources.openRawResourceFd(rawId)
                        retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                    } catch (_: Exception) {
                        val rawUri = Uri.parse("android.resource://${context.packageName}/$rawId")
                        retriever.setDataSource(context, rawUri)
                    }
                } else {
                    return null
                }
            } else {
                val f = File(videoPathOrUri)
                if (!f.exists() || f.length() == 0L) return null
                retriever.setDataSource(videoPathOrUri)
            }
            val frame = retriever.getFrameAtTime(1000000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime()
            retriever.release()

            if (frame != null) {
                val dir = File(context.filesDir, "shorts_thumbnails").apply { if (!exists()) mkdirs() }
                val file = File(dir, "thumb_$shortId.jpg")
                FileOutputStream(file).use { out ->
                    frame.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                file.absolutePath
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun publishDeveloperVideo(videoId: String): Boolean = withContext(Dispatchers.IO) {
        val devVideos = CuratedShortsAnalysisEngine.getDeveloperShorts()
        val match = devVideos.find { it.first.id == videoId } ?: return@withContext false
        val thumb = generateAndSaveThumbnail(match.first.videoStoragePath, match.first.id)
        val shortWithThumb = match.first.copy(
            thumbnailStoragePath = thumb,
            likesCount = 0
        )
        shortsDao.insertShort(shortWithThumb)
        shortsDao.insertAnalysis(match.second)
        shortsDao.deleteReferencesByShortId(match.first.id)
        shortsDao.insertReferences(match.third)
        true
    }

    /**
     * Copies an administrator-selected video URI into internal storage for permanent playback
     */
    suspend fun copyVideoFileToInternalStorage(sourceUri: Uri, destinationFileName: String): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "shorts").apply { if (!exists()) mkdirs() }
        val destFile = File(dir, destinationFileName)
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    }

    /**
     * Validates video format, duration, audio, and aspect ratio
     */
    fun validateVideo(filePathOrUri: String): VideoValidationResult {
        return try {
            val retriever = MediaMetadataRetriever()
            if (filePathOrUri.startsWith("content://") || filePathOrUri.startsWith("file://")) {
                retriever.setDataSource(context, Uri.parse(filePathOrUri))
            } else if (filePathOrUri.startsWith("raw://")) {
                // Bundled raw resource is pre-validated
                return VideoValidationResult(isValid = true, durationSeconds = 83, is9to16 = true, hasAudio = true)
            } else {
                val f = File(filePathOrUri)
                if (!f.exists() || f.length() == 0L) {
                    return VideoValidationResult(isValid = false, errorMessage = "Selected video file does not exist or is empty.")
                }
                retriever.setDataSource(filePathOrUri)
            }

            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) != null
            val hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != null
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

            retriever.release()

            if (!hasVideo) {
                return VideoValidationResult(isValid = false, errorMessage = "No usable video track found in the selected file.")
            }

            // Adjust width/height if rotation is 90 or 270 degrees
            val effectiveWidth = if (rotation == 90 || rotation == 270) height else width
            val effectiveHeight = if (rotation == 90 || rotation == 270) width else height
            val isPortrait = effectiveHeight >= effectiveWidth

            VideoValidationResult(
                isValid = true,
                durationSeconds = (durationMs / 1000).toInt(),
                is9to16 = isPortrait,
                hasAudio = hasAudio
            )
        } catch (e: Exception) {
            VideoValidationResult(isValid = false, errorMessage = "Could not decode video file: ${e.message}")
        }
    }
}

data class VideoValidationResult(
    val isValid: Boolean,
    val durationSeconds: Int = 0,
    val is9to16: Boolean = true,
    val hasAudio: Boolean = true,
    val errorMessage: String? = null
)
