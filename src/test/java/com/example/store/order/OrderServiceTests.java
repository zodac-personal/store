package com.example.store.order;

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
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderMapper);
    }

    @Test
    void getAllOrders_returnsMappedOrders() {
        final Order order = new Order();
        order.setId(1L);
        final OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.ordersToOrderDTOs(List.of(order))).thenReturn(List.of(orderDTO));

        final List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).containsExactly(orderDTO);
    }

    @Test
    void getAllOrdersPaginated_returnsMappedPage() {
        final Order order = new Order();
        order.setId(1L);
        final OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        final Pageable pageable = PageRequest.of(0, 20);
        final Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.orderToOrderDTO(order)).thenReturn(orderDTO);

        final Page<OrderDTO> result = orderService.getAllOrders(pageable);

        assertThat(result.getContent()).containsExactly(orderDTO);
    }

    @Test
    void getOrderById_returnsMappedOrder_whenFound() {
        final Order order = new Order();
        order.setId(1L);
        final OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.orderToOrderDTO(order)).thenReturn(orderDTO);

        final OrderDTO result = orderService.getOrderById(1L);

        assertThat(result).isEqualTo(orderDTO);
    }

    @Test
    void getOrderById_throwsNotFound_whenMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> orderService.getOrderById(404L))
                .satisfies(exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createOrder_savesAndReturnsMappedOrder() {
        final Order order = new Order();
        order.setDescription("Chair");
        final Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setDescription("Chair");
        final OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setDescription("Chair");

        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(orderMapper.orderToOrderDTO(savedOrder)).thenReturn(orderDTO);

        final OrderDTO result = orderService.createOrder(order);

        assertThat(result).isEqualTo(orderDTO);
    }
}
