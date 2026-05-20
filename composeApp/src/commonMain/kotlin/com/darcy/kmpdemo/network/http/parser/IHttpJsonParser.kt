package com.darcy.kmpdemo.network.http.parser

import com.darcy.kmpdemo.bean.http.base.BaseResult
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import kotlinx.serialization.KSerializer

interface IHttpJsonParser {
    fun <T> toBean(
        json: String,
        kSerializer: KSerializer<T>,
        success: ((BaseResult<T>) -> Unit),
        successList: ((BaseResult<List<T>>) -> Unit),
        error: ((ErrorResponse) -> Unit)
    )
}