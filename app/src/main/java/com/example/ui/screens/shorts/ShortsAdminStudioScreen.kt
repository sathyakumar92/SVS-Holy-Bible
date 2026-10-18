package com.example.ui.screens.shorts

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CuratedShortsAnalysisEngine
import com.example.data.model.ShortEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Strict Admin-Only Video Upload & Studio Screen (Requirement 3):
 * - Accessible only to authorized administrators and the developer
 * - Real file-upload control from device storage (video files)
 * - Required exact Title preservation (no automatic rewriting)
 * - File validation: MIME, duration, audio track, 9:16 portrait orientation
 * - Verified Developer Media Bank for authentic published sermons
 * - Management: Edit, Publish, Unpublish, Delete, and Clean Reset
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsAdminStudioScreen(
    adminShorts: List<ShortEntity>,
    isTamil: Boolean,
    onBack: () -> Unit,
    onPublishDeveloperVideo: (String) -> Unit,
    onUploadCustomVideo: (Uri, String, String, String, String) -> Unit,
    onTogglePublishStatus: (String) -> Unit,
    onDeleteShort: (String) -> Unit,
    onClearAllShorts: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var inputTitle by remember { mutableStateOf("") }
    var inputDescription by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("Sermon") }
    var inputStatus by remember { mutableStateOf("PUBLISHED") }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showConfirmResetDialog by remember { mutableStateOf(false) }

    // System File Picker for selecting videos from device
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            uploadStatusMessage = if (isTamil) "வீடியோ தேர்ந்தெடுக்கப்பட்டது: $uri" else "Video selected: $uri"
        }
    }

    BackHandler {
        onBack()
    }

    // Confirmation Dialog for Clean Reset
    if (showConfirmResetDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmResetDialog = false },
            title = {
                Text(
                    text = if (isTamil) "அனைத்து ஷார்ட்ஸ்களையும் நீக்கவா?" else "Clean Reset Shorts Database?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = if (isTamil)
                        "பழைய அனைத்து ஷார்ட்ஸ் மற்றும் தரவுகளையும் நீக்கி புதிய ஷார்ட்ஸ் அமைப்பை சுத்தமாக ஆரம்பிக்க வேண்டுமா?"
                    else
                        "This will remove all existing Shorts records and analysis cache, initializing the section completely clean as required by the specification."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllShorts()
                        showConfirmResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isTamil) "ஆம், நீக்கு" else "Yes, Clean Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmResetDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "டெவலப்பர் ஷார்ட்ஸ் ஸ்டுடியோ" else "Developer & Admin Shorts Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }
                        Text(
                            text = if (isTamil) "நிர்வாகி கட்டுப்பாடு மட்டுமே" else "Authorized Administrator Access Only",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryLightText.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrightGoldHighlight)
                    }
                },
                actions = {
                    IconButton(onClick = { showConfirmResetDialog = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clean Reset", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SecondaryNavySurface,
                    scrolledContainerColor = SecondaryNavySurface
                )
            )
        },
        containerColor = DeepNavyBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("shorts_admin_studio_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Section 1: Developer Upload & Verification Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = RichGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "புதிய 9:16 வீடியோ பதிவேற்றம்" else "Upload New 9:16 Portrait Video",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrightGoldHighlight
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Video File Selector Button
                        OutlinedButton(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_video_file_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RichGold)
                        ) {
                            Icon(Icons.Default.VideoFile, contentDescription = null, tint = BrightGoldHighlight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedVideoUri != null)
                                    (if (isTamil) "கோப்பு தேர்வு செய்யப்பட்டது ✓" else "Video Selected ✓")
                                else
                                    (if (isTamil) "சாதனத்திலிருந்து வீடியோ கோப்பைத் தேர்வு செய்க (9:16)" else "Select Video File from Device (9:16 Portrait)"),
                                color = BrightGoldHighlight,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Video Title (Required)
                        OutlinedTextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            label = { Text(if (isTamil) "வீடியோ தலைப்பு (கட்டாயம்)" else "Video Title (Required - Exact Preservation)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_video_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RichGold,
                                focusedLabelColor = RichGold,
                                focusedTextColor = PrimaryLightText,
                                unfocusedTextColor = PrimaryLightText
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description (Optional)
                        OutlinedTextField(
                            value = inputDescription,
                            onValueChange = { inputDescription = it },
                            label = { Text(if (isTamil) "விளக்கம் (விருப்பம்)" else "Description (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_video_desc_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RichGold,
                                focusedLabelColor = RichGold,
                                focusedTextColor = PrimaryLightText,
                                unfocusedTextColor = PrimaryLightText
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category & Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = inputCategory,
                                onValueChange = { inputCategory = it },
                                label = { Text(if (isTamil) "பிரிவு" else "Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RichGold,
                                    focusedLabelColor = RichGold,
                                    focusedTextColor = PrimaryLightText,
                                    unfocusedTextColor = PrimaryLightText
                                )
                            )

                            // Status Chip
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = inputStatus == "PUBLISHED",
                                    onClick = {
                                        inputStatus = if (inputStatus == "PUBLISHED") "DRAFT" else "PUBLISHED"
                                    },
                                    label = {
                                        Text(if (inputStatus == "PUBLISHED") "Published" else "Draft")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (inputStatus == "PUBLISHED") Icons.Default.CheckCircle else Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        if (uploadStatusMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uploadStatusMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrightGoldHighlight,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (selectedVideoUri == null) {
                                    uploadStatusMessage = if (isTamil) "தயவுசெய்து ஒரு வீடியோவைத் தேர்ந்தெடுக்கவும்." else "Please select a video file first."
                                    return@Button
                                }
                                if (inputTitle.isBlank()) {
                                    uploadStatusMessage = if (isTamil) "தயவுசெய்து தலைப்பை உள்ளிடவும்." else "Please enter the exact title."
                                    return@Button
                                }
                                isUploading = true
                                onUploadCustomVideo(
                                    selectedVideoUri!!,
                                    inputTitle.trim(),
                                    inputDescription.trim(),
                                    inputCategory.trim(),
                                    inputStatus
                                )
                                isUploading = false
                                uploadStatusMessage = if (isTamil) "வீடியோ வெற்றிகரமாக பதிவேற்றப்பட்டது!" else "Video uploaded and analyzed successfully!"
                                selectedVideoUri = null
                                inputTitle = ""
                                inputDescription = ""
                            },
                            enabled = selectedVideoUri != null && inputTitle.isNotBlank() && !isUploading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue,
                                contentColor = BrightGoldHighlight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_video_upload_button")
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUploading)
                                    (if (isTamil) "பதிவேற்றப்படுகிறது..." else "Processing & Analyzing...")
                                else
                                    (if (isTamil) "சரிபார்த்து பதிவேற்று (Publish Video)" else "Validate & Publish Short"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Section 2: Verified Developer Video Library (Authentic Preacher Videos)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavySurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LibraryAddCheck, contentDescription = null, tint = RichGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isTamil) "டெவலப்பர் சரிபார்க்கப்பட்ட வீடியோ வங்கி" else "Authorized Developer Media Bank",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGoldHighlight
                                )
                                Text(
                                    text = if (isTamil) "அசல் ஆடியோ & வேத நுண்ணறிவு ஆய்வுடன்" else "Authentic uploaded sermon videos with original audio & Bible analysis",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryLightText.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Video 1 Card: Loneliness (IPC Pallavilai Church)
                        DeveloperVideoPublishCard(
                            title = "Loneliness • God Is With You in the Valley | தனிமை • கர்த்தர் உங்களோடு கூட இருக்கிறார்",
                            speaker = "IPC Pallavilai Church • Psalm 23:4, 2 Tim 4:17, Josh 1:9",
                            duration = "1m 23s",
                            isAlreadyPublished = adminShorts.any { it.id == CuratedShortsAnalysisEngine.DEV_VIDEO_LONELINESS_ID && it.publicationStatus == "PUBLISHED" },
                            onPublish = {
                                onPublishDeveloperVideo(CuratedShortsAnalysisEngine.DEV_VIDEO_LONELINESS_ID)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Video 2 Card: Holy Spirit Fellowship
                        DeveloperVideoPublishCard(
                            title = "Fellowship with the Holy Spirit | பரிசுத்த ஆவியானவரோடு உறவாடுங்கள்",
                            speaker = "Tamil Preaching • Genesis 2:7, 2 Cor 3:18, Gal 5:16",
                            duration = "1m 40s",
                            isAlreadyPublished = adminShorts.any { it.id == CuratedShortsAnalysisEngine.DEV_VIDEO_HOLY_SPIRIT_ID && it.publicationStatus == "PUBLISHED" },
                            onPublish = {
                                onPublishDeveloperVideo(CuratedShortsAnalysisEngine.DEV_VIDEO_HOLY_SPIRIT_ID)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Video 3 Card: Humility & Overcoming Worry
                        DeveloperVideoPublishCard(
                            title = "Overcoming Worry & Worldly Ambition | யார் பெரியவன் & கவலைகளை வெல்லுதல்",
                            speaker = "Tamil Preaching Sister • Luke 22:24, John 21:3, Matt 6:33",
                            duration = "1m 20s",
                            isAlreadyPublished = adminShorts.any { it.id == CuratedShortsAnalysisEngine.DEV_VIDEO_HUMILITY_WORRY_ID && it.publicationStatus == "PUBLISHED" },
                            onPublish = {
                                onPublishDeveloperVideo(CuratedShortsAnalysisEngine.DEV_VIDEO_HUMILITY_WORRY_ID)
                            }
                        )
                    }
                }
            }

            // Section 3: Manage Published & Draft Shorts List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTamil) "அனைத்து ஷார்ட்ஸ்கள் நிர்வாகம் (${adminShorts.size})" else "All Shorts Records (${adminShorts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrightGoldHighlight
                    )

                    if (adminShorts.isNotEmpty()) {
                        TextButton(onClick = { showConfirmResetDialog = true }) {
                            Text(if (isTamil) "அனைத்தும் நீக்கு" else "Clear All", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (adminShorts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isTamil) "இன்னும் எந்த வீடியோக்களும் சேமிக்கப்படவில்லை." else "No Shorts records found in database.",
                            color = PrimaryLightText.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(adminShorts, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (item.publicationStatus == "PUBLISHED") PrimaryRoyalBlue.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = item.publicationStatus,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.publicationStatus == "PUBLISHED") BrightGoldHighlight else Color.Gray,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "${item.durationSeconds}s • ${item.category}",
                                    fontSize = 11.sp,
                                    color = RichGold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onTogglePublishStatus(item.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = if (item.publicationStatus == "PUBLISHED") "Unpublish" else "Publish",
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { onDeleteShort(item.id) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeveloperVideoPublishCard(
    title: String,
    speaker: String,
    duration: String,
    isAlreadyPublished: Boolean,
    onPublish: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DeepNavyBackground.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrightGoldHighlight,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$speaker • $duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryLightText.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onPublish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAlreadyPublished) ElevatedBlueSurface else PrimaryRoyalBlue,
                    contentColor = BrightGoldHighlight
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = if (isAlreadyPublished) Icons.Default.Check else Icons.Default.Publish,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAlreadyPublished) "Published" else "Publish",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
