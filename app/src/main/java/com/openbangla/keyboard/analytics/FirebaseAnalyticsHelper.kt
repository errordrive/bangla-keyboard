package com.openbangla.keyboard.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseAnalyticsHelper(context: Context) {

    private val firebaseAnalytics: FirebaseAnalytics? = try {
        FirebaseAnalytics.getInstance(context)
    } catch (e: Exception) {
        null
    }

    private val crashlytics: FirebaseCrashlytics? = try {
        FirebaseCrashlytics.getInstance()
    } catch (e: Exception) {
        null
    }

    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        try {
            val bundle = Bundle()
            params.forEach { (k, v) -> bundle.putString(k, v) }
            firebaseAnalytics?.logEvent(eventName, bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logNonFatalException(throwable: Throwable) {
        try {
            crashlytics?.recordException(throwable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
