# Guía: Página de Mensajería del Administrador

## Resumen de cambios realizados

He creado un sistema completo de mensajería para que el administrador pueda ver tickets de errores, avisos del sistema y solicitudes de soporte.

### Cambios en el Frontend

#### 1. **Componente AdminMessages.vue**
   **Ubicación:** `frontend-web/src/AdminMessages/AdminMessages.vue`
   
   ✅ **Completamente funcional** con:
   - **Encabezado con estadísticas**: Total de mensajes, sin leer y críticos
   - **Sistema de filtros**: Por estado, prioridad y tipo
   - **Lista de mensajes**: Con vista previa y resaltado del seleccionado
   - **Panel de detalle**: Muestra toda la información del ticket
   - **Acciones**: Marcar como leído, cambiar estado, marcar como resuelto, eliminar
   - **Sistema de notas**: Añade notas administrativas a los tickets
   - **Datos de demostración**: Si el API no está disponible, muestra ejemplos

#### 2. **Módulo exportado**
   **Ubicación:** `frontend-web/src/AdminMessages/index.js`
   
   Ya estaba creado correctamente.

#### 3. **Integración en AdminPage**
   **Ubicación:** `frontend-web/src/AdminPage/AdminPage.vue`
   
   ✅ **Ya está integrado**:
   - Componente importado (línea ~39)
   - Menú lateral configurado (línea ~16 - "Mensajes")
   - Router de caso switch configurado (línea ~62)

---

### Cambios en el Backend

#### 1. **Nuevos endpoints en server.js**
   **Ubicación:** `backend/server.js`
   
   Se añadieron 5 endpoints RESTful:

   ```
   GET  /api/admin/messages              - Obtiene todos los mensajes
   GET  /api/admin/messages/:id          - Obtiene un mensaje específico
   PATCH /api/admin/messages/:id/read    - Marca como leído
   PATCH /api/admin/messages/:id/status  - Cambia el estado
   POST  /api/admin/messages/:id/notes   - Añade una nota
   DELETE /api/admin/messages/:id        - Elimina un mensaje
   ```

#### 2. **Nuevas tablas en la BD**
   **Ubicación:** `backend/sql/init.sql`
   
   Dos tablas creadas:

   **a) `admin_messages`** - Almacena los tickets
   ```sql
   - id (autoincrement)
   - subject: Título del ticket
   - content: Descripción detallada
   - sender_name: De quién viene (usuario, email, "Sistema")
   - type: error | advertencia | info | soporte
   - status: nuevo | abierto | en_progreso | resuelto | cerrado
   - priority: baja | media | alta | crítica
   - read: 0/1 (leído o no)
   - error_details: JSON con detalles técnicos (opcional)
   - created_at / updated_at: Timestamps
   ```

   **b) `admin_message_notes`** - Almacena las notas de los admins
   ```sql
   - id (autoincrement)
   - message_id: FK a admin_messages
   - text: La nota en sí
   - created_at: Timestamp
   ```

---

## Cómo desplegarlo

### Opción 1: Con Docker (Recomendado)

1. **Reconstruir y levantar los contenedores:**
   ```bash
   cd /mnt/Personal/TFG-DAM
   docker-compose down
   docker-compose up -d --build
   ```

2. El MySQL recreará la BD con las nuevas tablas automáticamente.

3. **Accede a la página:**
   - Ve a la aplicación web
   - Inicia sesión como administrador
   - Haz clic en "Mensajes" en el menú lateral

### Opción 2: Manual (Sin Docker)

1. **Actualizar la BD:**
   ```bash
   mysql -h localhost -u root -p schooltrack < backend/sql/init.sql
   ```

2. **Reiniciar el backend:**
   ```bash
   cd backend
   npm install
   npm start
   ```

3. **Reiniciar el frontend:**
   ```bash
   cd frontend-web
   npm install
   npm run dev
   ```

---

## Estructura de datos - Ejemplos

### Crear un ticket manualmente (para testing)

**SQL directo:**
```sql
INSERT INTO admin_messages (subject, content, sender_name, type, status, priority, read)
VALUES (
  'Error en el login',
  'Los usuarios no pueden iniciar sesión. Error 500 en /api/login',
  'juan.admin@example.com',
  'error',
  'nuevo',
  'alta',
  0
);
```

### Crear detalles técnicos en un ticket

```sql
INSERT INTO admin_messages (subject, content, sender_name, type, status, priority, error_details)
VALUES (
  'Fallo de base de datos',
  'La conexión a MySQL se cerró de forma inesperada',
  'Sistema',
  'error',
  'abierto',
  'crítica',
  JSON_OBJECT(
    'endpoint', '/api/buses',
    'statusCode', 500,
    'message', 'Connection timeout',
    'timestamp', NOW()
  )
);
```

---

## Funcionalidades principales

### Vista de mensajes
- **Lista lateral**: Muestra todos los mensajes filtrados
- **Búsqueda visual**: Los mensajes sin leer tienen fondo amarillo
- **Selección**: Haz clic en un mensaje para verlo en detalle
- **Estados visuales**: Badges con colores según prioridad y estado

### Panel de detalle
- **Información completa**: Subject, tipo, sender, fecha, ID del ticket
- **Contenido**: Texto plano con formato preservado
- **Detalles técnicos**: Si hay JSON de error, se muestra de forma legible
- **Notas administrativas**: Puedes añadir y ver notas previas

### Acciones disponibles
- **Marcar como leído**: Se marca automáticamente al abrir
- **Tomar acción**: Cambia estado a "en progreso"
- **Marcar resuelto**: Cierra el ticket
- **Eliminar**: Borra el ticket permanentemente (y sus notas)

### Filtros
- Por **estado**: nuevo, abierto, en progreso, resuelto, cerrado
- Por **prioridad**: baja, media, alta, crítica
- Por **tipo**: error, advertencia, info, soporte

---

## Personalización

### Cambiar colores
En `AdminMessages.vue`, sección `<style scoped>`, modifica:
- `.messages-header` - Encabezado (gradient azul)
- `.priority-badge.xxxxx` - Colores de prioridad
- `.status-badge.xxxxx` - Colores de estado

### Añadir campos nuevos
Si necesitas más información en un ticket:

1. **BD:** Añade columna a `admin_messages`
   ```sql
   ALTER TABLE admin_messages ADD COLUMN nuevo_campo VARCHAR(255);
   ```

2. **Backend:** El endpoint GET ya devuelve todo automáticamente

3. **Frontend:** En el template, usa `{{ selectedMessage.nuevo_campo }}`

### Integrar notificaciones en tiempo real
Aunque la versión actual funciona, puedes mejorarla con:
- **WebSockets**: Para actualizar en tiempo real cuando llegan nuevos tickets
- **Polling**: Recargar los mensajes cada X segundos
- **FCM/Push**: Notificaciones al navegador (ya tienes infraestructura)

---

## Testing

### ¿Cómo veo los mensajes de demo?

Los datos de demostración se cargan automáticamente si:
1. La BD está vacía (o los endpoints fallan)
2. Incluyen 3 tickets de ejemplo con diferentes prioridades

### ¿Cómo creo un ticket real?

**Opción 1: Desde la BD**
```bash
mysql -h localhost -u root -p schooltrack
```
```sql
INSERT INTO admin_messages (subject, content, sender_name, type, status, priority)
VALUES ('Test', 'Este es un test', 'admin', 'info', 'nuevo', 'media');
```

**Opción 2: Desde código del backend**
En cualquier punto donde detectes un error, puedes hacer:
```javascript
await pool.query(
  `INSERT INTO admin_messages (subject, content, sender_name, type, priority) 
   VALUES (?, ?, ?, ?, ?)`,
  ['Error en X', 'Descripción...', 'Sistema', 'error', 'alta']
);
```

---

## Próximos pasos sugeridos

1. **Integración de errores reales**
   - Captura automáticamente errores del backend
   - Crea tickets cuando hay fallos en la BD

2. **Notificaciones en tiempo real**
   - WebSockets para actualizar en vivo
   - Animaciones cuando un nuevo ticket llega

3. **Exportación de reportes**
   - Genera PDFs con los tickets resueltos
   - Estadísticas mensuales

4. **Asignación de tickets**
   - Asigna tickets a técnicos específicos
   - Sistema de comentarios

5. **Historial completo**
   - Ver cambios de estado con timestamps
   - Quién hizo cada cambio

---

## Troubleshooting

### No veo la página de mensajes
- ✅ Verifica que `docker-compose up` ejecutó sin errores
- ✅ Borra el caché del navegador (Ctrl+Shift+Del)
- ✅ Abre la consola (F12) y busca errores en rojo

### Los filtros no funcionan
- Recarga la página
- Comprueba que los valores coincidan (lowercase): "error", "baja", etc.

### No me guarda las notas
- Verifica que tengas token en localStorage
- Abre la consola y busca el error de fetch

### La BD tiene datos viejos
- Ejecuta: `docker-compose down -v` (borra volúmenes)
- Luego: `docker-compose up -d --build`

---

## Arquitectura final

```
Frontend: Vue 3 (AdminMessages.vue)
    ↓
API: Express.js (server.js endpoints)
    ↓
Base de datos: MySQL (admin_messages + admin_message_notes)
```

**Flujo típico:**
1. Usuario abre el admin panel
2. `onMounted()` en Vue → GET `/api/admin/messages`
3. Servidor devuelve array de mensajes con notas incluidas
4. Vue renderiza lista y detalle
5. Usuario hace clic → cambios de estado vía PATCH/POST/DELETE

---

**¡Listo! Tu página de mensajería está completamente funcional.**

Si necesitas más ayuda o quieres añadir más features, avísame.
