# Panel administrativo

Este documento explica la parte web que usa la administración para mantener el sistema al día.

## Para qué sirve

El panel permite revisar el estado general del servicio y administrar los datos principales del proyecto sin tocar directamente la base de datos.

## Qué incluye

- Dashboard general.
- Gestión de autobuses.
- Gestión de usuarios.
- Gestión de colegios.
- Gestión de alumnos.
- Gestión de rutas.
- Gestión de paradas.
- Mensajes e incidencias administrativas.

## Pantallas principales

- `frontend-web/src/AdminPage/AdminPage.vue`
- `frontend-web/src/AdminDashboard/AdminDashboard.vue`
- `frontend-web/src/BusManagement/BusManagement.vue`
- `frontend-web/src/UserManagement/UserManagement.vue`
- `frontend-web/src/SchoolManagement/SchoolManagement.vue`
- `frontend-web/src/StudentManagement/StudentManagement.vue`
- `frontend-web/src/RouteManagement/RouteManagement.vue`
- `frontend-web/src/StopManagement/StopManagement.vue`
- `frontend-web/src/AdminMessages/AdminMessages.vue`

## Rutas del backend

- `/api/users`, `/api/users/:id`
- `/api/colegios`, `/api/schools`
- `/api/buses`
- `/api/students`
- `/api/routes`
- `/api/stops`
- `/api/admin/messages` y subrutas asociadas

## Datos relacionados

- `users`
- `schools`
- `buses`
- `students`
- `routes`
- `stops`
- `route_assignments`
- `student_stops`
- `admin_messages`
- `admin_message_notes`

## Flujo de uso

1. Entra como administrador.
2. Revisa el dashboard.
3. Gestiona colegios, rutas y paradas.
4. Asigna alumnos y comprueba la relación alumno -> parada -> ruta -> bus.

## Pendiente de completar

- Describir con más detalle qué valida cada formulario.
- Confirmar si todos los nombres de rutas del backend están estables.