package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import supermarket.pricing.CategoryDiscount;
import supermarket.pricing.NoPricingPolicy;
import supermarket.pricing.PricingPolicy;

class PricingPolicyTest {

    @Test
    void noPricingPolicyLeavesPriceUnchanged() {
        PricingPolicy policy = new NoPricingPolicy();
        assertEquals(1.20, policy.apply(1.20), 1e-9);
    }

    @Test
    void categoryDiscountReducesPriceByPercent() {
        PricingPolicy policy = new CategoryDiscount(10);
        assertEquals(0.27, policy.apply(0.30), 1e-9); // -10%
    }

    @Test
    void categoryDiscountExposesItsPercent() {
        CategoryDiscount policy = new CategoryDiscount(5);
        assertEquals(5.0, policy.getPercent(), 1e-9);
        assertEquals(4.0 * 0.95, policy.apply(4.0), 1e-9);
    }
}
