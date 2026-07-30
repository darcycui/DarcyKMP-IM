package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.bean.ui.RegisterBean

actual object RegisterPlatform {
    actual fun initRegisterInfo(): RegisterBean {
        return RegisterBean(
            username = "网页",
            passwordHash = "123456",
            nickname = "网页网页",
            phone = "157000111222",
            email = "web@email.com",
        )
    }
}