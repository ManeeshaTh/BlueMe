package com.plcoding.bluetoothchat.data.chat

import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage
import androidx.compose.foundation.Image
import android.util.Base64
import java.util.Date

fun String.toBluetoothMessage(isFromLocalUser: Boolean): BluetoothMessage {
    val parts = split("#")
    val name = parts[0]
    val type = parts[1]
    val content = parts[2]
    val timestamp = if (parts.size > 3) Date(parts[3].toLong()) else Date()
    
    return when (type) {
        "TEXT" -> BluetoothMessage.TextMessage(
            message = content,
            senderName = name,
            isFromLocalUser = isFromLocalUser,
            timestamp = timestamp
        )
        "IMAGE" -> {
            val imageParts = content.split("|")
            val imageUri = imageParts[0]
            val imageData = if (imageParts.size > 1) Base64.decode(imageParts[1], Base64.DEFAULT) else null
            val fileName = if (imageParts.size > 2) imageParts[2] else null
            
            BluetoothMessage.ImageMessage(
                imageUri = imageUri,
                imageData = imageData,
                fileName = fileName,
                senderName = name,
                isFromLocalUser = isFromLocalUser,
                timestamp = timestamp
            )
        }
        else -> throw IllegalArgumentException("Unknown message type: $type")
    }
}

fun BluetoothMessage.toByteArray(): ByteArray {
    val timestamp = when (this) {
        is BluetoothMessage.TextMessage -> timestamp
        is BluetoothMessage.ImageMessage -> timestamp
    }
    
    return when (this) {
        is BluetoothMessage.TextMessage -> "$senderName#TEXT#$message#${timestamp.time}"
        is BluetoothMessage.ImageMessage -> {
            val imageDataStr = imageData?.let { Base64.encodeToString(it, Base64.DEFAULT) } ?: ""
            val fileNameStr = fileName ?: ""
            "$senderName#IMAGE#$imageUri|$imageDataStr|$fileNameStr#${timestamp.time}"
        }
    }.encodeToByteArray()
}