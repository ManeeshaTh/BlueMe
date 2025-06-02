package com.plcoding.bluetoothchat.domain.chat

sealed class BluetoothMessage {
    data class TextMessage(
        val message: String,
        val senderName: String,
        val isFromLocalUser: Boolean
    ) : BluetoothMessage()

    data class ImageMessage(
        val imageUri: String,
        val imageData: ByteArray? = null,
        val fileName: String? = null,
        val senderName: String,
        val isFromLocalUser: Boolean
    ) : BluetoothMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ImageMessage

            if (imageUri != other.imageUri) return false
            if (!imageData.contentEquals(other.imageData ?: ByteArray(0))) return false
            if (fileName != other.fileName) return false
            if (senderName != other.senderName) return false
            if (isFromLocalUser != other.isFromLocalUser) return false

            return true
        }

        override fun hashCode(): Int {
            var result = imageUri.hashCode()
            result = 31 * result + (imageData?.contentHashCode() ?: 0)
            result = 31 * result + (fileName?.hashCode() ?: 0)
            result = 31 * result + senderName.hashCode()
            result = 31 * result + isFromLocalUser.hashCode()
            return result
        }
    }
}
