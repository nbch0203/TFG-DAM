package com.schoolsafetrack.app.fcm;

/**
 * Servicio FCM para recibir notificaciones push.
 *
 * PARA ACTIVAR:
 * 1. Añade tu proyecto en Firebase Console (https://console.firebase.google.com)
 * 2. Descarga google-services.json y colócalo en app/
 * 3. Añade en build.gradle.kts (app):
 *      implementation("com.google.firebase:firebase-messaging:24.1.0")
 * 4. Añade en build.gradle.kts (proyecto):
 *      id("com.google.gms.google-services") version "4.4.2" apply false
 * 5. Descomenta el bloque <service> en AndroidManifest.xml
 * 6. Descomenta el código de esta clase y hazla extender FirebaseMessagingService
 *
 * El backend ya dispone de la tabla device_tokens para registrar tokens FCM.
 * Registra el token llamando a POST /api/users/device-token (endpoint a implementar)
 * o directamente en la tabla device_tokens.
 */
public class SchoolFcmService {
    // Ver instrucciones arriba para activar FCM completo.
}
