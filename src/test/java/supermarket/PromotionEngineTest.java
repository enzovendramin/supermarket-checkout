package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import supermarket.model.Cart;
import supermarket.model.CartEntry;
import supermarket.model.Category;
import supermarket.model.Item;
import supermarket.promotion.BuyNGetMFreePromotion;
import supermarket.promotion.PercentageCouponPromotion;
import supermarket.promotion.PromotionEngine;

class PromotionEngineTest {

    private final Item rice = new Item("rice", new Category("grocery"), 2.00, 1.00);

    private Cart cartWith(int units) {
        Cart cart = new Cart();
        cart.addEntry(new CartEntry(rice, units));
        return cart;
    }

    @Test
    void buyTwoGetOneFreeDiscountsTheFreeUnits() {
        // buy 2 get 1 free -> every group of 3 yields 1 free unit.
        BuyNGetMFreePromotion promo = new BuyNGetMFreePromotion(rice, 2, 1);
        assertEquals(0.0, promo.discount(cartWith(2)), 1e-9);   // not a full group
        assertEquals(2.00, promo.discount(cartWith(3)), 1e-9);  // 1 free
        assertEquals(4.00, promo.discount(cartWith(6)), 1e-9);  // 2 free
        assertEquals(4.00, promo.discount(cartWith(8)), 1e-9);  // still 2 free (8/3=2)
    }

    @Test
    void percentageCouponDiscountsRawTotal() {
        PercentageCouponPromotion coupon = new PercentageCouponPromotion(10);
        assertEquals(1.20, coupon.discount(cartWith(6)), 1e-9); // 10% of €12.00
    }

    @Test
    void engineSumsAllActivePromotions() {
        PromotionEngine engine = new PromotionEngine();
        engine.addPromotion(new BuyNGetMFreePromotion(rice, 2, 1)); // €4.00 on 6 units
        engine.addPromotion(new PercentageCouponPromotion(10));     // €1.20
        assertEquals(5.20, engine.totalDiscount(cartWith(6)), 1e-9);
    }
}
