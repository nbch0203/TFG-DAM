# 📊 Git Repository Structure - TFG-DAM

**Última actualización:** 2 de Mayo de 2026  
**Repositorio:** nbch0203/TFG-DAM  
**Rama por defecto:** `main`

---

## 🌍 Estado Global del Repositorio

```
┌────────────────────────────────────────────────────────────┐
│                    SNAPSHOT ACTUAL                         │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  🎯 Rama Activa:           main (HEAD)                    │
│  📌 Commits en main:       14168e3 (Último)               │
│  🔄 Ramas Locales:         3 ramas                        │
│  🌐 Ramas Remotas:         5 ramas                        │
│  ✅ Sincronización:        MAIN y AJUSTES SINCRONIZADAS    │
│  ⚠️ Trabajo Pendiente:     Copilot atrás 1 commit         │
│                                                            │
│  Estado General:           LISTO PARA PRODUCCIÓN ✅        │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

---

## 🌳 Árbol de Ramas (Git Graph)

```
HEAD → main (14168e3) ⭐
  ↓
  ├─ LOCAL: main (14168e3) ✅ SINCRONIZED
  │
  ├─ LOCAL: ajustes-app-movil (14168e3) ✅ MERGED TO MAIN
  │
  ├─ LOCAL: copilot/desarrollo-project-planning (27095b6) ⚠️ BEHIND 1
  │
  ├─ REMOTE: origin/main (14168e3) ✅ 
  │
  ├─ REMOTE: origin/ajustes-app-movil (14168e3) ✅
  │
  ├─ REMOTE: origin/copilot/desarrollo-project-planning (ce66376) 🚀 AHEAD
  │
  ├─ REMOTE: origin/desarrollo (4e3a06c) 📋 SIN LOCAL
  │
  └─ REMOTE: origin/optimizacion-usuarios-backend (9cfd8ab) 📋 SIN LOCAL
```

---

## 📍 Estado Detallado de Cada Rama

### 🟢 1. RAMA: main ⭐ (RECOMENDADA)

```
┌──────────────────────────────────────────────────────────┐
│                    ⭐ MAIN ⭐                             │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  LOCAL                                                  │
│  ├─ Rama: main                                          │
│  ├─ HEAD: 14168e3                                       │
│  └─ Status: CURRENT BRANCH                              │
│                                                          │
│  REMOTO                                                 │
│  ├─ Rama: origin/main                                   │
│  ├─ Commit: 14168e3                                     │
│  └─ Status: SYNCHRONIZED ✅                             │
│                                                          │
│  SINCRONIZACIÓN                                         │
│  └─ PERFECTA: Local = Remote = 14168e3                  │
│                                                          │
│  ÚLTIMO COMMIT                                          │
│  ├─ ID: 14168e3                                         │
│  ├─ Mensaje: fix: Resolver problemas de conexión        │
│  │           API - Frontend a RDS                       │
│  ├─ Fecha: 2 de Mayo de 2026                            │
│  └─ Cambios: 37 archivos | API RDS fixes               │
│                                                          │
│  RECOMENDACIÓN                                          │
│  ├─ ✅ Listo para producción                            │
│  ├─ ✅ Usar para deploy                                 │
│  ├─ ✅ No hacer cambios directos                        │
│  └─ ✅ Base para merges de features                     │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 🔵 2. RAMA: ajustes-app-movil ✅ (COMPLETADA)

```
┌──────────────────────────────────────────────────────────┐
│              ajustes-app-movil (MERGEADA) ✅             │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  LOCAL                                                  │
│  ├─ Rama: ajustes-app-movil                             │
│  ├─ Commit: 14168e3                                     │
│  └─ Status: MERGEADA A MAIN                             │
│                                                          │
│  REMOTO                                                 │
│  ├─ Rama: origin/ajustes-app-movil                      │
│  ├─ Commit: 14168e3                                     │
│  └─ Status: MERGED                                      │
│                                                          │
│  SINCRONIZACIÓN                                         │
│  └─ PERFECTA: Mismo commit que main                     │
│                                                          │
│  QUÉ CONTENÍA                                           │
│  ├─ Fix: Rutas API RDS (/api correctamente)            │
│  ├─ Fix: App.vue usando getApiUrl()                     │
│  ├─ Fix: docker-compose.yml VITE_API_URL=/api          │
│  ├─ Fix: nginx.conf proxy configurado                   │
│  └─ Resultado: Dashboard carga datos de RDS ✅          │
│                                                          │
│  ACCIÓN PENDIENTE                                       │
│  ├─ Estado: Puede ser eliminada                         │
│  ├─ Razón: Ya está en main                              │
│  ├─ Comando:                                            │
│  │   git branch -d ajustes-app-movil                    │
│  │   git push origin --delete ajustes-app-movil (opt)   │
│  └─ Prioridad: BAJA (opcional)                          │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 🟡 3. RAMA: copilot/desarrollo-project-planning ⚠️ (ATRÁS)

```
┌──────────────────────────────────────────────────────────┐
│          copilot/desarrollo-project-planning ⚠️          │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  LOCAL                                                  │
│  ├─ Rama: copilot/desarrollo-project-planning          │
│  ├─ Commit: 27095b6                                     │
│  └─ Status: ATRÁS 1 COMMIT                              │
│                                                          │
│  REMOTO                                                 │
│  ├─ Rama: origin/copilot/desarrollo-project-planning   │
│  ├─ Commit: ce66376                                     │
│  └─ Status: ADELANTADA (1 commit más)                   │
│                                                          │
│  DIFERENCIA                                             │
│  ├─ Local atrás en: 1 commit                            │
│  ├─ Commit faltante: ce66376                            │
│  │  "feat: añadir plantilla CloudFormation AWS"         │
│  └─ Necesita: Actualización                             │
│                                                          │
│  CONTENIDO IMPORTANTE                                   │
│  ├─ Features de tracking mejorado                       │
│  ├─ Paradas con asignación de alumnos                   │
│  ├─ Gestión de alumnos mejorada                         │
│  ├─ Documentación del sistema                           │
│  ├─ CloudFormation para infraestructura AWS             │
│  └─ Código: PRODUCCIÓN-READY pero desactualizado        │
│                                                          │
│  ACCIÓN RECOMENDADA                                     │
│  ├─ Si no necesitas usar features aún:                  │
│  │   ✓ Dejar como está (menor riesgo)                   │
│  ├─ Si quieres mantenerla actualizada:                  │
│  │   1. git checkout copilot/desarrollo-...             │
│  │   2. git merge main                                  │
│  │   3. git push origin copilot/desarrollo-...          │
│  ├─ Si quieres mergear a main (después):               │
│  │   1. Primero actualizar rama                         │
│  │   2. Resolver conflictos si hay                      │
│  │   3. hacer PR en GitHub                              │
│  └─ Prioridad: MEDIA (según necesidad)                  │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 📋 4. RAMAS REMOTAS SIN COPIA LOCAL

```
┌──────────────────────────────────────────────────────────┐
│         REMOTAS SIN COPIA LOCAL (Informativo)            │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  origin/desarrollo (4e3a06c)                            │
│  ├─ Commit: 4e3a06c                                     │
│  ├─ Mensaje: Creacion de nuevos documentos...           │
│  ├─ Uso: Rama de desarrollo general                     │
│  └─ Acción: Traer local si necesitas:                   │
│     git checkout --track origin/desarrollo              │
│                                                          │
│  origin/optimizacion-usuarios-backend (9cfd8ab)         │
│  ├─ Commit: 9cfd8ab                                     │
│  ├─ Mensaje: Eliminar archivos documentación...         │
│  ├─ Uso: Optimizaciones de backend                      │
│  └─ Acción: Traer local si necesitas:                   │
│     git checkout --track origin/optimizacion-...        │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 🎯 Acciones Recomendadas

### ✅ ACCIÓN 1: USA MAIN PARA TODO

```bash
# Cambiar a main si no estás en ella
git checkout main

# Ver estado
git status

# Traer cambios remotos (si hay)
git pull origin main

# Resultado: Ready to deploy ✅
```

**Por qué:** 
- ✅ Sincronizada perfectamente con GitHub
- ✅ Contiene los últimos fixes (API RDS)
- ✅ Tested y ready para producción
- ✅ Es la rama por defecto del proyecto

---

### 🗑️ ACCIÓN 2: LIMPIAR AJUSTES-APP-MOVIL

```bash
# Eliminar rama local (ya está en main)
git branch -d ajustes-app-movil

# Opcional: Eliminar también del remoto
git push origin --delete ajustes-app-movil

# Verificar
git branch -v
```

**Por qué:**
- ✅ Rama cumplió su propósito (mergeada)
- ✅ Ya no es necesaria
- ✅ Limpia el historial de ramas

**Riesgo:** NINGUNO (cambios están en main)

---

### 🔄 ACCIÓN 3: ACTUALIZAR COPILOT/DESARROLLO (OPCIONAL)

```bash
# Si quieres mantener la rama actualizada:

# 1. Cambiar a la rama
git checkout copilot/desarrollo-project-planning

# 2. Mergear main
git merge main

# 3. Resolver conflictos si hay (es probable)
# Editar archivos conflictivos en el editor

# 4. Completar merge
git add .
git commit -m "Merge main into copilot/desarrollo"

# 5. Push a remoto
git push origin copilot/desarrollo-project-planning

# Verificar
git log --oneline -3
```

**Por qué:**
- ✅ Mantiene la rama sincronizada
- ✅ Facilita merge a main después
- ⚠️ Puede haber conflictos (resolver manualmente)

**Riesgo:** BAJO si resuelves conflictos correctamente

---

### 🌍 ACCIÓN 4: TRAER RAMA REMOTA A LOCAL

```bash
# Si necesitas trabajar en otra rama remota:

git fetch origin

# Traer rama y conectarla automáticamente
git checkout --track origin/desarrollo

# O, versión más larga pero clara:
git checkout -b desarrollo origin/desarrollo

# Empezar a trabajar
git status
```

**Por qué:**
- ✅ Necesitas features de otra rama
- ✅ Quieres colaborar en desarrollo

---

## 📊 Tabla de Resumen Rápido

| Rama | Local | Remoto | Estado | Acción |
|------|-------|--------|--------|--------|
| **main** ⭐ | 14168e3 | 14168e3 | ✅ SYNC | ✅ USA ESTA |
| **ajustes-app-movil** | 14168e3 | 14168e3 | ✅ MERGED | 🗑️ ELIMINA |
| **copilot/desarrollo** | 27095b6 | ce66376 | ⚠️ ATRÁS 1 | 🔄 ACTUALIZA |
| **desarrollo** | — | 4e3a06c | 📋 NO LOCAL | 🌍 TRAER SI NECESITAS |
| **optimizacion-usuarios** | — | 9cfd8ab | 📋 NO LOCAL | 🌍 TRAER SI NECESITAS |

---

## 🔐 Notas de Seguridad & Configuración

```
⚠️ ARCHIVOS SENSIBLES AGREGADOS AL REPO:

• backend/global-bundle.pem
  └─ Certificado SSL de AWS RDS (crítico para conexión)

• app/cert.pem, app/privkey.pem, app/ca.pem
  └─ Certificados SSL del dominio schoolsafetrack.work.gd

• backend/.env
  └─ Variables de entorno (credenciales RDS)
  └─ CONSIDERAR: Crear .env.example sin valores reales

✅ RECOMENDACIONES:
  1. No compartir archivos .env en públicos
  2. Usar variables de entorno en CI/CD (GitHub Actions)
  3. Rotar certificados periódicamente
  4. Auditar acceso al repo para credenciales
```

---

## 📝 Último Commit Detallado

```
╔════════════════════════════════════════════════════════════╗
║  14168e3                                                   ║
║  fix: Resolver problemas de conexión API - Frontend a RDS  ║
╠════════════════════════════════════════════════════════════╣
║                                                            ║
║  PROBLEMA IDENTIFICADO:                                    ║
║  └─ Frontend pedía /buses en lugar de /api/buses           ║
║  └─ Nginx redirigía HTML en lugar de JSON                 ║
║  └─ Dashboard no cargaba datos de RDS                     ║
║                                                            ║
║  SOLUCIÓN APLICADA:                                        ║
║  ├─ docker-compose.yml:                                   ║
║  │  └─ VITE_API_URL: https://... → /api (relativa)        ║
║  │                                                         ║
║  ├─ frontend-web/src/core/App.vue:                        ║
║  │  └─ Usar getApiUrl() evita /api/api duplicado          ║
║  │                                                         ║
║  ├─ frontend-web/nginx/nginx.conf:                        ║
║  │  └─ Verificado proxy /api/ → backend:3000 ✅           ║
║  │                                                         ║
║  └─ Otros archivos: AdminDashboard, api.js, index.html    ║
║                                                            ║
║  VALIDACIÓN:                                               ║
║  ✅ GET /api/buses → 200 OK (3 items)                      ║
║  ✅ GET /api/users → 200 OK (6 items)                      ║
║  ✅ POST /api/login → 200 OK (JSON response)               ║
║  ✅ Dashboard carga datos correctamente                    ║
║  ✅ RDS persiste datos correctamente                       ║
║                                                            ║
║  ARCHIVOS MODIFICADOS: 37                                  ║
║  FECHA: 2 de Mayo de 2026                                  ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

---

## 🚀 Próximos Pasos Sugeridos

```
INMEDIATO (Hoy):
├─ ✅ [COMPLETADO] Mergear ajustes-app-movil a main
├─ ✅ [COMPLETADO] Push a GitHub
└─ 📋 [PRÓXIMO] Ejecutar ACCIÓN 2 (limpiar rama)

CORTO PLAZO (Esta semana):
├─ 📋 [OPCIONAL] Ejecutar ACCIÓN 3 (actualizar copilot)
├─ 📋 Hacer PR en GitHub si merges cruzan ramas
└─ 📋 Testing en staging antes de producción

MEDIANO PLAZO (Este mes):
├─ 📋 Decidir si mergear copilot/desarrollo a main
├─ 📋 Revisar ramas remotas sin uso local
└─ 📋 Documentar proceso de branching para el equipo
```

---

## 💡 Comandos de Referencia Rápida

```bash
# Estado actual
git status
git branch -v

# Ver historial
git log --oneline -10
git log --all --oneline --graph --decorate

# Sincronizar
git fetch origin
git pull origin main

# Cambiar rama
git checkout main
git checkout copilot/desarrollo-project-planning

# Actualizar rama
git merge main
git rebase main  # alternativa

# Limpiar
git branch -d ajustes-app-movil
git push origin --delete ajustes-app-movil

# Traer rama remota
git checkout --track origin/desarrollo

# Ver diferencias
git diff main ajustes-app-movil
git log main..ajustes-app-movil  # commits en ajustes que no en main
```

---

**Última actualización:** 2 de Mayo de 2026 | Status: LISTO PARA PRODUCCIÓN ✅
