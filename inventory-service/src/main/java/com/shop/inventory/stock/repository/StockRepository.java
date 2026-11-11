package com.shop.inventory.stock.repository;

import com.shop.inventory.stock.domain.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository to perform data access operations on {@link StockItem}.
 */
public interface StockRepository extends JpaRepository<StockItem, String> {

    /**
     * Check and change in ONE statement, so concurrent orders can't oversell.
     * <p>
     * The naive way, "read available, if enough then save available - qty",
     * breaks when two threads both read available = 1 and both save 0: two
     * laptops sold, one in stock. Here Postgres locks the row while it evaluates
     * the WHERE, so the second UPDATE sees the first one's result and matches
     * 0 rows.
     *
     * @return 1 if reserved, 0 if not enough stock or unknown SKU
     */
    @Modifying
    @Query(value = """
            UPDATE stock
            SET available = available - :qty,
                reserved  = reserved + :qty,
                updated_at = now()
            WHERE sku = :sku AND available >= :qty
            """, nativeQuery = true)
    int tryReserve(@Param("sku") final String sku, @Param("qty") final int qty);
}