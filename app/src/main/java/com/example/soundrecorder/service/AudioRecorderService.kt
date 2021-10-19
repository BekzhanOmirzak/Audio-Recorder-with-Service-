package com.example.soundrecorder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.media.MediaRecorder.OutputFormat
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.soundrecorder.R
import java.io.File

class AudioRecorderService : Service() {

    private val TAG = "AudioRecorderService"
    private val CHANNEL_ID = "First Channel";
    private val NOTIFICATION_ID = 101;

    private var mediaRecorder: MediaRecorder? = null;

    companion object {
        private const val TAG = "AudioRecorderService"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(
            TAG,
            "onStartCommand: Incoming message :  ${intent!!.extras?.getString("message")}"
        )

        val message = intent.extras?.getString("message");
        if (message == "start") {
            createNotification();
            configureMediateRecorder();
            prepareRecording();
            mediaRecorder?.start();
        } else if (message == "stop") {
            stopRecording();
        }
        return START_STICKY;
    }

    private fun createNotification() {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;

        if (SDK_INT > Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Аудио запись channel",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Запись в процессе")
            .setContentText("Будьте осторожны, ваш голос записывается")
            .setSmallIcon(R.drawable.ic_service)
            .setDefaults(Notification.DEFAULT_SOUND)
            .build();

        if (SDK_INT >= Build.VERSION_CODES.R)
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            );
        else
            startForeground(
                NOTIFICATION_ID,
                notification
            );
    }


    private fun configureMediateRecorder() {
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder().apply {
                reset();
                setAudioSource(MediaRecorder.AudioSource.MIC);
                setOutputFormat(OutputFormat.THREE_GPP);
                setAudioEncoder(OutputFormat.AMR_NB);
                setOutputFile(getFilePath());
                setMaxDuration(60 * 1000 * 5);
                setOnInfoListener { mr, what, extra ->
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        Log.e(TAG, "onInfo: Max duration time has been reached...");
                    }
                };
                setOnErrorListener { mr, what, extra ->
                    Log.e(TAG, "onError: Error  mediaRecorder: ${mr}")
                    Log.e(TAG, "onError: Error  what   : ${what}")
                }
            }
        }

    }

    private fun prepareRecording() {
        try {
            mediaRecorder?.prepare();
        } catch (ex: Exception) {
            Log.e(TAG, "startRecording: Exception has been thrown", ex);
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release();
            }
            Log.e(TAG, "stopRecording: Audio recording stopped : ")
        } catch (ex: Exception) {
            Log.e(TAG, "stopRecording: Exception has been thrown ", ex);
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null;

    private fun getFilePath(): String {
        var fileName =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath;
        fileName += File.separator + "AUD${System.currentTimeMillis()}.amr"
        return fileName;
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording();
    }


}