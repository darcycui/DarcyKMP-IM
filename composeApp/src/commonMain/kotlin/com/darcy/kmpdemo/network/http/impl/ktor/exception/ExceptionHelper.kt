package com.darcy.kmpdemo.network.http.impl.ktor.exception

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logW
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonDecodingException

object ExceptionHelper {
    private val TAG = ExceptionHelper::class.simpleName
    private val exceptionMap = mapOf(
        "java.net.ConnectException" to NetworkException.TimeOutException(),
        "java.net.ConnectTimeoutException" to NetworkException.TimeOutException(),
        "java.net.SocketTimeoutException" to NetworkException.TimeOutException(),
        "java.net.SocketException" to NetworkException.SocketException(),
        "java.net.UnknownHostException" to NetworkException.NotFoundException(),
        "java.net.NoRouteToHostException" to NetworkException.NotFoundException(),
        "java.net.UnknownServiceException" to NetworkException.NotFoundException(),
    )

    @OptIn(ExperimentalSerializationApi::class)
    fun mapException(throwable: Throwable): NetworkException {
        // 获取异常类名:Throwable.toString()在所有平台都能正常工作，它默认输出格式为 类全限定名: 异常消息
//        val clazzName = throwable::class.qualifiedName
        val clazzName = throwable::class.simpleName
        logW("$TAG 异常 mapException: $clazzName")
        if (clazzName in exceptionMap) {
            return exceptionMap[clazzName] ?: NetworkException.UnknownException()
        }
        return when (throwable) {
            is NetworkException -> throwable
            is ConnectTimeoutException -> NetworkException.TimeOutException()
            is ServerResponseException -> NetworkException.ServerException()
            is ClientRequestException -> NetworkException.ClientException()
            is JsonDecodingException -> NetworkException.DataFormatException()
            else -> {
                val message = throwable.message
                if (message != null && message.length in 1..20) {
                    NetworkException.CustomException(throwable.message!!)
                } else {
                    NetworkException.UnknownException()
                }
            }
        }
    }
}