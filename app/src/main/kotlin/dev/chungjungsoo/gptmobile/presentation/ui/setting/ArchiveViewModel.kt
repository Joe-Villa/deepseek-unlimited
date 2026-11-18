package dev.chungjungsoo.gptmobile.presentation.ui.setting

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.data.archive.ChatArchiveManager
import dev.chungjungsoo.gptmobile.data.archive.ChatArchiveProgress
import dev.chungjungsoo.gptmobile.data.archive.ChatArchiveResult
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val archiveManager: ChatArchiveManager
) : ViewModel() {

    data class UiState(
        val isWorking: Boolean = false,
        val progressLabel: String = "",
        val current: Int = 0,
        val total: Int = 0,
        val result: ChatArchiveResult? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun importArchive(uri: Uri) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { UiState(isWorking = true, progressLabel = context.getString(R.string.ds_archive_read_zip)) }
            try {
                val result = archiveManager.importZip(uri) { progress -> publishProgress(progress) }
                _state.update { it.copy(isWorking = false, result = result) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (throwable: Throwable) {
                _state.update { it.copy(isWorking = false, error = throwable.message ?: context.getString(R.string.ds_archive_import_failed)) }
            }
        }
    }

    fun exportArchive(uri: Uri) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { UiState(isWorking = true, progressLabel = context.getString(R.string.ds_archive_prepare_export)) }
            try {
                val result = archiveManager.exportZip(uri) { progress -> publishProgress(progress) }
                _state.update { it.copy(isWorking = false, result = result) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (throwable: Throwable) {
                _state.update { it.copy(isWorking = false, error = throwable.message ?: context.getString(R.string.ds_archive_export_failed)) }
            }
        }
    }

    fun clearResult() {
        _state.update { it.copy(result = null, error = null) }
    }

    private fun publishProgress(progress: ChatArchiveProgress) {
        _state.update {
            it.copy(
                progressLabel = progress.label,
                current = progress.current,
                total = progress.total
            )
        }
    }
}
