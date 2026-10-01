package com.z1fire.alma.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.z1fire.alma.ui.screens.CampusScreen
import com.z1fire.alma.ui.screens.CatalogScreen
import com.z1fire.alma.ui.screens.CourseDetailScreen
import com.z1fire.alma.ui.screens.CourseEditScreen
import com.z1fire.alma.ui.screens.ScheduleScreen
import com.z1fire.alma.ui.screens.SettingsScreen
import com.z1fire.alma.ui.screens.TranscriptScreen
import com.z1fire.alma.ui.screens.WelcomeScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("campus", "Campus", Icons.Filled.AccountBalance),
    Tab("catalog", "Catalog", Icons.AutoMirrored.Filled.MenuBook),
    Tab("schedule", "Schedule", Icons.Filled.CalendarMonth),
    Tab("transcript", "Transcript", Icons.Filled.WorkspacePremium),
)

private const val EDIT_ROUTE = "edit?id={id}"

private fun NavHostController.openCourse(id: String) = navigate("course/$id") { launchSingleTop = true }
private fun NavHostController.editCourse(id: String?) = navigate(if (id == null) "edit" else "edit?id=$id")

@Composable
fun AlmaNavHost(pendingCourseId: String?, onPendingConsumed: () -> Unit) {
    val data = rememberAppData()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val start = remember { if (data.profile.onboarded) "campus" else "welcome" }

    LaunchedEffect(pendingCourseId, data.profile.onboarded) {
        if (pendingCourseId != null && data.profile.onboarded) {
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
            startDestination = start,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            composable("welcome") {
                WelcomeScreen(onDone = {
                    nav.navigate("campus") { popUpTo("welcome") { inclusive = true } }
                })
            }
            composable("campus") {
                CampusScreen(
                    onOpenCourse = nav::openCourse,
                    onOpenSettings = { nav.navigate("settings") },
                    onNewCourse = { nav.editCourse(null) },
                )
            }
            composable("catalog") {
                CatalogScreen(onOpenCourse = nav::openCourse, onNewCourse = { nav.editCourse(null) })
            }
            composable("schedule") {
                ScheduleScreen(onOpenCourse = nav::openCourse)
            }
            composable("transcript") {
                TranscriptScreen(onOpenCourse = nav::openCourse)
            }
            composable("settings") {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
            composable("course/{id}") { e ->
                CourseDetailScreen(
                    courseId = e.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.editCourse(it) },
                    onOpenCourse = nav::openCourse,
                )
            }
            composable(
                EDIT_ROUTE,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null }),
            ) { e ->
                val id = e.arguments?.getString("id")
                CourseEditScreen(
                    courseId = id,
                    onBack = { nav.popBackStack() },
                    onSaved = { savedId ->
                        if (id == null) {
                            nav.navigate("course/$savedId") { popUpTo(EDIT_ROUTE) { inclusive = true } }
                        } else {
                            nav.popBackStack()
                        }
                    },
                )
            }
        }
    }
}
