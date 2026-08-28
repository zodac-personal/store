package com.example.store.order;

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
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Cacheable(cacheNames = "orders")
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderMapper.ordersToOrderDTOs(orderRepository.findAll());
    }

    @Cacheable(cacheNames = "orders")
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::orderToOrderDTO);
    }

    @Cacheable(cacheNames = "orders")
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        return orderRepository
                .findById(id)
                .map(orderMapper::orderToOrderDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
    }

    @CacheEvict(cacheNames = "orders", allEntries = true)
    @Transactional
    public OrderDTO createOrder(Order order) {
        return orderMapper.orderToOrderDTO(orderRepository.save(order));
    }
}
