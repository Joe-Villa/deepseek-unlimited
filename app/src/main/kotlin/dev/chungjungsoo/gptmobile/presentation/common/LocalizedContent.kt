package dev.chungjungsoo.gptmobile.presentation.common

import android.content.res.Configuration
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

/**
 * Switches Compose string resources without recreating the Activity.
 * `system` leaves the resource configuration untouched.
 */
@Composable
fun LocalizedContent(languageTag: String, content: @Composable () -> Unit) {
    if (languageTag == "system" || languageTag.isBlank()) {
        content()
        return
    }
    val baseContext = LocalContext.current
    val localized = remember(languageTag, baseContext) {
        val locale = Locale.forLanguageTag(languageTag)
        val configuration = Configuration(baseContext.resources.configuration).apply {
            setLocale(locale)
        }
        baseContext.createConfigurationContext(configuration)
    }
    val configuration = localized.resources.configuration
    // Deliberately do not replace LocalContext: dialogs, activity-result launchers and
    // other Activity-scoped owners need the original ComponentActivity. Compose's
    // stringResource() reads LocalResources, so UI text still switches language.
    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalResources provides localized.resources,
        LocalLayoutDirection provides if (configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
    ) {
        content()
    }
}
