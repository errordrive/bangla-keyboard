package com.openbangla.keyboard.analytics

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class KeyboardFCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("KeyboardFCM", "New FCM registration token received: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("KeyboardFCM", "From: ${remoteMessage.from}, Notification: ${remoteMessage.notification?.body}")
    }
}
