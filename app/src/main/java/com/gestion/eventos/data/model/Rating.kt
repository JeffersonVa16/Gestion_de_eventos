package com.gestion.eventos.data.model

data class Rating(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val rating: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

