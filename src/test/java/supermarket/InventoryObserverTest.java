package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import supermarket.inventory.Inventory;
import supermarket.inventory.StockObserver;
import supermarket.model.Category;
import supermarket.model.Item;

class InventoryObserverTest {

    /** Recording observer to assert when/whether the alert fires. */
    private static class RecordingObserver implements StockObserver {
        final List<String> alerts = new ArrayList<>();
        @Override
        public void onLowStock(Item item, int currentStock, int threshold) {
            alerts.add(item.getName() + ":" + currentStock);
        }
    }

    @Test
    void perishableItemFiresAlertOnlyWhenAtOrBelowThreshold() {
        Inventory inventory = new Inventory();
        RecordingObserver observer = new RecordingObserver();
        inventory.addObserver(observer);

        Item steak = new Item("steak", new Category("meat"), 12.50, 0.50);
        inventory.addItem(steak, 5); // default threshold = 3

        inventory.decrement(steak, 1); // 5 -> 4, above threshold, no alert
        assertTrue(observer.alerts.isEmpty());

        inventory.decrement(steak, 4); // 4 -> 0, at/below threshold, fires
        assertEquals(1, observer.alerts.size());
        assertEquals("steak:0", observer.alerts.get(0));
    }

    @Test
    void nonPerishableItemNeverFiresAlert() {
        Inventory inventory = new Inventory();
        RecordingObserver observer = new RecordingObserver();
        inventory.addObserver(observer);

        Item apple = new Item("apple", new Category("fruit-and-vegetables"), 0.30, 0.20);
        inventory.addItem(apple, 2);

        inventory.decrement(apple, 2); // 2 -> 0, but not perishable
        assertTrue(observer.alerts.isEmpty());
    }

    @Test
    void restockIncreasesStock() {
        Inventory inventory = new Inventory();
        Item steak = new Item("steak", new Category("meat"), 12.50, 0.50);
        inventory.addItem(steak, 5);
        inventory.restock(steak, 10);
        assertEquals(15, inventory.getStock(steak));
    }
}
