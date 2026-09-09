package com.example.store.customer;

import com.example.store.config.PageRequestResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private PageRequestResolver pageRequestResolver;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("John Doe");
        customer.setId(1L);

        customerDTO = new CustomerDTO(1L, "John Doe", List.of());
    }

    @Test
    void testCreateCustomer() throws Exception {
        when(customerService.createCustomer(customer)).thenReturn(customerDTO);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        when(customerService.getAllCustomers(null)).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name").value("John Doe"));
    }

    @Test
    void testGetCustomerByNameWithBlankQuery() throws Exception {
        when(customerService.getAllCustomers("")).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer").param("name", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name").value("John Doe"));
    }

    @Test
    void testGetCustomerByNameWithValidQuery() throws Exception {
        when(customerService.getAllCustomers("john")).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer").param("name", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name").value("John Doe"));
    }

    @Test
    void testGetCustomerByNameWithInvalidQuery() throws Exception {
        when(customerService.getAllCustomers("zzz")).thenReturn(List.of());

        mockMvc.perform(get("/customer").param("name", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetAllCustomersPaginated() throws Exception {
        final Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        when(pageRequestResolver.resolve(0, 20)).thenReturn(pageable);
        when(customerService.getAllCustomers(null, pageable))
                .thenReturn(new PageImpl<>(List.of(customerDTO), pageable, 1));

        mockMvc.perform(get("/customer").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(jsonPath("$..name").value("John Doe"));
    }

    @Test
    void testGetCustomerByNamePaginated() throws Exception {
        final Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        when(pageRequestResolver.resolve(0, 20)).thenReturn(pageable);
        when(customerService.getAllCustomers("john", pageable))
                .thenReturn(new PageImpl<>(List.of(customerDTO), pageable, 1));

        mockMvc.perform(get("/customer")
                        .param("name", "john")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name").value("John Doe"));
    }

    @Test
    void testGetCustomerDetails() throws Exception {
        final Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        final CustomerDetailsDTO details =
                new CustomerDetailsDTO("John Doe", 5L, List.of(new CustomerOrderDTO(1L, "Chair")));
        when(pageRequestResolver.resolve(0, 20, Sort.by("id"))).thenReturn(pageable);
        when(customerService.getCustomerDetails(1L, pageable)).thenReturn(details);

        mockMvc.perform(get("/customer/1/details").param("page", "0").param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.totalOrders").value(5))
                .andExpect(jsonPath("$.orders[0].id").value(1))
                .andExpect(jsonPath("$.orders[0].description").value("Chair"));
    }

    @Test
    void testGetCustomerDetailsWithSort() throws Exception {
        final Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "description"));
        final CustomerDetailsDTO details = new CustomerDetailsDTO("John Doe", 5L, List.of());
        when(pageRequestResolver.resolve(null, null, Sort.by(Sort.Direction.DESC, "description")))
                .thenReturn(pageable);
        when(customerService.getCustomerDetails(1L, pageable)).thenReturn(details);

        mockMvc.perform(get("/customer/1/details").param("sort", "description,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testGetCustomerDetailsWithInvalidSortField() throws Exception {
        mockMvc.perform(get("/customer/1/details").param("sort", "unknownField,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCustomerDetailsWithMissingCustomer() throws Exception {
        final Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        when(pageRequestResolver.resolve(null, null, Sort.by("id"))).thenReturn(pageable);
        when(customerService.getCustomerDetails(999L, pageable))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: 999"));

        mockMvc.perform(get("/customer/999/details")).andExpect(status().isNotFound());
    }
}
