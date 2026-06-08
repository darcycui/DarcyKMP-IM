package com.darcy.kmpdemo.network.http.impl.ktor.exception

import com.darcy.kmpdemo.log.logD
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException

object ExceptionHelper {
    private val TAG = ExceptionHelper::class.java.simpleName
    private val exceptionMap = mapOf(
        "java.net.ConnectException" to NetworkException.TimeOutException(),
        "java.net.ConnectTimeoutException" to NetworkException.TimeOutException(),
        "java.net.SocketTimeoutException" to NetworkException.TimeOutException(),
        "java.net.SocketException" to NetworkException.UnknownException(),
        "java.net.UnknownHostException" to NetworkException.NotFoundException(),
        "java.net.NoRouteToHostException" to NetworkException.NotFoundException(),
        "java.net.UnknownServiceException" to NetworkException.NotFoundException(),
    )

    fun mapException(throwable: Throwable): NetworkException {
        val clazzName = throwable::class.qualifiedName
        logD("$TAG mapException: $clazzName")
        if (clazzName != null && clazzName in exceptionMap) {
            return exceptionMap[clazzName] ?: NetworkException.UnknownException()
        }
        return when (throwable) {
            is NetworkException -> throwable
            is ConnectTimeoutException -> NetworkException.TimeOutException()
            is ServerResponseException -> NetworkException.ServerException()
            is ClientRequestException -> NetworkException.ClientException()
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