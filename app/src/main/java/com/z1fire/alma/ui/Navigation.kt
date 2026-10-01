package com.z1fire.alma.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.z1fire.alma.ui.screens.CourseScreen
import com.z1fire.alma.ui.screens.MainScreen

@Composable
fun AlmaNavHost(pendingCourseId: String?, onPendingConsumed: () -> Unit) {
    val nav = rememberNavController()
    val openCourse = { id: String -> nav.navigate("course/$id") { launchSingleTop = true } }

    LaunchedEffect(pendingCourseId) {
        if (pendingCourseId != null) {
            openCourse(pendingCourseId)
            onPendingConsumed()
        }
    }

    NavHost(navController = nav, startDestination = "main") {
        composable("main") { MainScreen(onOpenCourse = openCourse) }
        composable("course/{id}") { e ->
            CourseScreen(courseId = e.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
        }
    }
}
