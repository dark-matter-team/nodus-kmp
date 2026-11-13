package com.darkmatter.nodus.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.iosevka_charon_bold
import nodus.shared.generated.resources.khand_regular
import org.jetbrains.compose.resources.Font

// Шрифти з макета, файли з Google Fonts (ліцензія OFL)

@Composable
fun khandFontFamily(): FontFamily = FontFamily(Font(Res.font.khand_regular, FontWeight.Normal))

@Composable
fun iosevkaCharonFontFamily(): FontFamily = FontFamily(Font(Res.font.iosevka_charon_bold, FontWeight.Bold))
