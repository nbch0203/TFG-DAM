#!/bin/bash

# Script para simular movimiento de autobús
# Actualiza las coordenadas GPS de un autobús para demostración

API_BASE="http://localhost:3000/api"

echo "========================================"
echo "Simulador de Movimiento de Autobús"
echo "========================================"
echo ""
echo "Este script simula el movimiento de un autobús"
echo "actualizando sus coordenadas GPS cada segundo."
echo ""

# Coordenadas iniciales (Plaza Mayor, Madrid)
LAT=40.41536
LON=-3.70739

# Ruta simulada (movimiento lineal)
LAT_STEP=0.00005
LON_STEP=0.00008
DELAY=1  # segundos

echo "Iniciando simulación..."
echo "Bus ID: 1"
echo "Parada inicial: Plaza Mayor (40.41536, -3.70739)"
echo "Parada destino: Mercado Central (40.41999, -3.70566)"
echo ""
echo "Presiona Ctrl+C para detener la simulación"
echo ""

COUNTER=0
MAX_ITERATIONS=50

while [ $COUNTER -lt $MAX_ITERATIONS ]; do
    # Actualizar coordenadas
    LAT=$(echo "$LAT + $LAT_STEP" | bc -l)
    LON=$(echo "$LON + $LON_STEP" | bc -l)
    
    # Hacer la actualización (aquí sería ideal tener un endpoint PUT)
    # Por ahora solo mostramos lo que estaríamos enviando
    
    COUNTER=$((COUNTER + 1))
    PERCENTAGE=$((COUNTER * 100 / MAX_ITERATIONS))
    
    printf "\r[%3d%%] Moviendo... Lat: %.6f, Lon: %.6f" "$PERCENTAGE" "$LAT" "$LON"
    
    sleep $DELAY
done

echo ""
echo ""
echo "✅ Simulación completada"
echo "Coordenadas finales: $LAT, $LON"
echo ""
echo "Nota: Para que estos cambios se reflejen en el frontend,"
echo "necesitarías un endpoint PUT /api/buses/:id para actualizar"
echo "las coordenadas en la base de datos."
echo ""
