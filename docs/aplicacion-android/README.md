# Aplicación Android - SchoolSafeTrack

La aplicación Android es usada principalmente por conductores. Permite registrar la ruta, enviar la ubicación y consultar información del trayecto.

## Qué hace

- **Autenticación**: login seguro para conductores (y potencialmente padres).
- **Gestión de rutas**: mostrar la ruta asignada para el día.
- **Envío de ubicación**: captura posición GPS y la envía periódicamente al backend.
- **Visualización de paradas**: muestra las paradas en orden para guiar el recorrido.
- **Control de inicio/pausa/fin de ruta**: permite activar y desactivar el envío de ubicación.
- **Consulta de autobús** (para padres): ver el autobús asignado en un mapa.

## Estructura del código

```text
aplicacion-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/schoolsafetrack/
│   │   │   │   ├── ui/                   Pantallas y Activities
│   │   │   │   ├── services/             Servicios (GPS, WebSocket)
│   │   │   │   ├── data/                 Modelos y acceso a datos
│   │   │   │   ├── utils/                Funciones auxiliares
│   │   │   │   └── MainActivity.java     Punto de entrada
│   │   │   └── res/                      Recursos (layouts, strings, etc.)
│   │   ├── androidTest/                  Tests de instrumentación
│   │   └── test/                         Tests unitarios
│   ├── build.gradle.kts                  Configuración de build
│   └── google-services.json              Configuración de Firebase (si aplica)
├── gradle/
│   └── wrapper/                          Gradle Wrapper
├── build.gradle.kts                      Build root
├── settings.gradle.kts                   Configuración de módulos
└── local.properties                      Propiedades locales
```

## Tecnologías

- **Java**: lenguaje principal de Android.
- **Android SDK**: framework de Android.
- **Retrofit** (si aplica): cliente HTTP para llamadas a API.
- **Jetpack Compose o XML**: layouts de UI.
- **Google Play Services**: acceso a GPS y servicios del dispositivo.
- **Firebase Cloud Messaging** (si implementado): notificaciones push.
- **Room** (si implementado): base de datos local para modo offline.

## Flujo de una ruta típica (conductor)

1. **Login**: ingresa credenciales y recibe token JWT.
2. **Selección de ruta**: ve la ruta asignada para el día.
3. **Inicio de ruta**: pulsa botón "Iniciar ruta".
4. **Servicio de ubicación**: app captura GPS cada X segundos.
5. **Envío al backend**: ubicación se envía continuamente.
6. **Fin de ruta**: pulsa "Finalizar ruta" para detener envío.

## Pantallas principales

- **LoginActivity**: autenticación.
- **DriverDashboard**: menú principal del conductor.
- **RouteDetail**: detalles de la ruta con paradas.
- **RouteMap**: mapa de la ruta actual.
- **ParentView** (si implementado): padres ven bus en tiempo real.
- **SettingsActivity**: configuración de la app.

## Servicios en segundo plano

- **LocationService**: captura GPS periódicamente.
- **SyncService**: envía datos al backend cuando hay conexión (si hay offline).
- **NotificationService**: recibe y muestra notificaciones.

## Pendiente de completar

- Detallar el servicio de ubicación GPS.
- Documentar cómo manejar modo offline con Room.
- Guía para implementar notificaciones push con Firebase.
- Instrucciones de build y firma para generar APK.
- Detalles de permisos Android necesarios.
