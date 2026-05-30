package supermarket.model;

public class Item {
    private final String name;
    private final Category category;
    private final double unitPrice;
    private final double weightKg;

    // Optional quantity-based pricing (R3): a per-unit discount when buying in bulk.
    private int bulkMinQuantity = 0;        // 0 means no bulk rule
    private double bulkDiscountPercent = 0; // applied to unit price when qty >= bulkMinQuantity

    public Item(String name, Category category, double unitPrice, double weightKg) {
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.weightKg = weightKg;
    }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public double getUnitPrice() { return unitPrice; }
    public double getWeightKg() { return weightKg; }

    /** Configures a bulk discount: {@code percent}% off the unit price from {@code minQuantity} units (R3). */
    public void setQuantityDiscount(int minQuantity, double percent) {
        this.bulkMinQuantity = minQuantity;
        this.bulkDiscountPercent = percent;
    }

    public boolean hasQuantityDiscount() { return bulkMinQuantity > 0; }
    public int getBulkMinQuantity() { return bulkMinQuantity; }
    public double getBulkDiscountPercent() { return bulkDiscountPercent; }

    /** Unit price for a given quantity, applying the bulk discount when it kicks in (R3). */
    public double unitPriceFor(int quantity) {
        if (bulkMinQuantity > 0 && quantity >= bulkMinQuantity) {
            return unitPrice * (1.0 - bulkDiscountPercent / 100.0);
        }
        return unitPrice;
    }
}
