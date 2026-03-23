package com.zxy.daydayplan

import com.zxy.daydayplan.domain.model.ScheduleItem
import com.zxy.daydayplan.domain.model.parseTimeOrNull
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanModelsTest {
    @Test
    fun `parseTimeOrNull parses valid time`() {
        assertNotNull("09:30".parseTimeOrNull())
    }

    @Test
    fun `schedule is ongoing within interval`() {
        val item = ScheduleItem(
            date = LocalDate.of(2026, 3, 19),
            title = "Write plan",
            startTime = "09:00",
            endTime = "10:00"
        )

        assertTrue(item.isOngoing(LocalTime.of(9, 30)))
        assertFalse(item.isOngoing(LocalTime.of(10, 30)))
    }

    @Test
    fun `schedule becomes overdue only after end time`() {
        val item = ScheduleItem(
            date = LocalDate.of(2026, 3, 19),
            title = "Standup",
            startTime = "09:00",
            endTime = "10:00"
        )

        assertFalse(item.isOverdue(LocalTime.of(10, 0)))
        assertTrue(item.isOverdue(LocalTime.of(10, 1)))
    }

    @Test
    fun `schedule before or during current slot is still pending now or later until overdue`() {
        val item = ScheduleItem(
            date = LocalDate.of(2026, 3, 19),
            title = "Write summary",
            startTime = "09:00",
            endTime = "10:00"
        )

        assertTrue(item.isPendingNowOrLater(LocalTime.of(8, 30)))
        assertTrue(item.isPendingNowOrLater(LocalTime.of(9, 30)))
        assertFalse(item.isPendingNowOrLater(LocalTime.of(10, 1)))
    }
}
