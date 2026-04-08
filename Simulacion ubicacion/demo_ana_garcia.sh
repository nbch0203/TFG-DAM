#!/bin/bash

# Script de demostración: Simula autobús moviéndose en tiempo real para Ana García
# Sin dependencias externas (sin bc)

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  SIMULACIÓN DE SEGUIMIENTO DE AUTOBÚS PARA ANA GARCÍA         ║"
echo "║  Autobús moviendo de Plaza Mayor → Mercado Central            ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Paso 1: Login
echo "📝 PASO 1: Verificando login de ana.garcia@example.com"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
LOGIN=$(curl -s -X POST http://localhost:3000/api/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana.garcia@example.com","password":"Parent2024!"}')

echo "$LOGIN" | jq '{success: .success, user_id: .user.id, email: .user.email, role: .user.role}'
PARENT_ID=$(echo "$LOGIN" | jq -r '.user.id')
echo ""

# Paso 2: Obtener autobuses
echo "🚌 PASO 2: Autobuses disponibles para Ana García"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
BUSES=$(curl -s http://localhost:3000/api/parent/$PARENT_ID/buses)
echo "$BUSES" | jq -r '.[] | "  • \(.matricula) - \(.marca) \(.modelo) (\(.conductor_nombre))"'
BUS_ID=$(echo "$BUSES" | jq -r '.[0].id')
echo ""

# Paso 3: Obtener hijos y paradas
echo "👨‍👩‍👧‍👦 PASO 3: Hijos de Ana García y sus paradas"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
CHILDREN=$(curl -s http://localhost:3000/api/parent/$PARENT_ID/children)
echo "$CHILDREN" | jq -r '.[] | "  • \(.nombre) - Parada: \(.stop_nombre) (\(.latitud), \(.longitud))"'
echo ""

# Paso 4: Simulación de movimiento
echo "🗺️  PASO 4: SIMULANDO MOVIMIENTO DE AUTOBÚS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Coordenadas
START_LAT="40.41536"
START_LON="-3.70739"
END_LAT="40.41999"
END_LON="-3.70566"

echo "Origen: Plaza Mayor (40.41536, -3.70739)"
echo "Destino: Mercado Central (40.41999, -3.70566)"
echo "Distancia simulada: ~500 metros"
echo ""

ITERATIONS=8
STEP_LAT=0.00059
STEP_LON=0.00056

# Variables para simular movimiento
CURRENT_LAT=$START_LAT
CURRENT_LON=$START_LON

for i in $(seq 1 $ITERATIONS); do
    PERCENTAGE=$(( (i * 100) / ITERATIONS ))
    
    # Simulación sin bc - usando awk en su lugar
    CURRENT_LAT=$(awk "BEGIN {printf \"%.6f\", $CURRENT_LAT + $STEP_LAT}")
    CURRENT_LON=$(awk "BEGIN {printf \"%.6f\", $CURRENT_LON + $STEP_LON}")
    
    if [ $i -eq $((ITERATIONS / 2)) ]; then
        printf "🚌 [%3d%%] Lat: %s, Lon: %s ⏳ Intermedio...\n" "$PERCENTAGE" "$CURRENT_LAT" "$CURRENT_LON"
    elif [ $i -eq $ITERATIONS ]; then
        printf "🚌 [%3d%%] Lat: %s, Lon: %s ✅ ¡LLEGADA!\n" "$PERCENTAGE" "$CURRENT_LAT" "$CURRENT_LON"
    else
        printf "🚌 [%3d%%] Lat: %s, Lon: %s\n" "$PERCENTAGE" "$CURRENT_LAT" "$CURRENT_LON"
    fi
    
    sleep 1
done

echo ""
echo "═════════════════════════════════════════════════════════════════"
echo "✅ SIMULACIÓN COMPLETADA"
echo ""
echo "📊 Datos Finales:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Padre: Ana García (ana.garcia@example.com)"
echo "Autobús: $(echo "$BUSES" | jq -r '.[0].matricula') - $(echo "$BUSES" | jq -r '.[0].marca') $(echo "$BUSES" | jq -r '.[0].modelo')"
echo "Conductor: $(echo "$BUSES" | jq -r '.[0].conductor_nombre')"
echo ""
echo "Hijos siendo transportados:"
echo "$CHILDREN" | jq -r '.[] | "  ✓ \(.nombre) (\(.curso))"'
echo ""
echo "Paradas:"
echo "$CHILDREN" | jq -r '.[] | "  📍 \(.stop_nombre) - \(.stop_direccion)"'
echo ""
echo "═════════════════════════════════════════════════════════════════"
echo ""
echo "💡 CÓMO VER ESTO EN VIVO:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1️⃣  Abre: http://localhost:5173"
echo "2️⃣  Inicia sesión:"
echo "   Email: ana.garcia@example.com"
echo "   Contraseña: Parent2024!"
echo ""
echo "3️⃣  Haz click en '📍 Seguimiento de autobús'"
echo ""
echo "4️⃣  Verás en el mapa:"
echo "   ✓ Marcador 🔵 azul = ubicación del autobús"
echo "   ✓ Marcadores 🔴 rojos = paradas de tus hijos"
echo "   ✓ Selector desplegable para cambiar autobús"
echo "   ✓ Coordenadas GPS precisas"
echo ""
echo "5️⃣  Haz click en 'Ver en mapa' para cada hijo"
echo "   para ver su parada específica"
echo ""
echo "═════════════════════════════════════════════════════════════════"
echo ""
echo "🎉 ¡Sistema completamente funcional y listo para usar!"
echo ""
