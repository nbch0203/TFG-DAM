# School Safe Track - Guía de Prueba

## Sistema Completado

El sistema de gestión de transporte escolar está completamente operativo con todas las funcionalidades implementadas.

## Credenciales de Prueba

### Administrador
- **Email:** admin@schoolsafetrack.com
- **Contraseña:** admin123
- **Rol:** ADMIN
- **Acceso:** Panel completo de administración

### Profesor
- **Email:** profesorprueba@schoolsafetrack.com
- **Contraseña:** profesor123
- **Rol:** PROFESOR
- **Acceso:** Seguimiento de autobuses y gestión de rutas

### Padre/Madre
- **Email:** padre@schoolsafetrack.com
- **Contraseña:** padre123
- **Rol:** PARENT
- **Acceso:** Seguimiento de autobús de hijos y gestión de hijos

## Características Implementadas

### 1. Panel de Administración (ADMIN)
- ✅ Dashboard con estadísticas
- ✅ Seguimiento en tiempo real de autobuses
- ✅ Gestión de autobuses (CRUD)
- ✅ Gestión de usuarios (CRUD)
- ✅ Gestión de estudiantes (CRUD)
- ✅ Gestión de rutas (CRUD)
- ✅ Gestión de paradas (CRUD)

### 2. Panel de Profesor (PROFESOR)
- ✅ Seguimiento de autobuses asignados
- ✅ Consulta de rutas y paradas

### 3. Panel de Padre (PARENT) - NUEVO
- ✅ **Seguimiento de autobús:** Ver ubicación en tiempo real del autobús de su hijo
  - Mapa interactivo con Leaflet
  - Información del conductor
  - Ruta y parada actual
  - Tiempo estimado de llegada (ETA)
  - Actualización automática cada 30 segundos

- ✅ **Gestión de hijos:** Ver información completa de sus hijos
  - Datos personales (nombre, apellidos, curso)
  - Colegio asignado
  - Autobús y parada habitual
  - Modal con detalles completos
  - FAQ con preguntas frecuentes

## Estructura del Proyecto

```
frontend-web/src/
├── core/
│   ├── App.vue              # Enrutamiento principal (login y roles)
│   ├── main.js
│   └── style.css
├── AdminPage/
│   └── AdminPage.vue        # Panel de administración
├── AdminDashboard/
│   └── AdminDashboard.vue   # Dashboard de admin
├── BusManagement/
│   └── BusManagement.vue    # Gestión de autobuses
├── UserManagement/
│   └── UserManagement.vue   # Gestión de usuarios (FIJO)
├── StudentManagement/
│   └── StudentManagement.vue
├── RouteManagement/
│   └── RouteManagement.vue
├── StopManagement/
│   └── StopManagement.vue
├── BusTracking/
│   └── BusTracking.vue
├── ProfesorPage/
│   └── ProfesorPage.vue
├── ParentPage/              # NUEVO
│   └── ParentPage.vue       # Panel de padres
├── ParentBusTracking/       # NUEVO
│   └── ParentBusTracking.vue# Seguimiento para padres
├── ParentChildrenManagement/# NUEVO
│   └── ParentChildrenManagement.vue # Gestión de hijos
└── utils/
    └── api.js              # Utilidades de API
```

## API Backend

### Endpoints Implementados

#### Autenticación
- `POST /api/login` - Iniciar sesión

#### Autobuses
- `GET /api/buses` - Listar todos los autobuses
- `POST /api/buses` - Crear autobús
- `PUT /api/buses/:id` - Actualizar autobús
- `DELETE /api/buses/:id` - Eliminar autobús

#### Usuarios
- `GET /api/users` - Listar usuarios
- `POST /api/users` - Crear usuario
- `PUT /api/users/:id` - Actualizar usuario
- `DELETE /api/users/:id` - Eliminar usuario

#### Estudiantes
- `GET /api/students` - Listar estudiantes
- `POST /api/students` - Crear estudiante
- `PUT /api/students/:id` - Actualizar estudiante
- `DELETE /api/students/:id` - Eliminar estudiante

#### Rutas
- `GET /api/routes` - Listar rutas
- `POST /api/routes` - Crear ruta
- `PUT /api/routes/:id` - Actualizar ruta
- `DELETE /api/routes/:id` - Eliminar ruta

#### Paradas
- `GET /api/stops` - Listar paradas
- `POST /api/stops` - Crear parada
- `PUT /api/stops/:id` - Actualizar parada
- `DELETE /api/stops/:id` - Eliminar parada

#### Colegios
- `GET /api/colegios` - Listar colegios

## Instrucciones para Ejecutar

### 1. Iniciar los contenedores
```bash
docker compose up -d
```

### 2. Acceder a la aplicación
```
http://localhost:5173
```

### 3. Probar los diferentes roles
- Inicia sesión con las credenciales proporcionadas
- Cada rol verá su panel específico
- Todos los CRUD están operativos

## Problemas Resueltos

### ✅ Error de Login
- **Problema:** Hash de bcrypt incorrecto
- **Solución:** Regenerar hash con bcrypt.hash()

### ✅ Contenedor Frontend Inestable
- **Problema:** ExitCode 126 (error de permisos)
- **Solución:** Actualizar Dockerfile, usar Alpine + npm ci, regenerar package-lock.json

### ✅ Página de Usuarios Rota
- **Problema:** Variable `usuariosFiltrados` no definida
- **Solución:** Crear computed property que filtre usuarios por rol y email

### ✅ URLs de API Malformadas
- **Problema:** StudentManagement, RouteManagement, StopManagement con URLs incorrectas
- **Solución:** Crear utilidad `getApiUrl()` centralizada

### ✅ Componentes sin Inicialización
- **Problema:** BusManagement sin propiedades reactivas
- **Solución:** Agregar refs necesarios (selected, todosSeleccionados)

## Tecnologías Utilizadas

- **Frontend:** Vue 3 + Vite 7.3.0 + Tailwind CSS v4.1.18
- **Backend:** Express.js + Node.js
- **Base de Datos:** MySQL 8.0
- **Mapas:** Leaflet + OpenStreetMap
- **Autenticación:** bcrypt
- **Contenedorización:** Docker Compose

## Próximas Mejoras Sugeridas

1. Implementar socket.io para actualizaciones en tiempo real de ubicación
2. Agregar notificaciones push cuando el autobús llega a la parada
3. Agregar historial de viajes y reportes
4. Implementar suscripción a eventos por rol
5. Agregar fotos de perfil de conductores
6. Sistema de mensajería entre padres y escuela
7. Validación de campos más robusta
8. Temas de color personalizables

## Soporte

Para reportar problemas o sugerencias, contacta con el equipo de desarrollo.

---

**Versión:** 1.0  
**Fecha:** 8 de enero de 2026  
**Estado:** ✅ Completado y Operativo
