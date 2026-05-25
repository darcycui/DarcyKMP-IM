package com.darcy.kmpdemo.ui.screen.phone.chat.usecase

import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.PrivateMessageEntity
import com.darcy.kmpdemo.ui.base.IUseCase

class SaveOfflineToDBMessageUseCase(
) : IUseCase<List<PrivateMessageEntity>, Unit> {
    private val privateMessageDao = getDarcyIMDatabase().privateMessageDao()

    override suspend fun invoke(params: Map<String, String>, bean: List<PrivateMessageEntity>): Result<Unit> {
        return try {
            bean.forEach { message ->
                val exists = privateMessageDao.getByMsgId(message.msgId)
                if (exists == null) {
                    privateMessageDao.insert(message)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}