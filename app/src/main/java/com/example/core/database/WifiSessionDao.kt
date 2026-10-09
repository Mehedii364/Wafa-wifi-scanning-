package com.example.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiSessionDao {

    @Query("SELECT * FROM wifi_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WifiSessionEntity>>

    @Query("SELECT * FROM wifi_sessions WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getSessionsBySessionId(sessionId: String): Flow<List<WifiSessionEntity>>

    @Query("SELECT * FROM wifi_sessions WHERE ssid LIKE '%' || :query || '%' OR userLabel LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchSessions(query: String): Flow<List<WifiSessionEntity>>

    @Query("SELECT COUNT(*) FROM wifi_sessions")
    fun getSessionCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WifiSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<WifiSessionEntity>)

    @Update
    suspend fun update(session: WifiSessionEntity)

    @Delete
    suspend fun delete(session: WifiSessionEntity)

    @Query("DELETE FROM wifi_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM wifi_sessions")
    suspend fun clearAll()
}
