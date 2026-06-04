package supermarket.gui;

import supermarket.register.Supermarket;

/**
 * Builds a ready-to-use {@link Supermarket} for the GUI demo: the default setup
 * plus a small catalogue, a category discount and a category VAT. The GUI is
 * just another front-end over this same domain core (no domain code is changed).
 */
public final class DemoData {
    /** Default customer/cashier and test card created by {@code setup()}. */
    public static final String CUSTOMER = "customer";
    public static final String CARD = "4242424242424242";
    public static final String PIN = "1234";

    private DemoData() {}

    public static Supermarket build() {
        Supermarket market = new Supermarket();
        market.setup(); // dairy/meat/fruit-and-vegetables, default users, test card

        market.addItem("milk", "dairy", 1.20, 1.00, 50);
        market.addItem("yogurt", "dairy", 0.80, 0.15, 30);
        market.addItem("steak", "meat", 12.50, 0.50, 5);
        market.addItem("chicken", "meat", 8.00, 1.00, 20);
        market.addItem("apple", "fruit-and-vegetables", 0.30, 0.20, 200);
        market.addItem("tomato", "fruit-and-vegetables", 0.90, 0.25, 100);

        market.setCategoryDiscount("fruit-and-vegetables", 10); // R6
        market.setCategoryTax("dairy", 5);                      // VAT extension
        return market;
    }
}
