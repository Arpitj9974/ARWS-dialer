package com.arws.hrcalltracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * BootReceiver — Listens for the BOOT_COMPLETED broadcast.
 *
 * Automatically starts the CallTrackingService and schedules clock-aligned
 * periodic sync work when the phone finishes booting.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "📲 Device booted — checking if Call Tracking needs to start")

            val prefs = PrefsManager(context)

            // Only start the service if initial setup (Login + SIM select) is complete
            if (prefs.isSetupComplete()) {
                Log.d(TAG, "✅ Setup complete — starting CallTrackingService")
                val serviceIntent = Intent(context, CallTrackingService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                // Reset sync lock flag (may be stale from before reboot)
                prefs.setSyncRunning(false)

                // Schedule clock-aligned periodic sync work
                scheduleClockAlignedSync(context)
            } else {
                Log.d(TAG, "⏸️ Setup not complete — not starting service")
            }
        }
    }

    private fun scheduleClockAlignedSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val nowMillis = System.currentTimeMillis()
        val initialDelayMs = PeriodicSyncWorker.getDelayToNextBoundary(nowMillis)

        Log.d(TAG, "⏰ Scheduling clock-aligned sync. Next boundary in ${initialDelayMs / 1000}s")

        val workRequest = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(30, TimeUnit.MINUTES)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PeriodicSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}

