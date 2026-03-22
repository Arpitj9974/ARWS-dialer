package com.arws.hrcalltracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * CallDao — Data Access Object for Room database.
 */
@Dao
interface CallDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCall(call: CallEntity): Long

    @Query("SELECT * FROM pending_calls WHERE isSynced = 0 ORDER BY id ASC") // Only get calls not yet synced
    suspend fun getPendingCalls(): List<CallEntity>

    @Query("SELECT * FROM pending_calls WHERE isSynced = 0 ORDER BY id ASC")
    fun getPendingCallsLiveData(): androidx.lifecycle.LiveData<List<CallEntity>>

    @Query("UPDATE pending_calls SET isSynced = 1 WHERE id = :callId")
    suspend fun markAsSynced(callId: Long)

    @Query("SELECT COUNT(*) FROM pending_calls WHERE uniqueCallId = :uniqueId")
    suspend fun checkExists(uniqueId: String): Int

    @Query("DELETE FROM pending_calls WHERE isSynced = 1 AND dateMillis < :thresholdMillis")
    suspend fun deleteOldSyncedCalls(thresholdMillis: Long)
}
