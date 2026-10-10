# Order Management API

API REST para la gestión de categorías, productos, clientes y pedidos, desarrollada con Java 21 y Spring Boot. Este proyecto de portafolio demuestra la implementación de autenticación JWT, autorización por roles, reglas de negocio, persistencia de datos, pruebas automatizadas y despliegue en la nube.

## Demo en producción

* **API desplegada:** https://order-management-api-w870.onrender.com
* **Swagger UI:** https://order-management-api-w870.onrender.com/swagger-ui/index.html
* **Repositorio:** https://github.com/97samir/order-management-api

La API está desplegada en Render y utiliza PostgreSQL alojado en Neon.

Puedes utilizar Swagger UI para consultar los endpoints y probar las operaciones disponibles. Los endpoints protegidos requieren autenticación y los permisos correspondientes al rol del usuario.

## Características principales

* **Autenticación y autorización:** registro e inicio de sesión con JWT y control de acceso basado en roles.
* **Gestión de categorías:** creación, consulta, actualización y eliminación según los permisos configurados.
* **Gestión de productos:** administración de productos, precios, stock, estado y categorías asociadas.
* **Gestión de clientes:** administración de clientes y desactivación lógica.
* **Gestión de pedidos:** creación y consulta de pedidos, confirmación, cancelación y finalización.
* **Control de inventario:** validación de stock y actualización automática al crear pedidos; recuperación del stock al cancelar pedidos cuando las reglas del negocio lo permiten.
* **Validaciones de negocio:** comprobación de datos de entrada, disponibilidad de productos y condiciones de las operaciones.
* **Manejo centralizado de errores:** respuestas consistentes para errores de validación, recursos inexistentes y conflictos.
* **Documentación interactiva:** endpoints documentados mediante Swagger UI y OpenAPI.
* **Persistencia de datos:** almacenamiento en PostgreSQL.
* **Contenedores:** ejecución local mediante Docker y Docker Compose.
* **Pruebas automatizadas:** pruebas unitarias y de integración.

## Tecnologías utilizadas

| Tecnología                       | Uso                                   |
| -------------------------------- | ------------------------------------- |
| Java 21                          | Lenguaje de programación              |
| Spring Boot                      | Desarrollo de la aplicación backend   |
| Spring Web                       | Creación de endpoints REST            |
| Spring Security                  | Autenticación y autorización          |
| Spring Data JPA                  | Persistencia y acceso a datos         |
| Hibernate                        | Mapeo objeto-relacional               |
| PostgreSQL                       | Base de datos relacional              |
| JWT                              | Autenticación mediante tokens         |
| Bean Validation                  | Validación de datos                   |
| Swagger / OpenAPI                | Documentación y pruebas de endpoints  |
| Maven                            | Gestión de dependencias y compilación |
| Docker / Docker Compose          | Contenedorización y entorno local     |
| JUnit / Mockito / Testcontainers | Pruebas automatizadas                 |

## Roles y permisos

La API utiliza dos roles principales:

| Rol        | Permisos principales                                                                                            |
| ---------- | --------------------------------------------------------------------------------------------------------------- |
| `ADMIN`    | Administración de categorías, productos, clientes y operaciones administrativas de pedidos.                     |
| `CUSTOMER` | Consulta de categorías y productos, creación y consulta de pedidos, y cancelación según las reglas del sistema. |

Las operaciones administrativas están protegidas y requieren el rol correspondiente. Las operaciones sobre pedidos también deben respetar las validaciones de negocio y las restricciones de acceso aplicables a cada usuario.

## Probar la API en producción

Puedes comenzar sin instalar Java, Maven ni PostgreSQL. Solo necesitas un navegador y acceso a Swagger UI.

### 1. Abrir Swagger UI

Accede a:

https://order-management-api-w870.onrender.com/swagger-ui/index.html

### 2. Consultar categorías y productos

Busca los siguientes endpoints:

* `GET /api/categories`
* `GET /api/products`

Utiliza **Try it out** y después **Execute** para enviar cada solicitud.

El entorno cuenta con categorías y productos de demostración. Si los endpoints requieren autenticación, inicia sesión y autorízate primero.

### 3. Registrar una cuenta

Utiliza `POST /api/auth/register` para crear una cuenta de cliente y completa el JSON según el esquema que muestra Swagger.

Después, utiliza `POST /api/auth/login` con las credenciales registradas.

### 4. Autorizar las solicitudes protegidas

Copia el JWT que devuelve el inicio de sesión y utiliza el botón **Authorize** de Swagger.

Si la documentación muestra el esquema Bearer, introduce el token según las indicaciones de la interfaz.

### 5. Crear y consultar un pedido

1. Consulta los productos disponibles.
2. Identifica el ID real de un producto y verifica que tenga stock suficiente.
3. Ejecuta `POST /api/orders` con el formato requerido por el endpoint.
4. Consulta el pedido creado mediante los endpoints de pedidos.
5. Prueba la cancelación si el estado del pedido y las reglas de negocio lo permiten.
6. Comprueba el comportamiento del stock después de la operación.

**Importante:** utiliza los IDs que devuelve la API, no valores asumidos. Los datos de demostración son compartidos en el entorno desplegado, por lo que las operaciones pueden modificar el stock y el estado de los pedidos.

### Operaciones administrativas

Las operaciones para crear, modificar o eliminar categorías y productos requieren el rol `ADMIN`.

El usuario administrador se configura mediante el mecanismo de inicialización de la aplicación. No se publican credenciales administrativas en este repositorio. Para probar esas operaciones, utiliza una cuenta administradora configurada de forma segura en el entorno correspondiente.

## Requisitos para ejecutar localmente

Para iniciar el proyecto con Docker necesitas:

* Git.
* Docker Desktop con Docker Compose.

No es necesario instalar Java, Maven ni PostgreSQL por separado para ejecutar los servicios mediante Docker Compose.

## Instalación y ejecución local

### 1. Clonar el repositorio

```bash
git clone https://github.com/97samir/order-management-api.git
cd order-management-api
```

### 2. Configurar las variables de entorno

Copia el archivo de ejemplo.

**PowerShell (Windows):**

```powershell
Copy-Item .env.example .env
```

Edita el archivo `.env` y configura las variables necesarias para tu entorno local.

Revisa especialmente:

* `DB_HOST`
* `DB_PORT`
* `DB_NAME`
* `DB_USER`
* `DB_PASSWORD`
* `JWT_SECRET`
* `JWT_EXPIRATION`
* `DB_SSL_MODE`

Utiliza los valores adecuados para la base de datos local definida en Docker Compose. Genera un secreto JWT seguro en el formato esperado por la aplicación.

No publiques el archivo `.env`, contraseñas ni claves privadas. El archivo `.env.example` debe contener únicamente valores de ejemplo no sensibles.

### 3. Iniciar los servicios

Con Docker Desktop abierto, ejecuta:

```bash
docker compose up --build
```

Docker Compose iniciará PostgreSQL y construirá la imagen de la API.

### 4. Acceder a la API local

Cuando los servicios estén disponibles, abre:

* API: http://localhost:8081
* Swagger UI: http://localhost:8081/swagger-ui/index.html

La configuración local utiliza el puerto 8081, salvo que se configure otro valor mediante las variables de entorno.

## Ejecutar las pruebas automatizadas

Si tienes Java 21 y Docker Desktop instalados, ejecuta las pruebas desde la raíz del proyecto:

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

Los datos de PostgreSQL se conservan en el volumen de Docker configurado para el entorno local y estarán disponibles al volver a iniciar los servicios.

**Advertencia:** el siguiente comando elimina también los volúmenes asociados al proyecto y los datos que contengan:

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

## Despliegue y persistencia

El proyecto utiliza los siguientes servicios:

* **Render:** alojamiento de la API Spring Boot.
* **Neon:** base de datos PostgreSQL administrada.

Los datos creados mediante la API se almacenan en PostgreSQL y permanecen después de reiniciar o volver a desplegar la aplicación, siempre que se mantenga la misma base de datos.

Las variables de entorno de producción se configuran en Render. Las credenciales y los secretos no deben almacenarse en el repositorio.

## Alcance del proyecto

Este proyecto forma parte de mi portafolio de desarrollo backend. Su objetivo es demostrar la construcción de una API REST con Java y Spring Boot, autenticación, autorización por roles, persistencia relacional, reglas de negocio, manejo de errores, pruebas automatizadas, contenedorización y despliegue en la nube.

El proyecto se encuentra orientado al aprendizaje y la demostración técnica. Las credenciales de prueba, la configuración de seguridad y las operaciones disponibles deben gestionarse de forma segura antes de utilizar la aplicación en un entorno comercial.
