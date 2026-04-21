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
docker compose up -d --build
```

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

## Estructura del Proyecto

```text
TFG-DAM/
├── aplicacion-android/     App Android para conductor y seguimiento
├── backend/                API Express, lógica de negocio y SQL
├── frontend-web/           Panel web de administración y padres
├── docs/                   Documentación por secciones del proyecto
├── Simulacion ubicacion/    Scripts de simulación GPS
├── docker-compose.yml      Orquestación de servicios
└── README.md               Índice general del proyecto
```

## Documentación Principal

- [docs/indice-documentacion.md](docs/indice-documentacion.md): índice de la documentación interna.
- [docs/resumen-general.md](docs/resumen-general.md): visión general del sistema y módulos.
- [docs/flujo-sistema.md](docs/flujo-sistema.md): flujo entre login, roles, backend y mapas.
- [docs/guia-pruebas.md](docs/guia-pruebas.md): credenciales, pruebas y verificación.
- [docs/panel-administrativo.md](docs/panel-administrativo.md): detalle técnico de la gestión administrativa.

## Usuarios de Prueba

El proyecto incluye usuarios de ejemplo para validar los distintos roles. Revisa [docs/guia-pruebas.md](docs/guia-pruebas.md) para ver las credenciales actualizadas y el alcance de cada perfil.

## Tecnologías

- Backend: Node.js, Express, MySQL2
- Frontend: Vue 3, Vite
- Android: Java según módulo del proyecto
- Mapa: Leaflet y OpenStreetMap
- Contenedores: Docker y Docker Compose
