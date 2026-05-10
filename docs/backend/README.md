# Backend - SchoolSafeTrack

El backend es el núcleo del sistema. Se encarga de recibir peticiones desde la web y la app móvil, aplicar las reglas de negocio y conectar con la base de datos.

## Qué hace

- **Autenticación**: valida usuarios y genera tokens JWT para acceso seguro.
- **API REST**: expone endpoints para gestionar usuarios, rutas, paradas, alumnos, autobuses, etc.
- **Lógica de negocio**: aplica reglas como qué datos puede ver cada rol, cómo se asocian alumnos a rutas, etc.
- **Base de datos**: almacena y recupera información persistente en MySQL.
- **Tiempo real**: comparte ubicaciones del autobús con las vistas que las necesitan.

## Estructura del código

```text
backend/
├── src/
│   ├── controllers/     Lógica de cada endpoint
│   ├── routes/         Definición de rutas HTTP
│   ├── services/       Servicios y reglas de negocio
│   ├── models/         Consultas a base de datos
│   ├── middlewares/    Validación, autenticación, roles
│   └── sockets/        Comunicación en tiempo real
├── server.js           Entrada principal
├── package.json        Dependencias
└── .env.example        Variables de entorno
```

## Tecnologías

- **Node.js + Express**: servidor web y API REST.
- **MySQL2**: conexión a base de datos.
- **Socket.io**: comunicación en tiempo real.
- **JWT**: autenticación basada en tokens.
- **bcrypt**: hasheado seguro de contraseñas.

## Rutas principales de la API

### Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrarse (si está habilitado)

### Gestión de datos
- `GET /api/users` - Listar usuarios
- `POST /api/users` - Crear usuario
- `GET /api/routes` - Listar rutas
- `POST /api/routes` - Crear ruta
- `GET /api/stops` - Listar paradas
- `GET /api/buses` - Listar autobuses
- `GET /api/students` - Listar alumnos
- `GET /api/schools` - Listar colegios

### Ubicación en tiempo real
- `WebSocket /socket.io` - Canal de ubicación del autobús

## Base de datos

Las tablas principales son:
- `users` - usuarios del sistema.
- `schools` - colegios.
- `students` - alumnos.
- `routes` - rutas.
- `stops` - paradas.
- `buses` - autobuses.
- `route_assignments` - asignación ruta-bus-conductor por día.
- `real_time_location` - ubicaciones del autobús.
- `notifications` - avisos a usuarios.
- `admin_messages` - mensajes administrativos.

Para más detalle, consulta [../03-modelo-datos.md](../03-modelo-datos.md).

## Pendiente de completar

- Documentar endpoints específicos con parámetros y respuestas.
- Detalle de validaciones en cada formulario.
- Instrucciones para configurar variables de entorno (.env).
- Documentación de Socket.io para tiempo real.
