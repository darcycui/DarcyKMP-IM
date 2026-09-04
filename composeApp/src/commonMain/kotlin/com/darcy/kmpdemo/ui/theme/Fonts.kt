package com.darcy.kmpdemo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import darcykmp_im.composeapp.generated.resources.PingFang_Light
import darcykmp_im.composeapp.generated.resources.PingFang_Medium
import darcykmp_im.composeapp.generated.resources.PingFang_Regular
import darcykmp_im.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

/**
 * 自定义字体
 */
@Composable
fun customFonts(): Typography {
    val robotoFontFamily = FontFamily(
        Font(Res.font.PingFang_Regular, FontWeight.Normal),
        Font(Res.font.PingFang_Light, FontWeight.Light),
        Font(Res.font.PingFang_Medium, FontWeight.Medium),
    )
    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Medium
            ),
            displayMedium = displayMedium.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Normal
            ),
            displaySmall = displaySmall.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Light
            ),

            headlineLarge = headlineLarge.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Medium
            ),
            headlineMedium = headlineMedium.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Normal
            ),
            headlineSmall = headlineSmall.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Light
            ),

            bodyLarge = bodyLarge.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Medium
            ),
            bodyMedium = bodyMedium.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Normal
            ),
            bodySmall = bodySmall.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Light
            ),

            titleLarge = titleLarge.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Medium
            ),
            titleMedium = titleMedium.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Normal
            ),
            titleSmall = titleSmall.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Light
            ),

            labelLarge = labelLarge.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Medium
            ),
            labelMedium = labelMedium.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Normal
            ),
            labelSmall = labelSmall.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Light
            ),
        )
    }
}