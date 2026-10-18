package com.example.ui.screens.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.AppFeedback
import com.example.ui.theme.*
import com.example.ui.viewmodel.BibleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    viewModel: BibleViewModel
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val isTamil = prefs.interfaceLanguage == "ta"
    val feedbacks by viewModel.allFeedbacks.collectAsState()

    var rating by remember { mutableStateOf(5) }
    var selectedCategory by remember { mutableStateOf("General") }
    var userNameInput by remember { mutableStateOf("") }
    var userEmailInput by remember { mutableStateOf("") }
    var commentsInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "General" to if (isTamil) "பொதுவான கருத்து" else "General",
        "Feature Request" to if (isTamil) "புதிய அம்சம் தேவை" else "Feature Request",
        "Bug Report" to if (isTamil) "பிழை அறிக்கை" else "Bug Report",
        "Verse Correction" to if (isTamil) "வசன திருத்தம்" else "Verse Correction",
        "Praise" to if (isTamil) "நன்றி & ஆசீர்வாதம்" else "Praise & Encouragement"
    )

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("feedback_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RichGold.copy(alpha = 0.4f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PrimaryRoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = "Feedback",
                            tint = BrightGoldHighlight,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTamil) "நேரடி பயனர் கருத்து" else "Realtime User Feedback",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTamil)
                                "உங்கள் அனுபவத்தைப் பகிருங்கள்! பிழைகள், ஆலோசனைகள் அல்லது நல்வாழ்த்துகளை உடனுக்குடன் பதிவு செய்யலாம்."
                            else
                                "Your feedback shapes SVS Holy Bible. Share thoughts, report issues, or suggest improvements in real-time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryLightText
                        )
                    }
                }
            }
        }

        // 2. Feedback Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = if (isTamil) "உங்கள் மதிப்பீடு" else "Rate Your Experience",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5-Star Rating Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (star in 1..5) {
                            IconButton(
                                onClick = { rating = star },
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("feedback_star_$star")
                            ) {
                                Icon(
                                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star Stars",
                                    tint = if (star <= rating) RichGold else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val ratingLabel = when (rating) {
                            5 -> if (isTamil) "அற்புதம்! (5/5)" else "Excellent (5/5)"
                            4 -> if (isTamil) "மிக நன்று (4/5)" else "Very Good (4/5)"
                            3 -> if (isTamil) "நன்று (3/5)" else "Good (3/5)"
                            2 -> if (isTamil) "சுமாராக (2/5)" else "Fair (2/5)"
                            else -> if (isTamil) "மேம்படுத்த வேண்டும் (1/5)" else "Needs Improvement (1/5)"
                        }

                        Text(
                            text = ratingLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichGold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isTamil) "வகை தேர்வு செய்க" else "Feedback Category",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(3).forEach { (catKey, catLabel) ->
                            FilterChip(
                                selected = selectedCategory == catKey,
                                onClick = { selectedCategory = catKey },
                                label = { Text(catLabel, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryRoyalBlue.copy(alpha = 0.25f),
                                    selectedLabelColor = RichGold
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.drop(3).forEach { (catKey, catLabel) ->
                            FilterChip(
                                selected = selectedCategory == catKey,
                                onClick = { selectedCategory = catKey },
                                label = { Text(catLabel, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryRoyalBlue.copy(alpha = 0.25f),
                                    selectedLabelColor = RichGold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Name Field
                    OutlinedTextField(
                        value = userNameInput,
                        onValueChange = { userNameInput = it },
                        label = { Text(if (isTamil) "உங்கள் பெயர் (விருப்பமானது)" else "Your Name (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RichGold) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // User Email Field
                    OutlinedTextField(
                        value = userEmailInput,
                        onValueChange = { userEmailInput = it },
                        label = { Text(if (isTamil) "மின்னஞ்சல் (விருப்பமானது)" else "Email Address (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RichGold) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_email_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Comments Text Area
                    OutlinedTextField(
                        value = commentsInput,
                        onValueChange = { commentsInput = it },
                        label = { Text(if (isTamil) "உங்கள் கருத்து / விமர்சனம் *" else "Your Message & Feedback *") },
                        placeholder = {
                            Text(
                                if (isTamil)
                                    "பயன்பாட்டின் செயல்பாடு, ஆடியோ வாசிப்பு, வசனங்கள் அல்லது புதிய யோசனைகளை எழுதவும்..."
                                else
                                    "Tell us how the Bible app is working, report any verse or audio feedback, or suggest features..."
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("feedback_comments_input"),
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (commentsInput.trim().isNotEmpty()) {
                                isSubmitting = true
                                viewModel.submitUserFeedback(
                                    rating = rating,
                                    category = selectedCategory,
                                    name = userNameInput,
                                    email = userEmailInput,
                                    comments = commentsInput
                                ) {
                                    isSubmitting = false
                                    commentsInput = ""
                                    showSuccessDialog = true
                                }
                            }
                        },
                        enabled = commentsInput.trim().isNotEmpty() && !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("feedback_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRoyalBlue,
                            contentColor = BrightGoldHighlight
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = BrightGoldHighlight,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isTamil) "பதிவாகிறது..." else "Submitting Realtime...")
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "நேரடி கருத்தை சமர்ப்பிக்கவும்" else "Submit Realtime Feedback",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Realtime Feedback Feed / History
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = RichGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTamil) "சமீபத்திய நேரடி கருத்துகள்" else "Live Feedback Stream",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PrimaryRoyalBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${feedbacks.size} ${if (isTamil) "பதிவுகள்" else "Entries"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RichGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (feedbacks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkChatRead,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTamil)
                                "இன்னும் கருத்துகள் இல்லை. முதல் கருத்தை நீங்கள் வழங்கலாம்!"
                            else
                                "No feedbacks yet. Be the first to share your thoughts!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(feedbacks, key = { it.id }) { feedback ->
                FeedbackItemCard(
                    feedback = feedback,
                    dateFormat = dateFormat,
                    isTamil = isTamil,
                    onDelete = { viewModel.deleteUserFeedback(feedback) }
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTamil) "நன்றி!" else "Feedback Received!")
                }
            },
            text = {
                Text(
                    if (isTamil)
                        "உங்கள் நேரடி கருத்து வெற்றிகரமாக பெறப்பட்டு கணினியில் பதிவு செய்யப்பட்டுள்ளது. எஸ்.வி.எஸ் பரிசுத்த வேதாகம செயலியை மேலும் செம்மையாக்க இது பெரிதும் உதவும்."
                    else
                        "Your feedback has been recorded in real-time. Thank you for helping us improve SVS Holy Bible for users around the world!"
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue, contentColor = BrightGoldHighlight)
                ) {
                    Text(if (isTamil) "சரி" else "OK")
                }
            }
        )
    }
}

@Composable
fun FeedbackItemCard(
    feedback: AppFeedback,
    dateFormat: SimpleDateFormat,
    isTamil: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feedback_item_${feedback.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star rating display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= feedback.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= feedback.rating) RichGold else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryRoyalBlue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = feedback.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = RichGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = feedback.comments,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val author = if (feedback.userName.isNotEmpty()) feedback.userName else if (isTamil) "பயனர்" else "Anonymous Reader"
                Text(
                    text = "— $author",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = dateFormat.format(Date(feedback.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
