package com.example.lifelog

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lifelog.ui.theme.LifeLogTheme
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : ComponentActivity() {
    private var hasAudioPerms by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check permission
        hasAudioPerms = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        // Request if not granted
        if (!hasAudioPerms) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                100
            )
        }

        setContent {
            LifeLogTheme {
                if (hasAudioPerms) {
                    StopwatchScreen(applicationContext)
                } else {
                    PermissionPrompt { requestAudioPermission() }
                }
            }
        }
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            100
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            hasAudioPerms = true
            recreate()
        }
    }
}

@Composable
fun PermissionPrompt(onRequest: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onRequest) {
            Text("Grant Microphone Permission")
        }
    }
}

@Composable
fun StopwatchScreen(context: Context) {
    val transcriber = remember { Transcriber(context) }
    val transcription = remember { mutableStateOf("Starting...") }
    val state = remember { mutableStateOf(transcriber.state) }
    val timeout = remember { mutableStateOf(30_000) }
    val TIME_UNIT = 1000
    val timePassed = remember { mutableStateOf(0) }
    val needUpdate = remember { mutableStateOf(true) }

    fun extractTextFromJson(jsonString: String): String {
        val jsonElement = Json.parseToJsonElement(jsonString)
        return jsonElement.jsonObject["text"]?.jsonPrimitive?.content ?: ""
    }

    fun saveTranscriptionToFile(transcription: String, forceCleanup: Boolean = false) {
        Log.i("Transcription", transcription)
        timePassed.value = 0
    }

    LaunchedEffect(Unit) {
        transcriber.addOnResultCallback("result") { results ->
            transcription.value = extractTextFromJson(results)
            if (!transcription.value.isEmpty()) {
                saveTranscriptionToFile(transcription.value)
                timePassed.value = 0
                needUpdate.value = true
                Log.i("Transcription", "Transcription: $transcription")
            } else {
                Log.i("Transcription", "Transcription is empty")
            }
        }
        transcriber.addOnFinalResultCallback("final") { results ->
            transcription.value = extractTextFromJson(results)
            saveTranscriptionToFile(transcription.value, true)
        }
    }

    suspend fun makeGeminiApiCall(): String? {
        val text = "This is a test."
        val client = OkHttpClient()
        val apiKey = "AIzaSyDV1h6_D0Oka42djjNnIgqNpGkYB12WffE"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

        val json = """
            {
                "contents": [
                    {
                        "parts": [
                            {
                                "text": "$text"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-goog-api-key", apiKey)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        response.body?.string()
                    } else {
                        Log.e("GeminiAPI", "API call failed: ${response.code}")
                        null
                    }
                }
            } catch (e: IOException) {
                Log.e("GeminiAPI", "Network error: ${e.message}")
                null
            }
        }
    }

    LaunchedEffect(state) {
        while (true) {
            if (timePassed.value >= timeout.value && needUpdate.value) {
                Log.i("GeminiAPI", "Timeout reached")
//                val apiResponse = makeGeminiApiCall("Explain how AI works in a few words")
//                apiResponse?.let {
//                    Log.i("GeminiAPI", "Response: $it")
//                }
                timePassed.value = 0
            }
            delay(TIME_UNIT.toLong())
            timePassed.value += TIME_UNIT
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            transcriber.destroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                if (state.value == State.Playing) {
                    transcriber.pause(true)
                } else {
                    transcriber.pause(false)
                }
                state.value = transcriber.state
            }) {
                Text(if (state.value == State.Playing) "Start" else "Stop")
            }
            Text(text = transcription.value)
        }
    }
}
