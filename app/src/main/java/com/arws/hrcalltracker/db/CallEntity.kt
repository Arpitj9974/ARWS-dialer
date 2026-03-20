package com.arws.hrcalltracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * CallEntity — Room Database Table for storing offline/pending calls.
 *
 * It uses a unique identifier composite of (phoneNumber + date + duration)
 * to enforce duplicate call protection at the database level.
 */
@Entity(tableName = "pending_calls")
data class CallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val hrName: String,
    val phoneNumber: String,
    val callType: String,
    val duration: String,
    val date: String,
    val simName: String,
    
    // Unique ID used to prevent duplicate uploads: number_date_duration
    val uniqueCallId: String
)
