package com.z1fire.alma.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
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
import com.z1fire.alma.ui.screens.CatalogueScreen
import com.z1fire.alma.ui.screens.CourseScreen
import com.z1fire.alma.ui.screens.CurriculumScreen
import com.z1fire.alma.ui.screens.FinishedScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("curriculum", "Curriculum", Icons.Filled.AutoStories),
    Tab("catalogue", "Catalogue", Icons.AutoMirrored.Filled.LibraryBooks),
    Tab("finished", "Finished", Icons.Filled.WorkspacePremium),
)

private fun NavHostController.openTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun AlmaNavHost(pendingCourseId: String?, onPendingConsumed: () -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val openCourse = { id: String -> nav.navigate("course/$id") { launchSingleTop = true } }

    LaunchedEffect(pendingCourseId) {
        if (pendingCourseId != null) {
            openCourse(pendingCourseId)
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
                            onClick = { nav.openTab(tab.route) },
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
            startDestination = "curriculum",
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable("curriculum") { CurriculumScreen(onOpenCourse = openCourse, onOpenCatalogue = { nav.openTab("catalogue") }) }
            composable("catalogue") { CatalogueScreen(onOpenCourse = openCourse) }
            composable("finished") { FinishedScreen(onOpenCourse = openCourse) }
            composable("course/{id}") { e ->
                CourseScreen(courseId = e.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
        }
    }
}
