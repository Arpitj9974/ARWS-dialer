package com.arws.hrcalltracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * BootReceiver — Listens for the BOOT_COMPLETED broadcast.
 *
 * Automatically starts the CallTrackingService when the phone finishes booting,
 * ensuring call tracking resumes even if the phone has been restarted.
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
            } else {
                Log.d(TAG, "⏸️ Setup not complete — not starting service")
            }
        }
    }
}
