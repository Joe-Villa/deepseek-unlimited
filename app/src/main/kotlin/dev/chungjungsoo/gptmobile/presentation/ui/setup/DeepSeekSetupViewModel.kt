package dev.chungjungsoo.gptmobile.presentation.ui.setup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.data.context.CompactionStore
import dev.chungjungsoo.gptmobile.data.context.ModelCapacity
import dev.chungjungsoo.gptmobile.data.database.entity.PlatformV2
import dev.chungjungsoo.gptmobile.data.model.ClientType
import dev.chungjungsoo.gptmobile.data.repository.SettingRepository
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The only setup this app ever asks for: a DeepSeek API key.
 *
 * No account, no login, no provider picker, no model picker, no pricing tiers.
 * Paste the key, press one button, start talking.
 */
@HiltViewModel
class DeepSeekSetupViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingRepository: SettingRepository,
    private val compactionStore: CompactionStore
) : ViewModel() {

    data class UiState(
        val apiKey: String = "",
        val isSaving: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onApiKeyChange(value: String) {
        _uiState.update { it.copy(apiKey = value, error = null) }
    }

    fun save(onComplete: () -> Unit) {
        val key = _uiState.value.apiKey.trim()
        if (key.isBlank()) {
            _uiState.update { it.copy(error = context.getString(R.string.ds_setup_empty_key)) }
            return
        }
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            when (checkKey(key)) {
                KeyCheck.REJECTED -> {
                    _uiState.update {
                        it.copy(isSaving = false, error = context.getString(R.string.ds_setup_bad_key))
                    }
                    return@launch
                }

                else -> Unit
            }

            runCatching {
                val platform = PlatformV2(
                    name = "DeepSeek",
                    compatibleType = ClientType.OPENAI,
                    enabled = true,
                    apiUrl = DEEPSEEK_API_URL,
                    token = key,
                    model = DEEPSEEK_MODEL,
                    stream = true
                )
                settingRepository.addPlatformV2(platform)
                // Tell the context manager how much room DeepSeek actually has.
                // Without this, a custom endpoint reports "unknown capacity" and the
                // first message would be blocked by a "enter context window" prompt.
                // A failure here must not undo a perfectly good key, so it stays separate.
                runCatching {
                    compactionStore.saveCapacity(
                        ModelCapacity(
                            platformUid = platform.uid,
                            endpoint = platform.apiUrl,
                            model = platform.model,
                            overrideContextWindowTokens = DEEPSEEK_CONTEXT_WINDOW
                        )
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                onComplete()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = context.getString(
                            R.string.ds_setup_save_failed,
                            throwable.message ?: context.getString(R.string.ds_unknown_error)
                        )
                    )
                }
            }
        }
    }

    /**
     * Ask DeepSeek whether it recognises this key.
     *
     * A clear "no" (401/403) blocks the user with a friendly message.
     * Anything else — network down, endpoint missing — lets them through,
     * because being offline is not the same as having a bad key.
     */
    private suspend fun checkKey(key: String): KeyCheck = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL("$DEEPSEEK_API_URL/models").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $key")
                connectTimeout = 15_000
                readTimeout = 15_000
            }
            try {
                when (connection.responseCode) {
                    401, 403 -> KeyCheck.REJECTED
                    in 200..299 -> KeyCheck.ACCEPTED
                    else -> KeyCheck.UNKNOWN
                }
            } finally {
                connection.disconnect()
            }
        }.getOrDefault(KeyCheck.UNKNOWN)
    }

    private enum class KeyCheck { ACCEPTED, REJECTED, UNKNOWN }

    companion object {
        const val DEEPSEEK_API_URL = "https://api.deepseek.com/v1"
        const val DEEPSEEK_MODEL = "deepseek-flash"
        const val GET_KEY_URL = "https://platform.deepseek.com/api_keys"
        const val DEEPSEEK_CONTEXT_WINDOW = 1_000_000
    }
}
