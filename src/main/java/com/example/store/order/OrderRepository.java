package com.example.store.order;

import com.example.store.customer.CustomerOrderDTO;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = "customer")
    Optional<Order> findById(Long id);

    @Query("SELECT new com.example.store.customer.CustomerOrderDTO(ord.id, ord.description) "
            + "FROM Order ord WHERE ord.customer.id = :customerId")
    List<CustomerOrderDTO> findCustomerOrderDTOByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
}
