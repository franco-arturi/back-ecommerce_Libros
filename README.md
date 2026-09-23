# E-commerce JU - Libros

API REST desarrollada con Spring Boot para un e-commerce de venta de libros: registro y login de
usuarios, catálogo de libros por categoría, publicación de libros con imágenes, carrito de compras
y checkout con descuento de stock.

## Stack

- Java 21, Spring Boot 4.1 (Web, Data JPA, Actuator, Security)
- Autenticación con JWT (jjwt 0.12.6) y contraseñas encriptadas con BCrypt
- MySQL (persistencia principal) / H2 en memoria (perfil `demo`)
- Maven (`mvnw` / `mvnw.cmd`)

## Requisitos

- JDK 21
- MySQL corriendo en `localhost:3306`, base `ecommerce`, usuario `root` / password `root`
  (ver [src/main/resources/application.properties](src/main/resources/application.properties)).
- Docker Desktop, para levantar MySQL sin instalarlo directamente en el sistema (ver el paso a
  paso más abajo).
- Postman (o similar) para probar los endpoints.

## Instalar y levantar MySQL con Docker

Si no tenés MySQL instalado, la forma más simple es correrlo dentro de un contenedor de Docker.
No hace falta instalar MySQL en la máquina, solo Docker.

1. **Instalar Docker Desktop** (si no lo tenés ya): descargarlo desde
   [docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop/) e
   instalarlo con las opciones por defecto. En Windows puede pedir reiniciar la computadora.
2. **Abrir Docker Desktop** y esperar a que la ballena del ícono de la bandeja del sistema deje de
   animarse (indica que el motor de Docker ya está corriendo). Se puede confirmar desde una
   terminal con:
   ```powershell
   docker --version
   ```
3. **Levantar el contenedor de MySQL** (crea el contenedor, lo deja corriendo en segundo plano, y
   ya crea la base `ecommerce` vacía):
   ```powershell
   docker run -d --name ecommerce-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=ecommerce mysql:8.0
   ```
4. **Verificar que el contenedor quedó corriendo:**
   ```powershell
   docker ps
   ```
   Tiene que aparecer una fila con `NAMES = ecommerce-mysql` y `PORTS` incluyendo `3306`.

Con esto MySQL ya queda disponible en `localhost:3306` con las credenciales que espera
`application.properties` (usuario `root`, password `root`). Este contenedor sigue existiendo
aunque se apague la computadora; para volver a arrancarlo (sin crear uno nuevo) alcanza con:
```powershell
docker start ecommerce-mysql
```

## Inicializar la base de datos y crear las tablas

No hace falta escribir ningún `CREATE TABLE` a mano: Hibernate crea el esquema completo (tablas,
columnas y foreign keys) la primera vez que la aplicación se conecta a una base vacía, gracias a
`spring.jpa.hibernate.ddl-auto=update`.

1. Tener MySQL corriendo (ver la sección anterior "Instalar y levantar MySQL con Docker").
2. Si el contenedor ya existía de antes y la base `ecommerce` tiene datos de otra corrida, se puede
   recrear vacía:
   ```powershell
   docker exec ecommerce-mysql mysql -uroot -proot -e "DROP DATABASE ecommerce; CREATE DATABASE ecommerce;"
   ```
3. Levantar la aplicación con el perfil por defecto:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   En los logs de arranque se ven las sentencias `Hibernate: create table ...` y
   `Hibernate: alter table ... add constraint ...` para cada tabla y relación.
4. Verificar que las tablas quedaron creadas:
   ```powershell
   docker exec ecommerce-mysql mysql -uroot -proot -e "USE ecommerce; SHOW TABLES;"
   ```
   Deberían aparecer: `usuarios`, `categorias`, `libros`, `imagenes_libro`, `carritos` e
   `items_carrito`.

A partir de ahí, la base ya está lista para cargar datos siguiendo la
[guía de pruebas](postman/GUIA-PRUEBAS-MANUAL.md).

## Ejecución

Contra MySQL (perfil por defecto):

```powershell
.\mvnw.cmd spring-boot:run
```

Con datos precargados en una base H2 en memoria, sin necesidad de MySQL (perfil `demo`):

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

La aplicación queda disponible en `http://localhost:8080`. Con `ddl-auto=update`, Hibernate crea
las tablas y relaciones automáticamente al iniciar contra una base vacía.

## Endpoints principales

| Módulo | Endpoint |
|---|---|
| Autenticación | `POST /api/auth/register`, `POST /api/auth/login` (públicos) |
| Usuarios | `GET /api/usuarios/{id}`, `GET /api/usuarios/listarUser` (solo ADMIN) |
| Categorías | `GET /api/categorias`, `GET /api/categorias/{id}`, `POST /api/categorias`, `PUT /api/categorias/{id}`, `DELETE /api/categorias/{id}` |
| Libros | `GET /api/libros`, `GET /api/libros/{id}`, `POST /api/libros`, `PUT /api/libros/{id}`, `PUT /api/libros/{id}/stock`, `DELETE /api/libros/{id}` |
| Imágenes de libro | `GET/POST /api/libros/{libroId}/imagenes`, `DELETE /api/libros/{libroId}/imagenes/{imagenId}` |
| Carrito | `GET /api/carritos/{usuarioId}`, `POST /api/carritos/{usuarioId}/items`, `PUT /api/carritos/{usuarioId}/items/{itemId}`, `DELETE /api/carritos/{usuarioId}/items/{itemId}`, `DELETE /api/carritos/{usuarioId}`, `POST /api/carritos/{usuarioId}/checkout` |

Los errores se devuelven en un formato uniforme (`timestamp`, `status`, `error`, `mensaje`, `path`),
manejado de forma centralizada en `ApiExceptionHandler`.

## Seguridad (Spring Security + JWT)

La API es **stateless**: no guarda sesión, cada request se autentica con un JSON Web Token.

1. `POST /api/auth/register` crea el usuario con rol `USER` y la contraseña encriptada con BCrypt.
   Responde `201 Created` sin devolver la contraseña.
2. `POST /api/auth/login` valida email y contraseña con el `AuthenticationManager` y devuelve un
   JWT firmado (vence en 1 hora):

   ```json
   { "token": "eyJhbGciOi...", "tipo": "Bearer", "expiraEnSegundos": 3600, "usuario": { "id": 1, "rol": "USER", "...": "..." } }
   ```

3. En el resto de los endpoints se envía el header `Authorization: Bearer <token>`.

| Acceso        | Endpoints                                                                  |
| ------------- | -------------------------------------------------------------------------- |
| Público       | `/api/auth/**`, `GET /api/libros/**`, `GET /api/categorias/**`, `/h2-console` |
| Solo `ADMIN`  | `GET /api/usuarios/listarUser`                                             |
| Autenticado   | Todo lo demás (alta/edición de libros y categorías, imágenes, carrito)     |

- Sin token o con token inválido/vencido: `401 Unauthorized`. Sin el rol necesario: `403 Forbidden`.
  Ambos con el mismo formato de error que el resto de la API.
- Al arrancar se crea un administrador si no existe: `admin@ecommerce.com` / `admin123`
  (se cambia con las variables `ADMIN_EMAIL` / `ADMIN_PASSWORD`). La clave del JWT se puede pasar
  con la variable `JWT_SECRET`.
- Si la base ya tenía usuarios con contraseña en texto plano, se encriptan automáticamente al
  arrancar (`DataInitializer`), así siguen pudiendo loguearse.

Clases involucradas: `config/SecurityConfig`, `config/DataInitializer`, `security/JwtService`,
`security/JwtAuthenticationFilter`, `security/UsuarioDetailsService`, `security/SecurityErrorHandler`,
`controller/AuthController`, `service/AuthService`, `model/Role`, `model/Usuario` (implementa `UserDetails`).

## Tests automatizados

```powershell
.\mvnw.cmd test
```

Corre contra su propia base H2 en memoria (no requiere MySQL levantado ni la app corriendo) y
al finalizar muestra `Tests run: 31, Failures: 0, Errors: 0` y `BUILD SUCCESS`. Para correr una
sola clase: `.\mvnw.cmd test -Dtest=NombreDeLaClase`.

| Clase | Qué cubre |
|---|---|
| `ECommerceJuApplicationTests` | El contexto de Spring levanta correctamente |
| `AuthServiceTest` | Registro (rol por defecto, hash de password, duplicados, formato de email, largo mínimo) y login (token, credenciales inválidas) — con mocks |
| `CategoriaServiceTest` | Alta, nombre duplicado/vacío, categoría en uso, 404 — con mocks |
| `UsuarioServiceTest` | Consulta de usuarios — con mocks |
| `AuthControllerTest` | `/api/auth/register` y `/api/auth/login` de punta a punta (201/409/400/200/401) |
| `SecurityIntegrationTest` | Público vs. protegido vs. rol `ADMIN` (200/401/403), usando el admin que crea `DataInitializer` |

## Cómo probar la aplicación (Postman)

La forma de probar la aplicación es seguir la guía
[postman/GUIA-PRUEBAS-MANUAL.md](postman/GUIA-PRUEBAS-MANUAL.md), que detalla paso a paso
(método, URL, body y resultado esperado) las 36 solicitudes del flujo completo — registro, login,
categoría, libro, imágenes, carrito y checkout — con casos **positivos y negativos**, para
cargarlas a mano en Postman.

1. Levantar MySQL y crear las tablas siguiendo ["Instalar y levantar MySQL con
   Docker"](#instalar-y-levantar-mysql-con-docker) e ["Inicializar la base de datos y crear las
   tablas"](#inicializar-la-base-de-datos-y-crear-las-tablas) más arriba, y dejar la aplicación
   corriendo con el perfil por defecto (`.\mvnw.cmd spring-boot:run`).
2. Abrir [postman/GUIA-PRUEBAS-MANUAL.md](postman/GUIA-PRUEBAS-MANUAL.md) y crear cada solicitud
   en Postman siguiendo el método, la URL y el body indicados, en el orden numerado.
3. Anotar el `id` que devuelve cada respuesta (usuario, categoría, libro, imagen, ítem del carrito)
   para reemplazarlo en los pasos siguientes que lo referencian (`{{usuarioId}}`, `{{libroId}}`,
   etc., como se explica al principio de la guía).
4. Confirmar en cada paso que el código HTTP devuelto coincide con el "Esperado" de la guía.

También existen dos colecciones exportadas más chicas, enfocadas en un solo módulo, útiles para
pruebas puntuales:

- [postman/Usuarios.postman_collection.json](postman/Usuarios.postman_collection.json)
- [postman/Carrito-Demo.postman_collection.json](postman/Carrito-Demo.postman_collection.json)
  (pensada para usarse con el perfil `demo`, ver [DOCUMENTACION-CARRITO.md](DOCUMENTACION-CARRITO.md))

## Evidencia de las pruebas realizadas

Se verificó lo siguiente contra MySQL real (perfil por defecto):

- **Tablas y relaciones**: `SHOW TABLES` confirma las 6 tablas (`usuarios`, `categorias`, `libros`,
  `imagenes_libro`, `carritos`, `items_carrito`) y `information_schema.KEY_COLUMN_USAGE` confirma
  las 6 foreign keys esperadas, incluidas `libros.categoria_id -> categorias.id` y
  `libros.vendedor_id -> usuarios.id`.
- **Flujo completo** (registro → login → categoría → libro → imagen → carrito → checkout):
  probado dos veces, una vez encadenando los endpoints por script y otra vez a mano en Postman.
  En ambos casos el checkout descontó el stock correctamente (de 5 a 3 unidades vendidas) y el
  carrito quedó vacío después de la compra.
- **Casos negativos**: probados manualmente durante el desarrollo (email duplicado, credenciales
  inválidas, stock insuficiente, edición/borrado por un usuario que no es el vendedor, categoría
  con libros asociados), y quedaron documentados paso a paso en
  [postman/GUIA-PRUEBAS-MANUAL.md](postman/GUIA-PRUEBAS-MANUAL.md).


## Estructura del proyecto

```
src/main/java/com/uade/e_commerce_ju/
├── controller/   endpoints REST (Usuario, Categoria, Libro, ImagenLibro, Carrito)
├── service/      logica de negocio y validaciones
├── repository/   interfaces Spring Data JPA
├── model/        entidades JPA (Usuario, Categoria, Libro, ImagenLibro, Carrito, ItemCarrito)
├── dto/          DTOs de entrada/salida por endpoint
├── exception/    excepciones de dominio, manejadas por ApiExceptionHandler
├── security/     JWT: generación/validación del token, filtro y errores 401/403
└── config/       SecurityConfig (reglas de acceso, BCrypt, CORS) y DataInitializer

src/test/java/com/uade/e_commerce_ju/   tests unitarios (Mockito) e integración (MockMvc + H2)
```
