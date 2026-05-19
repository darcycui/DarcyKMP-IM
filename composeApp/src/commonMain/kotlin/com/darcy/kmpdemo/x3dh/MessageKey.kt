package com.darcy.kmpdemo.x3dh

data class MessageKey(
    val fromUserId: Long = 0,
    val dxPublicKey: String = "",
    val sendingIndex: Long = 0,
    val receivingIndex: Long = 0,
    val messageKey: String = "",
) {
    companion object {
        private const val FROM_USER_ID = "fromUserId"
        private const val DX_PUBLIC_KEY = "dhPublicKey"
        private const val SENDING_INDEX = "sendingIndex"
        private const val RECEIVING_INDEX = "receivingIndex"
        private const val MESSAGE_KEY = "messageKey"

        fun fromMap(map: Map<String, String>): MessageKey {
            return MessageKey(
                fromUserId = map[FROM_USER_ID]?.toLongOrNull() ?: 0,
                dxPublicKey = map[DX_PUBLIC_KEY] ?: "",
                sendingIndex = map[SENDING_INDEX]?.toLongOrNull() ?: 0,
                receivingIndex = map[RECEIVING_INDEX]?.toLongOrNull() ?: 0,
            )
        }
    }

    fun toMap(): Map<String, String> {
        return mapOf(
            FROM_USER_ID to fromUserId.toString(),
            DX_PUBLIC_KEY to dxPublicKey,
            SENDING_INDEX to sendingIndex.toString(),
            RECEIVING_INDEX to receivingIndex.toString(),
        )
    }
}