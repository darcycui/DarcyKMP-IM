package com.darcy.kmpdemo.bean.http.error

import com.darcy.kmpdemo.network.http.impl.ktor.exception.ExceptionHelper
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val timestamp: Long = 0,
    var status: Int = 0,
    var error: String = "",
    var message: String = "",
    var path: String = "/",
) {
    companion object {

        val HTTP_REQUEST_ERROR = ErrorResponse(
            status = -1,
            error = "Http Request Error",
            message = "Http请求失败",
            path = "/"
        )

        fun create(
            status: Int = -1,
            error: String = "",
            message: String,
            path: String = "/"
        ): ErrorResponse {
            return ErrorResponse(
                status = status,
                error = error,
                message = "发生错误:$message",
                path = path
            )
        }

        fun create(
            status: Int = -1,
            error: String = "",
            throwable: Throwable,
            path: String = "/"
        ): ErrorResponse {
            val mappedException = ExceptionHelper.mapException(throwable)
            return ErrorResponse(
                status = status,
                error = error,
                message = "发生错误:${mappedException.message}",
                path = path
            )
        }
    }
}

fun ErrorResponse.toTipsIntent(
    title: String = "发生错误",
    middleButtonText: String = "确定"
): TipsIntent {
    return TipsIntent.ShowTips(
        title = title,
        tips = message,
        code = status,
        middleButtonText = middleButtonText
    )
}