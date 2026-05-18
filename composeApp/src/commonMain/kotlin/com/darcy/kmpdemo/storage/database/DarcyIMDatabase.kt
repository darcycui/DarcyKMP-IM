package com.darcy.kmpdemo.storage.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.darcy.kmpdemo.storage.database.daos.ConversationDao
import com.darcy.kmpdemo.storage.database.daos.ConversationUserCrossRefDao
import com.darcy.kmpdemo.storage.database.daos.FriendshipUserCrossRefDao
import com.darcy.kmpdemo.storage.database.daos.FriendshipUserDao
import com.darcy.kmpdemo.storage.database.daos.IdentityKeyDao
import com.darcy.kmpdemo.storage.database.daos.OneTimePreKeyDao
import com.darcy.kmpdemo.storage.database.daos.SessionRecordDao
import com.darcy.kmpdemo.storage.database.daos.SignedPreKeyDao
import com.darcy.kmpdemo.storage.database.daos.UserDao
import com.darcy.kmpdemo.storage.database.tables.ConversationEntity
import com.darcy.kmpdemo.storage.database.tables.ConversationUserCrossRef
import com.darcy.kmpdemo.storage.database.tables.FriendshipEntity
import com.darcy.kmpdemo.storage.database.tables.FriendshipUserCrossRef
import com.darcy.kmpdemo.storage.database.tables.IdentityKeyEntity
import com.darcy.kmpdemo.storage.database.tables.OneTimePreKeyEntity
import com.darcy.kmpdemo.storage.database.tables.SessionRecordEntity
import com.darcy.kmpdemo.storage.database.tables.SignedPreKeyEntity
import com.darcy.kmpdemo.storage.database.tables.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        // 添加数据库表
        UserEntity::class,
        ConversationEntity::class,
        ConversationUserCrossRef::class,
        FriendshipEntity::class,
        FriendshipUserCrossRef::class,
        IdentityKeyEntity::class,
        SignedPreKeyEntity::class,
        OneTimePreKeyEntity::class,
        SessionRecordEntity::class
    ],
    version = 3,
    exportSchema = true
)

//@ConstructedBy(DarcyIMDatabaseConstructor::class)
abstract class DarcyIMDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun conversationUserCrossRefDao(): ConversationUserCrossRefDao
    abstract fun friendshipDao(): FriendshipUserDao
    abstract fun friendshipUserCrossRefDao(): FriendshipUserCrossRefDao
    abstract fun identityKeyDao(): IdentityKeyDao
    abstract fun signedPreKeyDao(): SignedPreKeyDao
    abstract fun oneTimePreKeyDao(): OneTimePreKeyDao
    abstract fun sessionRecordDao(): SessionRecordDao
}

fun getDarcyIMDatabase(): DarcyIMDatabase {
    return getIMDatabaseBuilder()
        .setDriver(BundledSQLiteDriver()) // 使用内置的SQLite
        .setQueryCoroutineContext(Dispatchers.IO) // 协程上下文
        .addMigrations()
        .fallbackToDestructiveMigration(false)
        .build()
}

expect fun getIMDatabaseBuilder(): RoomDatabase.Builder<DarcyIMDatabase>