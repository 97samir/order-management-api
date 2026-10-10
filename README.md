# Order Management API

API REST para la gestión de pedidos, productos, categorías y clientes. Desarrollada como proyecto de portafolio para demostrar conocimientos de desarrollo backend con Java y Spring Boot.

## Características principales

* **Autenticación y autorización:** registro e inicio de sesión mediante JWT.
* **Gestión de categorías:** operaciones CRUD y desactivación lógica.
* **Gestión de productos:** consulta, filtros, precios, stock y categorías.
* **Gestión de clientes:** administración de clientes y desactivación lógica.
* **Gestión de pedidos:** creación y consulta de pedidos, confirmación, finalización y cancelación.
* **Control de inventario:** validación de stock y actualización automática al crear o cancelar pedidos.
* **Validaciones:** comprobación de datos de entrada, clientes activos, productos disponibles y permisos.
* **Manejo centralizado de errores:** respuestas consistentes para errores de validación, recursos inexistentes y conflictos.
* **Documentación interactiva:** endpoints documentados con Swagger UI.
* **Persistencia de datos:** PostgreSQL.
* **Contenedores:** ejecución de la API y la base de datos mediante Docker Compose.
* **Pruebas automatizadas:** pruebas unitarias y de integración.

## Tecnologías utilizadas

* Java 21
* Spring Boot
* Spring Web
* Spring Security
* Spring Data JPA
* Hibernate
* PostgreSQL
* JWT
* Bean Validation
* Swagger / OpenAPI
* Maven
* Docker y Docker Compose
* JUnit, Mockito y Testcontainers

## Roles de usuario

| Rol      | Permisos principales                                                                                                    |
|----------| ----------------------------------------------------------------------------------------------------------------------- |
| ADMIN    | Administración de categorías, productos, clientes y pedidos.                                                            |
| CUSTOMER | Consulta de productos y categorías, creación y consulta de pedidos propios, y cancelación según las reglas del sistema. |

Los permisos específicos dependen de las reglas configuradas para cada endpoint.

## Requisitos previos

Para ejecutar el proyecto con Docker necesitas:

* Git
* Docker Desktop, con Docker Compose disponible

No necesitas instalar PostgreSQL, Java ni Maven por separado para ejecutar la aplicación mediante Docker Compose.

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd order-management-api
```

### 2. Configurar las variables de entorno

Copia el archivo de ejemplo:

**PowerShell (Windows):**

```powershell
Copy-Item .env.example .env
```

Edita `.env` y configura los valores de tu entorno. Reemplaza `DB_PASSWORD` por una contraseña propia y `JWT_SECRET` por una clave segura en Base64.

No compartas ni publiques tu archivo `.env`.

### 3. Iniciar los servicios

Con Docker Desktop abierto, ejecuta:

```bash
docker compose up --build
```

Docker iniciará PostgreSQL y construirá la imagen de la API. La aplicación utiliza el servicio de base de datos definido en Docker Compose.

### 4. Acceder a Swagger UI

Cuando la API esté disponible, abre:

http://localhost:8081/swagger-ui.html

Desde Swagger puedes consultar los endpoints, registrar un usuario, iniciar sesión y probar las operaciones autorizadas.

Para endpoints protegidos, utiliza el token JWT obtenido al iniciar sesión y el esquema Bearer disponible en la documentación.

## Ejecutar las pruebas

Si tienes Java 21 y Docker Desktop instalados, puedes ejecutar las pruebas desde la raíz del proyecto:

**PowerShell (Windows):**

```powershell
.\mvnw.cmd clean test
```

Las pruebas de integración que utilizan Testcontainers necesitan que Docker esté disponible.

## Detener la aplicación

Para detener los contenedores:

```bash
docker compose down
```

Los datos de PostgreSQL se conservan en el volumen de Docker y estarán disponibles al iniciar nuevamente los servicios.

**Advertencia:** el siguiente comando también elimina el volumen de la base de datos y sus datos:

```bash
docker compose down -v
```

## Estructura general

```text
order-management-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── .dockerignore
├── .env.example
├── .gitignore
├── Dockerfile
├── compose.yaml
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Alcance del proyecto

Este proyecto se desarrolla con fines de aprendizaje y portafolio profesional. Su objetivo es demostrar la implementación de una API REST con autenticación, autorización, persistencia, validaciones, reglas de negocio, pruebas automatizadas y ejecución mediante contenedores.
