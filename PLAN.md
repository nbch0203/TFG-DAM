# 📋 Plan de Desarrollo del TFG-DAM

> Documento de planificación elaborado con GitHub Copilot para guiar el desarrollo del proyecto final de grado de DAM (2.º año).

---

## 🎯 Objetivo del Proyecto

Definir aquí el **propósito** del proyecto: qué problema resuelve, a quién va dirigido y qué valor aporta.

> _Pendiente de completar por el equipo._

---

## 🗂️ Fases de Desarrollo

### Fase 0 – Definición y Diseño (semana 1-2)

- [ ] Redactar la descripción completa del proyecto en el README
- [ ] Definir el stack tecnológico (lenguaje, framework, BBDD)
- [ ] Crear wireframes o mockups básicos de la aplicación (Figma, draw.io…)
- [ ] Definir las entidades/modelos de datos principales
- [ ] Establecer la estructura de carpetas del proyecto

### Fase 1 – Configuración del Entorno (semana 2-3)

- [ ] Inicializar el proyecto con la herramienta del framework elegido
- [ ] Configurar `.gitignore` adecuado
- [ ] Añadir un archivo `docker-compose.yml` si se usa base de datos local
- [ ] Configurar linter y formateador de código (ESLint/Prettier, Ktlint, etc.)
- [ ] Añadir GitHub Actions básico para CI (build + tests)

### Fase 2 – Desarrollo del Backend / Lógica de Negocio (semana 3-6)

- [ ] Implementar el modelo de datos (entidades, relaciones)
- [ ] Crear la capa de acceso a datos (repositorios/DAOs)
- [ ] Desarrollar la lógica de negocio principal (servicios)
- [ ] Exponer una API REST (o equivalente según la arquitectura)
- [ ] Validación de datos de entrada
- [ ] Manejo de errores y respuestas HTTP consistentes

### Fase 3 – Desarrollo del Frontend / UI (semana 5-9)

- [ ] Crear la estructura de pantallas/páginas principales
- [ ] Conectar la UI con el backend (llamadas a la API)
- [ ] Implementar autenticación de usuario (login/registro)
- [ ] Validaciones en formularios
- [ ] Diseño responsive o adaptado a la plataforma objetivo

### Fase 4 – Testing y Calidad (semana 8-10)

- [ ] Escribir tests unitarios para la lógica de negocio
- [ ] Escribir tests de integración para los endpoints principales
- [ ] Realizar pruebas manuales de flujo completo (happy path + casos de error)
- [ ] Revisar y corregir bugs detectados

### Fase 5 – Despliegue y Documentación (semana 10-12)

- [ ] Desplegar la aplicación en un entorno accesible (Railway, Render, Vercel, etc.)
- [ ] Documentar la API (Swagger / README)
- [ ] Completar el README con: descripción, instalación, uso y capturas de pantalla
- [ ] Preparar la memoria del TFG

---

## 🧰 Stack Tecnológico Sugerido

| Capa          | Opción A (Android/Kotlin)       | Opción B (Web Full-stack)        |
|---------------|---------------------------------|----------------------------------|
| Frontend      | Android + Jetpack Compose       | React / Vue.js                   |
| Backend       | Spring Boot (Kotlin/Java)       | Node.js + Express / Spring Boot  |
| Base de datos | Room (local) + MySQL (servidor) | MySQL / PostgreSQL                |
| Auth          | JWT                             | JWT / OAuth2                     |
| CI/CD         | GitHub Actions                  | GitHub Actions                   |
| Despliegue    | APK / Firebase                  | Railway / Render                 |

---

## 🏗️ Buenas Prácticas a Seguir

### Control de versiones
- Usar ramas por funcionalidad: `feature/nombre-feature`
- Nunca hacer commits directamente en `main`
- Escribir mensajes de commit descriptivos (Conventional Commits recomendado)
- Usar Pull Requests con revisión antes de mergear

### Arquitectura
- Separar la lógica en capas (MVC, MVVM o similar)
- Evitar lógica de negocio en controladores/vistas
- Mantener funciones cortas y con una única responsabilidad

### Seguridad básica
- No subir credenciales ni contraseñas al repositorio (usar variables de entorno o `.env`)
- Añadir `.env` al `.gitignore`
- Validar y sanitizar todos los datos de entrada del usuario
- Usar contraseñas hasheadas (BCrypt u equivalente)

### Documentación
- Comentar el código complejo o no obvio
- Mantener el README actualizado en todo momento
- Documentar los endpoints de la API

---

## 📈 Mejoras para un Proyecto más Sólido

Estas mejoras elevan la calidad del proyecto sin requerir experiencia avanzada:

| Mejora | Descripción |
|--------|-------------|
| **Tests automáticos** | Al menos tests unitarios de los servicios principales |
| **CI con GitHub Actions** | Ejecutar build y tests en cada push automáticamente |
| **Despliegue real** | Tener la app desplegada y accesible desde internet |
| **Gestión de errores** | Respuestas de error claras y consistentes en la API |
| **Variables de entorno** | Separar configuración del código fuente |
| **Paginación en la API** | No devolver listas enteras sin limitar resultados |
| **Logging básico** | Registrar eventos importantes para detectar problemas |
| **README completo** | Capturas, instrucciones de instalación y uso claro |
| **Diagrama ER** | Diagrama de la base de datos incluido en el repo |
| **Kanban / Issues de GitHub** | Usar GitHub Projects para gestionar tareas |

---

## 👥 Roles del Equipo

| Rol | Responsabilidad |
|-----|----------------|
| Desarrollador/a Backend | API, base de datos, lógica de negocio |
| Desarrollador/a Frontend | UI, llamadas a la API, experiencia de usuario |
| Responsable QA | Tests, revisión de bugs, documentación |

> En un equipo pequeño, los roles pueden solaparse.

---

## 📅 Timeline Resumido

```
Semana 1-2:  Diseño y planificación
Semana 2-3:  Configuración del entorno
Semana 3-6:  Backend
Semana 5-9:  Frontend
Semana 8-10: Testing
Semana 10-12: Despliegue y documentación
```

---

## 🔗 Recursos Útiles

- [Conventional Commits](https://www.conventionalcommits.org/es/)
- [GitHub Actions – Quickstart](https://docs.github.com/es/actions/quickstart)
- [Railway – Despliegue gratuito](https://railway.app/)
- [Draw.io – Diagramas](https://app.diagrams.net/)
- [Figma – Mockups](https://www.figma.com/)
- [Swagger – Documentar APIs](https://swagger.io/)
