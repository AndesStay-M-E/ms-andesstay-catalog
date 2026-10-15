# ms-andesstay-catalog

Microservicio de catálogo: consume comandos de tickets de limpieza.

| Elemento | Valor |
|---|---|
| Puerto | 8082 |
| Exchange origen | `andesstay.commands` (direct) |
| Routing key | `housekeeping.ticket` |
| Cola | `catalog.housekeeping.q` |
| DLQ | `catalog.housekeeping.dlq` (vía `andesstay.dlx`) |

## Manejo de mensajes

- ACK manual (`acknowledge-mode: manual`).
- Error no recuperable (datos inválidos): NACK sin reencolar, el mensaje va a la DLQ y se registra en el log.
- Error recuperable: se reintenta hasta `andesstay.messaging.retry.max-attempts` (3) y luego va a la DLQ.
- JSON inválido: el contenedor lo rechaza sin reencolar (`default-requeue-rejected: false`) y va a la DLQ.

## Base de datos

- Perfil `local` (por defecto): H2 en `./data/catalog`. Al iniciar carga 4 alojamientos de ejemplo si el catálogo está vacío.
- Perfil `cloud`: AWS RDS por variables de entorno `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` (ver `application-cloud.yaml`).

Tablas: `accommodations` y `housekeeping_tickets`.

## API REST (JWT de Azure AD)

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| GET | `/api/catalog/accommodations?onlyActive=true` | todos | Listar alojamientos |
| GET | `/api/catalog/accommodations/{id}` | todos | Detalle |
| POST | `/api/catalog/accommodations` | ADMIN, OPERADOR | Crear (201) |
| PUT | `/api/catalog/accommodations/{id}` | ADMIN, OPERADOR | Modificar |
| DELETE | `/api/catalog/accommodations/{id}` | ADMIN | Desactivar (baja lógica, 204) |
| GET | `/api/catalog/housekeeping-tickets?status=PENDIENTE` | ADMIN, OPERADOR | Listar tickets |
| PATCH | `/api/catalog/housekeeping-tickets/{id}/complete` | ADMIN, OPERADOR | Marcar completado (409 si ya lo está) |

Cada comando `housekeeping.ticket` recibido por RabbitMQ crea un ticket `PENDIENTE`. Si el mismo mensaje llega dos veces (mismo `eventId`), no se duplica.

## Ejecutar

1. Levantar RabbitMQ con el repositorio `infra-andesstay-messaging` (`docker compose up -d`).
2. `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).

## Pruebas

`./mvnw test`
