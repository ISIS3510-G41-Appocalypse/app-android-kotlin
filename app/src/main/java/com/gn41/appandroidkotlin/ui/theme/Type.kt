package com.gn41.appandroidkotlin.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gn41.appandroidkotlin.R

// ─────────────────────────────────────────
// FONT FAMILIES
// Place the .ttf files inside  res/font/
// ─────────────────────────────────────────

// Lexend → headers and main action labels
val LexendFontFamily = FontFamily(
    Font(R.font.lexend_regular, FontWeight.Normal),
    Font(R.font.lexend_bold,    FontWeight.Bold)
)

// Space Grotesk → body text and secondary information
val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_medium,  FontWeight.Medium)
)

// ─────────────────────────────────────────
// TYPOGRAPHY
// ─────────────────────────────────────────

val Typography = Typography(

    // --- Headers & main actions (Lexend) ---
    titleLarge = TextStyle(
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // --- Body & secondary information (Space Grotesk) ---
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)