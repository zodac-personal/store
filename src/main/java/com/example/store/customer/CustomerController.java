package com.example.store.customer;

import com.example.store.config.PageRequestResolver;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final PageRequestResolver pageRequestResolver;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CustomerDTO>> getAllCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        final boolean blankName = name == null || name.isBlank();

        if (page == null && size == null) {
            final List<Customer> customers =
                    blankName ? customerRepository.findAll() : customerRepository.findByNamePartialMatch(name);
            return ResponseEntity.ok(customerMapper.customersToCustomerDTOs(customers));
        }

        final Pageable pageable = pageRequestResolver.resolve(page, size);
        final Page<Customer> customerPage = blankName
                ? customerRepository.findAll(pageable)
                : customerRepository.findByNamePartialMatch(name, pageable);
        return ResponseEntity.ok()
                .header(PageRequestResolver.TOTAL_COUNT_HEADER, String.valueOf(customerPage.getTotalElements()))
                .header(PageRequestResolver.TOTAL_PAGES_HEADER, String.valueOf(customerPage.getTotalPages()))
                .body(customerMapper.customersToCustomerDTOs(customerPage.getContent()));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDTO createCustomer(@RequestBody Customer customer) {
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }
}
