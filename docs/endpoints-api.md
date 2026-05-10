# 🔌 Endpoints del Backend - API REST

**Versión:** 1.0  
**Base URL:** `/api` (o según configuración de VITE_API_URL)  
**Entorno:** Node.js + Express + MySQL

---

## 📋 Índice Rápido

- [Healthcheck / Sistema](#-healthcheck--sistema)
- [Autenticación](#-autenticación)
- [Colegios](#-colegios)
- [Usuarios](#-usuarios)
- [Autobuses](#-autobuses)
- [Estudiantes](#-estudiantes)
- [Rutas](#-rutas)
- [Paradas](#-paradas)
- [Asignaciones de Rutas](#-asignaciones-de-rutas)
- [Funciones del Conductor](#-funciones-del-conductor)
- [Funciones del Padre](#-funciones-del-padre)
- [Mensajes](#-mensajes)

---

## 🏥 Healthcheck / Sistema

### **GET /health**
Verifica el estado del servidor y conexión a base de datos.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `https://dominio/health` |
| **Autenticación** | ❌ No requerida |
| **Response** | JSON |

**Respuesta exitosa (200):**
```json
{
  "status": "ok",
  "db": "connected",
  "timestamp": "2026-05-02T10:30:00Z"
}
```

**Respuesta con error (503):**
```json
{
  "status": "error",
  "db": "disconnected",
  "timestamp": "2026-05-02T10:30:00Z"
}
```

---

### **GET /api/system/status**
Obtiene información detallada del servidor y servicios Docker.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `https://dominio/api/system/status` |
| **Autenticación** | ❌ No requerida |
| **Response** | JSON |

**Respuesta (200):**
```json
{
  "timestamp": "2026-05-02T10:30:00Z",
  "server": {
    "uptimeSeconds": 3600,
    "loadAvg": [0.5, 0.6, 0.7],
    "totalMemMB": 2048,
    "freeMemMB": 512
  },
  "docker": {
    "available": true,
    "containers": []
  }
}
```

---

## 🔐 Autenticación

### **POST /api/login**
Autentica un usuario con email y contraseña.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/login` |
| **Body** | JSON |
| **Autenticación** | ❌ No requerida |

**Request:**
```json
{
  "email": "usuario@example.com",
  "password": "password123"
}
```

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "role": "ADMIN"
  }
}
```

**Respuesta con error (401):**
```json
{
  "error": "Correo y contraseña requeridos"
}
```

**Roles disponibles:**
- `ADMIN` - Administrador del sistema
- `DRIVER` - Conductor de autobús
- `PARENT` - Padre/Tutor de estudiante
- `PROFESOR` - Profesor/Educador

---

## 🏫 Colegios

### **GET /api/schools**
Obtiene lista de todos los colegios registrados.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/schools` |
| **Parámetros** | ❌ Ninguno |
| **Autenticación** | ❌ No requerida |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "nombre": "Colegio Principal",
    "direccion": "Calle Principal 123",
    "telefono": "912345678",
    "email": "info@colegio.com",
    "activo": 1,
    "routes_count": 3,
    "stops_count": 12,
    "students_count": 45,
    "created_at": "2026-05-01T08:00:00Z",
    "updated_at": "2026-05-02T10:00:00Z"
  }
]
```

---

### **POST /api/schools**
Crea un nuevo colegio.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/schools` |
| **Body** | JSON |
| **Autenticación** | ❌ No requerida |

**Request:**
```json
{
  "nombre": "Colegio Ejemplo",
  "direccion": "Calle Nueva 456",
  "telefono": "987654321",
  "email": "contacto@colegioejemplo.com",
  "activo": 1
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "id": 2
}
```

---

### **PUT /api/schools/:id**
Actualiza datos de un colegio.

| Campo | Valor |
|-------|-------|
| **Método** | PUT |
| **URL** | `/api/schools/1` |
| **Parámetro** | `id` (número) |
| **Body** | JSON |

**Request:**
```json
{
  "nombre": "Colegio Actualizado",
  "direccion": "Nueva Dirección",
  "telefono": "912345678",
  "email": "nuevo@colegio.com",
  "activo": 1
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **DELETE /api/schools/:id**
Elimina un colegio (solo sin rutas/paradas/estudiantes asociados).

| Campo | Valor |
|-------|-------|
| **Método** | DELETE |
| **URL** | `/api/schools/2` |
| **Parámetro** | `id` (número) |

**Respuesta (200):**
```json
{
  "success": true
}
```

**Respuesta con error (409):**
```json
{
  "error": "No se puede eliminar el colegio porque tiene rutas, paradas o alumnos asociados"
}
```

---

## 👥 Usuarios

### **GET /api/users**
Obtiene lista de todos los usuarios.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/users` |
| **Autenticación** | ❌ No requerida |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "email": "admin@example.com",
    "nombre": "Admin",
    "apellidos": "Usuario",
    "role": "ADMIN",
    "created_at": "2026-05-01T08:00:00Z"
  }
]
```

---

### **GET /api/users/:id**
Obtiene datos de un usuario específico.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/users/1` |
| **Parámetro** | `id` (número) |

**Respuesta (200):** Igual que GET /api/users

---

### **GET /api/parents**
Obtiene lista de padres/tutores.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/parents` |

**Respuesta (200):** Array de usuarios con role = 'PARENT'

---

### **POST /api/users**
Crea un nuevo usuario.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/users` |
| **Body** | JSON |

**Request:**
```json
{
  "email": "newuser@example.com",
  "password": "password123",
  "nombre": "Juan",
  "apellidos": "Pérez",
  "role": "PARENT"
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "id": 5,
  "user": {
    "id": 5,
    "email": "newuser@example.com",
    "role": "PARENT"
  }
}
```

---

### **PUT /api/users/:id**
Actualiza datos de un usuario.

| Campo | Valor |
|-------|-------|
| **Método** | PUT |
| **URL** | `/api/users/1` |
| **Body** | JSON |

**Request:**
```json
{
  "email": "updated@example.com",
  "password": "newpassword123",
  "nombre": "Juan Actualizado",
  "apellidos": "Pérez",
  "role": "ADMIN"
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **PATCH /api/users/:id/profile**
Actualiza el perfil (nombre, apellidos) de un usuario.

| Campo | Valor |
|-------|-------|
| **Método** | PATCH |
| **URL** | `/api/users/1/profile` |
| **Body** | JSON |

**Request:**
```json
{
  "nombre": "Nuevo Nombre",
  "apellidos": "Nuevo Apellido"
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **DELETE /api/users/:id**
Elimina un usuario.

| Campo | Valor |
|-------|-------|
| **Método** | DELETE |
| **URL** | `/api/users/5` |
| **Parámetro** | `id` (número) |

**Respuesta (200):**
```json
{
  "success": true
}
```

---

## 🚍 Autobuses

### **GET /api/buses**
Obtiene lista de todos los autobuses.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/buses` |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "matricula": "AB-123-CD",
    "capacidad": 50,
    "conductor_id": 2,
    "latitud": 40.4168,
    "longitud": -3.7038,
    "activo": 1
  }
]
```

---

### **POST /api/buses**
Crea un nuevo autobús.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/buses` |
| **Body** | JSON |

**Request:**
```json
{
  "matricula": "XY-456-ZZ",
  "capacidad": 45,
  "conductor_id": null,
  "activo": 1
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "id": 4
}
```

---

### **PUT /api/buses/:id**
Actualiza datos de un autobús.

| Campo | Valor |
|-------|-------|
| **Método** | PUT |
| **URL** | `/api/buses/1` |
| **Body** | JSON |

**Request:**
```json
{
  "matricula": "AB-999-CD",
  "capacidad": 52,
  "activo": 1
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **DELETE /api/buses/:id**
Elimina un autobús.

| Campo | Valor |
|-------|-------|
| **Método** | DELETE |
| **URL** | `/api/buses/4` |

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **PATCH /api/buses/:id/location**
Actualiza la ubicación GPS de un autobús.

| Campo | Valor |
|-------|-------|
| **Método** | PATCH |
| **URL** | `/api/buses/1/location` |
| **Body** | JSON |

**Request:**
```json
{
  "latitud": 40.4170,
  "longitud": -3.7040,
  "velocidad": 45
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **PATCH /api/buses/:id/driver**
Asigna un conductor a un autobús.

| Campo | Valor |
|-------|-------|
| **Método** | PATCH |
| **URL** | `/api/buses/1/driver` |
| **Body** | JSON |

**Request:**
```json
{
  "conductor_id": 2
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

## 📚 Estudiantes

### **GET /api/students**
Obtiene lista de todos los estudiantes.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/students` |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "nombre": "Carlos",
    "apellidos": "García López",
    "parent_id": 3,
    "route_id": 1,
    "stop_id": 5,
    "school_id": 1,
    "activo": 1
  }
]
```

---

### **GET /api/students/:id**
Obtiene datos de un estudiante específico.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/students/1` |
| **Parámetro** | `id` (número) |

**Respuesta (200):** Datos del estudiante con detalles de ruta y parada

---

### **POST /api/students**
Crea un nuevo estudiante.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/students` |
| **Body** | JSON |

**Request:**
```json
{
  "nombre": "María",
  "apellidos": "Rodríguez Martín",
  "parent_id": 3,
  "route_id": 1,
  "stop_id": 5,
  "school_id": 1,
  "activo": 1
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "id": 10
}
```

---

### **PUT /api/students/:id**
Actualiza datos de un estudiante.

| Campo | Valor |
|-------|-------|
| **Método** | PUT |
| **URL** | `/api/students/1` |
| **Body** | JSON |

**Request:**
```json
{
  "nombre": "Carlos",
  "apellidos": "García López",
  "parent_id": 3,
  "route_id": 2,
  "stop_id": 6,
  "school_id": 1,
  "activo": 1
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **DELETE /api/students/:id**
Elimina un estudiante.

| Campo | Valor |
|-------|-------|
| **Método** | DELETE |
| **URL** | `/api/students/1` |

**Respuesta (200):**
```json
{
  "success": true
}
```

---

## 🛣️ Rutas

### **GET /api/routes**
Obtiene lista de todas las rutas.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/routes` |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "nombre": "Ruta Centro",
    "descripcion": "Ruta por el centro de la ciudad",
    "activa": 1,
    "school_id": 1
  }
]
```

---

### **POST /api/routes**
Crea una nueva ruta.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/routes` |
| **Body** | JSON |

**Request:**
```json
{
  "nombre": "Ruta Sur",
  "descripcion": "Ruta por el sur de la ciudad",
  "activa": 1,
  "school_id": 1
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "id": 5
}
```

---

### **PUT /api/routes/:id**
Actualiza datos de una ruta.

| Campo | Valor |
|-------|-------|
| **Método** | PUT |
| **URL** | `/api/routes/1` |
| **Body** | JSON |

**Request:**
```json
{
  "nombre": "Ruta Centro Actualizada",
  "descripcion": "Nueva descripción",
  "activa": 1
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **DELETE /api/routes/:id**
Elimina una ruta.

| Campo | Valor |
|-------|-------|
| **Método** | DELETE |
| **URL** | `/api/routes/5` |

**Respuesta (200):**
```json
{
  "success": true
}
```

---

## 🚏 Paradas

### **GET /api/stops**
Obtiene lista de todas las paradas.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/stops` |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "nombre": "Parada Central",
    "latitud": 40.4168,
    "longitud": -3.7038,
    "orden": 1,
    "route_id": 1,
    "school_id": 1
  }
]
```

---

### **POST /api/stops**
Crea una nueva parada.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/stops` |
| **Body** | JSON |

**Request:**
```json
{
  "nombre": "Parada Nueva",
  "latitud": 40.4200,
  "longitud": -3.7100,
  "orden": 5,
  "route_id": 1,
  "school_id": 1
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "id": 15
}
```

---

### **PUT /api/stops/:id**
Actualiza datos de una parada.

| Campo | Valor |
|-------|-------|
| **Método** | PUT |
| **URL** | `/api/stops/1` |
| **Body** | JSON |

**Request:**
```json
{
  "nombre": "Parada Central Actualizada",
  "latitud": 40.4170,
  "longitud": -3.7040,
  "orden": 1
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **DELETE /api/stops/:id**
Elimina una parada.

| Campo | Valor |
|-------|-------|
| **Método** | DELETE |
| **URL** | `/api/stops/15` |

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **PATCH /api/stops/:id/orden**
Actualiza el orden de una parada en la ruta.

| Campo | Valor |
|-------|-------|
| **Método** | PATCH |
| **URL** | `/api/stops/1/orden` |
| **Body** | JSON |

**Request:**
```json
{
  "orden": 2
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **GET /api/stops/:id/students**
Obtiene estudiantes asignados a una parada.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/stops/1/students` |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "nombre": "Carlos",
    "apellidos": "García López"
  }
]
```

---

### **POST /api/stops/:id/assign-students**
Asigna estudiantes a una parada.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/stops/1/assign-students` |
| **Body** | JSON |

**Request:**
```json
{
  "student_ids": [1, 2, 3]
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "assigned": 3
}
```

---

## 🔗 Asignaciones de Rutas

### **GET /api/route-assignments**
Obtiene lista de asignaciones ruta-conductor-bus.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/route-assignments` |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "route_id": 1,
    "bus_id": 1,
    "conductor_id": 2,
    "fecha": "2026-05-02",
    "status": "ACTIVA"
  }
]
```

---

### **POST /api/route-assignments**
Crea una nueva asignación ruta-conductor-bus.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/route-assignments` |
| **Body** | JSON |

**Request:**
```json
{
  "route_id": 1,
  "bus_id": 1,
  "conductor_id": 2,
  "fecha": "2026-05-02"
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "id": 5
}
```

---

### **PATCH /api/route-assignments/:id/driver**
Cambia el conductor de una asignación.

| Campo | Valor |
|-------|-------|
| **Método** | PATCH |
| **URL** | `/api/route-assignments/1/driver` |
| **Body** | JSON |

**Request:**
```json
{
  "conductor_id": 3
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **PATCH /api/route-assignments/:id/status**
Actualiza el estado de una asignación.

| Campo | Valor |
|-------|-------|
| **Método** | PATCH |
| **URL** | `/api/route-assignments/1/status` |
| **Body** | JSON |

**Request:**
```json
{
  "status": "COMPLETADA"
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **DELETE /api/route-assignments/:id**
Elimina una asignación.

| Campo | Valor |
|-------|-------|
| **Método** | DELETE |
| **URL** | `/api/route-assignments/5` |

**Respuesta (200):**
```json
{
  "success": true
}
```

---

## 🚗 Funciones del Conductor

### **GET /api/drivers**
Obtiene lista de conductores.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/drivers` |

**Respuesta (200):** Array de usuarios con role = 'DRIVER'

---

### **GET /api/driver/:driverId/today-route**
Obtiene la ruta asignada al conductor para hoy.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/driver/2/today-route` |
| **Parámetro** | `driverId` (número) |

**Respuesta (200):**
```json
{
  "id": 1,
  "nombre": "Ruta Centro",
  "bus_id": 1,
  "stops": [
    {
      "id": 1,
      "nombre": "Parada Central",
      "orden": 1,
      "latitud": 40.4168,
      "longitud": -3.7038,
      "students": [...]
    }
  ]
}
```

---

### **POST /api/driver/checkins**
Registra un check-in del conductor en una parada.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/driver/checkins` |
| **Body** | JSON |

**Request:**
```json
{
  "driver_id": 2,
  "stop_id": 1,
  "latitude": 40.4168,
  "longitude": -3.7038,
  "timestamp": "2026-05-02T08:30:00Z"
}
```

**Respuesta (201 Created):**
```json
{
  "success": true
}
```

---

### **POST /api/driver/incidents**
Reporta un incidente durante la ruta.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/driver/incidents` |
| **Body** | JSON |

**Request:**
```json
{
  "driver_id": 2,
  "bus_id": 1,
  "tipo": "RETRASO",
  "descripcion": "Tráfico intenso en la avenida principal",
  "latitude": 40.4170,
  "longitude": -3.7040
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "id": 1
}
```

---

### **GET /api/driver/:driverId/incidents**
Obtiene incidentes reportados por el conductor.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/driver/2/incidents` |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "tipo": "RETRASO",
    "descripcion": "Tráfico intenso",
    "timestamp": "2026-05-02T08:30:00Z"
  }
]
```

---

### **POST /api/driver/finish-route**
Marca la ruta como finalizada.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/driver/finish-route` |
| **Body** | JSON |

**Request:**
```json
{
  "driver_id": 2,
  "bus_id": 1,
  "route_id": 1
}
```

**Respuesta (200):**
```json
{
  "success": true,
  "message": "Ruta finalizada correctamente"
}
```

---

## 👨‍👩‍👧 Funciones del Padre

### **GET /api/parent/:parentId/children**
Obtiene la lista de hijos de un padre.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/parent/3/children` |
| **Parámetro** | `parentId` (número) |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "nombre": "Carlos",
    "apellidos": "García López",
    "route_id": 1,
    "stop_id": 5
  }
]
```

---

### **GET /api/parent/:parentId/buses**
Obtiene ubicación en tiempo real de los autobuses de sus hijos.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/parent/3/buses` |

**Respuesta (200):**
```json
[
  {
    "bus_id": 1,
    "matricula": "AB-123-CD",
    "latitud": 40.4170,
    "longitud": -3.7040,
    "velocidad": 45,
    "ruta": "Ruta Centro",
    "students_in_bus": 25,
    "next_stop": "Parada Central"
  }
]
```

---

### **GET /api/parent/:parentId/children/:childId/incidents**
Obtiene incidentes reportados para un hijo específico.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/parent/3/children/1/incidents` |
| **Parámetros** | `parentId`, `childId` (números) |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "tipo": "RETRASO",
    "descripcion": "Tráfico intenso",
    "timestamp": "2026-05-02T08:30:00Z",
    "bus_id": 1
  }
]
```

---

## 💬 Mensajes

### **GET /api/admin/messages**
Obtiene lista de mensajes en el panel administrativo.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/admin/messages` |

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "from_user_id": 3,
    "subject": "Consulta sobre ruta",
    "message": "¿A qué hora llega el bus?",
    "status": "PENDIENTE",
    "leida": false,
    "created_at": "2026-05-02T10:00:00Z"
  }
]
```

---

### **GET /api/admin/messages/:id**
Obtiene detalles de un mensaje específico.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/admin/messages/1` |

**Respuesta (200):** Detalles completos del mensaje

---

### **PATCH /api/admin/messages/:id/read**
Marca un mensaje como leído.

| Campo | Valor |
|-------|-------|
| **Método** | PATCH |
| **URL** | `/api/admin/messages/1/read` |

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **PATCH /api/admin/messages/:id/status**
Actualiza el estado de un mensaje.

| Campo | Valor |
|-------|-------|
| **Método** | PATCH |
| **URL** | `/api/admin/messages/1/status` |
| **Body** | JSON |

**Request:**
```json
{
  "status": "RESPONDIDA"
}
```

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **POST /api/admin/messages/:id/notes**
Añade notas internas a un mensaje.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/admin/messages/1/notes` |
| **Body** | JSON |

**Request:**
```json
{
  "nota": "Usuario llamó confirmando la ruta"
}
```

**Respuesta (201 Created):**
```json
{
  "success": true
}
```

---

### **DELETE /api/admin/messages/:id**
Elimina un mensaje.

| Campo | Valor |
|-------|-------|
| **Método** | DELETE |
| **URL** | `/api/admin/messages/1` |

**Respuesta (200):**
```json
{
  "success": true
}
```

---

### **POST /api/messages/send**
Envía un nuevo mensaje.

| Campo | Valor |
|-------|-------|
| **Método** | POST |
| **URL** | `/api/messages/send` |
| **Body** | JSON |

**Request:**
```json
{
  "from_user_id": 3,
  "subject": "Consulta sobre ruta",
  "message": "¿A qué hora llega el bus?"
}
```

**Respuesta (201 Created):**
```json
{
  "success": true,
  "id": 5
}
```

---

### **GET /api/messages/mine**
Obtiene los mensajes del usuario autenticado.

| Campo | Valor |
|-------|-------|
| **Método** | GET |
| **URL** | `/api/messages/mine` |

**Respuesta (200):** Array de mensajes del usuario

---

## 📊 Códigos de Estado HTTP

| Código | Significado |
|--------|------------|
| **200** | OK - Solicitud exitosa |
| **201** | Created - Recurso creado exitosamente |
| **400** | Bad Request - Datos inválidos |
| **401** | Unauthorized - Autenticación fallida |
| **404** | Not Found - Recurso no encontrado |
| **409** | Conflict - No se puede completar la acción |
| **503** | Service Unavailable - Servicio no disponible |

---

## 🔒 Notas de Seguridad

- ✅ Todas las contraseñas se envían hasheadas con bcrypt
- ✅ Los endpoints no requieren autenticación (JWT) pero pueden ser protegidos
- ✅ Las coordenadas GPS se almacenan en precisión de 4 decimales
- ✅ Las respuestas están en formato JSON con charset UTF-8

---

**Última actualización:** 2 de Mayo de 2026  
**API Version:** 1.0  
**Status:** OPERATIVA ✅
