package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.bean.ui.RegisterBean

actual object RegisterPlatform {
    actual fun initRegisterInfo(): RegisterBean {
        return RegisterBean(
            username = "电脑",
            passwordHash = "123456",
            nickname = "电脑电脑",
            phone = "155000111222",
            email = "desktop@email.com",
        )
    }
}