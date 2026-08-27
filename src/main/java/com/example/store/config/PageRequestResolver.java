package com.example.store.config;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageRequestResolver {

    public static final String TOTAL_COUNT_HEADER = "X-Total-Count";
    public static final String TOTAL_PAGES_HEADER = "X-Total-Pages";

    private final PagingProperties pagingProperties;

    public Pageable resolve(Integer page, Integer size) {
        final int resolvedPage = (page == null || page < 0) ? 0 : page;
        final int requestedSize = (size == null || size < 1) ? pagingProperties.defaultSize() : size;
        final int resolvedSize = Math.min(requestedSize, pagingProperties.maxSize());
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by("id"));
    }
}
