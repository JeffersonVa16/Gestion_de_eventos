# Cómo Actualizar la API Key en google-services.json

Si creaste una nueva API key, necesitas actualizar el archivo:

1. Abre `app/google-services.json`
2. Busca la línea que dice:
   ```json
   "current_key": "AIzaSyB_XHMklmCuNs35yBsitvnNqCPusgMxQ5Q"
   ```
3. Reemplaza `AIzaSyB_XHMklmCuNs35yBsitvnNqCPusgMxQ5Q` con tu nueva API key
4. Guarda el archivo

Pero es MEJOR regenerar el google-services.json desde Firebase Console (Opción 1 arriba).


