package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import supermarket.model.Customer;
import supermarket.model.Item;
import supermarket.register.Supermarket;

class SupermarketTest {

    @Test
    void addItemCreatesUnknownCategoryOnTheFly() {
        Supermarket market = new Supermarket();
        Item item = market.addItem("kombucha", "drinks", 2.50, 0.40, 12);
        assertNotNull(market.getCategory("drinks")); // R6b: category created on the fly
        assertEquals("drinks", item.getCategory().getName());
        assertEquals(12, market.getInventory().getStock(item));
    }

    @Test
    void requireItemThrowsForUnknownItem() {
        Supermarket market = new Supermarket();
        assertThrows(IllegalArgumentException.class, () -> market.requireItem("ghost"));
    }

    @Test
    void setCategoryDiscountThrowsForUnknownCategory() {
        Supermarket market = new Supermarket();
        assertThrows(IllegalArgumentException.class,
            () -> market.setCategoryDiscount("nonexistent", 10));
    }

    @Test
    void newCustomerStartsOnNormalPlanAndCanSubscribe() {
        Supermarket market = new Supermarket();
        Customer alice = market.registerCustomer("Alice", "Martin", "alice", "addr", "pwd");
        assertEquals("normal", alice.getDiscountPlan().getName());
        assertNotNull(alice.getBankCard()); // auto-generated test card

        market.subscribeToPlan(alice, "platinum");
        assertEquals("platinum", alice.getDiscountPlan().getName());
    }

    @Test
    void requestDeliveryIsConsumedByNextCheckout() {
        Supermarket market = new Supermarket();
        market.addItem("milk", "dairy", 1.20, 1.00, 10);
        market.registerCustomer("Alice", "Martin", "alice", "1 rue X", "pwd");

        market.requestDelivery("alice", "1 rue X");
        market.openCheckout("alice");
        market.getCashRegister().scanItem(market.getItem("milk"), 1);

        // Normal plan: delivery not discounted -> flat €15 added to the bill.
        assertEquals(15.0, market.getCashRegister().computeBill().getDeliveryCost(), 1e-9);
    }

    @Test
    void setupIsIdempotent() {
        Supermarket market = new Supermarket();
        market.setup();
        market.setup(); // must not fail or duplicate
        assertNotNull(market.getCategory("fruit-and-vegetables"));
        assertNotNull(market.getCategory("dairy"));
        assertNotNull(market.getCategory("meat"));
        assertTrue(market.getTas().isRegistered("4242424242424242"));
    }
}
