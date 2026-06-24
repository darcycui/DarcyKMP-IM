package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.bean.ui.RegisterBean

actual object RegisterPlatform {
    actual fun initRegisterInfo(): RegisterBean {
        return RegisterBean(
            username = "网页Wasm",
            passwordHash = "123456",
            nickname = "网页网页Wasm",
            phone = "158000111222",
            email = "webWasm@email.com",
        )
    }
}