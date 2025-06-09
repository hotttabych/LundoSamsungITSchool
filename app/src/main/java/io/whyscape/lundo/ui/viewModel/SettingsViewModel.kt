package io.whyscape.lundo.ui.viewModel

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.whyscape.lundo.domain.DataStoreManager
import io.whyscape.lundo.domain.DataStoreManager.Companion.AI_TASK_ACCESS_PREFERENCE_KEY
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val aiTaskAccessModeToggle = dataStoreManager.getToggleFlow(AI_TASK_ACCESS_PREFERENCE_KEY)

    fun updateToggle(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setToggle(key, value)
        }
    }
}
