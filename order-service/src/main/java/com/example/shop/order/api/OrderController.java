package com.example.shop.order.api;

import com.example.shop.order.OrderService;
import com.example.shop.order.domain.Order;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller for API access to {@link Order}.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    /**
     * Placed an order in the system.
     *
     * @param orderRequest The order request.
     *
     * @return Response received for order placement.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody final PlaceOrderRequest orderRequest) {
        final var order = service.placeOrder(orderRequest);

        return ResponseEntity.accepted()
                             .location(URI.create("/orders/" + order.id()))
                             .body(order);
    }

    /**
     * Fetches a specified order.
     *
     * @param id The unique identifier of the order to fetch.
     *
     * @return The order matching the specified identity.
     */
    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable final UUID id) {
        return service.getOrder(id);
    }
}
