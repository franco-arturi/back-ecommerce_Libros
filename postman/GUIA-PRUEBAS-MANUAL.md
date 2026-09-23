# Guía de pruebas manuales en Postman

Cubre el flujo completo: registro → login → categoría → libro → imágenes → carrito → checkout,
con casos **positivos** y **negativos**, contra MySQL (perfil por defecto, la app corriendo en
`http://localhost:8080`).

> ⚠️ **Antes de empezar**: la base MySQL tiene que existir con sus tablas ya creadas. Ver en
> [README.md](../README.md) las secciones "Instalar y levantar MySQL con Docker" e "Inicializar la
> base de datos y crear las tablas" para levantar MySQL y arrancar la aplicación con
> `.\mvnw.cmd spring-boot:run` (perfil por defecto, **no** el perfil `demo`).

## Cómo usar esta guía

- Cada bloque indica: método, URL, body (si aplica) y el resultado esperado.
- Los bodies están listos para copiar y pegar tal cual, sin reemplazar nada.
- Reemplazar `{{usuarioId}}`, `{{categoriaId}}`, `{{libroId}}`, etc. por el `id` real que devuelve
  la respuesta del paso donde se creó ese recurso.
- `Content-Type: application/json` en todos los `POST`/`PUT` con body.
- El username/email/nombre de categoría usan `test` como sufijo fijo. Si ya corriste esta guía
  antes y los pasos 1.1, 1.8 o 2.1 te dan `409` (duplicado), cambiá `test` por otro texto (por
  ejemplo `test2`) en esos 3 pasos y en los que reutilizan el mismo email (1.4, 1.5) o nombre
  (2.2, 2.7).

## Índice

1. [Usuarios](#1---usuarios)
2. [Categorías](#2---categorias)
3. [Libros](#3---libros)
4. [Imágenes de libro](#4---imagenes-de-libro)
5. [Carrito y checkout](#5---carrito-y-checkout)
6. [Cierre: categoría en uso](#6---cierre-categoria-en-uso)

---

## 1 - Usuarios

### 1.1 Registrar usuario (vendedor/comprador) — ✅ POSITIVO

`POST http://localhost:8080/api/usuarios/register`

```json
{
  "username": "flujo_completo_test",
  "email": "flujo_completo_test@test.com",
  "password": "password123",
  "nombre": "Flujo",
  "apellido": "Completo"
}
```

**Esperado:** `201 Created`. Anotar el `id` devuelto como `{{usuarioId}}`.

### 1.2 Registrar usuario con email duplicado — ❌ NEGATIVO

`POST http://localhost:8080/api/usuarios/register`

Body: igual al anterior (mismo email, username distinto).

**Esperado:** `409 Conflict`.

### 1.3 Registrar usuario sin nombre — ❌ NEGATIVO

`POST http://localhost:8080/api/usuarios/register`

```json
{
  "username": "sin_nombre",
  "email": "sin_nombre@test.com",
  "password": "password123",
  "nombre": "",
  "apellido": "Apellido"
}
```

**Esperado:** `400 Bad Request`.

### 1.4 Login correcto — ✅ POSITIVO

`POST http://localhost:8080/api/usuarios/login`

```json
{
  "email": "flujo_completo_test@test.com",
  "password": "password123"
}
```

**Esperado:** `200 OK`, con el mismo `id` que `{{usuarioId}}`.

### 1.5 Login con credenciales inválidas — ❌ NEGATIVO

`POST http://localhost:8080/api/usuarios/login`

```json
{
  "email": "flujo_completo_test@test.com",
  "password": "password_incorrecta"
}
```

**Esperado:** `401 Unauthorized`.

### 1.6 Obtener usuario por id — ✅ POSITIVO

`GET http://localhost:8080/api/usuarios/{{usuarioId}}`

**Esperado:** `200 OK`.

### 1.7 Obtener usuario inexistente — ❌ NEGATIVO

`GET http://localhost:8080/api/usuarios/999999`

**Esperado:** `404 Not Found`.

### 1.8 Registrar segundo usuario (para casos de "no autorizado") — ✅ POSITIVO

`POST http://localhost:8080/api/usuarios/register`

```json
{
  "username": "otro_usuario_test",
  "email": "otro_usuario_test@test.com",
  "password": "password123",
  "nombre": "Otro",
  "apellido": "Usuario"
}
```

**Esperado:** `201 Created`. Anotar el `id` devuelto como `{{otroUsuarioId}}`.

---

## 2 - Categorias

### 2.1 Crear categoria — ✅ POSITIVO

`POST http://localhost:8080/api/categorias`

```json
{
  "nombre": "Novelas Flujo Completo test",
  "descripcion": "Categoria de prueba del flujo completo"
}
```

**Esperado:** `201 Created`. Anotar el `id` devuelto como `{{categoriaId}}`.

### 2.2 Crear categoria con nombre duplicado — ❌ NEGATIVO

`POST http://localhost:8080/api/categorias`

Body: igual al anterior (mismo nombre).

**Esperado:** `409 Conflict`.

### 2.3 Crear categoria sin nombre — ❌ NEGATIVO

`POST http://localhost:8080/api/categorias`

```json
{
  "nombre": "   ",
  "descripcion": "Invalida"
}
```

**Esperado:** `400 Bad Request`.

### 2.4 Listar categorias — ✅ POSITIVO

`GET http://localhost:8080/api/categorias`

**Esperado:** `200 OK`, un array.

### 2.5 Obtener categoria por id — ✅ POSITIVO

`GET http://localhost:8080/api/categorias/{{categoriaId}}`

**Esperado:** `200 OK`.

### 2.6 Obtener categoria inexistente — ❌ NEGATIVO

`GET http://localhost:8080/api/categorias/999999`

**Esperado:** `404 Not Found`.

### 2.7 Actualizar categoria — ✅ POSITIVO

`PUT http://localhost:8080/api/categorias/{{categoriaId}}`

```json
{
  "nombre": "Novelas Flujo Completo test",
  "descripcion": "Descripcion actualizada"
}
```

**Esperado:** `200 OK`, con `"descripcion": "Descripcion actualizada"`.

### 2.8 Eliminar categoria inexistente — ❌ NEGATIVO

`DELETE http://localhost:8080/api/categorias/999999`

**Esperado:** `404 Not Found`.

---

## 3 - Libros

### 3.1 Alta de libro — ✅ POSITIVO

`POST http://localhost:8080/api/libros`

```json
{
  "vendedorId": {{usuarioId}},
  "titulo": "Libro del flujo completo",
  "autor": "Autor de prueba",
  "descripcion": "Libro creado por la guia de flujo completo",
  "precio": 20000,
  "stock": 5,
  "categoriaId": {{categoriaId}}
}
```

**Esperado:** `201 Created`. Anotar el `id` devuelto como `{{libroId}}`.

### 3.2 Alta de libro con categoria inexistente — ❌ NEGATIVO

`POST http://localhost:8080/api/libros`

Body: igual al anterior pero `"categoriaId": 999999`.

**Esperado:** `404 Not Found`.

### 3.3 Alta de libro sin titulo — ❌ NEGATIVO

`POST http://localhost:8080/api/libros`

Body: igual al anterior pero `"titulo": ""`.

**Esperado:** `400 Bad Request`.

### 3.4 Listar libros — ✅ POSITIVO

`GET http://localhost:8080/api/libros`

**Esperado:** `200 OK`.

### 3.5 Listar libros filtrados por categoria — ✅ POSITIVO

`GET http://localhost:8080/api/libros?categoriaId={{categoriaId}}`

**Esperado:** `200 OK`, todos los libros devueltos tienen esa categoría.

### 3.6 Obtener libro por id — ✅ POSITIVO

`GET http://localhost:8080/api/libros/{{libroId}}`

**Esperado:** `200 OK`.

### 3.7 Obtener libro inexistente — ❌ NEGATIVO

`GET http://localhost:8080/api/libros/999999`

**Esperado:** `404 Not Found`.

### 3.8 Actualizar libro (dueño) — ✅ POSITIVO

`PUT http://localhost:8080/api/libros/{{libroId}}`

```json
{
  "vendedorId": {{usuarioId}},
  "titulo": "Libro del flujo completo (editado)",
  "autor": "Autor de prueba",
  "descripcion": "Descripcion editada",
  "precio": 22000,
  "categoriaId": {{categoriaId}}
}
```

**Esperado:** `200 OK`, `titulo` actualizado.

### 3.9 Actualizar libro siendo otro usuario — ❌ NEGATIVO

`PUT http://localhost:8080/api/libros/{{libroId}}`

Body: igual al anterior pero `"vendedorId": {{otroUsuarioId}}`.

**Esperado:** `403 Forbidden`.

### 3.10 Actualizar stock (dueño) — ✅ POSITIVO

`PUT http://localhost:8080/api/libros/{{libroId}}/stock`

```json
{
  "vendedorId": {{usuarioId}},
  "stock": 5
}
```

**Esperado:** `200 OK`, `"stock": 5`.

### 3.11 Actualizar stock siendo otro usuario — ❌ NEGATIVO

`PUT http://localhost:8080/api/libros/{{libroId}}/stock`

```json
{
  "vendedorId": {{otroUsuarioId}},
  "stock": 100
}
```

**Esperado:** `403 Forbidden`.

### 3.12 Alta de libro descartable (para probar eliminar) — ✅ POSITIVO

`POST http://localhost:8080/api/libros`

```json
{
  "vendedorId": {{usuarioId}},
  "titulo": "Libro descartable",
  "autor": "Autor",
  "descripcion": "Se crea solo para probar el borrado",
  "precio": 500,
  "stock": 1,
  "categoriaId": {{categoriaId}}
}
```

**Esperado:** `201 Created`. Anotar el `id` devuelto como `{{libroDescartableId}}`.

### 3.13 Eliminar libro siendo otro usuario — ❌ NEGATIVO

`DELETE http://localhost:8080/api/libros/{{libroDescartableId}}?vendedorId={{otroUsuarioId}}`

**Esperado:** `403 Forbidden`.

### 3.14 Eliminar libro descartable (dueño) — ✅ POSITIVO

`DELETE http://localhost:8080/api/libros/{{libroDescartableId}}?vendedorId={{usuarioId}}`

**Esperado:** `204 No Content`.

---

## 4 - Imagenes de libro

### 4.1 Agregar imagen — ✅ POSITIVO

`POST http://localhost:8080/api/libros/{{libroId}}/imagenes`

```json
{
  "url": "https://images.demo.com/flujo-completo-portada.jpg"
}
```

**Esperado:** `201 Created`. Anotar el `id` devuelto como `{{imagenId}}`.

### 4.2 Agregar imagen con URL inválida — ❌ NEGATIVO

`POST http://localhost:8080/api/libros/{{libroId}}/imagenes`

```json
{
  "url": "no-es-una-url"
}
```

**Esperado:** `400 Bad Request`.

### 4.3 Agregar imagen duplicada — ❌ NEGATIVO

`POST http://localhost:8080/api/libros/{{libroId}}/imagenes`

Body: igual al 4.1 (misma URL).

**Esperado:** `409 Conflict`.

### 4.4 Listar imagenes del libro — ✅ POSITIVO

`GET http://localhost:8080/api/libros/{{libroId}}/imagenes`

**Esperado:** `200 OK`, al menos 1 imagen.

### 4.5 Eliminar imagen inexistente — ❌ NEGATIVO

`DELETE http://localhost:8080/api/libros/{{libroId}}/imagenes/999999`

**Esperado:** `404 Not Found`.

### 4.6 Eliminar imagen — ✅ POSITIVO

`DELETE http://localhost:8080/api/libros/{{libroId}}/imagenes/{{imagenId}}`

**Esperado:** `204 No Content`.

---

## 5 - Carrito y checkout

### 5.1 Agregar libro al carrito — ✅ POSITIVO

`POST http://localhost:8080/api/carritos/{{usuarioId}}/items`

```json
{
  "libroId": {{libroId}},
  "cantidad": 2
}
```

**Esperado:** `200 OK`. Anotar el `id` del item dentro de `items` como `{{itemId}}`.

### 5.2 Agregar item con cantidad inválida — ❌ NEGATIVO

`POST http://localhost:8080/api/carritos/{{usuarioId}}/items`

```json
{
  "libroId": {{libroId}},
  "cantidad": 0
}
```

**Esperado:** `400 Bad Request`.

### 5.3 Agregar item de un libro inexistente — ❌ NEGATIVO

`POST http://localhost:8080/api/carritos/{{usuarioId}}/items`

```json
{
  "libroId": 999999,
  "cantidad": 1
}
```

**Esperado:** `404 Not Found`.

### 5.4 Agregar item con stock insuficiente — ❌ NEGATIVO

`POST http://localhost:8080/api/carritos/{{usuarioId}}/items`

```json
{
  "libroId": {{libroId}},
  "cantidad": 999
}
```

**Esperado:** `409 Conflict`.

### 5.5 Consultar carrito — ✅ POSITIVO

`GET http://localhost:8080/api/carritos/{{usuarioId}}`

**Esperado:** `200 OK`, al menos 1 item.

### 5.6 Modificar cantidad del item — ✅ POSITIVO

`PUT http://localhost:8080/api/carritos/{{usuarioId}}/items/{{itemId}}`

```json
{
  "cantidad": 3
}
```

**Esperado:** `200 OK`, cantidad del item = 3.

### 5.7 Checkout de un usuario sin carrito — ❌ NEGATIVO

`POST http://localhost:8080/api/carritos/{{otroUsuarioId}}/checkout` (sin body)

`{{otroUsuarioId}}` nunca agregó items, no tiene carrito creado.

**Esperado:** `404 Not Found` (no `400`: no existe carrito, distinto de "existe pero vacío").

### 5.8 Checkout exitoso — ✅ POSITIVO

`POST http://localhost:8080/api/carritos/{{usuarioId}}/checkout` (sin body)

**Esperado:** `200 OK`, con `"total": 66000.0` y mensaje de éxito.

### 5.9 Verificar que el carrito quedó vacío — ✅ POSITIVO

`GET http://localhost:8080/api/carritos/{{usuarioId}}`

**Esperado:** `200 OK`, `items` es un array vacío.

### 5.10 Verificar que el stock del libro se descontó — ✅ POSITIVO

`GET http://localhost:8080/api/libros/{{libroId}}`

**Esperado:** `200 OK`, `"stock": 2` (5 inicial - 3 comprados en el paso 5.6/5.8).

### 5.11 Checkout de un carrito existente pero vacío — ❌ NEGATIVO

`POST http://localhost:8080/api/carritos/{{usuarioId}}/checkout` (sin body)

Se ejecuta después del 5.8: el carrito ya existe pero está vacío.

**Esperado:** `400 Bad Request`.

---

## 6 - Cierre: categoria en uso

### 6.1 Eliminar categoria que todavía tiene un libro — ❌ NEGATIVO

`DELETE http://localhost:8080/api/categorias/{{categoriaId}}`

**Esperado:** `409 Conflict` (el libro `{{libroId}}` sigue usando esa categoría).

---

**Fin de la guía.** Con estos 36 pasos quedan cubiertos: registro, login, categorías, libros,
imágenes, carrito y checkout, con casos positivos y negativos, tal como pide el punto 3.8 del TP.
