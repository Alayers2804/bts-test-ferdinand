# BTS Product API

A RESTful product management API built with Java and Spring Boot, backed by MariaDB. The API includes user registration and login endpoints, product CRUD operations, filtering, search, pagination, and OpenAPI/Swagger documentation.

> **Status:** This README describes the intended project setup based on the current configuration. Complete and verify authentication token handling, refresh-token lifecycle, and authorization before treating protected endpoints as production-ready.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Run with Docker Compose](#run-with-docker-compose)
- [Run from Eclipse](#run-from-eclipse)
- [API Documentation](#api-documentation)
- [API Endpoints](#api-endpoints)
- [Example Requests](#example-requests)
- [Database Notes](#database-notes)
- [Troubleshooting](#troubleshooting)
- [Security Notes](#security-notes)

## Features

- User registration and login endpoints
- Product create, read, update, and delete (CRUD)
- Product title search and category filtering
- Pagination using `limit` and `page`
- Request validation using Jakarta Bean Validation
- Password hashing with Spring Security's `PasswordEncoder`
- OpenAPI documentation and Swagger UI
- MariaDB persistence
- Docker Compose setup for the API and database

## Technology Stack

- Java (use the version configured by the project; the example Dockerfile below uses Java 21)
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security password encoding
- MariaDB
- Maven
- Springdoc OpenAPI / Swagger UI
- Docker and Docker Compose

## Prerequisites

For local development:

- JDK compatible with the project's Spring Boot version
- Maven, or Eclipse with Maven integration
- Docker Desktop or Docker Engine with Docker Compose
- Git

## Project Structure

The relevant files are expected to follow a structure similar to:

```text
bts-backend/
├── compose.yaml
├── Dockerfile
├── .env                 # local secrets; do not commit
├── .dockerignore
├── pom.xml
├── db/
│   └── init/            # optional SQL initialization scripts
└── src/
    └── main/
        ├── java/com/bts/
        │   ├── config/
        │   ├── controller/
        │   ├── dto/
        │   ├── entity/
        │   ├── repository/
        │   └── service/
        └── resources/
            └── application.properties
```

Adjust the structure in this document if your actual repository differs.

## Configuration

### Application properties

A typical `src/main/resources/application.properties` configuration is:

```properties
server.port=8080
server.servlet.context-path=/api

spring.datasource.url=${DB_URL:jdbc:mariadb://localhost:3307/product-db}
spring.datasource.username=${DB_USERNAME:bts_app}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs

app.jwt.secret=${JWT_SECRET}
```

The defaults above assume MariaDB is published on host port `3307` and the database is named `product-db`. If you map the database as `3306:3306`, change the local JDBC URL to `jdbc:mariadb://localhost:3306/product-db`.

### Environment variables

Create a `.env` file next to `compose.yaml`:

```dotenv
MARIADB_ROOT_PASSWORD=replace_with_a_strong_local_root_password
DB_PASSWORD=replace_with_a_strong_app_password
JWT_SECRET=replace_with_a_generated_base64_secret
```

Generate a random Base64 JWT secret, for example:

```bash
openssl rand -base64 32
```

Keep `.env` private. Add `.env` to `.gitignore` and never commit actual passwords or JWT signing secrets.

The application database password (`DB_PASSWORD`) must match the password assigned to the `bts_app` database user in Compose. `JWT_SECRET` must match the Base64 secret expected by `TokenService`.

## Run with Docker Compose

Run these commands from the directory containing `compose.yaml`.

### 1. Build and start the application and database

```bash
docker compose up --build -d
```

### 2. Check container status

```bash
docker compose ps
```

### 3. Follow application logs

```bash
docker compose logs -f app
```

To inspect MariaDB logs:

```bash
docker compose logs -f db
```

### 4. Open the API

When startup succeeds:

- Base URL: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`

If Swagger UI uses a different path or the application does not have `/api` as its context path, adjust the URL accordingly.

### 5. Stop the containers

```bash
docker compose down
```

This preserves the named database volume. To delete the database volume and its data as well:

```bash
docker compose down -v
```

**Warning:** `docker compose down -v` deletes the Compose-managed database data. Do not run it if you need to preserve that data.

### Docker networking note

In Docker Compose, the API container connects to MariaDB using the service name and container port:

```text
jdbc:mariadb://db:3306/product-db
```

The host port mapping (for example, `3307:3306`) is for connections from your host machine. It does not change the port the API container uses to connect to `db`.

## Run from Eclipse

Use this workflow when developing or debugging the application in Eclipse.

1. Start the database only:

   ```bash
   docker compose up -d db
   ```

2. Configure Eclipse to use the host-mapped MariaDB port. With `3307:3306`, use:

   ```properties
   spring.datasource.url=jdbc:mariadb://localhost:3307/product-db
   ```

3. Set the `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` environment variables in the Spring Boot Run Configuration, or configure equivalent local properties without committing secrets.

4. In Eclipse, right-click the project and select **Run As → Spring Boot App**. If that option is unavailable, use **Run As → Java Application** and select the class annotated with `@SpringBootApplication`.

5. Check the Eclipse Console for startup errors.

Do not run a second MariaDB instance on the same host port. If another container already occupies port `3306`, keep the Compose mapping at `3307:3306` or stop the conflicting container.

## API Documentation

Swagger UI allows you to inspect and execute the API endpoints:

- `http://localhost:8080/api/swagger-ui.html`
- `http://localhost:8080/api/v3/api-docs`

Swagger documents the controller mappings and annotations. It does not implement authentication, validation, or authorization on its own.

## API Endpoints

With `server.servlet.context-path=/api`, the expected routes are:

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/auth/register` | Register a user |
| `POST` | `/api/auth/login` | Authenticate and return tokens |
| `GET` | `/api/products` | List products with optional filters and pagination |
| `GET` | `/api/products/{id}` | Get a product by ID |
| `POST` | `/api/products` | Create a product |
| `PUT` | `/api/products/{id}` | Update a product |
| `DELETE` | `/api/products/{id}` | Delete a product |

### Product list query parameters

| Parameter | Optional | Default | Description |
|---|---:|---:|---|
| `search` | Yes | — | Search product titles |
| `category` | Yes | — | Filter by category |
| `limit` | Yes | `10` | Number of records per page |
| `page` | Yes | `1` | Page number, starting at 1 |

Example:

```http
GET http://localhost:8080/api/products?search=shirt&category=Clothes&limit=10&page=1
```

## Example Requests

The following examples assume the corresponding controller and service implementations are complete.

### Register

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json
```

```json
{
  "username": "testuser",
  "password": "Test123!@#",
  "password_confirmation": "Test123!@#",
  "name": "Jane Doe"
}
```

A successful registration should return HTTP `201 Created`. Use only test credentials locally.

### Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json
```

```json
{
  "username": "testuser",
  "password": "Test123!@#"
}
```

A successful implementation should return both tokens:

```json
{
  "authentication_token": "<access-token>",
  "refresh_token": "<refresh-token>"
}
```

**Implementation note:** The login controller must return the `LoginResponse` produced by the service. If it still throws `UnsupportedOperationException`, login will fail until that placeholder is removed and token generation is completed.

### Create a product

The request fields must match the actual `ProductRequest` DTO. A representative payload is:

```json
{
  "title": "Awesome T-Shirt",
  "price": 99.99,
  "description": "High-quality cotton t-shirt",
  "category": "Clothes",
  "images": [
    "https://example.com/images/t-shirt.jpg"
  ]
}
```

If the API requires authorization, include the access token in the header:

```http
Authorization: Bearer <access-token>
```

### Get a product

```http
GET http://localhost:8080/api/products/1
```

### Update a product

```http
PUT http://localhost:8080/api/products/1
Content-Type: application/json
Authorization: Bearer <access-token>
```

Send the fields required by `ProductRequest`.

### Delete a product

```http
DELETE http://localhost:8080/api/products/1
Authorization: Bearer <access-token>
```

A successful deletion is expected to return HTTP `204 No Content`.

## Database Notes

- The Compose configuration uses a named volume (`mariadb_data`) to persist database files.
- SQL scripts mounted into `/docker-entrypoint-initdb.d` are executed by the MariaDB image during initial database setup. They generally do not rerun when the existing data directory/volume has already been initialized.
- `spring.jpa.hibernate.ddl-auto=update` can update the schema during development. For production systems, use a migration tool such as Flyway or Liquibase and review schema changes carefully.
- Use the dedicated `bts_app` database account for the application rather than connecting as MariaDB root.

## Troubleshooting

### MariaDB connection refused

- Confirm the database container is running: `docker compose ps`.
- Inspect logs: `docker compose logs -f db`.
- From the host, use the mapped port (for example, `localhost:3307`).
- From the API container, use `db:3306`.

### Access denied for database user

- Confirm `DB_USERNAME` is `bts_app`.
- Confirm the Compose `DB_PASSWORD` matches the application user's password.
- Remember that changing environment variables does not automatically change credentials in an already initialized database volume.# BTS Product API



A RESTful product management API built with Java and Spring Boot, backed by MariaDB. The API includes user registration and login endpoints, product CRUD operations, filtering, search, pagination, and OpenAPI/Swagger documentation.



> **Status:** This README describes the intended project setup based on the current configuration. Complete and verify authentication token handling, refresh-token lifecycle, and authorization before treating protected endpoints as production-ready.



## Table of Contents



- [Features](#features)

- [Technology Stack](#technology-stack)

- [Prerequisites](#prerequisites)

- [Project Structure](#project-structure)

- [Configuration](#configuration)

- [Run with Docker Compose](#run-with-docker-compose)

- [Run from Eclipse](#run-from-eclipse)

- [API Documentation](#api-documentation)

- [API Endpoints](#api-endpoints)

- [Example Requests](#example-requests)

- [Database Notes](#database-notes)

- [Troubleshooting](#troubleshooting)

- [Security Notes](#security-notes)



## Features



- User registration and login endpoints

- Product create, read, update, and delete (CRUD)

- Product title search and category filtering

- Pagination using `limit` and `page`

- Request validation using Jakarta Bean Validation

- Password hashing with Spring Security's `PasswordEncoder`

- OpenAPI documentation and Swagger UI

- MariaDB persistence

- Docker Compose setup for the API and database



## Technology Stack



- Java (use the version configured by the project; the example Dockerfile below uses Java 21)

- Spring Boot

- Spring Web

- Spring Data JPA / Hibernate

- Spring Security password encoding

- MariaDB

- Maven

- Springdoc OpenAPI / Swagger UI

- Docker and Docker Compose



## Prerequisites



For local development:



- JDK compatible with the project's Spring Boot version

- Maven, or Eclipse with Maven integration

- Docker Desktop or Docker Engine with Docker Compose

- Git



## Project Structure



The relevant files are expected to follow a structure similar to:



```text

bts-backend/

├── compose.yaml

├── Dockerfile

├── .env                 # local secrets; do not commit

├── .dockerignore

├── pom.xml

├── db/

│   └── init/            # optional SQL initialization scripts

└── src/

    └── main/

        ├── java/com/bts/

        │   ├── config/

        │   ├── controller/

        │   ├── dto/

        │   ├── entity/

        │   ├── repository/

        │   └── service/

        └── resources/

            └── application.properties

```



Adjust the structure in this document if your actual repository differs.



## Configuration



### Application properties



A typical `src/main/resources/application.properties` configuration is:



```properties

server.port=8080

server.servlet.context-path=/api



spring.datasource.url=${DB_URL:jdbc:mariadb://localhost:3307/product-db}

spring.datasource.username=${DB_USERNAME:bts_app}

spring.datasource.password=${DB_PASSWORD}



spring.jpa.hibernate.ddl-auto=update

spring.jpa.show-sql=false



springdoc.swagger-ui.path=/swagger-ui.html

springdoc.api-docs.path=/v3/api-docs



app.jwt.secret=${JWT_SECRET}

```



The database name is `product-db`. The current Docker Compose configuration does not publish MariaDB to a host port; the `localhost:3307` URL is for the optional Eclipse workflow described below, where you temporarily map host port `3307` to container port `3306`. Inside Docker Compose, the API uses `jdbc:mariadb://db:3306/product-db`.



### Environment variables



Create a `.env` file next to `compose.yaml`:



```dotenv

MARIADB_ROOT_PASSWORD=replace_with_a_strong_local_root_password

DB_PASSWORD=replace_with_a_strong_app_password

JWT_SECRET=replace_with_a_generated_base64_secret

```



Generate a random Base64 JWT secret, for example:



```bash

openssl rand -base64 32

```



Keep `.env` private. Add `.env` to `.gitignore` and never commit actual passwords or JWT signing secrets.



The application database password (`DB_PASSWORD`) must match the password assigned to the `bts_app` database user in Compose. `JWT_SECRET` must match the Base64 secret expected by `TokenService`.



## Run with Docker Compose

Run these commands from the directory containing `compose.yaml`.

### 1. Configure environment variables

Create a `.env` file next to `compose.yaml`:

```dotenv
MARIADB_ROOT_PASSWORD=replace_with_a_strong_local_root_password
DB_PASSWORD=replace_with_a_strong_app_password
JWT_SECRET=replace_with_a_generated_base64_secret
```

Generate a random Base64 JWT secret, for example:

```bash
openssl rand -base64 32
```

Keep `.env` private. Add it to `.gitignore` and never commit actual passwords or JWT signing secrets.

### 2. Configure Nginx

Create `nginx.conf` beside `compose.yaml` with the following configuration. Nginx is the only application service published on the host; it forwards requests to the `app` service over the Compose network.

```nginx
server {
    listen 80;
    server_name _;

    location / {
        proxy_pass http://app:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 3. Docker Compose configuration

Use this Compose configuration (for example, in `compose.yaml`):

```yaml
services:
  db:
    image: mariadb:lts-ubi10
    container_name: bts-mariadb
    restart: unless-stopped
    environment:
      MARIADB_DATABASE: product-db
      MARIADB_ROOT_PASSWORD: ${MARIADB_ROOT_PASSWORD:?Set root password in .env}
      MARIADB_USER: bts_app
      MARIADB_PASSWORD: ${DB_PASSWORD:?Set DB_PASSWORD in .env}
    volumes:
      - mariadb_data:/var/lib/mysql
      - ./db/init:/docker-entrypoint-initdb.d:ro
    healthcheck:
      test: ["CMD", "healthcheck.sh", "--connect", "--innodb_initialized"]
      interval: 5s
      timeout: 3s
      retries: 20
    networks:
      - backend

  app:
    build: .
    container_name: bts-api
    restart: unless-stopped
    environment:
      DB_URL: jdbc:mariadb://db:3306/product-db
      DB_USERNAME: bts_app
      DB_PASSWORD: ${DB_PASSWORD:?Set DB_PASSWORD in .env}
      JWT_SECRET: ${JWT_SECRET:?Set JWT_SECRET in .env}
    expose:
      - "8080"
    depends_on:
      db:
        condition: service_healthy
    networks:
      - backend

  nginx:
    image: nginx:alpine
    container_name: bts-nginx
    restart: unless-stopped
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    depends_on:
      - app
    networks:
      - backend

volumes:
  mariadb_data:

networks:
  backend:
```

The `db`, `app`, and `nginx` services share the `backend` network. The API connects to MariaDB at `db:3306`; Nginx connects to the API at `app:8080`. Only Nginx publishes a host port (`80`). MariaDB and the API are not directly published on host ports.

### 4. Build and start the services

```bash
docker compose up --build -d
```

Check container status:

```bash
docker compose ps
```

Follow application logs:

```bash
docker compose logs -f app
```

Inspect Nginx logs:

```bash
docker compose logs -f nginx
```

Inspect MariaDB logs:

```bash
docker compose logs -f db
```

### 5. Open the API through Nginx

When the services are running, use these local URLs:

- API base URL: `http://localhost/api`
- Swagger UI: `http://localhost/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost/api/v3/api-docs`

Nginx forwards the original request path unchanged, so `/api/products` reaches Spring Boot at `/api/products`.

### 6. Use Cloudflare Tunnel (optional)

If a Cloudflare Tunnel runs on the same WSL host, configure its published application hostname to use this origin service:

```text
http://localhost:80
```

For example, if the published hostname is `https://bts-test.example.com`, the public API base URL becomes `https://bts-test.example.com/api`. Replace the example hostname with the actual hostname configured in Cloudflare.

The tunnel service and Docker Compose must be running. If the hostname fails to resolve, troubleshoot DNS separately; specifying a fixed IP with `curl --resolve` is only a diagnostic workaround, not a normal requirement.

### 7. Stop the containers

```bash
docker compose down
```

This preserves the named database volume. To delete the Compose-managed database volume and its data as well:

```bash
docker compose down -v
```

**Warning:** `docker compose down -v` deletes the Compose-managed database data. Do not run it if you need to preserve that data.

### Docker networking note

Inside the Compose network, the API uses `jdbc:mariadb://db:3306/product-db` to connect to MariaDB. The service name `db` is resolved by Docker's internal DNS. Host port mappings do not change the port used for service-to-service communication.

## Run from Eclipse



Use this workflow when developing or debugging the application in Eclipse.



1\. Start the database only:



   ```bash

   docker compose up -d db

   ```



2\. Configure Eclipse to use the host-mapped MariaDB port. With `3307:3306`, use:



   ```properties

   spring.datasource.url=jdbc:mariadb://localhost:3307/product-db

   ```



3\. Set the `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` environment variables in the Spring Boot Run Configuration, or configure equivalent local properties without committing secrets.



4\. In Eclipse, right-click the project and select **\*\*Run As → Spring Boot App\*\***. If that option is unavailable, use **\*\*Run As → Java Application\*\*** and select the class annotated with `@SpringBootApplication`.



5\. Check the Eclipse Console for startup errors.



Do not run a second MariaDB instance on the same host port. If another container already occupies port `3306`, keep the Compose mapping at `3307:3306` or stop the conflicting container.



## API Documentation



Swagger UI allows you to inspect and execute the API endpoints:



- `http://localhost/api/swagger-ui.html`

- `http://localhost/api/v3/api-docs`



Swagger documents the controller mappings and annotations. It does not implement authentication, validation, or authorization on its own.



## API Endpoints



With `server.servlet.context-path=/api`, the expected routes are:



\| Method | Endpoint | Purpose |

\|---|---|---|

\| `POST` | `/api/auth/register` | Register a user |

\| `POST` | `/api/auth/login` | Authenticate and return tokens |

\| `GET` | `/api/products` | List products with optional filters and pagination |

\| `GET` | `/api/products/{id}` | Get a product by ID |

\| `POST` | `/api/products` | Create a product |

\| `PUT` | `/api/products/{id}` | Update a product |

\| `DELETE` | `/api/products/{id}` | Delete a product |



### Product list query parameters



\| Parameter | Optional | Default | Description |

\|---|---:|---:|---|

\| `search` | Yes | — | Search product titles |

\| `category` | Yes | — | Filter by category |

\| `limit` | Yes | `10` | Number of records per page |

\| `page` | Yes | `1` | Page number, starting at 1 |



Example:



```http

GET http://localhost/api/products?search=shirt&category=Clothes&limit=10&page=1

```



## Example Requests



The following examples assume the corresponding controller and service implementations are complete.



### Register



```http

POST http://localhost/api/auth/register

Content-Type: application/json

```



```json

{

  "username": "testuser",

  "password": "Test123!@#",

  "password_confirmation": "Test123!@#",

  "name": "Jane Doe"

}

```



A successful registration should return HTTP `201 Created`. Use only test credentials locally.



### Login



```http

POST http://localhost/api/auth/login

Content-Type: application/json

```



```json

{

  "username": "testuser",

  "password": "Test123!@#"

}

```



A successful implementation should return both tokens:



```json

{

  "authentication_token": "\<access-token>",

  "refresh_token": "\<refresh-token>"

}

```



**\*\*Implementation note:\*\*** The login controller must return the `LoginResponse` produced by the service. If it still throws `UnsupportedOperationException`, login will fail until that placeholder is removed and token generation is completed.



### Create a product



The request fields must match the actual `ProductRequest` DTO. A representative payload is:



```json

{

  "title": "Awesome T-Shirt",

  "price": 99.99,

  "description": "High-quality cotton t-shirt",

  "category": "Clothes",

  "images": [

    "https://example.com/images/t-shirt.jpg"

  ]

}

```



If the API requires authorization, include the access token in the header:



```http

Authorization: Bearer \<access-token>

```



### Get a product



```http

GET http://localhost/api/products/1

```



### Update a product



```http

PUT http://localhost/api/products/1

Content-Type: application/json

Authorization: Bearer \<access-token>

```



Send the fields required by `ProductRequest`.



### Delete a product



```http

DELETE http://localhost/api/products/1

Authorization: Bearer \<access-token>

```



A successful deletion is expected to return HTTP `204 No Content`.



## Database Notes



- The Compose configuration uses a named volume (`mariadb_data`) to persist database files.

- SQL scripts mounted into `/docker-entrypoint-initdb.d` are executed by the MariaDB image during initial database setup. They generally do not rerun when the existing data directory/volume has already been initialized.

- `spring.jpa.hibernate.ddl-auto=update` can update the schema during development. For production systems, use a migration tool such as Flyway or Liquibase and review schema changes carefully.

- Use the dedicated `bts_app` database account for the application rather than connecting as MariaDB root.



## Troubleshooting



### MariaDB connection refused



- Confirm the database container is running: `docker compose ps`.

- Inspect logs: `docker compose logs -f db`.

- From the host, use the mapped port (for example, `localhost:3307`).

- From the API container, use `db:3306`.



### Access denied for database user



- Confirm `DB_USERNAME` is `bts_app`.

- Confirm the Compose `DB_PASSWORD` matches the application user's password.

- Remember that changing environment variables does not automatically change credentials in an already initialized database volume.



### Port already in use



If host port `3306` is occupied, publish MariaDB on another host port, such as `3307:3306`. Keep the internal Compose JDBC URL as `jdbc:mariadb://db:3306/product-db`.



### Swagger returns 404



- Confirm the Springdoc dependency is present.

- Check `server.servlet.context-path`.

- Check `springdoc.swagger-ui.path`.

- Inspect application startup logs.



### Login fails for seeded users



Seeded users need real password hashes generated by the same password encoder used by the application. Placeholder BCrypt strings will not authenticate. Register a user through the API or generate a valid hash using the application's `PasswordEncoder`.



### Container starts but API cannot connect to MariaDB



The API should wait for the database health check if Compose uses `depends_on` with `condition: service_healthy`. Check both containers' logs for datasource and migration/schema errors.



## Security Notes



- Do not commit `.env`, database passwords, JWT secrets, access tokens, or refresh tokens.

- Use BCrypt or another appropriate password encoder; never store plaintext passwords.

- Verify access-token signatures and expiry on protected routes.

- Do not accept refresh tokens as access tokens.

- Implement refresh-token expiration, rotation, and revocation before using the API beyond local testing.

- Configure CORS explicitly for the frontends that should be allowed to call the API. Publishing Docker ports does not configure CORS.

- Ensure protected product create/update/delete endpoints are enforced by Spring Security, not merely documented in Swagger.

- Avoid using MariaDB root credentials in the application.



## License



Add a license here if you intend to distribute this project publicly.


### Port already in use

If host port `3306` is occupied, publish MariaDB on another host port, such as `3307:3306`. Keep the internal Compose JDBC URL as `jdbc:mariadb://db:3306/product-db`.

### Swagger returns 404

- Confirm the Springdoc dependency is present.
- Check `server.servlet.context-path`.
- Check `springdoc.swagger-ui.path`.
- Inspect application startup logs.

### Login fails for seeded users

Seeded users need real password hashes generated by the same password encoder used by the application. Placeholder BCrypt strings will not authenticate. Register a user through the API or generate a valid hash using the application's `PasswordEncoder`.

### Container starts but API cannot connect to MariaDB

The API should wait for the database health check if Compose uses `depends_on` with `condition: service_healthy`. Check both containers' logs for datasource and migration/schema errors.

## Security Notes

- Do not commit `.env`, database passwords, JWT secrets, access tokens, or refresh tokens.
- Use BCrypt or another appropriate password encoder; never store plaintext passwords.
- Verify access-token signatures and expiry on protected routes.
- Do not accept refresh tokens as access tokens.
- Implement refresh-token expiration, rotation, and revocation before using the API beyond local testing.
- Configure CORS explicitly for the frontends that should be allowed to call the API. Publishing Docker ports does not configure CORS.
- Ensure protected product create/update/delete endpoints are enforced by Spring Security, not merely documented in Swagger.
- Avoid using MariaDB root credentials in the application.

## License

Add a license here if you intend to distribute this project publicly.
