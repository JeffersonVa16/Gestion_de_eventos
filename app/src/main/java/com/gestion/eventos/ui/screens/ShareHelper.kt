package com.gestion.eventos.ui.screens

import android.content.Context
import android.content.Intent
import com.gestion.eventos.data.model.Event
import java.text.SimpleDateFormat
import java.util.*

object ShareHelper {
    fun shareEvent(context: Context, event: Event) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date(event.date))
        
        val shareText = """
            📅 ${event.title}
            
            ${event.description}
            
            📍 Ubicación: ${event.location}
            🕐 Fecha: $dateStr a las ${event.time}
            👤 Organizador: ${event.organizerName}
            
            ¡No te lo pierdas!
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, event.title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        context.startActivity(Intent.createChooser(intent, "Compartir evento"))
    }
}

