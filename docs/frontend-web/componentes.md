# Componentes principales - Frontend Web

Los componentes son piezas reutilizables de interfaz que forman las pantallas. Este documento describe los componentes principales.

## Componentes de Layout

### AdminPage.vue
Contenedor principal del panel de administración. Define la estructura general con menú y área de contenido.

**Responsabilidades:**
- Mostrar menú lateral.
- Enrutar entre pantallas administrativas.
- Gestionar sesión.

### ParentPage.vue
Contenedor para padres que desean ver el autobús de sus hijos.

---

## Componentes de Gestión (CRUD)

### BusManagement.vue
Panel para crear, editar y eliminar autobuses.

**Datos principales:**
- Matrícula
- Capacidad
- Estado activo/inactivo

### RouteManagement.vue
Panel para gestionar rutas.

**Datos principales:**
- Nombre de la ruta
- Colegio asociado
- Paradas en orden

### StopManagement.vue
Panel para gestionar paradas. Incluye mapa interactivo para colocar paradas.

**Datos principales:**
- Nombre
- Coordenadas (latitud, longitud)
- Orden dentro de la ruta
- Ruta asociada

### StudentManagement.vue
Panel para registrar alumnos y vincularlos a padres y rutas.

### UserManagement.vue
Panel para crear usuarios y asignar roles.

### SchoolManagement.vue
Panel para gestionar colegios.

---

## Componentes de Visualización

### AdminDashboard.vue
Panel principal con estadísticas:
- Rutas activas
- Autobuses en marcha
- Alumnos registrados
- Mensajes pendientes

### BusTracking.vue
Mapa en vivo que muestra la posición de autobuses activos usando Leaflet.

**Funcionalidades:**
- Mapa interactivo con OpenStreetMap
- Marcadores de autobuses
- Actualización en tiempo real vía WebSocket

### ParentBusTracking.vue
Mapa para padres. Muestra solo el autobús asociado a sus hijos.

---

## Componentes de Datos

### AdminMessages.vue
Panel para gestionar mensajes e incidencias administrativas.

---

## Patrones de comunicación

### Con el backend (HTTP)
```javascript
// Ejemplo desde un componente Vue
import { useApi } from '@/utils/api'

const api = useApi()
const routes = await api.get('/api/routes')
```

### Tiempo real (WebSocket)
```javascript
// Escuchar actualizaciones de ubicación
socket.on('update-map', (busData) => {
  updateBusPosition(busData)
})
```

---

## Pendiente de completar

- Documentar props y eventos de cada componente.
- Describir el estado compartido (Pinia, si se usa).
- Guía de añadir nuevos componentes CRUD.
- Detallar validaciones de formularios.
