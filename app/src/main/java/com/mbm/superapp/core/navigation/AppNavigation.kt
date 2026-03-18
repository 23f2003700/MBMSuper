package com.mbm.superapp.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mbm.superapp.core.theme.ThemeEngine
import com.mbm.superapp.core.effects.MatrixRainOverlay
import com.mbm.superapp.features.alumni.AlumniListScreen
import com.mbm.superapp.features.chat.ChatListScreen
import com.mbm.superapp.features.chat.ChatRoomScreen
import com.mbm.superapp.features.exchange.CreateExchangeScreen
import com.mbm.superapp.features.exchange.ExchangeListScreen
import com.mbm.superapp.features.fest.FestListScreen
import com.mbm.superapp.features.games.GamesScreen
import com.mbm.superapp.features.games.catchcat.CatChaseScreen
import com.mbm.superapp.features.games.chess.ChessScreen
import com.mbm.superapp.features.games.dotbox.DotBoxScreen
import com.mbm.superapp.features.games.ludo.LudoScreen
import com.mbm.superapp.features.games.snake.SnakeScreen
import com.mbm.superapp.features.games.tictactoe.TicTacToeScreen
import com.mbm.superapp.features.home.HomeScreen
import com.mbm.superapp.features.library.LibraryScreen
import com.mbm.superapp.features.map.MBMMapScreen
import com.mbm.superapp.features.mudda.CreateIssueScreen
import com.mbm.superapp.features.mudda.MuddaListScreen
import com.mbm.superapp.features.profile.ProfileScreen
import com.mbm.superapp.features.settings.SettingsScreen
import com.mbm.superapp.features.splash.SplashScreen
import com.mbm.superapp.features.tools.ToolsScreen
import com.mbm.superapp.features.tools.image.ImageToolsScreen
import com.mbm.superapp.features.tools.pdf.PdfToolsScreen
import com.mbm.superapp.features.trips.CreateTripScreen
import com.mbm.superapp.features.trips.TripsListScreen

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val route: String,
)

@Composable
fun AppNavigation(themeEngine: ThemeEngine) {
    val navController = rememberNavController()
    val animDuration = 300

    // Touch state for tear effect
    var isTouching by remember { mutableStateOf(false) }
    var touchX by remember { mutableFloatStateOf(0f) }
    var touchY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val any = event.changes.any { it.pressed }
                        if (any) {
                            val c = event.changes.firstOrNull { it.pressed }
                            if (c != null) {
                                touchX = c.position.x
                                touchY = c.position.y
                            }
                            isTouching = true
                        } else {
                            isTouching = false
                        }
                    }
                }
            }
    ) {
    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = {
            fadeIn(tween(animDuration)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(animDuration)
            )
        },
        exitTransition = { fadeOut(tween(animDuration)) },
        popEnterTransition = {
            fadeIn(tween(animDuration)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(animDuration)
            )
        },
        popExitTransition = { fadeOut(tween(animDuration)) },
    ) {
        composable(
            "splash",
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(500)) },
        ) {
            SplashScreen(onFinished = {
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("main") {
            MainScaffold(rootNavController = navController, themeEngine = themeEngine)
        }

        // Settings
        composable("settings") {
            SettingsScreen(themeEngine = themeEngine, onBack = { navController.popBackStack() })
        }

        // Games
        composable("games/tictactoe") { TicTacToeScreen(onBack = { navController.popBackStack() }) }
        composable("games/dotbox") { DotBoxScreen(onBack = { navController.popBackStack() }) }
        composable("games/snake") { SnakeScreen(onBack = { navController.popBackStack() }) }
        composable("games/chess") { ChessScreen(onBack = { navController.popBackStack() }) }
        composable("games/ludo") { LudoScreen(onBack = { navController.popBackStack() }) }
        composable("games/catchcat") { CatChaseScreen(onBack = { navController.popBackStack() }) }

        // Tools
        composable("tools/pdf") { PdfToolsScreen(onBack = { navController.popBackStack() }) }
        composable("tools/image") { ImageToolsScreen(onBack = { navController.popBackStack() }) }

        // Mudda / Issues
        composable("mudda") {
            MuddaListScreen(
                onBack = { navController.popBackStack() },
                onCreateIssue = { navController.navigate("mudda/create") },
                onIssueClick = { },
            )
        }
        composable("mudda/create") { CreateIssueScreen(onBack = { navController.popBackStack() }) }

        // Exchange
        composable("exchange") {
            ExchangeListScreen(
                onBack = { navController.popBackStack() },
                onCreatePost = { navController.navigate("exchange/create") },
                onPostClick = { },
            )
        }
        composable("exchange/create") { CreateExchangeScreen(onBack = { navController.popBackStack() }) }

        // Chat
        composable("chat") {
            ChatListScreen(
                onBack = { navController.popBackStack() },
                onRoomClick = { roomId -> navController.navigate("chat/$roomId") },
            )
        }
        composable("chat/{roomId}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            ChatRoomScreen(roomId = roomId, onBack = { navController.popBackStack() })
        }

        // Fest
        composable("fest") {
            FestListScreen(
                onBack = { navController.popBackStack() },
                onEventClick = { },
            )
        }

        // Library
        composable("library") { LibraryScreen(onBack = { navController.popBackStack() }) }

        // Trips
        composable("trips") {
            TripsListScreen(
                onBack = { navController.popBackStack() },
                onCreateTrip = { navController.navigate("trips/create") },
                onTripClick = { },
            )
        }
        composable("trips/create") { CreateTripScreen(onBack = { navController.popBackStack() }) }

        // Alumni
        composable("alumni") { AlumniListScreen(onBack = { navController.popBackStack() }) }
    }
    // Matrix rain + tear overlay (always on, tear on touch, no pointer input)
    MatrixRainOverlay(isTouching = isTouching, touchX = touchX, touchY = touchY)
    }
}

@Composable
fun MainScaffold(rootNavController: NavHostController, themeEngine: ThemeEngine) {
    val tabNavController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        BottomNavItem("Home", Icons.Outlined.Home, Icons.Filled.Home, "tab_home"),
        BottomNavItem("Tools", Icons.Outlined.Build, Icons.Filled.Build, "tab_tools"),
        BottomNavItem("Games", Icons.Outlined.SportsEsports, Icons.Filled.SportsEsports, "tab_games"),
        BottomNavItem("Map", Icons.Outlined.Map, Icons.Filled.Map, "tab_map"),
        BottomNavItem("Profile", Icons.Outlined.Person, Icons.Filled.Person, "tab_profile"),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            if (selectedTab != index) {
                                selectedTab = index
                                tabNavController.navigate(item.route) {
                                    popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedTab == index) item.selectedIcon else item.icon,
                                contentDescription = item.label,
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        ),
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = "tab_home",
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) },
        ) {
            composable("tab_home") { HomeScreen(navController = rootNavController) }
            composable("tab_tools") { ToolsScreen(navController = rootNavController) }
            composable("tab_games") { GamesScreen(navController = rootNavController) }
            composable("tab_map") { MBMMapScreen() }
            composable("tab_profile") { ProfileScreen(navController = rootNavController, themeEngine = themeEngine) }
        }
    }
}
