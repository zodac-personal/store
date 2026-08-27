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

        assertThat(orderDTO.getId()).isEqualTo(1L);
        assertThat(orderDTO.getDescription()).isEqualTo("Chair");
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

        assertThat(orderDTO.getCustomer().getId()).isEqualTo(2L);
        assertThat(orderDTO.getCustomer().getName()).isEqualTo("John Doe");
    }

    @Test
    void ordersToOrderDTOs_mapsEachOrder() {
        final Order first = new Order();
        first.setId(1L);
        final Order second = new Order();
        second.setId(2L);

        final List<OrderDTO> orderDTOs = orderMapper.ordersToOrderDTOs(List.of(first, second));

        assertThat(orderDTOs).extracting(OrderDTO::getId).containsExactly(1L, 2L);
    }
}
