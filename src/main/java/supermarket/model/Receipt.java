package supermarket.model;

public class Receipt {
    private final String customerUsername;
    private final double subtotalBeforeDiscount;
    private final double subtotalAfterDiscount;
    private final double deliveryCost;
    private final double total;

    public Receipt(String customerUsername, double subtotalBeforeDiscount,
                   double subtotalAfterDiscount, double deliveryCost, double total) {
        this.customerUsername = customerUsername;
        this.subtotalBeforeDiscount = subtotalBeforeDiscount;
        this.subtotalAfterDiscount = subtotalAfterDiscount;
        this.deliveryCost = deliveryCost;
        this.total = total;
    }

    public String getCustomerUsername() { return customerUsername; }
    public double getSubtotalBeforeDiscount() { return subtotalBeforeDiscount; }
    public double getSubtotalAfterDiscount() { return subtotalAfterDiscount; }
    public double getDeliveryCost() { return deliveryCost; }
    public double getTotal() { return total; }

    @Override
    public String toString() {
        return String.format(
            "Receipt for %s:%n  Subtotal (before discount): €%.2f%n  Subtotal (after discount):  €%.2f%n  Delivery:                   €%.2f%n  TOTAL:                      €%.2f",
            customerUsername, subtotalBeforeDiscount, subtotalAfterDiscount, deliveryCost, total);
    }
}
