package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AssistantLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.util.NovaAppLauncher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NOVA AI", appName)
  }

  @Test
  fun `test command parsing for youtube`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = NovaAppLauncher.executeIfCommand(context, "Nova, open YouTube")
    assertNotNull(result)
    assertTrue(result!!.handled)
  }

  @Test
  fun `test command parsing for hinglish youtube`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = NovaAppLauncher.executeIfCommand(context, "YouTube kholo")
    assertNotNull(result)
    assertTrue(result!!.handled)
  }

  @Test
  fun `test chat message creation`() {
    val msg = ChatMessage(text = "Namaste NOVA", sender = MessageSender.USER)
    assertEquals("Namaste NOVA", msg.text)
    assertEquals(MessageSender.USER, msg.sender)
  }

  @Test
  fun `test package manager query for launcher intent`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
      addCategory(android.content.Intent.CATEGORY_LAUNCHER)
    }
    val activities = context.packageManager.queryIntentActivities(intent, 0)
    assertNotNull(activities)
  }

  @Test
  fun `test bilingual recognizer intent configuration for hinglish`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val recognizer = com.example.util.NovaSpeechRecognizer(
      context = context,
      onSpeechResult = {},
      onPartialResult = {},
      onAmplitudeChanged = {},
      onErrorOccurred = {},
      onListeningStateChanged = {}
    )
    val intent = recognizer.createRecognizerIntent(AssistantLanguage.HINGLISH)
    assertEquals("hi-IN", intent.getStringExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE))
    val additional = intent.getStringArrayExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES")
    assertNotNull(additional)
    assertTrue(additional!!.contains("en-IN"))
  }
}
