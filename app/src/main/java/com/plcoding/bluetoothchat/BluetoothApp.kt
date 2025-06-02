package com.plcoding.bluetoothchat

import android.app.Application
import com.plcoding.bluetoothchat.data.ChatDatabase
import com.plcoding.bluetoothchat.data.ChatRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BluetoothApp : Application() {
    
    // Database instance
    val database by lazy { ChatDatabase.getDatabase(this) }
    
    // Repository instance
    val repository by lazy { ChatRepository(database.chatMessageDao()) }

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide configurations here
    }
}