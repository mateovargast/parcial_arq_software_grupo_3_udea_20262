# API de inventario por almacenes

Parcial práctico de Arquitectura de Software, Universidad de Antioquia 2026-2, Equipo 3.

Servicio REST para consultar y registrar el inventario de productos de cada sede. Incluye versionamiento por header, HATEOAS, paginación, caché con Redis, documentación OpenAPI, observabilidad con Prometheus y Grafana, despliegue con Docker Compose y un frontend en React.

**Repositorio:** https://github.com/mateovargast/parcial_arq_software_grupo_3_udea_20262

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Data JPA, Spring HATEOAS |
| Base de datos | PostgreSQL 17 |
| Caché | Redis 7 (perfil `docker`), caché en memoria (perfil `local`) |
| Documentación | springdoc-openapi (Swagger UI) |
| Observabilidad | Micrometer, Prometheus, Grafana |
| Frontend | React, TypeScript, Vite, MUI, Nginx |
| Contenedores | Docker, Docker Compose |

## Estructura

```
backend/        API Spring Boot
frontend/       Aplicación React
database/       ddl.sql (esquema) y data.sql (datos de ejemplo)
deploy/         Configuración de Prometheus y Grafana
docs/           Diagramas de arquitectura
postman/        Colección de pruebas
compose.yaml    Stack completo
```

## Ejecución con Docker

```bash
cp .env.example .env
docker compose up -d --build
```

Las variables de `.env` definen las credenciales de PostgreSQL y de Grafana.

| Servicio | URL |
|---|---|
| Frontend | http://localhost:8088 |
| Swagger UI | http://localhost:8088/swagger-ui.html |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |

PostgreSQL y Redis solo son accesibles dentro de la red de Docker. La base se inicializa con `database/ddl.sql` y `database/data.sql` la primera vez que se crea el volumen `postgres_data`.

## Ejecución local

Requisitos: Java 21, Maven 3.9, Node.js 24 y PostgreSQL.

1. Crear la base `inventario_db` y ejecutar `database/ddl.sql` y `database/data.sql`.
2. Iniciar el backend en http://localhost:8080:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. Iniciar el frontend en http://localhost:5173:

   ```bash
   cd frontend
   npm ci
   cp .env.example .env.local
   npm run dev
   ```

Variables del backend: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`, `CACHE_TTL`, `DASHBOARD_CACHE_TTL` y `CORS_ALLOWED_ORIGINS`.

## API

Todas las rutas bajo `/api` requieren el header `X-API-Version`. Las respuestas usan HAL (`application/hal+json`) y los errores siguen el formato Problem Details (`application/problem+json`, RFC 9457).

| Método | Ruta | Versión | Descripción | Respuestas |
|---|---|---|---|---|
| GET | `/api` | 1, 2 | Punto de entrada con enlaces a los recursos | 200 |
| GET | `/api/almacenes` | 1, 2 | Lista paginada de almacenes | 200 |
| POST | `/api/almacenes` | 1, 2 | Crea un almacén | 201, 400, 409 |
| GET | `/api/almacenes/{sedeId}` | 1, 2 | Detalle de un almacén | 200, 404 |
| GET | `/api/almacenes/{sedeId}/inventario` | 1, 2 | Inventario paginado de la sede | 200, 400, 404 |
| POST | `/api/almacenes/{sedeId}/inventario` | 1, 2 | Registra productos con su stock inicial | 201, 400, 404, 409 |
| GET | `/api/almacenes/{sedeId}/inventario/resumen` | 2 | Totales de unidades, valor y stock bajo | 200, 404 |
| GET | `/api/productos/{productoId}` | 1, 2 | Detalle de un producto | 200, 404 |

**Versionamiento.** En la versión 2, el inventario agrega a cada producto `valorTotal`, `estadoStock` (`NORMAL`, `BAJO`, `AGOTADO`) y `alerta`, además del enlace al resumen. Si falta el header o la versión no existe, la API responde 400.

**Paginación.** Parámetros `page`, `size` (máximo 100) y `sort`, por ejemplo `?page=0&size=10&sort=producto.nombre,asc`. La respuesta incluye el bloque `page` y los enlaces `first`, `prev`, `next` y `last`.

**Registro de inventario.** Ejemplo de cuerpo:

```json
{
  "productos": [
    {
      "sku": "TEC-010",
      "nombre": "Monitor 24 pulgadas",
      "descripcion": "Monitor IPS Full HD",
      "categoria": "Tecnología",
      "unidadMedida": "UNIDAD",
      "precioUnitario": 650000,
      "cantidadInicial": 15,
      "stockMinimo": 3
    }
  ]
}
```

Si el SKU ya existe en el catálogo, se reutiliza el producto. Si ya está registrado en la sede, o se repite dentro de la misma solicitud, la carga se rechaza completa.

**Rutas del frontend.** El frontend consume un conjunto de rutas en inglés con la versión 1: `/api/dashboard/summary`, `/api/warehouses`, `/api/products` y `/api/products/batch`. Están documentadas en el grupo v1 de Swagger.

## Caché

Las consultas de inventario y de resumen se guardan en Redis con la clave `inventario-api::<caché>::<parámetros>` y un TTL de 10 minutos (`CACHE_TTL`). Cada registro de inventario invalida las consultas de inventario en caché y el resumen de la sede afectada.

## Observabilidad

El backend expone métricas en `/actuator/prometheus` y la salud del servicio en `/actuator/health`. Prometheus recoge las métricas cada 15 segundos y Grafana carga el dashboard **Inventario API** al iniciar. El dashboard tiene estas secciones:

- **Resumen:** estado de la API, tiempo activo, almacenes, registros de inventario, productos con stock bajo y porcentaje de aciertos de caché.
- **Tráfico HTTP:** peticiones por endpoint y por código de estado, distribución por versión de API, tasa de errores 4xx y 5xx, y latencias p50, p95 y p99.
- **Negocio:** productos registrados por sede, productos nuevos en el catálogo y cargas rechazadas por motivo.
- **Caché Redis:** aciertos, fallos, escrituras y eliminaciones por caché.
- **Base de datos y JVM:** pool de conexiones HikariCP, CPU, memoria, recolección de basura e hilos.

Métricas propias de la aplicación:

| Métrica | Tipo | Descripción |
|---|---|---|
| `inventario_productos_registrados_total` | Contador | Productos registrados, por sede |
| `inventario_catalogo_productos_creados_total` | Contador | Productos nuevos en el catálogo |
| `inventario_cargas_rechazadas_total` | Contador | Cargas rechazadas, por motivo |
| `inventario_almacenes` | Gauge | Almacenes registrados |
| `inventario_registros` | Gauge | Registros de inventario |
| `inventario_stock_bajo` | Gauge | Registros con stock igual o menor al mínimo |

Las métricas HTTP incluyen la etiqueta `api_version`, y los logs incluyen `traceId` y `spanId` para correlacionar peticiones.

## Pruebas

```bash
cd backend && mvn test
cd frontend && npm test
```

La colección `postman/inventario-api.postman_collection.json` cubre los flujos de las dos versiones, los casos de error y los endpoints de observabilidad. Para usarla en Docker, cambia la variable `baseUrl` a `http://localhost:8088`. Ahí los endpoints de Actuator no están publicados; sus métricas se consultan desde Prometheus.

## Arquitectura

Los diagramas de contexto, componentes y despliegue están en [docs/arquitectura.md](docs/arquitectura.md).
