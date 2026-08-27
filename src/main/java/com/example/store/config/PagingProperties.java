package com.example.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "store.paging")
public record PagingProperties(int defaultSize, int maxSize) {}
