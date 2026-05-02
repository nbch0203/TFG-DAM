# Estado Actual de la Aplicación - Resumen para dos personas

**Fecha:** 2 de Mayo de 2026
**Versión:** 14168e3 (Fix API RDS)

Este documento resume el estado operativo y las acciones ya realizadas. Está escrito para un equipo pequeño (tú y tu compañero).

## Resumen rápido

- Aplicación: OPERATIVA
- Backend: Node.js + Express conectando a RDS MySQL (SSL habilitado)
- Frontend: Vue 3 + Vite, dashboard muestra datos
- Nginx: Reverse proxy y SSL configurados

## Cambios clave realizados

- Corregido `VITE_API_URL` para usar ruta relativa `/api`.
- Normalizada la construcción de URLs en `frontend-web/src/utils/api.js`.
- Verificada la conexión con RDS y persistencia de datos.

## Validaciones realizadas (resumen)

- GET /api/buses → 200 OK
- GET /api/users → 200 OK
- POST /api/schools → 201 Created (persistencia verificada)
- Healthcheck → `{ status: 'ok', db: 'connected' }`

## Operaciones frecuentes

- Reiniciar servicios:
```
docker compose up --build -d
docker compose logs -f
```

- Ver health:
```
curl https://your-domain/health
```

## Responsable

Este proyecto lo mantienen **tú y tu compañero**; todas las decisiones y merges los gestionan ustedes dos.

**Última actualización:** 2 de Mayo de 2026