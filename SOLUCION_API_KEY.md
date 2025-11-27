# Solución para Error de API Key Inválida

## ✅ Cambios Realizados en el Código

He agregado:
1. ✅ Plugin de Google Services en `build.gradle.kts`
2. ✅ Dependencias de Firebase (Auth, Firestore, Storage)
3. ✅ Configuración correcta del proyecto

## 🔧 Pasos Adicionales Necesarios en Firebase Console

El error "API key not valid" generalmente ocurre porque las APIs de Firebase no están habilitadas en Google Cloud Console. Sigue estos pasos:

### Paso 1: Habilitar Firebase Authentication API

1. Ve a: https://console.cloud.google.com/apis/library/identitytoolkit.googleapis.com?project=gestion-eventos-77461
2. Haz clic en **Habilitar**
3. Espera unos segundos a que se habilite

### Paso 2: Habilitar Cloud Firestore API

1. Ve a: https://console.cloud.google.com/apis/library/firestore.googleapis.com?project=gestion-eventos-77461
2. Haz clic en **Habilitar**
3. Espera unos segundos a que se habilite

### Paso 3: Habilitar Firebase Storage API (opcional, para imágenes)

1. Ve a: https://console.cloud.google.com/apis/library/storage-component.googleapis.com?project=gestion-eventos-77461
2. Haz clic en **Habilitar**

### Paso 4: Verificar que Firebase Authentication esté Habilitado

1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/authentication
2. Si no está habilitado, haz clic en **Comenzar**
3. Selecciona **Correo electrónico/Contraseña** como método de autenticación
4. Haz clic en **Habilitar** y luego en **Guardar**

### Paso 5: Crear Firestore Database (si aún no lo has hecho)

1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/firestore
2. Si no existe, haz clic en **Crear base de datos**
3. Selecciona **Modo Nativo** (NO Datastore)
4. Elige una ubicación (ej: `us-central`)
5. Haz clic en **Habilitar**

### Paso 6: Configurar Reglas de Firestore

1. En la pestaña **Reglas** de Firestore, pega esto:

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

2. Haz clic en **Publicar**

### Paso 7: Verificar Restricciones de la API Key

1. Ve a: https://console.cloud.google.com/apis/credentials?project=gestion-eventos-77461
2. Busca la API key que empieza con `AIzaSyB_XHMklmCuNs35yBsitvnNqCPusgMxQ5Q`
3. Haz clic en ella para editarla
4. En **Restricciones de API**, asegúrate de que esté configurada como:
   - **No hay restricciones** (para desarrollo)
   - O si tiene restricciones, que incluya:
     - Identity Toolkit API
     - Cloud Firestore API
     - Firebase Storage API

### Paso 8: Limpiar y Reconstruir el Proyecto

Después de hacer los cambios:

1. En Android Studio, ve a **Build > Clean Project**
2. Luego **Build > Rebuild Project**
3. Ejecuta la aplicación nuevamente

## 🔍 Verificar que Todo Esté Correcto

Después de seguir todos los pasos:

1. ✅ Firebase Authentication habilitado
2. ✅ Cloud Firestore API habilitada
3. ✅ Identity Toolkit API habilitada
4. ✅ Firestore Database creada en Modo Nativo
5. ✅ Reglas de seguridad configuradas
6. ✅ API key sin restricciones (o con las correctas)

## ⚠️ Si el Error Persiste

Si después de seguir todos los pasos el error continúa:

1. **Verifica el SHA-1 de tu app**:
   - En Android Studio, ve a **Gradle** > **app** > **Tasks** > **android** > **signingReport**
   - Copia el SHA-1
   - Ve a Firebase Console > Configuración del proyecto > Agregar huella digital
   - Agrega el SHA-1

2. **Verifica que el package name sea correcto**:
   - Debe ser exactamente: `com.gestion.eventos`

3. **Descarga nuevamente el google-services.json**:
   - Ve a Firebase Console > Configuración del proyecto
   - Descarga el archivo `google-services.json` nuevamente
   - Reemplázalo en `app/google-services.json`

## 📝 Nota Importante

Después de habilitar las APIs, puede tomar unos minutos para que los cambios se propaguen. Si el error persiste inmediatamente, espera 2-3 minutos y vuelve a intentar.

