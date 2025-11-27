package com.gestion.eventos.data.repository

import com.gestion.eventos.data.model.Event
import com.gestion.eventos.data.model.Comment
import com.gestion.eventos.data.model.Rating
import com.gestion.eventos.util.SampleData
import java.util.UUID

/**
 * Repositorio local simple que almacena datos en memoria.
 * No requiere Firestore ni ninguna base de datos externa.
 */
class LocalEventRepository {
    // Almacenamiento en memoria
    private val events = mutableListOf<Event>()
    private val comments = mutableListOf<Comment>()
    private val ratings = mutableListOf<Rating>()
    
    init {
        // Inicializar con datos de muestra
        events.addAll(SampleData.sampleAllEvents)
    }

    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val eventId = UUID.randomUUID().toString()
            val eventWithId = event.copy(id = eventId)
            events.add(eventWithId)
            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEvents(): List<Event> {
        return events.sortedBy { it.date }
    }

    suspend fun getUpcomingEvents(): List<Event> {
        val currentTime = System.currentTimeMillis()
        return events.filter { it.date > currentTime }.sortedBy { it.date }
    }

    suspend fun getPastEvents(): List<Event> {
        val currentTime = System.currentTimeMillis()
        return events.filter { it.date < currentTime }.sortedByDescending { it.date }
    }

    suspend fun getEventById(eventId: String): Event? {
        return events.find { it.id == eventId }
    }

    suspend fun joinEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventIndex = events.indexOfFirst { it.id == eventId }
            if (eventIndex == -1) {
                return Result.failure(Exception("Este evento no existe"))
            }
            
            val event = events[eventIndex]
            val updatedAttendees = if (userId !in event.attendees) {
                event.attendees + userId
            } else {
                event.attendees
            }
            
            events[eventIndex] = event.copy(attendees = updatedAttendees)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventIndex = events.indexOfFirst { it.id == eventId }
            if (eventIndex == -1) {
                return Result.failure(Exception("Este evento no existe"))
            }
            
            val event = events[eventIndex]
            val updatedAttendees = event.attendees.filter { it != userId }
            events[eventIndex] = event.copy(attendees = updatedAttendees)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(comment: Comment): Result<String> {
        return try {
            val commentId = UUID.randomUUID().toString()
            val commentWithId = comment.copy(
                id = commentId,
                createdAt = System.currentTimeMillis()
            )
            comments.add(commentWithId)
            Result.success(commentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComments(eventId: String): List<Comment> {
        return comments
            .filter { it.eventId == eventId }
            .sortedByDescending { it.createdAt }
    }

    suspend fun addRating(rating: Rating): Result<String> {
        return try {
            // Eliminar calificación anterior del mismo usuario si existe
            ratings.removeAll { it.eventId == rating.eventId && it.userId == rating.userId }
            
            val ratingId = UUID.randomUUID().toString()
            val ratingWithId = rating.copy(id = ratingId)
            ratings.add(ratingWithId)
            
            // Actualizar promedio de calificaciones del evento
            updateEventRating(rating.eventId)
            
            Result.success(ratingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRating(eventId: String, userId: String): Rating? {
        return ratings.firstOrNull { it.eventId == eventId && it.userId == userId }
    }

    private suspend fun updateEventRating(eventId: String) {
        try {
            val eventRatings = ratings.filter { it.eventId == eventId }
            if (eventRatings.isNotEmpty()) {
                val average = eventRatings.map { it.rating }.average()
                val totalRatings = eventRatings.size
                
                val eventIndex = events.indexOfFirst { it.id == eventId }
                if (eventIndex != -1) {
                    val event = events[eventIndex]
                    events[eventIndex] = event.copy(
                        averageRating = average,
                        totalRatings = totalRatings
                    )
                }
            }
        } catch (e: Exception) {
            // Error al actualizar, pero no crítico
        }
    }

    suspend fun uploadEventImage(imageUri: android.net.Uri, eventId: String): Result<String> {
        // Para una versión local simple, no subimos imágenes reales
        // Podríamos usar la URI local directamente si es necesario
        return Result.failure(Exception("Subida de imágenes no disponible en modo local"))
    }

    suspend fun updateEventImage(eventId: String, imageUrl: String) {
        try {
            val eventIndex = events.indexOfFirst { it.id == eventId }
            if (eventIndex != -1) {
                val event = events[eventIndex]
                events[eventIndex] = event.copy(imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            // Ignorar: la imagen es opcional
        }
    }

    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            val eventIndex = events.indexOfFirst { it.id == event.id }
            if (eventIndex == -1) {
                return Result.failure(Exception("Evento no encontrado"))
            }
            
            events[eventIndex] = event
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

