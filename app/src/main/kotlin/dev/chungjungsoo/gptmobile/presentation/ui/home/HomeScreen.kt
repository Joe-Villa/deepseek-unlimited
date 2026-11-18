package dev.chungjungsoo.gptmobile.presentation.ui.home

import android.widget.Toast
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.presentation.common.CirclePlusIcon
import dev.chungjungsoo.gptmobile.presentation.common.DeepSeekMenuIcon
import dev.chungjungsoo.gptmobile.data.database.entity.ChatRoomV2
import dev.chungjungsoo.gptmobile.presentation.ui.chat.ChatAttachmentDraft
import dev.chungjungsoo.gptmobile.presentation.ui.chat.ChatInputBox
import dev.chungjungsoo.gptmobile.presentation.ui.home.HomeViewModel.ChatListState
import java.util.Calendar
import kotlinx.coroutines.launch

/**
 * DeepSeek-style home: a quiet greeting, one new-chat action, and the whole
 * history in the left drawer. The drawer is the only place where conversations
 * are listed, matching the official client's structure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    settingOnClick: () -> Unit,
    onExistingChatClick: (ChatRoomV2) -> Unit,
    navigateToNewChat: (enabledPlatforms: List<String>) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val chatListState by homeViewModel.chatListState.collectAsStateWithLifecycle()
    val platformState by homeViewModel.platformState.collectAsStateWithLifecycle()
    val activeChatIds by homeViewModel.activeChatIds.collectAsStateWithLifecycle()
    val searchQuery by homeViewModel.searchQuery.collectAsStateWithLifecycle()
    val showDeleteWarningDialog by homeViewModel.showDeleteWarningDialog.collectAsStateWithLifecycle()
    val selectedChatCount = chatListState.selectedChats.count { it }
    val inputState = rememberTextFieldState()
    val attachments = remember { mutableStateListOf<ChatAttachmentDraft>() }
    var thinkingEnabled by remember { mutableStateOf(true) }
    var webSearchEnabled by remember { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun hideKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    val openNewChat = {
        val enabled = platformState.filter { it.enabled }.map { it.uid }
        if (enabled.isEmpty()) {
            settingOnClick()
        } else {
            navigateToNewChat(enabled)
        }
    }

    val startChat = {
        HomeChatDraft.text = inputState.text.toString()
        HomeChatDraft.thinking = thinkingEnabled
        HomeChatDraft.webSearch = webSearchEnabled
        inputState.clearText()
        attachments.clear()
        openNewChat()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        homeViewModel.fetchChats()
        homeViewModel.fetchPlatformStatus()
    }

    BackHandler(enabled = drawerState.isOpen || chatListState.isSelectionMode) {
        when {
            chatListState.isSelectionMode -> homeViewModel.disableSelectionMode()
            else -> scope.launch { drawerState.close() }
        }
    }

    LaunchedEffect(Unit) {
        HomeDrawerRequest.pending.collect { pending ->
            if (pending) {
                HomeDrawerRequest.consume()
                drawerState.open()
            }
        }
    }

    // Like the official app: opening the history drawer dismisses the IME so the
    // composer and the drawer are not pushed around by the keyboard.
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawer(
                chatListState = chatListState,
                searchQuery = searchQuery,
                activeChatIds = activeChatIds,
                onSearchQueryChange = homeViewModel::updateSearchQuery,
                onChatClick = { chat ->
                    scope.launch { drawerState.close() }
                    onExistingChatClick(chat)
                },
                onEnterSelection = homeViewModel::enableSelectionMode,
                onCancelSelection = homeViewModel::disableSelectionMode,
                onToggleSelection = homeViewModel::selectChat,
                onDeleteSelected = homeViewModel::openDeleteWarningDialog,
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    settingOnClick()
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = {
                            hideKeyboard()
                            scope.launch { drawerState.open() }
                        }) {
                            DeepSeekMenuIcon(tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            Toast.makeText(context, context.getString(R.string.ds_home_no_answer), Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.VolumeUp,
                                contentDescription = stringResource(R.string.ds_read_aloud),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = {
                            HomeChatDraft.reset()
                            inputState.clearText()
                            attachments.clear()
                            openNewChat()
                        }) {
                            CirclePlusIcon(tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
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
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    HomeHero()
                }
                ChatInputBox(
                    inputState = inputState,
                    chatEnabled = true,
                    sendButtonEnabled = inputState.text.isNotBlank() || attachments.isNotEmpty(),
                    isRunning = false,
                    thinkingEnabled = thinkingEnabled,
                    webSearchEnabled = webSearchEnabled,
                    onThinkingToggle = { thinkingEnabled = !thinkingEnabled },
                    onWebSearchToggle = { webSearchEnabled = !webSearchEnabled },
                    selectedAttachments = attachments,
                    onFileSelected = { path ->
                        attachments.add(ChatAttachmentDraft(sourceFilePath = path))
                        HomeChatDraft.attachments.add(path)
                    },
                    onFileRemoved = { path ->
                        attachments.removeAll { it.sourceFilePath == path }
                        HomeChatDraft.attachments.remove(path)
                    },
                    onSendButtonClick = startChat
                )
                Text(
                    text = stringResource(R.string.ds_home_ai_footer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
            }
        }

    }

    if (showDeleteWarningDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = homeViewModel::closeDeleteWarningDialog,
            title = { Text(stringResource(R.string.ds_home_delete_title)) },
            text = { Text(stringResource(R.string.ds_home_delete_message, selectedChatCount)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        homeViewModel.closeDeleteWarningDialog()
                        homeViewModel.deleteSelectedChats()
                    }
                ) {
                    Text(stringResource(R.string.ds_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = homeViewModel::closeDeleteWarningDialog) {
                    Text(stringResource(R.string.ds_cancel))
                }
            }
        )
    }
}

@Composable
internal fun HomeDrawer(
    chatListState: ChatListState,
    searchQuery: String,
    activeChatIds: Set<Int>,
    onSearchQueryChange: (String) -> Unit,
    onChatClick: (ChatRoomV2) -> Unit,
    onEnterSelection: () -> Unit = {},
    onCancelSelection: () -> Unit = {},
    onToggleSelection: (Int) -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onSettingsClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(326.dp)
            .fillMaxHeight(),
        drawerShape = RoundedCornerShape(0.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Spacer(Modifier.height(10.dp))
            DrawerSearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            )
            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    chatListState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    chatListState.loadError != null -> {
                        DrawerHint(
                            title = stringResource(R.string.ds_home_load_failed),
                            detail = chatListState.loadError.orEmpty(),
                            modifier = Modifier.padding(20.dp)
                        )
                    }

                    chatListState.chats.isEmpty() -> {
                        DrawerHint(
                            title = if (searchQuery.isBlank()) stringResource(R.string.ds_home_empty_title) else stringResource(R.string.ds_home_no_search_title),
                            detail = if (searchQuery.isBlank()) stringResource(R.string.ds_home_empty_desc) else stringResource(R.string.ds_home_no_search_desc),
                            modifier = Modifier.padding(20.dp)
                        )
                    }

                    else -> {
                        ChatHistoryList(
                            chats = chatListState.chats,
                            activeChatIds = activeChatIds,
                            isSearching = searchQuery.isNotBlank(),
                            selectionMode = chatListState.isSelectionMode,
                            selectedChats = chatListState.selectedChats,
                            onEnterSelection = onEnterSelection,
                            onCancelSelection = onCancelSelection,
                            onToggleSelection = onToggleSelection,
                            onChatClick = onChatClick
                        )
                    }
                }
            }

            if (chatListState.isSelectionMode) {
                DrawerSelectionFooter(
                    selectedCount = chatListState.selectedChats.count { it },
                    onCancel = onCancelSelection,
                    onDelete = onDeleteSelected
                )
            } else {
                DrawerFooter(
                    chatCount = chatListState.chats.size,
                    onSettingsClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun DrawerSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp)
            )
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isBlank()) {
                    Text(
                        text = stringResource(R.string.ds_home_search_hint),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ChatHistoryList(
    chats: List<ChatRoomV2>,
    activeChatIds: Set<Int>,
    isSearching: Boolean,
    selectionMode: Boolean,
    selectedChats: List<Boolean>,
    onEnterSelection: () -> Unit,
    onCancelSelection: () -> Unit,
    onToggleSelection: (Int) -> Unit,
    onChatClick: (ChatRoomV2) -> Unit
) {
    val grouped = if (isSearching) {
        listOf(stringResource(R.string.ds_home_search_results) to chats)
    } else {
        groupChats(chats)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 12.dp)
    ) {
        grouped.forEachIndexed { groupIndex, (label, groupChats) ->
            item(key = "group-$label") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (label) {
                            "today" -> stringResource(R.string.ds_today)
                            "yesterday" -> stringResource(R.string.ds_yesterday)
                            "week" -> stringResource(R.string.ds_last_7_days)
                            else -> stringResource(R.string.ds_older)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        modifier = Modifier.weight(1f)
                    )
                    if (groupIndex == 0) {
                        Icon(
                            imageVector = if (selectionMode) Icons.Rounded.Close else Icons.Rounded.Tune,
                            contentDescription = if (selectionMode) stringResource(R.string.ds_home_exit_selection) else stringResource(R.string.ds_home_select_chats),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(19.dp)
                                .clickable {
                                    if (selectionMode) onCancelSelection() else onEnterSelection()
                                }
                        )
                    }
                }
            }
            items(groupChats, key = { it.id }) { chat ->
                val globalIndex = chats.indexOfFirst { it.id == chat.id }
                DrawerChatRow(
                    chat = chat,
                    isActive = chat.id in activeChatIds,
                    selectionMode = selectionMode,
                    selected = selectedChats.getOrElse(globalIndex) { false },
                    onClick = {
                        if (selectionMode) onToggleSelection(globalIndex) else onChatClick(chat)
                    },
                    onLongClick = {
                        if (!selectionMode) {
                            onEnterSelection()
                            onToggleSelection(globalIndex)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DrawerChatRow(
    chat: ChatRoomV2,
    isActive: Boolean,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 10.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectionMode) {
            Icon(
                imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = chat.title.ifBlank { stringResource(R.string.ds_new_chat) },
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DrawerSelectionFooter(
    selectedCount: Int,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.ds_home_selected_count, selectedCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        androidx.compose.material3.TextButton(onClick = onCancel) {
            Text(stringResource(R.string.ds_cancel))
        }
        androidx.compose.material3.TextButton(onClick = onDelete, enabled = selectedCount > 0) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.ds_delete), color = if (selectedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DrawerFooter(chatCount: Int, onSettingsClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
    ) {
        androidx.compose.material3.HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSettingsClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.ds_settings),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DrawerHint(title: String, detail: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        )
    }
}

@Composable
internal fun HomeHero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 44.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_whale),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = stringResource(R.string.ds_home_greeting),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                lineHeight = 24.sp
            ),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
internal fun NewChatPrimaryButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.ds_new_chat),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun groupChats(chats: List<ChatRoomV2>): List<Pair<String, List<ChatRoomV2>>> {
    val now = Calendar.getInstance()
    val todayStart = now.clone() as Calendar
    todayStart.set(Calendar.HOUR_OF_DAY, 0)
    todayStart.set(Calendar.MINUTE, 0)
    todayStart.set(Calendar.SECOND, 0)
    todayStart.set(Calendar.MILLISECOND, 0)
    val todaySeconds = todayStart.timeInMillis / 1000
    val yesterdaySeconds = todaySeconds - 86_400
    val weekSeconds = todaySeconds - 7 * 86_400

    val groups = linkedMapOf(
        "today" to mutableListOf<ChatRoomV2>(),
        "yesterday" to mutableListOf<ChatRoomV2>(),
        "week" to mutableListOf<ChatRoomV2>(),
        "older" to mutableListOf<ChatRoomV2>()
    )
    chats.forEach { chat ->
        val label = when {
            chat.updatedAt >= todaySeconds -> "today"
            chat.updatedAt >= yesterdaySeconds -> "yesterday"
            chat.updatedAt >= weekSeconds -> "week"
            else -> "older"
        }
        groups.getValue(label) += chat
    }
    return groups.filterValues { it.isNotEmpty() }.map { it.key to it.value }
}
