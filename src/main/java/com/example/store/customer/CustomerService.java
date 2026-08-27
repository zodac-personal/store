package com.example.store.customer;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers(String name) {
        final List<Customer> customers =
                isBlank(name) ? customerRepository.findAll() : customerRepository.findByNamePartialMatch(name);
        return customerMapper.customersToCustomerDTOs(customers);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(String name, Pageable pageable) {
        final Page<Customer> customerPage = isBlank(name)
                ? customerRepository.findAll(pageable)
                : customerRepository.findByNamePartialMatch(name, pageable);
        return customerPage.map(customerMapper::customerToCustomerDTO);
    }

    @Transactional
    public CustomerDTO createCustomer(Customer customer) {
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
