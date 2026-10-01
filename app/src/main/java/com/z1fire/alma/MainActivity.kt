package com.z1fire.alma

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import com.z1fire.alma.ui.AlmaNavHost
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.theme.AlmaTheme

class MainActivity : ComponentActivity() {
    /** Course to open, e.g. when launched from a class reminder. */
    private val pendingCourse = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) pendingCourse.value = intent.getStringExtra(EXTRA_COURSE_ID)
        val repo = (application as AlmaApp).repository
        setContent {
            AlmaTheme {
                CompositionLocalProvider(LocalRepository provides repo) {
                    AlmaNavHost(
                        pendingCourseId = pendingCourse.value,
                        onPendingConsumed = { pendingCourse.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(EXTRA_COURSE_ID)?.let { pendingCourse.value = it }
    }

    companion object {
        const val EXTRA_COURSE_ID = "openCourseId"
    }
}
