package com.example.soundcloudaddetector

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class SoundCloudAdDetectorService : NotificationListenerService() {

    private var originalVolume: Int? = null // Store original volume
    private lateinit var audioManager: AudioManager

    override fun onCreate() {
        super.onCreate()
        // Initialize AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        // Check if notification is from SoundCloud
        if (sbn?.packageName != "com.soundcloud.android") return

        val notification = sbn.notification
        val extras = notification.extras

        // Extract title and text from notification
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getString(Notification.EXTRA_TEXT) ?: ""

        Log.d(TAG, "Title: $title, Text: $text")

        // Determine if it's an ad or a song
        val status = classifyNotification(title, text)

        // Adjust volume based on detection
        adjustVolume(status.contains("Ad"))

        // Broadcast the result to MainActivity
        val intent = Intent("com.example.soundcloudaddetector.NOTIFICATION_UPDATE")
        intent.putExtra("status", status)
        sendBroadcast(intent)
    }

    private fun classifyNotification(title: String, text: String): String {
        return when {
            // Ad detection: Check for common ad indicators
            title.isEmpty() || title.contains("Advertisement", ignoreCase = true) ||
                    text.contains("Sponsored", ignoreCase = true) ||
                    text.contains("Ad", ignoreCase = true) -> {
                "Playing: Ad"
            }
            // Song detection: Assume presence of title and artist-like text indicates a song
            title.isNotEmpty() && text.isNotEmpty() -> {
                "Playing: Song\nTitle: $title\nArtist: $text"
            }
            // Fallback for ambiguous cases
            else -> {
                "Playing: Unknown"
            }
        }
    }

    private fun adjustVolume(isAd: Boolean) {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        if (isAd) {
            // Store original volume if not already stored
            if (originalVolume == null) {
                originalVolume = currentVolume
                Log.d(TAG, "Stored original volume: $originalVolume")
            }

            // Lower volume to 0%
            val adVolume = 0
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, adVolume, 0)
            Log.d(TAG, "Lowered volume to $adVolume for ad")
        } else {
            // Restore original volume if available
            originalVolume?.let { volume ->
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
                Log.d(TAG, "Restored volume to $volume for song")
                originalVolume = null // Reset after restoring
            }
        }
    }

    companion object {
        private const val TAG = "SoundCloudAdDetector"
    }
}