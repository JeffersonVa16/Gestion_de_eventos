package com.gestion.eventos.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.eventos.data.model.Event
import com.gestion.eventos.data.model.Comment
import com.gestion.eventos.data.model.Rating
import com.gestion.eventos.data.repository.EventRepository
import com.gestion.eventos.util.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventUiState(
    val events: List<Event> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val pastEvents: List<Event> = emptyList(),
    val currentEvent: Event? = null,
    val comments: List<Comment> = emptyList(),
    val userRating: Rating? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EventViewModel : ViewModel() {
    private val repository = EventRepository()

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private fun mergeEvents(firestoreEvents: List<Event>, sampleEvents: List<Event>): List<Event> {
        // Combinar eventos de Firestore con eventos de muestra
        // Si hay eventos en Firestore, los usamos; si no, usamos los de muestra
        val firestoreEventIds = firestoreEvents.map { it.id }.toSet()
        val uniqueSampleEvents = sampleEvents.filter { it.id !in firestoreEventIds }
        return (firestoreEvents + uniqueSampleEvents).sortedBy { it.date }
    }

    fun loadEvents() {
        viewModelScope.launch {
            val currentEvents = _uiState.value.events
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val events = repository.getEvents()
                _uiState.value = _uiState.value.copy(
                    events = if (events.isEmpty() && currentEvents.isEmpty()) {
                        SampleData.sampleAllEvents
                    } else if (events.isEmpty() && currentEvents.isNotEmpty()) {
                        // Si Firestore está vacío pero tenemos eventos en el estado, mantenerlos
                        currentEvents
                    } else {
                        mergeEvents(events, SampleData.sampleAllEvents)
                    },
                    isLoading = false
                )
            } catch (e: Exception) {
                // Si hay error, mantener los eventos actuales
                _uiState.value = _uiState.value.copy(
                    events = currentEvents.ifEmpty { SampleData.sampleAllEvents },
                    isLoading = false
                )
            }
        }
    }

    fun loadUpcomingEvents() {
        viewModelScope.launch {
            val currentUpcoming = _uiState.value.upcomingEvents
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val events = repository.getUpcomingEvents()
                _uiState.value = _uiState.value.copy(
                    upcomingEvents = if (events.isEmpty() && currentUpcoming.isEmpty()) {
                        SampleData.sampleUpcomingEvents
                    } else if (events.isEmpty() && currentUpcoming.isNotEmpty()) {
                        // Si Firestore está vacío pero tenemos eventos en el estado, mantenerlos
                        currentUpcoming
                    } else {
                        mergeEvents(events, SampleData.sampleUpcomingEvents)
                    },
                    isLoading = false
                )
            } catch (e: Exception) {
                // Si hay error, mantener los eventos actuales
                _uiState.value = _uiState.value.copy(
                    upcomingEvents = currentUpcoming.ifEmpty { SampleData.sampleUpcomingEvents },
                    isLoading = false
                )
            }
        }
    }

    fun loadPastEvents() {
        viewModelScope.launch {
            val currentPast = _uiState.value.pastEvents
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val events = repository.getPastEvents()
                _uiState.value = _uiState.value.copy(
                    pastEvents = if (events.isEmpty() && currentPast.isEmpty()) {
                        SampleData.samplePastEvents
                    } else if (events.isEmpty() && currentPast.isNotEmpty()) {
                        // Si Firestore está vacío pero tenemos eventos en el estado, mantenerlos
                        currentPast
                    } else {
                        mergeEvents(events, SampleData.samplePastEvents)
                    },
                    isLoading = false
                )
            } catch (e: Exception) {
                // Si hay error, mantener los eventos actuales
                _uiState.value = _uiState.value.copy(
                    pastEvents = currentPast.ifEmpty { SampleData.samplePastEvents },
                    isLoading = false
                )
            }
        }
    }

    fun loadEventDetails(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Cargar comentarios siempre, incluso si el evento no se encuentra
                loadComments(eventId)
                
                val event = repository.getEventById(eventId)
                if (event != null) {
                    _uiState.value = _uiState.value.copy(
                        currentEvent = event,
                        isLoading = false
                    )
                } else {
                    // Si no se encuentra el evento, buscar en los eventos de muestra
                    val sampleEvent = SampleData.sampleAllEvents.find { it.id == eventId }
                    _uiState.value = _uiState.value.copy(
                        currentEvent = sampleEvent,
                        isLoading = false,
                        errorMessage = if (sampleEvent == null) "Evento no encontrado" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar el evento: ${e.message}"
                )
            }
        }
    }

    fun createEvent(event: Event, userId: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val eventWithOrganizer = event.copy(organizerId = userId)
            val result = repository.createEvent(eventWithOrganizer)
            result.fold(
                onSuccess = { eventId ->
                    if (imageUri != null) {
                        val uploadResult = repository.uploadEventImage(imageUri, eventId)
                        uploadResult.fold(
                            onSuccess = { url ->
                                repository.updateEventImage(eventId, url)
                                loadEvents()
                                loadUpcomingEvents()
                                loadPastEvents()
                                _uiState.value = _uiState.value.copy(isLoading = false)
                            },
                            onFailure = { e ->
                                // Si falla la subida de imagen, el evento ya está creado
                                // Solo mostramos un mensaje pero no fallamos
                                loadEvents()
                                loadUpcomingEvents()
                                loadPastEvents()
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Evento creado pero la imagen no se pudo subir: ${e.message}"
                                )
                            }
                        )
                    } else {
                        loadEvents()
                        loadUpcomingEvents()
                        loadPastEvents()
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al crear evento"
                    )
                }
            )
        }
    }

    fun joinEvent(eventId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            
            // Actualizar optimistamente el evento actual si está cargado
            val currentEvent = _uiState.value.currentEvent
            if (currentEvent != null && currentEvent.id == eventId) {
                val updatedAttendees = if (userId !in currentEvent.attendees) {
                    currentEvent.attendees + userId
                } else {
                    currentEvent.attendees
                }
                _uiState.value = _uiState.value.copy(
                    currentEvent = currentEvent.copy(attendees = updatedAttendees)
                )
            }
            
            val result = repository.joinEvent(eventId, userId)
            result.fold(
                onSuccess = {
                    // Recargar para asegurar sincronización
                    loadEventDetails(eventId)
                    loadEvents()
                },
                onFailure = { e ->
                    // Revertir el cambio optimista si falla
                    val errorMsg = e.message ?: "Error al unirse al evento"
                    if (currentEvent != null && currentEvent.id == eventId) {
                        _uiState.value = _uiState.value.copy(
                            currentEvent = currentEvent,
                            errorMessage = errorMsg
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = errorMsg
                        )
                    }
                }
            )
        }
    }

    fun leaveEvent(eventId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            
            // Actualizar optimistamente el evento actual si está cargado
            val currentEvent = _uiState.value.currentEvent
            if (currentEvent != null && currentEvent.id == eventId) {
                val updatedAttendees = currentEvent.attendees.filter { it != userId }
                _uiState.value = _uiState.value.copy(
                    currentEvent = currentEvent.copy(attendees = updatedAttendees)
                )
            }
            
            val result = repository.leaveEvent(eventId, userId)
            result.fold(
                onSuccess = {
                    // Recargar para asegurar sincronización
                    loadEventDetails(eventId)
                    loadEvents()
                },
                onFailure = { e ->
                    // Revertir el cambio optimista si falla
                    if (currentEvent != null && currentEvent.id == eventId) {
                        _uiState.value = _uiState.value.copy(
                            currentEvent = currentEvent,
                            errorMessage = e.message ?: "Error al salir del evento"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = e.message ?: "Error al salir del evento"
                        )
                    }
                }
            )
        }
    }

    fun addComment(comment: Comment) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            
            // Agregar el comentario optimistamente a la UI
            val currentComments = _uiState.value.comments
            val optimisticComment = comment.copy(
                id = "temp_${System.currentTimeMillis()}",
                createdAt = System.currentTimeMillis()
            )
            _uiState.value = _uiState.value.copy(
                comments = listOf(optimisticComment) + currentComments
            )
            
            val result = repository.addComment(comment)
            result.fold(
                onSuccess = { commentId ->
                    // Recargar comentarios para obtener el ID real y asegurar sincronización
                    loadComments(comment.eventId)
                },
                onFailure = { e ->
                    // Revertir el comentario optimista si falla
                    _uiState.value = _uiState.value.copy(
                        comments = currentComments,
                        errorMessage = e.message ?: "Error al agregar comentario"
                    )
                }
            )
        }
    }

    fun loadComments(eventId: String) {
        viewModelScope.launch {
            val comments = repository.getComments(eventId)
            _uiState.value = _uiState.value.copy(comments = comments)
        }
    }

    fun addRating(rating: Rating) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            
            // Actualizar optimistamente la valoración del usuario
            _uiState.value = _uiState.value.copy(userRating = rating)
            
            val result = repository.addRating(rating)
            result.fold(
                onSuccess = { ratingId ->
                    // Recargar detalles del evento para actualizar el promedio de calificaciones
                    loadEventDetails(rating.eventId)
                    loadUserRating(rating.eventId, rating.userId)
                },
                onFailure = { e ->
                    // Revertir la valoración optimista si falla
                    _uiState.value = _uiState.value.copy(
                        userRating = null,
                        errorMessage = e.message ?: "Error al calificar evento"
                    )
                }
            )
        }
    }

    fun loadUserRating(eventId: String, userId: String) {
        viewModelScope.launch {
            val rating = repository.getUserRating(eventId, userId)
            _uiState.value = _uiState.value.copy(userRating = rating)
        }
    }

    fun updateEvent(event: Event, imageUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.updateEvent(event)
            result.fold(
                onSuccess = {
                    if (imageUri != null) {
                        val uploadResult = repository.uploadEventImage(imageUri, event.id)
                        uploadResult.fold(
                            onSuccess = { url ->
                                repository.updateEventImage(event.id, url)
                                loadEvents()
                                loadUpcomingEvents()
                                loadPastEvents()
                                loadEventDetails(event.id)
                                _uiState.value = _uiState.value.copy(isLoading = false)
                            },
                            onFailure = { e ->
                                loadEvents()
                                loadUpcomingEvents()
                                loadPastEvents()
                                loadEventDetails(event.id)
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Evento actualizado pero la imagen no se pudo subir: ${e.message}"
                                )
                            }
                        )
                    } else {
                        loadEvents()
                        loadUpcomingEvents()
                        loadPastEvents()
                        loadEventDetails(event.id)
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al actualizar evento"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

