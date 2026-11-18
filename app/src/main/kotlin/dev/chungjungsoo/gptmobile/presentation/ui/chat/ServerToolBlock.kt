package dev.chungjungsoo.gptmobile.presentation.ui.chat

import dev.chungjungsoo.gptmobile.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Surface
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.offset
import dev.chungjungsoo.gptmobile.data.database.entity.AssistantTimelineItem
import dev.chungjungsoo.gptmobile.data.database.entity.AssistantTimelineItemType
import dev.chungjungsoo.gptmobile.presentation.theme.defaultSpatialSpec
import dev.chungjungsoo.gptmobile.presentation.theme.fastEffectsSpec
import java.net.URI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val serverToolJson = Json { ignoreUnknownKeys = true }

private data class ServerSource(
    val url: String,
    val title: String?,
    val siteName: String?
)

private data class ServerToolSummary(
    val queries: List<String> = emptyList(),
    val pages: List<ServerSource> = emptyList(),
    val sources: List<ServerSource> = emptyList(),
    val searchCount: Int = 0,
    val fetchCount: Int = 0,
    val activeCalls: Int = 0
) {
    val hasActivity: Boolean = queries.isNotEmpty() || pages.isNotEmpty() || sources.isNotEmpty() ||
        searchCount > 0 || fetchCount > 0

}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ServerToolBlock(
    timeline: List<AssistantTimelineItem>,
    isLoading: Boolean,
    contentIdentity: Any,
    modifier: Modifier = Modifier
) {
    val summary = remember(timeline, isLoading) { summarizeServerTools(timeline, isLoading) }
    if (!summary.hasActivity) return

    var sheetOpen by rememberSaveable(contentIdentity) { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val allSources = (summary.sources + summary.pages).distinctBy { it.url }
    val hasSources = allSources.isNotEmpty()
    val active = isLoading && summary.activeCalls > 0
    val label = when {
        active && !hasSources -> stringResource(R.string.ds_web_searching)
        active && summary.fetchCount > 0 -> stringResource(R.string.ds_web_reading)
        hasSources -> stringResource(R.string.ds_web_pages_count, allSources.size)
        summary.searchCount > 0 -> stringResource(R.string.ds_web_summary_search, summary.searchCount)
        summary.fetchCount > 0 -> stringResource(R.string.ds_web_summary_pages, summary.fetchCount)
        else -> stringResource(R.string.ds_web_done)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        onClick = { if (hasSources || summary.queries.isNotEmpty()) sheetOpen = true }
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 40.dp)
                .padding(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasSources) {
                SourceFaviconStack(allSources.take(4))
                Spacer(Modifier.width(8.dp))
            } else if (active) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(15.dp),
                    strokeWidth = 1.8.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(7.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }

    if (sheetOpen) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { sheetOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            dragHandle = null
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ds_web_results),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    androidx.compose.material3.IconButton(
                        onClick = { sheetOpen = false },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.ds_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (summary.queries.isNotEmpty()) {
                    Text(
                        text = summary.queries.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 560.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 24.dp
                    )
                ) {
                    itemsIndexed(allSources) { index, source ->
                        SearchResultRow(
                            index = index + 1,
                            source = source,
                            onClick = { uriHandler.openUriSafely(source.url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceFaviconStack(sources: List<ServerSource>) {
    val palette = listOf(
        Color(0xFFF2C94C),
        Color(0xFF56CCF2),
        Color(0xFF6FCF97),
        Color(0xFFBB6BD9)
    )
    Box(modifier = Modifier.height(24.dp).width((22 + (sources.size - 1).coerceAtLeast(0) * 14).dp)) {
        sources.forEachIndexed { index, source ->
            val initial = hostOf(source.url).firstOrNull()?.uppercaseChar()?.toString() ?: "·"
            Box(
                modifier = Modifier
                    .offset(x = (index * 14).dp)
                    .size(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(palette[index % palette.size]),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    index: Int,
    source: ServerSource,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.siteName?.takeIf { it.isNotBlank() } ?: hostOf(source.url),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = source.title?.takeIf { it.isNotBlank() } ?: source.url,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp)
            )
            Text(
                text = source.url,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun SourceRow(source: ServerSource, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.title?.takeIf { it.isNotBlank() } ?: source.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = source.siteName?.takeIf { it.isNotBlank() } ?: hostOf(source.url),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun androidx.compose.ui.platform.UriHandler.openUriSafely(url: String) {
    runCatching { openUri(url) }
}

private fun hostOf(url: String): String = runCatching { URI(url).host.orEmpty() }.getOrDefault("")

private fun summarizeServerTools(
    timeline: List<AssistantTimelineItem>,
    isLoading: Boolean
): ServerToolSummary {
    val started = linkedMapOf<String, String>()
    val completed = linkedSetOf<String>()
    val queries = linkedSetOf<String>()
    val sources = linkedMapOf<String, ServerSource>()
    val pages = linkedMapOf<String, ServerSource>()
    var searchCount = 0
    var fetchCount = 0
    val countedSearches = linkedSetOf<String>()
    val countedFetches = linkedSetOf<String>()

    fun addSource(element: JsonElement?) {
        val obj = element.asObjectOrNull() ?: return
        val url = obj.string("url") ?: return
        if (url.isBlank()) return
        sources.putIfAbsent(
            url,
            ServerSource(
                url = url,
                title = obj.string("title"),
                siteName = obj.string("site_name") ?: obj.string("siteName")
            )
        )
    }

    fun addPage(element: JsonElement?) {
        val obj = element.asObjectOrNull() ?: return
        val url = obj.string("url") ?: return
        if (url.isBlank()) return
        pages.putIfAbsent(
            url,
            ServerSource(
                url = url,
                title = obj.string("title"),
                siteName = hostOf(url)
            )
        )
    }

    fun readCall(call: JsonObject, isComplete: Boolean) {
        val id = call.string("id").orEmpty()
        val action = call["action"].asObjectOrNull()
        val actionType = action?.string("type").orEmpty()
        when (actionType) {
            "search" -> {
                action?.get("queries").asArrayOrNull()?.forEach { query ->
                    query.asStringOrNull()?.takeIf { it.isNotBlank() }?.let(queries::add)
                }
                if (isComplete && id.isNotBlank() && countedSearches.add(id)) searchCount += 1
            }

            "open_page", "read_link" -> {
                addPage(call["page"])
                action?.string("url")?.let { url ->
                    if (url.isNotBlank()) {
                        pages.putIfAbsent(
                            url,
                            ServerSource(url = url, title = call.string("title"), siteName = hostOf(url))
                        )
                    }
                }
                if (isComplete && id.isNotBlank() && countedFetches.add(id)) fetchCount += 1
            }

            "find_in_page" -> Unit
        }
        call["sources"].asArrayOrNull()?.forEach(::addSource)
        if (isComplete) completed += id
    }

    timeline.forEach { item ->
        if (item.type != AssistantTimelineItemType.SERVER_TOOL) return@forEach
        val payload = runCatching { serverToolJson.parseToJsonElement(item.content).jsonObject }.getOrNull()
            ?: return@forEach
        when (payload.string("kind")) {
            "event" -> {
                val event = payload["value"].asObjectOrNull() ?: return@forEach
                val call = event["call"].asObjectOrNull() ?: return@forEach
                val id = call.string("id").orEmpty()
                when (event.string("type")) {
                    "call_started" -> {
                        if (id.isNotBlank()) started[id] = call.string("type").orEmpty()
                        readCall(call, isComplete = false)
                    }

                    "call_completed" -> readCall(call, isComplete = true)
                }
            }

            "final_calls" -> {
                payload["value"].asArrayOrNull()?.forEach { step ->
                    step.asObjectOrNull()?.get("calls").asArrayOrNull()?.forEach { call ->
                        call.asObjectOrNull()?.let { readCall(it, isComplete = true) }
                    }
                }
            }

            "annotations" -> {
                payload["value"].asArrayOrNull()?.forEach { annotation ->
                    val obj = annotation.asObjectOrNull() ?: return@forEach
                    val citation = obj["url_citation"].asObjectOrNull() ?: obj
                    addSource(citation)
                }
            }
        }
    }

    val active = if (isLoading) (started.keys - completed).size else 0
    return ServerToolSummary(
        queries = queries.toList(),
        pages = pages.values.toList(),
        sources = sources.values.toList(),
        searchCount = searchCount,
        fetchCount = fetchCount,
        activeCalls = active
    )
}

private fun JsonElement?.asObjectOrNull(): JsonObject? = runCatching {
    this?.jsonObject
}.getOrNull()

private fun JsonElement?.asArrayOrNull(): JsonArray? = runCatching {
    this?.jsonArray
}.getOrNull()

private fun JsonElement?.asStringOrNull(): String? = runCatching {
    this?.jsonPrimitive?.contentOrNull
}.getOrNull()

private fun JsonObject.string(key: String): String? = this[key].asStringOrNull()
