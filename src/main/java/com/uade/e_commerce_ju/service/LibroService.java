package com.uade.e_commerce_ju.service;

import org.springframework.stereotype.Service;

import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.repository.LibroRepository;

@Service
public class LibroService {

	private final LibroRepository libroRepository;

	public LibroService(LibroRepository libroRepository) {
		this.libroRepository = libroRepository;
	}

	public Libro getLibroById(Long id) {
		return libroRepository.findById(id).orElse(null);
	}
}
