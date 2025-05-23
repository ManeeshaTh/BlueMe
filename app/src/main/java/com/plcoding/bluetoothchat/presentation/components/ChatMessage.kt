package com.plcoding.bluetoothchat.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(message.imageUri)
                            .build()
                    ),
                    contentDescription = "Shared image",
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                        .heightIn(max = 250.dp),
                    contentScale = ContentScale.Fit
                )
            }
            else -> Alignment.Start
        }
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