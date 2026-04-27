package com.antigravity.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryItemTest {

    @Test
    void testInventoryItemStockStatus() {
        InventoryItem item = new InventoryItem();
        item.setStockQuantity(10);
        
        assertEquals(10, item.getStockQuantity(), "Stock quantity should be correctly retrieved");
        assertTrue(item.getStockQuantity() > 0, "Item should be in stock");
    }

    @Test
    void testInventoryItemPrice() {
        InventoryItem item = new InventoryItem();
        item.setPrice(new java.math.BigDecimal("99.99"));
        
        assertEquals(new java.math.BigDecimal("99.99"), item.getPrice(), "Price should match the set value");
    }
}
