package com.example.lifelog

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Button
import kotlinx.coroutines.delay
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
import java.io.File
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri

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

    transcriber.addOnResultCallback("result") { results ->
        transcription.value = results
    }

    transcriber.addOnFinalResultCallback("final") { results ->
        transcription.value = results
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                if (transcriber.state == State.Playing) {
                    transcriber.destroy()
                } else {
                    transcriber.init(context)
                }
            }) {
                Text(if (transcriber.state == State.Playing) "Stop" else "Start")
            }
            Text(
                text = transcription.value
            )
        }
    }
}