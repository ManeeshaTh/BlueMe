package com.plcoding.bluetoothchat.presentation.components

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage
import com.plcoding.bluetoothchat.ui.theme.BluetoothChatTheme
import com.plcoding.bluetoothchat.ui.theme.OldRose
import com.plcoding.bluetoothchat.ui.theme.Vanilla
import java.io.File
import java.io.FileOutputStream

@Composable
fun ChatMessage(
    message: BluetoothMessage,
    modifier: Modifier = Modifier
) {
    val isFromLocalUser = when (message) {
        is BluetoothMessage.TextMessage -> message.isFromLocalUser
        is BluetoothMessage.ImageMessage -> message.isFromLocalUser
    }
    
    val senderName = when (message) {
        is BluetoothMessage.TextMessage -> message.senderName
        is BluetoothMessage.ImageMessage -> message.senderName
    }

    val context = LocalContext.current
    var showSaveButton by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (isFromLocalUser) 15.dp else 0.dp,
                    topEnd = 15.dp,
                    bottomStart = 15.dp,
                    bottomEnd = if (isFromLocalUser) 0.dp else 15.dp
                )
            )
            .background(
                if (isFromLocalUser) OldRose else Vanilla
            )
            .padding(16.dp)
    ) {
        Text(
            text = senderName,
            fontSize = 10.sp,
            color = Color.Black
        )
        when (message) {
            is BluetoothMessage.TextMessage -> {
                Text(
                    text = message.message,
                    color = Color.Black,
                    modifier = Modifier.widthIn(max = 250.dp)
                )
            }
            is BluetoothMessage.ImageMessage -> {
                Box(
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                        .heightIn(max = 250.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(
                                    if (message.isFromLocalUser) message.imageUri
                                    else message.imageData
                                )
                                .build()
                        ),
                        contentDescription = "Shared image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    if (!message.isFromLocalUser && message.imageData != null) {
                        TextButton(
                            onClick = {
                                saveImageToGallery(context, message.imageData, message.fileName ?: "received_image.jpg")
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun saveImageToGallery(context: android.content.Context, imageData: ByteArray, fileName: String) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(imageData)
                }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val imageFile = File(imagesDir, fileName)
            FileOutputStream(imageFile).use { outputStream ->
                outputStream.write(imageData)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview
@Composable
fun ChatMessagePreview() {
    BluetoothChatTheme {
        ChatMessage(
            message = BluetoothMessage.TextMessage(
                message = "Hello World!",
                senderName = "Pixel 6",
                isFromLocalUser = false
            )
        )
    }
}