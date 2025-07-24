package com.example.lifelog;

import android.content.Context;
import android.os.Bundle;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import java.io.IOException;

public class Transcriber implements RecognitionListener {

    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;

    Transcriber(Bundle state, Context context) {
        System.out.println("TEST");
        //recognizeMicrophone();
        LibVosk.setLogLevel(LogLevel.INFO);
        //initModel(context);
    }

    private void initModel(Context context) {
        //TODO add the model files into android.
        StorageService.unpack(context, "model-en-us", "model",
                (model) -> {
                    this.model = model;
                },
                (exception) -> System.out.println("Failed to unpack the model" + exception.getMessage()));
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
        System.out.println(hypothesis);
    }

    @Override
    public void onFinalResult(String hypothesis) {
        System.out.println(hypothesis);
        if (speechStreamService != null) {
            speechStreamService = null;
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        System.out.println(hypothesis);
    }

    @Override
    public void onError(Exception e) {
        System.out.println(e.getMessage());
    }

    @Override
    public void onTimeout() {
        System.out.println("Timeout");
    }

    private void recognizeMicrophone() {
        if (speechService != null) {
            speechService.stop();
            speechService = null;
        } else {
            try {
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
        }
    }

}