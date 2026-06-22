package com.darcy.kmpdemo.storage.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

actual fun getIMDatabaseBuilder(): RoomDatabase.Builder<DarcyIMDatabase> {
    return Room.databaseBuilder<DarcyIMDatabase>(
        name = "darcy_im_room.db",
    ).setDriver(WebWorkerSQLiteDriver(Worker("DarcyIMDatabase."))) // 使用web的SQLite
}