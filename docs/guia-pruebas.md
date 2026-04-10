# Guia de Pruebas

Esta guia resume como validar el estado actual del proyecto desde el punto de vista funcional.

## Arranque rapido

1. Levanta la solucion:

```bash
docker compose up -d --build
```

2. Abre la aplicacion web:

```text
http://localhost:5173
```

3. Comprueba que el backend responde:

```bash
curl http://localhost:3000/health
```

## Validaciones recomendadas

- Administracion: login, CRUD y asignacion de paradas.
- Padres: seleccion de hijo y seguimiento del autobus.
- Conductores: flujo de ruta y envio de GPS.

## Casos habituales

- Si un alumno no muestra autobus, revisa que tenga parada vinculada.
- Si una parada no aparece en seguimiento, confirma que pertenezca a una ruta activa.
- Si un padre no ve hijos, comprueba que el `parent_id` coincide con el usuario autenticado.