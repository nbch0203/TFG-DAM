# 📋 Plan de Desarrollo – SchoolSafeTrack (TFG-DAM)

> Documento de planificación elaborado con GitHub Copilot para el Trabajo de Fin de Grado de DAM (2.º año).

---

## 🎯 Descripción del Proyecto

**SchoolSafeTrack** es una plataforma integral de seguimiento de transporte escolar en tiempo real.  
Permite a **padres**, **conductores** y **administración del colegio** coordinarse para garantizar la seguridad de los alumnos durante sus trayectos.

| Quién | Qué obtiene |
|-------|-------------|
| 👨‍👩‍👦 Padres | Ver la ubicación del bus de su hijo en tiempo real y recibir notificaciones |
| 🚍 Conductores | App móvil para iniciar/finalizar rutas y enviar su posición en segundo plano |
| 🏫 Administración | Panel web para gestionar rutas, paradas, alumnos y conductores |

---

## 👥 Roles del Sistema

### 👨‍👩‍👦 Padre / Tutor
- Ver ubicación del bus en tiempo real en un mapa
- Recibir notificaciones push:
  - *"El bus está a 5 minutos de tu parada"*
  - *"El bus llegó al colegio"*
- Consultar horarios y rutas
- Gestionar la lista de hijos vinculados

### 🚍 Conductor
- Login específico de conductor
- Botón **Iniciar ruta / Pausar / Finalizar ruta**
- Envío automático de ubicación en segundo plano cada X segundos
- Vista de paradas del día con orden de recogida

### 🏫 Administración del Colegio (web)
- Crear y editar rutas y paradas
- Asignar buses y conductores a rutas
- Registrar y gestionar alumnos
- Mapa en tiempo real con todos los buses activos
- Dashboard: rutas activas, tiempo promedio de recorrido, incidencias

---

## 🧰 Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| 📱 App móvil (padres + conductor) | Java (Android) |
| 🖥 Web (administración) | Vue.js |
| 🔌 Backend | Node.js + Express |
| 💾 Base de datos | MySQL |
| 🔄 Tiempo real | WebSockets (Socket.io) |
| 🔐 Autenticación | JWT |
| 🗺 Mapas | OpenStreetMap (Leaflet.js / osmdroid) |
| 📢 Notificaciones push | Firebase Cloud Messaging (FCM) |
---

## 💾 Modelo de Base de Datos

### Tablas principales

```
users            → id, nombre, email, password_hash, rol (padre/conductor/admin), created_at
students         → id, nombre, apellidos, user_id (padre), route_id, stop_id
routes           → id, nombre, descripcion, activa, colegio_id
stops            → id, nombre, latitud, longitud, orden, route_id
buses            → id, matricula, capacidad, conductor_id
route_assignments→ id, route_id, bus_id, conductor_id, fecha
real_time_location → id, bus_id, latitud, longitud, velocidad, timestamp
notifications    → id, user_id, mensaje, tipo, leida, created_at
```

<!--### Diagrama ER (pendiente de crear en draw.io / dbdiagram.io)
> Añadir enlace al diagrama aquí una vez creado.
-->
---

## 🗂️ Fases de Desarrollo

### ✅ Fase 0 – Definición y Diseño (semana 1-2)
- [x] Definir el proyecto y sus roles
- [x] Elegir el stack tecnológico
- [x] Crear wireframes de la app móvil (Figma)
- [x] Crear wireframes del panel web (Figma)
- [x] Diseñar el diagrama ER de la base de datos (dbdiagram.io)
- [x] Definir la estructura de repositorios/carpetas

### ✅ Fase 1 – Configuración del Entorno (semana 2-3) **COMPLETADA**
- [x] Crear la estructura de carpetas del monorepo (`/backend`, `/web`, `/android`)
- [x] Inicializar el proyecto Node.js (`npm init`) con Express y Socket.io
- [x] Inicializar el proyecto Vue.js (`npm create vue@latest`)
- [x] Crear el proyecto Android en Android Studio
- [x] Configurar `.gitignore` para Node, Vue y Android
- [x] Crear `docker-compose.yml` con MySQL para desarrollo local
- [x] Configurar ESLint + Prettier en backend y web
- [ ] Añadir GitHub Actions: CI básico (build en cada push)

### ✅ Fase 2 – Backend: API REST + WebSockets (semana 3-7) **COMPLETADA**
- [x] Crear la base de datos MySQL con todas las tablas
- [x] Configurar variables de entorno (`.env`) y conexión a BD
- [x] Implementar autenticación: registro, login, JWT, middleware de roles
- [x] CRUD de usuarios, alumnos, rutas, paradas, buses
- [x] Endpoint de asignación de rutas a conductores
- [ ] WebSocket: canal de ubicación en tiempo real por bus
- [x] Endpoint para recibir y almacenar ubicación del conductor
- [ ] Lógica de cálculo de ETA (distancia + velocidad promedio)
- [ ] Integración con FCM para notificaciones push
- [x] Documentar la API (60+ endpoints en docs/endpoints-api.md)

### 🟡 Fase 3 – App Android – Conductor (semana 4-7) **EN PROGRESO**
- [ ] Pantalla de login (JWT)
- [ ] Pantalla principal: botón Iniciar / Pausar / Finalizar ruta
- [ ] Servicio en segundo plano para enviar ubicación cada X segundos
- [ ] Vista de paradas del día con orden
- [ ] Persistencia local para modo offline (Room)
- [ ] Sincronización al recuperar conexión

### 🟡 Fase 4 – App Android – Padre (semana 5-9) **EN PROGRESO**
- [ ] Pantalla de login / registro
- [ ] Lista de hijos vinculados
- [ ] Mapa con ubicación en tiempo real del bus (osmdroid + WebSocket)
- [ ] Indicador de ETA en el mapa
- [ ] Pantalla de horarios y rutas
- [ ] Recepción de notificaciones push (FCM)
- [ ] Historial de últimas rutas

### ✅ Fase 5 – Panel Web – Administración (semana 5-9) **COMPLETADA**
- [x] Login de administrador
- [x] Dashboard: rutas activas, buses en marcha, estadísticas básicas
- [x] Gestión de rutas (CRUD con mapa para trazar paradas)
- [x] Gestión de paradas (añadir en mapa con Leaflet.js)
- [x] Gestión de alumnos y vinculación con padres
- [x] Gestión de conductores y buses
- [x] Mapa en tiempo real con todos los buses activos
- [ ] Alertas de desvío de ruta (opcional avanzado)
- [ ] Exportación de informes a PDF/Excel (opcional avanzado)

### ✅ Fase 6 – Testing y Calidad (semana 8-10) **COMPLETADA**
- [x] Tests manuales de flujo completo: validado todas endpoints
- [x] Pruebas de conectividad RDS y autenticación
- [x] Pruebas de CRUD en todas las entidades
- [ ] Tests unitarios de los servicios del backend (Jest)
- [ ] Tests de integración de endpoints clave (supertest)
- [ ] Pruebas de notificaciones push (FCM)

### ✅ Fase 7 – Despliegue y Documentación (semana 10-12) **EN PRODUCCIÓN**
- [x] Desplegar backend + RDS en AWS EC2 + AWS RDS
- [x] Desplegar panel web en Nginx (EC2)
- [ ] Generar APK firmado para la app Android
- [x] Documentar la API (60+ endpoints en docs/endpoints-api.md)
- [x] README actualizado con instrucciones y arquitectura
- [ ] Preparar la memoria del TFG

---

## 🏗️ Estructura del Repositorio

```
TFG-DAM/
├── backend/              # Node.js + Express + Socket.io
│   ├── src/
│   │   ├── controllers/
│   │   ├── routes/
│   │   ├── services/
│   │   ├── models/       # Consultas MySQL
│   │   ├── middlewares/  # Auth JWT, roles
│   │   └── sockets/      # Lógica WebSocket
│   ├── .env.example
│   └── package.json
├── web/                  # Vue.js (panel administración)
│   ├── src/
│   │   ├── views/
│   │   ├── components/
│   │   ├── stores/       # Pinia
│   │   └── router/
│   └── package.json
├── android/              # Android Studio (Java)
│   ├── app/
│   │   └── src/main/java/com/schoolsafetrack/
│   │       ├── ui/
│   │       ├── services/
│   │       ├── data/
│   │       └── utils/
│   └── build.gradle
├── docs/                 # Diagramas, wireframes, ER
├── docker-compose.yml
├── PLAN.md
└── README.md
```

---

## 🔒 Buenas Prácticas

### Control de versiones
- Ramas por funcionalidad: `feature/nombre-feature`, `fix/nombre-bug`
- Nunca hacer commits directamente en `main`
- Mensajes de commit descriptivos ([Conventional Commits](https://www.conventionalcommits.org/es/))
- Pull Requests con revisión antes de mergear

### Arquitectura
- Backend en capas: `routes → controllers → services → models`
- App Android con patrón MVVM (ViewModel + LiveData/Repository)
- Vue.js con Pinia para gestión del estado

### Seguridad
- Nunca subir credenciales al repositorio — usar `.env` (incluido en `.gitignore`)
- Contraseñas hasheadas con **bcrypt**
- Validar y sanitizar todos los datos de entrada
- Middleware de autenticación JWT en rutas protegidas
- Roles verificados en el backend (no solo en el frontend)

### Calidad del código
- ESLint + Prettier en backend y web
- Al menos tests unitarios de los servicios principales
- Manejo de errores centralizado en el backend (middleware de errores)
- Logging básico con `morgan` (HTTP) y `winston` (errores)

---

## 📈 Características Avanzadas (Opcionales)

| Característica | Descripción | Dificultad |
|----------------|-------------|-----------|
| **ETA dinámico** | Calcular tiempo estimado con distancia + velocidad real | Media |
| **Alertas de desvío** | Detectar si el bus se sale de la ruta y alertar al panel | Media-Alta |
| **Modo offline conductor** | Room para almacenar ubicaciones y sincronizar al reconectar | Media |
| **Exportación de informes** | PDF/Excel con tiempos de llegada y retrasos | Media |
| **Superadmin multi-colegio** | Panel para gestionar varios colegios (arquitectura SaaS) | Alta |

---

## 📅 Timeline

```
Semana 1-2:   Fase 0 – Diseño y planificación
Semana 2-3:   Fase 1 – Configuración del entorno
Semana 3-7:   Fase 2 – Backend (API + WebSockets)
Semana 4-7:   Fase 3 – App Android Conductor
Semana 5-9:   Fase 4 – App Android Padre
Semana 5-9:   Fase 5 – Panel Web Administración
Semana 8-10:  Fase 6 – Testing
Semana 10-12: Fase 7 – Despliegue y documentación
```

---

## 🔗 Recursos Útiles

- [Conventional Commits](https://www.conventionalcommits.org/es/)
- [Socket.io – Docs](https://socket.io/docs/v4/)
- [Leaflet.js – Mapas web](https://leafletjs.com/)
- [osmdroid – Mapas Android](https://github.com/osmdroid/osmdroid)
- [Firebase FCM – Notificaciones](https://firebase.google.com/docs/cloud-messaging)
- [dbdiagram.io – Diagrama ER online](https://dbdiagram.io/)
- [Swagger – Documentar APIs](https://swagger.io/)
- [Railway – Despliegue gratuito](https://railway.app/)
- [Figma – Wireframes](https://www.figma.com/)
- [GitHub Actions – Quickstart](https://docs.github.com/es/actions/quickstart)
