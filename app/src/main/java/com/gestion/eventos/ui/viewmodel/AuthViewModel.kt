package com.gestion.eventos.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.eventos.data.model.User
import com.gestion.eventos.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = repository.currentUser
        _uiState.value = _uiState.value.copy(
            isAuthenticated = user != null
        )
        if (user != null) {
            loadUserData()
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = repository.signInWithEmail(email, password)
            result.fold(
                onSuccess = {
                    loadUserData()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        infoMessage = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al iniciar sesión",
                        infoMessage = null
                    )
                }
            )
        }
    }

    fun signUpWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            try {
                val result = repository.signUpWithEmail(email, password, name)
                result.fold(
                    onSuccess = { user ->
                        // Crear un usuario básico inmediatamente para que la navegación funcione
                        val basicUser = User(
                            id = user.uid,
                            email = user.email ?: email,
                            name = name,
                            photoUrl = user.photoUrl?.toString()
                        )
                        // Actualizar el estado de autenticación inmediatamente
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = basicUser,
                            infoMessage = "¡Registro exitoso!"
                        )
                        // Cargar los datos completos del usuario en segundo plano sin bloquear
                        launch {
                            loadUserData()
                        }
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Error al registrarse",
                            infoMessage = null
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error inesperado al registrarse",
                    infoMessage = null
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = repository.signInWithGoogle(idToken)
            result.fold(
                onSuccess = {
                    loadUserData()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        infoMessage = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al iniciar sesión con Google",
                        infoMessage = null
                    )
                }
            )
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userData = repository.getCurrentUserData()
            if (userData != null) {
                _uiState.value = _uiState.value.copy(currentUser = userData)
            } else {
                val firebaseUser = repository.currentUser
                if (firebaseUser != null) {
                    _uiState.value = _uiState.value.copy(
                        currentUser = User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            name = firebaseUser.displayName ?: "Usuario",
                            photoUrl = firebaseUser.photoUrl?.toString()
                        )
                    )
                }
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearInfoMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }

    fun updateProfile(name: String?, photoUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = repository.updateUserProfile(name, photoUri)
            result.fold(
                onSuccess = { updatedUser ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = updatedUser,
                        infoMessage = "Perfil actualizado correctamente"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "No se pudo actualizar el perfil"
                    )
                }
            )
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = repository.updatePassword(newPassword)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        infoMessage = "Contraseña actualizada correctamente"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "No se pudo cambiar la contraseña"
                    )
                }
            )
        }
    }
}

