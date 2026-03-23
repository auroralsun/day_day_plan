package com.zxy.daydayplan.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Overview("overview", "总览", Icons.Outlined.Home),
    Schedule("schedule", "日程", Icons.Outlined.DateRange),
    Todo("todo", "待办", Icons.AutoMirrored.Outlined.EventNote),
    Completed("completed", "已完成", Icons.Outlined.CheckCircle),
    Review("review", "回顾", Icons.Outlined.History);

    companion object {
        fun fromRoute(route: String?): AppTab {
            return entries.firstOrNull { it.route == route } ?: Overview
        }
    }
}
