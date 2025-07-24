package com.example.lifelog

import android.content.Context
import android.util.Log
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.io.IOException

class Transcriber internal constructor(context: Context?) : RecognitionListener {
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null

    init {
        LibVosk.setLogLevel(LogLevel.INFO)

        // TOOD add the models to the source.
        StorageService.unpack(
            context, "model-en-us", "model",
            { model: Model? ->
                this.model = model
                try {
                    val rec = Recognizer(model, 16000.0f)
                    speechService = SpeechService(rec, 16000.0f)
                    speechService!!.startListening(this)
                } catch (e: Exception) {
                    Log.i("Benjamin=>1", e.message!!)
                }
            },
            { exception: IOException ->
                Log.i(
                    "Benjamin=>2",
                    exception.message!!
                )
            })
    }

    fun destroy() {
        if (speechService != null) {
            speechService!!.stop()
            speechService!!.shutdown()
        }

        if (speechStreamService != null) {
            speechStreamService!!.stop()
        }
    }

    override fun onResult(hypothesis: String) {
        Log.i("Benjamin=>3", hypothesis)
    }

    override fun onFinalResult(hypothesis: String) {
        Log.i("Benjamin=>4", hypothesis)
        if (speechStreamService != null) {
            speechStreamService = null
        }
    }

    override fun onPartialResult(hypothesis: String) {
//        Log.i("Benjamin=>",hypothesis);
    }

    override fun onError(e: Exception) {
        Log.i("Benjamin=>", e.message!!)
    }

    override fun onTimeout() {
        Log.i("Benjamin=>", "Timeout")
    }

    private fun pause(checked: Boolean) {
        if (speechService != null) {
            speechService!!.setPause(checked)
        }
    }
}