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
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

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
        }

        setContent {
            LifeLogTheme {
                StopwatchScreen(10000, hasAudioPerms, applicationContext)
            }
        }
    }
}

@Composable
fun StopwatchScreen(time: Long, hasAudioPerms: Boolean, context: Context) {
    var isRunning by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var transcription by remember { mutableStateOf("this is a test") }

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

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}

class Transcriber: RecognitionListener {
    private var speechService: SpeechService
    private var model: Model? = null

    constructor(context: Context) {
        StorageService.unpack(
            context,
            "model-en-us",
            "model",
            { model:Model ->
                this.model = model
            },
            { exception:Exception ->
                throw RuntimeException("Failed to unpack the model.")
            }
        )
        val rec = Recognizer(model, 16000.0f)
        speechService = SpeechService(rec, 16000.0f)
    }

    fun start() {
        speechService.startListening(this)
    }

    fun stop() {
        speachService.stop()
        // DO WE NEED TO SHUTDOWN THE SPEECH SERVICE?
        // speachService.shutdown()
    }

//    fun pause() {
//        speechService?.setPause(checked)
//    }
}

// Copyright 2019 Alpha Cephei Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//package org.vosk.demo
//
//import android.Manifest
//import android.app.Activity
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.text.method.ScrollingMovementMethod
//import android.widget.Button
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.ToggleButton
//import androidx.annotation.NonNull
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import org.vosk.LibVosk
//import org.vosk.LogLevel
//import org.vosk.Model
//import org.vosk.Recognizer
//import org.vosk.android.RecognitionListener
//import org.vosk.android.SpeechService
//import org.vosk.android.StorageService
//import java.io.IOException
//
//class MainActivity : Activity(), RecognitionListener {
//
//    companion object {
//        private const val STATE_START = 0
//        private const val STATE_READY = 1
//        private const val STATE_DONE = 2
//        private const val STATE_MIC = 3
//
//        /* Used to handle permission request */
//        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
//    }
//
//    private var model: Model? = null
//    private var speechService: SpeechService? = null
//    private lateinit var resultView: TextView
//    private lateinit var micButton: Button
//    private lateinit var pauseButton: ToggleButton
//
//    override fun onCreate(state: Bundle?) {
//        super.onCreate(state)
//
//        // Create UI programmatically
//        val layout = LinearLayout(this).apply {
//            orientation = LinearLayout.VERTICAL
//            setPadding(32, 32, 32, 32)
//        }
//
//        resultView = TextView(this).apply {
//            text = "Preparing..."
//            movementMethod = ScrollingMovementMethod()
//            minLines = 10
//        }
//
//        micButton = Button(this).apply {
//            text = "Start Microphone"
//            isEnabled = false
//            setOnClickListener { recognizeMicrophone() }
//        }
//
//        pauseButton = ToggleButton(this).apply {
//            textOn = "Resume"
//            textOff = "Pause"
//            isEnabled = false
//            setOnCheckedChangeListener { _, isChecked -> pause(isChecked) }
//        }
//
//        layout.addView(resultView)
//        layout.addView(micButton)
//        layout.addView(pauseButton)
//        setContentView(layout)
//
//        setUiState(STATE_START)
//
//        LibVosk.setLogLevel(LogLevel.INFO)
//
//        // Check if user has given permission to record audio, init the model after permission is granted
//        val permissionCheck = ContextCompat.checkSelfPermission(
//            applicationContext,
//            Manifest.permission.RECORD_AUDIO
//        )
//        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.RECORD_AUDIO),
//                PERMISSIONS_REQUEST_RECORD_AUDIO
//            )
//        } else {
//            initModel()
//        }
//    }
//
//    private fun initModel() {
//        StorageService.unpack(
//            this,
//            "model-en-us",
//            "model",
//            { model ->
//                this.model = model
//                setUiState(STATE_READY)
//            },
//            { exception ->
//                setErrorState("Failed to unpack the model: ${exception.message}")
//            }
//        )
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        @NonNull permissions: Array<String>,
//        @NonNull grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Recognizer initialization is a time-consuming and it involves IO,
//                // so we execute it in async task
//                initModel()
//            } else {
//                finish()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        speechService?.let {
//            it.stop()
//            it.shutdown()
//        }
//    }
//
//    override fun onResult(hypothesis: String) {
//        resultView.append("$hypothesis\n")
//    }
//
//    override fun onFinalResult(hypothesis: String) {
//        resultView.append("$hypothesis\n")
//        setUiState(STATE_DONE)
//    }
//
//    override fun onPartialResult(hypothesis: String) {
//        resultView.append("$hypothesis\n")
//    }
//
//    override fun onError(e: Exception) {
//        setErrorState(e.message ?: "Unknown error")
//    }
//
//    override fun onTimeout() {
//        setUiState(STATE_DONE)
//    }
//
//    private fun setUiState(state: Int) {
//        when (state) {
//            STATE_START -> {
//                resultView.text = "Preparing..."
//                micButton.isEnabled = false
//                pauseButton.isEnabled = false
//            }
//            STATE_READY -> {
//                resultView.text = "Ready"
//                micButton.text = "Start Microphone"
//                micButton.isEnabled = true
//                pauseButton.isEnabled = false
//            }
//            STATE_DONE -> {
//                micButton.text = "Start Microphone"
//                micButton.isEnabled = true
//                pauseButton.isEnabled = false
//                pauseButton.isChecked = false
//            }
//            STATE_MIC -> {
//                micButton.text = "Stop Microphone"
//                resultView.text = "Say something..."
//                micButton.isEnabled = true
//                pauseButton.isEnabled = true
//            }
//            else -> throw IllegalStateException("Unexpected value: $state")
//        }
//    }
//
//    private fun setErrorState(message: String) {
//        resultView.text = message
//        micButton.text = "Start Microphone"
//        micButton.isEnabled = false
//    }
//
//    private fun recognizeMicrophone() {
//        speechService?.let {
//            setUiState(STATE_DONE)
//            it.stop()
//            speechService = null
//        } ?: run {
//            setUiState(STATE_MIC)
//            try {
//                val rec = Recognizer(model, 16000.0f)
//                speechService = SpeechService(rec, 16000.0f)
//                speechService?.startListening(this)
//            } catch (e: IOException) {
//                setErrorState(e.message ?: "IO Error")
//            }
//        }
//    }
//
//    private fun pause(checked: Boolean) {
//        speechService?.setPause(checked)
//    }
//}