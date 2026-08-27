package com.example.store.order;

import com.example.store.config.PageRequestResolver;
import com.example.store.customer.Customer;

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

@WebMvcTest(OrderController.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private PageRequestResolver pageRequestResolver;

    private Order order;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        final Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setId(1L);

        order = new Order();
        order.setDescription("Test Order");
        order.setId(1L);
        order.setCustomer(customer);

        final OrderCustomerDTO orderCustomerDTO = new OrderCustomerDTO(1L, "John Doe");

        orderDTO = new OrderDTO(1L, "Test Order", orderCustomerDTO);
    }

    @Test
    void testCreateOrder() throws Exception {
        when(orderService.createOrder(order)).thenReturn(orderDTO);

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.name").value("John Doe"));
    }

    @Test
    void testGetOrder() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(orderDTO));

        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..description").value("Test Order"))
                .andExpect(jsonPath("$..customer.name").value("John Doe"));
    }

    @Test
    void testGetOrderById() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderDTO);

        mockMvc.perform(get("/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.name").value("John Doe"));
    }

    @Test
    void testGetOrderByIdMissingId() throws Exception {
        when(orderService.getOrderById(404L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: 404"));

        mockMvc.perform(get("/order/404")).andExpect(status().isNotFound());
    }

    @Test
    void testGetOrderByIdMalformed() throws Exception {
        mockMvc.perform(get("/order/invalid")).andExpect(status().isBadRequest());
    }

    @Test
    void testGetOrderPaginated() throws Exception {
        final Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        when(pageRequestResolver.resolve(0, 20)).thenReturn(pageable);
        when(orderService.getAllOrders(pageable)).thenReturn(new PageImpl<>(List.of(orderDTO), pageable, 1));

        mockMvc.perform(get("/order").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(jsonPath("$..description").value("Test Order"));
    }
}
