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

    val Shadow = Color.Black.copy(alpha = 0.25f)
    val StartLabelShadow = CardBorder.copy(alpha = 0.25f)

    // Екрани входу й реєстрації (Figma: Sign In / Sign Up Dark)
    val AuthTitle = Color(0xFFECECEC)
    val FieldFill = Color(153, 132, 255).copy(alpha = 0.47f)
    val FieldPlaceholder = Color(0xFF394FAE)
    val PrimaryFill = Color(41, 23, 128).copy(alpha = 0.47f)
    val PrimaryLabel = Color(0xFF92A4F4)
    val GoogleFill = Color(206, 199, 241).copy(alpha = 0.33f)
    val GoogleLabel = Color(0xFFA880ED)
    val SecondaryBorderDark = Color(0xFF1A1E61)
    val SecondaryBorderLight = Color(0xFFBE91E3)

    // Введеного тексту немає в макетах: колір підпису START з онбордингу
    val FieldText = StartLabel

    // Рамка поля в режимі color-dodge; у фокусі світліша (Figma: Forgot Password, New Password)
    val FieldBorder = Color(0xFF0E1782)
    val FieldBorderFocused = Color(0xFF5561E1)

    // Помилка з макета "Invalid Recovery code": червона рамка, червонувата заливка soft-light і текст
    val ErrorBorder = Color(0xFF920215)
    val ErrorFill = Color(0xFFFF0000).copy(alpha = 0.47f)
    val ErrorText = Color(0xFFF1BBC2)

    // Відновлення пароля
    val Link = Color(0xFF4455D5)
    val Hint = Color(0xFFC5CEF6)
    val CodeMessage = Color(0xFFBCC6FF)
    val SuccessTitle = Color(0xFFC7CEEF)

    // Кнопка Save Password і Sign In на екрані успіху: два шари soft-light. Нижній шар
    // на мобільному світлий, на десктопі фіолетовий; верхній однаковий
    val AccentButtonFillBottomCompact = Color(0xFFE6E1FF).copy(alpha = 0.47f)
    val AccentButtonFillBottomExpanded = Color(0xFF5F0077).copy(alpha = 0.47f)
    val AccentButtonFillTop = Color(0xFF7B67DF).copy(alpha = 0.47f)
}
