package dev.chungjungsoo.gptmobile.presentation.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chungjungsoo.gptmobile.data.localization.LanguagePreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val preferences: LanguagePreferences
) : ViewModel() {
    val languageTag: StateFlow<String> = preferences.languageTag

    fun setLanguage(tag: String) {
        viewModelScope.launch { preferences.setLanguage(tag) }
    }
}
