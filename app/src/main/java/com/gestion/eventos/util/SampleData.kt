package com.gestion.eventos.util

import com.gestion.eventos.data.model.Event
import java.util.UUID

object SampleData {
    private val currentTime = System.currentTimeMillis()

    val sampleUpcomingEvents: List<Event> = listOf(
        Event(
            id = UUID.randomUUID().toString(),
            title = "Feria de Emprendedores",
            description = "Encuentro local para apoyar a emprendedores con stands, charlas inspiradoras y networking.",
            date = currentTime + 86400000L,
            time = "10:00 AM",
            location = "Parque Central",
            organizerId = "demo_organizer",
            organizerName = "Comunidad Local",
            attendees = listOf("demo_user"),
            averageRating = 4.6,
            totalRatings = 32,
            imageUrl = null
        ),
        Event(
            id = UUID.randomUUID().toString(),
            title = "Limpieza de Playa",
            description = "Actividad comunitaria para limpiar la playa y sensibilizar sobre el cuidado del medio ambiente.",
            date = currentTime + 172800000L,
            time = "8:00 AM",
            location = "Playa Azul",
            organizerId = "demo_environment",
            organizerName = "Guardianes del Océano",
            attendees = listOf("demo_user", "demo_friend"),
            averageRating = 4.9,
            totalRatings = 18
        )
    )

    val samplePastEvents: List<Event> = listOf(
        Event(
            id = UUID.randomUUID().toString(),
            title = "Festival Gastronómico",
            description = "Degustación de platos típicos y música en vivo para recaudar fondos.",
            date = currentTime - 86400000L * 3,
            time = "5:00 PM",
            location = "Centro Cultural",
            organizerId = "demo_foodies",
            organizerName = "Sabores del Barrio",
            attendees = listOf("demo_user"),
            averageRating = 4.8,
            totalRatings = 45
        )
    )

    val sampleAllEvents: List<Event> = sampleUpcomingEvents + samplePastEvents
}

