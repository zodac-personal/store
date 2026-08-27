package com.example.store.customer;

import com.example.store.config.PageRequestResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@ComponentScan(basePackageClasses = CustomerMapper.class)
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private PageRequestResolver pageRequestResolver;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("John Doe");
        customer.setId(1L);
    }

    @Test
    void testCreateCustomer() throws Exception {
        when(customerRepository.save(customer)).thenReturn(customer);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name").value("John Doe"));
    }

    @Test
    void testGetCustomerByNameWithBlankQuery() throws Exception {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        mockMvc.perform(get("/customer").param("name", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name").value("John Doe"));
    }

    @Test
    void testGetCustomerByNameWithValidQuery() throws Exception {
        when(customerRepository.findByNamePartialMatch("john")).thenReturn(List.of(customer));

        mockMvc.perform(get("/customer").param("name", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name").value("John Doe"));
    }

    @Test
    void testGetCustomerByNameWithInvalidQuery() throws Exception {
        when(customerRepository.findByNamePartialMatch("zzz")).thenReturn(List.of());

        mockMvc.perform(get("/customer").param("name", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetAllCustomersPaginated() throws Exception {
        final Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        when(pageRequestResolver.resolve(0, 20)).thenReturn(pageable);
        when(customerRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(customer), pageable, 1));

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
        when(customerRepository.findByNamePartialMatch("john", pageable))
                .thenReturn(new PageImpl<>(List.of(customer), pageable, 1));

        mockMvc.perform(get("/customer")
                        .param("name", "john")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name").value("John Doe"));
    }
}
