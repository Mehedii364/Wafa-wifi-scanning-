package com.example.core.database

import kotlinx.coroutines.flow.Flow

interface WifiSessionRepository {
    fun getAllSessions(): Flow<List<WifiSessionEntity>>
    fun getSessionsBySessionId(sessionId: String): Flow<List<WifiSessionEntity>>
    fun searchSessions(query: String): Flow<List<WifiSessionEntity>>
    fun getSessionCount(): Flow<Int>
    suspend fun insertSession(session: WifiSessionEntity): Long
    suspend fun insertAll(sessions: List<WifiSessionEntity>)
    suspend fun deleteSession(session: WifiSessionEntity)
    suspend fun deleteById(id: Long)
    suspend fun clearAll()
}

class WifiSessionRepositoryImpl(
    private val dao: WifiSessionDao
) : WifiSessionRepository {

    override fun getAllSessions(): Flow<List<WifiSessionEntity>> = dao.getAllSessions()

    override fun getSessionsBySessionId(sessionId: String): Flow<List<WifiSessionEntity>> =
        dao.getSessionsBySessionId(sessionId)

    override fun searchSessions(query: String): Flow<List<WifiSessionEntity>> =
        dao.searchSessions(query)

    override fun getSessionCount(): Flow<Int> = dao.getSessionCount()

    override suspend fun insertSession(session: WifiSessionEntity): Long = dao.insert(session)

    override suspend fun insertAll(sessions: List<WifiSessionEntity>) = dao.insertAll(sessions)

    override suspend fun deleteSession(session: WifiSessionEntity) = dao.delete(session)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun clearAll() = dao.clearAll()
}
