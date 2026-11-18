package dev.chungjungsoo.gptmobile.data.localization

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class LanguagePreferences @Inject constructor(
    @param:ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("deepseek_language", Context.MODE_PRIVATE)

    private val _languageTag = MutableStateFlow(prefs.getString(KEY, "system") ?: "system")
    val languageTag: StateFlow<String> = _languageTag.asStateFlow()

    fun setLanguage(tag: String) {
        val safeTag = if (SupportedLanguages.all.any { it.tag == tag }) tag else "system"
        prefs.edit { putString(KEY, safeTag) }
        _languageTag.value = safeTag
    }

    private companion object {
        const val KEY = "language_tag"
    }
}
