package com.example.store.customer;

import com.example.store.order.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final OrderRepository orderRepository;

    @Cacheable(cacheNames = "customers")
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers(String name) {
        final List<Customer> customers =
                isBlank(name) ? customerRepository.findAll() : customerRepository.findByNamePartialMatch(name);
        return customerMapper.customersToCustomerDTOs(customers);
    }

    @Cacheable(cacheNames = "customers")
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(String name, Pageable pageable) {
        final Page<Customer> customerPage = isBlank(name)
                ? customerRepository.findAll(pageable)
                : customerRepository.findByNamePartialMatch(name, pageable);
        return customerPage.map(customerMapper::customerToCustomerDTO);
    }

    @Transactional(readOnly = true)
    public CustomerDetailsDTO getCustomerDetails(Long customerId, Pageable pageable) {
        final CustomerRepository.CustomerOrderStats stats = customerRepository
                .findOrderStatsByCustomerId(customerId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + customerId));
        final List<CustomerOrderDTO> orders = orderRepository.findCustomerOrderDTOByCustomerId(customerId, pageable);
        return new CustomerDetailsDTO(stats.getName(), stats.getTotalOrders(), orders);
    }

    @CacheEvict(cacheNames = "customers", allEntries = true)
    @Transactional
    public CustomerDTO createCustomer(Customer customer) {
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
