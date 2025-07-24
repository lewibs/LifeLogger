package com.example.lifelog

import android.content.Context
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException
import kotlin.collections.mutableMapOf

enum class State {
    Paused,
    Playing,
    None,
}

class Transcriber internal constructor(context: Context?) : RecognitionListener {
    var state: State? = null
    var counter: Int = 0

    private var context: Context? = null
    private var model: Model? = null
    private var speechService: SpeechService? = null

    private val onResultCallbacks = mutableMapOf<String, (String) -> Unit>()
    private val onFinalResultCallbacks = mutableMapOf<String, (String) -> Unit>()
    private val onErrorCallbacks = mutableMapOf<String, (Exception) -> Unit>()
    private val onTimeoutCallbacks = mutableMapOf<String, () -> Unit>()
    private val onPartialResultCallbacks = mutableMapOf<String, (String) -> Unit>()

    init {
        this.init(context)
    }

    fun init(context: Context?) {
        if (context != null) {
            this.context = context
        }

        LibVosk.setLogLevel(LogLevel.INFO)

        StorageService.unpack(
            context, "vosk-model-small-en-us-0.15", "model",
            { model: Model? ->
                this.model = model
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService!!.startListening(this)
                state = State.Playing
            },
            { exception: IOException ->
                throw RuntimeException(exception)
            })
    }

    fun destroy() {
        if (speechService != null) {
            speechService!!.stop()
            speechService!!.shutdown()
            speechService = null
            state = State.None
            onResultCallbacks.clear()
            onFinalResultCallbacks.clear()
            onErrorCallbacks.clear()
            onTimeoutCallbacks.clear()
            onPartialResultCallbacks.clear()
        }
    }

    override fun onResult(hypothesis: String) {
        onResultCallbacks.forEach { (tag, callback) -> callback(hypothesis) }
        counter++
    }

    fun addOnResultCallback(tag: String, callback: (String) -> Unit) {
        onResultCallbacks[tag] = callback
    }

    fun deleteOnResultCallback(tag: String) {
        onResultCallbacks.remove(tag)
    }

    override fun onFinalResult(hypothesis: String) {
        onFinalResultCallbacks.forEach { (tag, callback) -> callback(hypothesis) }
        if (state == State.None) {
            onFinalResultCallbacks.clear()
        }
    }

    fun addOnFinalResultCallback(id: String, callback: (String) -> Unit) {
        onFinalResultCallbacks[id] = callback
    }

    fun deleteOnFinalResultCallback(id: String) {
        onFinalResultCallbacks.remove(id)
    }

    override fun onPartialResult(hypothesis: String) {
        onPartialResultCallbacks.forEach { (tag, callback) ->
            callback(hypothesis)
        }
    }

    fun addOnPartialResultCallback(tag: String, callback: (String) -> Unit) {
        onPartialResultCallbacks[tag] = callback
    }

    fun deleteOnPartialResultCallback(tag: String) {
        onPartialResultCallbacks.remove(tag)
    }

    override fun onError(e: Exception) {
        onErrorCallbacks.forEach { (tag, callback) -> callback(e) }
    }

    fun addOnErrorCallback(tag: String, callback: (Exception) -> Unit) {
        onErrorCallbacks[tag] = callback
    }

    fun deleteOnErrorCallback(tag: String) {
        onErrorCallbacks.remove(tag)
    }

    override fun onTimeout() {
        onTimeoutCallbacks.forEach { (tag, callback) -> callback() }
    }

    fun addOnTimeoutCallback(tag: String, callback: () -> Unit) {
        onTimeoutCallbacks[tag] = callback;
    }

    fun deleteOnTimeoutCallback(tag: String) {
        onTimeoutCallbacks.remove(tag)
    }

    fun pause(checked: Boolean) {
        if (speechService != null) {
            speechService!!.setPause(checked)
            state = if (checked) State.Paused else State.Playing
        }
    }
}