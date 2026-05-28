package supermarket.model;

public class Item {
    private final String name;
    private final Category category;
    private final double unitPrice;
    private final double weightKg;

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
}
