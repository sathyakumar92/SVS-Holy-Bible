package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepNavyBackground
import com.example.ui.theme.RichGold
import com.example.ui.theme.SecondaryLightText
import com.example.ui.theme.SecondaryNavySurface

/**
 * Premium side-by-side segmented language switcher for the SVS Holy Bible app.
 *
 * Displays "EN" and "தமிழ்" simultaneously:
 * [ EN | தமிழ் ]
 *
 * Allows users to see both options at once and tap either option directly
 * to switch the Bible and app interface language without opening menus or dropdowns.
 */
@Composable
fun BibleLanguageSwitcher(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerTestTag: String = "language_toggle_button"
) {
    val isEnglish = selectedLanguage.equals("en", ignoreCase = true)
    val isTamil = selectedLanguage.equals("ta", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SecondaryNavySurface,
        border = BorderStroke(1.dp, RichGold.copy(alpha = 0.4f)),
        tonalElevation = 2.dp,
        modifier = modifier
            .testTag(containerTestTag)
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // EN Option
            LanguageSegment(
                label = "EN",
                isSelected = isEnglish,
                onClick = { onLanguageSelected("en") },
                accessibilityDesc = if (isEnglish) "English Bible, currently selected" else "Switch to English Bible",
                testTag = "language_option_en"
            )

            // Divider between segments
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(14.dp)
                    .background(RichGold.copy(alpha = 0.25f))
            )

            // தமிழ் Option
            LanguageSegment(
                label = "தமிழ்",
                isSelected = isTamil,
                onClick = { onLanguageSelected("ta") },
                accessibilityDesc = if (isTamil) "தமிழ் வேதாகமம், தேர்ந்தெடுக்கப்பட்டது" else "தமிழ் வேதாகமத்திற்கு மாற்றவும்",
                testTag = "language_option_ta"
            )
        }
    }
}

@Composable
private fun LanguageSegment(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accessibilityDesc: String,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor = animateColorAsState(
        targetValue = if (isSelected) RichGold else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "segmentBg"
    )

    val textColor = animateColorAsState(
        targetValue = if (isSelected) DeepNavyBackground else SecondaryLightText,
        animationSpec = tween(durationMillis = 200),
        label = "segmentText"
    )

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = accessibilityDesc
            }
            .testTag(testTag)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor.value,
            maxLines = 1
        )
    }
}
