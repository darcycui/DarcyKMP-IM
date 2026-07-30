package com.darcy.kmpdemo.crypto

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonCryptoHelperTest {

    @Test
    fun stripUrlToPath_standardHttps() {
        val result = JsonCryptoHelper.stripUrlToPath("https://darcycui.com.cn/api/x3dh/push/keys")
        assertEquals("/api/x3dh/push/keys", result)
    }

    @Test
    fun stripUrlToPath_standardHttp() {
        val result = JsonCryptoHelper.stripUrlToPath("http://example.com/api/test")
        assertEquals("/api/test", result)
    }

    @Test
    fun stripUrlToPath_webSocketWss() {
        val result = JsonCryptoHelper.stripUrlToPath("wss://example.com/ws/chat")
        assertEquals("/ws/chat", result)
    }

    @Test
    fun stripUrlToPath_webSocketWs() {
        val result = JsonCryptoHelper.stripUrlToPath("ws://example.com/ws")
        assertEquals("/ws", result)
    }

    @Test
    fun stripUrlToPath_withIpAndPort() {
        val result = JsonCryptoHelper.stripUrlToPath("http://192.168.1.1:8080/api/msg")
        assertEquals("/api/msg", result)
    }

    @Test
    fun stripUrlToPath_withLocalhostAndPort() {
        val result = JsonCryptoHelper.stripUrlToPath("http://localhost:3000/api/test")
        assertEquals("/api/test", result)
    }

    @Test
    fun stripUrlToPath_noScheme() {
        val result = JsonCryptoHelper.stripUrlToPath("darcycui.com.cn/api/x3dh/push/keys")
        assertEquals("/api/x3dh/push/keys", result)
    }

    @Test
    fun stripUrlToPath_domainOnly() {
        val result = JsonCryptoHelper.stripUrlToPath("https://example.com")
        assertEquals("/", result)
    }

    @Test
    fun stripUrlToPath_domainWithTrailingSlash() {
        val result = JsonCryptoHelper.stripUrlToPath("https://example.com/")
        assertEquals("/", result)
    }

    @Test
    fun stripUrlToPath_emptyString() {
        val result = JsonCryptoHelper.stripUrlToPath("")
        assertEquals("/", result)
    }

    @Test
    fun stripUrlToPath_schemeOnly() {
        val result = JsonCryptoHelper.stripUrlToPath("https://")
        assertEquals("/", result)
    }
}
