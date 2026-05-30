package supermarket.promotion;

import java.util.Locale;

import supermarket.model.Cart;

/** A store-wide coupon granting a percentage off the cart's raw total. */
public class PercentageCouponPromotion implements Promotion {
    private final double percent;

    public PercentageCouponPromotion(double percent) {
        this.percent = percent;
    }

    @Override
    public double discount(Cart cart) {
        return cart.rawTotal() * percent / 100.0;
    }

    @Override
    public String describe() {
        return String.format(Locale.US, "%.0f%% coupon", percent);
    }
}
