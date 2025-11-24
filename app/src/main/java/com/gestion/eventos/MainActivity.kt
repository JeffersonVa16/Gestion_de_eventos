package com.gestion.eventos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gestion.eventos.ui.navigation.NavGraph
import com.gestion.eventos.ui.screens.GoogleSignInHelper
import com.gestion.eventos.ui.theme.GestionDeEventosTheme
import com.gestion.eventos.ui.viewmodel.AuthViewModel
import com.gestion.eventos.ui.viewmodel.EventViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var eventViewModel: EventViewModel
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                authViewModel.signInWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            // Error al iniciar sesión con Google
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        authViewModel = AuthViewModel()
        eventViewModel = EventViewModel()
        
        setContent {
            GestionDeEventosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        authViewModel = authViewModel,
                        eventViewModel = eventViewModel,
                        onGoogleSignInClick = {
                            val signInClient = GoogleSignInHelper.getGoogleSignInClient(this)
                            val signInIntent = signInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        }
                    )
                }
            }
        }
    }
}