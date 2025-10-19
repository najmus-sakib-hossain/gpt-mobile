package dev.chungjungsoo.gptmobile.presentation.common

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.presentation.icons.SolarIcons

// NOTE: You now have access to 2500+ Material Icons Extended!
// To use any icon, simply import it like this:
// import androidx.compose.material.icons.filled.YourIconName
// import androidx.compose.material.icons.outlined.YourIconName
//
// Examples of available icons you can now use:
// - Icons.Filled.Dashboard, Icons.Outlined.Dashboard
// - Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle
// - Icons.Filled.ChatBubble, Icons.Outlined.ChatBubble
// - Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome
// - Icons.Filled.SmartToy, Icons.Outlined.SmartToy
// - Icons.Filled.LibraryBooks, Icons.Outlined.LibraryBooks
// - Icons.Filled.Bolt, Icons.Outlined.Bolt
// - Icons.Filled.Psychology, Icons.Outlined.Psychology
// - Icons.Filled.Explore, Icons.Outlined.Explore
// - And 2500+ more!
//
// Full icon list: https://fonts.google.com/icons?icon.set=Material+Icons

data class BottomNavItem(
        val title: String,
        val selectedIcon: Int,
        val unselectedIcon: Int,
        val route: String
)

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val bottomNavItems =
            listOf(
                    BottomNavItem(
                            title = stringResource(R.string.home),
                            selectedIcon = SolarIcons.HomeAngleBold,
                            unselectedIcon = SolarIcons.HomeAngleLine,
                            route = Route.CHAT_LIST
                    ),
                    BottomNavItem(
                            title = stringResource(R.string.variants),
                            selectedIcon = SolarIcons.LayersMinimalisticBold,
                            unselectedIcon = SolarIcons.LayersMinimalisticLine,
                            route = Route.VARIANTS
                    ),
                    BottomNavItem(
                            title = stringResource(R.string.automations),
                            selectedIcon = SolarIcons.HourglassLineBold,
                            unselectedIcon = SolarIcons.HourglassLineLine,
                            route = Route.AUTOMATIONS
                    ),
                    BottomNavItem(
                            title = stringResource(R.string.agents),
                            selectedIcon = SolarIcons.GhostBold,
                            unselectedIcon = SolarIcons.GhostLine,
                            route = Route.AGENTS
                    ),
                    BottomNavItem(
                            title = stringResource(R.string.library),
                            selectedIcon = SolarIcons.LibraryBold,
                            unselectedIcon = SolarIcons.LibraryLine,
                            route = Route.LIBRARY
                    )
            )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                                painter = painterResource(id = if (selected) item.selectedIcon else item.unselectedIcon),
                                contentDescription = item.title
                        )
                    },
                    label = {
                        Text(item.title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
            )
        }
    }
}
