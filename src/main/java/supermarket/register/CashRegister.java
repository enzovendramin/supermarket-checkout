package supermarket.register;

import supermarket.discount.DiscountPlan;
import supermarket.inventory.Inventory;
import supermarket.model.Cart;
import supermarket.model.CartEntry;
import supermarket.model.Customer;
import supermarket.model.Item;
import supermarket.model.Receipt;
import supermarket.payment.PaymentResult;
import supermarket.payment.POSDevice;

/**
 * Orchestrates a checkout session (spec 2.4): scanning items, computing the
 * bill (category pricing policy + customer discount plan + delivery), and
 * processing payment through the POS. On a successful sale it decrements the
 * inventory (possibly firing R9 alerts) and records the revenue.
 */
public class CashRegister {
    private final Inventory inventory;
    private final POSDevice pos;

    private Customer currentCustomer;
    private Cart cart;
    private boolean checkoutOpen = false;
    private double pendingDeliveryCost = 0.0;

    private double totalRevenue = 0.0;
    private Receipt lastReceipt;

    public CashRegister(Inventory inventory, POSDevice pos) {
        this.inventory = inventory;
        this.pos = pos;
    }

    /** Opens a checkout for a customer, loading their discount plan. */
    public void startCheckout(Customer customer) {
        this.currentCustomer = customer;
        this.cart = new Cart();
        this.checkoutOpen = true;
        // Delivery cost is computed by the delivery module (R7/R8) in a later phase.
        this.pendingDeliveryCost = 0.0;
    }

    public void scanItem(Item item, int quantity) {
        requireOpenCheckout();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        cart.addEntry(new CartEntry(item, quantity));
    }

    /** Sets the (pre-discount) delivery cost for this checkout; used by R7/R8. */
    public void setPendingDeliveryCost(double cost) {
        this.pendingDeliveryCost = cost;
    }

    /**
     * Computes the bill: each item is priced through its category's pricing
     * policy (R6), the customer's discount plan is applied to the subtotal (R5),
     * and the plan-adjusted delivery fee is added (R8/R8b).
     */
    public Receipt computeBill() {
        requireOpenCheckout();
        DiscountPlan plan = currentCustomer.getDiscountPlan();

        double rawTotal = 0.0;
        double afterCategory = 0.0;
        for (CartEntry entry : cart.getEntries()) {
            Item item = entry.getItem();
            int qty = entry.getQuantity();
            rawTotal += item.getUnitPrice() * qty;
            afterCategory += item.getCategory().getPricingPolicy().apply(item.getUnitPrice()) * qty;
        }

        double afterPlan = plan.applyDiscount(afterCategory);
        double deliveryCost = plan.applyDeliveryDiscount(pendingDeliveryCost);
        double total = afterPlan + deliveryCost;

        lastReceipt = new Receipt(
            currentCustomer.getUsername(), rawTotal, afterPlan, deliveryCost, total);
        return lastReceipt;
    }

    /**
     * Launches the POS payment for the current bill. On success the inventory is
     * decremented (possibly triggering the R9 low-stock notification), the
     * revenue is recorded, the receipt is finalized and the checkout is closed.
     * On failure the checkout stays open so the cashier can retry.
     */
    public PaymentResult pay(String cardNumber, String pin) {
        requireOpenCheckout();

        // Misuse check: never sell more than the current stock.
        for (CartEntry entry : cart.getEntries()) {
            if (inventory.getStock(entry.getItem()) < entry.getQuantity()) {
                throw new IllegalStateException(
                    "Not enough stock for '" + entry.getItem().getName() + "'.");
            }
        }

        Receipt receipt = computeBill();
        PaymentResult result = pos.pay(cardNumber, pin, receipt.getTotal());

        if (result == PaymentResult.SUCCESS) {
            for (CartEntry entry : cart.getEntries()) {
                inventory.decrement(entry.getItem(), entry.getQuantity());
            }
            totalRevenue += receipt.getTotal();
            closeCheckout();
        }
        return result;
    }

    public void simulatePayment(PaymentResult outcome) {
        pos.simulateNextPayment(outcome);
    }

    public boolean isCheckoutOpen() { return checkoutOpen; }
    public Cart getCart() { return cart; }
    public Customer getCurrentCustomer() { return currentCustomer; }
    public Receipt getLastReceipt() { return lastReceipt; }
    public double getTotalRevenue() { return totalRevenue; }

    private void closeCheckout() {
        checkoutOpen = false;
        currentCustomer = null;
        cart = null;
        pendingDeliveryCost = 0.0;
    }

    private void requireOpenCheckout() {
        if (!checkoutOpen) {
            throw new IllegalStateException("No checkout is currently open.");
        }
    }
}
