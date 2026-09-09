package com.uade.e_commerce_ju.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

	private Long id;
	private String titulo;
	private String autor;
	private double precio;
	private Integer stock;

	private String descripcion;
	private String categoria;
	private String imagenes;

	@ManyToOne(optional = false)
	@JoinColumn(name = "vendedor_id", nullable = false)
	private Usuario vendedor;
}
