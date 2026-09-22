package com.uade.e_commerce_ju.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.uade.e_commerce_ju.dto.categoria.CategoriaResponseDTO;
import com.uade.e_commerce_ju.model.Categoria;
import com.uade.e_commerce_ju.repository.CategoriaRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository CategoriaRepository;

    public CategoriaService(CategoriaRepository CategoriaRepository) {
        this.CategoriaRepository = CategoriaRepository;
    }

    public List<CategoriaResponseDTO> getAllCategorias() {
        // select * from Categorias
        return CategoriaRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    //busqueda por id
    public Optional<CategoriaResponseDTO> getCategoriaById(Long id) {
        return CategoriaRepository.findById(id)
            .map(this::toResponse);
    }

    private CategoriaResponseDTO toResponse(Categoria categoria) {
        return new CategoriaResponseDTO(
            categoria.getId(),
            categoria.getNombre(),
            categoria.getDescripcion()
        );
    }

}