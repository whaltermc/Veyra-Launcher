package dev.whaltermc.veyra.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.whaltermc.veyra.ui.screen.account.AccountScreen
import dev.whaltermc.veyra.ui.screen.instance.InstanceDetailScreen
import dev.whaltermc.veyra.ui.screen.instance.InstanceListScreen
import dev.whaltermc.veyra.ui.screen.instance.NewInstanceScreen
import dev.whaltermc.veyra.ui.screen.launch.LaunchScreen
import dev.whaltermc.veyra.ui.screen.settings.SettingsScreen
import dev.whaltermc.veyra.ui.screen.splash.SplashScreen

@Composable
fun VeyraNavHost(
    startDestination: String = "splash",
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val navController = rememberNavController()
    NavHost(navController, startDestination, modifier) {
        composable("splash") {
            SplashScreen(onNavigateToLogin = { navController.navigate("login") },
                onNavigateToHome = { navController.navigate("home") })
        }
        composable("login") {
            AccountScreen(onLoginSuccess = { navController.navigate("home") })
        }
        composable("home") {
            InstanceListScreen(
                onNewInstance = { navController.navigate("new_instance") },
                onInstanceClick = { id -> navController.navigate("instance/$id") },
                onInstanceLongClick = { id -> navController.navigate("instance/$id?detail=true") }
            )
        }
        composable(
            route = "new_instance",
            arguments = listOf(androidx.navigation.navArgument("instanceId") { type = androidx.navigation.NavType.StringType(); defaultValue = "" })
        ) { backStackEntry ->
            val instanceId = backStackEntry.getString() ?: ""
            NewInstanceScreen(
                instanceId = if (instanceId.isBlank()) null else instanceId,
                onSave = { navController.navigate("home") { popUpTo("home") { inclusive = false } } },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = "instance/{instanceId}",
            arguments = listOf(
                androidx.navigation.navArgument("instanceId") { type = androidx.navigation.NavType.StringType() },
                androidx.navigation.navArgument("detail") { type = androidx.navigation.NavType.BoolType(); defaultValue = "false" }
            )
        ) { backStackEntry ->
            val instanceId = backStackEntry.getString()!!
            val isDetail = backStackEntry.getBoolean()
            InstanceDetailScreen(
                instanceId = instanceId,
                isDetailView = isDetail,
                onLaunch = { navController.navigate("launch/$instanceId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "launch/{instanceId}",
            arguments = listOf(androidx.navigation.navArgument("instanceId") { type = androidx.navigation.NavType.StringType() })
        ) { backStackEntry ->
            val instanceId = backStackEntry.getString()!!
            LaunchScreen(
                instanceId = instanceId,
                onExit = { navController.navigate("home") { popUpTo("home") { inclusive = false } } }
            )
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}