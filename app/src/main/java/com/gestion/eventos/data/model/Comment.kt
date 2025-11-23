package com.gestion.eventos.data.model

data class Comment(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

