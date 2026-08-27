package com.example.store.customer;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @GetMapping
    public List<CustomerDTO> getAllCustomers(@RequestParam(required = false) String name) {
        final List<Customer> customers = (name == null || name.isBlank())
                ? customerRepository.findAll()
                : customerRepository.findByNamePartialMatch(name);
        return customerMapper.customersToCustomerDTOs(customers);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDTO createCustomer(@RequestBody Customer customer) {
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }
}
