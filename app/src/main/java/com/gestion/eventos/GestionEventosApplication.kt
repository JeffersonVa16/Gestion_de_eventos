package com.gestion.eventos

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GestionEventosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Verificar si Firebase ya está inicializado
            val apps = FirebaseApp.getApps(this)
            if (apps.isEmpty()) {
                // Firebase debería inicializarse automáticamente con google-services.json
                // Pero lo inicializamos manualmente como respaldo
                FirebaseApp.initializeApp(this)
                Log.d("Firebase", "Firebase inicializado manualmente")
            } else {
                Log.d("Firebase", "Firebase ya estaba inicializado")
            }
            
            // Verificar que Firebase Auth esté disponible
            val auth = FirebaseAuth.getInstance()
            Log.d("Firebase", "Firebase Auth disponible: ${auth.app.name}")
            
            // Verificar que Firestore esté disponible
            val db = FirebaseFirestore.getInstance()
            Log.d("Firebase", "Firestore disponible: ${db.app.name}")
            
        } catch (e: Exception) {
            Log.e("Firebase", "Error al inicializar Firebase: ${e.message}", e)
            e.printStackTrace()
        }
    }
}

