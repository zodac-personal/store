package com.example.store.customer;

import com.example.store.order.Order;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTests {

    private final CustomerMapper customerMapper = new CustomerMapperImpl();

    @Test
    void customerToCustomerDTO_mapsIdAndName() {
        final Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        final CustomerDTO customerDTO = customerMapper.customerToCustomerDTO(customer);

        assertThat(customerDTO.id()).isEqualTo(1L);
        assertThat(customerDTO.name()).isEqualTo("John Doe");
    }

    @Test
    void customerToCustomerDTO_mapsOrdersToCustomerOrderDTOs() {
        final Order order = new Order();
        order.setId(5L);
        order.setDescription("Chair");

        final Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setOrders(List.of(order));

        final CustomerDTO customerDTO = customerMapper.customerToCustomerDTO(customer);

        assertThat(customerDTO.orders()).hasSize(1);
        assertThat(customerDTO.orders().getFirst().id()).isEqualTo(5L);
        assertThat(customerDTO.orders().getFirst().description()).isEqualTo("Chair");
    }

    @Test
    void customersToCustomerDTOs_mapsEachCustomer() {
        final Customer first = new Customer();
        first.setId(1L);
        final Customer second = new Customer();
        second.setId(2L);

        final List<CustomerDTO> customerDTOs = customerMapper.customersToCustomerDTOs(List.of(first, second));

        assertThat(customerDTOs).extracting(CustomerDTO::id).containsExactly(1L, 2L);
    }
}
