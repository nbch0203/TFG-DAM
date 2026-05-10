# Modelo de datos

Esta documentación explica las tablas principales del proyecto y cómo se relacionan entre sí. La idea es entender qué guarda cada tabla y por qué existe.

## Tablas principales

| Tabla | Qué guarda |
|---|---|
| `users` | Usuarios del sistema, con su rol y datos básicos de acceso. |
| `schools` | Colegios gestionados desde la administración. |
| `students` | Alumnos vinculados a un usuario padre o tutor. |
| `routes` | Rutas de autobús. |
| `stops` | Paradas que forman parte de una ruta. |
| `buses` | Autobuses registrados en el sistema. |
| `route_assignments` | Asignación de una ruta a un bus y a un conductor en una fecha concreta. |
| `real_time_location` | Ubicaciones enviadas por el conductor durante una ruta. |
| `notifications` | Avisos enviados a los usuarios. |
| `admin_messages` | Mensajes o incidencias gestionadas por administración. |

## Relaciones importantes

- Un **usuario padre** puede tener varios alumnos asociados.
- Un **alumno** se relaciona con una ruta y una parada.
- Una **ruta** tiene varias paradas ordenadas.
- Un **bus** puede asignarse a una ruta concreta mediante `route_assignments`.
- Un **conductor** envía posiciones que se almacenan en `real_time_location`.
- Las **notificaciones** se asocian a un usuario concreto.

## Explicación simple del recorrido de datos

1. La administración crea rutas, paradas y autobuses.
2. Se asignan alumnos a una ruta o parada.
3. El conductor inicia la ruta desde la app.
4. El sistema guarda su ubicación mientras avanza.
5. La familia consulta el mapa y ve el autobús activo.

## Pendiente de completar

- Confirmar si todas las tablas mencionadas ya existen con el mismo nombre en la base de datos final.
- Añadir una relación visual si se incorpora un diagrama ER en la memoria.