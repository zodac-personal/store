package com.example.store.order;

public record OrderDTO(Long id, String description, OrderCustomerDTO customer) {}
