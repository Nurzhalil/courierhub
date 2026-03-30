package com.example.courierhub.repository;

import com.example.courierhub.model.Order;
import com.example.courierhub.model.OrderStatus;
import com.example.courierhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    Optional<Order> findFirstByCourierIdAndStatus(Long courierId, OrderStatus status);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCourierIdAndStatus(Long courierId, OrderStatus status);
}