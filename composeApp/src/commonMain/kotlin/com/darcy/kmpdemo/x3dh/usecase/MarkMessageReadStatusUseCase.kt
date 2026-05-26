package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.bean.http.response.MessageReadStatusResponse
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.platform.TimePlatform
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.MessageReadStatusEntity
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.storage.database.daos.MessageReadStatusDao

class MarkMessageReadStatusUseCase : IUseCase<Unit, List<MessageReadStatusEntity>> {
    private val messageReadStatusDao: MessageReadStatusDao =
        getDarcyIMDatabase().messageReadStatusDao()

    override suspend fun invoke(
        params: Map<String, String>,
        bean: Unit
    ): Result<List<MessageReadStatusEntity>> {
        val messageReadStatusResponseStr = params["messageReadStatusResponse"]
            ?: return Result.failure(Exception("消息参数不能为空"))
        val messageReadStatusResponse =
            JsonHelper.fromJson<MessageReadStatusResponse>(messageReadStatusResponseStr)
                ?: return Result.failure(Exception("消息参数格式错误"))
        val userId = messageReadStatusResponse.userId
        val msgIds = messageReadStatusResponse.msgIds
        msgIds.forEach { msgId ->
            messageReadStatusDao.findByUserIdAndMessageId(userId, msgId)?.apply {
                val updatedCount = messageReadStatusDao.markMessageAsRead(
                    userId,
                    msgId,
                    TimePlatform.getCurrentTimeStamp()
                )
            } ?: messageReadStatusDao.insert(
                MessageReadStatusEntity(
                    userId = userId,
                    msgId = msgId,
                    isRead = true,
                    messageType = 1,
                    readTime = TimePlatform.getCurrentTimeStamp()
                )
            )
        }
        val readList = messageReadStatusDao.findByUserIdAndMessageIdList(userId, msgIds)
        logE("更新了${readList.size} 条消息为已读")
        return Result.success(readList)
    }
}