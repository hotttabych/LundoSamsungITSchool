package io.whyscape.lundo.ui.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.whyscape.lundo.common.PinUtils
import io.whyscape.lundo.common.RepositoryInitializer
import io.whyscape.lundo.data.db.NoteEntity
import io.whyscape.lundo.domain.usecase.DiaryUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repoInitializer: RepositoryInitializer,
    private val diaryUseCases: DiaryUseCases,
    @ApplicationContext context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("diary_prefs", Context.MODE_PRIVATE)

    private val _isDbReady = MutableStateFlow(false)
    val isDbReady: StateFlow<Boolean> = _isDbReady

    private val _isPinSet = MutableStateFlow(isPinAlreadySet())
    val isPinSet: StateFlow<Boolean> = _isPinSet

    var notes by mutableStateOf<List<NoteEntity>>(emptyList())
        private set

    private fun isPinAlreadySet(): Boolean {
        return sharedPrefs.contains("pin_hash") && sharedPrefs.getString("pin_hash", "")?.isNotEmpty() == true
    }

    private fun setPinHash(pinHash: String) {
        sharedPrefs.edit { putString("pin_hash", pinHash) }
        _isPinSet.value = true
    }

    private fun getPinHash(): String? = sharedPrefs.getString("pin_hash", null)

    fun onPinEntered(pin: String): Boolean {
        val pinHash = PinUtils.hash(pin)

        if (isPinAlreadySet()) {
            val storedHash = getPinHash()
            if (storedHash != pinHash) {
                return false
            }
        } else {
            setPinHash(pinHash)
        }

        val passphrase = PinUtils.derivePassphrase(pin)
        repoInitializer.initSecureDb(passphrase)
        _isDbReady.value = true
        loadNotes()
        return true
    }

    fun loadNotes() {
        viewModelScope.launch {
            notes = diaryUseCases.getAllNotes()
        }
    }

    fun addNote(content: String) {
        viewModelScope.launch {
            diaryUseCases.insertNote(NoteEntity(content = content))
            loadNotes()
        }
    }

    fun updateNote(updatedNote: NoteEntity) {
        val index = notes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            notes = notes.toMutableList().apply {
                this[index] = updatedNote
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            diaryUseCases.deleteNote(note)
            loadNotes()
        }
    }
}