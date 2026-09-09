package com.example.store.customer;

import com.example.store.config.PageRequestResolver;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private static final Set<String> SORTABLE_ORDER_FIELDS = Set.of("id", "description");

    private final CustomerService customerService;
    private final PageRequestResolver pageRequestResolver;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CustomerDTO>> getAllCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(customerService.getAllCustomers(name));
        }

        final Pageable pageable = pageRequestResolver.resolve(page, size);
        final Page<CustomerDTO> customerPage = customerService.getAllCustomers(name, pageable);
        return ResponseEntity.ok()
                .header(PageRequestResolver.TOTAL_COUNT_HEADER, String.valueOf(customerPage.getTotalElements()))
                .header(PageRequestResolver.TOTAL_PAGES_HEADER, String.valueOf(customerPage.getTotalPages()))
                .body(customerPage.getContent());
    }

    @GetMapping(path = "/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public CustomerDetailsDTO getCustomerDetailsById(
            @PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sort) {
        final Pageable pageable = pageRequestResolver.resolve(page, pageSize, resolveOrderSort(sort));
        return customerService.getCustomerDetails(id, pageable);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDTO createCustomer(@RequestBody Customer customer) {
        return customerService.createCustomer(customer);
    }

    private static Sort resolveOrderSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by("id");
        }

        final String[] parts = sort.split(",", 2);
        if (!SORTABLE_ORDER_FIELDS.contains(parts[0])) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sort field: " + parts[0]);
        }

        final Sort.Direction direction =
                parts.length > 1 && Sort.Direction.DESC.name().equalsIgnoreCase(parts[1])
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
