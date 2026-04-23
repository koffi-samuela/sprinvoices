package com.example.sprinvoices.service;

import com.example.sprinvoices.models.Product;
import com.example.sprinvoices.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable : " + id));
    }

    @Transactional
    public void create(Product product) {
        productRepository.save(product);
    }

    @Transactional
    public void update(Long id, Product updated) {
        Product existing = findById(id);
        existing.setDesignation(updated.getDesignation());
        existing.setUnitPrice(updated.getUnitPrice());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        productRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}