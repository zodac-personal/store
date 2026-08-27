package com.example.store.product;

import com.example.store.order.Order;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO productToProductDTO(Product product);

    List<ProductDTO> productsToProductDTOs(List<Product> products);

    default Long orderToOrderId(Order order) {
        return order.getId();
    }
}
