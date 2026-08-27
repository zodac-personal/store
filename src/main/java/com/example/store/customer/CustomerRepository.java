package com.example.store.customer;

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

    default List<Customer> findByNamePartialMatch(String query) {
        final String escaped = escapeLikePattern(query.toLowerCase(Locale.ROOT));
        return findByNamePattern("%" + escaped + "%");
    }

    private static String escapeLikePattern(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
