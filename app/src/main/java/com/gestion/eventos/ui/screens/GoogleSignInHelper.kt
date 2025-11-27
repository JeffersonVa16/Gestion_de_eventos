package com.gestion.eventos.ui.screens

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

object GoogleSignInHelper {
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("287714325996-scufgn441d08dqdkdaskkvuuoppe0l9k.apps.googleusercontent.com")
            .requestEmail()
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
}

