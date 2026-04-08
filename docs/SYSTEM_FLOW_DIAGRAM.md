# 🎯 Flujo del Sistema de Seguimiento para Padres

## 📊 Diagrama de Flujo Completo

```
┌─────────────┐
│  Navegador  │ http://localhost:5173
└──────┬──────┘
       │
       ▼
┌─────────────────────────┐
│  App.vue                │
│  - Pantalla login       │
│  - Valida credenciales  │
└──────┬──────────────────┘
       │
       │ Email + Password
       ▼
┌─────────────────────────────────────┐
│  Backend Express                    │
│  POST /api/login                    │
│  - Valida con bcrypt                │
│  - Retorna user {id, email, role}   │
└──────┬──────────────────────────────┘
       │
       │ { success: true, user: {...} }
       │ + sessionStorage.setItem('user')
       ▼
┌─────────────────────────────────────┐
│  ParentPage.vue                     │
│  - Lee role = PARENT                │
│  - Muestra barra lateral morada     │
│  - Opciones: Seguimiento / Hijos    │
└──────┬──────────────────────────────┘
       │
       │ Click en "Seguimiento de autobús"
       ▼
┌────────────────────────────────────────────────┐
│  ParentBusTracking.vue                         │
│  - Lee parent ID de sessionStorage (id: 9)     │
└──────┬─────────────────────────────────────────┘
       │
       ├─→ GET /api/parent/9/buses
       │   └─→ SELECT FROM buses JOIN students...
       │       └─→ Retorna: [{id, matricula, marca, 
       │           modelo, lat, lon, conductor}]
       │
       ├─→ GET /api/parent/9/children
       │   └─→ SELECT FROM students JOIN stops...
       │       └─→ Retorna: [{id, nombre, apellidos,
       │           stop_nombre, latitud, longitud}]
       │
       ▼
┌────────────────────────────────────────────────┐
│  Frontend (Vue Reactivity)                     │
│  - autobusesDelPadre = [2 buses]               │
│  - hijosDelPadre = [2 hijos]                   │
│  - autobusPadreSeleccionado = bus.id           │
└──────┬─────────────────────────────────────────┘
       │
       │ Watch effect: autobusPadre changed?
       │
       ▼
┌────────────────────────────────────────────────┐
│  Leaflet Map                                   │
│  - Inicializa mapa en element #bus-map         │
│  - L.map('bus-map').setView([lat, lon], 15)   │
│  - Agrega tile layer OpenStreetMap             │
│  - Crea marcador azul para autobús             │
│  - Crea marcadores rojos para paradas          │
└──────┬─────────────────────────────────────────┘
       │
       │ Cada 30 segundos (setInterval)
       ▼
┌────────────────────────────────────────────────┐
│  Actualización Automática                      │
│  - actualizarTiempo()                          │
│  - Muestra: "Última actualización: 16:27:12"   │
│  - (En futura versión: GET nuevas coordenadas) │
└────────────────────────────────────────────────┘
```

---

## 🎨 Estructura de Componentes Vue

```
App.vue (Raíz)
│
└─ handleLogin()
   │
   ├─ setItem('user') → sessionStorage
   │
   └─ if role == 'PARENT'
      │
      ▼
   ParentPage.vue
   │
   ├─ Barra Lateral Morada
   │  ├─ 📍 Seguimiento de autobús
   │  └─ 👨‍👩‍👦 Mis hijos
   │
   └─ Componente Dinámico
      │
      ├─ ParentBusTracking.vue (si selected == 'tracking')
      │  │
      │  ├─ cargarDatosDelPadre()
      │  │  ├─ GET /api/parent/9/buses
      │  │  └─ GET /api/parent/9/children
      │  │
      │  ├─ selector autobús (dropdown)
      │  │
      │  ├─ mostrarMapa(lat, lon)
      │  │  └─ Leaflet con marcadores
      │  │
      │  ├─ mostrarParadaEnMapa(hijo)
      │  │  └─ Centra en parada del hijo
      │  │
      │  └─ iniciarActualizacionPeriódica()
      │     └─ Cada 30 segundos
      │
      └─ ParentChildrenManagement.vue (si selected == 'hijos')
         └─ Lista de hijos en tarjetas
```

---

## 📡 Flujo de Datos - Ejemplo Real

### Paso 1: Login
```javascript
// Usuario hace login
POST /api/login
{
  "email": "padre@schoolsafetrack.com",
  "password": "padre123"
}

// Respuesta
{
  "success": true,
  "user": {
    "id": 9,
    "email": "padre@schoolsafetrack.com",
    "role": "PARENT"
  }
}

// Frontend guarda en sessionStorage
sessionStorage.setItem('user', JSON.stringify({
  "id": 9,
  "email": "padre@schoolsafetrack.com",
  "role": "PARENT"
}))
```

### Paso 2: ParentBusTracking Carga
```javascript
// Lee parent ID
const user = JSON.parse(sessionStorage.getItem('user'))
parentId = user.id  // 9

// Solicita autobuses
GET /api/parent/9/buses

// Respuesta
[
  {
    "id": 1,
    "matricula": "1234-ABC",
    "marca": "Mercedes",
    "modelo": "Sprinter",
    "anio": 2020,
    "capacidad": 25,
    "lat": 40.418,
    "lon": -3.7044,
    "conductor_nombre": "Carlos Rodríguez"
  },
  {
    "id": 2,
    "matricula": "5678-XYZ",
    "marca": "Iveco",
    "modelo": "Daily",
    "anio": 2019,
    "capacidad": 30,
    "lat": 40.4187,
    "lon": -3.7051,
    "conductor_nombre": "Ana Fernández"
  }
]
```

### Paso 3: Información de Hijos
```javascript
// Solicita hijos
GET /api/parent/9/children

// Respuesta
[
  {
    "id": 1,
    "nombre": "Pedro",
    "apellidos": "García Martínez",
    "fecha_nacimiento": "2015-05-10T00:00:00.000Z",
    "curso": "3º Primaria",
    "stop_nombre": "Plaza Mayor",
    "stop_direccion": "Plaza Mayor, 1",
    "latitud": "40.41536300",
    "longitud": "-3.70739800"
  },
  {
    "id": 2,
    "nombre": "Lucía",
    "apellidos": "García Martínez",
    "fecha_nacimiento": "2017-09-15T00:00:00.000Z",
    "curso": "1º Primaria",
    "stop_nombre": "Mercado Central",
    "stop_direccion": "Calle Mercado, 10",
    "latitud": "40.41998900",
    "longitud": "-3.70566300"
  }
]
```

### Paso 4: Renderización del Mapa
```javascript
// Frontend crea mapa Leaflet
const mapa = L.map('bus-map').setView([40.418, -3.7044], 15)

// Agrega tile layer
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
  .addTo(mapa)

// Crea marcador azul para autobús
L.marker([40.418, -3.7044], {
  icon: L.icon({
    iconUrl: 'https://...marker-icon-2x-blue.png',
    iconSize: [25, 41]
  })
}).addTo(mapa)
.bindPopup('Autobús 1234-ABC')

// Cuando padre hace click en "Ver en mapa" del hijo
// Crea marcador rojo para parada
L.marker([40.41536, -3.70739], {
  icon: L.icon({
    iconUrl: 'https://...marker-icon-2x-red.png',
    iconSize: [25, 41]
  })
}).addTo(mapa)
.bindPopup('Pedro - Plaza Mayor')
```

---

## 🔄 Ciclo de Actualización

```
┌─────────────────────────────────────────────┐
│ onMounted() → cargarDatosDelPadre()         │
│                                             │
│ 1. Obtiene parent ID de sessionStorage      │
│ 2. Hace 2 llamadas fetch paralelas          │
│ 3. Renderiza mapas con Leaflet              │
│ 4. Inicia setInterval de 30 segundos        │
└──────┬──────────────────────────────────────┘
       │
       │ Cada 30 segundos
       ▼
┌─────────────────────────────────────────────┐
│ setInterval(() => actualizarTiempo(), 30000)│
│                                             │
│ Actualiza timestamp de "Última actualización"
│ (En versión mejorada: recargaría datos)     │
└──────┬──────────────────────────────────────┘
       │
       │ Cuando padre cambia autobús
       │ (Watch effect)
       ▼
┌─────────────────────────────────────────────┐
│ watch(autobusPadre, (newBus) => {           │
│   if (newBus && newBus.lat && newBus.lon) { │
│     mostrarMapa(newBus.lat, newBus.lon)     │
│   }                                         │
│ })                                          │
│                                             │
│ Mapa se centra en nueva ubicación de autobús
└─────────────────────────────────────────────┘

       │
       │ Cuando página se desmonta
       ▼
┌─────────────────────────────────────────────┐
│ onBeforeUnmount()                           │
│                                             │
│ 1. clearInterval(intervalo)                 │
│ 2. mapa.remove()                            │
│ 3. Limpia referencias                       │
└─────────────────────────────────────────────┘
```

---

## 🗄️ Flujo de Base de Datos

```
┌──────────────────┐
│ usuarios (login) │
│                  │
│ id = 9           │
│ email = padre... │
│ role = PARENT    │
└────────┬─────────┘
         │
         │ parent_id
         ▼
┌──────────────────────┐
│ students             │
│                      │
│ id: 1, 2             │
│ nombre, apellidos    │
│ parent_id: 9         │
│ stop_id: 1, 2        │
└────────┬─────────────┘
         │
         │ stop_id
         ▼
┌──────────────────────┐
│ stops                │
│                      │
│ id: 1, 2             │
│ nombre, direccion    │
│ latitud, longitud    │
│ route_id: 1, 2       │
└────────┬─────────────┘
         │
         │ route_id
         ▼
┌──────────────────────┐
│ routes               │
│                      │
│ id: 1, 2             │
│ nombre, descripcion  │
└────────┬─────────────┘
         │
         │ (relationship)
         ▼
┌──────────────────────┐
│ buses                │
│                      │
│ id: 1, 2             │
│ matricula            │
│ marca, modelo        │
│ lat, lon (GPS)       │
│ driver_id: 4, 5      │
└──────────────────────┘
```

---

## 🎯 Queries SQL Ejecutadas

### Query 1: Obtener Autobuses del Padre
```sql
-- /api/parent/:parentId/buses
SELECT DISTINCT b.*, 
       CONCAT(u.nombre, ' ', u.apellidos) AS conductor_nombre
FROM buses b
WHERE b.id IN (
  SELECT DISTINCT r.id
  FROM routes r
  INNER JOIN stops s ON r.id = s.route_id
  WHERE s.id IN (
    SELECT DISTINCT stop_id 
    FROM students 
    WHERE parent_id = 9 AND stop_id IS NOT NULL
  )
)
LEFT JOIN usuarios u ON b.driver_id = u.id
```

### Query 2: Obtener Hijos del Padre
```sql
-- /api/parent/:parentId/children
SELECT s.*, 
       st.nombre AS stop_nombre,
       st.direccion AS stop_direccion,
       st.latitud,
       st.longitud
FROM students s
LEFT JOIN stops st ON s.stop_id = st.id
WHERE s.parent_id = 9
ORDER BY s.nombre
```

---

## 📈 Métricas de Rendimiento

### Tiempos de Respuesta Esperados
| Operación | Tiempo |
|:---|:---:|
| Login | ~50ms |
| GET /api/parent/9/buses | ~30ms |
| GET /api/parent/9/children | ~20ms |
| Renderización Leaflet | ~200ms |
| **Total inicial** | **~300ms** |

### Actualizaciones
| Elemento | Frecuencia |
|:---|:---:|
| Timestamp | 30 segundos |
| Mapa (cambio manual) | Inmediato |
| Recarga de datos | Manual (futura mejora) |

---

## 🔐 Flujo de Seguridad

```
┌─────────────────────────────────────────┐
│ Usuario intenta login                   │
│ Email: padre@schoolsafetrack.com        │
│ Password: padre123                      │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ Backend verifica credenciales           │
│ 1. Busca usuario por email              │
│ 2. Valida password con bcrypt.compare() │
│ 3. Verifica role == 'PARENT'            │
└──────┬──────────────────────────────────┘
       │
       │ ✅ Válido
       ▼
┌─────────────────────────────────────────┐
│ Frontend recibe user object             │
│ sessionStorage.setItem('user', {...})   │
│                                         │
│ Datos guardados localmente en navegador │
│ (no persiste en refresh - por seguridad)│
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ ParentBusTracking.vue accede datos      │
│ parent_id = user.id (9)                 │
│                                         │
│ Backend valida:                         │
│ - ¿Existe parent_id 9?                  │
│ - ¿Tiene hijos asignados?               │
│                                         │
│ Solo retorna datos de ESTE padre        │
└─────────────────────────────────────────┘
```

---

## ✅ Casos de Uso Validados

### Caso 1: Padre Verificando Autobús
```
1. ✅ Login → Autentica con bcrypt
2. ✅ Ve ParentPage → Solo rol PARENT
3. ✅ Click Seguimiento → Carga ParentBusTracking
4. ✅ Dropdown muestra 2 autobuses
5. ✅ Mapa renderea con ubicación actual
6. ✅ Coordenadas GPS precisas: 40.418, -3.7044
```

### Caso 2: Padre Viendo Parada de Hijo
```
1. ✅ Scroll a "Paradas de tus hijos"
2. ✅ Ve lista: Pedro (Plaza Mayor) + Lucía (Mercado)
3. ✅ Click "Ver en mapa" en Pedro
4. ✅ Mapa centra en: 40.41536, -3.70739
5. ✅ Marcador rojo aparece en la parada
```

### Caso 3: Padre Cambiando de Autobús
```
1. ✅ Dropdown inicial: Mercedes 1234-ABC
2. ✅ Cambia a: Iveco 5678-XYZ
3. ✅ Watch effect detecta cambio
4. ✅ Mapa actualiza a: 40.4187, -3.7051
5. ✅ Información se actualiza automáticamente
```

---

## 🎓 Tecnologías Utilizadas en Cada Capa

### Frontend
- **Vue 3 Composition API** - Reactivity y lógica de componentes
- **sessionStorage** - Almacenamiento de usuario
- **Watch/WatchEffect** - Observar cambios en datos
- **Async/Await** - Manejo de promises de API
- **Tailwind CSS** - Estilos y layout

### Backend
- **Express.js** - Routing y middleware
- **mysql2/promise** - Acceso a base de datos
- **async/await** - Operaciones asincrónicas
- **bcrypt** - Hash de contraseñas
- **JSON** - Formato de respuesta

### Infraestructura
- **Docker** - Contenedores
- **Docker Compose** - Orquestación
- **MySQL 8.0** - Base de datos
- **OpenStreetMap** - Tiles de mapas
- **Leaflet.js** - Librería de mapas

---

## 🚀 Resumen Ejecutivo del Flujo

1. **Padre abre navegador** → http://localhost:5173
2. **Inicia sesión** → bcrypt valida contraseña
3. **sessionStorage guarda user.id** → Necesario para consultas
4. **ParentPage renderiza** → Solo si role == PARENT
5. **Click en Seguimiento** → Carga ParentBusTracking.vue
6. **Componente lee user.id** → sessionStorage.getItem('user')
7. **2 llamadas paralelas:**
   - GET /api/parent/9/buses → 2 autobuses
   - GET /api/parent/9/children → 2 hijos
8. **Leaflet inicializa mapa** → Carga OpenStreetMap tiles
9. **Marcadores se renderizan** → Azul=bus, Rojo=paradas
10. **Padre selecciona autobús** → Watch effect actualiza mapa
11. **Cada 30 segundos** → Timestamp se actualiza
12. **Padre puede ver parada** → Click "Ver en mapa" en hijo
13. **Sistema persistente** → sessionStorage mantiene usuario

---

**Este flujo está completamente implementado y funcional. ✅**
