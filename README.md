# 🚍 SchoolSafeTrack – TFG-DAM

**Sistema de Transporte Escolar Seguro**

Plataforma integral para el seguimiento en tiempo real del transporte escolar, orientada a garantizar la seguridad de los alumnos y la tranquilidad de las familias.

🟢 **ESTADO:** Operativo y en producción | ✅ **DOCUMENTACIÓN:** Actualizada al 2 de mayo de 2026

---

## 📌 Documentación Rápida

- **[📚 DOCUMENTACION_ACTUALIZADA.md](DOCUMENTACION_ACTUALIZADA.md)** – Guía general con índice y guías por rol
- **[🌳 GIT_STRUCTURE.md](GIT_STRUCTURE.md)** – Estado de todas las ramas del repositorio
- **[📊 APPLICATION_STATUS.md](APPLICATION_STATUS.md)** – Estado actual de la aplicación y validaciones
- **[🔌 docs/endpoints-api.md](docs/endpoints-api.md)** – Lista completa de endpoints del backend
- **[📋 PLAN.md](PLAN.md)** – Planificación del proyecto y fases de desarrollo

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

## 🚀 Cómo Empezar

### En Desarrollo Local

```bash
# Clona el repositorio
git clone https://github.com/nbch0203/TFG-DAM.git
cd TFG-DAM

# Levanta todos los servicios con Docker
docker compose up -d --build

# La web estará disponible en:
http://localhost:5173
```

### En Producción (AWS EC2 + RDS)

```bash
# En el servidor EC2
git checkout main
git pull origin main

# Reconstruir y recargar servicios
docker compose up --build -d
docker compose logs -f
```

**Endpoints disponibles:**
- 🌐 Web: `https://schoolsafetrack.work.gd` (HTTPS)
- 🔌 API: `https://schoolsafetrack.work.gd/api`
- 🏥 Health: `https://schoolsafetrack.work.gd/health`

### Documentación Detallada

1. **Empezar aquí:** [DOCUMENTACION_ACTUALIZADA.md](DOCUMENTACION_ACTUALIZADA.md) (guía por rol)
2. **Ver endpoints:** [docs/endpoints-api.md](docs/endpoints-api.md) (referencia API)
3. **Arquitectura:** [APPLICATION_STATUS.md](APPLICATION_STATUS.md) (diagrama y flujos)
4. **Ramas Git:** [GIT_STRUCTURE.md](GIT_STRUCTURE.md) (estado de ramas)
5. **Planificación:** [PLAN.md](PLAN.md) (fases del proyecto)
6. **Otros:**
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

## 🧰 Tecnologías

| Capa | Stack |
|------|-------|
| **Frontend Web** | Vue 3 + Vite + Tailwind CSS |
| **Backend** | Node.js + Express.js + MySQL2 |
| **Base de Datos** | AWS RDS MySQL 8.0 |
| **Mapas** | Leaflet + OpenStreetMap |
| **Autenticación** | JWT + bcrypt |
| **Reverse Proxy** | Nginx (Alpine) |
| **Contenedores** | Docker + Docker Compose |
| **SSL/TLS** | Let's Encrypt (schoolsafetrack.work.gd) |
| **App Móvil** | Java (Android) - En desarrollo |

## 📊 Estado del Proyecto

### Componentes Operativos ✅

```
✅ Frontend Web        - Dashboard con Vue 3
✅ Backend REST API    - 60+ endpoints documentados
✅ Base de Datos RDS   - MySQL operativo
✅ Nginx Reverse Proxy - Enrutamiento HTTPS
✅ Autenticación       - Login con JWT
✅ Seguridad           - Contraseñas hasheadas, SSL/TLS
⏳ App Móvil Android  - En desarrollo
```

### Validaciones Completadas ✅

- ✅ Todos los endpoints GET/POST funcionan correctamente
- ✅ RDS conectado y datos persistentes
- ✅ Dashboard carga datos desde la BD
- ✅ Autenticación de usuarios operativa
- ✅ CRUD de colegios, rutas, paradas, estudiantes, usuarios
- ✅ Seguimiento en tiempo real de ubicación de buses
- ✅ Sistema de mensajes entre usuarios
- ✅ Documentación de endpoints completa

### Datos en Base de Datos

```
📊 Colegios:     1+ registros
🚍 Autobuses:    3+ registros
👥 Usuarios:     6+ registros
📚 Estudiantes:  5+ registros
🛣️ Rutas:        4+ registros
🚏 Paradas:      12+ registros
```
