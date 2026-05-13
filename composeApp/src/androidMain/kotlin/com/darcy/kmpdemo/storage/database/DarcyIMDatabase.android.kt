package com.darcy.kmpdemo.storage.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.darcy.kmpdemo.app.AppContextProvider

actual fun getIMDatabaseBuilder(): RoomDatabase.Builder<DarcyIMDatabase> {
    val appContext = AppContextProvider.getAppContext().applicationContext
    val dbFile = appContext.getDatabasePath("darcy_im_room.db")
    return Room.databaseBuilder<DarcyIMDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}