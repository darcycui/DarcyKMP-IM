package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.bean.ui.RegisterBean

actual object RegisterPlatform {
    actual fun initRegisterInfo(): RegisterBean {
        return RegisterBean(
            username = "手机",
            passwordHash = "123456",
            nickname = "手机手机",
            phone = "156000111222",
            email = "phone@email.com",
        )
    }
}