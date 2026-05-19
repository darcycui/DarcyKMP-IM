package com.darcy.kmpdemo.network.websocket.listener

interface IOuterListener {
    fun onOpen()
    fun onSend(message: String)
    fun onSend(bytes: ByteArray)
    fun onMessage(body: String, headers: Map<String, String>)
    fun onMessage(bytes: ByteArray, headers: Map<String, String>)
    fun onFailure(errorMessage: String)
    fun onClosed()
}