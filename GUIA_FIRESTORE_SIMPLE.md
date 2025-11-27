# Guía Simple para Configurar Firestore

## Opción 1: Configurar Firestore en tu Proyecto Actual

Si quieres usar el proyecto Firebase actual (`gestion-eventos-51969`):

### Paso 1: Ir a la Consola de Firebase
1. Ve a: https://console.firebase.google.com/
2. Selecciona tu proyecto: **gestion-eventos-51969**

### Paso 2: Crear la Base de Datos Firestore
1. En el menú lateral, haz clic en **Firestore Database**
2. Si no existe, haz clic en **Crear base de datos**
3. **IMPORTANTE**: Selecciona **Modo Nativo** (NO Modo Datastore)
4. Elige una ubicación (ej: `us-central` o la más cercana)
5. Haz clic en **Habilitar**

### Paso 3: Configurar las Reglas de Seguridad
1. Ve a la pestaña **Reglas**
2. Copia y pega estas reglas (para desarrollo/pruebas):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Permitir lectura y escritura a usuarios autenticados
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

3. Haz clic en **Publicar**

### Paso 4: Verificar que la API esté Habilitada
1. Ve a: https://console.cloud.google.com/apis/library/firestore.googleapis.com?project=gestion-eventos-51969
2. Verifica que diga **Habilitada**
3. Si no, haz clic en **Habilitar**

¡Listo! Tu aplicación debería funcionar ahora.

---

## Opción 2: Crear un Nuevo Proyecto Firebase

Si prefieres empezar desde cero con un proyecto nuevo:

### Paso 1: Crear Nuevo Proyecto
1. Ve a: https://console.firebase.google.com/
2. Haz clic en **Agregar proyecto** o **Crear proyecto**
3. Ingresa un nombre (ej: `gestion-eventos-nuevo`)
4. Sigue los pasos para crear el proyecto

### Paso 2: Agregar App Android
1. En la pantalla principal del proyecto, haz clic en el ícono de Android
2. Ingresa el nombre del paquete: `com.gestion.eventos`
3. Descarga el archivo `google-services.json`
4. Reemplaza el archivo actual en tu proyecto: `app/google-services.json`

### Paso 3: Configurar Firestore
1. Ve a **Firestore Database**
2. Haz clic en **Crear base de datos**
3. Selecciona **Modo Nativo**
4. Elige una ubicación
5. Configura las reglas (usa las mismas del Paso 3 de la Opción 1)

### Paso 4: Actualizar el Código
Solo necesitas reemplazar el archivo `google-services.json` y el código ya está listo para usar Firestore.

---

## ¿Cuál Opción Elegir?

- **Opción 1**: Si ya tienes usuarios o datos en Firebase Auth del proyecto actual
- **Opción 2**: Si quieres empezar completamente desde cero

## Verificar que Funciona

Después de configurar, ejecuta la aplicación y:
1. Inicia sesión o regístrate
2. Intenta crear un evento
3. Si funciona sin errores, ¡está configurado correctamente!

## Problemas Comunes

**"Firestore no está configurado"**
- Verifica que esté en **Modo Nativo**, no Datastore
- Verifica que las reglas estén publicadas

**"PERMISSION_DENIED"**
- Verifica que las reglas de seguridad permitan acceso
- Verifica que el usuario esté autenticado

**"API no habilitada"**
- Ve a Google Cloud Console y habilita la API de Firestore

