package com.arws.hrcalltracker

import android.content.Context
import android.content.SharedPreferences

/**
 * Centralized SharedPreferences manager for storing HR login data,
 * selected company SIM info, and setup state.
 */
class PrefsManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "hr_call_tracker_prefs"
        private const val KEY_HR_NAME = "hr_name"
        private const val KEY_COMPANY_SIM_ID = "company_sim_id"
        private const val KEY_COMPANY_SIM_NAME = "company_sim_name"
        private const val KEY_SETUP_COMPLETE = "setup_complete"
        private const val KEY_LAST_UPLOADED_TIMESTAMP = "last_uploaded_timestamp"
        private const val KEY_SCRIPT_URL = "script_url"
        private const val KEY_LAST_PROCESSED_BOUNDARY = "last_processed_boundary"
        private const val KEY_SYNC_RUNNING = "sync_running"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Google Script URL (Dynamic) ---
    fun saveScriptUrl(url: String) {
        prefs.edit().putString(KEY_SCRIPT_URL, url).apply()
    }

    fun getScriptUrl(): String {
        return prefs.getString(KEY_SCRIPT_URL, "") ?: ""
    }

    // --- HR Name ---
    fun saveHrName(name: String) {
        prefs.edit().putString(KEY_HR_NAME, name).apply()
    }

    fun getHrName(): String {
        return prefs.getString(KEY_HR_NAME, "") ?: ""
    }

    // --- Company SIM Subscription ID ---
    fun saveCompanySimId(simId: Int) {
        prefs.edit().putInt(KEY_COMPANY_SIM_ID, simId).apply()
    }

    fun getCompanySimId(): Int {
        return prefs.getInt(KEY_COMPANY_SIM_ID, -1)
    }

    // --- Company SIM Display Name ---
    fun saveCompanySimName(name: String) {
        prefs.edit().putString(KEY_COMPANY_SIM_NAME, name).apply()
    }

    fun getCompanySimName(): String {
        return prefs.getString(KEY_COMPANY_SIM_NAME, "") ?: ""
    }

    // --- Setup Complete Flag ---
    fun setSetupComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETE, complete).apply()
    }

    fun isSetupComplete(): Boolean {
        return prefs.getBoolean(KEY_SETUP_COMPLETE, false)
    }

    // --- Last Uploaded Call Timestamp (legacy, kept for backward compat) ---
    fun saveLastUploadedTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_UPLOADED_TIMESTAMP, timestamp).apply()
    }

    fun getLastUploadedTimestamp(): Long {
        return prefs.getLong(KEY_LAST_UPLOADED_TIMESTAMP, 0L)
    }

    // --- Last Processed Boundary (clock-aligned epoch millis for scan windows) ---
    fun saveLastProcessedBoundary(boundary: Long) {
        prefs.edit().putLong(KEY_LAST_PROCESSED_BOUNDARY, boundary).commit()
    }

    fun getLastProcessedBoundary(): Long {
        return prefs.getLong(KEY_LAST_PROCESSED_BOUNDARY, 0L)
    }

    // --- Sync Running Lock Flag (lightweight backup to Mutex) ---
    fun setSyncRunning(running: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_RUNNING, running).commit()
    }

    fun isSyncRunning(): Boolean {
        return prefs.getBoolean(KEY_SYNC_RUNNING, false)
    }
}
