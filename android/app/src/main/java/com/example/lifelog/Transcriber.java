package com.example.lifelog;

import android.content.Context;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;
import android.util.Log;

public class Transcriber implements RecognitionListener {

    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;

    Transcriber(Context context) {
        LibVosk.setLogLevel(LogLevel.INFO);

        // TOOD add the models to the source.
        StorageService.unpack(context, "model-en-us", "model",
                (model) -> {
                    this.model = model;
                    try {
                        Recognizer rec = new Recognizer(model, 16000.0f);
                        speechService = new SpeechService(rec, 16000.0f);
                        speechService.startListening(this);
                    } catch (Exception e) {
                        Log.i("Benjamin=>1",e.getMessage());
                    }
                },
                (exception) -> {
                    Log.i("Benjamin=>2",exception.getMessage());
                });
    }

    public void destroy() {
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }

        if (speechStreamService != null) {
            speechStreamService.stop();
        }
    }

    @Override
    public void onResult(String hypothesis) {
        Log.i("Benjamin=>3",hypothesis);
    }

    @Override
    public void onFinalResult(String hypothesis) {
        Log.i("Benjamin=>4",hypothesis);
        if (speechStreamService != null) {
            speechStreamService = null;
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        Log.i("Benjamin=>",hypothesis);
    }

    @Override
    public void onError(Exception e) {
        Log.i("Benjamin=>",e.getMessage());
    }

    @Override
    public void onTimeout() {
        Log.i("Benjamin=>","Timeout");
    }

    private void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
        }
    }

}