package com.nocloudchat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nocloudchat.ui.theme.NoCloudChatColors

@Composable
fun SecretJoinDialog(onDismiss: () -> Unit, onJoin: (String) -> Unit) {
    var input by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(NoCloudChatColors.Surface)
                .padding(28.dp)
                .width(360.dp)
        ) {
            Column {
                Text(
                    "🔐 Protected Network",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NoCloudChatColors.TextPrimary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "A peer on this network requires a passphrase. Enter it to connect.",
                    fontSize = 14.sp,
                    color = NoCloudChatColors.TextMuted,
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Network passphrase",
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
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NoCloudChatColors.TextMuted,
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NoCloudChatColors.Border),
                    ) {
                        Text("Skip", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = { if (input.isNotBlank()) onJoin(input) },
                        enabled = input.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoCloudChatColors.Accent,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("Join", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
