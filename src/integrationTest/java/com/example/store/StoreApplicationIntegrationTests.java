package com.example.store;

import com.example.store.customer.CustomerDTO;
import com.example.store.order.OrderDTO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class StoreApplicationIntegrationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16.2");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAllCustomers_returnsCustomersSeededByLiquibase() {
        final ResponseEntity<CustomerDTO[]> response = restTemplate.getForEntity("/customer", CustomerDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(CustomerDTO::getName)
                .contains("Muriel Donnelly");
    }

    @Test
    void getAllOrders_returnsOrdersWithTheirCustomer() {
        final ResponseEntity<OrderDTO[]> response = restTemplate.getForEntity("/order", OrderDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isNotEmpty()
                .allSatisfy(order -> assertThat(order.getCustomer()).isNotNull());
    }

    @Test
    void getOrderById_returnsOrderWithItsCustomer_whenFound() {
        final ResponseEntity<OrderDTO> response = restTemplate.getForEntity("/order/1", OrderDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCustomer()).isNotNull();
    }

    @Test
    void getCustomers_matchesWordPrefix_whenNameQueryProvided() {
        final ResponseEntity<CustomerDTO[]> response =
                restTemplate.getForEntity("/customer?name=don", CustomerDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(CustomerDTO::getName)
                .contains("Muriel Donnelly")
                .allSatisfy(name -> assertThat(name.toLowerCase()).contains("don"));
    }

    @Test
    void getCustomers_matchesMidWordSubstring_whenNameQueryProvided() {
        final ResponseEntity<CustomerDTO[]> response =
                restTemplate.getForEntity("/customer?name=nell", CustomerDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(CustomerDTO::getName)
                .contains("Muriel Donnelly");
    }

    @Test
    void getCustomers_returnsEmpty_whenNameQueryMatchesNoWord() {
        final ResponseEntity<CustomerDTO[]> response =
                restTemplate.getForEntity("/customer?name=zzqzz", CustomerDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void getOrderById_returnsNotFound_whenMissing() {
        final ResponseEntity<OrderDTO> response = restTemplate.getForEntity("/order/999999", OrderDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllCustomers_returnsFullUnpagedList_whenPageAndSizeOmitted() {
        final ResponseEntity<CustomerDTO[]> response = restTemplate.getForEntity("/customer", CustomerDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("X-Total-Count")).isNull();
        assertThat(response.getBody()).isNotNull().hasSizeGreaterThan(20);
    }

    @Test
    void getAllCustomers_returnsBoundedPageWithHeaders_whenPageAndSizeProvided() {
        final ResponseEntity<CustomerDTO[]> response =
                restTemplate.getForEntity("/customer?page=0&size=5", CustomerDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(5);
        assertThat(response.getHeaders().getFirst("X-Total-Count")).isNotNull();
        assertThat(response.getHeaders().getFirst("X-Total-Pages")).isNotNull();
    }

    @Test
    void getAllOrders_returnsFullUnpagedList_whenPageAndSizeOmitted() {
        final ResponseEntity<OrderDTO[]> response = restTemplate.getForEntity("/order", OrderDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("X-Total-Count")).isNull();
        assertThat(response.getBody()).isNotNull().hasSizeGreaterThan(20);
    }

    @Test
    void getAllOrders_returnsBoundedPageWithHeaders_whenPageAndSizeProvided() {
        final ResponseEntity<OrderDTO[]> response = restTemplate.getForEntity("/order?page=0&size=5", OrderDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(5);
        assertThat(response.getHeaders().getFirst("X-Total-Count")).isNotNull();
        assertThat(response.getHeaders().getFirst("X-Total-Pages")).isNotNull();
    }
}
