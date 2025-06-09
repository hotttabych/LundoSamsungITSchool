package io.whyscape.lundo.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.whyscape.lundo.data.db.MoodEntry
import io.whyscape.lundo.data.repository.GetMoodHistoryUseCase
import io.whyscape.lundo.data.repository.SaveMoodUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodTrackerViewModel @Inject constructor(
    private val saveMoodUseCase: SaveMoodUseCase,
    getMoodHistoryUseCase: GetMoodHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodTrackerUiState())
    val uiState: StateFlow<MoodTrackerUiState> = _uiState.asStateFlow()

    fun selectMood(index: Int) {
        _uiState.update { it.copy(selectedMood = index) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveMoodEntry() {
        viewModelScope.launch {
            val mood = MoodEntry(
                mood = _uiState.value.selectedMood,
                note = _uiState.value.note,
                timestamp = System.currentTimeMillis()
            )
            saveMoodUseCase(mood)
        }
    }

    val moodHistory: StateFlow<List<MoodEntry>> =
        getMoodHistoryUseCase()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
}

data class MoodTrackerUiState(
    val selectedMood: Int = -1,
    val note: String = ""
)
