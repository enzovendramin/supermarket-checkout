package supermarket.promotion;

import java.util.Locale;

import supermarket.model.Cart;
import supermarket.model.CartEntry;
import supermarket.model.Item;

/**
 * "Buy N get M free" on a specific item: for every (N+M) units of that item in
 * the cart, M units are free.
 */
public class BuyNGetMFreePromotion implements Promotion {
    private final Item item;
    private final int buy;
    private final int free;

    public BuyNGetMFreePromotion(Item item, int buy, int free) {
        if (buy <= 0 || free <= 0) {
            throw new IllegalArgumentException("buy and free must be positive.");
        }
        this.item = item;
        this.buy = buy;
        this.free = free;
    }

    @Override
    public double discount(Cart cart) {
        int units = 0;
        for (CartEntry entry : cart.getEntries()) {
            if (entry.getItem() == item) {
                units += entry.getQuantity();
            }
        }
        int freeUnits = (units / (buy + free)) * free;
        return freeUnits * item.getUnitPrice();
    }

    @Override
    public String describe() {
        return String.format(Locale.US, "buy %d get %d free on %s", buy, free, item.getName());
    }
}
