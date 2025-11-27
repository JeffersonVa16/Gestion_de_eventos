# Checklist Completo para Resolver Error de API Key

## ✅ Pasos que DEBES seguir en orden:

### 1. Verificar SHA-1 y Agregarlo a Firebase (MUY IMPORTANTE)

**Obtener SHA-1:**
- En Android Studio: **Gradle** → **app** → **Tasks** → **android** → **signingReport**
- O ejecuta: `./gradlew signingReport` (o `gradlew signingReport` en Windows)
- Copia el SHA-1 que aparece (algo como: `XX:XX:XX:XX:XX:XX...`)

**Agregar a Firebase:**
1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/settings/general
2. Desplázate hasta **"Tus aplicaciones"**
3. Haz clic en la app Android
4. Haz clic en **"Agregar huella digital"**
5. Pega el SHA-1
6. **GUARDA**

### 2. Verificar Restricciones de API Key

1. Ve a: https://console.cloud.google.com/apis/credentials?project=gestion-eventos-77461
2. Busca la API key: `AIzaSyB_XHMklmCuNs35yBsitvnNqCPusgMxQ5Q`
3. Ábrela
4. En **"Restricciones de API"**:
   - **TEMPORALMENTE** cambia a **"No hay restricciones"** (solo para probar)
   - O asegúrate de que incluya: Identity Toolkit API, Cloud Firestore API
5. **GUARDA**

### 3. Verificar Firebase Authentication

1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/authentication/providers
2. Verifica que **"Correo electrónico/Contraseña"** esté **HABILITADO**
3. Si no está, habilítalo

### 4. Descargar Nuevamente google-services.json

1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/settings/general
2. Desplázate hasta **"Tus aplicaciones"**
3. Haz clic en la app Android
4. Haz clic en **"Descargar google-services.json"**
5. Reemplaza el archivo en `app/google-services.json`

### 5. Limpiar y Reconstruir Proyecto

En Android Studio:
1. **File** → **Invalidate Caches / Restart** → **Invalidate and Restart**
2. Espera a que se reinicie
3. **Build** → **Clean Project**
4. **Build** → **Rebuild Project**
5. **File** → **Sync Project with Gradle Files**

### 6. Verificar que el Proyecto se Sincronice Correctamente

1. Abre la pestaña **Build** en la parte inferior de Android Studio
2. Verifica que no haya errores de compilación
3. Si hay errores, compártelos

### 7. Probar la Aplicación

1. Ejecuta la app
2. Intenta registrarte
3. Si sigue fallando, revisa **Logcat** para ver el error completo

## 🔍 Verificar Logs Detallados

Si el error persiste:

1. En Android Studio, abre **Logcat**
2. Filtra por: `Firebase` o `Auth` o `ERROR`
3. Intenta registrarte
4. Busca el mensaje de error completo
5. **Copia el error completo** y compártelo

## ⚠️ Errores Comunes y Soluciones

### "API key not valid"
- **Causa**: Restricciones de API key o SHA-1 no registrado
- **Solución**: Quitar restricciones temporalmente Y agregar SHA-1

### "An internal error has occurred"
- **Causa**: Firebase no se inicializa correctamente
- **Solución**: Ya agregué una clase Application personalizada para inicializar Firebase

### "Network error"
- **Causa**: Sin conexión a internet o permisos faltantes
- **Solución**: Ya agregué permisos de internet al manifest

## 📝 Cambios Realizados en el Código

He hecho estos cambios:
1. ✅ Agregado permisos de internet al AndroidManifest
2. ✅ Creado clase Application personalizada para inicializar Firebase
3. ✅ Mejorado manejo de errores para mostrar mensajes más claros
4. ✅ Plugin de Google Services configurado
5. ✅ Dependencias de Firebase agregadas

## 🎯 Lo Más Importante

**El paso MÁS CRÍTICO es agregar el SHA-1 a Firebase.** Sin esto, Firebase no reconocerá tu app y dará error de API key.

Después de agregar el SHA-1 y quitar las restricciones de la API key temporalmente, debería funcionar.

