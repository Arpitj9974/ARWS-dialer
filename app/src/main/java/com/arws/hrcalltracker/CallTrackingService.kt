package com.arws.hrcalltracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.launch

/**
 * CallTrackingService — Foreground Service
 *
 * Runs persistently in the background to detect phone call state changes
 * using PhoneStateListener.
 *
 * States monitored:
 *   RINGING  → Someone is calling the phone
 *   OFFHOOK  → Call is active (talking)
 *   IDLE     → No call / call just ended
 *
 * When the state transitions to IDLE after a call (OFFHOOK),
 * it triggers call log processing (will be implemented in Step 3).
 */
class CallTrackingService : Service() {

    companion object {
        private const val TAG = "CallTrackingService"
        private const val NOTIFICATION_CHANNEL_ID = "hr_call_tracker_channel"
        private const val NOTIFICATION_ID = 1001
        @JvmField
        var isServiceRunning = false
    }

    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null

    // Track whether we were in a call so we know when IDLE means "call ended"
    private var wasInCall = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        isServiceRunning = true

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        setupPhoneStateListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY // Restart service if Android kills it
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        isServiceRunning = false

        // Unregister the phone state listener
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    /**
     * Create a notification channel (required for Android 8+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "HR Call Tracker",
                NotificationManager.IMPORTANCE_LOW // Low = no sound, just persistent
            ).apply {
                description = "Keeps the call tracking service running"
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Build the persistent foreground notification.
     */
    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, DashboardActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("RAW")
            .setContentText("Call tracking is active in background")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentIntent(pendingIntent)
            .setOngoing(true)       // Cannot be swiped away
            .setSilent(true)        // No sound
            .build()
    }

    /**
     * Set up the PhoneStateListener to monitor call states.
     *
     * Call flow:
     *   Incoming: RINGING → OFFHOOK → IDLE
     *   Outgoing: OFFHOOK → IDLE
     *   Missed:   RINGING → IDLE
     */
    private fun setupPhoneStateListener() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        phoneStateListener = object : PhoneStateListener() {
            @Deprecated("Deprecated in Java")
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        Log.d(TAG, "📞 RINGING — Incoming call from: $phoneNumber")
                        wasInCall = true
                    }

                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        Log.d(TAG, "📞 OFFHOOK — Call active")
                        wasInCall = true
                    }

                    TelephonyManager.CALL_STATE_IDLE -> {
                        Log.d(TAG, "📞 IDLE — Phone is idle")

                        if (wasInCall) {
                            Log.d(TAG, "✅ Call just ended! Will process call log...")
                            wasInCall = false

                            // ──────────────────────────────────────
                            // Step 3 will add: processLatestCallLog()
                            // For now, just log that we detected the event
                            onCallEnded()
                            // ──────────────────────────────────────
                        }
                    }
                }
            }
        }

        // Start listening
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        Log.d(TAG, "PhoneStateListener registered — now monitoring calls")
    }

    /**
     * Called when a call ends (IDLE after OFFHOOK/RINGING).
     * Reads the latest call log entry and filters by company SIM.
     *
     * Uses a short delay because Android may take a moment
     * to write the call log entry after the call ends.
     */
    private fun onCallEnded() {
        Log.d(TAG, "📋 Call ended — reading call log in 1.5 seconds...")

        // Small delay to ensure Android has written the call log entry
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val callLogHelper = CallLogHelper(this)
            val callInfo = callLogHelper.getLatestCallLog()

            if (callInfo != null) {
                // ── SIM FILTERING ──────────────────────
                val prefs = PrefsManager(this)
                val companySimId = prefs.getCompanySimId()

                Log.d(TAG, "🔍 SIM Check: call SIM=${callInfo.subscriptionId}, company SIM=$companySimId")

                if (callInfo.subscriptionId == companySimId) {
                    // ✅ Company SIM — process this call
                    Log.d(TAG, "✅ COMPANY SIM CALL — Recording:")
                    Log.d(TAG, "   Number:   ${callInfo.phoneNumber}")
                    Log.d(TAG, "   Type:     ${callInfo.callType}")
                    Log.d(TAG, "   Duration: ${callInfo.duration} sec")
                    Log.d(TAG, "   Date:     ${callInfo.date} ${callInfo.time}")
                    Log.d(TAG, "   SIM ID:   ${callInfo.subscriptionId}")

                    // ── SAVE LOCALLY & SYNC ──────────────
                    // Unique ID: number_dateMillis_duration_type_sim
                    val uniqueCallId = "${callInfo.phoneNumber}_${callInfo.dateMillis}_${callInfo.duration}_${callInfo.callType}_${callInfo.subscriptionId}"

                    // Insert to database and sync using Coroutines
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        val db = com.arws.hrcalltracker.db.AppDatabase.getDatabase(this@CallTrackingService)
                        val exists = db.callDao().checkExists(uniqueCallId)

                        if (exists > 0) {
                            Log.d(TAG, "⚠️ Duplicate call detected (ID: $uniqueCallId). Ignoring.")
                        } else {
                            val entity = com.arws.hrcalltracker.db.CallEntity(
                                phoneNumber = callInfo.phoneNumber,
                                callType = callInfo.callType,
                                duration = callInfo.duration,
                                date = callInfo.date,
                                time = callInfo.time,
                                simName = prefs.getCompanySimName(),
                                dateMillis = callInfo.dateMillis,
                                uniqueCallId = uniqueCallId
                            )

                            try {
                                val insertedId = db.callDao().insertCall(entity)
                                Log.d(TAG, "💾 Saved call locally to Room DB (Row ID: $insertedId)")
                            } catch (e: Exception) {
                                Log.d(TAG, "⚠️ Call insert blocked by Room DB constraint: $uniqueCallId")
                            }

                            // Trigger sync after saving
                            SyncManager(this@CallTrackingService).syncPendingCalls()
                        }
                    }
                    // ──────────────────────────────────────

                } else {
                    // ❌ Personal SIM — ignore
                    Log.d(TAG, "❌ PERSONAL SIM CALL — Ignored (SIM ${callInfo.subscriptionId})")
                }
                // ───────────────────────────────────────

            } else {
                Log.w(TAG, "⚠️ Could not read call log")
            }
        }, 1500) // 1.5 second delay
    }
}
