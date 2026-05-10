# Guía de desarrollo

Esta guía resume cómo empezar a trabajar en el proyecto sin perderse en la estructura del repositorio.

## Antes de empezar

Necesitas tener instalado lo básico para ejecutar el proyecto con Docker y trabajar con el código del backend y del frontend.

## Estructura de trabajo

- `backend/`: API y lógica de negocio.
- `frontend-web/`: panel web.
- `aplicacion-android/`: aplicación móvil.
- `docs/`: documentación del proyecto.

## Cómo arrancar el proyecto

1. Levanta los servicios:

```bash
docker compose up -d --build
```

2. Abre la web:

```text
https://SCHOOLSAFETRACK.WORK.GD
```

3. Comprueba el backend si hace falta.

## Qué tocar según lo que quieras cambiar

- Si cambias **lógica de negocio, API o base de datos**, revisa:
  - Carpeta: `backend/`
  - Documentación: [backend/README.md](backend/README.md)

- Si cambias **pantallas, formularios o visualización**, revisa:
  - Carpeta: `frontend-web/`
  - Documentación: [frontend-web/README.md](frontend-web/README.md)

- Si cambias **app móvil, GPS o ubicación**, revisa:
  - Carpeta: `aplicacion-android/`
  - Documentación: [aplicacion-android/README.md](aplicacion-android/README.md)

- Si cambias **relaciones o datos**, revisa:
  - Documentación: [03-modelo-datos.md](03-modelo-datos.md)
  - Carpeta: `backend/` (modelos y SQL)

## Recomendaciones básicas

- Mantén los nombres claros y consistentes.
- Documenta cualquier cambio funcional importante.
- No dupliques información que ya exista en otro documento.

## Pendiente de completar

- Añadir comandos concretos de instalación por módulo si se decide trabajar fuera de Docker.
- Documentar variables de entorno finales cuando estén cerradas.