package com.darcy.kmpdemo.storage.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

actual fun getIMDatabaseBuilder(): RoomDatabase.Builder<DarcyIMDatabase> {
    // WebWorkerSQLiteDriver 将 SQLite 操作放在 Web Worker 中执行（OPFS 持久化）
    // sqlite-worker.js 使用浏览器原生 ESM Module Worker 加载 @sqlite.org/sqlite-wasm
    // 依赖文件由 webpack afterEmit hook 复制到输出目录
    val worker: Worker = js("new Worker('sqlite-worker.js', {type: 'module'})").unsafeCast<Worker>()
    // Worker 本身已在加载时启动 WASM 初始化，首次数据库操作时 ensureInited() 会等待初始化完成。
    return Room.databaseBuilder<DarcyIMDatabase>(
        name = "darcy_im_room.db",
    ).setDriver(WebWorkerSQLiteDriver(worker))
}