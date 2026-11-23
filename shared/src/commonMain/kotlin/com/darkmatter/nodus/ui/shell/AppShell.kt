
package com.darkmatter.nodus.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkmatter.nodus.ui.screens.CatalogScreen
import com.darkmatter.nodus.ui.theme.khandFontFamily
import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.ic_moon
import nodus.shared.generated.resources.ic_sun
import org.jetbrains.compose.resources.painterResource

// Від цієї ширини вікна показуємо бічне меню (десктоп), менше: нижню панель (телефон)
private val DesktopBreakpoint = 600.dp

// Набір кольорів оболонки для однієї теми. Значення позначені "≈" оцінені зі скриншотів: звірити з Figma
private class ShellPalette(
    val sidebar: Color,
    val bottomBar: Color,
    val activeStart: Color,
    val activeEnd: Color,
    val inactiveFill: Color,
    val activeText: Color,
    val inactiveText: Color,
    val avatarFill: Color,
    val avatarBorder: Color,
)

private val DarkPalette = ShellPalette(
    sidebar = Color(0xFF7A70C7).copy(alpha = 0.20f),
    bottomBar = Color(0xFF7A70C7).copy(alpha = 0.20f),
    activeStart = Color(0xFF7B5BB8),   // ≈
    activeEnd = Color(0xFF3A1D55),     // ≈
    inactiveFill = Color(0xFF1B0C7A),  // ≈
    activeText = Color(0xFFE6DFFA),    // ≈
    inactiveText = Color(0xFF7F77C9),  // ≈
    avatarFill = Color(0xFF10086E),    // ≈
    avatarBorder = Color(0xFF7B6FD0),  // ≈
)

private val LightPalette = ShellPalette(
    sidebar = Color(0xFFB051D3).copy(alpha = 0.60f),     // ≈ (як нижня панель)
    bottomBar = Color(0xFFB051D3).copy(alpha = 0.60f),   // з Figma: B051D3, 60%
    activeStart = Color(0xFF8A4CB8),   // ≈
    activeEnd = Color(0xFF5E2D8A),     // ≈
    inactiveFill = Color(0xFFBF6EE3),  // ≈
    activeText = Color(0xFFF3E8FB),    // ≈
    inactiveText = Color(0xFFDDB6F2),  // ≈
    avatarFill = Color(0xFFF3E1FA),    // ≈
    avatarBorder = Color(0xFF5B2A86),  // ≈
)

@Composable
fun AppShell(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    var selectedTab by remember { mutableStateOf(ShellTab.GoalLab) }
    val palette = if (isDarkTheme) DarkPalette else LightPalette

    // Surface дає фон із теми й правильний колір тексту
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (maxWidth >= DesktopBreakpoint) {
                Row(modifier = Modifier.fillMaxSize()) {
                    SideMenu(
                        selected = selectedTab,
                        onSelect = { selectedTab = it },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        username = "USERNAME", // TODO: підставити ім'я з GET /api/users/me, коли буде API-клієнт
                        palette = palette,
                    )
                    TabContent(tab = selectedTab, modifier = Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                // statusBarsPadding: вміст опускається під системний рядок із годинником (на десктопі = 0)
                Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    TabContent(tab = selectedTab, modifier = Modifier.weight(1f).fillMaxWidth())
                    BottomBar(selected = selectedTab, onSelect = { selectedTab = it }, palette = palette)
                }
            }
        }
    }
}

@Composable
private fun TabContent(tab: ShellTab, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        when (tab) {
            ShellTab.GoalLab -> Text("Goal Lab (заглушка)")
            ShellTab.StarMap -> CatalogScreen(onOpenSettings = {})
            ShellTab.Focus -> Text("Focus (заглушка)")
            ShellTab.Profile -> Text("Profile (заглушка)")
        }
    }
}

@Composable
private fun SideMenu(
    selected: ShellTab,
    onSelect: (ShellTab) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    username: String,
    palette: ShellPalette,
) {
    val khand = khandFontFamily()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(palette.sidebar)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Аватар і ім'я: клік відкриває Profile (окремого пункту для нього в меню нема)
        Column(
            modifier = Modifier.clickable { onSelect(ShellTab.Profile) }.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(palette.avatarFill)
                    .border(2.dp, palette.avatarBorder, CircleShape),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = username,
                color = if (selected == ShellTab.Profile) palette.activeText else palette.inactiveText,
                fontFamily = khand,
                fontSize = 22.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Лише три вкладки: Profile відкривається через аватар
        Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
            ShellTab.entries.filter { it != ShellTab.Profile }.forEach { tab ->
                SideMenuItem(
                    tab = tab,
                    selected = tab == selected,
                    onClick = { onSelect(tab) },
                    palette = palette,
                )
            }
        }

        // weight(1f) забирає весь вільний простір і штовхає перемикач униз
        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onToggleTheme) {
            Icon(
                painter = painterResource(if (isDarkTheme) Res.drawable.ic_sun else Res.drawable.ic_moon),
                contentDescription = null,
                tint = palette.activeText,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isDarkTheme) "LIGHT MODE" else "DARK MODE",
                color = palette.activeText,
                fontFamily = khand,
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
private fun SideMenuItem(
    tab: ShellTab,
    selected: Boolean,
    onClick: () -> Unit,
    palette: ShellPalette,
) {
    val shape = RoundedCornerShape(10.dp)
    val khand = khandFontFamily()
    val contentColor = if (selected) palette.activeText else palette.inactiveText
    // Активний пункт: градієнт; неактивний: суцільна заливка
    val fill: Brush = if (selected) {
        Brush.horizontalGradient(listOf(palette.activeStart, palette.activeEnd))
    } else {
        SolidColor(palette.inactiveFill)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(fill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(tab.icon),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(40.dp),
        )
        Text(
            text = tab.title.uppercase(),
            color = contentColor,
            fontFamily = khand,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
        )
    }
}

@Composable
private fun BottomBar(
    selected: ShellTab,
    onSelect: (ShellTab) -> Unit,
    palette: ShellPalette,
) {
    // Спершу background, потім navigationBarsPadding: фон тягнеться до низу екрана,
    // а іконки піднімаються над смужкою жестів
    Row(
        modifier = Modifier.fillMaxWidth().background(palette.bottomBar).navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShellTab.entries.forEach { tab ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(tab.icon),
                    contentDescription = tab.title,
                    tint = if (tab == selected) palette.activeText else palette.inactiveText,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}
