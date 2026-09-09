package com.example.store.customer;

import com.example.store.order.OrderRepository;

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
class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private OrderRepository orderRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, customerMapper, orderRepository);
    }

    @Test
    void getAllCustomers_returnsAllCustomers_whenNameIsBlank() {
        final Customer customer = new Customer();
        customer.setId(1L);
        final CustomerDTO customerDTO = new CustomerDTO(1L, null, null);

        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(customerMapper.customersToCustomerDTOs(List.of(customer))).thenReturn(List.of(customerDTO));

        final List<CustomerDTO> result = customerService.getAllCustomers(" ");

        assertThat(result).containsExactly(customerDTO);
    }

    @Test
    void getAllCustomers_filtersByName_whenNameIsProvided() {
        final Customer customer = new Customer();
        customer.setId(1L);
        final CustomerDTO customerDTO = new CustomerDTO(1L, null, null);

        when(customerRepository.findByNamePartialMatch("john")).thenReturn(List.of(customer));
        when(customerMapper.customersToCustomerDTOs(List.of(customer))).thenReturn(List.of(customerDTO));

        final List<CustomerDTO> result = customerService.getAllCustomers("john");

        assertThat(result).containsExactly(customerDTO);
    }

    @Test
    void getAllCustomersPaginated_returnsAllCustomers_whenNameIsBlank() {
        final Customer customer = new Customer();
        customer.setId(1L);
        final CustomerDTO customerDTO = new CustomerDTO(1L, null, null);
        final Pageable pageable = PageRequest.of(0, 20);
        final Page<Customer> customerPage = new PageImpl<>(List.of(customer), pageable, 1);

        when(customerRepository.findAll(pageable)).thenReturn(customerPage);
        when(customerMapper.customerToCustomerDTO(customer)).thenReturn(customerDTO);

        final Page<CustomerDTO> result = customerService.getAllCustomers(null, pageable);

        assertThat(result.getContent()).containsExactly(customerDTO);
    }

    @Test
    void getAllCustomersPaginated_filtersByName_whenNameIsProvided() {
        final Customer customer = new Customer();
        customer.setId(1L);
        final CustomerDTO customerDTO = new CustomerDTO(1L, null, null);
        final Pageable pageable = PageRequest.of(0, 20);
        final Page<Customer> customerPage = new PageImpl<>(List.of(customer), pageable, 1);

        when(customerRepository.findByNamePartialMatch("john", pageable)).thenReturn(customerPage);
        when(customerMapper.customerToCustomerDTO(customer)).thenReturn(customerDTO);

        final Page<CustomerDTO> result = customerService.getAllCustomers("john", pageable);

        assertThat(result.getContent()).containsExactly(customerDTO);
    }

    @Test
    void getCustomerDetails_returnsNameTotalAndOrderPage_whenCustomerFound() {
        final Pageable pageable = PageRequest.of(0, 20);
        final CustomerRepository.CustomerOrderStats stats = new CustomerRepository.CustomerOrderStats() {
            @Override
            public String getName() {
                return "John Doe";
            }

            @Override
            public long getTotalOrders() {
                return 42L;
            }
        };
        final CustomerOrderDTO orderDTO = new CustomerOrderDTO(1L, "Chair");

        when(customerRepository.findOrderStatsByCustomerId(1L)).thenReturn(Optional.of(stats));
        when(orderRepository.findCustomerOrderDTOByCustomerId(1L, pageable)).thenReturn(List.of(orderDTO));

        final CustomerDetailsDTO result = customerService.getCustomerDetails(1L, pageable);

        assertThat(result).isEqualTo(new CustomerDetailsDTO("John Doe", 42L, List.of(orderDTO)));
    }

    @Test
    void getCustomerDetails_throwsNotFound_whenCustomerMissing() {
        final Pageable pageable = PageRequest.of(0, 20);

        when(customerRepository.findOrderStatsByCustomerId(404L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> customerService.getCustomerDetails(404L, pageable))
                .satisfies(exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createCustomer_savesAndReturnsMappedCustomer() {
        final Customer customer = new Customer();
        customer.setName("John Doe");
        final Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName("John Doe");
        final CustomerDTO customerDTO = new CustomerDTO(1L, "John Doe", null);

        when(customerRepository.save(customer)).thenReturn(savedCustomer);
        when(customerMapper.customerToCustomerDTO(savedCustomer)).thenReturn(customerDTO);

        final CustomerDTO result = customerService.createCustomer(customer);

        assertThat(result).isEqualTo(customerDTO);
    }
}
