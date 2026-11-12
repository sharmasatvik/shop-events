package com.shop.inventory.stock.controller;

import com.shop.inventory.stock.domain.StockItem;
import com.shop.inventory.stock.repository.StockRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for performing operations on {@link StockItem}.
 */
@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockRepository stock;

    public StockController(StockRepository stock) {
        this.stock = stock;
    }

    /**
     * Gets a list of all the stock available.
     *
     * @return A list of all the stock available.
     */
    @GetMapping
    public List<StockItem> all() {
        return stock.findAll();
    }
}