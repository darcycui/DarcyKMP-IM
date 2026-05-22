package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.bean.http.response.MessageReadStatusResponse
import com.darcy.kmpdemo.platform.TimePlatform
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.MessageReadStatus
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.storage.database.daos.MessageReadStatusDao

class MarkMessageReadStatusUseCase : IUseCase<List<MessageReadStatus>> {
    private val messageReadStatusDao: MessageReadStatusDao =
        getDarcyIMDatabase().messageReadStatusDao()

    override suspend fun invoke(params: Map<String, String>): Result<List<MessageReadStatus>> {
        val messageReadStatusResponseStr = params["messageReadStatusResponse"]
            ?: return Result.failure(Exception("消息参数不能为空"))
        val messageReadStatusResponse =
            JsonHelper.fromJson<MessageReadStatusResponse>(messageReadStatusResponseStr)
                ?: return Result.failure(Exception("消息参数格式错误"))
        val userId = messageReadStatusResponse.userId
        val msgIds = messageReadStatusResponse.msgIds
        msgIds.forEach { msgId ->
            messageReadStatusDao.findByUserIdAndMessageId(userId, msgId)?.apply {
                messageReadStatusDao.delete(this)
            }
        }
        val updatedCount = messageReadStatusDao.markMessagesAsRead(
            userId,
            msgIds,
            TimePlatform.getCurrentTimeStamp()
        )
        val readList = messageReadStatusDao.findByUserIdAndMessageIds(userId, msgIds)
        return Result.success(readList)
    }
}