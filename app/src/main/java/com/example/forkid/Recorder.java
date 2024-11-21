package com.example.forkid;

import android.media.MediaRecorder;
import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Recorder {
    private MediaRecorder mediaRecorder;
    private String outputFilePath;
    private String Tag = "Recorder";

    public String startRecording() {
        String uniqueID = UUID.randomUUID().toString();
        String fileName = "audio_record_" + uniqueID + ".3gp";
        outputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + fileName;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outputFilePath);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.d(Tag, "Recording started");
        } catch (IOException e) {

            Log.e(Tag, "Recording failed: " + e.getMessage());
            return "error";
        }

        // Останавливаем запись через 30 секунд
        new android.os.Handler().postDelayed(() -> stopRecording(), 30000);
        return outputFilePath;
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Log.d(Tag, "Recording stopped. File saved at: " + outputFilePath);
        }
    }


}
