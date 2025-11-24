package com.gestion.eventos.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val time: String = "",
    val location: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val attendees: List<String> = emptyList(),
    val imageUrl: String? = null,
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0
)

