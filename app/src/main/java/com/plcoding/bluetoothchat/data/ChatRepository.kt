package com.plcoding.bluetoothchat.data

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatMessageDao: ChatMessageDao) {
    
    fun getMessagesForDevice(deviceAddress: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesForDevice(deviceAddress)
    }

    suspend fun insertMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun deleteMessagesForDevice(deviceAddress: String) {
        chatMessageDao.deleteMessagesForDevice(deviceAddress)
    }

    fun getAllMessages(): Flow<List<ChatMessage>> {
        return chatMessageDao.getAllMessages()
    }
} 