package com.example.store.customer;

import java.util.List;

public record CustomerDetailsDTO(String name, long totalOrders, List<CustomerOrderDTO> orders) {}
