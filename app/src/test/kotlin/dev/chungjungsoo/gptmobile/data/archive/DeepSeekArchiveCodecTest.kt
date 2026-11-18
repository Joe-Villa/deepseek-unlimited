package dev.chungjungsoo.gptmobile.data.archive

import dev.chungjungsoo.gptmobile.data.model.ChatAttachment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepSeekArchiveCodecTest {
    private val chatJson = """
        {
          "id": "chat-uuid",
          "title": "导入测试",
          "pinned": false,
          "current_message_id": 4,
          "inserted_at": "2026-11-16T11:03:17.588Z",
          "updated_at": "2026-11-16T11:15:35.017Z",
          "messages": [
            {
              "id": 1, "parent_id": null, "role": "USER", "status": "FINISHED",
              "thinking_enabled": true, "search_enabled": true, "accumulated_token_usage": 69,
              "inserted_at": "2026-11-16T11:15:28.920Z",
              "fragments": [
                {"type": "FILE", "files": [{"id": "f1", "file_name": "chart.png", "file_size": 12, "inserted_at": "2026-11-16T11:15:28.000Z", "export_path": "chat_sessions/chat-uuid_files/f1_chart.png", "is_image": true, "width": 100, "height": 50}]},
                {"type": "REQUEST", "content": "这张图说明什么？"}
              ]
            },
            {
              "id": 2, "parent_id": 1, "role": "ASSISTANT", "status": "FINISHED",
              "thinking_enabled": true, "search_enabled": true, "accumulated_token_usage": 317,
              "inserted_at": "2026-11-16T11:15:29.000Z",
              "fragments": [
                {"type": "THINK", "content": "先看图。"},
                {"type": "TOOL_SEARCH", "queries": ["chart"], "results": [{"url": "https://example.com/a", "title": "A", "site_name": "Example"}]},
                {"type": "TOOL_OPEN", "url": "https://example.com/b", "title": "B", "site_name": "Example B"},
                {"type": "RESPONSE", "content": "这是回答。"}
              ]
            },
            {"id": 3, "parent_id": 2, "role": "USER", "status": "FINISHED", "thinking_enabled": true, "search_enabled": true, "accumulated_token_usage": 1, "inserted_at": "2026-11-16T11:15:30.000Z", "fragments": [{"type": "REQUEST", "content": "继续"}]},
            {"id": 4, "parent_id": 3, "role": "ASSISTANT", "status": "FINISHED", "thinking_enabled": true, "search_enabled": false, "accumulated_token_usage": 1, "inserted_at": "2026-11-16T11:15:31.000Z", "fragments": [{"type": "RESPONSE", "content": "好。"}]},
            {"id": 99, "parent_id": 2, "role": "ASSISTANT", "status": "FINISHED", "thinking_enabled": true, "search_enabled": false, "accumulated_token_usage": 1, "inserted_at": "2026-11-16T11:15:32.000Z", "fragments": [{"type": "RESPONSE", "content": "分支内容不应导入"}]}
          ]
        }
    """.trimIndent()

    @Test
    fun parsesCurrentBranchAndFragments() {
        val attachment = ChatAttachment(
            localFilePath = "/tmp/chart.png",
            preparedFilePath = "/tmp/chart.png",
            displayName = "chart.png",
            mimeType = "image/png",
            sizeBytes = 12
        )
        val parsed = DeepSeekArchiveCodec.parseChat(
            chatJson,
            platformUid = "ds",
            attachments = mapOf("chat_sessions/chat-uuid_files/f1_chart.png" to attachment)
        )!!
        assertEquals("导入测试", parsed.title)
        assertEquals(4, parsed.messages.size)
        assertEquals("这张图说明什么？", parsed.messages[0].content)
        assertEquals(1, parsed.messages[0].attachments.size)
        assertEquals("先看图。", parsed.messages[1].thoughts)
        assertEquals("这是回答。", parsed.messages[1].content)
        assertTrue(parsed.messages[1].timeline.single().content.contains("https://example.com/a"))
        assertTrue(parsed.messages[1].timeline.single().content.contains("https://example.com/b"))
        assertEquals("继续", parsed.messages[2].content)
        assertEquals("好。", parsed.messages[3].content)
        assertTrue(parsed.messages.none { it.content.contains("分支内容") })
    }

    @Test
    fun exportTimeUsesUtcIso() {
        assertEquals("2025-11-16T03:15:35.000Z", DeepSeekArchiveCodec.exportTime(1763262935))
    }
}
