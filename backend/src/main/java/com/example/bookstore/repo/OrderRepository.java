package com.example.bookstore.repo;

import com.example.bookstore.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
