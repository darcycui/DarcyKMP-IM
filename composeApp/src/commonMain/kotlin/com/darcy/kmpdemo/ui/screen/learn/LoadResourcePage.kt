package com.darcy.kmpdemo.ui.screen.learn

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.drawable_house
import darcykmp_im.composeapp.generated.resources.load_resource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun ShowLoadResource() {
    var bytes by remember {
        mutableStateOf(ByteArray(0))
    }
    LaunchedEffect(Unit) {
        bytes = Res.readBytes("files/txt/message.txt")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "加载字符串资源")
        Text(text = stringResource(Res.string.load_resource))
        Text(text = "加载文件资源")
        Text(text = bytes.decodeToString())
        Text(text = "加载图片资源")
        Image(
            painter = painterResource(Res.drawable.drawable_house),
            contentDescription = null,
            modifier = Modifier.width(100.dp).height(100.dp)
        )
    }
}