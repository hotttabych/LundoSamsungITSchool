package io.whyscape.lundo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.whyscape.lundo.R
import io.whyscape.lundo.data.db.MoodEntry
import io.whyscape.lundo.ui.viewModel.MoodTrackerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    moodTrackerViewModel: MoodTrackerViewModel
) {
    val trackerUiState by moodTrackerViewModel.uiState.collectAsState()
    val moodHistory by moodTrackerViewModel.moodHistory.collectAsState(initial = emptyList())
    val moodOptions = listOf("😄", "😊", "😐", "😞", "😭")

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = stringResource(R.string.mood),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)) {
            item {
                Text(
                    stringResource(R.string.how_are_you_feeling_today),
                    style = MaterialTheme.typography.headlineSmall
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    moodOptions.forEachIndexed { index, emoji ->
                        Text(
                            text = emoji,
                            fontSize = 30.sp,
                            modifier = Modifier
                                .clickable { moodTrackerViewModel.selectMood(index) }
                                .padding(8.dp)
                                .background(
                                    if (trackerUiState.selectedMood == index) MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.3f
                                    )
                                    else Color.Transparent,
                                    shape = CircleShape
                                )
                                .padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = trackerUiState.note,
                    onValueChange = { moodTrackerViewModel.updateNote(it) },
                    label = { Text(stringResource(R.string.add_note)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { moodTrackerViewModel.saveMoodEntry() }
                ) {
                    Text(stringResource(R.string.record))
                }

                Spacer(Modifier.height(32.dp))
                Text(stringResource(R.string.mood_history), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
            }

            items(moodHistory) { entry ->
                MoodEntryCard(entry)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun MoodEntryCard(entry: MoodEntry) {
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy – HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(timestamp))
    }

    fun moodEmoji(mood: Int): String = listOf("😄", "😊", "😐", "😞", "😭").getOrElse(mood) { "❓" }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatDate(entry.timestamp),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = moodEmoji(entry.mood),
                fontSize = 32.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            if (entry.note.isNotBlank()) {
                Text(entry.note, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

}