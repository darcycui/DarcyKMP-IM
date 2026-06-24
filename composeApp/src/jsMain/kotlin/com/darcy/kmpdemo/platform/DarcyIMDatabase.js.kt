package com.darcy.kmpdemo.platform

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.darcy.kmpdemo.storage.database.DarcyIMDatabase
import org.w3c.dom.Worker

actual object RoomDatabasePlatform {
    actual fun getIMDatabaseBuilder(): RoomDatabase.Builder<DarcyIMDatabase> {
        val worker = Worker(js("""new URL("sql-js-worker/worker.js", import.meta.url)"""))
        return Room.databaseBuilder<DarcyIMDatabase>(
            name = "darcy_im_room.db",
        ).setDriver(WebWorkerSQLiteDriver(worker))
    }
}