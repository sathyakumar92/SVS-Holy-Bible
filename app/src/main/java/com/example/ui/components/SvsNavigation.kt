package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SvsTopAppBar(
    title: String,
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit = {},
    onLanguageToggle: () -> Unit = {},
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    navigationIcon: (@Composable () -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                BibleLogo(
                    size = 32.dp,
                    animated = false,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "SVS Holy Bible",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            }
        },
        actions = {
            // Side-by-Side Language Switcher: EN | தமிழ்
            BibleLanguageSwitcher(
                selectedLanguage = currentLanguage,
                onLanguageSelected = { lang ->
                    onLanguageSelected(lang)
                },
                modifier = Modifier.padding(end = 4.dp),
                containerTestTag = "language_toggle_button"
            )

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("top_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("top_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

data class NavItem(
    val screen: AppScreen,
    val titleEn: String,
    val titleTa: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun SvsBottomNavigationBar(
    currentScreen: AppScreen,
    languageCode: String,
    onNavigate: (AppScreen) -> Unit
) {
    val items = listOf(
        NavItem(AppScreen.HOME, "Home", "முகப்பு", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem(AppScreen.READER, "Bible", "வேதம்", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        NavItem(AppScreen.TOPICS, "Topics", "தலைப்புகள்", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
        NavItem(AppScreen.SEARCH, "Search", "தேடல்", Icons.Filled.Search, Icons.Outlined.Search),
        NavItem(AppScreen.MORE, "More", "கூடுதல்", Icons.Filled.Apps, Icons.Outlined.Apps)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("svs_bottom_bar")
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen ||
                    (item.screen == AppScreen.TOPICS && currentScreen == AppScreen.TOPIC_DETAIL) ||
                    (item.screen == AppScreen.MORE && currentScreen in listOf(
                        AppScreen.AUDIO_PLAYER,
                        AppScreen.DAILY_DEVOTIONAL, AppScreen.QUIZ, AppScreen.READING_PLANS,
                        AppScreen.PLAN_DETAIL, AppScreen.LIBRARY,
                        AppScreen.SETTINGS, AppScreen.ABOUT
                    ))

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.titleEn,
                        tint = if (isSelected) RichGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = if (languageCode == "ta") item.titleTa else item.titleEn,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) RichGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PrimaryRoyalBlue.copy(alpha = 0.35f)
                ),
                modifier = Modifier.testTag("nav_item_${item.screen.name.lowercase()}")
            )
        }
    }
}
