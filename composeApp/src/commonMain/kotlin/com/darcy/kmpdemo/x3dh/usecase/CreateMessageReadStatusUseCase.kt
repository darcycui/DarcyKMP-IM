package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.bean.websocket.stomp.STOMPMessage
import com.darcy.kmpdemo.storage.database.daos.MessageReadStatusDao
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.MessageReadStatus
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.JsonHelper

class CreateMessageReadStatusUseCase : IUseCase<MessageReadStatus> {
    private val messageReadStatusDao: MessageReadStatusDao =
        getDarcyIMDatabase().messageReadStatusDao()

    override suspend fun invoke(params: Map<String, String>): Result<MessageReadStatus> {
        val stompMessageStr = params["stompMessage"]
            ?: return Result.failure(Exception("消息参数不能为空"))
        val stompMessage = JsonHelper.fromJson<STOMPMessage>(stompMessageStr)
            ?: return Result.failure(Exception("消息参数格式错误"))
        val messageReadStatus = MessageReadStatus(
            userId = stompMessage.senderId,
            targetId = stompMessage.receiverId,
            msgId = stompMessage.msgId,
            isRead = false,
        )
        val userId = messageReadStatus.userId
        messageReadStatusDao.findByUserIdAndMessageId(userId, messageReadStatus.msgId)?.apply {
            messageReadStatusDao.delete(this)
        }

        messageReadStatusDao.insert(messageReadStatus)
        return Result.success(messageReadStatus)
    }
}