package com.shop.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository to perform data access operations on {@link Order}.
 */
public interface OrderRepository extends JpaRepository<Order, UUID> {
}
