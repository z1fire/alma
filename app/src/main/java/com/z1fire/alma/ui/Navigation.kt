package com.z1fire.alma.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.z1fire.alma.ui.screens.CourseScreen
import com.z1fire.alma.ui.screens.FinishedScreen
import com.z1fire.alma.ui.screens.HomeScreen
import com.z1fire.alma.ui.screens.SettingsScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("studying", "Studying", Icons.Filled.AutoStories),
    Tab("finished", "Finished", Icons.Filled.WorkspacePremium),
)

private fun NavHostController.openCourse(id: String) = navigate("course/$id") { launchSingleTop = true }

@Composable
fun AlmaNavHost(pendingCourseId: String?, onPendingConsumed: () -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    LaunchedEffect(pendingCourseId) {
        if (pendingCourseId != null) {
            nav.openCourse(pendingCourseId)
            onPendingConsumed()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (tabs.any { it.route == route }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "studying",
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            composable("studying") {
                HomeScreen(onOpenCourse = nav::openCourse, onOpenSettings = { nav.navigate("settings") })
            }
            composable("finished") {
                FinishedScreen(onOpenCourse = nav::openCourse)
            }
            composable("settings") {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
            composable("course/{id}") { e ->
                CourseScreen(courseId = e.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
        }
    }
}
