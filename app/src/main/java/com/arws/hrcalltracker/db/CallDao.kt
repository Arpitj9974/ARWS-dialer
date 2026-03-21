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

    @Query("SELECT * FROM pending_calls ORDER BY id ASC") // Use ID (arrival order)
    suspend fun getPendingCalls(): List<CallEntity>

    @Query("SELECT * FROM pending_calls ORDER BY id ASC")
    fun getPendingCallsLiveData(): androidx.lifecycle.LiveData<List<CallEntity>>

    @Query("DELETE FROM pending_calls WHERE id = :callId")
    suspend fun deleteCall(callId: Long)

    @Query("SELECT COUNT(*) FROM pending_calls WHERE uniqueCallId = :uniqueId")
    suspend fun checkExists(uniqueId: String): Int
}
