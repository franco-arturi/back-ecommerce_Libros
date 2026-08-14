package com.uade.e_commerce_ju.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

    public List<Categoria> getAllCategorias() {
        // select * from Categorias
        return CategoriaRepository.findAll();
    }


    
}