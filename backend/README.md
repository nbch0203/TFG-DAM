# Backend SchoolSafeTrack

Backend Node.js/Express con MySQL para SchoolSafeTrack.

## Arranque

```bash
cp .env.example .env
npm install
npm start
```

## Firebase Messaging

Para enviar notificaciones push desde el backend, define una de estas opciones:

- `FIREBASE_SERVICE_ACCOUNT_PATH`: ruta al JSON del service account.
- `FIREBASE_SERVICE_ACCOUNT_JSON`: JSON completo en una sola línea.

Y asegúrate de configurar también `FIREBASE_PROJECT_ID` si el JSON no lo incluye.

## Endpoint de tokens

La app Android registra su token en:

- `POST /api/users/device-token`

Ese endpoint guarda o reasigna el token al usuario actual para evitar duplicados entre cuentas distintas.

## Script de prueba FCM

Para enviar una notificación de prueba a todos los padres con token activo:

```bash
npm run test:push:parents
```

Opcionalmente puedes personalizar el título y el cuerpo:

```bash
npm run test:push:parents -- --title="Prueba FCM" --body="Mensaje de validación para padres"
```

El script reutiliza el mismo canal FCM del backend y devuelve un resumen de enviados y fallidos por token.
