-- Los INSERT no fijan el id a mano: al hacerlo, la secuencia IDENTITY de H2
-- queda desfasada y la primera alta hecha por la API choca contra la clave
-- primaria. Como el script corre en orden, los ids quedan igual (1, 2, 3...).

INSERT INTO usuarios (username, email, password, nombre, apellido)
VALUES ('axel', 'axel@demo.com', 'password123', 'Axel', 'Antognoli');

INSERT INTO usuarios (username, email, password, nombre, apellido)
VALUES ('tito', 'roso@demo.com', 'password456', 'tito', 'roso');

INSERT INTO libros (vendedor_id, titulo, autor, precio, stock)
VALUES (1, 'Clean Code', 'Robert C. Martin', 25000.00, 5);

INSERT INTO libros (vendedor_id, titulo, autor, precio, stock)
VALUES (1, 'El Principito', 'Antoine de Saint-Exupery', 15000.00, 10);

INSERT INTO libros (vendedor_id, titulo, autor, precio, stock)
VALUES (2, 'Libro sin stock', 'Autor de prueba', 12000.00, 0);

INSERT INTO imagenes_libro (libro_id, url)
VALUES (1, 'https://images.demo.com/clean-code-portada.jpg');

INSERT INTO imagenes_libro (libro_id, url)
VALUES (1, 'https://images.demo.com/clean-code-contratapa.jpg');

INSERT INTO imagenes_libro (libro_id, url)
VALUES (2, 'https://images.demo.com/el-principito-portada.jpg');
