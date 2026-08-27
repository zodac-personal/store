package com.example.store.order;

import com.example.store.config.PageRequestResolver;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final PageRequestResolver pageRequestResolver;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderDTO>> getAllOrders(
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(orderMapper.ordersToOrderDTOs(orderRepository.findAll()));
        }

        final Pageable pageable = pageRequestResolver.resolve(page, size);
        final Page<Order> orderPage = orderRepository.findAll(pageable);
        return ResponseEntity.ok()
                .header(PageRequestResolver.TOTAL_COUNT_HEADER, String.valueOf(orderPage.getTotalElements()))
                .header(PageRequestResolver.TOTAL_PAGES_HEADER, String.valueOf(orderPage.getTotalPages()))
                .body(orderMapper.ordersToOrderDTOs(orderPage.getContent()));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDTO getOrderById(@PathVariable Long id) {
        return orderRepository
                .findById(id)
                .map(orderMapper::orderToOrderDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDTO createOrder(@RequestBody Order order) {
        return orderMapper.orderToOrderDTO(orderRepository.save(order));
    }
}
