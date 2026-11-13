package com.darkmatter.nodus.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkmatter.nodus.ui.theme.NodusColors
import com.darkmatter.nodus.ui.theme.iosevkaCharonFontFamily
import com.darkmatter.nodus.ui.theme.khandFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.graphics.shadow.Shadow as BoxShadow

private val CardShape = RoundedCornerShape(25.dp)

/** Мобільний макет: картка 273×488, тексти під нею. */
@Composable
internal fun CompactSlidePage(
    slide: OnboardingSlide,
    step: String,
    sizes: OnboardingSizes,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        // На низьких екранах картка зменшується, зберігаючи пропорції
        SlideCard(
            image = slide.image,
            modifier = Modifier
                .weight(1f, fill = false)
                .heightIn(max = 488.dp)
                .aspectRatio(273f / 488f, matchHeightConstraintsFirst = true),
        )
        Spacer(Modifier.height(63.dp))
        SlideTexts(
            slide = slide,
            step = step,
            sizes = sizes,
            stepToTitle = 7.dp,
            titleToDescription = 7.dp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

/** Десктопний макет: альбомна картка 922×650, тексти поверх її нижньої частини. */
@Composable
internal fun WideSlidePage(
    slide: OnboardingSlide,
    step: String,
    sizes: OnboardingSizes,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        SlideCard(
            image = slide.wideImage,
            modifier = Modifier
                .widthIn(max = 922.dp)
                .heightIn(max = 650.dp)
                .aspectRatio(922f / 650f, matchHeightConstraintsFirst = true),
        ) {
            SlideTexts(
                slide = slide,
                step = step,
                sizes = sizes,
                stepToTitle = 1.dp,
                titleToDescription = 4.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 24.dp, end = 24.dp, bottom = 34.dp),
            )
        }
    }
}

/** Ілюстрація з рамкою 2 dp, радіусом 25 і внутрішньою тінню зверху. */
@Composable
private fun SlideCard(
    image: DrawableResource,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.clip(CardShape)) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(
            Modifier
                .matchParentSize()
                .innerShadow(CardShape, BoxShadow(radius = 4.dp, color = NodusColors.CardBorder, offset = DpOffset(0.dp, 4.dp)))
                .border(2.dp, NodusColors.CardBorder, CardShape),
        )
        content()
    }
}

@Composable
private fun SlideTexts(
    slide: OnboardingSlide,
    step: String,
    sizes: OnboardingSizes,
    stepToTitle: Dp,
    titleToDescription: Dp,
    modifier: Modifier = Modifier,
) {
    val khand = khandFontFamily()
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        BasicText(
            text = step,
            style = onboardingTextStyle(khand, sizes.stepFontSize, NodusColors.StepLabel, shadowOffsetY = 2.dp),
        )
        Spacer(Modifier.height(stepToTitle))
        // Заголовок завжди в один рядок: на вузьких екранах шрифт зменшується
        BasicText(
            text = slide.title,
            style = onboardingTextStyle(
                fontFamily = iosevkaCharonFontFamily(),
                fontSize = sizes.titleFontSize,
                color = NodusColors.Title,
                shadowOffsetY = 1.dp,
            ),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 20.sp, maxFontSize = sizes.titleFontSize),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(titleToDescription))
        BasicText(
            text = slide.description,
            style = onboardingTextStyle(khand, sizes.descriptionFontSize, NodusColors.Description, shadowOffsetY = 2.dp),
        )
    }
}
