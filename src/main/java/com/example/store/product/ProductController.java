package com.example.store.product;

import com.example.store.config.PageRequestResolver;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final PageRequestResolver pageRequestResolver;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(productService.getAllProducts());
        }

        final Pageable pageable = pageRequestResolver.resolve(page, size);
        final Page<ProductDTO> productPage = productService.getAllProducts(pageable);
        return ResponseEntity.ok()
                .header(PageRequestResolver.TOTAL_COUNT_HEADER, String.valueOf(productPage.getTotalElements()))
                .header(PageRequestResolver.TOTAL_PAGES_HEADER, String.valueOf(productPage.getTotalPages()))
                .body(productPage.getContent());
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductDTO getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }
}
