package com.darcy.kmpdemo.ui.base.impl.paging

import com.darcy.kmpdemo.ui.base.IIntent

sealed class PagingIntent : IIntent {
    data class ActionLoadNewPage(
        val pageNumber: Int,
        val params: Map<String, String> = emptyMap(),
    ) : PagingIntent()

    data class RefreshByLoadNewPage<R>(
        val pageNumber: Int,
        val response: R,
    ) : PagingIntent()
}