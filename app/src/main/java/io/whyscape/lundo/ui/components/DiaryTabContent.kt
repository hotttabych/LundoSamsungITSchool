package io.whyscape.lundo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.whyscape.lundo.ui.viewModel.DiaryViewModel

@Composable
fun DiaryTabContent(
    viewModel: DiaryViewModel,
    modifier: Modifier = Modifier
) {
    val isDbReady by viewModel.isDbReady.collectAsState()
    val isPinSet by viewModel.isPinSet.collectAsState()

    if (!isDbReady) {
        PinCodeScreen(
            onPinAccepted = { pin ->
                viewModel.onPinEntered(pin)
            },
            modifier = modifier,
            isNewPin = !isPinSet
        )
    } else {
        NotesScreen(
            viewModel = viewModel
        )
    }
}