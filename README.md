# 🚍 SchoolSafeTrack – TFG-DAM

**Sistema de Transporte Escolar Seguro**

Plataforma integral para el seguimiento en tiempo real del transporte escolar, orientada a garantizar la seguridad de los alumnos y la tranquilidad de las familias.

> 📋 Consulta el archivo [PLAN.md](./PLAN.md) para ver la planificación completa del proyecto.

---

## 🎯 ¿Qué es SchoolSafeTrack?

| Quién | Qué obtiene |
|-------|-------------|
| 👨‍👩‍👦 **Padres** | Ven la ubicación del bus de su hijo en tiempo real y reciben notificaciones push |
| 🚍 **Conductores** | Usan una app móvil para iniciar/finalizar rutas y enviar su posición automáticamente |
| 🏫 **Administración** | Gestionan rutas, paradas, alumnos y conductores desde un panel web con mapas en vivo |

---

## 🧰 Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| 📱 App móvil | Java (Android) |
# SchoolSafeTrack

Sistema de transporte escolar para seguimiento en tiempo real, gestión de rutas y control por roles.

## Resumen General

SchoolSafeTrack organiza el transporte escolar en torno a cuatro perfiles:
- Administración: gestiona colegios, autobuses, rutas, paradas, alumnos y usuarios desde el panel web.
- Conductores: registran su ruta y envían ubicación GPS desde la app Android.
- Padres: consultan el autobús asociado a sus hijos y lo siguen en tiempo real.
- Profesores: acceden a funcionalidades de consulta y apoyo según el flujo definido por el proyecto.

## Cómo Empezar

1. Levanta la solución con Docker:

```bash
# Clona el repositorio
git clone https://github.com/nbch0203/TFG-DAM.git
cd TFG-DAM

# Levanta todos los servicios con Docker
docker compose up -d --build

2. Abre la web en:

```text
http://localhost:5173
```

3. Consulta la documentación detallada en:
- [docs/indice-documentacion.md](docs/indice-documentacion.md)
- [docs/resumen-general.md](docs/resumen-general.md)
- [docs/flujo-sistema.md](docs/flujo-sistema.md)
- [docs/guia-pruebas.md](docs/guia-pruebas.md)
- [docs/panel-administrativo.md](docs/panel-administrativo.md)

## Estructura del proyecto

```text
TFG-DAM/
├── aplicacion-android/   App Android para el conductor
├── backend/              API, lógica de negocio y base de datos
├── frontend-web/         Panel web y vistas para consulta
├── docs/                 Documentación general y técnica
├── Simulacion ubicacion/  Scripts para simular movimiento GPS
├── docker-compose.yml    Arranque conjunto de servicios
└── README.md             Punto de entrada del proyecto
```

## Documentación principal

- [docs/00-guia-lectura.md](docs/00-guia-lectura.md): por dónde empezar según el tipo de usuario.
- [docs/01-vision-general.md](docs/01-vision-general.md): explicación simple del proyecto, objetivos y roles.
- [docs/02-arquitectura.md](docs/02-arquitectura.md): cómo se conecta cada parte del sistema.
- [docs/03-modelo-datos.md](docs/03-modelo-datos.md): tablas principales y relaciones importantes.
- [docs/04-flujos-por-rol.md](docs/04-flujos-por-rol.md): qué hace cada perfil y cómo usa el sistema.
- [docs/guia-pruebas.md](docs/guia-pruebas.md): cómo arrancar y comprobar que todo funciona.

## Documentación por contenedor

- [docs/backend/README.md](docs/backend/README.md): API, rutas, base de datos.
- [docs/backend/api-endpoints.md](docs/backend/api-endpoints.md): lista de endpoints principales.
- [docs/frontend-web/README.md](docs/frontend-web/README.md): panel web, componentes, pantallas.
- [docs/frontend-web/componentes.md](docs/frontend-web/componentes.md): componentes Vue principales.
- [docs/aplicacion-android/README.md](docs/aplicacion-android/README.md): app móvil, estructura, flujos.
- [docs/aplicacion-android/gps-ubicacion.md](docs/aplicacion-android/gps-ubicacion.md): cómo funciona el GPS en la app.

## Usuarios de prueba

El proyecto incluye usuarios de ejemplo para validar los distintos roles. Consulta [docs/guia-pruebas.md](docs/guia-pruebas.md) para ver cómo probar cada perfil.

## Tecnologías

- Backend: Node.js, Express, MySQL2
- Frontend: Vue 3, Vite
- Android: Java según módulo del proyecto
- Mapa: Leaflet y OpenStreetMap
- Contenedores: Docker y Docker Compose
