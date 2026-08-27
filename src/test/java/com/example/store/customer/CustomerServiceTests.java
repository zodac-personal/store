package com.example.store.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, customerMapper);
    }

    @Test
    void getAllCustomers_returnsAllCustomers_whenNameIsBlank() {
        final Customer customer = new Customer();
        customer.setId(1L);
        final CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L);

        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(customerMapper.customersToCustomerDTOs(List.of(customer))).thenReturn(List.of(customerDTO));

        final List<CustomerDTO> result = customerService.getAllCustomers(" ");

        assertThat(result).containsExactly(customerDTO);
    }

    @Test
    void getAllCustomers_filtersByName_whenNameIsProvided() {
        final Customer customer = new Customer();
        customer.setId(1L);
        final CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L);

        when(customerRepository.findByNamePartialMatch("john")).thenReturn(List.of(customer));
        when(customerMapper.customersToCustomerDTOs(List.of(customer))).thenReturn(List.of(customerDTO));

        final List<CustomerDTO> result = customerService.getAllCustomers("john");

        assertThat(result).containsExactly(customerDTO);
    }

    @Test
    void getAllCustomersPaginated_returnsAllCustomers_whenNameIsBlank() {
        final Customer customer = new Customer();
        customer.setId(1L);
        final CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
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
        final CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        final Pageable pageable = PageRequest.of(0, 20);
        final Page<Customer> customerPage = new PageImpl<>(List.of(customer), pageable, 1);

        when(customerRepository.findByNamePartialMatch("john", pageable)).thenReturn(customerPage);
        when(customerMapper.customerToCustomerDTO(customer)).thenReturn(customerDTO);

        final Page<CustomerDTO> result = customerService.getAllCustomers("john", pageable);

        assertThat(result.getContent()).containsExactly(customerDTO);
    }

    @Test
    void createCustomer_savesAndReturnsMappedCustomer() {
        final Customer customer = new Customer();
        customer.setName("John Doe");
        final Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName("John Doe");
        final CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("John Doe");

        when(customerRepository.save(customer)).thenReturn(savedCustomer);
        when(customerMapper.customerToCustomerDTO(savedCustomer)).thenReturn(customerDTO);

        final CustomerDTO result = customerService.createCustomer(customer);

        assertThat(result).isEqualTo(customerDTO);
    }
}
