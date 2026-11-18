package dev.chungjungsoo.gptmobile.presentation.ui.home

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Small cross-screen hand-off: the chat top bar asks the home screen to open its
 * history drawer, then navigates back. Keeping it here avoids injecting a whole
 * second HomeViewModel into the chat route just to show the same drawer.
 */
object HomeDrawerRequest {
    val pending = MutableStateFlow(false)

    fun requestOpen() {
        pending.value = true
    }

    fun consume() {
        pending.value = false
    }
}
