package com.darcy.kmpdemo.ui.screen.phone.chat.usecase

import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.PrivateMessageEntity
import com.darcy.kmpdemo.ui.base.IUseCase

class SaveMessageToDBUseCase(
) : IUseCase<List<PrivateMessageEntity>, Boolean> {
    private val privateMessageDao = getDarcyIMDatabase().privateMessageDao()

    override suspend fun invoke(params: Map<String, String>, bean: List<PrivateMessageEntity>): Result<Boolean> {
        return try {
            bean.forEach { message ->
                if (message.msgId.isEmpty() || message.msgId.isBlank()) {
                    return Result.failure(Exception("msgId is empty or blank"))
                }
                val exists = privateMessageDao.getByMsgId(message.msgId)
                if (exists == null) {
                    privateMessageDao.insert(message)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}