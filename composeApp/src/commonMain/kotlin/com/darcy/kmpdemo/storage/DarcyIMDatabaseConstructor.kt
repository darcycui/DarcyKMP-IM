package com.darcy.kmpdemo.storage

import androidx.room3.RoomDatabaseConstructor
import com.darcy.kmpdemo.storage.database.DarcyIMDatabase

@Suppress("KotlinNoActualForExpect")
expect object DarcyIMDatabaseConstructor : RoomDatabaseConstructor<DarcyIMDatabase> {
    override fun initialize(): DarcyIMDatabase
}