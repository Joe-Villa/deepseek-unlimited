package dev.chungjungsoo.gptmobile.data.archive

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import dev.chungjungsoo.gptmobile.data.database.entity.ChatRoomV2
import dev.chungjungsoo.gptmobile.data.database.entity.MessageV2
import dev.chungjungsoo.gptmobile.data.database.entity.PlatformV2
import dev.chungjungsoo.gptmobile.data.model.ClientType
import dev.chungjungsoo.gptmobile.data.repository.ChatRepository
import dev.chungjungsoo.gptmobile.data.repository.SettingRepository
import java.io.File
import java.lang.reflect.Proxy
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChatArchiveManagerTest {
    private val chatJson = """
        {
          "id": "official-chat",
          "title": "官方导入对话",
          "pinned": false,
          "current_message_id": 2,
          "inserted_at": "2026-11-16T11:03:17.588Z",
          "updated_at": "2026-11-16T11:15:35.017Z",
          "messages": [
            {"id": 1, "parent_id": null, "role": "USER", "status": "FINISHED", "thinking_enabled": true, "search_enabled": true, "accumulated_token_usage": 1, "inserted_at": "2026-11-16T11:15:28.920Z", "fragments": [{"type": "FILE", "files": [{"file_name": "chart.png", "file_size": 4, "export_path": "chat_sessions/official-chat_files/f1_chart.png", "is_image": true}]}, {"type": "REQUEST", "content": "看看图"}]},
            {"id": 2, "parent_id": 1, "role": "ASSISTANT", "status": "FINISHED", "thinking_enabled": true, "search_enabled": true, "accumulated_token_usage": 2, "inserted_at": "2026-11-16T11:15:29.000Z", "fragments": [{"type": "THINK", "content": "先看图。"}, {"type": "TOOL_SEARCH", "queries": ["chart"], "results": [{"url": "https://example.com/a", "title": "A", "site_name": "Example"}]}, {"type": "RESPONSE", "content": "这是回答。"}]}
          ]
        }
    """.trimIndent()

    @Test
    fun importsAndExportsOfficialShape() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val chats = mutableListOf<ChatRoomV2>()
        val messages = mutableMapOf<Int, List<MessageV2>>()
        var nextId = 100

        val repo = Proxy.newProxyInstance(
            ChatRepository::class.java.classLoader,
            arrayOf(ChatRepository::class.java)
        ) { _, method, args ->
            when (method.name) {
                "fetchChatListV2" -> chats.toList()
                "fetchMessagesV2" -> messages[args!![0] as Int].orEmpty()
                "saveChatPlatformModels" -> Unit
                "importChatSnapshot" -> {
                    val room = args!![0] as ChatRoomV2
                    val imported = args[1] as List<MessageV2>
                    val id = nextId++
                    val saved = room.copy(id = id)
                    chats += saved
                    messages[id] = imported.map { it.copy(chatId = id) }
                    saved
                }
                "toString" -> "FakeChatRepository"
                "hashCode" -> System.identityHashCode(this)
                "equals" -> false
                else -> error("Unexpected ChatRepository.${method.name}")
            }
        } as ChatRepository

        val settingRepo = Proxy.newProxyInstance(
            SettingRepository::class.java.classLoader,
            arrayOf(SettingRepository::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "fetchPlatformV2s" -> listOf(
                    PlatformV2(
                        uid = "ds",
                        name = "DeepSeek",
                        compatibleType = ClientType.OPENAI,
                        enabled = true,
                        apiUrl = "https://api.deepseek.com",
                        model = "deepseek-flash"
                    )
                )
                "toString" -> "FakeSettingRepository"
                "hashCode" -> System.identityHashCode(this)
                "equals" -> false
                else -> error("Unexpected SettingRepository.${method.name}")
            }
        } as SettingRepository

        val manager = ChatArchiveManager(context, repo, settingRepo)
        val sourceZip = File(context.cacheDir, "source.zip")
        ZipOutputStream(sourceZip.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("chat_sessions/official-chat.json"))
            zip.write(chatJson.toByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("chat_sessions/official-chat_files/f1_chart.png"))
            zip.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47))
            zip.closeEntry()
        }

        val imported = manager.importZip(Uri.fromFile(sourceZip))
        assertEquals(1, imported.importedChats)
        assertEquals(2, imported.importedMessages)
        assertEquals(1, imported.importedAttachments)
        assertEquals("官方导入对话", chats.single().title)
        assertEquals(1, messages.values.single()[0].attachments.size)

        val exportFile = File(context.cacheDir, "export.zip")
        val exported = manager.exportZip(Uri.fromFile(exportFile))
        assertEquals(1, exported.importedChats)
        ZipFile(exportFile).use { zip ->
            assertTrue(zip.getEntry("manifest.json") != null)
            assertTrue(zip.getEntry("user.json") != null)
            val chatEntry = zip.entries().asSequence().first { it.name.startsWith("chat_sessions/") && it.name.endsWith(".json") }
            val text = zip.getInputStream(chatEntry).bufferedReader().readText()
            assertTrue(text.contains("官方导入对话"))
            assertTrue(text.contains("\"type\":\"REQUEST\""))
            assertTrue(text.contains("\"type\":\"RESPONSE\""))
            assertTrue(zip.entries().asSequence().any { it.name.contains("_files/") })
        }
    }
}
