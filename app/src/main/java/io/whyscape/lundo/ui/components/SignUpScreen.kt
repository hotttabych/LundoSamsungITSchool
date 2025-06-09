package io.whyscape.lundo.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.jeziellago.compose.markdowntext.MarkdownText
import io.whyscape.lundo.R
import io.whyscape.lundo.data.db.UserEntity
import io.whyscape.lundo.ui.viewModel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    onRegistrationComplete: () -> Unit,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var userInput by remember { mutableStateOf("") }

    val chatMessages = remember {
        mutableStateListOf<Pair<String, Boolean>>(
            context.getString(R.string.signup_greeting) to false
        )
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .windowInsetsPadding(WindowInsets.systemBars)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("android.resource://io.whyscape.lundo/drawable/luno")
                    .crossfade(true)
                    .build(),
                contentDescription = "Luno avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.luno_name), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.online), style = MaterialTheme.typography.labelSmall)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            itemsIndexed(chatMessages) { index, (text, isUser) ->
                ChatBubble(text = text, isUser = isUser)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (step == 1) {
                item {
                    UploadPhotoSection(
                        photoUri = photoUri,
                        onPhotoSelected = {
                            photoUri = it
                            chatMessages.add(context.getString(R.string.signup_are_you_ready_msg) to false)
                            step++
                        },
                        onSkip = {
                            chatMessages.add(context.getString(R.string.signup_are_you_ready_msg) to false)
                            step++
                        }
                    )
                }
            }

            if (step == 2) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                userViewModel.updateUser(
                                    UserEntity(
                                        id = 1,
                                        name = userName,
                                        handle = null,
                                        title = getLevelTitle(1),
                                        lastUsageTimestamp = System.currentTimeMillis(),
                                        coins = 0,
                                        photoUri = photoUri?.toString()
                                    )
                                )
                                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    .edit { putBoolean("registration_complete", true) }
                                onRegistrationComplete()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.start_button))
                    }
                }
            }
        }

        if (step == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.enter_your_name)) },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    if (userInput.isNotBlank()) {
                        chatMessages.add(userInput to true)
                        userName = userInput
                        userInput = ""
                        step++
                        chatMessages.add(context.getString(R.string.signup_pfp_invite_msg) to false)
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.plane_bold_duotone),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(R.string.send)
                    )
                }
            }
        }
    }
}

fun getLevelTitle(level: Int): String {
    val levelTitles = listOf(
        "Seeker of Change",
        "Awakening Mind",
        "Aspiring Soul",
        "Courageous Beginner",
        "Mindful Initiate",
        "Disciplined Wanderer",
        "Focused Dreamer",
        "Purposeful Striver",
        "Humble Warrior",
        "Empowered Learner",
        "Clarity Seeker",
        "Balance Keeper",
        "Inner Alchemist",
        "Pathfinder",
        "Serene Achiever",
        "Resilient Guardian",
        "Luminous Heart",
        "Boundless Spirit",
        "Wisdom Bearer",
        "Self-Mastery Sage"
    )

    return if (level in 1..levelTitles.size) {
        levelTitles[level - 1]
    } else {
        "Transcendent Explorer"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val bubbleColor =
        if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val alignment = if (isUser) Arrangement.End else Arrangement.Start
    val cornerShape = if (isUser)
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    else
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Column(
            modifier = Modifier
                .background(bubbleColor, cornerShape)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            MarkdownText(
                markdown = text,
                style = MaterialTheme.typography.bodyLarge.copy(color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary)
            )
        }
    }
}

@Composable
fun UploadPhotoSection(
    photoUri: Uri?,
    onPhotoSelected: (Uri) -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { onPhotoSelected(it) } }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (photoUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.profile_picture_title),
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable { launcher.launch("image/*") }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.tap_to_change),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text(stringResource(R.string.next_button), color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            stringResource(R.string.photo_upload_title),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { launcher.launch("image/*") }) {
                            Text(stringResource(R.string.upload_profile_photo_button))
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onSkip) {
                            Text(stringResource(R.string.skip_button), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}