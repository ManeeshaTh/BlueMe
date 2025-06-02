package com.plcoding.bluetoothchat.data.chat

import android.bluetooth.BluetoothSocket
import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage
import com.plcoding.bluetoothchat.domain.chat.ConnectionResult
import com.plcoding.bluetoothchat.domain.chat.TransferFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.DataInputStream
import java.io.DataOutputStream

class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    private val CHUNK_SIZE = 8192 // 8KB chunks

    fun listenForIncomingMessages(): Flow<BluetoothMessage> {
        return flow {
            if(!socket.isConnected) {
                return@flow
            }

            val inputStream = DataInputStream(socket.inputStream)
            while(true) {
                try {
                    // Read message length
                    val messageLength = inputStream.readInt()
                    
                    // Read message data
                    val messageData = ByteArray(messageLength)
                    var bytesRead = 0
                    while (bytesRead < messageLength) {
                        val count = inputStream.read(
                            messageData,
                            bytesRead,
                            messageLength - bytesRead
                        )
                        if (count == -1) break
                        bytesRead += count
                    }

                    emit(
                        messageData.decodeToString().toBluetoothMessage(
                            isFromLocalUser = false
                        )
                    )
                } catch(e: IOException) {
                    throw TransferFailedException()
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = DataOutputStream(socket.outputStream)
                
                // Write message length
                outputStream.writeInt(bytes.size)
                
                // Write message data in chunks
                var offset = 0
                while (offset < bytes.size) {
                    val chunkSize = minOf(CHUNK_SIZE, bytes.size - offset)
                    outputStream.write(bytes, offset, chunkSize)
                    offset += chunkSize
                }
                outputStream.flush()
                true
            } catch(e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }
}