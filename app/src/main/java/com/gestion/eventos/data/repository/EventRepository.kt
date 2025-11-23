package com.gestion.eventos.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.gestion.eventos.data.model.Event
import com.gestion.eventos.data.model.Comment
import com.gestion.eventos.data.model.Rating
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EventRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val eventId = UUID.randomUUID().toString()
            val eventWithId = event.copy(id = eventId)
            db.collection("events").document(eventId).set(eventWithId).await()
            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEvents(): List<Event> {
        return try {
            val snapshot = db.collection("events")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUpcomingEvents(): List<Event> {
        return try {
            val currentTime = System.currentTimeMillis()
            val snapshot = db.collection("events")
                .whereGreaterThan("date", currentTime)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPastEvents(): List<Event> {
        return try {
            val currentTime = System.currentTimeMillis()
            val snapshot = db.collection("events")
                .whereLessThan("date", currentTime)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = db.collection("events").document(eventId).get().await()
            doc.toObject(Event::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun joinEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventRef = db.collection("events").document(eventId)
            db.runTransaction { transaction ->
                val event = transaction.get(eventRef).toObject(Event::class.java)
                    ?: throw Exception("Evento no encontrado")
                val updatedAttendees = if (userId in event.attendees) {
                    event.attendees
                } else {
                    event.attendees + userId
                }
                transaction.update(eventRef, "attendees", updatedAttendees)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> 
                    "Firestore API no está habilitada. Por favor, habilítala en la consola de Firebase."
                e.message?.contains("API has not been used", ignoreCase = true) == true ->
                    "Firestore API no está habilitada. Visita: https://console.developers.google.com/apis/api/firestore.googleapis.com/overview?project=gestion-eventos-51969"
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ||
                e.message?.contains("does not exist", ignoreCase = true) == true ->
                    "La base de datos Firestore no está configurada. Visita: https://console.cloud.google.com/datastore/setup?project=gestion-eventos-51969 para crear la base de datos."
                e.message?.contains("no encontrado", ignoreCase = true) == true ->
                    "Este evento no existe en la base de datos. Solo puedes unirte a eventos creados en la aplicación."
                else -> e.message ?: "Error al unirse al evento"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun leaveEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventRef = db.collection("events").document(eventId)
            db.runTransaction { transaction ->
                val event = transaction.get(eventRef).toObject(Event::class.java)
                    ?: throw Exception("Evento no encontrado")
                val updatedAttendees = event.attendees.filter { it != userId }
                transaction.update(eventRef, "attendees", updatedAttendees)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(comment: Comment): Result<String> {
        return try {
            val commentId = UUID.randomUUID().toString()
            val commentWithId = comment.copy(id = commentId)
            db.collection("comments").document(commentId).set(commentWithId).await()
            Result.success(commentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComments(eventId: String): List<Comment> {
        return try {
            val snapshot = db.collection("comments")
                .whereEqualTo("eventId", eventId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addRating(rating: Rating): Result<String> {
        return try {
            val ratingId = UUID.randomUUID().toString()
            val ratingWithId = rating.copy(id = ratingId)
            db.collection("ratings").document(ratingId).set(ratingWithId).await()
            
            // Actualizar promedio de calificaciones del evento
            updateEventRating(rating.eventId)
            
            Result.success(ratingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRating(eventId: String, userId: String): Rating? {
        return try {
            val snapshot = db.collection("ratings")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject(Rating::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun updateEventRating(eventId: String) {
        try {
            val ratings = db.collection("ratings")
                .whereEqualTo("eventId", eventId)
                .get()
                .await()
            
            val ratingsList = ratings.documents.mapNotNull { it.toObject(Rating::class.java) }
            if (ratingsList.isNotEmpty()) {
                val average = ratingsList.map { it.rating }.average()
                db.collection("events").document(eventId)
                    .update("averageRating", average, "totalRatings", ratingsList.size)
                    .await()
            }
        } catch (e: Exception) {
            // Error al actualizar, pero no crítico
        }
    }

    suspend fun uploadEventImage(imageUri: android.net.Uri, eventId: String): Result<String> {
        return try {
            val storageRef = storage.reference.child("event_images/$eventId.jpg")
            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEventImage(eventId: String, imageUrl: String) {
        try {
            db.collection("events").document(eventId)
                .update("imageUrl", imageUrl)
                .await()
        } catch (_: Exception) {
            // Ignorar: la imagen es opcional
        }
    }

    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            val updates = mapOf(
                "title" to event.title,
                "description" to event.description,
                "date" to event.date,
                "time" to event.time,
                "location" to event.location,
                "organizerName" to event.organizerName
            )
            db.collection("events").document(event.id)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

