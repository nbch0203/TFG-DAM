# Flujo del sistema

Este documento explica el recorrido básico de la información dentro de SchoolSafeTrack.

## Flujo general

```text
Usuario -> interfaz web o app -> backend -> base de datos -> respuesta en pantalla
```

## Flujo de acceso

```text
Pantalla de login -> backend -> validación de usuario -> acceso según rol
```

## Flujo de seguimiento

```text
Conductor inicia ruta -> app envía ubicación -> backend guarda datos -> familia ve el autobús en el mapa
```

## Flujo de administración

```text
Admin entra al panel -> crea o edita datos -> backend guarda cambios -> el sistema refleja la actualización
```

## Relaciones clave

- `students.parent_id` vincula al alumno con su tutor o padre.
- `stops.route_id` indica a qué ruta pertenece una parada.
- `route_assignments` relaciona ruta, bus y conductor para una fecha concreta.
- `real_time_location` guarda la posición del autobús mientras circula.

## Pendiente de completar

- Añadir el flujo específico del profesorado cuando su uso quede definido.
- Explicar con más detalle cómo se generan las alertas o notificaciones.