package com.shop.order;

import com.shop.events.EventTypes;
import com.shop.events.OrderPlaced;
import com.shop.events.Topics;
import com.shop.order.dto.OrderResponse;
import com.shop.order.dto.PlaceOrderRequest;
import com.shop.order.domain.Order;
import com.shop.order.domain.OrderRepository;
import com.shop.outbox.OutboxWriter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

/**
 * Service to perform business actions on {@link Order}.
 */
@Service
public class OrderService {

    private final OrderRepository orders;

    private final OutboxWriter outbox;

    public OrderService(OrderRepository orders, OutboxWriter outbox) {
        this.orders = orders;
        this.outbox = outbox;
    }

    /**
     * The heart of the outbox pattern: the order row and the OrderPlaced outbox row are written in ONE
     * database transaction. Either both are committed or neither is. Kafka is not touched here at all.
     */
    @Transactional
    public OrderResponse placeOrder(final PlaceOrderRequest orderRequest) {
        final var order = orders.save(Order.newPending(orderRequest.customerId()
                , orderRequest.sku()
                , orderRequest.quantity()));

        final var event = new OrderPlaced(UUID.randomUUID()
                , order.getId()
                , order.getCustomerId()
                , order.getSku()
                , order.getQuantity()
                , Instant.now());

        outbox.write(event.eventId()
                , Topics.ORDER_EVENTS
                , order.getId()
                , EventTypes.ORDER_PLACED
                , event);

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(final UUID id) {
        return orders.findById(id)
                     .map(OrderResponse::from)
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }
}
