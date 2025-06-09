package io.whyscape.lundo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.whyscape.lundo.R
import io.whyscape.lundo.domain.model.TestData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestBottomSheet(test: TestData, onDismiss: () -> Unit) {
    val currentIndex = remember { mutableIntStateOf(0) }
    val score = remember { mutableIntStateOf(0) }
    val showExplanation = remember { mutableStateOf(false) }
    val selectedAnswer = remember { mutableStateOf<String?>(null) }
    val isFinished = remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = test.title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (isFinished.value) {
            Text(
                text = stringResource(R.string.test_complete),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.test_result, score.intValue, test.questions.size),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        } else {
            val currentQuestion = test.questions[currentIndex.intValue]

            Text(text = currentQuestion.text, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(4.dp))

            currentQuestion.answers.forEach { answer ->
                val isCorrect = answer == currentQuestion.correctAnswer
                val isSelected = selectedAnswer.value == answer
                val backgroundColor = when {
                    showExplanation.value && isSelected && isCorrect -> Color.Green.copy(alpha = 0.2f)
                    showExplanation.value && isSelected && !isCorrect -> Color.Red.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.secondary
                }

                Button(
                    onClick = {
                        if (!showExplanation.value) {
                            selectedAnswer.value = answer
                            showExplanation.value = true
                            if (isCorrect) score.intValue++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = answer,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            if (showExplanation.value) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.test_answer_explanation, currentQuestion.explanation),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        showExplanation.value = false
                        selectedAnswer.value = null
                        if (currentIndex.intValue < test.questions.lastIndex) {
                            currentIndex.intValue++
                        } else {
                            isFinished.value = true
                        }
                    }
                ) {
                    Text(stringResource(R.string.next_button))
                }
            }
        }
    }
}