package com.shop.order.service;

import com.shop.events.*;
import com.shop.order.domain.Order;
import com.shop.order.domain.OrderRepository;
import com.shop.order.dto.OrderResponse;
import com.shop.order.dto.PlaceOrderRequest;
import com.shop.outbox.OutboxWriter;
import com.shop.outbox.ProcessedEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static com.shop.events.EventTypes.ORDER_CONFIRMED;
import static com.shop.events.EventTypes.ORDER_REJECTED;

/**
 * Service to perform business actions on {@link Order}.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orders;

    private final OutboxWriter outbox;

    private final ProcessedEvents processedEvents;

    public OrderService(final OrderRepository orders
            , final OutboxWriter outbox
            , final ProcessedEvents processedEvents) {
        this.orders = orders;
        this.outbox = outbox;
        this.processedEvents = processedEvents;
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

    /**
     * Handles scenarios where a stock is reserve for an order.
     *
     * @param event The stock reservation event.
     */
    @Transactional
    public void handleStockReserved(final StockReserved event) {
        if (!processedEvents.markProcessed(event.eventId())) {
            log.info("Duplicate stock reserved for event: [{}], skipping."
                    , event.eventId());

            return;
        }

        final var orderOptional = orders.findById(event.orderId());

        if (orderOptional.isEmpty()) {
            return;
        }

        final var order = orderOptional.get();

        order.confirm();

        final var confirmed = new OrderConfirmed(UUID.randomUUID()
                , order.getId()
                , order.getCustomerId()
                , order.getSku()
                , order.getQuantity()
                , Instant.now());

        outbox.write(confirmed.eventId()
                , Topics.ORDER_EVENTS
                , order.getId()
                , ORDER_CONFIRMED
                , confirmed);

        log.info("Order: [{}] is confirmed", order.getId());
    }

    /**
     * Handles scenarios where a stock is rejected for an order.
     *
     * @param event The stock rejection event.
     */
    @Transactional
    public void handleOrderRejected(final OrderRejected event) {
        if (!processedEvents.markProcessed(event.eventId())) {
            log.info("Duplicate stock rejected for event: [{}], skipping.", event.eventId());

            return;
        }

        final var order = findPending(event.orderId());

        if (order == null) {
            return;
        }

        order.reject(event.reason());

        final var rejected = new OrderRejected(UUID.randomUUID()
                , order.getId()
                , order.getCustomerId()
                , event.reason()
                , Instant.now());

        outbox.write(rejected.eventId()
                , Topics.ORDER_EVENTS
                , order.getId()
                , ORDER_REJECTED
                , rejected);

        log.info("Order: [{}] is rejected with reason: [{}]"
                , order.getId()
                , event.reason());
    }

    /**
     * Searches for pending order in the database.
     *
     * @param orderId The order to search for.
     *
     * @return The order details.
     */
    private Order findPending(final UUID orderId) {
        final var order = orders.findById(orderId).orElse(null);

        if (order == null) {
            // Can't normally happen: the outbox only publishes Order Placed
            // Event AFTER the order is committed.
            // In production this would raise an alert.
            log.warn("Result for unknown order [{}], ignoring.", orderId);

            return null;
        }

        if (!order.isPending()) {
            log.warn("Order: [{}] is already in [{}] state, ignoring late result."
                    , orderId
                    , order.getStatus());

            return null;
        }

        return order;
    }
}
