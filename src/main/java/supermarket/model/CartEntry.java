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
    public double subtotal() { return item.getUnitPrice() * quantity; }
    public double totalWeightKg() { return item.getWeightKg() * quantity; }
}
