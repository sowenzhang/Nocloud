package com.nocloudchat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nocloudchat.state.AppState
import com.nocloudchat.ui.theme.NoCloudChatColors
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(state: AppState, onDismiss: () -> Unit, onOpenAbout: () -> Unit = {}, onShowTutorial: () -> Unit = {}) {
    val myName by state.myName.collectAsState()
    val isDark by state.isDarkMode.collectAsState()
    val secretEnabled by state.networkSecretEnabled.collectAsState()
    var nameInput by remember(myName) { mutableStateOf(myName) }
    var passphraseInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    fun save() {
        if (nameInput.isNotBlank()) {
            scope.launch { state.setDisplayName(nameInput) }
            if (secretEnabled && passphraseInput.isNotBlank()) {
                state.setNetworkSecret(passphraseInput)
            }
            onDismiss()
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(NoCloudChatColors.Surface)
                .padding(28.dp)
                .width(400.dp)
        ) {
            Column {
                Text(
                    "⚙ Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NoCloudChatColors.TextPrimary,
                )

                Spacer(Modifier.height(24.dp))

                // Display name
                Text(
                    "DISPLAY NAME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = NoCloudChatColors.TextMuted,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 32) nameInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onKeyEvent { e ->
                            when {
                                e.type == KeyEventType.KeyDown && e.key == Key.Enter -> { save(); true }
                                e.type == KeyEventType.KeyDown && e.key == Key.Escape -> { onDismiss(); true }
                                else -> false
                            }
                        },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoCloudChatColors.Accent,
                        unfocusedBorderColor = NoCloudChatColors.Border,
                        focusedTextColor = NoCloudChatColors.TextPrimary,
                        unfocusedTextColor = NoCloudChatColors.TextPrimary,
                        cursorColor = NoCloudChatColors.Accent,
                        focusedContainerColor = NoCloudChatColors.Background,
                        unfocusedContainerColor = NoCloudChatColors.Background,
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                )

                Spacer(Modifier.height(24.dp))

                // Appearance
                Text(
                    "APPEARANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = NoCloudChatColors.TextMuted,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (isDark) "Dark mode" else "Light mode",
                        fontSize = 14.sp,
                        color = NoCloudChatColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = isDark,
                        onCheckedChange = { state.setDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NoCloudChatColors.Accent,
                            uncheckedThumbColor = NoCloudChatColors.TextMuted,
                            uncheckedTrackColor = NoCloudChatColors.Surface2,
                        ),
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Security
                Text(
                    "SECURITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = NoCloudChatColors.TextMuted,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Network passphrase",
                            fontSize = 14.sp,
                            color = NoCloudChatColors.TextPrimary,
                        )
                        Text(
                            text = "Only peers with the same passphrase can connect",
                            fontSize = 11.sp,
                            color = NoCloudChatColors.TextMuted,
                        )
                    }
                    Switch(
                        checked = secretEnabled,
                        onCheckedChange = { state.setNetworkSecretEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NoCloudChatColors.Accent,
                            uncheckedThumbColor = NoCloudChatColors.TextMuted,
                            uncheckedTrackColor = NoCloudChatColors.Surface2,
                        ),
                    )
                }
                AnimatedVisibility(visible = secretEnabled) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passphraseInput,
                            onValueChange = { passphraseInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Enter network passphrase",
                                    color = NoCloudChatColors.TextMuted,
                                    fontSize = 14.sp,
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NoCloudChatColors.Accent,
                                unfocusedBorderColor = NoCloudChatColors.Border,
                                focusedTextColor = NoCloudChatColors.TextPrimary,
                                unfocusedTextColor = NoCloudChatColors.TextPrimary,
                                cursorColor = NoCloudChatColors.Accent,
                                focusedContainerColor = NoCloudChatColors.Background,
                                unfocusedContainerColor = NoCloudChatColors.Background,
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Peer ID (read-only info)
                Text(
                    "YOUR PEER ID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = NoCloudChatColors.TextMuted,
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NoCloudChatColors.Background)
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.myId,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NoCloudChatColors.TextMuted,
                    )
                }

                Spacer(Modifier.height(28.dp))

                // Buttons
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = onOpenAbout) {
                        Text(
                            "About",
                            fontSize = 12.sp,
                            color = NoCloudChatColors.TextMuted,
                        )
                    }
                    TextButton(onClick = { onDismiss(); onShowTutorial() }) {
                        Text(
                            "Show tutorial",
                            fontSize = 12.sp,
                            color = NoCloudChatColors.TextMuted,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NoCloudChatColors.TextMuted,
                        ),
                        border = BorderStroke(1.dp, NoCloudChatColors.Border),
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = ::save,
                        enabled = nameInput.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoCloudChatColors.Accent,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
