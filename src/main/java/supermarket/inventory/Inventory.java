package supermarket.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import supermarket.model.Item;

/**
 * Live stock count (R9). Every finalized sale decrements the inventory; when a
 * perishable item falls at or below its low-stock threshold, the registered
 * {@link StockObserver}s are notified (Observer pattern).
 */
public class Inventory {
    /** Categories whose items are perishable and trigger low-stock alerts (R9). */
    private static final Set<String> PERISHABLE_CATEGORIES =
            Set.of("meat", "dairy", "diary", "fish");

    /** Default low-stock threshold applied to every item unless overridden. */
    public static final int DEFAULT_THRESHOLD = 3;

    private final Map<Item, Integer> stock = new HashMap<>();
    private final Map<Item, Integer> thresholds = new HashMap<>();
    private final List<StockObserver> observers = new ArrayList<>();

    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    /** Registers an item with an initial stock and the default threshold. */
    public void addItem(Item item, int initialStock) {
        stock.put(item, initialStock);
        thresholds.putIfAbsent(item, DEFAULT_THRESHOLD);
    }

    public void setThreshold(Item item, int threshold) {
        thresholds.put(item, threshold);
    }

    public int getStock(Item item) {
        return stock.getOrDefault(item, 0);
    }

    public int getThreshold(Item item) {
        return thresholds.getOrDefault(item, DEFAULT_THRESHOLD);
    }

    public boolean isLow(Item item) {
        return isPerishable(item) && getStock(item) <= getThreshold(item);
    }

    public void restock(Item item, int quantity) {
        stock.merge(item, quantity, Integer::sum);
    }

    /**
     * Decrements the stock of an item after a finalized sale. If the item is
     * perishable and now at or below its threshold, observers are notified.
     */
    public void decrement(Item item, int quantity) {
        int newStock = getStock(item) - quantity;
        stock.put(item, newStock);
        if (isPerishable(item) && newStock <= getThreshold(item)) {
            notifyObservers(item, newStock);
        }
    }

    public Map<Item, Integer> snapshot() {
        return new HashMap<>(stock);
    }

    private boolean isPerishable(Item item) {
        return PERISHABLE_CATEGORIES.contains(item.getCategory().getName().toLowerCase());
    }

    private void notifyObservers(Item item, int currentStock) {
        for (StockObserver observer : observers) {
            observer.onLowStock(item, currentStock, getThreshold(item));
        }
    }
}
