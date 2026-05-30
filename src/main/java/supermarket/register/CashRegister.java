package supermarket.register;

import supermarket.command.CommandHistory;
import supermarket.command.ScanItemCommand;
import supermarket.delivery.DeliveryCalculator;
import supermarket.delivery.DeliveryRequest;
import supermarket.discount.DiscountPlan;
import supermarket.inventory.Inventory;
import supermarket.loyalty.LoyaltyProgram;
import supermarket.model.Cart;
import supermarket.model.CartEntry;
import supermarket.model.Customer;
import supermarket.model.Item;
import supermarket.model.Receipt;
import supermarket.payment.PaymentResult;
import supermarket.payment.POSDevice;
import supermarket.promotion.PromotionEngine;

/**
 * Orchestrates a checkout session (spec 2.4): scanning items, computing the
 * bill (category pricing policy + customer discount plan + delivery), and
 * processing payment through the POS. On a successful sale it decrements the
 * inventory (possibly firing R9 alerts) and records the revenue.
 */
public class CashRegister {
    private final Inventory inventory;
    private final POSDevice pos;
    private final DeliveryCalculator deliveryCalculator;
    private final PromotionEngine promotionEngine;
    private final LoyaltyProgram loyaltyProgram;

    private Customer currentCustomer;
    private Cart cart;
    private boolean checkoutOpen = false;
    private DeliveryRequest pendingDelivery;
    private CommandHistory history = new CommandHistory();

    private double totalRevenue = 0.0;
    private Receipt lastReceipt;
    private int lastPointsEarned = 0;

    public CashRegister(Inventory inventory, POSDevice pos, DeliveryCalculator deliveryCalculator,
                        PromotionEngine promotionEngine, LoyaltyProgram loyaltyProgram) {
        this.inventory = inventory;
        this.pos = pos;
        this.deliveryCalculator = deliveryCalculator;
        this.promotionEngine = promotionEngine;
        this.loyaltyProgram = loyaltyProgram;
    }

    /** Opens a checkout for a customer with no home delivery. */
    public void startCheckout(Customer customer) {
        startCheckout(customer, null);
    }

    /**
     * Opens a checkout for a customer, loading their discount plan and an
     * optional pending home-delivery request (R7).
     */
    public void startCheckout(Customer customer, DeliveryRequest delivery) {
        this.currentCustomer = customer;
        this.cart = new Cart();
        this.checkoutOpen = true;
        this.pendingDelivery = delivery;
        this.history = new CommandHistory();
    }

    /** Scans an item as an undoable command (Command pattern). */
    public void scanItem(Item item, int quantity) {
        requireOpenCheckout();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        history.run(new ScanItemCommand(cart, item, quantity));
    }

    /** Undoes the last reversible checkout action (e.g. a scan); returns its description. */
    public String undoLastAction() {
        requireOpenCheckout();
        return history.undo();
    }

    /** Redoes the last undone checkout action; returns its description. */
    public String redoLastAction() {
        requireOpenCheckout();
        return history.redo();
    }

    /**
     * Computes the bill: each item is priced through its category's pricing
     * policy (R6), the customer's discount plan is applied to the subtotal (R5),
     * and—if a delivery was requested—the plan-adjusted delivery fee is added
     * (R8/R8b). Throws {@code DeliveryNotSupportedException} if the order is too
     * heavy to deliver.
     */
    public Receipt computeBill() {
        requireOpenCheckout();
        DiscountPlan plan = currentCustomer.getDiscountPlan();

        double rawTotal = 0.0;
        double afterCategory = 0.0;
        double tax = 0.0;
        for (CartEntry entry : cart.getEntries()) {
            Item item = entry.getItem();
            int qty = entry.getQuantity();
            double unitPrice = item.unitPriceFor(qty); // R3: price may depend on quantity
            rawTotal += unitPrice * qty;
            double netLine = item.getCategory().getPricingPolicy().apply(unitPrice) * qty; // R6
            afterCategory += netLine;
            tax += netLine * item.getCategory().getVatRate() / 100.0; // VAT on the net line price
        }

        // Promotions reduce the items subtotal, capped so it never goes negative.
        double promoDiscount = Math.min(promotionEngine.totalDiscount(cart), afterCategory);
        double afterPromo = afterCategory - promoDiscount;

        double afterPlan = plan.applyDiscount(afterPromo); // R5

        double deliveryCost = 0.0;
        if (pendingDelivery != null) {
            double rawFee = deliveryCalculator.computeFee(
                cart.totalWeightKg(), pendingDelivery.getDistanceKm(), afterPlan);
            deliveryCost = plan.applyDeliveryDiscount(rawFee); // R8/R8b
        }

        double total = afterPlan + tax + deliveryCost;

        lastReceipt = new Receipt(currentCustomer.getUsername(),
            rawTotal, promoDiscount, afterPlan, tax, deliveryCost, total);
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
            lastPointsEarned = loyaltyProgram.pointsFor(receipt.getTotal());
            currentCustomer.addLoyaltyPoints(lastPointsEarned); // loyalty rewards
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
    public int getLastPointsEarned() { return lastPointsEarned; }

    private void closeCheckout() {
        checkoutOpen = false;
        currentCustomer = null;
        cart = null;
        pendingDelivery = null;
    }

    private void requireOpenCheckout() {
        if (!checkoutOpen) {
            throw new IllegalStateException("No checkout is currently open.");
        }
    }
}
