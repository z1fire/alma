package com.z1fire.alma.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.z1fire.alma.AlmaApp
import com.z1fire.alma.MainActivity
import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.active
import com.z1fire.alma.data.streak
import com.z1fire.alma.data.weekMinutes
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.theme.deptColor
import java.time.LocalDate

// Same palette as the app, in day/night pairs.
private val Bg = ColorProvider(day = Color(0xFFFBF8F1), night = Color(0xFF1E2229))
private val Ink = ColorProvider(day = Color(0xFF1C1B17), night = Color(0xFFE6E2D9))
private val Muted = ColorProvider(day = Color(0xFF6B6455), night = Color(0xFF9EA2AA))
private val Accent = ColorProvider(day = Color(0xFF7A2232), night = Color(0xFFF0B3BC))
private val Bar = ColorProvider(day = Color(0xFF1F3A5F), night = Color(0xFFA9C7F0))
private val Track = ColorProvider(day = Color(0x221F3A5F), night = Color(0x33A9C7F0))

/**
 * Home-screen widget: study time this week against the goal, and the courses you're studying.
 * (Class name kept from v1 so widgets already on the home screen keep working.)
 */
class TodayWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = (context.applicationContext as AlmaApp).repository
        provideContent {
            val data by repo.state.collectAsState()
            Content(context, data, LocalDate.now())
        }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

private fun openApp(context: Context, courseId: String? = null): Action {
    val intent = Intent(context, MainActivity::class.java)
    if (courseId != null) {
        intent.putExtra(MainActivity.EXTRA_COURSE_ID, courseId)
        // Distinct data keeps each row's PendingIntent from collapsing into one.
        intent.data = Uri.parse("alma://course/$courseId")
    }
    return actionStartActivity(intent)
}

@Composable
private fun Content(context: Context, data: AppData, today: LocalDate) {
    val minutes = data.weekMinutes(today)
    val goal = data.profile.weeklyGoalMinutes.coerceAtLeast(1)
    val streak = data.streak(today)

    Column(
        GlanceModifier
            .fillMaxSize()
            .background(Bg)
            .cornerRadius(20.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .clickable(openApp(context)),
    ) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("THIS WEEK", style = TextStyle(color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold))
            Spacer(GlanceModifier.width(6.dp))
            Text(
                "${formatMinutes(minutes)} of ${formatMinutes(goal)}",
                style = TextStyle(color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.defaultWeight(),
            )
            if (streak > 0) Text("$streak-day streak", style = TextStyle(color = Accent, fontSize = 11.sp))
        }
        Spacer(GlanceModifier.height(4.dp))
        LinearProgressIndicator(
            progress = (minutes.toFloat() / goal).coerceIn(0f, 1f),
            modifier = GlanceModifier.fillMaxWidth().height(6.dp),
            color = Bar,
            backgroundColor = Track,
        )
        Spacer(GlanceModifier.height(6.dp))

        val courses = data.active
        if (courses.isEmpty()) {
            Text("Nothing in progress", style = TextStyle(color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Text("Open Alma to start a course.", style = TextStyle(color = Muted, fontSize = 12.sp))
        } else {
            LazyColumn(GlanceModifier.fillMaxWidth().defaultWeight()) {
                items(courses, itemId = { it.id.hashCode().toLong() }) { course -> CourseRow(context, course, today) }
            }
        }
    }
}

@Composable
private fun CourseRow(context: Context, course: Course, today: LocalDate) {
    Row(
        GlanceModifier.fillMaxWidth().padding(vertical = 4.dp).clickable(openApp(context, course.id)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(GlanceModifier.width(4.dp).height(32.dp).cornerRadius(2.dp).background(deptColor(course.colorIndex))) {}
        Spacer(GlanceModifier.width(8.dp))
        Column(GlanceModifier.defaultWeight()) {
            Text(course.title, style = TextStyle(color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold), maxLines = 1)
            val parts = listOfNotNull(
                course.weekMinutes(today).takeIf { it > 0 }?.let { "${formatMinutes(it)} this week" },
                course.items.takeIf { it.isNotEmpty() }?.let { "${course.doneCount}/${it.size} done" },
            )
            Text(parts.ifEmpty { listOf("Not started this week") }.joinToString(" · "), style = TextStyle(color = Muted, fontSize = 11.sp), maxLines = 1)
        }
    }
}
