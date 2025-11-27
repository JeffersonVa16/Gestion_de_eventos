# Cambio a Modo Local - Sin Firestore

## ✅ Cambios Realizados

Se ha simplificado la aplicación para que funcione **sin necesidad de Firestore**. Ahora todos los datos se almacenan en memoria de forma local.

### 1. Nuevo Repositorio Local
- **Archivo creado**: `LocalEventRepository.kt`
- **Funcionalidad**: Almacena todos los eventos, comentarios y calificaciones en memoria
- **Ventajas**: 
  - No requiere configuración de base de datos
  - Funciona inmediatamente sin configuraciones adicionales
  - Sin errores de conexión a Firestore

### 2. EventViewModel Simplificado
- Actualizado para usar `LocalEventRepository` en lugar de `EventRepository` (Firestore)
- Eliminada la lógica de merge entre Firestore y datos locales
- Funciones simplificadas para trabajar solo con datos locales

### 3. AuthRepository Simplificado
- Eliminadas todas las dependencias de Firestore
- Mantiene Firebase Auth para autenticación (solo requiere configuración básica de Firebase)
- Los datos de usuario se obtienen directamente de Firebase Auth

## 🎯 Funcionalidades Mantenidas

✅ Crear eventos
✅ Ver eventos (próximos y pasados)
✅ Unirse/salir de eventos
✅ Agregar comentarios
✅ Calificar eventos
✅ Editar eventos

## ⚠️ Limitaciones del Modo Local

1. **Los datos se pierden al cerrar la app**: Al ser almacenamiento en memoria, todos los eventos, comentarios y calificaciones se eliminan al cerrar la aplicación.

2. **Sin sincronización**: Los datos no se comparten entre dispositivos.

3. **Imágenes no disponibles**: La subida de imágenes está deshabilitada por simplicidad.

4. **Sin persistencia**: Si necesitas que los datos persistan, necesitarías agregar almacenamiento local (SharedPreferences, Room, etc.).

## 🔧 Cómo Usar

La aplicación ahora funciona de forma inmediata:
1. No necesitas configurar Firestore
2. Solo necesitas Firebase Auth configurado (para login/registro)
3. Todos los eventos de muestra se cargan automáticamente
4. Puedes crear nuevos eventos y se guardarán en memoria mientras la app esté abierta

## 📝 Notas

- Si en el futuro necesitas persistencia de datos, puedes:
  - Usar Room Database para almacenamiento local persistente
  - O volver a habilitar Firestore si lo necesitas
  - O usar SharedPreferences para datos simples

- El archivo `EventRepository.kt` (original con Firestore) sigue existiendo pero no se usa. Puedes eliminarlo si no planeas volver a usarlo.

