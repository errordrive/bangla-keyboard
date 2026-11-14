package com.openbangla.keyboard.monetization

import android.app.Activity
import android.content.Context

/**
 * Ad-free implementation for clean user experience.
 */
class AdMobManager(private val context: Context) {
    fun isEnabled(): Boolean = false
    fun loadInterstitial(onLoaded: (() -> Unit)? = null) {}
    fun showInterstitial(activity: Activity) {}
}
