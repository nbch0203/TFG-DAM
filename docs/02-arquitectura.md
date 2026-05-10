# Arquitectura del sistema

SchoolSafeTrack está dividido en varias partes para separar responsabilidades y hacer el proyecto más fácil de mantener.

## Vista general

```text
App Android (conductor)   ->
Frontend web              ->  Backend API  -> Base de datos MySQL
App web para familias     ->
```

El backend actúa como punto central. Recibe las peticiones de la web y de la aplicación móvil, aplica las reglas del sistema y consulta o actualiza la base de datos.

## Componentes principales

### Aplicación Android

La usa el conductor. Permite iniciar la ruta, enviar ubicación y consultar información básica del trayecto.

### Frontend web

Es el panel visual que usa la administración y, en parte, las familias. Muestra formularios, tablas y mapas.

### Backend

Es el núcleo del sistema. Gestiona el login, los permisos, la lógica de negocio y las consultas a la base de datos.

### Base de datos

Guarda la información persistente del proyecto: usuarios, alumnos, rutas, paradas, autobuses, mensajes y ubicaciones.

## Cómo fluye la información

1. Un usuario inicia sesión desde la web o la app.
2. El backend comprueba sus credenciales.
3. Si el acceso es correcto, devuelve la información necesaria para su perfil.
4. La interfaz muestra solo las opciones permitidas.
5. Cuando el conductor envía su ubicación, el backend la guarda y la comparte con las vistas que la necesitan.

## REST y tiempo real

- **REST** se usa para operaciones normales como iniciar sesión, listar datos o guardar cambios.
- **Tiempo real** se usa para actualizar el mapa cuando la posición del autobús cambia.

## Capas del proyecto

- **Presentación**: web y app móvil.
- **Lógica**: backend y reglas de acceso.
- **Datos**: base de datos MySQL.

## Pendiente de completar

- Añadir un diagrama visual si se decide incluirlo en la memoria final.
- Confirmar si todas las funciones de tiempo real quedan en WebSocket o si alguna usa otro mecanismo.