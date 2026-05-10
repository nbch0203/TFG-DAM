# 🚍 SchoolSafeTrack – TFG-DAM

SchoolSafeTrack es una plataforma de transporte escolar que permite seguir autobuses en tiempo real, coordinar rutas y controlar el servicio por perfiles de usuario. El objetivo es dar tranquilidad a las familias, facilitar el trabajo de la administración y ayudar al conductor a registrar la ruta con menos errores. La información se organiza para que cualquier persona pueda entender el proyecto sin conocer su base técnica.

> 📋 Si quieres ver la planificación completa, consulta [PLAN.md](./PLAN.md).

## Qué resuelve

El proyecto resuelve un problema muy concreto: saber dónde está el autobús escolar, qué ruta está haciendo y cómo se relaciona esa información con los alumnos y las paradas. Así, los padres pueden seguir el trayecto, el conductor puede enviar su ubicación y la administración puede gestionar la operación desde un panel web.

## Funcionalidades principales

| Perfil | Para qué sirve |
|---|---|
| 👨‍👩‍👦 Padres | Ver el autobús asociado a sus hijos en un mapa y recibir avisos del trayecto. |
| 🚍 Conductores | Iniciar y terminar rutas, y enviar la ubicación del autobús de forma automática. |
| 🏫 Administración | Gestionar colegios, rutas, paradas, alumnos, autobuses y usuarios desde el panel web. |
| 👩‍🏫 Profesorado | Consultar información de apoyo relacionada con el flujo del proyecto. |

## Cómo está hecho, a grandes rasgos

- **App Android**: usada por el conductor para registrar la ruta y compartir la ubicación.
- **Frontend web**: panel de administración y vista para familias.
- **Backend**: API que recibe peticiones, aplica reglas de acceso y conecta con la base de datos.
- **Base de datos MySQL**: guarda usuarios, rutas, paradas, alumnos, mensajes y ubicaciones.
- **Mapa en vivo**: muestra el autobús sobre el mapa usando datos del backend.

## Cómo se usa o se ejecuta

1. Inicia los servicios con Docker:

```bash
docker compose up -d --build
```

2. Abre la web en:

```text
https://SCHOOLSAFETRACK.WORK.GD
```

3. Revisa la documentación en este orden:

- [docs/00-guia-lectura.md](docs/00-guia-lectura.md)
- [docs/01-vision-general.md](docs/01-vision-general.md)
- [docs/02-arquitectura.md](docs/02-arquitectura.md)
- [docs/04-flujos-por-rol.md](docs/04-flujos-por-rol.md)
- [docs/guia-pruebas.md](docs/guia-pruebas.md)

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

## Pendiente de completar

- Documentar credenciales definitivas de prueba si cambian respecto al entorno local.
- Confirmar el alcance exacto del perfil de profesorado si se añaden nuevas funciones.
