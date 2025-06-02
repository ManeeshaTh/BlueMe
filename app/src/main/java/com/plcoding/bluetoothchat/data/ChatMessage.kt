package com.plcoding.bluetoothchat.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val message: String,
    val senderName: String,
    val isFromLocalUser: Boolean,
    val timestamp: Date,
    val deviceAddress: String, // Bluetooth device address
    val messageType: String = "TEXT", // "TEXT" or "IMAGE"
    val imageData: ByteArray? = null, // Store image data for received images
    val fileName: String? = null // Store filename for received images
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatMessage

        if (id != other.id) return false
        if (message != other.message) return false
        if (senderName != other.senderName) return false
        if (isFromLocalUser != other.isFromLocalUser) return false
        if (timestamp != other.timestamp) return false
        if (deviceAddress != other.deviceAddress) return false
        if (messageType != other.messageType) return false
        if (!imageData.contentEquals(other.imageData)) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + senderName.hashCode()
        result = 31 * result + isFromLocalUser.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + deviceAddress.hashCode()
        result = 31 * result + messageType.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        result = 31 * result + (fileName?.hashCode() ?: 0)
        return result
    }
} 