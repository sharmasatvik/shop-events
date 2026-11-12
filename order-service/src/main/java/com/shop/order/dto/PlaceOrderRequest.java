package com.shop.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to place an order.
 *
 * @param customerId The customer who placed the order.
 * @param sku        The SKU present in the order.
 * @param quantity   The quantity of the SKU.
 */
public record PlaceOrderRequest(@NotBlank String customerId
        , @NotBlank String sku
        , @Min(1) int quantity) {
}
