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

// Real Postgres via Testcontainers, not mocks, so the actual Liquibase migrations run.
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
}
