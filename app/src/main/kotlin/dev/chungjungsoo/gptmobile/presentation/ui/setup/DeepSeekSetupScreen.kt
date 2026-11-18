package dev.chungjungsoo.gptmobile.presentation.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.presentation.common.PrimaryLongButton
import dev.chungjungsoo.gptmobile.presentation.ui.setup.DeepSeekSetupViewModel.Companion.GET_KEY_URL

/**
 * First screen of the app.
 *
 * There is exactly one thing to do here: paste the key.
 * Everything else (providers, models, parameters) is configured for you.
 */
@Composable
fun DeepSeekSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: DeepSeekSetupViewModel = hiltViewModel(),
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    DeepSeekSetupContent(
        modifier = modifier,
        uiState = uiState,
        onApiKeyChange = viewModel::onApiKeyChange,
        onStart = { viewModel.save(onComplete) },
        onGetKey = { uriHandler.openUri(GET_KEY_URL) }
    )
}

@Composable
fun DeepSeekSetupContent(
    modifier: Modifier = Modifier,
    uiState: DeepSeekSetupViewModel.UiState,
    onApiKeyChange: (String) -> Unit,
    onStart: () -> Unit,
    onGetKey: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(56.dp))

            Icon(
                painter = painterResource(R.drawable.ic_whale),
                contentDescription = null,
                modifier = Modifier.size(104.dp),
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "DeepSeek",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.ds_setup_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.ds_setup_local),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.ds_setup_key_label)) },
                placeholder = { Text("sk-...") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                isError = uiState.error != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText = {
                    Text(
                        text = uiState.error ?: stringResource(R.string.ds_setup_key_hint),
                        color = if (uiState.error != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            )

            TextButton(onClick = onGetKey) {
                Text(stringResource(R.string.ds_setup_get_key))
            }

            Spacer(Modifier.height(24.dp))

            PrimaryLongButton(
                enabled = uiState.apiKey.isNotBlank() && !uiState.isSaving,
                onClick = onStart,
                text = if (uiState.isSaving) stringResource(R.string.ds_setup_saving) else stringResource(R.string.ds_setup_start)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.ds_setup_key_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(56.dp))
        }
    }
}
