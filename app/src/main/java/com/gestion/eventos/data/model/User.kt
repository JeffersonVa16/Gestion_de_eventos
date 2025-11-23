package com.gestion.eventos.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

