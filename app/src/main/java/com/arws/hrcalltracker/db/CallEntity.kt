package com.arws.hrcalltracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * CallEntity — Room Database Table for storing offline/pending calls.
 */
@Entity(tableName = "pending_calls")
data class CallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val phoneNumber: String,
    val callType: String,
    val duration: String,
    val date: String,      // dd/MM/yyyy
    val time: String,      // HH:mm:ss
    val simName: String,
    val dateMillis: Long,  // epoch millis — used for uniqueCallId deduplication

    // Unique ID: number_dateMillis_duration — prevents duplicate DB inserts
    val uniqueCallId: String,

    // Flag indicating if the call was successfully pushed to Google Sheets
    val isSynced: Boolean = false
)
