package com.z1fire.alma.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.z1fire.alma.reminders.Notifier

/**
 * Returns a function that runs an action once notification permission is available — asking for it
 * first on Android 13+. [onDenied] runs if the student says no.
 */
@Composable
fun rememberNotificationGate(onDenied: () -> Unit): (() -> Unit) -> Unit {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pending?.invoke() else onDenied()
        pending = null
    }
    return { action ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifier.canNotify(context)) {
            pending = action
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }
}
