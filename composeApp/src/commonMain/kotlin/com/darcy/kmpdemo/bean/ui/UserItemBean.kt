package com.darcy.kmpdemo.bean.ui

import com.darcy.kmpdemo.bean.http.base.IUIBean

data class UserItemBean(
    val id: Long = -1,
    val name: String = "",
    val phone: String = "",
    val nickName: String = "",
    val avatar: String = "",
    val age: Int = 0,
    val sex: String = "",
) : IUIBean {
    companion object {
        fun empty(): UserItemBean {
            return UserItemBean()
        }
    }
}