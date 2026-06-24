package com.darcy.kmpdemo.platform

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.darcy.kmpdemo.storage.database.DarcyIMDatabase
import org.w3c.dom.Worker

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsWorker(): Worker =
    js("""new Worker(new URL("sql-js-worker/worker.js", import.meta.url))""")

actual object RoomDatabasePlatform {
    actual fun getIMDatabaseBuilder(): RoomDatabase.Builder<DarcyIMDatabase> {
        return Room.databaseBuilder<DarcyIMDatabase>(
            name = "darcy_im_room.db",
        ).setDriver(WebWorkerSQLiteDriver(jsWorker()))
    }
}