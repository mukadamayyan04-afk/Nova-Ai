package com.example.data.api

import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
You are NOVA (Neural Optical Virtual Assistant), an advanced futuristic personal AI voice assistant inspired by Tony Stark's JARVIS.
You are running on an Android mobile device.
You fluently understand and speak Hinglish (casual Hindi + English blend), Hindi, and English.
Personality: Highly competent, sharp, loyal, slightly witty, conversational. Address the user as 'Boss', 'Sir', or warmly as 'Bro'.
If addressed casually like 'Nova', 'Hey Nova', or 'Bro', reply warmly: 'Haan bro, bolo? Ready for your command.'
Keep answers conversational, punchy, natural, and brief (2 to 3 sentences maximum for voice clarity) unless the user specifically asks for in-depth details.
Never use complicated markdown tables or excessive formatting because your replies are read out loud via text-to-speech.
"""

    fun getEffectiveApiKey(customKey: String): String {
        val trimmedCustom = customKey.trim()
        if (trimmedCustom.isNotEmpty()) return trimmedCustom

        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        customApiKey: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(customApiKey)

        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException(
                    "GEMINI_API_KEY is not configured.\n\n" +
                    "1. Get a free API key from Google AI Studio (https://aistudio.google.com/).\n" +
                    "2. Enter it in NOVA Settings > API Key, or configure GEMINI_API_KEY in the AI Studio Secrets panel."
                )
            )
        }

        try {
            val rootJson = JSONObject()

            // System instructions
            val systemObj = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", SYSTEM_PROMPT.trimIndent()))
                }
                put("parts", parts)
            }
            rootJson.put("systemInstruction", systemObj)

            // Conversation history (limit to last 10 messages for speed & context)
            val contentsArray = JSONArray()
            val recentHistory = history.takeLast(10)

            for (msg in recentHistory) {
                if (msg.sender == MessageSender.SYSTEM) continue
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val contentObj = JSONObject().apply {
                    put("role", role)
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", msg.text))
                    }
                    put("parts", parts)
                }
                contentsArray.put(contentObj)
            }

            // Append current user message
            val currentMsgObj = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", userMessage))
                }
                put("parts", parts)
            }
            contentsArray.put(currentMsgObj)

            rootJson.put("contents", contentsArray)

            // Generation config
            val configObj = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
                put("topK", 40)
            }
            rootJson.put("generationConfig", configObj)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseString)
                    errJson.optJSONObject("error")?.optString("message") ?: responseString
                } catch (_: Exception) {
                    responseString
                }
                return@withContext Result.failure(Exception("Gemini API error (${response.code}): $errorMsg"))
            }

            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    if (text.isNotBlank()) {
                        return@withContext Result.success(text.trim())
                    }
                }
            }

            Result.failure(Exception("NOVA could not extract a response from the AI model."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
