package com.example.ui.screens.shorts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortCommentEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsCommentsSheet(
    shortId: String,
    shortTitle: String,
    comments: List<ShortCommentEntity>,
    isTamil: Boolean,
    onDismiss: () -> Unit,
    onSubmitComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit = {}
) {
    var commentInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<String?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM d • h:mm a", Locale.getDefault()) }

    // Confirmation dialog before deleting comment
    if (commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { commentToDelete = null },
            title = {
                Text(
                    text = if (isTamil) "கருத்தை நீக்கவா?" else "Delete Comment?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = if (isTamil) "இந்தக் கருத்தை நிரந்தரமாக நீக்க விரும்புகிறீர்களா?"
                    else "Are you sure you want to delete this comment? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = commentToDelete
                        commentToDelete = null
                        if (id != null) {
                            onDeleteComment(id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isTamil) "நீக்கு" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { commentToDelete = null }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SecondaryNavySurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RichGold) },
        modifier = Modifier.testTag("shorts_comments_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTamil) "கருத்துகள் (${comments.size})" else "Comments (${comments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )
                    Text(
                        text = shortTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryLightText.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_comments_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = PrimaryLightText)
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isTamil) "இன்னும் கருத்துகள் இல்லை." else "No comments yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryLightText.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTamil) "முதல் கருத்தை பதிவு செய்யுங்கள்!" else "Be the first to share your biblical reflection!",
                            style = MaterialTheme.typography.bodySmall,
                            color = RichGold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("comments_list"),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("comment_item_${comment.id}"),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryRoyalBlue,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = BrightGoldHighlight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = comment.userName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrightGoldHighlight
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dateFormat.format(Date(comment.createdAt)),
                                            fontSize = 10.sp,
                                            color = PrimaryLightText.copy(alpha = 0.6f)
                                        )

                                        // Allow deleting own comment
                                        if (comment.userId == "current_user" || comment.userId.isBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = { commentToDelete = comment.id },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete comment",
                                                    tint = Color.White.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = comment.commentText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PrimaryLightText,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Comment Input Field & Submission
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = {
                            if (it.length <= 500) commentInput = it
                        },
                        placeholder = {
                            Text(
                                text = if (isTamil) "வேத கருத்தைப் பகிருங்கள்..." else "Add a biblical reflection...",
                                fontSize = 13.sp,
                                color = PrimaryLightText.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RichGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            focusedTextColor = PrimaryLightText,
                            unfocusedTextColor = PrimaryLightText
                        ),
                        maxLines = 3,
                        enabled = !isSubmitting
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val trimmed = commentInput.trim()
                            if (trimmed.isNotBlank() && !isSubmitting) {
                                isSubmitting = true
                                onSubmitComment(trimmed)
                                commentInput = ""
                                isSubmitting = false
                            }
                        },
                        enabled = commentInput.isNotBlank() && !isSubmitting,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (commentInput.isNotBlank() && !isSubmitting) PrimaryRoyalBlue else ElevatedBlueSurface)
                            .testTag("submit_comment_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Post Comment",
                            tint = if (commentInput.isNotBlank() && !isSubmitting) BrightGoldHighlight else PrimaryLightText.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Character counter when typing
                if (commentInput.length > 300) {
                    Text(
                        text = "${commentInput.length}/500",
                        fontSize = 10.sp,
                        color = if (commentInput.length >= 500) MaterialTheme.colorScheme.error else PrimaryLightText.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp, end = 12.dp)
                    )
                }
            }
        }
    }
}
