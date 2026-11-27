# Inventario por almacenes

Aplicación académica de inventario con backend Spring Boot, PostgreSQL y frontend React. 

## Requisitos

- Java 21 y Maven 3.9 o posterior para ejecución local.
- Node.js 24 y npm para el frontend.
- Docker Compose v2 para ejecutar el stack contenedorizado.

## Ejecución local

1. Crear la base de datos `inventario_db` en PostgreSQL.
2. Ejecutar una sola vez `database/ddl.sql` y, si se necesitan los datos de ejemplo, `database/data.sql`. **El DDL contiene `DROP TABLE`; no lo ejecutes sobre una base con datos que deban conservarse.**
3. En una terminal:

   ```powershell
   cd backend
   mvn spring-boot:run
   ```

4. En otra terminal:

   ```powershell
   cd frontend
   npm ci
   Copy-Item .env.example .env.local
   npm run dev
   ```

5. Abrir `http://localhost:5173`. Vite reenvía `/api` a `http://localhost:8080`; CORS también permite `localhost:5173` y `127.0.0.1:5173`.

Configuración backend: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`, `CORS_ALLOWED_ORIGINS`, `CACHE_TTL` y `DASHBOARD_CACHE_TTL`. El perfil `local` es el predeterminado y usa caché simple en memoria. Para cambiar el proxy de Vite, ajustar `API_PROXY_TARGET` en `frontend/.env.local`.

## API del equipo: rutas en español y versiones v1/v2

Las rutas de esta API requieren `X-API-Version: 1` o `X-API-Version: 2`; cada operación declara las versiones que soporta. Las respuestas usan HAL y HATEOAS. En colecciones, leer los elementos desde `_embedded`, los enlaces desde `_links` y la paginación desde `page`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api` | Descubrimiento de recursos HATEOAS. |
| GET, POST | `/api/almacenes` | Listar y crear almacenes. |
| GET | `/api/almacenes/{sedeId}` | Obtener un almacén por ID. |
| GET | `/api/productos/{productoId}` | Obtener un producto por ID. |
| GET | `/api/almacenes/{sedeId}/inventario` | Consultar inventario; la respuesta depende de la versión. |
| GET | `/api/almacenes/{sedeId}/inventario/resumen` | Resumen del inventario, solo v2. |
| POST | `/api/almacenes/{sedeId}/inventario` | Registrar productos con su stock inicial. |

Las colecciones admiten `page`, `size` y `sort`. La colección Postman del repositorio incluye flujos de las dos versiones, errores y observabilidad: `postman/inventario-api.postman_collection.json`.

## API de compatibilidad para el frontend: rutas en inglés

El frontend envía `X-API-Version: 1` y consume estas rutas:

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/dashboard/summary` | Conteos y resumen agregado por almacén. |
| GET | `/api/warehouses?page=0&size=10&q=` | Almacenes paginados y búsqueda por nombre, código o ciudad. |
| GET | `/api/warehouses/{id}` | Detalle de un almacén. |
| GET | `/api/warehouses/{id}/inventory?page=0&size=10&q=` | Inventario paginado de una sede; búsqueda por SKU, nombre o categoría. |
| GET | `/api/products?page=0&size=10&q=` | Catálogo paginado y búsqueda por SKU, nombre o categoría. |
| GET | `/api/products/{id}` | Detalle de un producto. |
| GET | `/api/products/{id}/inventory?page=0&size=10` | Existencias del producto por almacén. |
| POST | `/api/products/batch` | Crea hasta 100 productos nuevos y su stock inicial en un almacén, en una transacción. |

El lote solo registra SKU nuevos. SKU repetido dentro del lote o ya presente produce `409 Conflict`; no se incrementa ni reemplaza inventario existente. Validación produce `400`, almacén/producto inexistente `404`, creación correcta `201`. Los errores usan `application/problem+json`. No hay endpoints para modificar/eliminar almacenes, ni movimientos o historial de inventario.

## OpenAPI y observabilidad

Swagger UI: `/swagger-ui.html`; OpenAPI JSON: `/v3/api-docs`. Los grupos OpenAPI v1/v2 describen la API versionada del equipo y las rutas de compatibilidad aparecen solo en v1. Actuator expone health, info, metrics y Prometheus; el endpoint de métricas es `/actuator/prometheus`. Los detalles de salud no se exponen.

## Docker Compose

Desde la raíz, preparar variables locales y arrancar:

```powershell
Copy-Item .env.example .env
# Edita .env y reemplaza las contraseñas de ejemplo.
docker compose up --build
```

- Frontend/Nginx: `http://localhost:8088`
- Swagger UI: `http://localhost:8088/swagger-ui.html`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (credenciales del archivo `.env`)

PostgreSQL y Redis no publican puertos en el host. PostgreSQL conserva datos en el volumen `postgres_data`; las bases se inicializan con los SQL existentes solo cuando ese volumen está vacío. Los scripts de inicialización no vuelven a ejecutarse sobre volúmenes ya inicializados. No usar `docker compose down -v` salvo que se quiera borrar explícitamente la persistencia.

Redis almacena el resumen del dashboard durante 60 segundos por defecto (`DASHBOARD_CACHE_TTL`) y las cachés del inventario usan `CACHE_TTL` (10 minutos por defecto). El perfil `local` usa caché simple en memoria. Prometheus consulta `/actuator/prometheus` y Grafana incluye datasource/dashboard provisionados. No se incluye autenticación de negocio; no expongas la API o Actuator a redes no confiables sin añadir seguridad.

## Comandos de desarrollo

```powershell
cd frontend
npm ci
npm run dev
npm run build
npm test
```

```powershell
cd backend
mvn test
```

## Arquitectura

Los diagramas de contexto, componentes y despliegue están en [`docs/arquitectura.md`](docs/arquitectura.md). El Compose y los servicios están definidos, pero deben ejecutarse y verificarse en el entorno destino antes de considerarlos operativos.
