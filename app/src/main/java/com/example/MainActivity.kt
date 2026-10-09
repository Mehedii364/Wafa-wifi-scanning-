package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature.about.AboutScreen
import com.example.feature.compatibility.CompatibilityScreen
import com.example.feature.csi.CsiScreen
import com.example.feature.dashboard.DashboardScreen
import com.example.feature.diagnostics.DiagnosticsScreen
import com.example.feature.experimentalsensing.ExperimentalSensingScreen
import com.example.feature.history.HistoryScreen
import com.example.feature.networkscanner.NetworkScannerScreen
import com.example.feature.privacy.PrivacyCenterScreen
import com.example.feature.settings.SettingsScreen
import com.example.feature.wifianalyzer.WifiAnalyzerScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VioletAccent
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val currentTheme by mainViewModel.themeMode.collectAsState()

            MyApplicationTheme(themeMode = currentTheme) {
                WafaAppRoot(viewModel = mainViewModel)
            }
        }
    }
}

data class DrawerMenuItem(
    val screen: NavScreen,
    val titleKey: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WafaAppRoot(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val maskPii by viewModel.maskPii.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Runtime Permission Launcher
    var hasRequestedPermissions by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.wifiMonitor.refreshCurrentConnection()
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedPermissions) {
            hasRequestedPermissions = true
            val perms = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
            permissionLauncher.launch(perms.toTypedArray())
        }
    }

    // Handle back button press when on secondary screens
    BackHandler(enabled = currentScreen != NavScreen.DASHBOARD) {
        viewModel.navigateTo(NavScreen.DASHBOARD)
    }

    val menuItems = listOf(
        DrawerMenuItem(NavScreen.DASHBOARD, "home", Icons.Default.Home),
        DrawerMenuItem(NavScreen.ANALYZER, "analyzer", Icons.Default.Analytics),
        DrawerMenuItem(NavScreen.SCANNER, "scanner", Icons.Default.Radar),
        DrawerMenuItem(NavScreen.SENSING, "sensing", Icons.Default.Sensors),
        DrawerMenuItem(NavScreen.CSI, "csi", Icons.Default.DeveloperBoard),
        DrawerMenuItem(NavScreen.HISTORY, "history", Icons.Default.History),
        DrawerMenuItem(NavScreen.COMPATIBILITY, "compatibility", Icons.Default.PhoneAndroid),
        DrawerMenuItem(NavScreen.DIAGNOSTICS, "diagnostics", Icons.Default.BugReport),
        DrawerMenuItem(NavScreen.PRIVACY, "privacy", Icons.Default.Security),
        DrawerMenuItem(NavScreen.SETTINGS, "settings", Icons.Default.Settings),
        DrawerMenuItem(NavScreen.ABOUT, "about", Icons.Default.Info)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Drawer Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Wafa Wi-Fi Sense",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Developed by Mehedi364",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    menuItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(viewModel.getString(item.titleKey)) },
                            icon = { Icon(imageVector = item.icon, contentDescription = null) },
                            selected = currentScreen == item.screen,
                            onClick = {
                                viewModel.navigateTo(item.screen)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                selectedIconColor = CyanAccent,
                                selectedTextColor = CyanAccent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .testTag("drawer_item_${item.screen.name}")
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Wafa Wi-Fi Sense",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = viewModel.getString("tagline"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("hamburger_menu_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleMaskPii() },
                            modifier = Modifier.testTag("appbar_mask_toggle")
                        ) {
                            Icon(
                                imageVector = if (maskPii) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle PII Masking",
                                tint = CyanAccent
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val bottomItems = listOf(
                        Triple(NavScreen.DASHBOARD, "home", Icons.Default.Home),
                        Triple(NavScreen.ANALYZER, "analyzer", Icons.Default.Analytics),
                        Triple(NavScreen.SCANNER, "scanner", Icons.Default.Radar),
                        Triple(NavScreen.SENSING, "sensing", Icons.Default.Sensors)
                    )

                    bottomItems.forEach { (screen, titleKey, icon) ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = { Icon(imageVector = icon, contentDescription = viewModel.getString(titleKey)) },
                            label = { Text(viewModel.getString(titleKey), fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanAccent,
                                selectedTextColor = CyanAccent,
                                indicatorColor = CyanAccent.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("bottom_nav_${screen.name}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    NavScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    NavScreen.ANALYZER -> WifiAnalyzerScreen(viewModel = viewModel)
                    NavScreen.SCANNER -> NetworkScannerScreen(viewModel = viewModel)
                    NavScreen.SENSING -> ExperimentalSensingScreen(viewModel = viewModel)
                    NavScreen.CSI -> CsiScreen(viewModel = viewModel)
                    NavScreen.HISTORY -> HistoryScreen(viewModel = viewModel)
                    NavScreen.COMPATIBILITY -> CompatibilityScreen(viewModel = viewModel)
                    NavScreen.DIAGNOSTICS -> DiagnosticsScreen(viewModel = viewModel)
                    NavScreen.EXPORT -> HistoryScreen(viewModel = viewModel)
                    NavScreen.PRIVACY -> PrivacyCenterScreen(viewModel = viewModel)
                    NavScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    NavScreen.ABOUT -> AboutScreen(viewModel = viewModel)
                }
            }
        }
    }
}
