package com.gestion.eventos.data.repository

import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gestion.eventos.data.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            // Crear usuario en Firebase Auth primero
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            
            // Actualizar el displayName en Firebase Auth inmediatamente (rápido)
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                // Si falla, continuar de todas formas
                println("Error al actualizar perfil en Auth: ${e.message}")
            }
            
            // Intentar crear perfil en Firestore, pero no bloquear si falla
            // El usuario ya está autenticado, así que podemos continuar
            try {
                val userData = User(
                    id = user.uid,
                    email = email,
                    name = name
                )
                // Usar timeout corto para no bloquear demasiado tiempo
                kotlinx.coroutines.withTimeout(3000) {
                    db.collection("users").document(user.uid).set(userData).await()
                }
            } catch (firestoreError: Exception) {
                // Log del error pero no fallar el registro
                // El usuario puede usar la app aunque Firestore falle
                println("Error al crear perfil en Firestore: ${firestoreError.message}")
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user!!
            
            // Verificar si el usuario ya existe en Firestore
            val userDoc = db.collection("users").document(user.uid).get().await()
            if (!userDoc.exists()) {
                // Crear perfil de usuario en Firestore
                val userData = User(
                    id = user.uid,
                    email = user.email ?: "",
                    name = user.displayName ?: "",
                    photoUrl = user.photoUrl?.toString()
                )
                db.collection("users").document(user.uid).set(userData).await()
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserData(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun updateUserProfile(name: String?, photoUri: Uri?): Result<User> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Usuario no autenticado"))
        return try {
            var photoUrl: String? = null

            if (photoUri != null) {
                val storageRef = storage.reference.child("profile_images/${user.uid}.jpg")
                storageRef.putFile(photoUri).await()
                photoUrl = storageRef.downloadUrl.await().toString()
            }

            // Actualizar Firebase Auth profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .apply {
                    if (!name.isNullOrBlank()) {
                        displayName = name
                    }
                    if (!photoUrl.isNullOrBlank()) {
                        setPhotoUri(photoUrl.toUri())
                    }
                }
                .build()

            user.updateProfile(profileUpdates).await()

            // Verificar si el documento existe en Firestore
            val userDocRef = db.collection("users").document(user.uid)
            val userDoc = userDocRef.get().await()
            
            if (userDoc.exists()) {
                // Si existe, actualizar - siempre actualizar el nombre si se proporciona
                val updates = mutableMapOf<String, Any>()
                if (name != null) {
                    updates["name"] = name
                }
                if (!photoUrl.isNullOrBlank()) {
                    updates["photoUrl"] = photoUrl
                }
                if (updates.isNotEmpty()) {
                    userDocRef.update(updates).await()
                }
            } else {
                // Si no existe, crear el documento completo
                val userData = User(
                    id = user.uid,
                    email = user.email ?: "",
                    name = name ?: user.displayName ?: "",
                    photoUrl = photoUrl ?: user.photoUrl?.toString()
                )
                userDocRef.set(userData).await()
            }

            // Obtener el usuario actualizado de Firestore
            val updatedUserDoc = db.collection("users").document(user.uid).get().await()
            val updatedUser = if (updatedUserDoc.exists()) {
                updatedUserDoc.toObject(User::class.java) ?: User(
                    id = user.uid,
                    email = user.email ?: "",
                    name = name ?: user.displayName ?: "",
                    photoUrl = photoUrl ?: user.photoUrl?.toString()
                )
            } else {
                // Si no existe en Firestore, crear uno con los datos actuales
                User(
                    id = user.uid,
                    email = user.email ?: "",
                    name = name ?: user.displayName ?: "",
                    photoUrl = photoUrl ?: user.photoUrl?.toString()
                )
            }

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Usuario no autenticado"))
        return try {
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

