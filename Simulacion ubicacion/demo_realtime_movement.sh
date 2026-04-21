#!/bin/bash

# Script mejorado: Simula movimiento de autobús en TIEMPO REAL
# Actualiza las coordenadas en la base de datos para que se vea en ParentBusTracking
# Requiere que ParentBusTracking esté haciendo polling del API

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  SIMULACIÓN DE MOVIMIENTO EN TIEMPO REAL PARA ANA GARCÍA      ║"
echo "║  Las coordenadas se actualizan en la BD cada segundo          ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Parámetros
BUS_ID=1
API_URL="http://localhost:3000/api"
DELAY=1  # Cada segundo

# Coordenadas
START_LAT="40.41536"
START_LON="-3.70739"
END_LAT="40.41999"
END_LON="-3.70566"

CURRENT_LAT=$START_LAT
CURRENT_LON=$START_LON

STEP_LAT=0.00059
STEP_LON=0.00056

ITERATIONS=8

echo "📝 CONFIGURACIÓN:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Bus ID: $BUS_ID"
echo "  Origen: Plaza Mayor ($START_LAT, $START_LON)"
echo "  Destino: Mercado Central ($END_LAT, $END_LON)"
echo "  Pasos: $ITERATIONS"
echo "  Intervalo: $DELAY segundo(s)"
echo ""
echo "💡 IMPORTANTE: Mantén ParentBusTracking abierto en el navegador"
echo "   para ver el autobús moviéndose en TIEMPO REAL"
echo ""
echo "🗺️  INICIANDO SIMULACIÓN..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

for i in $(seq 1 $ITERATIONS); do
    PERCENTAGE=$(( (i * 100) / ITERATIONS ))
    
    # Calcular nuevas coordenadas
    CURRENT_LAT=$(awk "BEGIN {printf \"%.6f\", $CURRENT_LAT + $STEP_LAT}")
    CURRENT_LON=$(awk "BEGIN {printf \"%.6f\", $CURRENT_LON + $STEP_LON}")
    
    # Actualizar en la base de datos
    RESPONSE=$(curl -s -X PATCH "$API_URL/buses/$BUS_ID/location" \
      -H "Content-Type: application/json" \
      -d "{\"lat\": $CURRENT_LAT, \"lon\": $CURRENT_LON}")
    
    # Mostrar progreso
    printf "🚌 [%3d%%] Lat: %s, Lon: %s" "$PERCENTAGE" "$CURRENT_LAT" "$CURRENT_LON"
    
    if [ $i -eq $((ITERATIONS / 2)) ]; then
        echo " ⏳ Intermedio..."
    elif [ $i -eq $ITERATIONS ]; then
        echo " ✅ ¡LLEGADA!"
    else
        echo ""
    fi
    
    sleep $DELAY
done

echo ""
echo "═════════════════════════════════════════════════════════════════"
echo "✅ SIMULACIÓN COMPLETADA"
echo ""
echo "🎯 UBICACIÓN FINAL:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Latitud:  $CURRENT_LAT"
echo "  Longitud: $CURRENT_LON"
echo ""
echo "📱 SI NO VES EL MOVIMIENTO EN TIEMPO REAL:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Asegúrate de que ParentBusTracking está abierto"
echo "2. Abre la consola del navegador (F12)"
echo "3. Verifica que se hacen llamadas a /api/parent/10/buses"
echo "4. El mapa debería actualizar cada segundo"
echo ""
echo "═════════════════════════════════════════════════════════════════"
echo ""
echo "✨ PRÓXIMAS MEJORAS (implementar WebSocket para tiempo real):"
echo "   • Socket.io para actualizaciones instantáneas"
echo "   • Eliminar necesidad de polling"
echo "   • Movimiento más suave en el mapa"
echo ""
