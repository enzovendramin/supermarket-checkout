package supermarket.inventory;

import supermarket.model.Item;

/** Observer that alerts the store manager about a low-stock perishable item (R9). */
public class ManagerNotifier implements StockObserver {
    @Override
    public void onLowStock(Item item, int currentStock, int threshold) {
        System.out.printf(
            "[MANAGER ALERT] Low stock on '%s' (%s): %d left (threshold %d).%n",
            item.getName(), item.getCategory().getName(), currentStock, threshold);
    }
}
