package supermarket.promotion;

import java.util.ArrayList;
import java.util.List;

import supermarket.model.Cart;

/**
 * Aggregates the active store promotions and computes their combined discount
 * on a cart. Promotions are pluggable (Strategy): adding a new promotion type
 * needs no change here.
 */
public class PromotionEngine {
    private final List<Promotion> promotions = new ArrayList<>();

    public void addPromotion(Promotion promotion) {
        promotions.add(promotion);
    }

    public boolean isEmpty() {
        return promotions.isEmpty();
    }

    /** Combined discount of all active promotions for the cart. */
    public double totalDiscount(Cart cart) {
        return promotions.stream().mapToDouble(p -> p.discount(cart)).sum();
    }

    public List<String> descriptions() {
        return promotions.stream().map(Promotion::describe).toList();
    }
}
