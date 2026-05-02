# 📚 DOCUMENTACIÓN ACTUALIZADA - TFG-DAM

**Última actualización:** 2 de Mayo de 2026  
**Autores:** Equipo de Desarrollo + GitHub Copilot  
**Estado:** PRODUCCIÓN READY ✅

---

## 📂 Estructura de Documentación

Este repositorio contiene la siguiente documentación actualizada:

### 1. **GIT_STRUCTURE.md** 
   📌 Estado detallado de todas las ramas (local y remoto)
   - Ramas locales vs remotas
   - Commits y sincronización
   - Acciones recomendadas por rama
   - Comandos de referencia

### 2. **APPLICATION_STATUS.md**
   📌 Estado actual de la aplicación
   - Cambios realizados (Commit 14168e3)
   - Validaciones completadas
   - Arquitectura y flujos de datos
   - Checklist pre-producción

### 3. **DOCUMENTACION_ACTUALIZADA.md** (Este archivo)
   📌 Índice y guía de toda la documentación

---

## 🎯 Guía Rápida por Rol

### 👨‍💻 Developer Frontend

**Documentación relevante:**
- GIT_STRUCTURE.md → Secciones: RAMA main, copilot/desarrollo
- APPLICATION_STATUS.md → Flujo de datos, Endpoints GET

**Comandos que usarás:**
```bash
git checkout main
git pull origin main
npm run dev  # en frontend-web/
```

**Archivos importantes:**
- `frontend-web/src/core/App.vue` - Punto de entrada
- `frontend-web/src/utils/api.js` - Construcción de URLs API
- `docker-compose.yml` - VITE_API_URL=/api

---

### 🔧 Developer Backend

**Documentación relevante:**
- APPLICATION_STATUS.md → RDS, Endpoints POST, Seguridad
- GIT_STRUCTURE.md → Estado de la rama main

**Comandos que usarás:**
```bash
git checkout main
npm install  # en backend/
node server.js  # o con docker-compose
```

**Archivos importantes:**
- `backend/server.js` - Express app principal
- `backend/.env` - Credenciales RDS
- `backend/sql/init.sql` - Schema de BD

---

### 🚀 DevOps / Deployment

**Documentación relevante:**
- APPLICATION_STATUS.md → Arquitectura, Deployment Status
- GIT_STRUCTURE.md → Proceso de merge a main

**Comandos que usarás:**
```bash
git checkout main
docker-compose up --build -d
docker-compose logs -f
```

**Archivos importantes:**
- `docker-compose.yml` - Orquestación de servicios
- `cloudformation.yml` - Infraestructura AWS
- `frontend-web/nginx/nginx.conf` - Reverse proxy

---

### 📋 Project Manager / QA

**Documentación relevante:**
- APPLICATION_STATUS.md → Checklist, Estadísticas de datos
- GIT_STRUCTURE.md → Estado de ramas y próximos pasos

**Información clave:**
- ✅ Aplicación LISTA para producción
- ✅ Últimas validaciones completadas
- 📊 3 autobuses, 6 usuarios, 5 estudiantes en BD
- 🔒 SSL y RDS operativos

---

## 🔄 Flujo de Trabajo Recomendado

### Para Hacer un Cambio

```
1. Actualizar main local
   git checkout main
   git pull origin main

2. Crear rama de feature
   git checkout -b feature/nombre-descriptivo

3. Hacer cambios
   # editar archivos...

4. Commitear
   git add .
   git commit -m "feat: descripción clara"

5. Push a remoto
   git push origin feature/nombre-descriptivo

6. Crear Pull Request en GitHub
   - Asignar reviewers
   - Agregar descripción
   - Esperar aprobación

7. Mergear a main (después de aprobación)
   - GitHub auto-merge o merge manual
   - Eliminar rama de feature

8. Pull en EC2 para deploy
   git checkout main
   git pull origin main
   docker-compose up --build -d
```

---

## 🐛 Solución de Problemas Comunes

### "No puedo conectar a RDS"

**Checklist:**
1. ✅ Verificar credenciales en `backend/.env`
2. ✅ Verificar que `backend/global-bundle.pem` existe
3. ✅ Verificar que el security group de RDS permite puerto 3306
4. ✅ Ver logs: `docker-compose logs backend`

**Solución:**
```bash
# Verificar conectividad
docker-compose exec backend curl http://localhost:3000/health

# Ver logs detallados
docker-compose logs backend | grep -i "error\|connected"
```

---

### "El dashboard no carga datos"

**Causas y soluciones:**
1. ✅ Verificar que VITE_API_URL = /api (no URL absoluta)
2. ✅ Verificar que nginx proxea /api/ al backend
3. ✅ Ver logs del navegador (F12 → Network y Console)

**Debug:**
```bash
# Probar endpoint directamente
curl -k https://localhost/api/buses

# Si devuelve HTML → Problema de nginx/routing
# Si devuelve JSON → Problema del frontend
```

---

### "Error: /api/api en las URLs"

**Causa:** VITE_API_URL concatenado manualmente  
**Solución:** ✅ YA RESUELTO en commit 14168e3  

```javascript
// ✅ CORRECTO - Usar getApiUrl()
import { getApiUrl } from './api.js'
const url = getApiUrl() + '/login'  // /api/login

// ❌ INCORRECTO (causa /api/api)
const url = import.meta.env.VITE_API_URL + '/api/login'
```

---

### "Los cambios no aparecen después de git push"

**Checklist:**
1. ✅ Verificar rama actual: `git branch`
2. ✅ Verificar que push fue exitoso: `git log --oneline -3`
3. ✅ Si es en EC2: hacer git pull en el directorio del proyecto
4. ✅ Reconstruir contenedores: `docker-compose up --build -d`

---

## 📊 Métricas del Proyecto

```
┌─────────────────────────────────┐
│     ESTADO DEL REPOSITORIO      │
├─────────────────────────────────┤
│                                 │
│ Ramas locales:          3       │
│ Ramas remotas:          5       │
│ Commits en main:        100+    │
│ Últimos 7 días:         5+      │
│ Files en repo:          ~500    │
│ Tamaño BD:              ~3 MB   │
│                                 │
│ Status:        OPERATIVO ✅      │
│ Documentación: ACTUALIZADA ✅    │
│ Tests:         COMPLETADOS ✅    │
│                                 │
└─────────────────────────────────┘
```

---

## 🔐 Checklist de Seguridad

- [ ] No compartir `.env` en público
- [ ] Certificados SSL válidos (revisar expiración)
- [ ] RDS credenciales en variables de entorno (CI/CD)
- [ ] CORS restringido si es necesario
- [ ] Rate limiting en API endpoints
- [ ] Validación de inputs en backend
- [ ] Logs monitoreados para errores

---

## 📝 Próximas Tareas Documentadas

| Tarea | Prioridad | Responsable | Estado |
|-------|-----------|-------------|--------|
| Limpiar rama ajustes-app-movil | BAJA | Cualquiera | ⏳ Pendiente |
| Actualizar copilot/desarrollo | MEDIA | Si necesitas features | ⏳ Pendiente |
| Revisar certificados SSL | MEDIA | DevOps | ⏳ Pendiente |
| Hacer backups RDS | ALTA | DevOps | ⏳ Pendiente |
| Documentar API endpoints | MEDIA | Backend | ⏳ Pendiente |

---

## 🎓 Recursos de Aprendizaje

### Git Workflow
- [Git branching model](https://nvie.com/posts/a-successful-git-branching-model/)
- [GitHub Flow](https://guides.github.com/introduction/flow/)

### Docker Compose
- [Docker Compose oficial](https://docs.docker.com/compose/)
- [Best practices](https://docs.docker.com/compose/compose-file/compose-file-v3/)

### AWS RDS
- [RDS MySQL documentation](https://docs.aws.amazon.com/rds/latest/UserGuide/CHAP_MySQL.html)
- [Security groups](https://docs.aws.amazon.com/vpc/latest/userguide/VPC_SecurityGroups.html)

### Vue 3 + Vite
- [Vue 3 docs](https://vuejs.org/)
- [Vite guide](https://vitejs.dev/guide/)

---

## 📞 Contacto y Soporte

**Repositorio:** https://github.com/nbch0203/TFG-DAM  
**Rama de producción:** main (14168e3)  
**Documentación:** Este directorio  

**Reportar issues:**
1. Crear issue en GitHub con:
   - Título descriptivo
   - Pasos para reproducir
   - Error/log si hay
   - Entorno (local/EC2/AWS)

---

## 📌 Historial de Documentación

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2 May 2026 | Documentación completa actualizada | Copilot + Dev Team |
| - | GIT_STRUCTURE.md creado | - |
| - | APPLICATION_STATUS.md creado | - |
| - | Resolver API/RDS issues | Copilot |

---

## ✅ Validación de Documentación

```
Documentación Actualizada:
✅ GIT_STRUCTURE.md - Estructura de ramas
✅ APPLICATION_STATUS.md - Estado de la app
✅ DOCUMENTACION_ACTUALIZADA.md - Este archivo
✅ Guías por rol
✅ Solución de problemas
✅ Checklist de seguridad
✅ Próximas tareas

Última verificación: 2 de Mayo de 2026
Estado: COMPLETO Y ACTUALIZADO ✅
```

---

**Documentación Mantenida por:** Equipo de Desarrollo  
**Última actualización:** 2 de Mayo de 2026  
**Vigencia:** Actualizar cada cambio importante
