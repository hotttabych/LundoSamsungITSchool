package io.whyscape.lundo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.whyscape.lundo.R

@Composable
fun PinCodeScreen(
    onPinAccepted: (String) -> Boolean,
    modifier: Modifier = Modifier,
    isNewPin: Boolean = false
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isNewPin) stringResource(R.string.come_up_with_a_pin) else stringResource(R.string.enter_pin),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 16) pin = it
            },
            label = { Text(stringResource(R.string.pin_field_label)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            )
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(text = it, color = Color.Red)
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (pin.length < 4) {
                error = context.getString(R.string.pin_too_short)
            } else {
                if (!onPinAccepted(pin)) {
                    error(context.getString(R.string.pin_incorrect))
                }
            }
        }) {
            Text(stringResource(R.string.ok))
        }
    }
}