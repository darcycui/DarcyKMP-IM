package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.bean.ui.RegisterBean

actual object RegisterPlatform {
    actual fun initRegisterInfo(): RegisterBean {
        return RegisterBean(
            username = "手机iOS",
            passwordHash = "123456",
            nickname = "手机收iOS",
            phone = "159000111222",
            email = "phoneiOS@email.com",
        )
    }
}