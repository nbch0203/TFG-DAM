# Frontend Web - SchoolSafeTrack

El frontend web es la interfaz visual del panel de administración y consulta. Se usa desde navegador y conecta con el backend para mostrar y gestionar la información.

## Qué hace

- **Panel administrativo**: permite crear y editar rutas, paradas, autobuses, alumnos y usuarios.
- **Mapa en vivo**: muestra la posición actual de los autobuses sobre un mapa.
- **Dashboard**: resumen visual del estado del servicio.
- **Formularios CRUD**: crear, leer, actualizar y eliminar datos maestros.
- **Consulta para familias**: permite a padres ver el autobús de sus hijos.

## Estructura del código

```text
frontend-web/
├── src/
│   ├── AdminDashboard/         Panel principal de administrador
│   ├── AdminPage/              Contenedor principal del admin
│   ├── BusManagement/          Gestión de autobuses
│   ├── UserManagement/         Gestión de usuarios
│   ├── SchoolManagement/       Gestión de colegios
│   ├── StudentManagement/      Gestión de alumnos
│   ├── RouteManagement/        Gestión de rutas
│   ├── StopManagement/         Gestión de paradas
│   ├── AdminMessages/          Mensajes administrativos
│   ├── ParentPage/             Interfaz para padres
│   ├── ParentBusTracking/      Mapa de seguimiento para padres
│   ├── BusTracking/            Mapa en vivo general
│   ├── core/                   Configuración y rutas globales
│   └── utils/                  Funciones auxiliares (API, helpers)
├── public/                      Archivos estáticos
├── package.json                Dependencias
├── vite.config.js              Configuración de bundler
├── tailwind.config.js          Configuración de estilos
└── index.html                  Punto de entrada HTML
```

## Tecnologías

- **Vue 3**: framework de interfaz.
- **Vite**: bundler rápido para desarrollo.
- **Tailwind CSS**: estilos de utilidad.
- **Leaflet.js**: mapas interactivos.
- **OpenStreetMap**: datos de mapas libres.
- **Socket.io (cliente)**: escucha actualizaciones en tiempo real.

## Pantallas principales

### Administración
- **AdminPage**: menú principal del administrador.
- **AdminDashboard**: estadísticas y estado general.
- **RouteManagement**: crear y editar rutas.
- **StopManagement**: gestionar paradas en un mapa.
- **BusManagement**: registrar autobuses.
- **UserManagement**: crear usuarios y asignar roles.
- **StudentManagement**: vincularse alumnos a padres y rutas.
- **SchoolManagement**: gestionar colegios.
- **AdminMessages**: mensajes e incidencias.

### Padres y consulta
- **ParentPage**: entrada para padres.
- **ParentBusTracking**: mapa con el autobús del hijo.
- **BusTracking**: mapa general de autobuses activos.

## Cómo funciona la conexión con el backend

1. El usuario abre la web en navegador.
2. Vue carga la aplicación y establece una sesión.
3. Al iniciar sesión, el backend devuelve un token JWT.
4. Las peticiones HTTP incluyen el token en las cabeceras.
5. Para el mapa en vivo, se abre un WebSocket que actualiza posiciones en tiempo real.

## Pendiente de completar

- Detallar componentes reutilizables.
- Documentar el estado global (si usa Pinia o similar).
- Instrucciones para desarrolladores locales.
- Guía de añadir nuevas pantallas CRUD.
