package com.example.soundcloudaddetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var permissionButton: Button

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra("status") ?: "No data"
            statusTextView.text = status
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)
        permissionButton = findViewById(R.id.permissionButton)

        // Register receiver for updates from service
        val filter = IntentFilter("com.example.soundcloudaddetector.NOTIFICATION_UPDATE")
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        // Check and request notification access
        permissionButton.setOnClickListener {
            if (!isNotificationServiceEnabled()) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } else {
                permissionButton.text = "Permission Granted"
                permissionButton.isEnabled = false
            }
        }

        // Update button state
        updatePermissionButton()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }

    private fun updatePermissionButton() {
        if (isNotificationServiceEnabled()) {
            permissionButton.text = "Permission Granted"
            permissionButton.isEnabled = false
        } else {
            permissionButton.text = "Grant Notification Access"
            permissionButton.isEnabled = true
        }
    }
}