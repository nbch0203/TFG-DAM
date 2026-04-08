# SchoolSafeTrack

Sistema de seguimiento y gestión de rutas escolares.

## Requisitos Previos

- Docker Desktop instalado
- Docker Compose v2.0 o superior
- Puertos disponibles: 3000 (backend), 3306 (mysql), 5173 (frontend)

## Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd TFG-DAM
```

### 2. Configurar variables de entorno (Infraestructura)

Desde la raiz del proyecto, crea tu archivo `.env` a partir de la plantilla:

```bash
cp .env.example .env
```

Variables clave:
- `MYSQL_ROOT_PASSWORD`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `BACKEND_DB_HOST`, `BACKEND_DB_PORT`, `BACKEND_DB_USER`, `BACKEND_DB_PASSWORD`, `BACKEND_DB_NAME`, `BACKEND_PORT`
- `FRONTEND_VITE_API_URL`

Si no personalizas valores, `docker-compose.yml` usa defaults para desarrollo local.

### 3. Lanzar los contenedores

Desde la raíz del proyecto:

```bash
docker-compose up -d --build
```

Este comando:
- Construye las imágenes de Docker para backend y frontend
- Descarga la imagen de MySQL 8.0
- Crea y configura la base de datos con datos iniciales
- Inicia los tres servicios (MySQL, Backend, Frontend)

### 4. Verificar que los servicios están corriendo

**Windows (PowerShell):**
```powershell
.\verify.ps1
```

**Linux/Mac:**
```bash
bash verify.sh
```

O manualmente:
```bash
docker-compose ps
```

Comprobacion de salud del backend:
```bash
curl http://localhost:3000/health
```

Deberías ver tres contenedores en estado "running":
- `mysql_db` - Base de datos MySQL
- `backend` - API Node.js/Express
- `frontend` - Aplicación Vue.js con Vite

### 5. Acceder a la aplicación

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:3000/api
- **MySQL**: localhost:3306 (usuario: `tfg_user`, password: `tfg_password`)

## Usuarios de Prueba

El sistema incluye usuarios predefinidos para pruebas:

- **Administrador**: admin@schoolsafetrack.com / admin123
- **Padre 1**: padre1@example.com / password123
- **Padre 2**: padre2@example.com / password123
- **Conductor 1**: conductor1@example.com / password123
- **Conductor 2**: conductor2@example.com / password123

## Detener los Servicios

```bash
docker-compose down
```

Para eliminar también los volúmenes de datos (base de datos):

```bash
docker-compose down -v
```

## Estructura del Proyecto

```
TFG-DAM/
├── aplicacion-android/     # Aplicación móvil Android
├── backend/                # API Node.js/Express
│   ├── sql/               # Scripts de base de datos
│   │   └── backup.sql     # Esquema e datos iniciales
│   ├── Dockerfile
│   ├── package.json
│   └── server.js
├── frontend-web/           # Aplicación web Vue.js
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── docs/                   # Documentación
├── Simulacion ubicacion/  # Scripts de simulación
└── docker-compose.yml     # Orquestación de contenedores
```

## Solución de Problemas

### El backend se reinicia continuamente

Verifica los logs:
```bash
docker logs backend
```

### La base de datos no tiene tablas

Asegúrate de eliminar los volúmenes y recrear:
```bash
docker-compose down -v
docker-compose up -d --build
```

### Error de conexión a la base de datos

El backend espera a que MySQL esté "healthy" antes de iniciar. Si el problema persiste:
```bash
docker logs mysql_db
docker logs backend
```

## Desarrollo

Los cambios en el código se reflejan automáticamente:

- **Backend**: Los archivos en `./backend` están montados en el contenedor. Reinicia el servicio para aplicar cambios:
  ```bash
  docker-compose restart backend
  ```

- **Frontend**: Los cambios se detectan automáticamente gracias al hot-reload de Vite.

## Tecnologías

- **Backend**: Node.js 20, Express, MySQL2
- **Frontend**: Vue.js 3, Vite, Tailwind CSS
- **Base de Datos**: MySQL 8.0
- **Containerización**: Docker, Docker Compose
- **Móvil**: Android (Kotlin)
