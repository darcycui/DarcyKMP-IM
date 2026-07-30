package com.darcy.kmpdemo.platform

import androidx.room3.RoomDatabase
import com.darcy.kmpdemo.storage.database.DarcyIMDatabase

expect object RoomDatabasePlatform {
    fun getIMDatabaseBuilder(): RoomDatabase.Builder<DarcyIMDatabase>
}