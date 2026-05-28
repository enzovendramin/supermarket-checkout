package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import supermarket.model.Cart;
import supermarket.model.CartEntry;
import supermarket.model.Category;
import supermarket.model.Item;

class CartTest {

    private Item item(String name, double price, double weight) {
        return new Item(name, new Category("misc"), price, weight);
    }

    @Test
    void cartEntryComputesSubtotalAndWeight() {
        CartEntry entry = new CartEntry(item("steak", 12.50, 0.50), 4);
        assertEquals(50.0, entry.subtotal(), 1e-9);
        assertEquals(2.0, entry.totalWeightKg(), 1e-9);
    }

    @Test
    void cartAggregatesTotalsAcrossEntries() {
        Cart cart = new Cart();
        cart.addEntry(new CartEntry(item("milk", 1.20, 1.00), 2));   // 2.40, 2.0 kg
        cart.addEntry(new CartEntry(item("apple", 0.30, 0.20), 10)); // 3.00, 2.0 kg
        assertEquals(5.40, cart.rawTotal(), 1e-9);
        assertEquals(4.0, cart.totalWeightKg(), 1e-9);
    }

    @Test
    void clearEmptiesTheCart() {
        Cart cart = new Cart();
        cart.addEntry(new CartEntry(item("milk", 1.20, 1.00), 1));
        cart.clear();
        assertEquals(0.0, cart.rawTotal(), 1e-9);
        assertTrue(cart.getEntries().isEmpty());
    }

    @Test
    void entriesViewIsUnmodifiable() {
        Cart cart = new Cart();
        assertThrows(UnsupportedOperationException.class,
            () -> cart.getEntries().add(new CartEntry(item("x", 1.0, 1.0), 1)));
    }
}
