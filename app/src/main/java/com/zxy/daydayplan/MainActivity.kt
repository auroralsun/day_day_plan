package com.zxy.daydayplan

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.zxy.daydayplan.data.local.AppDatabase
import com.zxy.daydayplan.data.repository.DayPlanRepositoryImpl
import com.zxy.daydayplan.data.repository.UiPreferencesRepository
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.startUpdateFlowForResult
import com.zxy.daydayplan.reminder.ReminderScheduler
import com.zxy.daydayplan.ui.DayDayPlanApp
import com.zxy.daydayplan.ui.MainViewModel
import com.zxy.daydayplan.ui.theme.DayDayPlanTheme

class MainActivity : ComponentActivity() {
    private lateinit var appUpdateManager: AppUpdateManager

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "day_day_plan.db"
        ).addMigrations(AppDatabase.Migration1To2).build()
    }

    private val viewModel by viewModels<MainViewModel> {
        MainViewModel.factory(
            repository = DayPlanRepositoryImpl(
                scheduleDao = database.scheduleDao(),
                todoDao = database.todoDao(),
                dailyScheduleTemplateDao = database.dailyScheduleTemplateDao()
            ),
            preferencesRepository = UiPreferencesRepository(applicationContext),
            reminderScheduler = ReminderScheduler(applicationContext)
        )
    }

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {}

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureNotificationPermission()
        ensureExactAlarmPermission()
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        checkForAppUpdate()
        setContent {
            DayDayPlanTheme {
                DayDayPlanApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeUpdateIfNeeded()
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isImmediateUpdateAllowed
            ) {
                startImmediateUpdate(appUpdateInfo)
            }
        }
    }

    private fun resumeUpdateIfNeeded() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateUpdate(appUpdateInfo)
            }
        }
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        }
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun ensureExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val alarmManager = getSystemService(AlarmManager::class.java) ?: return
        if (alarmManager.canScheduleExactAlarms()) return

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
        }
        runCatching { startActivity(intent) }
    }
}
