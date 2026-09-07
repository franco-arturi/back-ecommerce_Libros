# Carrito de compras - guía de ejecución y prueba

Esta guía explica cómo levantar el backend con datos precargados y probar el
módulo de carrito desde Postman. El perfil `demo` usa una base H2 en memoria,
por lo que no requiere instalar ni configurar MySQL.

## Requisitos

- Java 21.
- Postman.
- Puerto `8080` disponible.

## Iniciar el entorno demo

Desde PowerShell, ubicarse en la raíz del repositorio y ejecutar:

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

La aplicación está disponible cuando la consola muestra:

```text
Started ECommerceJuApplication
```

La URL base del módulo es:

```text
http://localhost:8080/api/carritos
```

Los datos se reinician cada vez que se detiene y vuelve a iniciar la aplicación.
El perfil normal del proyecto continúa usando MySQL; H2 solamente se activa con
el perfil `demo`.

## Datos precargados

### Usuarios

| ID | Uso sugerido |
|---:|---|
| 1 | Carrito principal de prueba |
| 2 | Segundo carrito o carrito vacío |

### Libros

| ID | Título | Precio | Stock |
|---:|---|---:|---:|
| 1 | Clean Code | 25000.00 | 5 |
| 2 | El Principito | 15000.00 | 10 |
| 3 | Libro sin stock | 12000.00 | 0 |

## Configurar Postman

Crear una colección y agregar la variable:

```text
baseUrl = http://localhost:8080
```

Para las solicitudes `POST` y `PUT`, elegir `Body`, `raw`, `JSON`. Postman
agregará el encabezado `Content-Type: application/json`.

## Consultar un carrito

```http
GET {{baseUrl}}/api/carritos/1
```

Un usuario sin ítems devuelve `200 OK`:

```json
{
  "id": null,
  "usuarioId": 1,
  "items": [],
  "total": 0
}
```

Si el carrito ya fue creado, `id` tendrá un número aunque `items` esté vacío.

## Agregar un libro

```http
POST {{baseUrl}}/api/carritos/1/items
```

```json
{
  "libroId": 1,
  "cantidad": 2
}
```

La respuesta `200 OK` incluye el `id` del ítem, el subtotal y el total:

```json
{
  "id": 1,
  "usuarioId": 1,
  "items": [
    {
      "id": 1,
      "libroId": 1,
      "titulo": "Clean Code",
      "precioUnitario": 25000.0,
      "cantidad": 2,
      "subtotal": 50000.0
    }
  ],
  "total": 50000.0
}
```

Si se vuelve a agregar el libro `1`, el sistema incrementa la cantidad del ítem
existente. No crea una fila duplicada.

## Modificar la cantidad

Reemplazar `1` al final de la URL por el `id` del ítem obtenido en la respuesta
anterior:

```http
PUT {{baseUrl}}/api/carritos/1/items/1
```

```json
{
  "cantidad": 3
}
```

La respuesta esperada es `200 OK` con la cantidad, subtotal y total actualizados.

## Eliminar un ítem

```http
DELETE {{baseUrl}}/api/carritos/1/items/1
```

La respuesta esperada es `204 No Content`.

## Vaciar el carrito

```http
DELETE {{baseUrl}}/api/carritos/1
```

La respuesta esperada es `204 No Content`. El carrito permanece creado, pero
su lista de ítems queda vacía.

## Casos de error

### Libro sin stock

```http
POST {{baseUrl}}/api/carritos/1/items
```

```json
{
  "libroId": 3,
  "cantidad": 1
}
```

Resultado esperado: `409 Conflict`.

### Cantidad superior al stock

```json
{
  "libroId": 1,
  "cantidad": 6
}
```

Resultado esperado: `409 Conflict`, porque el libro `1` tiene stock `5`.

### Cantidad inválida

```json
{
  "libroId": 1,
  "cantidad": 0
}
```

Resultado esperado: `400 Bad Request`.

### Usuario o libro inexistente

```http
GET {{baseUrl}}/api/carritos/999
```

```json
{
  "libroId": 999,
  "cantidad": 1
}
```

Resultado esperado: `404 Not Found`.

Los errores tienen esta estructura:

```json
{
  "timestamp": "2026-09-07T21:54:26Z",
  "status": 409,
  "error": "Conflict",
  "mensaje": "Stock insuficiente para el libro 3: disponible 0, solicitado 1",
  "path": "/api/carritos/1/items"
}
```

## Consola H2 opcional

Mientras el perfil demo está activo se puede abrir:

```text
http://localhost:8080/h2-console
```

Usar los siguientes datos:

```text
JDBC URL: jdbc:h2:mem:ecommerce-demo
User Name: sa
Password: dejar vacío
```

Consultas útiles:

```sql
SELECT * FROM usuarios;
SELECT * FROM libros;
SELECT * FROM carritos;
SELECT * FROM items_carrito;
```

## Ejecutar las pruebas automatizadas

Detener primero la aplicación con `Ctrl+C` si se quiere liberar la consola y
después ejecutar:

```powershell
.\mvnw.cmd clean test
```

La suite cubre servicio, controlador, errores HTTP, contexto de Spring y un flujo
de integración completo con H2.

## Detener y reiniciar

- Para detener el backend: `Ctrl+C` en la consola donde está ejecutándose.
- Para restaurar los datos originales: detenerlo y volver a ejecutar el comando
  del perfil `demo`.

Agregar productos al carrito solamente valida el stock; no lo descuenta. El
descuento definitivo corresponde al módulo de checkout.
