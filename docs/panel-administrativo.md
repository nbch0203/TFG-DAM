# Panel Administrativo

Documento tecnico de la parte administrativa del sistema: panel web, CRUD principales y extensiones del backend.

## Alcance funcional

- Dashboard general.
- Gestion de autobuses.
- Gestion de usuarios.
- Gestion de colegios.
- Gestion de alumnos.
- Gestion de rutas.
- Gestion de paradas.
- Mensajes e incidencias administrativas.

## Frontend

- `frontend-web/src/AdminPage/AdminPage.vue`
- `frontend-web/src/AdminDashboard/AdminDashboard.vue`
- `frontend-web/src/BusManagement/BusManagement.vue`
- `frontend-web/src/UserManagement/UserManagement.vue`
- `frontend-web/src/SchoolManagement/SchoolManagement.vue`
- `frontend-web/src/StudentManagement/StudentManagement.vue`
- `frontend-web/src/RouteManagement/RouteManagement.vue`
- `frontend-web/src/StopManagement/StopManagement.vue`
- `frontend-web/src/AdminMessages/AdminMessages.vue`

## Backend

- `/api/users`, `/api/users/:id`
- `/api/colegios`, `/api/schools`
- `/api/buses`
- `/api/students`
- `/api/routes`
- `/api/stops`
- `/api/admin/messages` y subrutas asociadas

## Base de datos

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

## Uso recomendado

1. Entra como administrador.
2. Revisa el dashboard.
3. Gestiona colegios, rutas y paradas.
4. Asigna alumnos y comprueba la relacion alumno -> parada -> ruta -> bus.