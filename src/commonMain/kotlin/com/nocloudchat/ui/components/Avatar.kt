package com.nocloudchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocloudchat.ui.theme.NoCloudChatColors

private val PALETTE = listOf(
    Pair(Color(0xFF6C63FF), Color(0xFF4D44D4)),
    Pair(Color(0xFFE94560), Color(0xFFC73050)),
    Pair(Color(0xFFF5A623), Color(0xFFD48920)),
    Pair(Color(0xFF4ADE80), Color(0xFF2ECC71)),
    Pair(Color(0xFF38BDF8), Color(0xFF0EA5E9)),
    Pair(Color(0xFFF472B6), Color(0xFFDB2777)),
)

private fun avatarColors(id: String): Pair<Color, Color> {
    if (id.isEmpty()) return PALETTE[0]
    var h = 0
    for (c in id) h = (h * 31 + c.code) and Int.MAX_VALUE
    return PALETTE[h % PALETTE.size]
}

fun nameInitials(name: String): String =
    name.trim().split("\\s+".toRegex())
        .map { it.firstOrNull()?.uppercaseChar() ?: ' ' }
        .take(2)
        .joinToString("")
        .ifBlank { "?" }

@Composable
fun Avatar(
    name: String,
    id: String,
    size: Dp = 36.dp,
    isSelf: Boolean = false,
) {
    val (c1, c2) = if (isSelf)
        Pair(NoCloudChatColors.Accent, NoCloudChatColors.Accent2)
    else
        avatarColors(id)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(c1, c2)))
    ) {
        Text(
            text = nameInitials(name),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp,
        )
    }
}
