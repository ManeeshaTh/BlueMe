package com.plcoding.bluetoothchat.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.bluetoothchat.data.ChatMessage
import com.plcoding.bluetoothchat.data.ChatRepository
import com.plcoding.bluetoothchat.domain.chat.BluetoothController
import com.plcoding.bluetoothchat.domain.chat.BluetoothDeviceDomain
import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage
import com.plcoding.bluetoothchat.domain.chat.ConnectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
    private val chatRepository: ChatRepository
): ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ) { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            messages = if(state.isConnected) state.messages else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    private var deviceConnectionJob: Job? = null
    private var currentDeviceAddress: String? = null

    init {
        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update { it.copy(
                errorMessage = error
            ) }
        }.launchIn(viewModelScope)
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        _state.update { it.copy(isConnecting = true) }
        currentDeviceAddress = device.address
        // Load chat history for this device
        viewModelScope.launch {
            chatRepository.getMessagesForDevice(device.address).collect { messages ->
                _state.update { it.copy(
                    messages = messages.map { chatMessage ->
                        when (chatMessage.messageType) {
                            "IMAGE" -> BluetoothMessage.ImageMessage(
                                imageUri = chatMessage.message,
                                imageData = chatMessage.imageData,
                                fileName = chatMessage.fileName,
                                senderName = chatMessage.senderName,
                                isFromLocalUser = chatMessage.isFromLocalUser
                            )
                            else -> BluetoothMessage.TextMessage(
                                message = chatMessage.message,
                                senderName = chatMessage.senderName,
                                isFromLocalUser = chatMessage.isFromLocalUser
                            )
                        }
                    }
                ) }
            }
        }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        currentDeviceAddress = null
        _state.update { it.copy(
            isConnecting = false,
            isConnected = false
        ) }
    }

    fun waitForIncomingConnections() {
        _state.update { it.copy(isConnecting = true) }
        // Load chat history for incoming connections
        viewModelScope.launch {
            chatRepository.getAllMessages().collect { messages ->
                _state.update { it.copy(
                    messages = messages.map { chatMessage ->
                        when (chatMessage.messageType) {
                            "IMAGE" -> BluetoothMessage.ImageMessage(
                                imageUri = chatMessage.message,
                                imageData = chatMessage.imageData,
                                fileName = chatMessage.fileName,
                                senderName = chatMessage.senderName,
                                isFromLocalUser = chatMessage.isFromLocalUser
                            )
                            else -> BluetoothMessage.TextMessage(
                                message = chatMessage.message,
                                senderName = chatMessage.senderName,
                                isFromLocalUser = chatMessage.isFromLocalUser
                            )
                        }
                    }
                ) }
            }
        }
        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendMessage(message)
            if(bluetoothMessage != null) {
                // Save message to database
                currentDeviceAddress?.let { address ->
                    chatRepository.insertMessage(
                        ChatMessage(
                            message = message,
                            senderName = (bluetoothMessage as BluetoothMessage.TextMessage).senderName,
                            isFromLocalUser = true,
                            timestamp = Date(),
                            deviceAddress = address
                        )
                    )
                }
                _state.update { it.copy(
                    messages = it.messages + bluetoothMessage
                ) }
            }
        }
    }

    fun sendImage(imageUri: Uri) {
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendImage(imageUri)
            if(bluetoothMessage != null) {
                // Save image message to database
                currentDeviceAddress?.let { address ->
                    chatRepository.insertMessage(
                        ChatMessage(
                            message = imageUri.toString(),
                            senderName = (bluetoothMessage as BluetoothMessage.ImageMessage).senderName,
                            isFromLocalUser = true,
                            timestamp = Date(),
                            deviceAddress = address,
                            messageType = "IMAGE"
                        )
                    )
                }
                _state.update { it.copy(
                    messages = it.messages + bluetoothMessage
                ) }
            }
        }
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when(result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update { it.copy(
                        isConnected = true,
                        isConnecting = false,
                        errorMessage = null
                    ) }
                }
                is ConnectionResult.TransferSucceeded -> {
                    // Save received message to database
                    currentDeviceAddress?.let { address ->
                        when (val message = result.message) {
                            is BluetoothMessage.TextMessage -> {
                                chatRepository.insertMessage(
                                    ChatMessage(
                                        message = message.message,
                                        senderName = message.senderName,
                                        isFromLocalUser = false,
                                        timestamp = Date(),
                                        deviceAddress = address,
                                        messageType = "TEXT"
                                    )
                                )
                            }
                            is BluetoothMessage.ImageMessage -> {
                                chatRepository.insertMessage(
                                    ChatMessage(
                                        message = message.imageUri,
                                        senderName = message.senderName,
                                        isFromLocalUser = false,
                                        timestamp = Date(),
                                        deviceAddress = address,
                                        messageType = "IMAGE",
                                        imageData = message.imageData,
                                        fileName = message.fileName
                                    )
                                )
                            }
                        }
                    }
                    _state.update { it.copy(
                        messages = it.messages + result.message
                    ) }
                }
                is ConnectionResult.Error -> {
                    _state.update { it.copy(
                        isConnected = false,
                        isConnecting = false,
                        errorMessage = result.message
                    ) }
                }
            }
        }
            .catch { throwable ->
                bluetoothController.closeConnection()
                _state.update { it.copy(
                    isConnected = false,
                    isConnecting = false,
                ) }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}