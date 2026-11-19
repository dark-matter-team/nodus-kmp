package com.darkmatter.nodus.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkmatter.nodus.ui.theme.NebulaBackground
import kotlinx.coroutines.launch
import kotlin.math.min

// Розміри з фреймів "General Onboarding N Dark Mobile" (389×874) і "General Onboarding N Dark" (1512×982)
private val CompactSizes = OnboardingSizes(
    stepFontSize = 14.sp,
    titleFontSize = 40.sp,
    descriptionFontSize = 12.sp,
    buttonFontSize = 20.sp,
    buttonSize = DpSize(104.dp, 26.dp),
    buttonsHorizontalPadding = 18.dp,
    dotSize = 15.dp,
    dotSpacing = 22.dp,
)

private val ExpandedDesignWidth = 1512.dp
private val ExpandedDesignHeight = 982.dp

private val ExpandedSizes = OnboardingSizes(
    stepFontSize = 24.sp,
    titleFontSize = 48.sp,
    descriptionFontSize = 16.sp,
    buttonFontSize = 32.sp,
    buttonSize = DpSize(282.dp, 61.dp),
    buttonsHorizontalPadding = 104.dp,
    dotSize = 20.dp,
    dotSpacing = 29.dp,
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { onboardingSlides.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingSlides.lastIndex
    val onNext: () -> Unit = {
        if (isLastPage) {
            onFinished()
        } else {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        NebulaBackground()
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            // Десктопний макет для широких вікон, мобільний для решти (зокрема телефонів у портреті)
            val isExpanded = maxWidth >= 600.dp && maxWidth > maxHeight
            if (isExpanded) {
                // Десктопний макет масштабується цілком (відступи, кнопки, шрифти) під розмір вікна,
                // тому пропорції лишаються як у фреймі 1512×982 навіть у маленькому вікні
                val scale = min(maxWidth / ExpandedDesignWidth, maxHeight / ExpandedDesignHeight)
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
                    ExpandedOnboarding(pagerState, isLastPage, onSkip = onFinished, onNext = onNext)
                }
            } else {
                CompactOnboarding(pagerState, isLastPage, onSkip = onFinished, onNext = onNext)
            }
        }
    }
}

@Composable
private fun CompactOnboarding(
    pagerState: PagerState,
    isLastPage: Boolean,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(64.dp))
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            CompactSlidePage(slide = onboardingSlides[page], step = stepLabel(page), sizes = CompactSizes)
        }
        Spacer(Modifier.height(51.dp))
        NavigationRow(pagerState, isLastPage, CompactSizes, onSkip = onSkip, onNext = onNext)
        // У макеті 81 dp до краю фрейму; частину з них тут займає системна панель навігації
        Spacer(Modifier.height(47.dp))
    }
}

@Composable
private fun ExpandedOnboarding(
    pagerState: PagerState,
    isLastPage: Boolean,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(124.dp))
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            WideSlidePage(slide = onboardingSlides[page], step = stepLabel(page), sizes = ExpandedSizes)
        }
        Spacer(Modifier.height(96.dp))
        NavigationRow(pagerState, isLastPage, ExpandedSizes, onSkip = onSkip, onNext = onNext)
        Spacer(Modifier.height(51.dp))
    }
}

@Composable
private fun NavigationRow(
    pagerState: PagerState,
    isLastPage: Boolean,
    sizes: OnboardingSizes,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().height(sizes.buttonSize.height)) {
        SkipButton(
            sizes = sizes,
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = sizes.buttonsHorizontalPadding),
        )
        PageDots(
            count = onboardingSlides.size,
            current = pagerState.currentPage,
            sizes = sizes,
            modifier = Modifier.align(Alignment.Center),
        )
        NextButton(
            isLastPage = isLastPage,
            sizes = sizes,
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = sizes.buttonsHorizontalPadding),
        )
    }
}

private fun stepLabel(page: Int) = "STEP ${page + 1}/${onboardingSlides.size}"
