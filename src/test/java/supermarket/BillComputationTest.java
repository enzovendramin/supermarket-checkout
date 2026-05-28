package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import supermarket.model.Customer;
import supermarket.model.Receipt;
import supermarket.register.CashRegister;
import supermarket.register.Supermarket;

class BillComputationTest {

    private Supermarket buildScenarioMarket() {
        Supermarket market = new Supermarket();
        market.addItem("milk", "diary", 1.20, 1.00, 50);
        market.addItem("yogurt", "diary", 0.80, 0.15, 30);
        market.addItem("steak", "meat", 12.50, 0.50, 5);
        market.addItem("apple", "fruit-and-vegetables", 0.30, 0.20, 200);
        market.addItem("tomato", "fruit-and-vegetables", 0.90, 0.25, 100);
        market.setCategoryDiscount("fruit-and-vegetables", 10);
        market.registerCustomer("Alice", "Martin", "alice", "12 rue de la Paix", "pwd");
        return market;
    }

    @Test
    void categoryPolicyAppliesButPrimeBelowThresholdDoesNot() {
        Supermarket market = buildScenarioMarket();
        Customer alice = market.requireCustomer("alice");
        market.subscribeToPlan(alice, "prime");

        CashRegister register = market.getCashRegister();
        register.startCheckout(alice);
        register.scanItem(market.getItem("milk"), 2);
        register.scanItem(market.getItem("yogurt"), 3);
        register.scanItem(market.getItem("steak"), 1);
        register.scanItem(market.getItem("apple"), 10);
        register.scanItem(market.getItem("tomato"), 4);

        Receipt bill = register.computeBill();

        // Raw catalogue total, no discounts.
        assertEquals(2.40 + 2.40 + 12.50 + 3.00 + 3.60, bill.getSubtotalBeforeDiscount(), 1e-9);
        // -10% on fruit-and-vegetables; prime not applied (subtotal 23.24 < €50).
        assertEquals(2.40 + 2.40 + 12.50 + 2.70 + 3.24, bill.getSubtotalAfterDiscount(), 1e-9);
        assertEquals(0.0, bill.getDeliveryCost(), 1e-9);
        assertEquals(23.24, bill.getTotal(), 1e-9);
    }

    @Test
    void primeAppliesWhenSubtotalReachesThreshold() {
        Supermarket market = buildScenarioMarket();
        Customer alice = market.requireCustomer("alice");
        market.subscribeToPlan(alice, "prime");

        CashRegister register = market.getCashRegister();
        register.startCheckout(alice);
        register.scanItem(market.getItem("steak"), 5); // 62.50 >= 50

        Receipt bill = register.computeBill();
        assertEquals(62.50, bill.getSubtotalBeforeDiscount(), 1e-9);
        assertEquals(62.50 * 0.80, bill.getSubtotalAfterDiscount(), 1e-9);
    }

    @Test
    void platinumAlwaysDiscounts() {
        Supermarket market = buildScenarioMarket();
        Customer alice = market.requireCustomer("alice");
        market.subscribeToPlan(alice, "platinum");

        CashRegister register = market.getCashRegister();
        register.startCheckout(alice);
        register.scanItem(market.getItem("milk"), 1); // 1.20, below €50

        Receipt bill = register.computeBill();
        assertEquals(1.20 * 0.70, bill.getSubtotalAfterDiscount(), 1e-9);
    }
}
