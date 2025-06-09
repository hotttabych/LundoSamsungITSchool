package io.whyscape.lundo.domain

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.plugins.HttpTimeout
import io.whyscape.lundo.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val AI_TASK_ACCESS_PREFERENCE_KEY = booleanPreferencesKey("ai_task_access")
    }

    val aiTaskAccessToggleFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[AI_TASK_ACCESS_PREFERENCE_KEY] == true }

    suspend fun setAiTaskAccessToggle(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AI_TASK_ACCESS_PREFERENCE_KEY] = value
        }
        HttpTimeout
    }

    fun getToggleFlow(key: Preferences.Key<Boolean>): Flow<Boolean> =
        context.dataStore.data.map { it[key] == true }

    suspend fun setToggle(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }
}