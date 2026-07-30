package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.bean.ui.RegisterBean

expect object RegisterPlatform {
    fun initRegisterInfo(): RegisterBean
}