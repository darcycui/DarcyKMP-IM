package com.darcy.kmpdemo.storage.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.darcy.kmpdemo.platform.FilePlatform
import java.io.File

actual fun getIMDatabaseBuilder(): RoomDatabase.Builder<DarcyIMDatabase> {
    val dbFile = File(FilePlatform.getCacheDir().toString(), "darcy_im_room.db")
    return Room.databaseBuilder<DarcyIMDatabase>(
        name = dbFile.absolutePath,
    )
}