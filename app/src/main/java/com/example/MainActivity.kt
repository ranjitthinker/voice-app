package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PlaybackScreen
import com.example.ui.screens.RecordScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VoiceVaultViewModel
import com.example.viewmodel.VaultScreen
import com.example.translate

class MainActivity : ComponentActivity() {

    private val viewModel: VoiceVaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ThemeSettings.init(applicationContext)
        LanguageManager.init(applicationContext)

        enableEdgeToEdge()

        setContent {
            val isDarkTheme by ThemeSettings.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val screenState by viewModel.currentScreen.collectAsState()

                Scaffold(
                    bottomBar = {
                        if (screenState != VaultScreen.SPLASH && screenState != VaultScreen.RECORDING) {
                            BottomNavBar(
                                currentScreen = screenState,
                                onTabClick = { target ->
                                    if (target == VaultScreen.RECORDING) {
                                        viewModel.triggerStartRecording()
                                    } else {
                                        viewModel.setScreen(target)
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AnimatedContent(
                            targetState = screenState,
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                VaultScreen.SPLASH -> {
                                    SplashScreen()
                                }
                                VaultScreen.DASHBOARD -> {
                                    DashboardScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                    )
                                }
                                VaultScreen.RECORDING -> {
                                    RecordScreen(
                                        viewModel = viewModel
                                    )
                                }
                                VaultScreen.PLAYBACK -> {
                                    PlaybackScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
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
fun BottomNavBar(
    currentScreen: VaultScreen,
    onTabClick: (VaultScreen) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(84.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                label = translate("home"),
                isSelected = currentScreen == VaultScreen.DASHBOARD,
                onClick = { onTabClick(VaultScreen.DASHBOARD) }
            )
            NavBarItem(
                icon = Icons.Default.Mic,
                label = translate("record"),
                isSelected = currentScreen == VaultScreen.RECORDING,
                onClick = { onTabClick(VaultScreen.RECORDING) }
            )
            NavBarItem(
                icon = Icons.Default.Star,
                label = translate("favorites"),
                isSelected = false,
                onClick = { onTabClick(VaultScreen.DASHBOARD) }
            )
            NavBarItem(
                icon = Icons.Default.Search,
                label = translate("search"),
                isSelected = false,
                onClick = { onTabClick(VaultScreen.DASHBOARD) }
            )
            NavBarItem(
                icon = Icons.Default.Settings,
                label = translate("settings"),
                isSelected = false,
                onClick = { onTabClick(VaultScreen.DASHBOARD) }
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        val background = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent

        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(background)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = contentColor
        )
    }
}
