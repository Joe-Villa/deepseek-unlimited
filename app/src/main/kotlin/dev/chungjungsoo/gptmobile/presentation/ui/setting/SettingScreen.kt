package dev.chungjungsoo.gptmobile.presentation.ui.setting

import dev.chungjungsoo.gptmobile.R

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import dev.chungjungsoo.gptmobile.data.localization.SupportedLanguages
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chungjungsoo.gptmobile.BuildConfig
import dev.chungjungsoo.gptmobile.data.database.entity.PlatformV2
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * The whole settings surface: connect, local data, about.
 *
 * Deliberately no account, quota, theme, language, voice, update check or logout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModelV2 = hiltViewModel(),
    onNavigationClick: () -> Unit,
    onNavigateToAboutPage: () -> Unit,
    onNavigateToSetup: () -> Unit = {}
) {
    val platforms by settingViewModel.platformState.collectAsStateWithLifecycle()
    val chatCount by settingViewModel.chatCount.collectAsStateWithLifecycle()
    val platform = platforms.firstOrNull()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var showKeyDialog by rememberSaveable { mutableStateOf(false) }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val languageViewModel: LanguageViewModel = hiltViewModel()
    val languageTag by languageViewModel.languageTag.collectAsStateWithLifecycle()
    val language = SupportedLanguages.byTag(languageTag)
    val archiveViewModel: ArchiveViewModel = hiltViewModel()
    val archiveState by archiveViewModel.state.collectAsStateWithLifecycle()
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) archiveViewModel.importArchive(uri)
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) archiveViewModel.exportArchive(uri)
    }
    val exportName = remember {
        "deepseek_data-${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}.zip"
    }

    LaunchedEffect(archiveState.result) {
        if (archiveState.result != null) settingViewModel.refreshChatCount()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        settingViewModel.fetchPlatforms()
        settingViewModel.refreshChatCount()
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ds_settings),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigationClick) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.ds_back),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionLabel(stringResource(R.string.ds_settings_app))
            SettingsCard {
                SettingRow(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.ds_settings_language),
                    subtitle = "",
                    value = if (languageTag == "system") {
                        stringResource(R.string.ds_language_system)
                    } else {
                        language.nativeName
                    },
                    onClick = { showLanguageDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 48.dp))
                SettingRow(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.ds_settings_appearance),
                    subtitle = "",
                    value = stringResource(R.string.ds_settings_dark),
                    onClick = {
                        Toast.makeText(context, context.getString(R.string.ds_settings_dark_fixed), Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 48.dp))
                SettingRow(
                    icon = Icons.Outlined.FormatSize,
                    title = stringResource(R.string.ds_settings_font_size),
                    subtitle = "",
                    value = stringResource(R.string.ds_settings_standard),
                    onClick = {
                        Toast.makeText(context, context.getString(R.string.ds_settings_font_standard), Toast.LENGTH_SHORT).show()
                    }
                )
            }

            SectionLabel(stringResource(R.string.ds_settings_voice))
            SettingsCard {
                SettingRow(
                    icon = Icons.Outlined.RecordVoiceOver,
                    title = stringResource(R.string.ds_settings_voice_tone),
                    subtitle = "",
                    value = stringResource(R.string.ds_settings_system_default),
                    onClick = {
                        Toast.makeText(context, context.getString(R.string.ds_settings_tts_system), Toast.LENGTH_SHORT).show()
                    }
                )
            }

            SectionLabel(stringResource(R.string.ds_settings_connection))
            SettingsCard {
                SettingRow(
                    icon = Icons.Outlined.Key,
                    title = "API Key",
                    subtitle = if (platform != null) stringResource(R.string.ds_settings_connected_local) else stringResource(R.string.ds_settings_not_configured),
                    value = platform?.token?.takeIf { it.isNotBlank() }?.let { maskKey(context, it) } ?: stringResource(R.string.ds_settings_not_configured_short),
                    onClick = {
                        if (platform == null) onNavigateToSetup() else showKeyDialog = true
                    }
                )
            }

            SectionLabel(stringResource(R.string.ds_settings_local_data))
            SettingsCard {
                SettingRow(
                    icon = Icons.Outlined.Storage,
                    title = stringResource(R.string.ds_settings_storage),
                    subtitle = stringResource(R.string.ds_settings_storage_desc),
                    value = stringResource(R.string.ds_settings_chat_count, chatCount),
                    showChevron = false,
                    onClick = {}
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
                SettingRow(
                    icon = Icons.Outlined.FileUpload,
                    title = stringResource(R.string.ds_settings_import),
                    subtitle = stringResource(R.string.ds_settings_import_desc),
                    onClick = {
                        importLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
                SettingRow(
                    icon = Icons.Outlined.FileDownload,
                    title = stringResource(R.string.ds_settings_export),
                    subtitle = stringResource(R.string.ds_settings_export_desc),
                    onClick = { exportLauncher.launch(exportName) }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
                SettingRow(
                    icon = Icons.Outlined.DeleteOutline,
                    title = stringResource(R.string.ds_settings_clear),
                    subtitle = stringResource(R.string.ds_settings_clear_desc),
                    danger = true,
                    showChevron = false,
                    onClick = { showClearDialog = true }
                )
            }

            SectionLabel(stringResource(R.string.ds_settings_about))
            SettingsCard {
                SettingRow(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.ds_settings_check_update),
                    subtitle = "",
                    value = BuildConfig.VERSION_NAME,
                    onClick = {
                        Toast.makeText(context, context.getString(R.string.ds_settings_latest, BuildConfig.VERSION_NAME), Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
                SettingRow(
                    icon = Icons.Outlined.Description,
                    title = stringResource(R.string.ds_settings_terms),
                    subtitle = "",
                    onClick = onNavigateToAboutPage
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
                SettingRow(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.ds_settings_about_disclaimer),
                    subtitle = stringResource(R.string.ds_settings_about_desc),
                    onClick = onNavigateToAboutPage
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.ds_settings_model_name),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(R.string.ds_settings_ai_footer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }

    if (showKeyDialog && platform != null) {
        ApiKeyDialog(
            platform = platform,
            onDismiss = { showKeyDialog = false },
            onCopy = {
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("api-key", platform.token.orEmpty())))
                    Toast.makeText(context, context.getString(R.string.ds_settings_key_copied), Toast.LENGTH_SHORT).show()
                }
            },
            onSave = { key ->
                settingViewModel.updateApiKey(platform, key) {
                    showKeyDialog = false
                    Toast.makeText(context, context.getString(R.string.ds_settings_key_updated), Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.ds_settings_clear_title)) },
            text = { Text(stringResource(R.string.ds_settings_clear_msg, chatCount)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    settingViewModel.clearAllChats {
                        Toast.makeText(context, context.getString(R.string.ds_settings_cleared), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(stringResource(R.string.ds_settings_clear_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(stringResource(R.string.ds_cancel)) }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.ds_language_title)) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 460.dp)) {
                    items(SupportedLanguages.all, key = { it.tag }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    languageViewModel.setLanguage(item.tag)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (item.tag == "system") {
                                        stringResource(R.string.ds_language_system)
                                    } else {
                                        item.nativeName
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.englishName != item.nativeName) {
                                    Text(
                                        text = item.englishName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (item.tag == languageTag) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (archiveState.isWorking) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(archiveState.progressLabel.ifBlank { stringResource(R.string.ds_settings_processing_zip) }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LinearProgressIndicator(
                        progress = {
                            if (archiveState.total > 0) {
                                (archiveState.current.toFloat() / archiveState.total).coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (archiveState.total > 0) {
                        Text(
                            text = "${archiveState.current} / ${archiveState.total}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.ds_settings_parsing),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }

    archiveState.result?.let { result ->
        AlertDialog(
            onDismissRequest = archiveViewModel::clearResult,
            title = { Text(stringResource(R.string.ds_settings_operation_done)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(result.summary(context))
                    if (result.errors.isNotEmpty()) {
                        Text(
                            text = result.errors.joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = archiveViewModel::clearResult) { Text(stringResource(R.string.ds_settings_ok)) }
            }
        )
    }

    archiveState.error?.let { error ->
        AlertDialog(
            onDismissRequest = archiveViewModel::clearResult,
            title = { Text(stringResource(R.string.ds_settings_operation_failed)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = archiveViewModel::clearResult) { Text(stringResource(R.string.ds_settings_ok)) }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        modifier = Modifier.padding(start = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String? = null,
    showChevron: Boolean = true,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .padding(0.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
        if (showChevron) {
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ApiKeyDialog(
    platform: PlatformV2,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var key by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("API Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.ds_settings_current_key),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = maskKey(context, platform.token.orEmpty()),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = stringResource(R.string.ds_settings_copy_key))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = stringResource(R.string.ds_settings_change_key),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = {
                        key = it
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.ds_settings_paste_key)) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Text(
                    text = stringResource(R.string.ds_settings_key_local),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = key.trim()
                    if (!trimmed.startsWith("sk-") || trimmed.length < 20) {
                        error = context.getString(R.string.ds_settings_key_invalid)
                    } else {
                        onSave(trimmed)
                    }
                }
            ) { Text(stringResource(R.string.ds_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ds_cancel)) }
        }
    )
}

private fun maskKey(context: android.content.Context, key: String): String {
    if (key.isBlank()) return context.getString(R.string.ds_settings_not_configured_short)
    val tail = key.takeLast(4)
    return "sk-••••••••$tail"
}
