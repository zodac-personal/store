package com.example.store.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Locale;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(nativeQuery = true, value = """
            SELECT *
            FROM customer
            WHERE lower(name) LIKE :pattern ESCAPE '\\'
        """)
    List<Customer> findByNamePattern(@Param("pattern") String pattern);

    @Query(nativeQuery = true, value = """
                SELECT *
                FROM customer
                WHERE lower(name) LIKE :pattern ESCAPE '\\'
                ORDER BY id
            """, countQuery = """
                SELECT count(*)
                FROM customer
                WHERE lower(name) LIKE :pattern ESCAPE '\\'
            """)
    Page<Customer> findByNamePattern(@Param("pattern") String pattern, Pageable pageable);

    default List<Customer> findByNamePartialMatch(String query) {
        return findByNamePattern(likePattern(query));
    }

    default Page<Customer> findByNamePartialMatch(String query, Pageable pageable) {
        return findByNamePattern(likePattern(query), pageable);
    }

    private static String likePattern(String query) {
        return "%" + escapeLikePattern(query.toLowerCase(Locale.ROOT)) + "%";
    }

    private static String escapeLikePattern(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
