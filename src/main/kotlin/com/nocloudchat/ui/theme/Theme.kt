package com.nocloudchat.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Color Scheme ─────────────────────────────────────────────────────────────

data class NoCloudChatColorScheme(
    val Background: Color,
    val Surface: Color,
    val Surface2: Color,
    val Surface3: Color,
    val Accent: Color,
    val AccentHover: Color,
    val Accent2: Color,
    val TextPrimary: Color,
    val TextMuted: Color,
    val TextDim: Color,
    val OnlineGreen: Color,
    // Outgoing bubble (colored background) — always uses white-based text
    val MessageOut: Color,
    val MessageOutText: Color,
    val MessageOutMuted: Color,
    // Incoming bubble
    val MessageIn: Color,
    val MessageInText: Color,
    val MessageInMuted: Color,
    val Border: Color,
    val BorderFocus: Color,
    val isDark: Boolean,
)

// ─── Dark Theme ───────────────────────────────────────────────────────────────

val NoCloudChatDarkColors = NoCloudChatColorScheme(
    Background      = Color(0xFF1A1A2E),
    Surface         = Color(0xFF16213E),
    Surface2        = Color(0xFF0F3460),
    Surface3        = Color(0xFF162040),
    Accent          = Color(0xFFE94560),
    AccentHover     = Color(0xFFC73050),
    Accent2         = Color(0xFFF5A623),
    TextPrimary     = Color(0xFFEEF2FF),
    TextMuted       = Color(0xFF9BA3C9),   // was #7B7F9E — brighter for readability
    TextDim         = Color(0xFF5C6485),
    OnlineGreen     = Color(0xFF4ADE80),
    MessageOut      = Color(0xFFE94560),
    MessageOutText  = Color.White,
    MessageOutMuted = Color(0xCCFFFFFF),   // 80% white — always readable on red
    MessageIn       = Color(0xFF1E2D50),
    MessageInText   = Color(0xFFEEF2FF),
    MessageInMuted  = Color(0xFF9BA3C9),
    Border          = Color(0x1AFFFFFF),
    BorderFocus     = Color(0xFFE94560),
    isDark          = true,
)

// ─── Light Theme ──────────────────────────────────────────────────────────────

val NoCloudChatLightColors = NoCloudChatColorScheme(
    Background      = Color(0xFFF0F2F9),
    Surface         = Color(0xFFFFFFFF),
    Surface2        = Color(0xFFE4E8F5),
    Surface3        = Color(0xFFD5DAF0),
    Accent          = Color(0xFFCF3050),
    AccentHover     = Color(0xFFAF2040),
    Accent2         = Color(0xFFD4820D),
    TextPrimary     = Color(0xFF1A1F3E),
    TextMuted       = Color(0xFF5A6280),
    TextDim         = Color(0xFF8890B5),
    OnlineGreen     = Color(0xFF16A34A),
    MessageOut      = Color(0xFFCF3050),
    MessageOutText  = Color.White,
    MessageOutMuted = Color(0xCCFFFFFF),
    MessageIn       = Color(0xFFE4E8F5),
    MessageInText   = Color(0xFF1A1F3E),
    MessageInMuted  = Color(0xFF5A6280),
    Border          = Color(0x1A000000),
    BorderFocus     = Color(0xFFCF3050),
    isDark          = false,
)

// ─── CompositionLocal ─────────────────────────────────────────────────────────

val LocalNoCloudChatColors = compositionLocalOf { NoCloudChatDarkColors }

/** Access the current theme colors from any composable. */
val NoCloudChatColors: NoCloudChatColorScheme
    @Composable get() = LocalNoCloudChatColors.current

// ─── Typography ───────────────────────────────────────────────────────────────

val NoCloudChatTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
    ),
)

// ─── Theme composable ─────────────────────────────────────────────────────────

@Composable
fun AppTheme(isDark: Boolean, content: @Composable () -> Unit) {
    val colors = if (isDark) NoCloudChatDarkColors else NoCloudChatLightColors
    val materialColors = if (isDark) {
        darkColorScheme(
            primary          = colors.Accent,
            onPrimary        = Color.White,
            secondary        = colors.Accent2,
            onSecondary      = Color(0xFF1A1A2E),
            background       = colors.Background,
            onBackground     = colors.TextPrimary,
            surface          = colors.Surface,
            onSurface        = colors.TextPrimary,
            surfaceVariant   = colors.Surface2,
            onSurfaceVariant = colors.TextMuted,
            outline          = colors.Border,
            error            = Color(0xFFCF6679),
        )
    } else {
        lightColorScheme(
            primary          = colors.Accent,
            onPrimary        = Color.White,
            secondary        = colors.Accent2,
            onSecondary      = Color.White,
            background       = colors.Background,
            onBackground     = colors.TextPrimary,
            surface          = colors.Surface,
            onSurface        = colors.TextPrimary,
            surfaceVariant   = colors.Surface2,
            onSurfaceVariant = colors.TextMuted,
            outline          = colors.Border,
            error            = Color(0xFFE53935),
        )
    }
    CompositionLocalProvider(LocalNoCloudChatColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography  = NoCloudChatTypography,
            content     = content,
        )
    }
}

