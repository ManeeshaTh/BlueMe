package com.plcoding.bluetoothchat.domain.chat

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name: String?,   //name of the bluetooth device
    val address: String   //mac address of the device
)
