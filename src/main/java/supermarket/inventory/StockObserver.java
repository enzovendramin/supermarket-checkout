package supermarket.inventory;

import supermarket.model.Item;

/** Observer notified when a perishable item falls below its low-stock threshold (R9). */
public interface StockObserver {
    void onLowStock(Item item, int currentStock, int threshold);
}
