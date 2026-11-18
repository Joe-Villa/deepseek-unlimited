package dev.chungjungsoo.gptmobile.data.archive

import dev.chungjungsoo.gptmobile.data.database.entity.AssistantTimelineItem
import dev.chungjungsoo.gptmobile.data.database.entity.AssistantTimelineItemType
import dev.chungjungsoo.gptmobile.data.database.entity.MessageV2
import dev.chungjungsoo.gptmobile.data.database.entity.isEffectivelyBlank
import dev.chungjungsoo.gptmobile.data.model.ChatAttachment
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

data class ParsedChatArchive(
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<MessageV2>
)

/**
 * Parser for the chat JSON shipped by DeepSeek's official `deepseek_data-*.zip`
 * (`export_format_version: 3`). It is deliberately independent of Android so the
 * tricky bits — current branch, fragments, sources, attachments — can be tested.
 */
object DeepSeekArchiveCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseChat(
        text: String,
        platformUid: String,
        attachments: Map<String, ChatAttachment> = emptyMap(),
        fallbackTitle: String = "Untitled"
    ): ParsedChatArchive? {
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return null
        val raw = root["messages"]?.asArray()?.mapNotNull { it.asObject() } ?: return null
        if (raw.isEmpty()) return null

        val byId = raw.mapNotNull { message ->
            message.long("id")?.let { it to message }
        }.toMap()
        val path = currentPath(root, raw, byId)
        if (path.isEmpty()) return null

        val messages = path.mapNotNull { message ->
            parseMessage(message, platformUid, attachments)
        }.filterNot { it.isEffectivelyBlank() }
        if (messages.isEmpty()) return null

        val createdAt = parseInstantSeconds(root.string("inserted_at")) ?: messages.firstOrNull()?.createdAt ?: now()
        val updatedAt = parseInstantSeconds(root.string("updated_at")) ?: messages.lastOrNull()?.createdAt ?: createdAt
        val title = root.string("title")?.trim().orEmpty().ifBlank { fallbackTitle }
        return ParsedChatArchive(
            title = title,
            createdAt = createdAt,
            updatedAt = maxOf(updatedAt, createdAt),
            messages = messages
        )
    }

    private fun currentPath(
        root: JsonObject,
        raw: List<JsonObject>,
        byId: Map<Long, JsonObject>
    ): List<JsonObject> {
        val current = root.long("current_message_id")
        val path = mutableListOf<JsonObject>()
        val seen = mutableSetOf<Long>()
        var cursor = current
        while (cursor != null && byId.containsKey(cursor) && seen.add(cursor)) {
            val message = byId.getValue(cursor)
            path += message
            cursor = message.long("parent_id")
        }
        path.reverse()
        return path.ifEmpty {
            raw.sortedBy { it.long("id") ?: 0L }
        }
    }

    private fun parseMessage(
        message: JsonObject,
        platformUid: String,
        attachments: Map<String, ChatAttachment>
    ): MessageV2? {
        val role = message.string("role")?.uppercase().orEmpty()
        val fragments = message["fragments"]?.asArray()?.mapNotNull { it.asObject() }.orEmpty()
        val createdAt = parseInstantSeconds(message.string("inserted_at")) ?: now()

        return when (role) {
            "USER" -> {
                val content = fragments
                    .filter { it.string("type") == "REQUEST" }
                    .mapNotNull { it.string("content") }
                    .joinToString("\n")
                val files = fragments
                    .filter { it.string("type") == "FILE" }
                    .flatMap { fragment -> fragment["files"]?.asArray()?.mapNotNull { it.asObject() }.orEmpty() }
                    .mapNotNull { attachment -> attachment.string("export_path")?.let(attachments::get) }
                if (content.isBlank() && files.isEmpty()) {
                    null
                } else {
                    MessageV2(
                        content = content,
                        attachments = files,
                        platformType = null,
                        createdAt = createdAt
                    )
                }
            }

            "ASSISTANT" -> {
                val thoughts = fragments
                    .filter { it.string("type") == "THINK" }
                    .mapNotNull { it.string("content") }
                    .joinToString("\n\n")
                val response = fragments
                    .filter {
                        val type = it.string("type")
                        type == "RESPONSE" || type == "TEMPLATE_RESPONSE"
                    }
                    .mapNotNull { it.string("content") }
                    .joinToString("\n\n")
                val risk = fragments
                    .filter { it.string("type") == "CONTENT_RISK" }
                    .mapNotNull { it.string("content") }
                    .joinToString("\n\n")
                val content = response.ifBlank { risk }
                val sources = collectSources(fragments)
                if (content.isBlank() && thoughts.isBlank() && sources.isEmpty()) {
                    null
                } else {
                    MessageV2(
                        thoughts = thoughts,
                        content = content,
                        platformType = platformUid,
                        createdAt = createdAt,
                        timeline = sources.takeIf { it.isNotEmpty() }?.let {
                            listOf(
                                AssistantTimelineItem(
                                    type = AssistantTimelineItemType.SERVER_TOOL,
                                    content = buildJsonObject {
                                        put("kind", "annotations")
                                        put("value", JsonArray(it))
                                    }.toString()
                                )
                            )
                        }.orEmpty()
                    )
                }
            }

            else -> null
        }
    }

    private fun collectSources(fragments: List<JsonObject>): List<JsonObject> {
        val seen = linkedSetOf<String>()
        val result = mutableListOf<JsonObject>()
        fun add(resultElement: JsonElement?) {
            val obj = resultElement?.asObject() ?: return
            val url = obj.string("url")?.takeIf { it.isNotBlank() } ?: return
            if (!seen.add(url)) return
            result += buildJsonObject {
                put("url", url)
                obj.string("title")?.takeIf { it.isNotBlank() }?.let { put("title", it) }
                obj.string("site_name")?.takeIf { it.isNotBlank() }?.let { put("site_name", it) }
            }
        }

        fragments.forEach { fragment ->
            when (fragment.string("type")) {
                "TOOL_SEARCH" -> fragment["results"]?.asArray()?.forEach { add(it) }
                "TOOL_OPEN" -> add(fragment)
                "READ_LINK" -> fragment["results"]?.asArray()?.forEach { add(it) }
            }
        }
        return result
    }

    fun exportSources(timeline: List<AssistantTimelineItem>): List<JsonObject> {
        val seen = linkedSetOf<String>()
        val result = mutableListOf<JsonObject>()
        timeline.forEach { item ->
            if (item.type != AssistantTimelineItemType.SERVER_TOOL) return@forEach
            val payload = runCatching { json.parseToJsonElement(item.content).jsonObject }.getOrNull() ?: return@forEach
            if (payload.string("kind") != "annotations") return@forEach
            payload["value"]?.asArray()?.forEach { element ->
                val obj = element.asObject() ?: return@forEach
                val url = obj.string("url") ?: return@forEach
                if (!seen.add(url)) return@forEach
                result += buildJsonObject {
                    put("url", url)
                    put("title", obj.string("title").orEmpty())
                    put("snippet", "")
                    put("published_at", JsonNull)
                    put("site_icon", JsonNull)
                    put("site_name", obj.string("site_name").orEmpty())
                }
            }
        }
        return result
    }

    private val exportFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)

    fun exportTime(epochSeconds: Long): String =
        exportFormatter.format(Instant.ofEpochSecond(epochSeconds))

    private fun parseInstantSeconds(value: String?): Long? = runCatching {
        Instant.parse(value).epochSecond
    }.getOrNull()

    private fun now(): Long = System.currentTimeMillis() / 1000
}

private fun JsonElement?.asObject(): JsonObject? = runCatching { this?.jsonObject }.getOrNull()
private fun JsonElement?.asArray(): JsonArray? = runCatching { this?.jsonArray }.getOrNull()
private fun JsonObject.string(key: String): String? = this[key]?.let { runCatching { it.jsonPrimitive.contentOrNull }.getOrNull() }
private fun JsonObject.long(key: String): Long? = this[key]?.let { runCatching { it.jsonPrimitive.longOrNull }.getOrNull() }
