# GPS y Ubicación - Aplicación Android

La funcionalidad de GPS es crítica para el conductor. Este documento explica cómo funciona el envío de ubicación.

## Flujo general

1. El conductor inicia sesión en la app.
2. Selecciona la ruta asignada.
3. Pulsa "Iniciar ruta".
4. Un servicio en segundo plano captura la ubicación GPS cada X segundos.
5. La ubicación se envía al backend.
6. El backend guarda la posición y la comparte con familias y admin.
7. El conductor finaliza la ruta.

## Servicio de ubicación

### LocationService
Servicio que captura la ubicación del dispositivo en segundo plano.

**Responsabilidades:**
- Obtener coordenadas GPS periódicamente.
- Enviar ubicación al backend.
- Mantener precisión razonable sin agotar batería.

**Permisos necesarios:**
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.ACCESS_COARSE_LOCATION`
- `android.permission.ACCESS_BACKGROUND_LOCATION` (si está activo en segundo plano)

### Intervalo de captura
Por defecto, se recomienda capturar cada 10-30 segundos. Menos frecuencia consume menos batería pero menos precisión. Más frecuencia es más preciso pero agota batería.

## Envío al backend

La ubicación se envía como JSON:

```json
{
  "driver_id": 5,
  "bus_id": 3,
  "latitude": 40.415363,
  "longitude": -3.707398,
  "accuracy": 10,
  "speed": 25.5,
  "timestamp": "2024-05-09T14:30:00Z"
}
```

Métodos de envío:
- **REST HTTP**: petición POST a `/api/location` con token JWT.
- **WebSocket**: si está activo, enviar por socket.io.

## Manejo de modo offline

Si el dispositivo pierde conexión, la app debería:
1. Seguir capturando ubicación localmente.
2. Almacenar en una base de datos local (Room).
3. Sincronizar cuando recupere conexión.

## Precisión y batería

- Usar `ACCESS_FINE_LOCATION` para GPS preciso.
- Usar `ACCESS_COARSE_LOCATION` si necesitas menos precisión pero más batería.
- Considerar GPS+red+sensores para mejorar precisión.

## Pendiente de completar

- Código específico del servicio LocationService.
- Instrucciones de permisos en Android 6.0+.
- Documentación de Room para almacenamiento local.
- Pruebas de consumo de batería.
