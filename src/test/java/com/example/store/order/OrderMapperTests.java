package com.example.store.order;

import com.example.store.customer.Customer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTests {

    private final OrderMapper orderMapper = new OrderMapperImpl();

    @Test
    void orderToOrderDTO_mapsIdAndDescription() {
        final Order order = new Order();
        order.setId(1L);
        order.setDescription("Chair");

        final OrderDTO orderDTO = orderMapper.orderToOrderDTO(order);

        assertThat(orderDTO.id()).isEqualTo(1L);
        assertThat(orderDTO.description()).isEqualTo("Chair");
    }

    @Test
    void orderToOrderDTO_mapsCustomerToOrderCustomerDTO() {
        final Customer customer = new Customer();
        customer.setId(2L);
        customer.setName("John Doe");

        final Order order = new Order();
        order.setId(1L);
        order.setDescription("Chair");
        order.setCustomer(customer);

        final OrderDTO orderDTO = orderMapper.orderToOrderDTO(order);

        assertThat(orderDTO.customer().id()).isEqualTo(2L);
        assertThat(orderDTO.customer().name()).isEqualTo("John Doe");
    }

    @Test
    void ordersToOrderDTOs_mapsEachOrder() {
        final Order first = new Order();
        first.setId(1L);
        final Order second = new Order();
        second.setId(2L);

        final List<OrderDTO> orderDTOs = orderMapper.ordersToOrderDTOs(List.of(first, second));

        assertThat(orderDTOs).extracting(OrderDTO::id).containsExactly(1L, 2L);
    }
}
