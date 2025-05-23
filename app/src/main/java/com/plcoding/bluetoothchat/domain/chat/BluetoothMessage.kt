package com.plcoding.bluetoothchat.domain.chat

sealed class BluetoothMessage {
    data class TextMessage(
        val message: String,
        val senderName: String,
        val isFromLocalUser: Boolean
    ) : BluetoothMessage()

    data class ImageMessage(
        val imageUri: String,
        val senderName: String,
        val isFromLocalUser: Boolean
    ) : BluetoothMessage()
}
