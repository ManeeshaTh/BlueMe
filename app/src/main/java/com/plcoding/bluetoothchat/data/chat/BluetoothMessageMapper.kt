package com.plcoding.bluetoothchat.data.chat

import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage
import androidx.compose.foundation.Image

fun String.toBluetoothMessage(isFromLocalUser: Boolean): BluetoothMessage {
    val parts = split("#")
    val name = parts[0]
    val type = parts[1]
    val content = parts[2]
    
    return when (type) {
        "TEXT" -> BluetoothMessage.TextMessage(
            message = content,
            senderName = name,
            isFromLocalUser = isFromLocalUser
        )
        "IMAGE" -> BluetoothMessage.ImageMessage(
            imageUri = content,
            senderName = name,
            isFromLocalUser = isFromLocalUser
        )
        else -> throw IllegalArgumentException("Unknown message type: $type")
    }
}

fun BluetoothMessage.toByteArray(): ByteArray {
    return when (this) {
        is BluetoothMessage.TextMessage -> "$senderName#TEXT#$message"
        is BluetoothMessage.ImageMessage -> "$senderName#IMAGE#$imageUri"
    }.encodeToByteArray()
}