package supermarket.inventory;

import supermarket.model.Item;

/** Observer that alerts the supplier to reorder a low-stock perishable item (R9). */
public class SupplierNotifier implements StockObserver {
    @Override
    public void onLowStock(Item item, int currentStock, int threshold) {
        System.out.printf(
            "[SUPPLIER ALERT] Please restock '%s' (%s): only %d left.%n",
            item.getName(), item.getCategory().getName(), currentStock);
    }
}
