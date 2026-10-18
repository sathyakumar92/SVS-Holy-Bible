package com.example.ui.screens.topics

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CanonicalBibleData
import com.example.data.model.BibleBook
import com.example.data.model.RichTopicStudy
import com.example.data.model.TopicAiMessage
import com.example.data.model.TopicReferenceEntity
import com.example.ui.theme.*
import com.example.util.TopicAssistantEngine

/**
 * AI-Powered Bible Topic Assistant Tab (Requirement 2.1):
 * - Understands current topic context for follow-up questions
 * - Quick suggested question chips
 * - Grounded Scripture citations with direct link to Bible reader
 * - Offline/Online indicator and clear AI commentary disclosure
 */
@Composable
fun TopicAiAssistantTab(
    study: RichTopicStudy,
    messages: List<TopicAiMessage>,
    isLoading: Boolean,
    isTamil: Boolean,
    onAskQuestion: (String) -> Unit,
    onClearChat: () -> Unit,
    onOpenReader: (BibleBook, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val topic = study.topic

    val suggestedQuestions = remember(isTamil) {
        if (isTamil) TopicAssistantEngine.defaultSuggestedQuestionsTa else TopicAssistantEngine.defaultSuggestedQuestionsEn
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("topic_ai_assistant_tab")
    ) {
        // Conversation List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Assistant Topic Context Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTamil) "வேத ஆய்வு உதவியாளர்" else "AI Bible Topic Assistant",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGoldHighlight
                                )
                            }

                            if (messages.isNotEmpty()) {
                                TextButton(onClick = onClearChat) {
                                    Text(if (isTamil) "அழி" else "Reset", color = RichGold, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isTamil)
                                "தற்போதைய ஆய்வு தலைப்பு: ${topic.titleTa} (${topic.titleEn}). உங்கள் கேள்விகள் இந்த தலைப்பின் வேத சத்தியங்களை அடிப்படையாகக் கொண்டு பதிலளிக்கப்படும்."
                            else
                                "Current Study Topic: ${topic.titleEn} (${topic.titleTa}). Questions are grounded directly in this topic's verified Scripture data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryLightText,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryRoyalBlue.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (isTamil)
                                    "⚠️ இது வேத ஆராய்ச்சிக்கான உதவியாளர். விளக்கங்கள் வேத வசனங்களோடு ஒப்பிட்டுப் பார்க்கப்பட வேண்டும்."
                                else
                                    "✨ AI-assisted study commentary. Always compare all explanations with the inspired Scripture.",
                                fontSize = 10.sp,
                                color = BrightGoldHighlight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Initial Starter Greeting if no messages yet
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isTamil) "பரிந்துரைக்கப்பட்ட கேள்விகள்:" else "Suggested Study Questions:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichGold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        suggestedQuestions.forEach { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        onAskQuestion(suggestion)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = RichGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Message History
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isUser) 14.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 14.dp
                        ),
                        color = if (isUser) PrimaryRoyalBlue else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (isUser) 0.dp else 2.dp,
                        border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth(if (isUser) 0.85f else 0.95f)
                            .testTag(if (isUser) "user_message_${msg.id}" else "assistant_message_${msg.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Assistant Header Label
                            if (!isUser) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RichGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTamil) "வேத ஆய்வு விளக்கம்" else "Biblical Study Commentary",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RichGold
                                    )
                                    if (msg.isOfflineGrounded) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(Verified Scripture Mode)",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Message Body
                            val displayText = if (isTamil && msg.contentTa.isNotBlank()) msg.contentTa else msg.contentEn
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) BrightGoldHighlight else MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )

                            // Cited Scripture Passages with Tap-to-read
                            if (msg.citedReferences.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (isTamil) "குறிப்பிடப்பட்ட வேத வசனங்கள்:" else "Cited Scripture References:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RichGold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                msg.citedReferences.forEach { ref ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SecondaryNavySurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable {
                                                val book = CanonicalBibleData.findBookById(ref.bookId) ?: CanonicalBibleData.allBooks[0]
                                                onOpenReader(book, ref.chapterNumber, ref.verseStart)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = BrightGoldHighlight, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isTamil) ref.referenceTa else ref.referenceEn,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BrightGoldHighlight
                                                )
                                                Text(
                                                    text = if (isTamil) ref.verseTextTa else ref.verseTextEn,
                                                    fontSize = 10.sp,
                                                    color = PrimaryLightText,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = if (isTamil) "வாசிக்க" else "Read",
                                                fontSize = 10.sp,
                                                color = RichGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Loading Indicator
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = RichGold,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isTamil) "வேத சத்தியங்களிலிருந்து ஆராய்கிறது..." else "Searching Scriptures & analyzing topic...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bottom Input Field Bar
        Surface(
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (isTamil) "${topic.titleTa} பற்றி கேளுங்கள்..." else "Ask about ${topic.titleEn}...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_topic_question_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RichGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val q = inputText.trim()
                        if (q.isNotBlank()) {
                            onAskQuestion(q)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (inputText.isNotBlank() && !isLoading) PrimaryRoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                            androidx.compose.foundation.shape.CircleShape
                        )
                        .testTag("send_ai_question_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isLoading) BrightGoldHighlight else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
