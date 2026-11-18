package dev.chungjungsoo.gptmobile.presentation.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.content.ClipData
import android.speech.tts.TextToSpeech
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CallSplit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.AttachFile
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.presentation.common.CirclePlusIcon
import dev.chungjungsoo.gptmobile.presentation.common.DeepSeekMenuIcon
import dev.chungjungsoo.gptmobile.data.database.entity.ACTIVE_REVISION_LATEST
import dev.chungjungsoo.gptmobile.data.database.entity.AgentRun
import dev.chungjungsoo.gptmobile.data.database.entity.AgentRunStatus
import dev.chungjungsoo.gptmobile.data.database.entity.MessageV2
import dev.chungjungsoo.gptmobile.data.database.entity.PlatformV2
import dev.chungjungsoo.gptmobile.data.database.entity.ToolEvent
import dev.chungjungsoo.gptmobile.data.database.entity.effectiveContent
import dev.chungjungsoo.gptmobile.data.database.entity.effectiveRunId
import dev.chungjungsoo.gptmobile.data.database.entity.effectiveThoughts
import dev.chungjungsoo.gptmobile.data.database.entity.effectiveTimeline
import dev.chungjungsoo.gptmobile.util.isAssistantErrorMessage
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = hiltViewModel(),
    onBackAction: () -> Unit,
    onNavigateToLocalModels: () -> Unit = {},
    onNewChat: (List<String>) -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onFork: (dev.chungjungsoo.gptmobile.data.database.entity.ChatRoomV2) -> Unit = {}
) {
    val containerSize = LocalWindowInfo.current.containerSize
    val screenWidthDp = with(LocalDensity.current) { containerSize.width.toDp() }
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboard.current
    val systemChatMargin = 32.dp
    val maximumUserChatBubbleWidth = (screenWidthDp - systemChatMargin) * 0.8F
    val maximumOpponentChatBubbleWidth = screenWidthDp - systemChatMargin
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val chatRoom by chatViewModel.chatRoom.collectAsStateWithLifecycle()
    val groupedMessages by chatViewModel.groupedMessages.collectAsStateWithLifecycle()
    val thinkingEnabled by chatViewModel.thinkingEnabled.collectAsStateWithLifecycle()
    val webSearchEnabled by chatViewModel.webSearchEnabled.collectAsStateWithLifecycle()
    val listState = rememberChatListState(groupedMessages.userMessages.size)
    val isUserDragging by listState.interactionSource.collectIsDraggedAsState()
    var isFollowingBottom by remember { mutableStateOf(true) }
    val agentRunsById by chatViewModel.agentRunsById.collectAsStateWithLifecycle()
    val runNoticesById by chatViewModel.runNoticesById.collectAsStateWithLifecycle()
    val toolEventsByRun by chatViewModel.toolEventsByRun.collectAsStateWithLifecycle()
    val indexStates by chatViewModel.indexStates.collectAsStateWithLifecycle()
    val loadingStates by chatViewModel.loadingStates.collectAsStateWithLifecycle()
    val isChatTitleDialogOpen by chatViewModel.isChatTitleDialogOpen.collectAsStateWithLifecycle()
    val isChatModelDialogOpen by chatViewModel.isChatModelDialogOpen.collectAsStateWithLifecycle()
    val chatContextState by chatViewModel.chatContextState.collectAsStateWithLifecycle()
    val isContextBusy by chatViewModel.isContextBusy.collectAsStateWithLifecycle()
    var isContextPlatformPickerOpen by rememberSaveable { mutableStateOf(false) }
    val messageEditSession by chatViewModel.messageEditSession.collectAsStateWithLifecycle()
    val isSelectTextSheetOpen by chatViewModel.isSelectTextSheetOpen.collectAsStateWithLifecycle()
    val selectedAttachments by chatViewModel.selectedAttachments.collectAsStateWithLifecycle()
    val attachmentNotice by chatViewModel.attachmentNotice.collectAsStateWithLifecycle()
    val needsLocalNetworkAccess by chatViewModel.needsLocalNetworkAccess.collectAsStateWithLifecycle()
    val appEnabledPlatforms by chatViewModel.enabledPlatformsInApp.collectAsStateWithLifecycle()
    val appAllPlatforms by chatViewModel.platformsInApp.collectAsStateWithLifecycle()
    val chatPlatformModels by chatViewModel.chatPlatformModels.collectAsStateWithLifecycle()
    val downloadedLocalModels by chatViewModel.downloadedLocalModels.collectAsStateWithLifecycle()
    val enabledPlatformLookup = remember(appEnabledPlatforms) { appEnabledPlatforms.associateBy { it.uid } }
    val canUseChat = (chatViewModel.enabledPlatformsInChat.toSet() - appEnabledPlatforms.map { it.uid }.toSet()).isEmpty()
    val isIdle = loadingStates.all { it == ChatViewModel.LoadingState.Idle } && !isContextBusy
    val context = LocalContext.current
    val lastMessageIndex = groupedMessages.userMessages.lastIndex
    var requestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
    var sendAfterNotificationPermission by rememberSaveable { mutableStateOf(false) }
    var sendAfterLocalNetworkPermission by rememberSaveable { mutableStateOf(false) }
    val localNetworkPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && sendAfterLocalNetworkPermission) {
            chatViewModel.askQuestion()
            focusManager.clearFocus()
        } else if (!granted) {
            Toast.makeText(context, R.string.local_network_permission_required, Toast.LENGTH_SHORT).show()
        }
        sendAfterLocalNetworkPermission = false
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        requestedNotificationPermission = true
        if (sendAfterNotificationPermission) {
            if (needsLocalNetworkAccess &&
                Build.VERSION.SDK_INT >= 37 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED
            ) {
                sendAfterLocalNetworkPermission = true
                localNetworkPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
            } else {
                chatViewModel.askQuestion()
                focusManager.clearFocus()
            }
        }
        sendAfterNotificationPermission = false
    }

    val scope = rememberCoroutineScope()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        chatViewModel.refreshLocalNetworkRequirement()
    }

    LaunchedEffect(isUserDragging, listState.isScrollInProgress, listState.canScrollForward, listState.lastScrolledBackward) {
        isFollowingBottom = nextFollowBottom(
            isFollowing = isFollowingBottom,
            isUserScrolling = isUserDragging || listState.isScrollInProgress,
            isScrollingAway = listState.lastScrolledBackward,
            canScrollForward = listState.canScrollForward
        )
    }

    LaunchedEffect(lastMessageIndex) {
        isFollowingBottom = true
    }

    ChatBottomAutoScroller(
        listState = listState,
        isEnabled = shouldAutoScrollToBottom(
            isFollowing = isFollowingBottom,
            isUserDragging = isUserDragging,
            isScrollInProgress = listState.isScrollInProgress,
            isScrollingAway = listState.lastScrolledBackward
        )
    )

    // The bottom anchor already handles content growth, but an explicit nudge keeps
    // long streaming reasoning/answer text pinned to the newest line on every frame.
    val streamingLength = groupedMessages.assistantMessages.lastOrNull()?.lastOrNull()?.let { message ->
        message.effectiveContent().length + message.effectiveThoughts().length + message.effectiveTimeline().size
    } ?: 0
    LaunchedEffect(streamingLength) {
        if (isFollowingBottom && listState.layoutInfo.totalItemsCount > 0) {
            listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    LaunchedEffect(attachmentNotice) {
        attachmentNotice?.let { notice ->
            Toast.makeText(context, notice, Toast.LENGTH_SHORT).show()
            chatViewModel.consumeAttachmentNotice()
        }
    }

    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }
    val lastAssistantText = groupedMessages.assistantMessages
        .asReversed()
        .flatMap { it.asReversed() }
        .firstOrNull { it.effectiveContent().isNotBlank() }
        ?.effectiveContent()
        .orEmpty()

    fun speakAnswer(text: String) {
        when {
            text.isBlank() -> Toast.makeText(context, context.getString(R.string.ds_chat_no_answer), Toast.LENGTH_SHORT).show()
            !ttsReady -> Toast.makeText(context, context.getString(R.string.ds_chat_tts_preparing), Toast.LENGTH_SHORT).show()
            else -> {
                tts.language = Locale.CHINA
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "deepseek-answer")
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ChatTopBar(
                title = chatRoom.title,
                onBackAction = onHistoryClick,
                onReadAloud = { speakAnswer(lastAssistantText) },
                onNewChat = { onNewChat(chatViewModel.enabledPlatformsInChat) },
                onFork = { chatViewModel.forkChat(onFork) },
                forkEnabled = isIdle,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    itemsIndexed(
                        items = groupedMessages.userMessages,
                        key = { index, message -> chatMessagePairKey(message, index) }
                    ) { index, message ->
                        ChatMessagePair(
                            messageIndex = index,
                            message = message,
                            assistantMessages = groupedMessages.assistantMessages.getOrNull(index) ?: emptyList(),
                            agentRunsById = agentRunsById,
                            runNoticesById = runNoticesById,
                            toolEventsByRun = toolEventsByRun,
                            platformIndexState = indexStates.getOrElse(index) { 0 },
                            loadingStates = loadingStates,
                            enabledPlatformsInChat = chatViewModel.enabledPlatformsInChat,
                            enabledPlatformLookup = enabledPlatformLookup,
                            canUseChat = canUseChat,
                            isIdle = isIdle,
                            isActiveMessage = index == lastMessageIndex,
                            maximumUserChatBubbleWidth = maximumUserChatBubbleWidth,
                            maximumOpponentChatBubbleWidth = maximumOpponentChatBubbleWidth,
                            onEditQuestion = chatViewModel::openUserMessageEditDialog,
                            onEditAssistant = chatViewModel::openAssistantMessageEditDialog,
                            onCopyText = { copiedText ->
                                scope.launch {
                                    clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText(copiedText, copiedText)))
                                }
                            },
                            onPlatformClick = chatViewModel::updateChatPlatformIndex,
                            onSelectText = chatViewModel::openSelectTextSheet,
                            onSpeakText = { text -> speakAnswer(text) },
                            onRetry = chatViewModel::retryChat,
                            onShowPreviousRevision = chatViewModel::showPreviousAssistantRevision,
                            onShowNextRevision = chatViewModel::showNextAssistantRevision
                        )
                    }
                    if (groupedMessages.userMessages.isNotEmpty()) {
                        item(key = "chat-bottom-anchor") {
                            Spacer(Modifier.size(1.dp))
                        }
                    }
                }

                if (!isFollowingBottom && listState.canScrollForward) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        ScrollToBottomButton {
                            scope.launch {
                                listState.animateScrollToLatestChatMessage()
                                isFollowingBottom = true
                            }
                        }
                    }
                }
            }

            ChatInputBox(
                inputState = chatViewModel.question,
                chatEnabled = canUseChat,
                sendButtonEnabled = isIdle,
                isRunning = !isIdle,
                thinkingEnabled = thinkingEnabled,
                webSearchEnabled = webSearchEnabled,
                onThinkingToggle = chatViewModel::toggleThinking,
                onWebSearchToggle = chatViewModel::toggleWebSearch,
                selectedAttachments = selectedAttachments,
                onFileSelected = { filePath -> chatViewModel.addSelectedFile(filePath) },
                onFileRemoved = { filePath -> chatViewModel.removeSelectedFile(filePath) },
                onCancelButtonClick = chatViewModel::cancelActiveRuns
            ) {
                if (!requestedNotificationPermission &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    sendAfterNotificationPermission = true
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else if (needsLocalNetworkAccess &&
                    Build.VERSION.SDK_INT >= 37 &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED
                ) {
                    sendAfterLocalNetworkPermission = true
                    localNetworkPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                } else {
                    chatViewModel.askQuestion()
                    focusManager.clearFocus()
                }
            }
            Text(
                text = stringResource(R.string.ds_chat_ai_footer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )
        }

        if (isChatTitleDialogOpen) {
            ChatTitleDialog(
                initialTitle = chatRoom.title,
                onDefaultTitleMode = chatViewModel::generateDefaultChatTitle,
                onConfirmRequest = { title -> chatViewModel.updateChatTitle(title) },
                onDismissRequest = chatViewModel::closeChatTitleDialog
            )
        }

        if (isContextPlatformPickerOpen) {
            ChatContextPlatformPicker(
                platforms = appAllPlatforms.filter { it.uid in chatViewModel.enabledPlatformsInChat },
                onSelect = { uid ->
                    isContextPlatformPickerOpen = false
                    chatViewModel.openChatContextSettings(uid)
                },
                onDismiss = { isContextPlatformPickerOpen = false }
            )
        }
        chatContextState?.let { state ->
            ChatContextDialog(
                platformName = state.platform.name,
                model = state.settings.model,
                contextWindowTokens = state.settings.overrideContextWindowTokens,
                detectedContextWindowTokens = state.settings.detectedContextWindowTokens,
                resumableReplies = state.settings.resumableReplies,
                supportsResumableReplies = state.settings.supportsResumableReplies,
                canCompact = chatRoom.id > 0 && isIdle && state.settings.canCompact,
                maxContextWindowTokens = state.settings.detectedContextWindowTokens.takeIf {
                    state.platform.compatibleType == dev.chungjungsoo.gptmobile.data.model.ClientType.LITERT_LM
                },
                onConfirm = chatViewModel::saveChatContextSettings,
                onDismiss = chatViewModel::closeChatContextSettings
            )
        }
        if (isChatModelDialogOpen) {
            val platformNames = chatViewModel.enabledPlatformsInChat.associateWith { uid ->
                appAllPlatforms.find { it.uid == uid }?.name ?: stringResource(R.string.unknown)
            }
            ChatModelDialog(
                platformOrder = chatViewModel.enabledPlatformsInChat,
                initialModels = chatPlatformModels,
                platformNames = platformNames,
                platformClientTypes = appAllPlatforms.associate { it.uid to it.compatibleType },
                downloadedLocalModels = downloadedLocalModels,
                onNavigateToLocalModels = onNavigateToLocalModels,
                onDismissRequest = chatViewModel::closeChatModelDialog,
                onConfirmRequest = { models ->
                    chatViewModel.updateChatPlatformModels(models)
                    chatViewModel.closeChatModelDialog()
                }
            )
        }

        messageEditSession?.let { session ->
            when (session.role) {
                ChatViewModel.MessageEditRole.USER -> {
                    UserMessageEditDialog(
                        initialQuestion = session.message,
                        attachments = session.attachments,
                        onFileSelected = chatViewModel::addMessageEditFile,
                        onCopyFailed = chatViewModel::notifyAttachmentCopyFailed,
                        onFileRemoved = chatViewModel::removeMessageEditFile,
                        onDismissRequest = chatViewModel::discardMessageEditDialog,
                        onConfirmRequest = { question ->
                            if (chatViewModel.saveUserMessageEdit(question, session.attachments)) {
                                chatViewModel.finishMessageEditDialog()
                            }
                        }
                    )
                }

                ChatViewModel.MessageEditRole.ASSISTANT -> {
                    AssistantMessageEditDialog(
                        initialMessage = session.message,
                        attachments = session.attachments,
                        onFileSelected = chatViewModel::addMessageEditFile,
                        onCopyFailed = chatViewModel::notifyAttachmentCopyFailed,
                        onFileRemoved = chatViewModel::removeMessageEditFile,
                        onDismissRequest = chatViewModel::discardMessageEditDialog,
                        onConfirmRequest = { message, thoughts ->
                            if (chatViewModel.saveAssistantMessageEdit(message, thoughts, session.attachments)) {
                                chatViewModel.finishMessageEditDialog()
                            }
                        }
                    )
                }
            }
        }

        if (isSelectTextSheetOpen) {
            val selectedText by chatViewModel.selectedText.collectAsStateWithLifecycle()
            ModalBottomSheet(onDismissRequest = chatViewModel::closeSelectTextSheet) {
                SelectionContainer(
                    modifier = Modifier
                        .padding(24.dp)
                        .heightIn(min = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(selectedText)
                }
            }
        }
    }
}

@Composable
private fun ChatMessagePair(
    messageIndex: Int,
    message: MessageV2,
    assistantMessages: List<MessageV2>,
    agentRunsById: Map<String, AgentRun>,
    runNoticesById: Map<String, List<ChatRunNotice>>,
    toolEventsByRun: Map<String, List<ToolEvent>>,
    platformIndexState: Int,
    loadingStates: List<ChatViewModel.LoadingState>,
    enabledPlatformsInChat: List<String>,
    enabledPlatformLookup: Map<String, PlatformV2>,
    canUseChat: Boolean,
    isIdle: Boolean,
    isActiveMessage: Boolean,
    maximumUserChatBubbleWidth: Dp,
    maximumOpponentChatBubbleWidth: Dp,
    onEditQuestion: (MessageV2) -> Unit,
    onEditAssistant: (Int, Int) -> Unit,
    onCopyText: (String) -> Unit,
    onPlatformClick: (Int, Int) -> Unit,
    onSelectText: (String) -> Unit,
    onSpeakText: (String) -> Unit = {},
    onRetry: (Int, Int) -> Unit,
    onShowPreviousRevision: (Int, Int) -> Unit,
    onShowNextRevision: (Int, Int) -> Unit
) {
    val selectedAssistantMessage = assistantMessages.getOrNull(platformIndexState)
    val assistantContent = selectedAssistantMessage?.effectiveContent() ?: ""
    val assistantThoughts = selectedAssistantMessage?.effectiveThoughts() ?: ""
    val assistantTimeline = selectedAssistantMessage?.effectiveTimeline().orEmpty()
    val selectedRunId = selectedAssistantMessage?.effectiveRunId()
    val agentRun = selectedRunId?.let(agentRunsById::get)
    val toolEvents = selectedRunId?.let(toolEventsByRun::get).orEmpty()
    val canShowPreviousRevision = selectedAssistantMessage?.let { assistantMessage ->
        assistantMessage.revisions.isNotEmpty() &&
            assistantMessage.activeRevisionIndex < assistantMessage.revisions.lastIndex
    } ?: false
    val canShowNextRevision = selectedAssistantMessage?.let { assistantMessage ->
        assistantMessage.revisions.isNotEmpty() &&
            assistantMessage.activeRevisionIndex != ACTIVE_REVISION_LATEST
    } ?: false
    val selectedPlatformUid = enabledPlatformsInChat.getOrElse(platformIndexState) { "" }
    val isCurrentPlatformLoading =
        loadingStates.getOrElse(platformIndexState) { ChatViewModel.LoadingState.Idle } == ChatViewModel.LoadingState.Loading
    val canEdit = canUseChat && isIdle
    val canRetry = canUseChat && isActiveMessage && !isCurrentPlatformLoading
    val isError = agentRun?.status == AgentRunStatus.FAILED && isAssistantErrorMessage(assistantContent)

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End
        ) {
            UserChatBubble(
                modifier = Modifier.widthIn(max = maximumUserChatBubbleWidth),
                text = message.content,
                files = message.attachments.map { it.filePathForDisplay },
                contentIdentity = chatMessagePairKey(message, messageIndex),
                canEdit = canEdit,
                onCopyClick = { onCopyText(message.content) },
                onEditClick = { onEditQuestion(message) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            OpponentChatBubble(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .widthIn(max = maximumOpponentChatBubbleWidth),
                canEdit = canEdit,
                canRetry = canRetry,
                isLoading = isActiveMessage && isCurrentPlatformLoading,
                isError = isError,
                text = assistantContent,
                thoughts = assistantThoughts,
                timeline = assistantTimeline,
                attachments = selectedAssistantMessage?.attachments.orEmpty().map { it.filePathForDisplay },
                agentRun = agentRun,
                runNotices = selectedRunId?.let(runNoticesById::get).orEmpty(),
                toolEvents = toolEvents,
                contentIdentity = "$messageIndex:$selectedPlatformUid:${selectedRunId.orEmpty()}:${selectedAssistantMessage?.activeRevisionIndex}",
                revisionIndexLabel = selectedAssistantMessage?.let { assistantMessage ->
                    val totalRevisions = assistantMessage.revisions.size + 1
                    if (assistantMessage.activeRevisionIndex == ACTIVE_REVISION_LATEST) {
                        stringResource(
                            R.string.revision_counter,
                            totalRevisions,
                            totalRevisions
                        )
                    } else {
                        stringResource(
                            R.string.revision_counter,
                            assistantMessage.revisions.size - assistantMessage.activeRevisionIndex,
                            totalRevisions
                        )
                    }
                },
                canShowPreviousRevision = canShowPreviousRevision,
                canShowNextRevision = canShowNextRevision,
                onCopyClick = { onCopyText(assistantContent) },
                onSelectClick = { onSelectText(assistantContent) },
                onSpeakClick = { onSpeakText(assistantContent) },
                onViewFull = onSelectText,
                onRetryClick = { onRetry(messageIndex, platformIndexState) },
                onEditClick = { onEditAssistant(messageIndex, platformIndexState) },
                onShowPreviousRevision = { onShowPreviousRevision(messageIndex, platformIndexState) },
                onShowNextRevision = { onShowNextRevision(messageIndex, platformIndexState) }
            )
        }
    }
}

private fun chatMessagePairKey(message: MessageV2, index: Int): String = if (message.id > 0) {
    "message-${message.id}"
} else {
    "message-${message.createdAt}-$index"
}

internal fun shouldShowReplyLoadingIndicator(
    isActiveMessage: Boolean,
    loadingStates: List<ChatViewModel.LoadingState>
): Boolean = isActiveMessage && loadingStates.any { it == ChatViewModel.LoadingState.Loading }

@Composable
internal fun rememberChatListState(messageCount: Int): LazyListState = key(messageCount > 0) {
    rememberLazyListState(initialFirstVisibleItemIndex = messageCount)
}

internal fun nextFollowBottom(
    isFollowing: Boolean,
    isUserScrolling: Boolean,
    isScrollingAway: Boolean,
    canScrollForward: Boolean
): Boolean = when {
    !canScrollForward -> true
    isUserScrolling && isScrollingAway -> false
    else -> isFollowing
}

internal fun shouldAutoScrollToBottom(
    isFollowing: Boolean,
    isUserDragging: Boolean,
    isScrollInProgress: Boolean,
    isScrollingAway: Boolean
): Boolean = isFollowing &&
    !isUserDragging &&
    !(isScrollInProgress && isScrollingAway)

@Composable
internal fun ChatBottomAutoScroller(
    listState: LazyListState,
    isEnabled: Boolean
) {
    LaunchedEffect(listState, isEnabled) {
        if (!isEnabled) return@LaunchedEffect

        snapshotFlow { listState.layoutInfo }
            .collectLatest { layoutInfo ->
                val latestItemIndex = layoutInfo.totalItemsCount - 1
                if (latestItemIndex >= 0 && listState.canScrollForward) {
                    listState.requestScrollToItem(latestItemIndex)
                }
            }
    }
}

internal suspend fun LazyListState.animateScrollToLatestChatMessage() {
    val latestItemIndex = layoutInfo.totalItemsCount - 1
    if (latestItemIndex >= 0) {
        animateScrollToItem(latestItemIndex)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChatTopBar(
    title: String,
    onBackAction: () -> Unit,
    onReadAloud: () -> Unit,
    onNewChat: () -> Unit,
    onFork: () -> Unit,
    forkEnabled: Boolean,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackAction) {
                DeepSeekMenuIcon(tint = MaterialTheme.colorScheme.onSurface)
            }
        },
        actions = {
            IconButton(onClick = onReadAloud) {
                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = stringResource(R.string.ds_read_aloud)
                )
            }
            IconButton(onClick = onFork, enabled = forkEnabled) {
                Icon(
                    imageVector = Icons.Rounded.CallSplit,
                    contentDescription = stringResource(R.string.ds_chat_fork),
                    tint = if (forkEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                )
            }
            IconButton(onClick = onNewChat) {
                CirclePlusIcon(tint = MaterialTheme.colorScheme.onSurface)
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun ChatDropdownMenu(
    isDropdownMenuExpanded: Boolean,
    isMenuItemEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onChatTitleItemClick: () -> Unit,
    onExportChatItemClick: () -> Unit
) {
    DropdownMenu(
        modifier = Modifier.wrapContentSize(),
        expanded = isDropdownMenuExpanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            enabled = isMenuItemEnabled,
            text = { Text(text = stringResource(R.string.update_chat_title)) },
            onClick = onChatTitleItemClick
        )
        /* Export Chat */
        DropdownMenuItem(
            enabled = isMenuItemEnabled,
            text = { Text(text = stringResource(R.string.export_chat)) },
            onClick = {
                onExportChatItemClick()
                onDismissRequest()
            }
        )
    }
}

private fun exportChat(context: Context, chatViewModel: ChatViewModel) {
    try {
        val (fileName, fileContent) = chatViewModel.exportChat(
            toolTraceLabels = context.toolTraceLabels(),
            legacyOrderNotice = context.getString(R.string.legacy_assistant_order_unavailable)
        )
        val file = File(context.getExternalFilesDir(null), fileName)
        file.writeText(fileContent)
        val uri = getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/markdown"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Chat Export").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val resInfo = context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
        resInfo.forEach { res ->
            context.grantUriPermission(res.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        Log.e("ChatExport", "Failed to export chat", e)
        Toast.makeText(context, "Failed to export chat", Toast.LENGTH_SHORT).show()
    }
}

private fun Context.toolTraceLabels(): ToolTraceLabels = ToolTraceLabels(
    expandToolTrace = getString(R.string.tool_trace_expand_content_description),
    collapseToolTrace = getString(R.string.tool_trace_collapse_content_description),
    expand = getString(R.string.tool_trace_expand),
    collapse = getString(R.string.tool_trace_collapse),
    call = getString(R.string.tool_trace_call_singular),
    calls = getString(R.string.tool_trace_call_plural),
    running = getString(R.string.tool_trace_status_running),
    failed = getString(R.string.tool_trace_status_failed),
    completedWithErrors = getString(R.string.tool_trace_status_completed_with_errors),
    canceled = getString(R.string.tool_trace_status_canceled),
    completed = getString(R.string.tool_trace_status_completed),
    status = getString(R.string.tool_trace_status),
    callId = getString(R.string.tool_trace_call_id),
    connection = getString(R.string.tool_trace_connection),
    tool = getString(R.string.tool_trace_tool),
    modelTool = getString(R.string.tool_trace_model_tool),
    timing = getString(R.string.tool_trace_timing),
    error = getString(R.string.tool_trace_error),
    arguments = getString(R.string.tool_trace_arguments),
    result = getString(R.string.tool_trace_result),
    exportHeader = { count -> getString(R.string.tool_trace_export_header, count) },
    startedAt = getString(R.string.tool_trace_timing_started_at)
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ChatInputBox(
    inputState: TextFieldState = rememberTextFieldState(),
    chatEnabled: Boolean = true,
    sendButtonEnabled: Boolean = true,
    isRunning: Boolean = false,
    thinkingEnabled: Boolean = true,
    webSearchEnabled: Boolean = true,
    onThinkingToggle: () -> Unit = {},
    onWebSearchToggle: () -> Unit = {},
    selectedAttachments: List<ChatAttachmentDraft> = emptyList(),
    onFileSelected: (String) -> Unit = {},
    onFileRemoved: (String) -> Unit = {},
    onCancelButtonClick: () -> Unit = {},
    onSendButtonClick: () -> Unit = {}
) {
    val localStyle = LocalTextStyle.current
    val mergedStyle = localStyle.merge(TextStyle(color = LocalContentColor.current))
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chatInputLineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5)
    val hasQuestionText = inputState.text.isNotEmpty()
    var isAttachMenuOpen by remember { mutableStateOf(false) }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            .orEmpty()
        if (spoken.isNotBlank()) {
            inputState.setTextAndPlaceCursorAtEnd(spoken)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            runCatching {
                val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.png")
                file.outputStream().use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 92, output)
                }
                onFileSelected(file.absolutePath)
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val filePath = withContext(Dispatchers.IO) {
                    copyFileToAppDirectory(context, it)
                }
                filePath?.let { path -> onFileSelected(path) }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            if (selectedAttachments.isNotEmpty()) {
                FileThumbnailRow(
                    selectedAttachments = selectedAttachments,
                    onFileRemoved = onFileRemoved
                )
            }
            BasicTextField(
                state = inputState,
                modifier = Modifier.fillMaxWidth(),
                enabled = chatEnabled,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                lineLimits = chatInputLineLimits,
                decorator = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        if (inputState.text.isEmpty()) {
                            Text(
                                modifier = Modifier.alpha(0.48f),
                                text = if (chatEnabled) stringResource(R.string.ds_chat_input_hint) else stringResource(R.string.some_platforms_disabled),
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                            )
                        }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            innerTextField()
                        }
                    }
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 12.dp, top = 2.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeaturePill(
                    text = stringResource(R.string.ds_chat_deep_thinking),
                    icon = Icons.Rounded.AutoAwesome,
                    active = thinkingEnabled,
                    onClick = onThinkingToggle
                )
                Spacer(Modifier.width(8.dp))
                FeaturePill(
                    text = stringResource(R.string.ds_chat_smart_search),
                    icon = Icons.Rounded.Public,
                    active = webSearchEnabled,
                    onClick = onWebSearchToggle
                )
                Spacer(Modifier.weight(1f))
                val sendEnabled = chatEnabled && sendButtonEnabled && hasQuestionText
                when {
                    isRunning -> CircleActionButton(
                        icon = Icons.Rounded.Close,
                        description = stringResource(R.string.cancel_active_runs),
                        onClick = onCancelButtonClick
                    )

                    sendEnabled -> CircleActionButton(
                        icon = ImageVector.vectorResource(id = R.drawable.ic_send),
                        description = stringResource(R.string.send),
                        filled = true,
                        onClick = onSendButtonClick
                    )

                    else -> CircleActionButton(
                        icon = Icons.Outlined.Add,
                        description = stringResource(R.string.ds_chat_add_attachment),
                        onClick = { isAttachMenuOpen = true }
                    )
                }
                Spacer(Modifier.width(10.dp))
                CircleActionButton(
                    icon = Icons.Rounded.GraphicEq,
                    description = stringResource(R.string.ds_chat_voice_input),
                    enabled = chatEnabled,
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.ds_chat_voice_prompt))
                        }
                        runCatching { voiceLauncher.launch(intent) }
                            .onFailure {
                                Toast.makeText(context, context.getString(R.string.ds_chat_voice_unavailable), Toast.LENGTH_SHORT).show()
                            }
                    }
                )
            }
        }
    }

    if (isAttachMenuOpen) {
        ModalBottomSheet(
            onDismissRequest = { isAttachMenuOpen = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AttachmentOption(
                    label = stringResource(R.string.ds_chat_camera),
                    icon = Icons.Outlined.PhotoCamera,
                    modifier = Modifier.weight(1f)
                ) {
                    isAttachMenuOpen = false
                    runCatching { takePictureLauncher.launch(null) }
                }
                AttachmentOption(
                    label = stringResource(R.string.ds_chat_gallery),
                    icon = Icons.Outlined.PhotoLibrary,
                    modifier = Modifier.weight(1f)
                ) {
                    isAttachMenuOpen = false
                    filePickerLauncher.launch("image/*")
                }
                AttachmentOption(
                    label = stringResource(R.string.ds_chat_file),
                    icon = Icons.Outlined.AttachFile,
                    modifier = Modifier.weight(1f)
                ) {
                    isAttachMenuOpen = false
                    filePickerLauncher.launch("*/*")
                }
            }
            Spacer(Modifier.navigationBarsPadding())
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CircleActionButton(
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.size(38.dp),
        shape = CircleShape,
        color = if (filled) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (filled) null else BorderStroke(
            1.2.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = if (enabled) 0.9f else 0.4f)
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier.size(19.dp),
                tint = when {
                    filled -> MaterialTheme.colorScheme.onPrimary
                    enabled -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                }
            )
        }
    }
}

@Composable
private fun AttachmentOption(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(104.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeaturePill(
    text: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            1.dp,
            if (active) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (active) {
                    Color(0xFF7C97FF)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (active) {
                    Color(0xFF7C97FF)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
internal fun FileThumbnailRow(
    selectedAttachments: List<ChatAttachmentDraft>,
    onFileRemoved: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        selectedAttachments.forEach { attachment ->
            FileThumbnail(
                attachment = attachment,
                onRemove = { onFileRemoved(attachment.sourceFilePath) }
            )
        }
    }
}

@Composable
internal fun FileThumbnail(
    attachment: ChatAttachmentDraft,
    onRemove: () -> Unit
) {
    val file = File(attachment.preparedFilePath ?: attachment.sourceFilePath)
    val isImage = isImageFile(file.extension)

    Column(
        modifier = Modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (isImage) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_image),
                    contentDescription = file.name,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_file),
                    contentDescription = file.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            MaterialTheme.colorScheme.error,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            if (attachment.status == ChatAttachmentDraft.Status.Preparing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                        .size(18.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Text(
            text = file.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .padding(top = 4.dp)
                .width(72.dp)
        )

        attachment.notice?.let { notice ->
            Text(
                text = notice,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.width(72.dp)
            )
        }

        attachment.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.width(72.dp)
            )
        }
    }
}

internal fun copyFileToAppDirectory(context: Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val rawFileName = getFileName(context, uri)
        val sanitizedFileName = sanitizeFileName(rawFileName)

        val attachmentsDir = File(context.filesDir, "attachments")
        attachmentsDir.mkdirs()

        var targetFile = File(attachmentsDir, sanitizedFileName)

        // If file exists, append timestamp to avoid overwrites
        if (targetFile.exists()) {
            val nameWithoutExt = sanitizedFileName.substringBeforeLast(".")
            val ext = sanitizedFileName.substringAfterLast(".", "")
            val uniqueName = if (ext.isNotEmpty()) {
                "${nameWithoutExt}_${System.currentTimeMillis()}.$ext"
            } else {
                "${sanitizedFileName}_${System.currentTimeMillis()}"
            }
            targetFile = File(attachmentsDir, uniqueName)
        }

        // Verify canonical path is within attachments directory to prevent path traversal
        val attachmentsDirCanonical = attachmentsDir.canonicalPath
        val targetFileCanonical = targetFile.canonicalPath
        if (!targetFileCanonical.startsWith(attachmentsDirCanonical + File.separator) &&
            targetFileCanonical != attachmentsDirCanonical
        ) {
            return null
        }

        inputStream.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        targetFile.absolutePath
    } catch (e: Exception) {
        null
    }
}

private fun getFileName(context: Context, uri: android.net.Uri): String {
    var fileName = "attachment_${System.currentTimeMillis()}"

    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex != -1) {
            fileName = cursor.getString(nameIndex) ?: fileName
        }
    }

    return fileName
}

private fun sanitizeFileName(fileName: String): String {
    val maxLength = 200

    // Remove path separators and ".." segments
    val withoutPathTraversal = fileName
        .replace("..", "")
        .replace("/", "")
        .replace("\\", "")

    // Keep only safe characters: alphanumerics, dash, underscore, dot
    val sanitized = withoutPathTraversal
        .filter { it.isLetterOrDigit() || it == '-' || it == '_' || it == '.' }
        .take(maxLength)
        .trim('.')

    // If sanitized name is empty, generate a fallback
    return sanitized.ifEmpty { "attachment_${System.currentTimeMillis()}" }
}

private fun isImageFile(extension: String?): Boolean {
    val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    return extension?.lowercase() in imageExtensions
}

@Composable
fun ScrollToBottomButton(onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Icon(Icons.Rounded.KeyboardArrowDown, stringResource(R.string.scroll_to_bottom_icon))
    }
}
