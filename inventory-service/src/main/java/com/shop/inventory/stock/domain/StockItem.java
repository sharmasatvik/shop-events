package com.shop.inventory.stock.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entity for stock items for an SKU.
 */
@Entity
@Table(name = "stock")
public class StockItem {

    /**
     * The available quantity of the stock.
     */
    private int available;

    /**
     * Whether the stock is reserved for another order.
     */
    private int reserved;

    /**
     * The SKU of the stock.
     */
    @Id
    private String sku;

    /**
     * The latest time when the stock was updated.
     */
    private Instant updatedAt;

    protected StockItem() {
    }

    /**
     * Gets the available quantity of the stock.
     *
     * @return The available quantity of the stock.
     */
    public int getAvailable() {
        return available;
    }

    /**
     * Gets whether the stock is reserved or not.
     *
     * @return Whether the stock is reserved or not.
     */
    public int getReserved() {
        return reserved;
    }

    /**
     * Gets the SKU of the stock.
     *
     * @return The SKU of the stock.
     */
    public String getSku() {
        return sku;
    }

    /**
     * Gets the last updated time of the stock.
     *
     * @return The last updated time of the stock.
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}