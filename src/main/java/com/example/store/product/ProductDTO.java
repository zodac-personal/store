package com.example.store.product;

import java.util.List;

public record ProductDTO(Long id, String description, List<Long> orders) {}
