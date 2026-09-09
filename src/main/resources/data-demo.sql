INSERT INTO usuarios (id, username, email, password, nombre, apellido)
VALUES (1, 'axel', 'axel@demo.com', 'password123', 'Axel', 'Antognoli');

INSERT INTO usuarios (id, username, email, password, nombre, apellido)
VALUES (2, 'tito', 'roso@demo.com', 'password456', 'tito', 'roso');

INSERT INTO libros (id, titulo, autor, precio, stock)
VALUES (1, 'Clean Code', 'Robert C. Martin', 25000.00, 5);

INSERT INTO libros (id, titulo, autor, precio, stock)
VALUES (2, 'El Principito', 'Antoine de Saint-Exupery', 15000.00, 10);

INSERT INTO libros (id, titulo, autor, precio, stock)
VALUES (3, 'Libro sin stock', 'Autor de prueba', 12000.00, 0);

-- el id lo asigna la base: si se fija a mano, la secuencia IDENTITY queda
-- desfasada y el primer alta por API choca con la clave primaria
INSERT INTO imagenes_libro (libro_id, url)
VALUES (1, 'https://images.demo.com/clean-code-portada.jpg');

INSERT INTO imagenes_libro (libro_id, url)
VALUES (1, 'https://images.demo.com/clean-code-contratapa.jpg');

INSERT INTO imagenes_libro (libro_id, url)
VALUES (2, 'https://images.demo.com/el-principito-portada.jpg');
