// SPDX-License-Identifier: GPL-3.0-or-later

package com.otasks.app.ui.theme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

object AppType {
    val titleLg =
        TextStyle(fontFamily = Lora, fontWeight = FontWeight.Medium, fontSize = Tokens.Type.titleLg, lineHeight = Tokens.Type.titleLgLine)
    val titleMd =
        TextStyle(fontFamily = Lora, fontWeight = FontWeight.Medium, fontSize = Tokens.Type.titleMd, lineHeight = Tokens.Type.titleMdLine)
    val body = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = Tokens.Type.body, lineHeight = Tokens.Type.bodyLine)
    val bodySm =
        TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = Tokens.Type.bodySm, lineHeight = Tokens.Type.bodySmLine)
    val caption =
        TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = Tokens.Type.caption, lineHeight = Tokens.Type.captionLine)
    val label =
        TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = Tokens.Type.label, lineHeight = Tokens.Type.labelLine)
    val numberLg =
        TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = Tokens.Type.numberLg,
            lineHeight = Tokens.Type.numberLgLine,
            fontFeatureSettings = "tnum",
        )
}

val AppTypography =
    Typography(
        displayLarge = AppType.numberLg,
        displayMedium = AppType.numberLg,
        displaySmall = AppType.numberLg,
        headlineLarge = AppType.titleLg,
        headlineMedium = AppType.titleLg,
        headlineSmall = AppType.titleMd,
        titleLarge = AppType.titleLg,
        titleMedium = AppType.titleMd,
        titleSmall = AppType.body,
        bodyLarge = AppType.body,
        bodyMedium = AppType.bodySm,
        bodySmall = AppType.caption,
        labelLarge = AppType.label,
        labelMedium = AppType.label,
        labelSmall = AppType.label,
    )
