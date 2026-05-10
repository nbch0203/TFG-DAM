# Guía de pruebas

Esta guía explica cómo arrancar el proyecto y comprobar sus funciones principales de forma sencilla.

## Arranque rápido

1. Levanta la solución:

```bash
docker compose up -d --build
```

2. Abre la aplicación web:

```text
https://SCHOOLSAFETRACK.WORK.GD
```

3. Comprueba que el backend responde consultando cualquier sección del panel. Nginx se encargará de enrutar la petición correctamente.

## Qué deberías poder verificar

- **Administración**: entrar al panel, ver el dashboard y editar datos básicos.
- **Padres y madres**: iniciar sesión, ver hijos vinculados y consultar el autobús.
- **Conductores**: iniciar una ruta y enviar la ubicación.

## Pasos de comprobación simples

### Administración

1. Inicia sesión como administrador.
2. Abre el dashboard.
3. Revisa que se cargan rutas, autobuses y alumnos.

### Padres y madres

1. Inicia sesión con un usuario de prueba.
2. Selecciona un hijo.
3. Comprueba que aparece la ruta o el autobús asociado.

### Conductores

1. Abre la app del conductor.
2. Inicia una ruta.
3. Comprueba que la ubicación se actualiza.

## Problemas comunes

- Si un alumno no muestra autobús, revisa que tenga una parada vinculada.
- Si una parada no aparece en seguimiento, confirma que pertenezca a una ruta activa.
- Si un padre no ve hijos, comprueba que el usuario autenticado sea el correcto.
- Si no carga el mapa, revisa que el backend esté levantado y que haya datos disponibles.

## Pendiente de completar

- Añadir credenciales exactas de prueba si se fijan en un archivo aparte.
- Documentar un caso de prueba completo para GPS y notificaciones cuando estén cerrados.