package com.zxy.daydayplan.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zxy.daydayplan.ui.AppTab
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UiPreferencesRepository(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { File(context.filesDir, "ui_prefs.preferences_pb") }
    )

    val selectedTab: Flow<AppTab> = dataStore.data.map { preferences ->
        val value = preferences[SelectedTabKey] ?: AppTab.Overview.route
        AppTab.fromRoute(value)
    }

    suspend fun setSelectedTab(tab: AppTab) {
        dataStore.edit { preferences ->
            preferences[SelectedTabKey] = tab.route
        }
    }

    private companion object {
        val SelectedTabKey = stringPreferencesKey("selected_tab")
    }
}
