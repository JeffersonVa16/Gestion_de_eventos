# Configuración Completa de Firebase - Paso a Paso

## ✅ Lo que YA está configurado en el código:

1. ✅ Plugin de Google Services agregado
2. ✅ Dependencias de Firebase agregadas
3. ✅ Clase Application personalizada creada
4. ✅ Permisos de internet agregados
5. ✅ google-services.json actualizado con el nuevo proyecto

## 🔧 Pasos FINALES que DEBES hacer en Firebase Console:

### PASO 1: Verificar y Habilitar Firebase Authentication

1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/authentication/providers
2. Si ves "Comenzar", haz clic
3. Selecciona **"Correo electrónico/Contraseña"**
4. Activa el toggle
5. Haz clic en **"Guardar"**

### PASO 2: Obtener SHA-1 (CRÍTICO)

**En Android Studio:**
1. Abre la pestaña **Gradle** (lado derecho)
2. Expande: **Gestion de Eventos** → **app** → **Tasks** → **android**
3. Haz doble clic en **signingReport**
4. En la consola de abajo, busca:
   ```
   Variant: debug
   Config: debug
   Store: ...
   Alias: ...
   SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
   ```
5. **COPIA el SHA-1 completo**

**O desde terminal:**
```bash
cd "C:\Users\ivoal\Documents\GitHub\Gestion_de_eventos"
gradlew signingReport
```

### PASO 3: Agregar SHA-1 a Firebase (MUY IMPORTANTE)

1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/settings/general
2. Desplázate hasta **"Tus aplicaciones"**
3. Haz clic en la app Android (debería mostrar `com.gestion.eventos`)
4. Haz clic en **"Agregar huella digital"**
5. Pega el SHA-1 que copiaste
6. Haz clic en **"Guardar"**

### PASO 4: Verificar/Crear Firestore

1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/firestore
2. Si no existe, haz clic en **"Crear base de datos"**
3. **IMPORTANTE**: Selecciona **"Modo Nativo"** (NO Datastore)
4. Elige una ubicación (ej: `us-central`)
5. Haz clic en **"Habilitar"**

### PASO 5: Configurar Reglas de Firestore

1. En Firestore, ve a la pestaña **"Reglas"**
2. Reemplaza todo con esto:

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

3. Haz clic en **"Publicar"**

### PASO 6: Verificar API Key (Quitar Restricciones Temporalmente)

1. Ve a: https://console.cloud.google.com/apis/credentials?project=gestion-eventos-77461
2. Busca la API key que empieza con `AIzaSyB_XHMklmCuNs35yBsitvnNqCPusgMxQ5Q`
3. Haz clic en ella
4. En **"Restricciones de API"**, selecciona **"No hay restricciones"**
5. Haz clic en **"Guardar"**

### PASO 7: Verificar que las APIs estén Habilitadas

Verifica estas URLs (deberían decir "Habilitada"):

1. Identity Toolkit API: https://console.cloud.google.com/apis/library/identitytoolkit.googleapis.com?project=gestion-eventos-77461
2. Cloud Firestore API: https://console.cloud.google.com/apis/library/firestore.googleapis.com?project=gestion-eventos-77461

### PASO 8: Descargar google-services.json Nuevamente

1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/settings/general
2. Desplázate hasta **"Tus aplicaciones"**
3. Haz clic en la app Android
4. Haz clic en el ícono de descarga (📥) junto a **"google-services.json"**
5. Reemplaza el archivo en `app/google-services.json` con el descargado

### PASO 9: Limpiar Proyecto Completamente

En Android Studio:

1. **File** → **Invalidate Caches / Restart** → **Invalidate and Restart**
2. Espera a que se reinicie completamente
3. **Build** → **Clean Project**
4. Espera a que termine
5. **Build** → **Rebuild Project**
6. Espera a que termine
7. **File** → **Sync Project with Gradle Files**

### PASO 10: Probar la Aplicación

1. Ejecuta la app
2. Intenta registrarte con un email y contraseña
3. Si funciona, ¡listo!
4. Si no funciona, revisa Logcat para ver el error exacto

## 🔍 Si Sigue Sin Funcionar:

### Ver Logs Detallados:

1. En Android Studio, abre **Logcat**
2. Filtra por: `Firebase` o `Auth` o `ERROR`
3. Intenta registrarte
4. Busca mensajes que empiecen con "Firebase:" o "ERROR"
5. Copia el error completo

### Verificar que el Proyecto se Compile Correctamente:

1. Ve a la pestaña **Build** en la parte inferior
2. Verifica que no haya errores
3. Si hay errores, compártelos

## ⚠️ Checklist Final:

Antes de probar, verifica que tengas:

- [ ] Firebase Authentication habilitado (Correo/Contraseña)
- [ ] SHA-1 agregado a Firebase
- [ ] Firestore creado en Modo Nativo
- [ ] Reglas de Firestore configuradas
- [ ] API Key sin restricciones (temporalmente)
- [ ] APIs habilitadas (Identity Toolkit, Firestore)
- [ ] google-services.json descargado nuevamente
- [ ] Proyecto limpiado y reconstruido

## 📞 Si Nada Funciona:

Comparte:
1. El error exacto de Logcat
2. Una captura de pantalla de la configuración de Firebase Authentication
3. Una captura de pantalla mostrando que el SHA-1 está agregado

