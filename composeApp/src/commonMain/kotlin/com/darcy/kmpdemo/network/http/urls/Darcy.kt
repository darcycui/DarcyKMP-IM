package com.darcy.kmpdemo.network.http.urls

import com.darcy.kmpdemo.network.http.urls.Hosts.DOMAIN

object Darcy {
//    const val HOME_URL = "https://$DOMAIN"
    const val HOME_URL = "http://$DOMAIN"

    const val LOGIN_URL = "$HOME_URL/api/login"
    const val REGISTER_URL = "$HOME_URL/api/register"
    const val SEARCH_FRIEND_URL = "$HOME_URL/api/users/query/phone"
    const val APPLY_FRIEND_URL = "$HOME_URL/api/friend-requests/create"
    const val QUERY_FRIEND_FROM_URL = "$HOME_URL/api/friend-requests/query/from"
    const val QUERY_FRIEND_TO_URL = "$HOME_URL/api/friend-requests/query/to"
    const val ACCEPT_FRIEND_URL = "$HOME_URL/api/friend-requests/accept"
    const val QUERY_FRIENDSHIP_LIST_URL = "$HOME_URL/api/friendships/query/all"
    const val QUERY_CONVERSATION_LIST_URL = "$HOME_URL/api/conversations/query/all"
    const val CREATE_CONVERSATION_URL = "$HOME_URL/api/conversations/create"

    const val QUERY_PRIVATE_MESSAGE_URL = "$HOME_URL/api/private-messages/query/page"
    const val PUSH_X3DH_KEYS_URL = "$HOME_URL/api/x3dh/push/keys"
    const val PULL_X3DH_KEYS_URL = "$HOME_URL/api/x3dh/pull/keys"
    const val PUSH_ALICE_HELLO_MESSAGE_URL = "$HOME_URL/api/x3dh/push/alice/hello"
    const val PULL_ALICE_HELLO_MESSAGE_URL = "$HOME_URL/api/x3dh/pull/alice/hello"

    const val RECEIVER_PUSH_MESSAGE_READ_STATUS_URL = "$HOME_URL/api/message/read/receiver/push/read"
    const val RECEIVER_PULL_MESSAGE_READ_STATUS_URL = "$HOME_URL/api/message/read/receiver/pull/unread"
    const val SENDER_SYNC_MESSAGE_READ_STATUS_URL = "$HOME_URL/api/message/read/sender/sync/read"
}