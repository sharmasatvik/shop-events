package com.example.shop.order.api;

import com.example.shop.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    /**
     * 202 Accepted, not 201 Created: the order is recorded, but whether it succeeds depends on
     * inventory, which answers asynchronously. The client polls GET /orders/{id} (or gets notified).
     */
    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest req) {
        OrderResponse order = service.placeOrder(req);
        return ResponseEntity.accepted().location(URI.create("/orders/" + order.id())).body(order);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return service.getOrder(id);
    }
}
