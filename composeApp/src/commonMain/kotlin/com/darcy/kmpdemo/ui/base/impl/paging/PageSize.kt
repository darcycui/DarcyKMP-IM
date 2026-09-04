package com.darcy.kmpdemo.ui.base.impl.paging

import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.page_size_10
import org.jetbrains.compose.resources.StringResource

data class PageSize(
    val size: Int,
    val name: StringResource,
    val isSelected: Boolean = false
) {
    companion object {
        val DEFAULT = PageSize(10, Res.string.page_size_10)
    }
}