package com.arws.hrcalltracker.db

/**
 * HRAccount — Data model for HR personnel stored in Firebase Firestore.
 */
data class HRAccount(
    val hr_name: String = "",
    val employee_id: String = "",
    val username: String = "",
    val password: String = "",
    val script_url: String = ""
)
