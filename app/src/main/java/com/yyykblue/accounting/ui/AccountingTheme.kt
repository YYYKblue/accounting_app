package com.yyykblue.accounting.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AccountingColors = lightColorScheme(
    primary = Color(0xFFE85D3F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBD1),
    onPrimaryContainer = Color(0xFF3B0901),
    secondary = Color(0xFF3E6656),
    background = Color(0xFFFFF9F1),
    surface = Color(0xFFFFF9F1),
    surfaceVariant = Color(0xFFF4EDE5),
)

@Composable
fun AccountingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AccountingColors,
        content = content,
    )
}
