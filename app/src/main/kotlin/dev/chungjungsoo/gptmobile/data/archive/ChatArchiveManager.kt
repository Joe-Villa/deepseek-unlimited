package dev.chungjungsoo.gptmobile.data.archive

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import dev.chungjungsoo.gptmobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.chungjungsoo.gptmobile.data.database.entity.AssistantTimelineItem
import dev.chungjungsoo.gptmobile.data.database.entity.AssistantTimelineItemType
import dev.chungjungsoo.gptmobile.data.database.entity.ChatRoomV2
import dev.chungjungsoo.gptmobile.data.database.entity.MessageV2
import dev.chungjungsoo.gptmobile.data.model.ChatAttachment
import dev.chungjungsoo.gptmobile.data.repository.ChatRepository
import dev.chungjungsoo.gptmobile.data.repository.SettingRepository
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class ChatArchiveProgress(
    val label: String,
    val current: Int = 0,
    val total: Int = 0
)

data class ChatArchiveResult(
    val importedChats: Int = 0,
    val importedMessages: Int = 0,
    val importedAttachments: Int = 0,
    val skippedChats: Int = 0,
    val failedChats: Int = 0,
    val errors: List<String> = emptyList()
) {
    fun summary(context: Context): String {
        val base = context.getString(R.string.ds_archive_summary, importedChats, importedMessages)
        val extras = buildList {
            if (importedAttachments > 0) add(context.getString(R.string.ds_archive_attachment_count, importedAttachments))
            if (skippedChats > 0) add(context.getString(R.string.ds_archive_skipped, skippedChats))
            if (failedChats > 0) add(context.getString(R.string.ds_archive_failed, failedChats))
        }
        return if (extras.isEmpty()) base else "$base (${extras.joinToString(", ")})"
    }
}

@Singleton
class ChatArchiveManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val chatRepository: ChatRepository,
    private val settingRepository: SettingRepository
) {
    private val attachmentsDir: File
        get() = File(context.filesDir, "attachments").apply { mkdirs() }

    suspend fun importZip(
        uri: Uri,
        onProgress: (ChatArchiveProgress) -> Unit = {}
    ): ChatArchiveResult = withContext(Dispatchers.IO) {
        val platform = resolvePlatform()
        val extracted = mutableMapOf<String, ChatAttachment>()
        var totalChats = 0

        onProgress(ChatArchiveProgress(context.getString(R.string.ds_archive_prepare_import)))
        openZip(uri).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.isDirectory) {
                    zip.closeEntry()
                    continue
                }
                val name = entry.name
                when {
                    name.startsWith("chat_sessions/") && name.endsWith(".json") -> {
                        totalChats += 1
                        zip.closeEntry()
                    }

                    name.contains("_files/") -> {
                        val attachment = extractAttachment(zip, entry)
                        if (attachment != null) extracted[name] = attachment
                        zip.closeEntry()
                    }

                    else -> zip.closeEntry()
                }
            }
        }

        if (totalChats == 0) {
            return@withContext ChatArchiveResult(
                errors = listOf(context.getString(R.string.ds_archive_no_chats))
            )
        }

        val existing = chatRepository.fetchChatListV2()
            .associateBy { importKey(it.title, it.createdAt) }
            .toMutableMap()
        val errors = mutableListOf<String>()
        var importedChats = 0
        var importedMessages = 0
        var importedAttachments = 0
        var skippedChats = 0
        var failedChats = 0
        var current = 0

        openZip(uri).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.isDirectory) {
                    zip.closeEntry()
                    continue
                }
                if (!entry.name.startsWith("chat_sessions/") || !entry.name.endsWith(".json")) {
                    zip.closeEntry()
                    continue
                }
                current += 1
                val bytes = readEntry(zip, maxBytes = 12 * 1024 * 1024)
                val parsed = runCatching {
                    bytes?.toString(Charsets.UTF_8)
                        ?.let { DeepSeekArchiveCodec.parseChat(it, platform.uid, extracted, context.getString(R.string.ds_untitled)) }
                }.getOrNull()

                if (parsed == null) {
                    failedChats += 1
                    if (errors.size < 8) errors += entry.name
                    onProgress(ChatArchiveProgress(context.getString(R.string.ds_archive_importing), current, totalChats))
                    zip.closeEntry()
                    continue
                }

                val key = importKey(parsed.title, parsed.createdAt)
                if (existing.containsKey(key)) {
                    skippedChats += 1
                    onProgress(ChatArchiveProgress(context.getString(R.string.ds_archive_importing), current, totalChats))
                    zip.closeEntry()
                    continue
                }

                runCatching {
                    val chatRoom = chatRepository.importChatSnapshot(
                        chatRoom = ChatRoomV2(
                            title = parsed.title,
                            enabledPlatform = listOf(platform.uid),
                            createdAt = parsed.createdAt,
                            updatedAt = parsed.updatedAt
                        ),
                        messages = parsed.messages
                    )
                    chatRepository.saveChatPlatformModels(
                        chatId = chatRoom.id,
                        models = mapOf(platform.uid to platform.model)
                    )
                    existing[key] = chatRoom
                    importedChats += 1
                    importedMessages += parsed.messages.size
                    importedAttachments += parsed.messages.sumOf { it.attachments.size }
                }.onFailure { throwable ->
                    failedChats += 1
                    if (errors.size < 8) errors += "${parsed.title}: ${throwable.message.orEmpty()}"
                }

                onProgress(ChatArchiveProgress(context.getString(R.string.ds_archive_importing), current, totalChats))
                zip.closeEntry()
            }
        }

        ChatArchiveResult(
            importedChats = importedChats,
            importedMessages = importedMessages,
            importedAttachments = importedAttachments,
            skippedChats = skippedChats,
            failedChats = failedChats,
            errors = errors
        )
    }

    suspend fun exportZip(
        uri: Uri,
        onProgress: (ChatArchiveProgress) -> Unit = {}
    ): ChatArchiveResult = withContext(Dispatchers.IO) {
        val chats = chatRepository.fetchChatListV2().sortedByDescending { it.updatedAt }
        if (chats.isEmpty()) {
            return@withContext ChatArchiveResult(errors = listOf(context.getString(R.string.ds_archive_nothing_export)))
        }

        var exportedChats = 0
        var exportedMessages = 0
        var exportedAttachments = 0
        var current = 0
        val output = context.contentResolver.openOutputStream(uri, "w")
            ?: return@withContext ChatArchiveResult(errors = listOf(context.getString(R.string.ds_archive_write_failed)))
        try {
            ZipOutputStream(BufferedOutputStream(output)).use { zip ->
                addTextEntry(zip, "manifest.json", """{"export_format_version":3}""")
                addTextEntry(
                    zip,
                    "user.json",
                    """{"user_id":"local","email":null,"mobile":null,"oauth_profiles":[]}"""
                )

                chats.forEach { chat ->
                    current += 1
                    onProgress(ChatArchiveProgress(context.getString(R.string.ds_archive_exporting), current, chats.size))
                    val messages = chatRepository.fetchMessagesV2(chat.id)
                        .filterNot { it.isEffectivelyBlank() }
                    if (messages.isEmpty()) return@forEach

                    val uuid = UUID.nameUUIDFromBytes(
                        "deepseek-chat:${chat.id}:${chat.createdAt}".toByteArray()
                    ).toString()
                    val prefix = "chat_sessions/$uuid"
                    val messageJson = mutableListOf<kotlinx.serialization.json.JsonObject>()
                    var previousId: Int? = null
                    var messageId = 0

                    messages.forEach { message ->
                        messageId += 1
                        val id = messageId
                        val effectiveRole = if (message.platformType == null) "USER" else "ASSISTANT"

                        val fragments = buildJsonArray {
                            if (effectiveRole == "USER") {
                                val files = mutableListOf<kotlinx.serialization.json.JsonObject>()
                                message.attachments.forEach { attachment ->
                                    val file = File(attachment.filePathForDisplay)
                                    if (!file.isFile) return@forEach
                                    val safeName = safeFileName(file.name)
                                    val entryName = "${prefix}_files/${UUID.randomUUID()}_$safeName"
                                    val path = addFileEntry(zip, entryName, file)
                                    if (path) {
                                        exportedAttachments += 1
                                        files += buildJsonObject {
                                            put("id", UUID.randomUUID().toString())
                                            put("file_name", attachment.resolvedDisplayName)
                                            put("file_size", file.length())
                                            put("token_usage", 0)
                                            put("inserted_at", DeepSeekArchiveCodec.exportTime(message.createdAt))
                                            put("export_path", entryName)
                                            put("is_image", attachment.mimeType.startsWith("image/"))
                                            attachment.width?.let { put("width", it) }
                                            attachment.height?.let { put("height", it) }
                                        }
                                    }
                                }
                                if (files.isNotEmpty()) {
                                    add(buildJsonObject {
                                        put("type", "FILE")
                                        put("files", buildJsonArray { files.forEach { add(it) } })
                                    })
                                }
                                add(buildJsonObject {
                                    put("type", "REQUEST")
                                    put("content", message.content)
                                })
                            } else {
                                if (message.thoughts.isNotBlank()) {
                                    add(buildJsonObject {
                                        put("type", "THINK")
                                        put("content", message.thoughts)
                                        put("elapsed_secs", 0)
                                    })
                                }
                                val sources = DeepSeekArchiveCodec.exportSources(message.timeline)
                                if (sources.isNotEmpty()) {
                                    add(buildJsonObject {
                                        put("type", "TOOL_SEARCH")
                                        put("queries", buildJsonArray { })
                                        put("results", buildJsonArray { sources.forEach { add(it) } })
                                        put("status", "FINISHED")
                                        put("stage_id", 1)
                                    })
                                }
                                if (message.content.isNotBlank()) {
                                    add(buildJsonObject {
                                        put("type", "RESPONSE")
                                        put("content", message.content)
                                    })
                                }
                            }
                        }

                        messageJson += buildJsonObject {
                            put("id", id)
                            if (previousId == null) put("parent_id", JsonNull) else put("parent_id", previousId)
                            put("role", effectiveRole)
                            put("status", "FINISHED")
                            put("thinking_enabled", message.thoughts.isNotBlank())
                            put("search_enabled", DeepSeekArchiveCodec.exportSources(message.timeline).isNotEmpty())
                            put("accumulated_token_usage", 0)
                            put("inserted_at", DeepSeekArchiveCodec.exportTime(message.createdAt))
                            put("fragments", buildJsonArray { fragments.forEach { add(it) } })
                        }
                        previousId = id
                    }

                    val chatJson = buildJsonObject {
                        put("id", uuid)
                        put("title", chat.title)
                        put("pinned", false)
                        put("current_message_id", messageId)
                        put("inserted_at", DeepSeekArchiveCodec.exportTime(chat.createdAt))
                        put("updated_at", DeepSeekArchiveCodec.exportTime(chat.updatedAt))
                        put("messages", buildJsonArray { messageJson.forEach { add(it) } })
                    }
                    addTextEntry(zip, "$prefix.json", chatJson.toString())
                    exportedChats += 1
                    exportedMessages += messages.size
                }
            }
        } finally {
            runCatching { output.close() }
        }

        ChatArchiveResult(
            importedChats = exportedChats,
            importedMessages = exportedMessages,
            importedAttachments = exportedAttachments
        )
    }

    private suspend fun resolvePlatform(): dev.chungjungsoo.gptmobile.data.database.entity.PlatformV2 {
        val platforms = runCatching { settingRepository.fetchPlatformV2s() }.getOrDefault(emptyList())
        return platforms.firstOrNull { it.enabled && it.apiUrl.contains("deepseek.com", ignoreCase = true) }
            ?: platforms.firstOrNull { it.enabled }
            ?: throw IllegalStateException(context.getString(R.string.ds_archive_need_key))
    }

    private fun openZip(uri: Uri): ZipInputStream {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException(context.getString(R.string.ds_archive_read_failed))
        return ZipInputStream(BufferedInputStream(input))
    }

    private fun extractAttachment(zip: ZipInputStream, entry: ZipEntry): ChatAttachment? {
        val bytes = readEntry(zip, maxBytes = 80L * 1024 * 1024) ?: return null
        val originalName = File(entry.name).name.substringAfter('_', File(entry.name).name)
        val safeName = safeFileName(originalName)
        val target = File(attachmentsDir, "${UUID.randomUUID()}_$safeName")
        target.outputStream().use { it.write(bytes) }
        val mime = mimeOf(safeName)
        val bounds = if (mime.startsWith("image/")) {
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(target.absolutePath, this)
            }
        } else {
            null
        }
        return ChatAttachment(
            localFilePath = target.absolutePath,
            preparedFilePath = target.absolutePath,
            displayName = originalName,
            mimeType = mime,
            sizeBytes = bytes.size.toLong(),
            width = bounds?.outWidth?.takeIf { it > 0 },
            height = bounds?.outHeight?.takeIf { it > 0 }
        )
    }

    private fun readEntry(zip: ZipInputStream, maxBytes: Long): ByteArray? {
        val buffer = ByteArrayOutputStream()
        val chunk = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = zip.read(chunk)
            if (read <= 0) break
            total += read
            if (total > maxBytes) return null
            buffer.write(chunk, 0, read)
        }
        return buffer.toByteArray()
    }

    private fun addTextEntry(zip: ZipOutputStream, name: String, text: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(text.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun addFileEntry(zip: ZipOutputStream, name: String, file: File): Boolean = runCatching {
        zip.putNextEntry(ZipEntry(name))
        FileInputStream(file).use { input -> input.copyTo(zip) }
        zip.closeEntry()
        true
    }.getOrDefault(false)

    private fun safeFileName(name: String): String {
        val cleaned = name.replace(Regex("[\\\\/:*?\"<>|\\u0000-\\u001F]"), "_").trim()
        return cleaned.ifBlank { "file" }.take(120)
    }

    private fun mimeOf(name: String): String {
        val extension = name.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }

    private fun importKey(title: String, createdAt: Long): String =
        "${title.trim().lowercase()}|$createdAt"
}

private fun MessageV2.isEffectivelyBlank(): Boolean =
    content.isBlank() && thoughts.isBlank() && attachments.isEmpty() && timeline.isEmpty()
