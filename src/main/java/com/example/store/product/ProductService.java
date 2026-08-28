package com.example.store.product;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Cacheable(cacheNames = "products")
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productMapper.productsToProductDTOs(productRepository.findAll());
    }

    @Cacheable(cacheNames = "products")
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::productToProductDTO);
    }

    @Cacheable(cacheNames = "products")
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productRepository
                .findById(id)
                .map(productMapper::productToProductDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    @Transactional
    public ProductDTO createProduct(Product product) {
        return productMapper.productToProductDTO(productRepository.save(product));
    }
}
