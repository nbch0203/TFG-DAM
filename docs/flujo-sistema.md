# Flujo del Sistema

Este documento resume como se conectan autenticacion, paneles por rol, backend y mapas.

## Flujo principal

```text
Navegador -> App.vue -> POST /api/login -> sessionStorage -> panel por rol
```

## Panel de padres

```text
ParentPage.vue -> ParentBusTracking.vue -> children + buses -> calculo de bus activo -> Leaflet
```

El seguimiento de padres usa la relacion:

```text
alumno -> parada(s) -> ruta -> bus asignado ese dia
```

## Administracion

```text
AdminPage.vue -> modulos CRUD -> endpoints REST -> base de datos
```

## Datos y vinculos

- `students.parent_id` apunta al usuario padre.
- `student_stops` guarda las paradas asociadas a cada alumno.
- `stops.route_id` relaciona una parada con una ruta.
- `route_assignments` vincula rutas con autobuses para un dia concreto.
- `buses.lat/lon` alimenta el mapa de seguimiento.