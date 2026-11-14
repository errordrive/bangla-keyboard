package com.openbangla.keyboard.analytics

interface ExternalTelemetryService {
    fun trackUserSession(sessionId: String)
    fun trackTypingSpeed(wordsPerMinute: Float)
    fun trackThemeSelection(themeName: String)
}

class DefaultTelemetryStub : ExternalTelemetryService {
    override fun trackUserSession(sessionId: String) {}
    override fun trackTypingSpeed(wordsPerMinute: Float) {}
    override fun trackThemeSelection(themeName: String) {}
}
