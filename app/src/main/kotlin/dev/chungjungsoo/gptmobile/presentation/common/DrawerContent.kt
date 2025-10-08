package dev.chungjungsoo.gptmobile.presentation.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import dev.chungjungsoo.gptmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    settingOnClick: () -> Unit
) {
    ModalDrawerSheet {
        val items = listOf(
            stringResource(R.string.home) to Route.CHAT_LIST,
            stringResource(R.string.variants) to Route.VARIANTS,
            stringResource(R.string.automations) to Route.AUTOMATIONS,
            stringResource(R.string.agents) to Route.AGENTS,
            stringResource(R.string.library) to Route.LIBRARY
        )
        items.forEach { (title, route) ->
            NavigationDrawerItem(
                label = { Text(title) },
                selected = false, // Adjust based on current route if needed
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.weight(1f)) // Push settings to bottom
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.settings)) },
            selected = false,
            onClick = {
                settingOnClick()
                scope.launch { drawerState.close() }
            },
            modifier = Modifier
        )
    }
}