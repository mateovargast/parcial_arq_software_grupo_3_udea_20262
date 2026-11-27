# API de Inventario por Almacén — Parcial Arquitectura de Software (Equipo 3, UdeA 2026-2)


Servicio web RESTful que expone un API para consultar y registrar el inventario de productos de los almacenes (sedes) de una organización.

## Stack
- Java 21 + Spring Boot 4 (Spring MVC, Spring Data JPA, Spring HATEOAS, Validation, Actuator, Cache)
- PostgreSQL
- Redis (caché), Prometheus + Grafana (observabilidad), Docker

## Estructura del monorepo
```
backend/     API Spring Boot
database/    ddl.sql (esquema) y data.sql (datos semilla)
docs/        diagramas de arquitectura y evidencias
postman/     colección de pruebas
```

## Inicio rápido (local)
1. Crear la base de datos `inventario_db` en PostgreSQL y ejecutar `database/ddl.sql` (y si quieres, `database/data.sql`).
2. `cd backend && mvn spring-boot:run` (perfil `local` por defecto).
   Variables opcionales: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`.
