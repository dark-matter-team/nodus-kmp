package com.darkmatter.nodus.ui.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkmatter.nodus.ui.theme.NodusColors
import com.darkmatter.nodus.ui.theme.iosevkaCharonFontFamily
import com.darkmatter.nodus.ui.theme.khandFontFamily
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.auth_trophy

private val TrophyIllustration = ResetIllustration(
    image = Res.drawable.auth_trophy,
    compactSize = DpSize(301.dp, 227.dp),
    expandedSize = DpSize(673.dp, 507.6.dp),
    expandedOffsetX = 2.7.dp,
)

/** Фрейми "Forgot Password L Mobile" (успіх) і "Forgot Password D-2". */
@Composable
fun PasswordChangedScreen(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ResetScreenScaffold(
        illustration = TrophyIllustration,
        compactGap = 28.3.dp,
        contentOffsetX = (-21).dp,
        modifier = modifier,
    ) { spec ->
        val expanded = spec.isExpanded
        // Ширина як у кнопки: заголовок переноситься так само, як у макеті (3 рядки на телефоні, 2 на десктопі)
        Column(Modifier.width(if (expanded) 498.dp else 324.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val headerHeight = if (expanded) 166.9.dp else 197.7.dp
            Box(Modifier.fillMaxWidth().height(headerHeight), contentAlignment = Alignment.TopCenter) {
                BasicText(
                    text = "PASSWORD HAS CHANGED SUCCESSFULLY",
                    style = resetTextStyle(
                        fontFamily = iosevkaCharonFontFamily(),
                        fontSize = if (expanded) 48.sp else 40.sp,
                        color = NodusColors.SuccessTitle,
                        fontWeight = FontWeight.Bold,
                        shadowOffsetY = 1.dp,
                    ),
                    modifier = Modifier.fillMaxWidth().baselineSlot(headerHeight, if (expanded) 48.7.dp else 40.6.dp),
                )
                BasicText(
                    text = "Your password has been successfully reset.",
                    style = resetTextStyle(khandFontFamily(), 16.sp, NodusColors.Description, shadowOffsetY = 2.dp),
                    modifier = Modifier.baselineSlot(headerHeight, if (expanded) 135.65.dp else 169.45.dp),
                )
            }
            AuthAccentButton(
                text = "Sign In",
                isLoading = false,
                isExpanded = expanded,
                onClick = onSignIn,
                height = if (expanded) 80.dp else 46.dp,
                fontSize = spec.buttonFontSize,
            )
        }
    }
}
