package com.example.soundrecorder.ui

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.soundrecorder.BuildConfig
import com.example.soundrecorder.R
import com.example.soundrecorder.service.AudioRecorderService
import com.example.soundrecorder.util.TempStorage


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val PERMISSIONS = arrayOf(
        WRITE_EXTERNAL_STORAGE,
        READ_EXTERNAL_STORAGE,
        RECORD_AUDIO,
        MANAGE_EXTERNAL_STORAGE
    )

    private lateinit var btnStart: Button;
    private lateinit var btnStop: Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TempStorage.initSharedPreferences(this);
        initViews();
        if (TempStorage.getValue())
            audioRecordingRunning(true);
        else
            audioRecordingRunning(false)

        checkPermissions(); //Assuming that the  user will the give permissions

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

        checkStorageAccessPermissionForAndroidR();

    }

    private fun checkPermissions() {
        for (k in PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, k) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
            }
        }
    }

    private fun checkStorageAccessPermissionForAndroidR() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val uri: Uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
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