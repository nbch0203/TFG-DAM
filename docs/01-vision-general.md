# Visión general

SchoolSafeTrack es un sistema de transporte escolar pensado para saber en todo momento dónde está el autobús, qué ruta sigue y cómo se relaciona ese trayecto con los alumnos y sus familias. El proyecto combina una aplicación móvil, una web de administración y un backend central para que la información se vea de forma sencilla y ordenada.

## Qué problema resuelve

Cuando el transporte escolar no está digitalizado, es difícil saber si el autobús va con retraso, si la ruta ha cambiado o si un alumno está asociado a la parada correcta. SchoolSafeTrack centraliza esa información para evitar llamadas innecesarias, reducir errores y dar más seguridad a las familias.

## Quién usa el sistema

### Padres y madres

Pueden consultar el bus asociado a sus hijos, ver su posición aproximada en el mapa y recibir avisos relacionados con la ruta.

### Conductores

Usan la app Android para iniciar y terminar rutas, y para enviar la ubicación del autobús mientras trabajan.

### Administración

Gestiona colegios, rutas, paradas, alumnos, autobuses y usuarios desde el panel web.

### Profesorado

Tiene un papel de consulta o apoyo según el flujo definido en el proyecto. Si se amplía este rol, se documentará en la sección de pendientes.

## Funcionalidades principales

- Ver autobuses en tiempo real sobre un mapa.
- Registrar la posición del autobús durante una ruta.
- Asociar alumnos a rutas y paradas.
- Gestionar autobuses, usuarios, colegios y rutas desde la web.
- Consultar el estado general del servicio de transporte.

## Términos básicos

| Término | Significado sencillo |
|---|---|
| GPS | Posición del autobús obtenida por satélite. |
| Mapa en vivo | Mapa que se actualiza con los datos actuales del autobús. |
| API | Parte del sistema que recibe y responde peticiones entre la web, la app y la base de datos. |
| WebSocket | Canal de comunicación que permite enviar datos en tiempo real sin recargar la pantalla. |
| JWT | Token de acceso que se usa para saber quién ha iniciado sesión. |

## Pendiente de completar

- Definir con más detalle las funciones exactas del profesorado si el proyecto las amplía.
- Añadir ejemplos reales de mensajes o avisos cuando estén cerrados los textos finales.