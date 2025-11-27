# Cómo Habilitar Firestore API - SOLUCIÓN RÁPIDA

## ⚡ Solución en 3 Pasos:

### Paso 1: Habilitar Firestore API
1. **Haz clic en este enlace directo:**
   https://console.cloud.google.com/apis/library/firestore.googleapis.com?project=gestion-eventos-77461

2. Haz clic en el botón **"HABILITAR"** (o "ENABLE" si está en inglés)

3. Espera unos segundos a que se habilite

### Paso 2: Verificar que esté Habilitada
- Deberías ver un mensaje verde que dice "API habilitada" o "API enabled"
- El botón debería cambiar a "ADMINISTRAR" o "MANAGE"

### Paso 3: Probar la Aplicación
1. Vuelve a la app
2. Intenta registrarte o crear un evento
3. Debería funcionar ahora

## 🔍 Si el Enlace No Funciona:

1. Ve a: https://console.cloud.google.com/
2. Selecciona el proyecto: **gestion-eventos-77461**
3. Ve a **"APIs y servicios"** → **"Biblioteca"**
4. Busca: **"Cloud Firestore API"**
5. Haz clic en **"HABILITAR"**

## ⚠️ También Verifica:

### Firebase Authentication debe estar habilitado:
1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/authentication/providers
2. Asegúrate de que **"Correo electrónico/Contraseña"** esté **HABILITADO**

### Firestore Database debe estar creada:
1. Ve a: https://console.firebase.google.com/project/gestion-eventos-77461/firestore
2. Si no existe, haz clic en **"Crear base de datos"**
3. Selecciona **"Modo Nativo"** (NO Datastore)
4. Elige una ubicación
5. Haz clic en **"Habilitar"**

## ✅ Checklist Rápido:

- [ ] Firestore API habilitada en Google Cloud Console
- [ ] Firebase Authentication habilitado (Correo/Contraseña)
- [ ] Firestore Database creada en Modo Nativo
- [ ] Reglas de Firestore configuradas (para desarrollo: permitir todo a usuarios autenticados)

Después de hacer estos pasos, la app debería funcionar correctamente.

