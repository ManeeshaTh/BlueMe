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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage
import com.plcoding.bluetoothchat.ui.theme.*
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
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .wrapContentWidth(if (isFromLocalUser) Alignment.End else Alignment.Start)
    ) {
        Column(
            modifier = Modifier
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(
                        topStart = if (isFromLocalUser) 16.dp else 2.dp,
                        topEnd = if (isFromLocalUser) 2.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .clip(
                    RoundedCornerShape(
                        topStart = if (isFromLocalUser) 16.dp else 2.dp,
                        topEnd = if (isFromLocalUser) 2.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(
                    if (isFromLocalUser) SenderBubble else ReceiverBubble
                )
                .padding(12.dp)
        ) {
            Text(
                text = senderName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isFromLocalUser) SenderText.copy(alpha = 0.7f) else ReceiverText.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            when (message) {
                is BluetoothMessage.TextMessage -> {
                    Text(
                        text = message.message,
                        color = if (isFromLocalUser) SenderText else ReceiverText,
                        fontSize = 16.sp,
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
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        
                        if (!message.isFromLocalUser && message.imageData != null) {
                            Button(
                                onClick = {
                                    saveImageToGallery(context, message.imageData, message.fileName ?: "received_image.jpg")
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Blue500,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save", fontSize = 12.sp)
                            }
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