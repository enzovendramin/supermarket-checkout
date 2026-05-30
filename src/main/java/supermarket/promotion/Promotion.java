package supermarket.promotion;

import supermarket.model.Cart;

/**
 * A store promotion that yields a discount on a cart (Strategy). New kinds of
 * promotion are added by implementing this interface, with no change to the
 * checkout — the {@link PromotionEngine} simply aggregates them.
 */
public interface Promotion {
    /** Discount amount (in euros) this promotion grants for the given cart. */
    double discount(Cart cart);
    String describe();
}
