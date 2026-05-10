# API Endpoints - Backend

Este documento lista los endpoints principales del backend. Para una documentación más completa, consulta el código o una documentación interactiva (Swagger) si está disponible.

## Autenticación

### POST /api/auth/login
Inicia sesión y devuelve un token JWT.

**Parámetros:**
- `email` (string): correo del usuario.
- `password` (string): contraseña.

**Respuesta:**
```json
{
  "token": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "admin@school.com",
    "role": "admin"
  }
}
```

---

## Gestión de usuarios

### GET /api/users
Lista todos los usuarios. Requiere autenticación.

**Respuesta:**
```json
[
  {
    "id": 1,
    "email": "admin@school.com",
    "name": "Administrador",
    "role": "admin"
  }
]
```

### POST /api/users
Crea un nuevo usuario. Requiere rol admin.

### GET /api/users/:id
Obtiene un usuario específico.

### PUT /api/users/:id
Edita un usuario existente.

### DELETE /api/users/:id
Elimina un usuario.

---

## Gestión de rutas

### GET /api/routes
Lista todas las rutas.

**Respuesta:**
```json
[
  {
    "id": 1,
    "name": "Ruta A - Centro",
    "school_id": 1,
    "active": true
  }
]
```

### POST /api/routes
Crea una nueva ruta. Requiere rol admin.

### PUT /api/routes/:id
Edita una ruta existente.

### DELETE /api/routes/:id
Elimina una ruta.

---

## Gestión de paradas

### GET /api/stops
Lista todas las paradas.

### GET /api/stops?route_id=1
Lista paradas de una ruta específica.

**Respuesta:**
```json
[
  {
    "id": 1,
    "name": "Centro comercial",
    "latitude": 40.415363,
    "longitude": -3.707398,
    "order": 1,
    "route_id": 1
  }
]
```

### POST /api/stops
Crea una nueva parada.

### PUT /api/stops/:id
Edita una parada.

---

## Gestión de alumnos

### GET /api/students
Lista todos los alumnos.

### GET /api/students?parent_id=5
Lista alumnos de un padre específico.

### POST /api/students
Crea un nuevo alumno.

### PUT /api/students/:id
Edita un alumno.

---

## Tiempo real

### WebSocket /socket.io
Canal de comunicación en tiempo real para ubicaciones.

**Eventos:**
- `driver-location`: envía ubicación del autobús.
- `update-map`: actualiza mapa en clientes suscritos.

---

## Pendiente de completar

- Documentar respuestas de error.
- Detallar parámetros de query (filtros, paginación).
- Incluir ejemplos de cURL o Postman.
- Documentar validaciones y restricciones.
