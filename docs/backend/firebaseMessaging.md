# Backend SchoolSafeTrack

Backend Node.js/Express con MySQL para SchoolSafeTrack.

## Arranque

### Con Docker Compose (recomendado)

```bash
docker-compose up -d backend
```

Esto levanta el contenedor `backend` con las variables de entorno definidas en `.env`.

### Localmente (sin contenedor)

```bash
cp .env.example .env
npm install
npm start
```

## Firebase Messaging

Para enviar notificaciones push desde el backend, necesitas:

1. **Descargar la clave de servicio** de Firebase Console:
   - Ve a `Project Settings > Service Accounts > Generate new private key`
   - Guarda el archivo JSON como `backend/service-account.json`

2. **Configurar variables de entorno** en `.env`:
   - `FIREBASE_SERVICE_ACCOUNT_PATH=./service-account.json` (archivo en el backend)
   - `FIREBASE_PROJECT_ID=schoolsafetrack` (debe coincidir con el JSON)

3. **No commitear credenciales**: el archivo `service-account.json` está en `.gitignore` para evitar exponer claves privadas.

### Cómo funciona la autenticación
- Al ejecutarse el script, `fcm.js` lee el JSON del service account
- Construye un JWT firmado con la clave privada RSA
- Intercambia el JWT con Google por un access token temporal (válido 1 hora, cacheado)
- Usa el access token para llamar a la API FCM HTTP v1 y enviar notificaciones

## Endpoint de tokens

La app Android registra su token en:

- `POST /api/users/device-token`

Ese endpoint guarda o reasigna el token al usuario actual para evitar duplicados entre cuentas distintas.

## Implementación del script

### Archivos clave

| Archivo | Responsabilidad |
|---------|-----------------|
| `fcm.js` | Autenticación con Firebase (JWT + OAuth2) y envío de mensajes FCM HTTP v1 |
| `scripts/send-test-parent-push.js` | Script de prueba: consulta padres en BD, envía notificaciones en paralelo |
| `.env` | Variables de configuración (rutas, credenciales, project ID) |
| `service-account.json` | Clave privada de Firebase (nunca commitear, en `.gitignore`) |

### Flujo del script

```
Inicio
  ↓
Validar .env (no placeholder values)
  ↓
Conectar a MySQL (con SSL si existe global-bundle.pem)
  ↓
Consultar PARENT activos con tokens activos
  ↓
Por cada token: enviar FCM en paralelo
  └─ fcm.js → OAuth2 → Google → FCM HTTP v1
  ↓
Reportar sent/failed
  ↓
Fin
```

### Error común: "ETIMEDOUT" en RDS

Si el script no puede conectar a la BBDD, significa que el host no tiene acceso a RDS. Usa `docker-compose exec` para ejecutar desde el contenedor (que sí tiene acceso vía VPC).

## Script de prueba FCM

### Qué hace
El script `scripts/send-test-parent-push.js` valida que FCM funciona correctamente:

1. **Conecta a la BBDD** y obtiene todos los tokens FCM activos de usuarios con rol `PARENT`
2. **Envía una notificación** a cada token usando `sendFcmMessage()` de `fcm.js`
3. **Reporta el resultado**: cuántos se enviaron correctamente y cuántos fallaron (con mensajes de error)

### Cómo ejecutar

En el contenedor (recomendado):

```bash
docker-compose exec backend npm run test:push:parents
```

Con parámetros personalizados:

```bash
docker-compose exec backend npm run test:push:parents -- --title="Mi Prueba" --body="Contenido personalizado"
```

Localmente (si tienes Node instalado y acceso a RDS):

```bash
npm run test:push:parents
```

### Qué esperar

La salida típica es:

```
Destinatarios encontrados: 5
Enviadas: 5
Fallidas: 0
```

Si hay fallos, muestra detalles:

```
Destinatarios encontrados: 5
Enviadas: 4
Fallidas: 1
Errores:
- parentId=123 parentEmail=user@example.com error=FCM respondió 401: ...
```

### Verificar en Android

Cuando el padre recibe la notificación:
- Aparecerá en el panel de notificaciones del dispositivo
- Al tocarla, abre la app con el payload: `type=TEST_NOTIFICATION`, `parentId=...`, etc.
- Si no aparece, revisa:
  - Que el usuario padre esté registrado en la BBDD con rol `PARENT` y `activo=1`
  - Que el dispositivo tenga un token activo en la tabla `device_tokens` (`activo=1`)
  - Que las notificaciones estén habilitadas en la app Android
