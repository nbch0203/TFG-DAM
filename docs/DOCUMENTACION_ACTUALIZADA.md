# Documentación actualizada (versión simplificada)

**Última actualización:** 2 de Mayo de 2026

Esta documentación está pensada para un equipo pequeño (tú y tu compañero). Evita referencias a grandes equipos o roles múltiples; incluye lo esencial para continuar desarrollo y despliegue.

## Contenido rápido

- `docs/endpoints-api.md` → Referencia de endpoints del backend (útil para desarrollar e integrar).
- `APPLICATION_STATUS.md` → Estado actual de la aplicación y cambios recientes.
- `PLAN.md` → Plan y fases del proyecto.
- `README.md` → Instrucciones de arranque y despliegue.
- `GIT_STRUCTURE.md` → Estado de ramas y pasos para sincronizar (adaptado para 2 personas).

## Guía práctica por responsabilidades (tú y tu compañero)

- Frontend (persona A):
  - Ejecutar y desarrollar `frontend-web`.
  - Comprobar `VITE_API_URL=/api` y usar `getApiUrl()` para llamadas.
  - Tests manuales y verificación visual del dashboard.

- Backend & Despliegue (persona B):
  - Mantener `backend/server.js` y conexiones a RDS.
  - Gestionar `docker-compose.yml` y Nginx en EC2.
  - Ejecutar migrations, backups y healthchecks.

- Trabajo conjunto:
  - Revisar y mergear cambios en `main` antes del deploy.
  - Mantener documentación de endpoints y PLAN actualizada.

## Flujo Git sencillo (para dos personas)

1. `git pull origin main`
2. `git checkout -b feature/tu-descripcion`
3. Hacer cambios y `git commit -m "feat: descripción"`
4. `git push origin feature/tu-descripcion`
5. Revisar el PR y mergear cuando ambos estén de acuerdo.

Si necesitas, elimina la rama local después de mergear:
```
git branch -d feature/tu-descripcion
git push origin --delete feature/tu-descripcion
```

## Notas prácticas

- No subir credenciales: usa `.env` y `.env.example`.
- Mantener `docs/endpoints-api.md` actualizada cuando cambien endpoints.
- Para cualquier despliegue en EC2: hacer `git pull` y `docker compose up --build -d`.

**Hecho para dos personas — claro y directo.**
