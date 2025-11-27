# Solución para el Error de Firestore

Si estás viendo el mensaje "La base de datos Firestore no está configurada" aunque ya tengas una base de datos creada, sigue estos pasos para diagnosticar y solucionar el problema:

## 1. Verificar que Firestore esté en Modo Nativo

Firestore puede estar en dos modos:
- **Modo Nativo (Native Mode)**: El que necesitas para esta aplicación
- **Modo Datastore (Datastore Mode)**: Modo legacy

**Pasos:**
1. Ve a la [Consola de Firebase](https://console.firebase.google.com/)
2. Selecciona tu proyecto: `gestion-eventos-51969`
3. Ve a **Firestore Database**
4. Verifica que diga "Firestore Database" y no "Cloud Datastore"
5. Si dice "Cloud Datastore", necesitas crear una nueva base de datos Firestore en modo nativo

## 2. Verificar las Reglas de Seguridad de Firestore

Las reglas de seguridad pueden estar bloqueando las operaciones. Verifica y actualiza las reglas:

1. Ve a la [Consola de Firestore](https://console.firebase.google.com/project/gestion-eventos-51969/firestore)
2. Ve a la pestaña **Reglas**
3. Verifica que las reglas permitan lectura y escritura para usuarios autenticados

**Reglas recomendadas para desarrollo:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para usuarios
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Reglas para eventos
    match /events/{eventId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null;
      allow delete: if request.auth != null;
    }
    
    // Reglas para comentarios
    match /comments/{commentId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        request.resource.data.userId == request.auth.uid;
    }
    
    // Reglas para calificaciones
    match /ratings/{ratingId} {
      allow read: if request.auth != null;
      allow create, update: if request.auth != null;
    }
  }
}
```

**⚠️ IMPORTANTE:** Estas reglas son para desarrollo. Para producción, deberías hacerlas más restrictivas.

## 3. Verificar que la API de Firestore esté Habilitada

1. Ve a la [Consola de Google Cloud](https://console.cloud.google.com/apis/library/firestore.googleapis.com?project=gestion-eventos-51969)
2. Verifica que la API de **Cloud Firestore API** esté habilitada
3. Si no está habilitada, haz clic en **Habilitar**

## 4. Verificar el Archivo google-services.json

1. Asegúrate de que el archivo `app/google-services.json` esté actualizado
2. Verifica que el `project_id` sea correcto: `gestion-eventos-51969`

## 5. Verificar la Inicialización de Firebase

El archivo `google-services.json` debe estar correctamente configurado. Verifica que:
- El archivo esté en la ruta correcta: `app/google-services.json`
- El `package_name` en el archivo coincida con tu aplicación: `com.gestion.eventos`

## 6. Verificar los Índices de Firestore

Si estás usando consultas con `orderBy` o `where`, es posible que necesites crear índices:

1. Ve a la [Consola de Firestore](https://console.firebase.google.com/project/gestion-eventos-51969/firestore)
2. Ve a la pestaña **Índices**
3. Si hay errores de índices faltantes, Firebase te dará un enlace para crearlos automáticamente

## 7. Verificar los Logs de Errores

Para obtener más información sobre el error real:

1. Abre Android Studio
2. Ve a la pestaña **Logcat**
3. Filtra por "Firebase" o "Firestore"
4. Busca los mensajes de error completos

## 8. Solución Rápida: Reglas Temporales para Pruebas

Si quieres probar rápidamente si el problema son las reglas, puedes usar temporalmente estas reglas permisivas (⚠️ **SOLO PARA PRUEBAS, NO PARA PRODUCCIÓN**):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 9. Crear la Base de Datos si No Existe

Si realmente no tienes una base de datos Firestore:

1. Ve a: https://console.cloud.google.com/datastore/setup?project=gestion-eventos-51969
2. Selecciona **Crear base de datos**
3. Elige **Modo Nativo de Firestore**
4. Selecciona una ubicación para tu base de datos (recomendado: una cerca de tus usuarios)
5. Haz clic en **Crear**

## 10. Reiniciar la Aplicación

Después de hacer cambios en las reglas o configuración:

1. Cierra completamente la aplicación
2. Limpia el caché de la aplicación (Configuración > Aplicaciones > Tu App > Almacenamiento > Limpiar caché)
3. Vuelve a abrir la aplicación

## Verificación Final

Después de seguir estos pasos, intenta:

1. Iniciar sesión en la aplicación
2. Crear un nuevo evento
3. Unirte a un evento existente

Si el problema persiste, revisa los logs en Logcat para obtener el mensaje de error exacto que Firebase está devolviendo.

## Contacto y Soporte

Si después de seguir todos estos pasos el problema persiste, proporciona:
- El mensaje de error exacto de Logcat
- Una captura de pantalla de las reglas de Firestore
- El estado de la API de Firestore en Google Cloud Console

