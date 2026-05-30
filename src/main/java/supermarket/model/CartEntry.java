package supermarket.model;

public class CartEntry {
    private final Item item;
    private final int quantity;

    public CartEntry(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }
    /** Line subtotal using the quantity-adjusted unit price (R3), before category/plan discounts. */
    public double subtotal() { return item.unitPriceFor(quantity) * quantity; }
    public double totalWeightKg() { return item.getWeightKg() * quantity; }
}
