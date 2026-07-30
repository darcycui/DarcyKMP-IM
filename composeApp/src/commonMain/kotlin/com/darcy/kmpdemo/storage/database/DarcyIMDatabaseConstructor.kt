package com.darcy.kmpdemo.storage.database

import androidx.room3.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object DarcyIMDatabaseConstructor : RoomDatabaseConstructor<DarcyIMDatabase> {
    override fun initialize(): DarcyIMDatabase
}