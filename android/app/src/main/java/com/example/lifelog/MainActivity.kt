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
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 100)
            //TODO if they say no throw an error?
        } else {
            val transcriber = Transcriber(applicationContext)
            transcriber.destroy()
        }

        setContent {
            LifeLogTheme {
                StopwatchScreen(10000, applicationContext)
            }
        }
    }
}

@Composable
fun StopwatchScreen(time: Long, context: Context) {
    var isRunning by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }
    val transcription by remember { mutableStateOf("this is a test") }

    fun playAudio() {
        AudioPlayer(context = context).playFile(File(context.filesDir, "audio.mp3"))
    }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            val record = AudioRecorder(context = context)
            record.start(File(context.filesDir, "audio.mp3"))
            startTime = System.currentTimeMillis() - elapsedTime
            while (elapsedTime < time) {
                elapsedTime = System.currentTimeMillis() - startTime
                delay(10) // Update more frequently for smoother display
            }
            record.stop()
            elapsedTime = 0
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = elapsedTime.toString(),
            )
            Button(onClick = { isRunning = !isRunning }) {
                Text(if (isRunning) "Stop" else "Start")
            }
            Button(onClick = { playAudio() }) {
                Text("Play Audio")
            }
            Text(
                text = transcription
            )
        }
    }
}

class AudioRecorder(
    private val context: Context
) {
    private var recorder: MediaRecorder? = null

    fun start(outputFile: File) {
        recorder = MediaRecorder(context)
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder?.setOutputFile(outputFile)
        recorder?.prepare()
        recorder?.start()
    }

    fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}

class AudioPlayer(
    private val context: Context
) {

    private var player: MediaPlayer? = null

    fun playFile(file: File) {
        player = MediaPlayer.create(context, file.toUri())
        player?.start()
    }

//    fun stop() {
//        player?.stop()
//        player?.release()
//        player = null
//    }
}