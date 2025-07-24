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
import android.app.AlertDialog;
import android.content.DialogInterface;

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
                        System.out.println(e.getMessage());
                    }
                },
                (exception) -> {
                    System.out.println(exception.getMessage());
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

    private void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
        }
    }

}