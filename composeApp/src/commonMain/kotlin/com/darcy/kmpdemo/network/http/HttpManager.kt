package com.darcy.kmpdemo.network.http

import com.darcy.kmpdemo.bean.http.base.BaseResult
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.network.http.impl.ktor.KtorHttpClient
import kotlinx.serialization.KSerializer

object HttpManager : IHttp {
    private val iHttp: IHttp = KtorHttpClient()

    override fun <T> doGetRequest(
        serializer: KSerializer<T>,
        url: String,
        params: Map<String, String>,
        needRetry: Boolean,
        needCache: Boolean,
        success: (BaseResult<T>) -> Unit,
        successList: (BaseResult<List<T>>) -> Unit,
        errors: (ErrorResponse) -> Unit
    ) {
        iHttp.doGetRequest(
            serializer, url, params, needRetry, needCache, success, successList, errors
        )
    }

    override fun <T> doPostFormRequest(
        serializer: KSerializer<T>,
        url: String,
        params: Map<String, String>,
        needRetry: Boolean,
        needCache: Boolean,
        success: (BaseResult<T>) -> Unit,
        successList: (BaseResult<List<T>>) -> Unit,
        errors: (ErrorResponse) -> Unit,
        encrypt: Boolean
    ) {
        iHttp.doPostFormRequest(
            serializer, url, params, needRetry, needCache, success, successList, errors, encrypt
        )
    }

    override fun <R, T> doPostJsonRequest(
        serializerR: KSerializer<R>,
        serializerT: KSerializer<T>,
        url: String,
        params: R,
        needRetry: Boolean,
        needCache: Boolean,
        success: (BaseResult<T>) -> Unit,
        successList: (BaseResult<List<T>>) -> Unit,
        errors: (ErrorResponse) -> Unit,
        encrypt: Boolean
    ) {
        iHttp.doPostJsonRequest(
            serializerR,
            serializerT,
            url,
            params,
            needRetry,
            needCache,
            success,
            successList,
            errors,
            encrypt
        )
    }
}