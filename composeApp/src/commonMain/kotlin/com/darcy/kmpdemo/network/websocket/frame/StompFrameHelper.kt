package com.darcy.kmpdemo.network.websocket.frame

import kotlinx.io.bytestring.decodeToString
import org.hildan.krossbow.stomp.frame.FrameBody

fun FrameBody?.toJsonString(): String {
    return when (this) {
        null -> "null"
        is FrameBody.Text -> this.text
        is FrameBody.Binary -> "二进制数据不能转为json字符串"
    }
}