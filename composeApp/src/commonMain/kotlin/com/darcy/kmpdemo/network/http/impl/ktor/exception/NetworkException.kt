package com.darcy.kmpdemo.network.http.impl.ktor.exception

sealed class NetworkException(val code: Int, message: String) : Exception(message) {
    class UnknownException : NetworkException(-1, "未知异常")
    class CustomException(message: String) : NetworkException(-999, message)

    class TimeOutException : NetworkException(1001, "请求超时")
    class UnauthorizedException : NetworkException(1002, "未授权")
    class ForbiddenException : NetworkException(1003, "无权限")
    class NotFoundException : NetworkException(1004, "未找到请求地址")
    class MethodNotAllowedException : NetworkException(1005, "请求方法错误")
    class ServerException : NetworkException(1006, "服务端错误")
    class ClientException : NetworkException(1007, "客户端错误")
    class SocketException : NetworkException(1007, "连接异常")

    class DataFormatException : NetworkException(2001, "数据格式错误")

}