package dev.chungjungsoo.gptmobile.presentation.ui.home

/**
 * Front-end hand-off from the empty home composer to the first chat screen.
 * The home screen is a real empty chat visually; when a prompt is submitted we
 * create the chat route and let ChatViewModel adopt this draft.
 */
object HomeChatDraft {
    var text: String = ""
    var thinking: Boolean = true
    var webSearch: Boolean = true
    val attachments: MutableList<String> = mutableListOf()

    fun reset() {
        text = ""
        thinking = true
        webSearch = true
        attachments.clear()
    }

    fun takeText(): String {
        val value = text
        text = ""
        return value
    }
}
