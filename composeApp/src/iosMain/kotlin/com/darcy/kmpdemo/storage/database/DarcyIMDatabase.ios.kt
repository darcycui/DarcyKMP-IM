package com.darcy.kmpdemo.storage.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun getIMDatabaseBuilder(): RoomDatabase.Builder<DarcyIMDatabase> {
    val dbFilePath = documentDirectory() + "/darcy_im_room.db"
    return Room.databaseBuilder<DarcyIMDatabase>(
        name = dbFilePath,
    ).setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver()) // 使用内置的SQLite
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}