package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import supermarket.model.Customer;
import supermarket.model.Receipt;
import supermarket.register.CashRegister;
import supermarket.register.Supermarket;

class DeliveryCheckoutTest {

    @Test
    void primeDeliveryFeeIsHalvedAndAddedToBill() {
        Supermarket market = new Supermarket();
        market.addItem("milk", "diary", 1.20, 1.00, 50);
        market.addItem("yogurt", "diary", 0.80, 0.15, 30);
        market.addItem("steak", "meat", 12.50, 0.50, 5);
        market.addItem("apple", "fruit-and-vegetables", 0.30, 0.20, 200);
        market.addItem("tomato", "fruit-and-vegetables", 0.90, 0.25, 100);
        market.setCategoryDiscount("fruit-and-vegetables", 10);

        Customer alice = market.registerCustomer("Alice", "Martin", "alice", "12 rue de la Paix", "pwd");
        market.subscribeToPlan(alice, "prime");
        market.requestDelivery("alice", "12 rue de la Paix");

        market.openCheckout("alice");
        CashRegister register = market.getCashRegister();
        register.scanItem(market.getItem("milk"), 2);
        register.scanItem(market.getItem("yogurt"), 3);
        register.scanItem(market.getItem("steak"), 1);
        register.scanItem(market.getItem("apple"), 10);
        register.scanItem(market.getItem("tomato"), 4);

        Receipt bill = register.computeBill();

        // Items: 23.24 (prime not applied, below €50). Delivery: 5.95 kg @10 km -> €15 flat,
        // prime halves it -> €7.50. Total: 30.74.
        assertEquals(23.24, bill.getSubtotalAfterDiscount(), 1e-9);
        assertEquals(7.50, bill.getDeliveryCost(), 1e-9);
        assertEquals(30.74, bill.getTotal(), 1e-9);
    }
}
