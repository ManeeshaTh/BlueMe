package com.plcoding.bluetoothchat.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE deviceAddress = :deviceAddress ORDER BY timestamp ASC")
    fun getMessagesForDevice(deviceAddress: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE deviceAddress = :deviceAddress")
    suspend fun deleteMessagesForDevice(deviceAddress: String)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>
} 