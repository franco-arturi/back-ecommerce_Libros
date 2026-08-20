/* 2026-08-20 19:59:30 [28 ms] */ 
CREATE TABLE Libro(  
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary Key',
    titulo VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    fecha_publicacion DATE NOT NULL,
    precio DECIMAL(10,2) NOT NULL
);
