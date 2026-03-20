package com.arws.hrcalltracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * CallDao — Data Access Object for Room database.
 *
 * Handles inserting pending calls, fetching them for retry,
 * and deleting them once successfully sent to Google Sheets.
 */
@Dao
interface CallDao {

    /**
     * Insert a call into the pending queue.
     * Uses IGNORE strategy to naturally drop exactly duplicate calls
     * (matching number + date + duration).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCall(call: CallEntity): Long

    /**
     * Get all pending calls that need to be uploaded.
     */
    @Query("SELECT * FROM pending_calls ORDER BY date ASC")
    suspend fun getPendingCalls(): List<CallEntity>

    /**
     * Get pending calls as LiveData for UI updates.
     */
    @Query("SELECT * FROM pending_calls ORDER BY date ASC")
    fun getPendingCallsLiveData(): androidx.lifecycle.LiveData<List<CallEntity>>

    /**
     * Delete a specific call after successful upload.
     */
    @Query("DELETE FROM pending_calls WHERE id = :callId")
    suspend fun deleteCall(callId: Long)

    /**
     * Check if a specific call already exists to prevent duplicates.
     */
    @Query("SELECT COUNT(*) FROM pending_calls WHERE uniqueCallId = :uniqueId")
    suspend fun checkExists(uniqueId: String): Int
}
