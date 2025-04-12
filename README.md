# SoundCloud Ad Detector (Android)

An Android app that detects **SoundCloud ads** by analyzing notifications and automatically mutes the volume when an ad is playing. Once the ad ends, the volume is restored to its original level — no root access needed.

---

## 🎯 Features

- Detects ads on SoundCloud using notification metadata
- Mutes device volume when an ad is playing
- Automatically restores volume after the ad
- Runs quietly in the background via a `NotificationListenerService`
- Built with **Kotlin** in **Android Studio**

---

## 🧠 How It Works

This app listens for all notifications using a `NotificationListenerService`. When a SoundCloud notification is received, the app checks the **content text or description** to classify it as either:

- A regular song (e.g., includes artist name and track title), or
- An advertisement (e.g., generic or missing content, like "Sponsored" or "Ad playing")

### Detection Logic (Simplified):

```kotlin
override fun onNotificationPosted(sbn: StatusBarNotification) {
    val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
    
    if (notificationText.contains("sponsored", ignoreCase = true) || notificationText.length < 10) {
        // Likely an ad
        muteVolume()
    } else {
        restoreVolume()
    }
}
