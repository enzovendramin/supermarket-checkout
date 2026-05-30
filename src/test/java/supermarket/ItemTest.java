package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import supermarket.model.Category;
import supermarket.model.Item;

class ItemTest {

    @Test
    void noBulkRuleMeansFlatUnitPrice() {
        Item item = new Item("rice", new Category("grocery"), 2.00, 1.00);
        assertFalse(item.hasQuantityDiscount());
        assertEquals(2.00, item.unitPriceFor(1), 1e-9);
        assertEquals(2.00, item.unitPriceFor(100), 1e-9);
    }

    @Test
    void bulkDiscountAppliesOnlyFromThreshold() {
        Item item = new Item("rice", new Category("grocery"), 2.00, 1.00);
        item.setQuantityDiscount(10, 25); // -25% from 10 units (R3)
        assertTrue(item.hasQuantityDiscount());
        assertEquals(2.00, item.unitPriceFor(9), 1e-9);        // below threshold
        assertEquals(2.00 * 0.75, item.unitPriceFor(10), 1e-9); // at threshold
        assertEquals(2.00 * 0.75, item.unitPriceFor(50), 1e-9);
    }
}
