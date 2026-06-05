package com.darcy.kmpdemo.x3dh

data class MessageKey(
    val fromUserId: Long = 0,
    val dhPublicKey: String = "",
    val messageKey: String = "",
    val macKey: String = "",
    val iv: String = "",
    val nKey: Long = 0,
    val pnKey: Long = 0,
    val url: String = ""
) {
    companion object {
        private const val FROM_USER_ID = "fromUserId"
        private const val DX_PUBLIC_KEY = "dhPublicKey"
        private const val URL = "url"
        private const val N_KEY = "N_KEY"
        private const val PN_KEY = "PN_KEY"

        fun fromMap(map: Map<String, String>): MessageKey {
            return MessageKey(
                fromUserId = map[FROM_USER_ID]?.toLongOrNull() ?: 0,
                dhPublicKey = map[DX_PUBLIC_KEY] ?: "",
                nKey = map[N_KEY]?.toLongOrNull() ?: 0,
                pnKey = map[PN_KEY]?.toLongOrNull() ?: 0,
                url = map[URL] ?: ""
            )
        }
    }

    fun toMap(): Map<String, String> {
        return mapOf(
            FROM_USER_ID to fromUserId.toString(),
            DX_PUBLIC_KEY to dhPublicKey,
            N_KEY to nKey.toString(),
            PN_KEY to pnKey.toString(),
            URL to url
        )
    }
}