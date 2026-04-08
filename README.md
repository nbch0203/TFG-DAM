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
| 🖥 Panel web | Vue.js |
| 🔌 Backend | Node.js + Express |
| 💾 Base de datos | MySQL |
| 🔄 Tiempo real | WebSockets (Socket.io) |
| 🗺 Mapas | OpenStreetMap (Leaflet / osmdroid) |
| 📢 Notificaciones | Firebase Cloud Messaging (FCM) |
| 🔐 Auth | JWT |

---

## 🚀 Estado del Proyecto

> En fase de planificación. Consulta [PLAN.md](./PLAN.md) para ver las fases de desarrollo y el progreso actual.

---

## 📁 Estructura del Repositorio

```
TFG-DAM/
├── backend/      # API REST + WebSockets (Node.js)
├── web/          # Panel de administración (Vue.js)
├── android/      # Apps móviles padre y conductor (Java)
├── docs/         # Diagramas, wireframes, ER
├── docker-compose.yml
├── PLAN.md
└── README.md
```