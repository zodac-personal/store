package com.example.store.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, productMapper);
    }

    @Test
    void getAllProducts_returnsMappedProducts() {
        final Product product = new Product();
        product.setId(1L);
        final ProductDTO productDTO = new ProductDTO(1L, "Chair", List.of());

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.productsToProductDTOs(List.of(product))).thenReturn(List.of(productDTO));

        final List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).containsExactly(productDTO);
    }

    @Test
    void getAllProductsPaginated_returnsMappedPage() {
        final Product product = new Product();
        product.setId(1L);
        final ProductDTO productDTO = new ProductDTO(1L, "Chair", List.of());
        final Pageable pageable = PageRequest.of(0, 20);
        final Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.productToProductDTO(product)).thenReturn(productDTO);

        final Page<ProductDTO> result = productService.getAllProducts(pageable);

        assertThat(result.getContent()).containsExactly(productDTO);
    }

    @Test
    void getProductById_returnsMappedProduct_whenFound() {
        final Product product = new Product();
        product.setId(1L);
        final ProductDTO productDTO = new ProductDTO(1L, "Chair", List.of());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.productToProductDTO(product)).thenReturn(productDTO);

        final ProductDTO result = productService.getProductById(1L);

        assertThat(result).isEqualTo(productDTO);
    }

    @Test
    void getProductById_throwsNotFound_whenMissing() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> productService.getProductById(404L))
                .satisfies(exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createProduct_savesAndReturnsMappedProduct() {
        final Product product = new Product();
        product.setDescription("Chair");
        final Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setDescription("Chair");
        final ProductDTO productDTO = new ProductDTO(1L, "Chair", List.of());

        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.productToProductDTO(savedProduct)).thenReturn(productDTO);

        final ProductDTO result = productService.createProduct(product);

        assertThat(result).isEqualTo(productDTO);
    }
}
