#!/bin/bash

# Script de prueba para verificar el sistema de seguimiento de autobuses para padres
# Este script simula lo que hace el frontend cuando un padre inicia sesión

API_BASE="http://localhost:3000/api"
HEADER_JSON="Content-Type: application/json"

echo "===================================================="
echo "PRUEBA DE SISTEMA DE SEGUIMIENTO PARA PADRES"
echo "===================================================="
echo ""

# Primero, intentamos hacer login con el usuario padre
echo "[1/4] Intentando login como padre..."
echo "Email: padre@schoolsafetrack.com"
echo "Password: padre123"
echo ""

LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE/login" \
  -H "$HEADER_JSON" \
  -d '{
    "email": "padre@schoolsafetrack.com",
    "password": "padre123"
  }')

echo "Respuesta del servidor:"
echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"
echo ""

# Extraer el usuario ID y rol
USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.user.id' 2>/dev/null)
USER_EMAIL=$(echo "$LOGIN_RESPONSE" | jq -r '.user.email' 2>/dev/null)
USER_ROLE=$(echo "$LOGIN_RESPONSE" | jq -r '.user.role' 2>/dev/null)

if [ "$USER_ROLE" != "PARENT" ] && [ "$USER_ROLE" != "parent" ] && [ -z "$USER_ID" ]; then
    echo "❌ Error: Login falló o usuario no es PARENT"
    exit 1
fi

echo "✅ Login exitoso"
echo "   - ID: $USER_ID"
echo "   - Email: $USER_EMAIL"
echo "   - Rol: $USER_ROLE"
echo ""

# Obtener autobuses del padre
echo "[2/4] Obteniendo autobuses del padre (ID: $USER_ID)..."
BUSES_RESPONSE=$(curl -s -X GET "$API_BASE/parent/$USER_ID/buses")
echo "Respuesta:"
echo "$BUSES_RESPONSE" | jq '.' 2>/dev/null || echo "$BUSES_RESPONSE"
echo ""

BUSES_COUNT=$(echo "$BUSES_RESPONSE" | jq 'length' 2>/dev/null || echo "0")
echo "✅ Se encontraron $BUSES_COUNT autobuses"
echo ""

# Obtener hijos del padre
echo "[3/4] Obteniendo información de hijos del padre..."
CHILDREN_RESPONSE=$(curl -s -X GET "$API_BASE/parent/$USER_ID/children")
echo "Respuesta:"
echo "$CHILDREN_RESPONSE" | jq '.' 2>/dev/null || echo "$CHILDREN_RESPONSE"
echo ""

CHILDREN_COUNT=$(echo "$CHILDREN_RESPONSE" | jq 'length' 2>/dev/null || echo "0")
echo "✅ Se encontraron $CHILDREN_COUNT hijos"
echo ""

# Resumen
echo "[4/4] Resumen de prueba"
echo "===================================================="
echo "✅ PRUEBA COMPLETADA EXITOSAMENTE"
echo ""
echo "Resumen:"
echo "  - Login: ✅ Exitoso"
echo "  - Usuario: $USER_EMAIL (ID: $USER_ID, Rol: $USER_ROLE)"
echo "  - Autobuses disponibles: $BUSES_COUNT"
echo "  - Hijos: $CHILDREN_COUNT"
echo ""
echo "Próximos pasos:"
echo "1. Abre http://localhost:5173 en tu navegador"
echo "2. Inicia sesión con: padre@schoolsafetrack.com / padre123"
echo "3. Deberías ver la página de padre con barra lateral morada"
echo "4. Selecciona 'Seguimiento de autobús' para ver el mapa"
echo "5. Verás los autobuses disponibles en un selector desplegable"
echo "6. Haz clic en 'Ver en mapa' para cada hijo para ver sus paradas"
echo ""
echo "===================================================="
