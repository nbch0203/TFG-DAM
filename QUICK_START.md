# Inicio Rápido - SchoolSafeTrack

## Comandos Esenciales

### Iniciar todo (primera vez o después de cambios)
```bash
docker-compose up -d --build
```

### Ver estado de contenedores
```bash
docker-compose ps
```

### Ver logs
```bash
# Todos los servicios
docker-compose logs -f

# Solo backend
docker logs backend -f

# Solo frontend
docker logs frontend -f

# Solo MySQL
docker logs mysql_db -f
```

### Detener servicios
```bash
docker-compose down
```

### Reiniciar todo (borrando datos)
```bash
docker-compose down -v
docker-compose up -d --build
```

### Reiniciar un servicio específico
```bash
docker-compose restart backend
docker-compose restart frontend
docker-compose restart mysql
```

### Acceder a la base de datos
```bash
docker exec -it mysql_db mysql -u tfg_user -ptfg_password schoolsafetrack
```

## URLs de Acceso

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:3000/api
- **MySQL**: localhost:3306

## Credenciales de Prueba

### Base de Datos
- Usuario: `tfg_user`
- Password: `tfg_password`
- Base de datos: `schoolsafetrack`

### Aplicación Web
- **Admin**: admin@schoolsafetrack.com / admin123
- **Padre**: padre1@example.com / password123
- **Conductor**: conductor1@example.com / password123

## Verificar que todo funciona

**Windows:**
```powershell
.\verify.ps1
```

**Linux/Mac:**
```bash
bash verify.sh
```

## Solución de Problemas Rápida

### Puerto ocupado
Si algún puerto (3000, 3306, 5173) está ocupado:
```bash
# Ver qué está usando el puerto
netstat -ano | findstr :3000
```

### Limpiar todo y empezar de nuevo
```bash
docker-compose down -v
docker system prune -a
docker-compose up -d --build
```

### Backend se reinicia
```bash
# Ver logs
docker logs backend

# Si hay error de dependencias, reconstruir
docker-compose down
docker-compose up -d --build
```

### Base de datos vacía
```bash
# Recrear con datos
docker-compose down -v
docker-compose up -d
```
