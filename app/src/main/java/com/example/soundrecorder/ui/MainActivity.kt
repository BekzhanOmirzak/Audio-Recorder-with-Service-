package com.example.soundrecorder.ui

import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import com.example.soundrecorder.R
import com.example.soundrecorder.service.AudioRecorderService
import com.example.soundrecorder.util.TempStorage
import com.example.soundrecorder.util.Utils
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var btnStart: Button;
    private lateinit var btnStop: Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.permissionToReadAndCreateFile(this);
        TempStorage.initSharedPreferences(this);
        initViews();
        if (TempStorage.getValue())
            audioRecordingRunning(true);
        else
            audioRecordingRunning(false)

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            Intent(this, AudioRecorderService::class.java).also {
                it.putExtra("message", "start");
                startService(it);
                audioRecordingRunning(true);
            }
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {

            Intent(this, AudioRecorderService::class.java).also {
                it.putExtra("message", "stop")
                stopService(it);
                audioRecordingRunning(false);
            }
        }

    }

    private fun audioRecordingRunning(running: Boolean) {
        TempStorage.saveValue(running);
        if (running) {
            btnStart.isEnabled = false;
            btnStop.isEnabled = true;
        } else {
            btnStart.isEnabled = true;
            btnStop.isEnabled = false;
        }
    }


    private fun initViews() {
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
    }


}