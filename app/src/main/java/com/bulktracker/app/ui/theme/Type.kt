package com.bulktracker.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.googlefonts.*
import com.bulktracker.app.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val lora = GoogleFont("Lora")
private val inter = GoogleFont("Inter")

val SerifFamily = FontFamily(
    Font(googleFont = lora, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = lora, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = lora, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(familyName = DeviceFontFamilyName("serif"), weight = FontWeight.Normal),
    Font(familyName = DeviceFontFamilyName("serif"), weight = FontWeight.Medium),
    Font(familyName = DeviceFontFamilyName("serif"), weight = FontWeight.SemiBold)
)

val SansFamily = FontFamily(
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(familyName = DeviceFontFamilyName("sans-serif"), weight = FontWeight.Normal),
    Font(familyName = DeviceFontFamilyName("sans-serif"), weight = FontWeight.Medium),
    Font(familyName = DeviceFontFamilyName("sans-serif"), weight = FontWeight.SemiBold)
)

// Type tokens (family / size / line height / weight):
// titleLg Serif 26sp/32sp Medium
val TitleLg = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Medium,
    fontSize = Tokens.TitleLgSize,
    lineHeight = Tokens.TitleLgLineHeight,
    fontFeatureSettings = "tnum"
)

// titleMd Serif 18sp/24sp Medium
val TitleMd = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Medium,
    fontSize = Tokens.TitleMdSize,
    lineHeight = Tokens.TitleMdLineHeight,
    fontFeatureSettings = "tnum"
)

// body Sans 16sp/24sp Regular
val Body = TextStyle(
    fontFamily = SansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = Tokens.BodySize,
    lineHeight = Tokens.BodyLineHeight,
    fontFeatureSettings = "tnum"
)

// bodySm Sans 15sp/22sp Regular
val BodySm = TextStyle(
    fontFamily = SansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = Tokens.BodySmSize,
    lineHeight = Tokens.BodySmLineHeight,
    fontFeatureSettings = "tnum"
)

// caption Sans 13sp/18sp Regular
val Caption = TextStyle(
    fontFamily = SansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = Tokens.CaptionSize,
    lineHeight = Tokens.CaptionLineHeight,
    fontFeatureSettings = "tnum"
)

// label Sans 12sp/16sp Medium
val Label = TextStyle(
    fontFamily = SansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = Tokens.LabelSize,
    lineHeight = Tokens.LabelLineHeight,
    fontFeatureSettings = "tnum"
)

private val defaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = SansFamily, fontFeatureSettings = "tnum"),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = SansFamily, fontFeatureSettings = "tnum"),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = SansFamily, fontFeatureSettings = "tnum"),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = SansFamily, fontFeatureSettings = "tnum"),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = SansFamily, fontFeatureSettings = "tnum"),
    headlineSmall = TitleLg,
    titleLarge = TitleLg,
    titleMedium = TitleMd,
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = SansFamily, fontFeatureSettings = "tnum"),
    bodyLarge = Body,
    bodyMedium = BodySm,
    bodySmall = Caption,
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = SansFamily, fontFeatureSettings = "tnum"),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = SansFamily, fontFeatureSettings = "tnum"),
    labelSmall = Label
)
