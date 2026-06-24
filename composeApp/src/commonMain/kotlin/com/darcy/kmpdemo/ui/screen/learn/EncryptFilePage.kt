package com.darcy.kmpdemo.ui.screen.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darcy.kmpdemo.platform.EncryptPlatform
import com.darcy.kmpdemo.platform.Platform

@Composable
fun ShowEncryptFile() {

    var content by remember { mutableStateOf(Platform.getPlatform().name + " " + Platform.getPlatform().version) }

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = content)
        Button(onClick = { content = EncryptPlatform.encryptString(content) }) {
            Text("Encrypt")
        }
        Button(onClick = { content = EncryptPlatform.decryptString(content) }) {
            Text("Decrypt")
        }
        TextField(value = content, onValueChange = { content = it })

    }
}