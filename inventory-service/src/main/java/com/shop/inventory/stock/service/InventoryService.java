package com.shop.inventory.stock.service;

import com.shop.events.*;
import com.shop.inventory.idempotency.ProcessedEvents;
import com.shop.inventory.stock.repository.StockRepository;
import com.shop.outbox.OutboxWriter;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Business layer for performing actions on inventory of an SKU.
 */
@Service
@Slf4j
public class InventoryService {

    private final ProcessedEvents processedEvents;

    private final OutboxWriter outboxWriter;

    private final StockRepository stockRepository;

    public InventoryService(final ProcessedEvents processedEvents
            , final OutboxWriter outboxWriter
            , final StockRepository stockRepository) {
        this.processedEvents = processedEvents;
        this.outboxWriter = outboxWriter;
        this.stockRepository = stockRepository;
    }

    /**
     * Handles processing of inventory for a specified order and sku.
     *
     * @param order The order for which the processing is required.
     */
    @Transactional
    public void handle(final OrderPlaced order) {
        if (!processedEvents.markProcessed(order.eventId())) {
            log.info("Duplicate order placed [{}] for order [{}]: already handled, skipping",
                    order.eventId(), order.orderId());
        }

        final boolean reserved = stockRepository.tryReserve(order.sku(), order.quantity()) == 1;

        if (reserved) {
            final var event = new StockReserved(UUID.randomUUID()
                    , order.orderId()
                    , order.sku()
                    , order.quantity()
                    , Instant.now());

            outboxWriter.write(event.eventId()
                    , Topics.INVENTORY_EVENTS
                    , order.orderId()
                    , EventTypes.STOCK_RESERVED
                    , event);

            log.info("Stock reserved [{}] for sku [{}] with order id: [{}]"
                    , order.quantity()
                    , order.orderId()
                    , order.sku());
        } else {
            final var reason = stockRepository.findById(order.sku())
                                              .map(stock -> "Only [" + stock.getAvailable() + "] quantity available")
                                              .orElse("Unknown sku [" + order.sku() + "]");

            final var event = new StockRejected(UUID.randomUUID()
                    , order.orderId()
                    , order.sku()
                    , order.quantity()
                    , reason
                    , Instant.now());

            outboxWriter.write(event.eventId()
                    , Topics.INVENTORY_EVENTS
                    , order.orderId()
                    , EventTypes.STOCK_REJECTED
                    , event);

            log.info("Rejected order [{}]: [{}]", order.orderId(), order.sku());
        }
    }
}
