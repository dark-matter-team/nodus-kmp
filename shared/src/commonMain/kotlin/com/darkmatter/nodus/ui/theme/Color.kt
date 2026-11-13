package com.darkmatter.nodus.ui.theme

import androidx.compose.ui.graphics.Color

/** Кольори з макета Figma "Nodi Space", темна тема (стиль "Neon Nebula Dark"). */
object NodusColors {
    // Радіальний градієнт фону: від центру до країв
    val NebulaCore = Color(0xFF7A37C2)
    val NebulaInner = Color(0xFF5828A4)
    val NebulaMiddle = Color(0xFF33118B)
    val NebulaOuter = Color(0xFF1A0D6E)
    val NebulaEdge = Color(0xFF0D0B60)
    val NebulaDeep = Color(0xFF010952)

    val Accent = Color(0xFF4326EB)
    val StepLabel = Color(0xFF3077E2)
    val Title = Color(0xFF898DE8)
    val Description = Color(0xFF8B9AEF)
    val Outline = Color(0xFF3F51A2)
    val NextLabel = Color(0xFF6274C3)
    val StartLabel = Color(0xFFD0D9FF)
    val CardBorder = Color(0xFFAF9AE9)

    // Заливка кнопок: два шари з режимом накладання soft-light
    val NextFillTop = Color(106, 81, 232).copy(alpha = 0.47f)
    val NextFillBottom = Color(97, 85, 159).copy(alpha = 0.47f)
    val StartFillTop = Color(244, 242, 255).copy(alpha = 0.47f)
    val StartFillBottom = Color(239, 236, 255).copy(alpha = 0.47f)

    val Shadow = Color.Black.copy(alpha = 0.25f)
    val StartLabelShadow = CardBorder.copy(alpha = 0.25f)
}
