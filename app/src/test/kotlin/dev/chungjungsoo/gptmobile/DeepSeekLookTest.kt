package dev.chungjungsoo.gptmobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.chungjungsoo.gptmobile.data.database.entity.AssistantTimelineItem
import dev.chungjungsoo.gptmobile.data.database.entity.AssistantTimelineItemType
import dev.chungjungsoo.gptmobile.data.model.ThemeMode
import dev.chungjungsoo.gptmobile.presentation.theme.GPTMobileTheme
import dev.chungjungsoo.gptmobile.presentation.ui.chat.ChatInputBox
import dev.chungjungsoo.gptmobile.presentation.ui.chat.OpponentChatBubble
import dev.chungjungsoo.gptmobile.presentation.ui.chat.UserChatBubble
import dev.chungjungsoo.gptmobile.data.database.entity.ChatRoomV2
import dev.chungjungsoo.gptmobile.presentation.common.CirclePlusIcon
import dev.chungjungsoo.gptmobile.presentation.common.DeepSeekMenuIcon
import dev.chungjungsoo.gptmobile.presentation.ui.home.HomeDrawer
import dev.chungjungsoo.gptmobile.presentation.ui.home.HomeHero
import dev.chungjungsoo.gptmobile.presentation.ui.home.HomeViewModel
import dev.chungjungsoo.gptmobile.presentation.ui.home.NewChatPrimaryButton
import dev.chungjungsoo.gptmobile.presentation.ui.setup.DeepSeekSetupContent
import dev.chungjungsoo.gptmobile.presentation.ui.setup.DeepSeekSetupViewModel
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders key surfaces in the JVM so the DeepSeek look can be checked
 * without a device. Output lands in app/build/screenshots/.
 *
 * Order is fixed on purpose: the markdown renderer warms up asynchronously,
 * and the first chat capture must not run after another screen has taken over
 * the Robolectric window.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class DeepSeekLookTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun aChatScreenLight() {
        composeRule.setContent {
            GPTMobileTheme(themeMode = ThemeMode.LIGHT) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        UserChatBubble(text = "我今天有点累，想找个人说说话。")
                        Spacer(Modifier.height(18.dp))
                        OpponentChatBubble(
                            canRetry = true,
                            isLoading = false,
                            text = "我在。先别急着做什么，愿意的话，跟我说说今天发生了什么？"
                        )
                        Spacer(Modifier.height(28.dp))
                        ChatInputBox(inputState = rememberTextFieldState("我最近总是睡不好"))
                    }
                }
            }
        }
        settle()
        composeRule.onRoot().captureRoboImage("build/screenshots/chat-light.png")
    }

    @Test
    fun bChatScreenDark() {
        composeRule.setContent {
            GPTMobileTheme(themeMode = ThemeMode.DARK) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        UserChatBubble(text = "我今天有点累，想找个人说说话。")
                        Spacer(Modifier.height(18.dp))
                        OpponentChatBubble(
                            canRetry = true,
                            isLoading = false,
                            text = "我在。先别急着做什么，愿意的话，跟我说说今天发生了什么？"
                        )
                        Spacer(Modifier.height(28.dp))
                        ChatInputBox(inputState = rememberTextFieldState("我最近总是睡不好"))
                    }
                }
            }
        }
        settle()
        composeRule.onRoot().captureRoboImage("build/screenshots/chat-dark.png")
    }

    @Test
    fun cHomeScreen() {
        composeRule.setContent {
            GPTMobileTheme(themeMode = ThemeMode.LIGHT) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(vertical = 20.dp)) {
                        Text(
                            text = "DeepSeek",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        NewChatPrimaryButton(onClick = {})
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "会话",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        listOf("今天聊的那些事", "关于失眠", "写给三年后的自己").forEach {
                            ListItem(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                headlineContent = { Text(it) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
        settle()
        composeRule.onRoot().captureRoboImage("build/screenshots/home-light.png")
    }

    @Test
    fun dSetupScreen() {
        composeRule.setContent {
            GPTMobileTheme(themeMode = ThemeMode.DARK) {
                DeepSeekSetupContent(
                    uiState = DeepSeekSetupViewModel.UiState(apiKey = "sk-0f3a9c4e1b7d2a6f"),
                    onApiKeyChange = {},
                    onStart = {},
                    onGetKey = {}
                )
            }
        }
        settle()
        composeRule.onRoot().captureRoboImage("build/screenshots/setup-dark.png")
    }


    @Test
    fun eChatThinkingAndSearchDark() {
        val timeline = listOf(
            AssistantTimelineItem(
                type = AssistantTimelineItemType.SERVER_TOOL,
                content = """{"kind":"event","value":{"type":"call_completed","step":0,"call":{"index":0,"id":"call_1","type":"deepseek_web_search","kind":"search","action":{"type":"search","queries":["DeepSeek V4.1 更新"]},"status":"completed","sources":[{"type":"url","url":"https://api-docs.deepseek.com/news/news261017/","title":"DeepSeek V4.1 Flash 新版本","snippet":"...","site_name":"DeepSeek"}]}}}"""
            ),
            AssistantTimelineItem(
                type = AssistantTimelineItemType.SERVER_TOOL,
                content = """{"kind":"annotations","value":[{"type":"url_citation","url_citation":{"url":"https://api-docs.deepseek.com/news/news261017/","title":"DeepSeek V4.1 Flash 新版本","start_index":0,"end_index":10}}]}"""
            )
        )
        composeRule.setContent {
            GPTMobileTheme(themeMode = ThemeMode.DARK) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        UserChatBubble(text = "DeepSeek V4.1 有哪些更新？")
                        Spacer(Modifier.height(18.dp))
                        OpponentChatBubble(
                            canRetry = false,
                            isLoading = true,
                            text = "这次更新主要分为三块：",
                            thoughts = "先确认用户问的是 V4.1 的版本更新，需要同时查官方 changelog 和发布说明。\n\n第一步，先搜索 V4.1 的发布新闻；第二步，核对 Flash 的价格是否变化；第三步，整理成三条结论。",
                            timeline = timeline,
                            toolEvents = emptyList()
                        )
                        Spacer(Modifier.height(20.dp))
                        ChatInputBox(inputState = rememberTextFieldState("继续说说"))
                    }
                }
            }
        }
        settle()
        composeRule.onRoot().captureRoboImage("build/screenshots/chat-thinking-dark.png")
    }


    @Test
    fun fHomeHeroDark() {
        composeRule.setContent {
            GPTMobileTheme(themeMode = ThemeMode.DARK) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.IconButton(onClick = {}) {
                                DeepSeekMenuIcon(tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.weight(1f))
                            androidx.compose.material3.IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Outlined.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            androidx.compose.material3.IconButton(onClick = {}) {
                                CirclePlusIcon(tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            HomeHero()
                        }
                        ChatInputBox(
                            inputState = rememberTextFieldState(),
                            sendButtonEnabled = false
                        )
                        Text(
                            text = "回答由 AI 生成，仅供参考",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }
        settle()
        composeRule.onRoot().captureRoboImage("build/screenshots/home-dark.png")
    }


    @Test
    fun iSourcePillDark() {
        val timeline = listOf(
            AssistantTimelineItem(
                type = AssistantTimelineItemType.SERVER_TOOL,
                content = """{"kind":"annotations","value":[{"url":"https://www.bestbuy.com/site/","title":"Best Buy","site_name":"Best Buy"},{"url":"https://www.sohu.com/","title":"手机搜狐网","site_name":"手机搜狐网"},{"url":"https://www.googleapis.com/","title":"googleapis.com","site_name":"googleapis.com"},{"url":"https://www.ingdan.com/","title":"ingdan.com.hk","site_name":"ingdan.com.hk"}]}"""
            )
        )
        composeRule.setContent {
            GPTMobileTheme(themeMode = ThemeMode.DARK) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        UserChatBubble(text = "电脑续航短是因为功耗高吗？")
                        Spacer(Modifier.height(18.dp))
                        OpponentChatBubble(
                            canRetry = true,
                            isLoading = false,
                            text = "电脑电池容量还受物理之外的限制。民航规定，超过 100Wh 的锂电池不允许随身登机。所以多数笔记本电池被刻意限制在 99.9Wh 以内。",
                            thoughts = "",
                            timeline = timeline,
                            toolEvents = emptyList()
                        )
                    }
                }
            }
        }
        settle()
        composeRule.onRoot().captureRoboImage("build/screenshots/chat-source-dark.png")
    }

    @Test
    fun hHomeDrawerDark() {
        val chats = listOf(
            ChatRoomV2(id = 1, title = "电脑手机待机功耗差异", enabledPlatform = listOf("p")),
            ChatRoomV2(id = 2, title = "今晚吃什么", enabledPlatform = listOf("p")),
            ChatRoomV2(id = 3, title = "写给三年后的自己", enabledPlatform = listOf("p"))
        )
        composeRule.setContent {
            GPTMobileTheme(themeMode = ThemeMode.DARK) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    HomeDrawer(
                        chatListState = HomeViewModel.ChatListState(chats = chats, isLoading = false),
                        searchQuery = "",
                        activeChatIds = setOf(1),
                        onSearchQueryChange = {},
                        onChatClick = {},
                        onSettingsClick = {}
                    )
                }
            }
        }
        settle()
        composeRule.onRoot().captureRoboImage("build/screenshots/home-drawer-dark.png")
    }

    @Test
    fun gThinkingExpandedDark() {
        val timeline = listOf(
            AssistantTimelineItem(
                type = AssistantTimelineItemType.SERVER_TOOL,
                content = """{"kind":"event","value":{"type":"call_started","step":0,"call":{"index":0,"id":"call_1","type":"deepseek_web_search","kind":"search","action":{"type":"search","queries":["DeepSeek V4.1 更新"]}}}}"""
            )
        )
        composeRule.setContent {
            GPTMobileTheme(themeMode = ThemeMode.DARK) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        UserChatBubble(text = "DeepSeek V4.1 有哪些更新？")
                        Spacer(Modifier.height(18.dp))
                        OpponentChatBubble(
                            canRetry = false,
                            isLoading = true,
                            text = "",
                            thoughts = "先确认用户问的是 V4.1 的版本更新，需要同时查官方 changelog 和发布说明。\n\n第一步，先搜索 V4.1 的发布新闻；第二步，核对 Flash 的价格是否变化；第三步，整理成三条结论。",
                            timeline = timeline,
                            toolEvents = emptyList()
                        )
                    }
                }
            }
        }
        settle()
        composeRule.onRoot().captureRoboImage("build/screenshots/chat-thinking-expanded-dark.png")
    }

    private fun settle() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(800)
        composeRule.waitForIdle()
    }
}
