package com.openbangla.keyboard

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.openbangla.keyboard.data.db.AppDatabase
import com.openbangla.keyboard.worker.DictionarySyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BanglaApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()

        // 1. Safely initialize Firebase with default fallback options
        // Prevents crash when google-services.json is absent on device
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:123456789012:android:abcdef0123456789")
                    .setApiKey("AIzaSyDummyKeyForOpenBanglaAppId001")
                    .setProjectId("openbangla-keyboard")
                    .setGcmSenderId("123456789012")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }


        // 3. Schedule periodic background dictionary maintenance safely
        try {
            scheduleBackgroundWorker()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun scheduleBackgroundWorker() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<DictionarySyncWorker>(
                24, TimeUnit.HOURS
            ).setConstraints(constraints).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DictionarySyncWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
