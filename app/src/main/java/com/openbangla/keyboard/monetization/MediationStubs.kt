package com.openbangla.keyboard.monetization

import android.content.Context
import android.view.View

/**
 * Pluggable Mediation Slot Interfaces for Meta Audience Network, AppLovin, and Vungle
 */
interface MetaAudienceNetworkMediationSlot {
    fun initialize(context: Context, placementId: String)
    fun loadBanner(context: Context, onLoaded: (View?) -> Unit)
    fun showInterstitial()
}

interface AppLovinMediationSlot {
    fun initialize(context: Context, sdkKey: String)
    fun loadAd(onLoaded: () -> Unit)
    fun showAd()
}

interface VungleMediationSlot {
    fun initialize(context: Context, appId: String)
    fun loadPlacement(placementId: String, onLoaded: () -> Unit)
    fun playAd(placementId: String)
}

class DefaultMetaAudienceNetworkStub : MetaAudienceNetworkMediationSlot {
    override fun initialize(context: Context, placementId: String) {}
    override fun loadBanner(context: Context, onLoaded: (View?) -> Unit) { onLoaded(null) }
    override fun showInterstitial() {}
}

class DefaultAppLovinStub : AppLovinMediationSlot {
    override fun initialize(context: Context, sdkKey: String) {}
    override fun loadAd(onLoaded: () -> Unit) {}
    override fun showAd() {}
}

class DefaultVungleStub : VungleMediationSlot {
    override fun initialize(context: Context, appId: String) {}
    override fun loadPlacement(placementId: String, onLoaded: () -> Unit) {}
    override fun playAd(placementId: String) {}
}
