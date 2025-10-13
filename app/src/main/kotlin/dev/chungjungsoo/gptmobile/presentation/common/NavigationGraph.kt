package dev.chungjungsoo.gptmobile.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import dev.chungjungsoo.gptmobile.data.model.ApiType
import dev.chungjungsoo.gptmobile.presentation.ui.chat.ChatScreen
import dev.chungjungsoo.gptmobile.presentation.ui.home.HomeScreen
import dev.chungjungsoo.gptmobile.presentation.ui.home.HomeViewModel
import dev.chungjungsoo.gptmobile.presentation.ui.home.LibraryScreen
import dev.chungjungsoo.gptmobile.presentation.ui.setting.AboutScreen
import dev.chungjungsoo.gptmobile.presentation.ui.setting.LicenseScreen
import dev.chungjungsoo.gptmobile.presentation.ui.setting.PlatformSettingScreen
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import dev.chungjungsoo.gptmobile.presentation.ui.setting.SettingScreen
import dev.chungjungsoo.gptmobile.presentation.ui.setting.SettingViewModel
import dev.chungjungsoo.gptmobile.presentation.ui.setup.SelectModelScreen
import dev.chungjungsoo.gptmobile.presentation.ui.setup.SelectPlatformScreen
import dev.chungjungsoo.gptmobile.presentation.ui.setup.SetupAPIUrlScreen
import dev.chungjungsoo.gptmobile.presentation.ui.setup.SetupCompleteScreen
import dev.chungjungsoo.gptmobile.presentation.ui.setup.SetupViewModel
import dev.chungjungsoo.gptmobile.presentation.ui.setup.TokenInputScreen
import dev.chungjungsoo.gptmobile.presentation.ui.startscreen.StartScreen
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.DrawerValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.ModalNavigationDrawer
import dev.chungjungsoo.gptmobile.presentation.ui.offlinemodel.ModelBrowserScreen
import dev.chungjungsoo.gptmobile.presentation.ui.offlinemodel.ModelDetailScreen

fun NavGraphBuilder.startScreenNavigation(navController: NavHostController) {
    composable(Route.GET_STARTED) {
        StartScreen { navController.navigate(Route.SETUP_ROUTE) }
    }
}

@Composable
fun SetupNavGraph(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(navController, drawerState, scope, settingOnClick = { navController.navigate(Route.SETTING_ROUTE) { launchSingleTop = true } })
        }
    ) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            // Remove top padding from innerPadding so screens with their own top bars are not pushed down
            val contentPadding = PaddingValues(
                start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                top = 0.dp,
                end = innerPadding.calculateRightPadding(LayoutDirection.Ltr),
                bottom = innerPadding.calculateBottomPadding()
            )
            NavHost(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                navController = navController,
                startDestination = Route.CHAT_LIST
            ) {
                homeScreenNavigation(navController, drawerState, scope)
                startScreenNavigation(navController)
                setupNavigation(navController)
                settingNavigation(navController)
                chatScreenNavigation(navController)
                offlineModelNavigation(navController)
            composable(Route.VARIANTS) {
                // Placeholder for Variants screen
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Variants Screen", style = MaterialTheme.typography.headlineMedium)
                }
            }
            composable(Route.AUTOMATIONS) {
                // Placeholder for Automations screen
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Automations Screen", style = MaterialTheme.typography.headlineMedium)
                }
            }
            composable(Route.AGENTS) {
                // Placeholder for Agents screen
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Agents Screen", style = MaterialTheme.typography.headlineMedium)
                }
            }
            composable(Route.LIBRARY) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                val chatListState by homeViewModel.chatListState.collectAsStateWithLifecycle()
                LibraryScreen(
                    chatListState = chatListState,
                    onExistingChatClick = { chatRoom ->
                        val enabledPlatformString = chatRoom.enabledPlatform.joinToString(",") { v -> v.name }
                        navController.navigate(
                            Route.CHAT_ROOM
                                .replace(oldValue = "{chatRoomId}", newValue = "${chatRoom.id}")
                                .replace(oldValue = "{enabledPlatforms}", newValue = enabledPlatformString)
                        )
                    },
                    homeViewModel = homeViewModel
                )
            }
        }
    }
}
}
fun NavGraphBuilder.setupNavigation(
    navController: NavHostController
) {
    navigation(startDestination = Route.SELECT_PLATFORM, route = Route.SETUP_ROUTE) {
        composable(route = Route.SELECT_PLATFORM) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SelectPlatformScreen(
                setupViewModel = setupViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.TOKEN_INPUT) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            TokenInputScreen(
                setupViewModel = setupViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.OPENAI_MODEL_SELECT) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SelectModelScreen(
                setupViewModel = setupViewModel,
                currentRoute = Route.OPENAI_MODEL_SELECT,
                platformType = ApiType.OPENAI,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.ANTHROPIC_MODEL_SELECT) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SelectModelScreen(
                setupViewModel = setupViewModel,
                currentRoute = Route.ANTHROPIC_MODEL_SELECT,
                platformType = ApiType.ANTHROPIC,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.GOOGLE_MODEL_SELECT) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SelectModelScreen(
                setupViewModel = setupViewModel,
                currentRoute = Route.GOOGLE_MODEL_SELECT,
                platformType = ApiType.GOOGLE,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.GROQ_MODEL_SELECT) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SelectModelScreen(
                setupViewModel = setupViewModel,
                currentRoute = Route.GROQ_MODEL_SELECT,
                platformType = ApiType.GROQ,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.OLLAMA_MODEL_SELECT) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SelectModelScreen(
                setupViewModel = setupViewModel,
                currentRoute = Route.OLLAMA_MODEL_SELECT,
                platformType = ApiType.OLLAMA,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.OLLAMA_API_ADDRESS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SetupAPIUrlScreen(
                setupViewModel = setupViewModel,
                currentRoute = Route.OLLAMA_API_ADDRESS,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.SETUP_COMPLETE) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SetupCompleteScreen(
                setupViewModel = setupViewModel,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Route.GET_STARTED) { inclusive = true }
                    }
                },
                onBackAction = { navController.navigateUp() }
            )
        }
    }
}

fun NavGraphBuilder.homeScreenNavigation(navController: NavHostController, drawerState: androidx.compose.material3.DrawerState, scope: kotlinx.coroutines.CoroutineScope) {
    composable(Route.CHAT_LIST) {
        HomeScreen(
            settingOnClick = { navController.navigate(Route.SETTING_ROUTE) { launchSingleTop = true } },
            onExistingChatClick = { chatRoom ->
                val enabledPlatformString = chatRoom.enabledPlatform.joinToString(",") { v -> v.name }
                navController.navigate(
                    Route.CHAT_ROOM
                        .replace(oldValue = "{chatRoomId}", newValue = "${chatRoom.id}")
                        .replace(oldValue = "{enabledPlatforms}", newValue = enabledPlatformString)
                )
            },
            navigateToNewChat = {
                val enabledPlatformString = it.joinToString(",") { v -> v.name }
                navController.navigate(
                    Route.CHAT_ROOM
                        .replace(oldValue = "{chatRoomId}", newValue = "0")
                        .replace(oldValue = "{enabledPlatforms}", newValue = enabledPlatformString)
                )
            },
            drawerState = drawerState,
            scope = scope,
            navController = navController
        )
    }
}

fun NavGraphBuilder.offlineModelNavigation(navController: NavHostController) {
    composable(Route.OFFLINE_MODEL_BROWSER) {
        ModelBrowserScreen(
            onBackClick = { navController.navigateUp() },
            onModelClick = { modelId ->
                val encodedModelId = URLEncoder.encode(modelId, StandardCharsets.UTF_8.toString())
                navController.navigate(
                    Route.OFFLINE_MODEL_DETAIL.replace("{modelId}", encodedModelId)
                )
            }
        )
    }
    
    composable(
        Route.OFFLINE_MODEL_DETAIL,
        arguments = listOf(
            navArgument("modelId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val encodedModelId = backStackEntry.arguments?.getString("modelId") ?: ""
        val modelId = URLDecoder.decode(encodedModelId, StandardCharsets.UTF_8.toString())
        ModelDetailScreen(
            modelId = modelId,
            onBackClick = { navController.navigateUp() }
        )
    }
}

fun NavGraphBuilder.chatScreenNavigation(navController: NavHostController) {
    composable(
        Route.CHAT_ROOM,
        arguments = listOf(
            navArgument("chatRoomId") { type = NavType.IntType },
            navArgument("enabledPlatforms") { defaultValue = "" }
        )
    ) {
        ChatScreen(
            onBackAction = { navController.navigateUp() }
        )
    }
}

fun NavGraphBuilder.settingNavigation(navController: NavHostController) {
    navigation(startDestination = Route.SETTINGS, route = Route.SETTING_ROUTE) {
        composable(Route.SETTINGS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETTING_ROUTE)
            }
            val settingViewModel: SettingViewModel = hiltViewModel(parentEntry)
            SettingScreen(
                settingViewModel = settingViewModel,
                onNavigationClick = { navController.navigateUp() },
                onNavigateToPlatformSetting = { apiType ->
                    when (apiType) {
                        ApiType.OPENAI -> navController.navigate(Route.OPENAI_SETTINGS)
                        ApiType.ANTHROPIC -> navController.navigate(Route.ANTHROPIC_SETTINGS)
                        ApiType.GOOGLE -> navController.navigate(Route.GOOGLE_SETTINGS)
                        ApiType.GROQ -> navController.navigate(Route.GROQ_SETTINGS)
                        ApiType.OLLAMA -> navController.navigate(Route.OLLAMA_SETTINGS)
                        ApiType.OFFLINE_AI -> navController.navigate(Route.OFFLINE_MODEL_BROWSER)
                    }
                },
                onNavigateToAboutPage = { navController.navigate(Route.ABOUT_PAGE) }
            )
        }
        composable(Route.OPENAI_SETTINGS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETTING_ROUTE)
            }
            val settingViewModel: SettingViewModel = hiltViewModel(parentEntry)
            PlatformSettingScreen(
                settingViewModel = settingViewModel,
                apiType = ApiType.OPENAI
            ) { navController.navigateUp() }
        }
        composable(Route.ANTHROPIC_SETTINGS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETTING_ROUTE)
            }
            val settingViewModel: SettingViewModel = hiltViewModel(parentEntry)
            PlatformSettingScreen(
                settingViewModel = settingViewModel,
                apiType = ApiType.ANTHROPIC
            ) { navController.navigateUp() }
        }
        composable(Route.GOOGLE_SETTINGS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETTING_ROUTE)
            }
            val settingViewModel: SettingViewModel = hiltViewModel(parentEntry)
            PlatformSettingScreen(
                settingViewModel = settingViewModel,
                apiType = ApiType.GOOGLE
            ) { navController.navigateUp() }
        }
        composable(Route.GROQ_SETTINGS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETTING_ROUTE)
            }
            val settingViewModel: SettingViewModel = hiltViewModel(parentEntry)
            PlatformSettingScreen(
                settingViewModel = settingViewModel,
                apiType = ApiType.GROQ
            ) { navController.navigateUp() }
        }
        composable(Route.OLLAMA_SETTINGS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETTING_ROUTE)
            }
            val settingViewModel: SettingViewModel = hiltViewModel(parentEntry)
            PlatformSettingScreen(
                settingViewModel = settingViewModel,
                apiType = ApiType.OLLAMA
            ) { navController.navigateUp() }
        }
        composable(Route.ABOUT_PAGE) {
            AboutScreen(
                onNavigationClick = { navController.navigateUp() },
                onNavigationToLicense = { navController.navigate(Route.LICENSE) }
            )
        }
        composable(Route.LICENSE) {
            LicenseScreen(onNavigationClick = { navController.navigateUp() })
        }
    }
}
