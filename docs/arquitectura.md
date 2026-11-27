# Arquitectura implementada

El backend expone dos contratos HTTP diferenciados: la API del equipo con rutas en español y versiones v1/v2, y las rutas en inglés que necesita el frontend, limitadas a v1. Ambas aplicaciones usan el mismo esquema PostgreSQL, pero conservan sus controladores, servicios, DTO y repositorios. Los componentes de infraestructura están configurados en el repositorio; su ejecución depende del entorno.

## Contexto

```mermaid
flowchart LR
  operador["Operador"]
  navegador["Navegador"]
  frontend["React + MUI"]
  nginx["Nginx"]
  api["Spring Boot"]
  equipo["API del equipo<br/>rutas en español · v1/v2"]
  compat["API de compatibilidad<br/>rutas en inglés · v1"]
  db[("PostgreSQL")]
  redis[("Redis cache")]
  prom["Prometheus"]
  grafana["Grafana"]

  operador --> navegador --> frontend
  frontend -->|"HTTP /api · HAL · X-API-Version: 1"| nginx
  nginx --> api
  api --> equipo
  api --> compat
  equipo --> db
  compat --> db
  api --> redis
  prom -->|"/actuator/prometheus"| api
  grafana --> prom
```

## Componentes

```mermaid
flowchart TB
  ui["Frontend React<br/>páginas, formularios y gráficos"]
  client["Axios<br/>header v1 y lector HAL"]
  nginx["Nginx<br/>proxy /api"]
  api["Spring Boot"]
  teamControllers["Controladores del equipo<br/>almacenes, productos, inventario"]
  teamServices["Servicios del equipo<br/>validación, reglas y métricas"]
  teamRepos["Repositorios Spring Data JPA"]
  compatControllers["Controladores de compatibilidad<br/>warehouses, products, inventory, dashboard"]
  compatServices["Servicios de compatibilidad<br/>resumen y carga por lote"]
  compatRepos["Repositorios Spring Data JPA"]
  postgres[("PostgreSQL<br/>almacen, producto, inventario")]
  cache["Spring Cache<br/>inventario/resumen · dashboard 60 s"]
  redis[("Redis")]
  actuator["Actuator + Micrometer + OpenTelemetry"]
  prometheus["Prometheus"]
  grafana["Grafana"]
  openapi["OpenAPI / Swagger<br/>grupos v1/v2"]

  ui --> client --> nginx --> api --> compatControllers --> compatServices --> compatRepos --> postgres
  api --> teamControllers --> teamServices --> teamRepos --> postgres
  teamServices --> cache
  compatServices --> cache
  cache --> redis
  actuator --> prometheus --> grafana
  teamControllers -. "contrato versionado" .-> openapi
  compatControllers -. "incluidos en v1" .-> openapi
```

Las rutas de compatibilidad y las del equipo no colisionan. Las escrituras de ambas superficies invalidan el resumen compartido del dashboard para que las lecturas posteriores reflejen el estado persistido.

## Despliegue Docker Compose configurado

```mermaid
flowchart LR
  browser["Navegador"] -->|"8088:80"| frontend["Nginx + React"]
  frontend -->|"red app · /api"| backend["Spring Boot · 8080"]
  backend -->|"red app"| postgres[("PostgreSQL · volumen persistente")]
  backend -->|"red app"| redis[("Redis · caché")]
  backend -->|"red monitoring"| prometheus["Prometheus · 9090"]
  prometheus --> grafana["Grafana · 3000"]
```

Las redes `app` y `monitoring` son internas; `edge` publica frontend, Prometheus y Grafana. PostgreSQL y Redis no publican puertos de host. El volumen PostgreSQL y los volúmenes de datos de Prometheus/Grafana están declarados en Compose. El backend se sirve solo internamente y Nginx reenvía el prefijo `/api` conservándolo.
