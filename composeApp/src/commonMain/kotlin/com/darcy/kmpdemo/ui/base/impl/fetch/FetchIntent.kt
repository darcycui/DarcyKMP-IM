package com.darcy.kmpdemo.ui.base.impl.fetch

import com.darcy.kmpdemo.ui.base.IIntent

sealed class FetchIntent : IIntent {
    data class ActionFetchData(val params: Map<String, String> = mapOf()) : FetchIntent()

    data class RefreshByFetchData<T>(
        val result: T
    ) : FetchIntent()
}