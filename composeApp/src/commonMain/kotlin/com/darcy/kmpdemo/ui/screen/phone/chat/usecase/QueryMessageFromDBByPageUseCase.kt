package com.darcy.kmpdemo.ui.screen.phone.chat.usecase

import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.storage.database.daos.MessageReadStatusDao
import com.darcy.kmpdemo.storage.database.daos.PrivateMessageDao
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.ui.base.IUseCase

class QueryMessageFromDBByPageUseCase : IUseCase<Unit, List<PrivateMessageResponse>> {
    private val privateMessageDao: PrivateMessageDao = getDarcyIMDatabase().privateMessageDao()
    private val messageReadStatusDao: MessageReadStatusDao = getDarcyIMDatabase().messageReadStatusDao()
    override suspend fun invoke(
        params: Map<String, String>,
        bean: Unit
    ): Result<List<PrivateMessageResponse>> {
        val userId = params["userId"]?.toLongOrNull() ?: return Result.failure(Exception("userId is null"))
        val targetId = params["targetId"]?.toLongOrNull() ?: return Result.failure(Exception("targetId is null"))
        val conversationId = params["conversationId"]?.toLongOrNull() ?: return Result.failure(Exception("conversationId is null"))
        val page = params["page"]?.toIntOrNull() ?: return Result.failure(Exception("page is null"))
        val size = params["size"]?.toIntOrNull() ?: return Result.failure(Exception("size is null"))
        val offset = (page - 1) * size

        val entities = privateMessageDao.queryByUserIdAndTargetIdOrderByDesc(userId, targetId, size, offset)

        val responses = entities.reversed().map { entity ->
            PrivateMessageResponse(
                msgId = entity.msgId,
                senderId = entity.userId,
                receiverId = entity.targetId,
                content = entity.content,
                msgType = "TEXT",
                sendTime = entity.createdTime,
                isRead = if (entity.isSelfSend()) {
                    messageReadStatusDao.findByUserIdAndMessageId(userId, entity.msgId)?.isRead ?: false
                } else {
                    false
                },
                isRecalled = false
            )
        }
        return Result.success(responses)
    }
}