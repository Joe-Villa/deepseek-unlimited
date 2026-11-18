package dev.chungjungsoo.gptmobile.presentation.common

import android.app.Activity
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.chungjungsoo.gptmobile.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalizedContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun switchingLanguageKeepsActivityContextAndUpdatesResources() {
        var languageTag by mutableStateOf("en")

        composeRule.setContent {
            LocalizedContent(languageTag) {
                val context = LocalContext.current
                Text(if (context is Activity) "activity-context" else "other-context")
                Text(stringResource(R.string.ds_language_title))
            }
        }

        composeRule.onNodeWithText("activity-context").assertExists()
        composeRule.onNodeWithText("Language").assertExists()

        languageTag = "zh-CN"
        composeRule.waitForIdle()

        composeRule.onNodeWithText("activity-context").assertExists()
        composeRule.onNodeWithText("语言").assertExists()
    }
}
