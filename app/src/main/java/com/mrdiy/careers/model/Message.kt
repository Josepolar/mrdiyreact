package com.mrdiy.careers.model

data class Message(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val lastMessage: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val unreadCount: Int = 0,
    val jobId: String? = null
)