package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import supermarket.discount.DiscountPlan;
import supermarket.discount.DiscountPlanFactory;
import supermarket.discount.NormalPlan;
import supermarket.discount.PlatinumPlan;
import supermarket.discount.PrimePlan;

class DiscountPlanTest {

    @Test
    void normalPlanAppliesNoDiscount() {
        DiscountPlan plan = new NormalPlan();
        assertEquals(80.0, plan.applyDiscount(80.0), 1e-9);
        assertEquals(15.0, plan.applyDeliveryDiscount(15.0), 1e-9);
    }

    @Test
    void primePlanDiscountsOnlyAtOrAbove50() {
        DiscountPlan plan = new PrimePlan();
        assertEquals(49.99, plan.applyDiscount(49.99), 1e-9);      // below threshold
        assertEquals(50.0 * 0.80, plan.applyDiscount(50.0), 1e-9); // exactly at threshold
        assertEquals(100.0 * 0.80, plan.applyDiscount(100.0), 1e-9);
        assertEquals(15.0 * 0.50, plan.applyDeliveryDiscount(15.0), 1e-9); // 50% off delivery
    }

    @Test
    void platinumPlanAlwaysDiscountsAndDeliversFree() {
        DiscountPlan plan = new PlatinumPlan();
        assertEquals(10.0 * 0.70, plan.applyDiscount(10.0), 1e-9);
        assertEquals(100.0 * 0.70, plan.applyDiscount(100.0), 1e-9);
        assertEquals(0.0, plan.applyDeliveryDiscount(15.0), 1e-9); // free delivery
    }

    @Test
    void factoryCreatesPlansByName() {
        assertInstanceOf(NormalPlan.class, DiscountPlanFactory.create("normal"));
        assertInstanceOf(PrimePlan.class, DiscountPlanFactory.create("prime"));
        assertInstanceOf(PlatinumPlan.class, DiscountPlanFactory.create("platinum"));
        assertInstanceOf(PrimePlan.class, DiscountPlanFactory.create("PRIME")); // case-insensitive
    }

    @Test
    void factoryRejectsUnknownPlan() {
        assertThrows(IllegalArgumentException.class, () -> DiscountPlanFactory.create("gold"));
    }

    @Test
    void annualFeesMatchTheSpecification() {
        assertEquals(0.0, new NormalPlan().getAnnualFee(), 1e-9);
        assertEquals(50.0, new PrimePlan().getAnnualFee(), 1e-9);
        assertEquals(200.0, new PlatinumPlan().getAnnualFee(), 1e-9);
    }
}
