package dev.chungjungsoo.gptmobile

import dev.chungjungsoo.gptmobile.data.agent.ProviderEvent
import dev.chungjungsoo.gptmobile.data.agent.provider.ChatCompletionsEventAssembler
import dev.chungjungsoo.gptmobile.data.dto.openai.common.Role
import dev.chungjungsoo.gptmobile.data.dto.openai.request.ChatCompletionRequest
import dev.chungjungsoo.gptmobile.data.dto.openai.request.ChatMessage
import dev.chungjungsoo.gptmobile.data.dto.openai.request.ChatThinkingConfig
import dev.chungjungsoo.gptmobile.data.dto.openai.response.ChatCompletionChunk
import dev.chungjungsoo.gptmobile.data.network.NetworkClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class DeepSeekWireFormatTest {

    @Test
    fun `request carries thinking server tools and replay fields`() {
        val historyToolCalls = Json.parseToJsonElement(
            """[{"index":0,"reasoning_content":"","calls":[{"index":0,"id":"call_1","type":"deepseek_read_url","kind":"read_link","action":{"type":"read_link","url":"https://example.com"},"status":"completed","page":{"url":"https://example.com","title":"Example","published_at":null},"signature":"sig"}]}]"""
        )
        val request = ChatCompletionRequest(
            model = "deepseek-flash",
            messages = listOf(
                ChatMessage(
                    role = Role.ASSISTANT,
                    content = listOf(dev.chungjungsoo.gptmobile.data.dto.openai.common.TextContent("answer")),
                    reasoningContent = "reasoning",
                    serverToolCalls = historyToolCalls
                )
            ),
            thinking = ChatThinkingConfig(),
            reasoningEffort = "high",
            tools = listOf(
                buildJsonObject { put("type", "deepseek_web_search") },
                buildJsonObject { put("type", "deepseek_read_url") }
            )
        )

        val encoded = NetworkClient.openAIJson.encodeToString(request)
        assertTrue(encoded, encoded.contains("\"thinking\":{\"type\":\"enabled\"}"))
        assertTrue(encoded, encoded.contains("\"reasoning_effort\":\"high\""))
        assertTrue(encoded, encoded.contains("\"reasoning_content\":\"reasoning\""))
        assertTrue(encoded, encoded.contains("\"server_tool_calls\""))
        assertTrue(encoded, encoded.contains("\"type\":\"deepseek_web_search\""))
        assertTrue(encoded, encoded.contains("\"type\":\"deepseek_read_url\""))
    }

    @Test
    fun `stream parses reasoning tool events and citations`() {
        val chunk = NetworkClient.openAIJson.decodeFromString<ChatCompletionChunk>(
            """
            {
              "choices": [{
                "index": 0,
                "delta": {"reasoning_content": "先想一想", "content": "答案"},
                "server_tool_event": {
                  "type": "call_started",
                  "step": 0,
                  "call": {
                    "index": 0,
                    "id": "call_1",
                    "type": "deepseek_web_search",
                    "kind": "search",
                    "action": {"type": "search", "queries": ["DeepSeek V4.1"]}
                  }
                }
              }]
            }
            """.trimIndent()
        )
        val event = chunk.choices!!.single().serverToolEvent
        assertTrue(event != null)
        assertEquals("先想一想", chunk.choices!!.single().delta.reasoningContent)

        val events = ChatCompletionsEventAssembler().accept(
            content = chunk.choices!!.single().delta.content,
            reasoning = chunk.choices!!.single().delta.reasoningContent,
            toolCalls = null,
            finishReason = null,
            serverToolEvent = event
        )
        assertTrue(events.any { it is ProviderEvent.ThinkingDelta })
        assertTrue(events.any { it is ProviderEvent.TextDelta })
        assertTrue(events.any { it is ProviderEvent.ServerTool })
    }
}
