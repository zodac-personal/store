package com.example.store.product;

import com.example.store.order.Order;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTests {

    private final ProductMapper productMapper = new ProductMapperImpl();

    @Test
    void productToProductDTO_mapsIdAndDescription() {
        final Product product = new Product();
        product.setId(1L);
        product.setDescription("Chair");

        final ProductDTO productDTO = productMapper.productToProductDTO(product);

        assertThat(productDTO.id()).isEqualTo(1L);
        assertThat(productDTO.description()).isEqualTo("Chair");
    }

    @Test
    void productToProductDTO_mapsOrdersToTheirIds() {
        final Order firstOrder = new Order();
        firstOrder.setId(10L);
        final Order secondOrder = new Order();
        secondOrder.setId(20L);

        final Product product = new Product();
        product.setId(1L);
        product.setDescription("Chair");
        product.setOrders(Set.of(firstOrder, secondOrder));

        final ProductDTO productDTO = productMapper.productToProductDTO(product);

        assertThat(productDTO.orders()).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void productToProductDTO_mapsEmptyOrdersToEmptyList() {
        final Product product = new Product();
        product.setId(1L);
        product.setDescription("Chair");

        final ProductDTO productDTO = productMapper.productToProductDTO(product);

        assertThat(productDTO.orders()).isEmpty();
    }

    @Test
    void productsToProductDTOs_mapsEachProduct() {
        final Product first = new Product();
        first.setId(1L);
        first.setDescription("Chair");
        final Product second = new Product();
        second.setId(2L);
        second.setDescription("Table");

        final List<ProductDTO> productDTOs = productMapper.productsToProductDTOs(List.of(first, second));

        assertThat(productDTOs).extracting(ProductDTO::id).containsExactly(1L, 2L);
    }
}
