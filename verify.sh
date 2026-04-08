#!/bin/bash
# Script de verificación para SchoolSafeTrack
# Este script verifica que todos los servicios estén funcionando correctamente

MYSQL_USER=${MYSQL_USER:-tfg_user}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-tfg_password}
MYSQL_DATABASE=${MYSQL_DATABASE:-schoolsafetrack}

echo "==================================="
echo "Verificando estado de contenedores"
echo "==================================="
docker ps --format "table {{.Names}}\t{{.Status}}"

echo ""
echo "==================================="
echo "Verificando Backend"
echo "==================================="
BACKEND_STATUS=$(docker logs backend 2>&1 | grep "escuchando en puerto")
if [ -z "$BACKEND_STATUS" ]; then
    echo "❌ Backend NO está funcionando correctamente"
    echo "Logs del backend:"
    docker logs backend --tail 20
    exit 1
else
    echo "✅ Backend está funcionando: $BACKEND_STATUS"
fi

HEALTH_STATUS=$(curl -fsS http://localhost:3000/health 2>/dev/null)
if [ -z "$HEALTH_STATUS" ]; then
    echo "❌ Endpoint /health no responde correctamente"
    exit 1
else
    echo "✅ Healthcheck backend OK: $HEALTH_STATUS"
fi

echo ""
echo "==================================="
echo "Verificando Base de Datos"
echo "==================================="
TABLE_COUNT=$(docker exec mysql_db mysql -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SELECT COUNT(*) as total FROM information_schema.tables WHERE table_schema = '$MYSQL_DATABASE' AND table_type = 'BASE TABLE';" 2>/dev/null | tail -n 1)
if [ "$TABLE_COUNT" -eq 10 ]; then
    echo "✅ Base de datos tiene $TABLE_COUNT tablas (correcto)"
else
    echo "❌ Base de datos tiene $TABLE_COUNT tablas (se esperaban 10)"
    exit 1
fi

USER_COUNT=$(docker exec mysql_db mysql -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT COUNT(*) as total FROM users;" 2>/dev/null | tail -n 1)
echo "✅ Usuarios en la base de datos: $USER_COUNT"

echo ""
echo "==================================="
echo "Verificando Frontend"
echo "==================================="
FRONTEND_STATUS=$(docker logs frontend 2>&1 | grep -E "Local:|ready in")
if [ -z "$FRONTEND_STATUS" ]; then
    echo "⚠️  Frontend puede estar iniciando aún..."
    echo "Últimos logs:"
    docker logs frontend --tail 10
else
    echo "✅ Frontend está funcionando"
fi

echo ""
echo "==================================="
echo "URLs de Acceso"
echo "==================================="
echo "Frontend: http://localhost:5173"
echo "Backend:  http://localhost:3000/api"
echo "MySQL:    localhost:3306"
echo ""
echo "Usuario de prueba:"
echo "  Email:    admin@schoolsafetrack.com"
echo "  Password: admin123"
echo ""
echo "✅ Todos los servicios están operativos"
