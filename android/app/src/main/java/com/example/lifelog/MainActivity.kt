package com.example.lifelog

import android.content.pm.PackageManager
import kotlinx.serialization.json.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lifelog.ui.theme.LifeLogTheme
import android.content.Context
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private var hasAudioPerms by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hasAudioPerms = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPerms) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                100
            )
            // TODO Throw if permission is not granted
        }

        setContent {
            LifeLogTheme {
                StopwatchScreen(applicationContext)
            }
        }
    }
}

@Composable
fun StopwatchScreen(context: Context) {
    val transcriber = Transcriber(context)
    val transcription = remember { mutableStateOf("Starting...") }
    val state = remember { mutableStateOf(transcriber.state) };
    val timeout = remember { mutableStateOf(1000*30) }
    val TIME_UNIT = 1000
    val timePassed = remember { mutableStateOf(0) }

    fun extractTextFromJson(jsonString: String): String {
        val jsonElement = Json.parseToJsonElement(jsonString)
        return jsonElement.jsonObject["text"]?.jsonPrimitive?.content ?: ""
    }

    fun saveTranscriptionToFile(transcription: String, forceCleanup: Boolean = false) {
        // TODO save timestamp
        // TODO write to file
        Log.i("Transcription", transcription)
        timePassed.value = 0
    }

    // LLM Trigger
    LaunchedEffect(state) {
        while (true) {
            if (timePassed.value >= timeout.value) {
                //TODO Trigger LLM
                //MIGHT NEED TO ADD A START/STOP STRING HERE IN THE FILE SO WE CAN KEEP ADDING WHILE THE LLM IS DOING ITS THING.
                Log.i("TIMEOUT", "TIMEOUT")
                timePassed.value = 0
            }

            Log.i("TIME PASSED", timePassed.value.toString())
            delay(TIME_UNIT.toLong())
            timePassed.value += TIME_UNIT
        }
    }

    LaunchedEffect(Unit) {
        transcriber.addOnResultCallback("result") { results ->
            transcription.value = extractTextFromJson(results)
            saveTranscriptionToFile(transcription.value)
        }

//        transcriber.addOnPartialResultCallback("partial") { results ->
//            if (extractTextFromJson(results).length > 0) {
//                timePassed.value = 0
//            }
//        }

        transcriber.addOnFinalResultCallback("final") { results ->
            transcription.value = extractTextFromJson(results)
            saveTranscriptionToFile(transcription.value, true)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            transcriber.destroy()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            Text(
                text = transcription.value
            )
        }
    }
}