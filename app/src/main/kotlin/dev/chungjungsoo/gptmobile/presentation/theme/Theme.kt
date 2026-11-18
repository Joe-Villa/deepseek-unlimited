package dev.chungjungsoo.gptmobile.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.chungjungsoo.gptmobile.data.model.DynamicTheme
import dev.chungjungsoo.gptmobile.data.model.ThemeMode

@Immutable
data class ExtendedColorScheme(
    val customColor1: ColorFamily,
    val chatGPTOfficialColor: ColorFamily,
    val customColor2: ColorFamily
)

// DeepSeek brand palette (see README / design tokens).
// One cobalt, one pale blue, pure white canvas, hairline dividers — nothing else.
private val DsCanvas = Color(0xFFFFFFFF)
private val DsSurfaceSecondary = Color(0xFFF7F8FA)
private val DsCobalt = Color(0xFF4D6BFE)
private val DsPaleBlue = Color(0xFFEEF2FF)
private val DsTextPrimary = Color(0xFF15171B)
private val DsTextSecondary = Color(0xFF777B84)
private val DsDivider = Color(0xFFE7E9ED)
private val DsError = Color(0xFFD84C4C)

private val DsDarkCanvas = Color(0xFF0F0F0F)
private val DsDarkSurface = Color(0xFF242424)
private val DsDarkSurfaceHigh = Color(0xFF2A2A2A)
private val DsDarkCobalt = Color(0xFF4D6BFE)
private val DsDarkPaleBlue = Color(0xFF1F2B4D)
private val DsDarkTextPrimary = Color(0xFFFFFFFF)
private val DsDarkTextSecondary = Color(0xFFA6A6A6)
private val DsDarkDivider = Color(0xFF333333)

private val lightScheme = lightColorScheme(
    primary = DsCobalt,
    onPrimary = Color.White,
    primaryContainer = DsPaleBlue,
    onPrimaryContainer = DsTextPrimary,
    secondary = DsTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = DsSurfaceSecondary,
    onSecondaryContainer = DsTextPrimary,
    tertiary = DsCobalt,
    onTertiary = Color.White,
    tertiaryContainer = DsPaleBlue,
    onTertiaryContainer = DsTextPrimary,
    error = DsError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = DsCanvas,
    onBackground = DsTextPrimary,
    surface = DsCanvas,
    onSurface = DsTextPrimary,
    surfaceVariant = DsSurfaceSecondary,
    onSurfaceVariant = DsTextSecondary,
    outline = DsDivider,
    outlineVariant = DsDivider,
    scrim = Color.Black,
    inverseSurface = DsTextPrimary,
    inverseOnSurface = DsCanvas,
    inversePrimary = Color(0xFF8FA8FF),
    surfaceDim = Color(0xFFF1F2F5),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFBFC),
    surfaceContainer = DsSurfaceSecondary,
    surfaceContainerHigh = Color(0xFFF2F3F6),
    surfaceContainerHighest = Color(0xFFEDEFF3)
)

private val darkScheme = darkColorScheme(
    primary = DsDarkCobalt,
    onPrimary = Color.White,
    primaryContainer = DsDarkPaleBlue,
    onPrimaryContainer = DsDarkTextPrimary,
    secondary = DsDarkTextSecondary,
    onSecondary = Color(0xFF17181C),
    secondaryContainer = DsDarkSurface,
    onSecondaryContainer = DsDarkTextPrimary,
    tertiary = DsDarkCobalt,
    onTertiary = Color.White,
    tertiaryContainer = DsDarkPaleBlue,
    onTertiaryContainer = DsDarkTextPrimary,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DsDarkCanvas,
    onBackground = DsDarkTextPrimary,
    surface = DsDarkCanvas,
    onSurface = DsDarkTextPrimary,
    surfaceVariant = DsDarkSurface,
    onSurfaceVariant = DsDarkTextSecondary,
    outline = DsDarkDivider,
    outlineVariant = DsDarkDivider,
    scrim = Color.Black,
    inverseSurface = DsDarkTextPrimary,
    inverseOnSurface = DsDarkCanvas,
    inversePrimary = Color(0xFF3A55C8),
    surfaceDim = Color(0xFF0A0A0A),
    surfaceBright = Color(0xFF303030),
    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = Color(0xFF1A1A1A),
    surfaceContainer = Color(0xFF1E1E1E),
    surfaceContainerHigh = DsDarkSurfaceHigh,
    surfaceContainerHighest = Color(0xFF303030)
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast
)

val extendedLight = ExtendedColorScheme(
    customColor1 = ColorFamily(
        customColor1Light,
        onCustomColor1Light,
        customColor1ContainerLight,
        onCustomColor1ContainerLight
    ),
    chatGPTOfficialColor = ColorFamily(
        chatGPTOfficialColorLight,
        onChatGPTOfficialColorLight,
        chatGPTOfficialColorContainerLight,
        onChatGPTOfficialColorContainerLight
    ),
    customColor2 = ColorFamily(
        customColor2Light,
        onCustomColor2Light,
        customColor2ContainerLight,
        onCustomColor2ContainerLight
    )
)

val extendedDark = ExtendedColorScheme(
    customColor1 = ColorFamily(
        customColor1Dark,
        onCustomColor1Dark,
        customColor1ContainerDark,
        onCustomColor1ContainerDark
    ),
    chatGPTOfficialColor = ColorFamily(
        chatGPTOfficialColorDark,
        onChatGPTOfficialColorDark,
        chatGPTOfficialColorContainerDark,
        onChatGPTOfficialColorContainerDark
    ),
    customColor2 = ColorFamily(
        customColor2Dark,
        onCustomColor2Dark,
        customColor2ContainerDark,
        onCustomColor2ContainerDark
    )
)

val extendedLightMediumContrast = ExtendedColorScheme(
    customColor1 = ColorFamily(
        customColor1LightMediumContrast,
        onCustomColor1LightMediumContrast,
        customColor1ContainerLightMediumContrast,
        onCustomColor1ContainerLightMediumContrast
    ),
    chatGPTOfficialColor = ColorFamily(
        chatGPTOfficialColorLightMediumContrast,
        onChatGPTOfficialColorLightMediumContrast,
        chatGPTOfficialColorContainerLightMediumContrast,
        onChatGPTOfficialColorContainerLightMediumContrast
    ),
    customColor2 = ColorFamily(
        customColor2LightMediumContrast,
        onCustomColor2LightMediumContrast,
        customColor2ContainerLightMediumContrast,
        onCustomColor2ContainerLightMediumContrast
    )
)

val extendedLightHighContrast = ExtendedColorScheme(
    customColor1 = ColorFamily(
        customColor1LightHighContrast,
        onCustomColor1LightHighContrast,
        customColor1ContainerLightHighContrast,
        onCustomColor1ContainerLightHighContrast
    ),
    chatGPTOfficialColor = ColorFamily(
        chatGPTOfficialColorLightHighContrast,
        onChatGPTOfficialColorLightHighContrast,
        chatGPTOfficialColorContainerLightHighContrast,
        onChatGPTOfficialColorContainerLightHighContrast
    ),
    customColor2 = ColorFamily(
        customColor2LightHighContrast,
        onCustomColor2LightHighContrast,
        customColor2ContainerLightHighContrast,
        onCustomColor2ContainerLightHighContrast
    )
)

val extendedDarkMediumContrast = ExtendedColorScheme(
    customColor1 = ColorFamily(
        customColor1DarkMediumContrast,
        onCustomColor1DarkMediumContrast,
        customColor1ContainerDarkMediumContrast,
        onCustomColor1ContainerDarkMediumContrast
    ),
    chatGPTOfficialColor = ColorFamily(
        chatGPTOfficialColorDarkMediumContrast,
        onChatGPTOfficialColorDarkMediumContrast,
        chatGPTOfficialColorContainerDarkMediumContrast,
        onChatGPTOfficialColorContainerDarkMediumContrast
    ),
    customColor2 = ColorFamily(
        customColor2DarkMediumContrast,
        onCustomColor2DarkMediumContrast,
        customColor2ContainerDarkMediumContrast,
        onCustomColor2ContainerDarkMediumContrast
    )
)

val extendedDarkHighContrast = ExtendedColorScheme(
    customColor1 = ColorFamily(
        customColor1DarkHighContrast,
        onCustomColor1DarkHighContrast,
        customColor1ContainerDarkHighContrast,
        onCustomColor1ContainerDarkHighContrast
    ),
    chatGPTOfficialColor = ColorFamily(
        chatGPTOfficialColorDarkHighContrast,
        onChatGPTOfficialColorDarkHighContrast,
        chatGPTOfficialColorContainerDarkHighContrast,
        onChatGPTOfficialColorContainerDarkHighContrast
    ),
    customColor2 = ColorFamily(
        customColor2DarkHighContrast,
        onCustomColor2DarkHighContrast,
        customColor2ContainerDarkHighContrast,
        onCustomColor2ContainerDarkHighContrast
    )
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified,
    Color.Unspecified,
    Color.Unspecified,
    Color.Unspecified
)

internal val AppShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun GPTMobileTheme(
    dynamicTheme: DynamicTheme = DynamicTheme.OFF,
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val useDynamicColor = dynamicTheme == DynamicTheme.ON
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        useDynamicColor -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        useDarkTheme -> darkScheme

        else -> lightScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
                window.isStatusBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDarkTheme
                isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
