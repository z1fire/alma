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
import com.z1fire.alma.data.Meeting
import com.z1fire.alma.data.assignmentsDueOn
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.department
import com.z1fire.alma.data.meetingsOn
import com.z1fire.alma.data.nextMeetingAfter
import com.z1fire.alma.ui.dayName
import com.z1fire.alma.ui.formatMonthDay
import com.z1fire.alma.ui.formatTime
import com.z1fire.alma.ui.formatTimeRange
import com.z1fire.alma.ui.theme.deptColor
import java.time.LocalDateTime

// Same collegiate palette as the app, in day/night pairs.
private val Bg = ColorProvider(day = Color(0xFFFBF8F1), night = Color(0xFF1E2229))
private val Ink = ColorProvider(day = Color(0xFF1C1B17), night = Color(0xFFE6E2D9))
private val Muted = ColorProvider(day = Color(0xFF6B6455), night = Color(0xFF9EA2AA))
private val Accent = ColorProvider(day = Color(0xFF7A2232), night = Color(0xFFF0B3BC))
private val Now = ColorProvider(day = Color(0xFF1F3A5F), night = Color(0xFFA9C7F0))

/** Home-screen widget listing today's class meetings, or the next one when today is free. */
class TodayWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = (context.applicationContext as AlmaApp).repository
        provideContent {
            val data by repo.state.collectAsState()
            Content(context, data, LocalDateTime.now())
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
private fun Content(context: Context, data: AppData, now: LocalDateTime) {
    val today = now.toLocalDate()
    val nowMinute = now.hour * 60 + now.minute
    val meetings = data.meetingsOn(today)
    val dueToday = data.assignmentsDueOn(today).count { !it.second.done }

    Column(
        GlanceModifier
            .fillMaxSize()
            .background(Bg)
            .cornerRadius(20.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .clickable(openApp(context)),
    ) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("TODAY", style = TextStyle(color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold))
            Spacer(GlanceModifier.width(6.dp))
            Text(
                "${dayName(today.dayOfWeek.value)}, ${formatMonthDay(today)}",
                style = TextStyle(color = Muted, fontSize = 12.sp),
                modifier = GlanceModifier.defaultWeight(),
            )
            if (dueToday > 0) {
                Text("$dueToday due", style = TextStyle(color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold))
            }
        }
        Spacer(GlanceModifier.height(6.dp))

        if (meetings.isEmpty()) {
            Text("No classes today", style = TextStyle(color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold))
            val next = data.nextMeetingAfter(now)
            Text(
                if (next == null) "Nothing on the timetable — enroll in a course to fill it." else
                    "Next: ${dayName(next.date.dayOfWeek.value)} ${formatTime(next.meeting.startMinute)} · " +
                        "${data.codeOf(next.course)} ${next.meeting.label}",
                style = TextStyle(color = Muted, fontSize = 12.sp),
                maxLines = 2,
                modifier = if (next == null) GlanceModifier else GlanceModifier.clickable(openApp(context, next.course.id)),
            )
        } else {
            LazyColumn(GlanceModifier.fillMaxWidth().defaultWeight()) {
                items(meetings, itemId = { it.second.id.hashCode().toLong() }) { (course, meeting) ->
                    MeetingRow(context, data, course, meeting, nowMinute)
                }
            }
        }
    }
}

@Composable
private fun MeetingRow(context: Context, data: AppData, course: Course, meeting: Meeting, nowMinute: Int) {
    val end = meeting.startMinute + meeting.durationMinutes
    val over = nowMinute >= end
    val live = nowMinute in meeting.startMinute until end
    val bar = deptColor(data.department(course.departmentId)?.colorIndex)
    Row(
        GlanceModifier.fillMaxWidth().padding(vertical = 4.dp).clickable(openApp(context, course.id)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            GlanceModifier
                .width(4.dp)
                .height(38.dp)
                .cornerRadius(2.dp)
                .background(if (over) bar.copy(alpha = 0.35f) else bar),
        ) {}
        Spacer(GlanceModifier.width(8.dp))
        Column(GlanceModifier.defaultWeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatTimeRange(meeting.startMinute, meeting.durationMinutes), style = TextStyle(color = Muted, fontSize = 11.sp))
                if (live) {
                    Spacer(GlanceModifier.width(6.dp))
                    Text("NOW", style = TextStyle(color = Now, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                }
            }
            Text(
                "${data.codeOf(course)} · ${meeting.label}",
                style = TextStyle(color = if (over) Muted else Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            val sub = listOf(course.title, meeting.location).filter { it.isNotBlank() }.joinToString(" · ")
            Text(sub, style = TextStyle(color = Muted, fontSize = 11.sp), maxLines = 1)
        }
    }
}
