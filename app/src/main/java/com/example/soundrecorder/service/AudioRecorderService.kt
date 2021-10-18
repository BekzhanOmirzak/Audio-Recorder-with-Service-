package com.example.soundrecorder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.soundrecorder.R
import com.example.soundrecorder.ui.MainActivity
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

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
            startRecording();
        } else if (message == "stop") {
            stopRecording();
        }
        return START_STICKY;
    }

    private fun createNotification() {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
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

        startForeground(NOTIFICATION_ID, notification);

    }


    private fun configureMediateRecorder() {
        mediaRecorder = MediaRecorder().apply {
            reset();
            setAudioSource(MediaRecorder.AudioSource.MIC);
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            setOutputFile(getOutPutMediaFile());
            setMaxDuration(60*1000*5)
            setOnInfoListener(object : MediaRecorder.OnInfoListener {
                override fun onInfo(mr: MediaRecorder?, what: Int, extra: Int) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        Log.e(TAG, "onInfo: Max duration time has been reached...");
                    }
                }
            })
        }

    }

    private fun startRecording() {
        try {
            mediaRecorder?.prepare();
            mediaRecorder?.start();
            Log.e(TAG, "startRecording: Audio recording started...")
        } catch (ex: Exception) {
            Log.e(TAG, "startRecording: Exception has been thrown", ex);
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release();
            }
            Log.e(TAG, "stopRecording: Audio recording stopped : ")
        } catch (ex: Exception) {
            Log.e(TAG, "stopRecording: Exception has been thrown ", ex);
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null;


    private fun getOutPutMediaFile(): File? {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Audio Recorder"
        )
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "Failed to create directory")
                return null
            }
        }
        val formatter_date = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        return File(file.absoluteFile.toString() + File.separator + "AUD_" + formatter_date + ".amr")
    }


    override fun onDestroy() {
        super.onDestroy()
        stopRecording();
    }


}