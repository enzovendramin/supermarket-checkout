package supermarket.model;

import java.util.Locale;

public class Receipt {
    private final String customerUsername;
    private final double subtotalBeforeDiscount;
    private final double promotionDiscount;
    private final double subtotalAfterDiscount;
    private final double tax;
    private final double deliveryCost;
    private final double total;

    public Receipt(String customerUsername, double subtotalBeforeDiscount,
                   double promotionDiscount, double subtotalAfterDiscount,
                   double tax, double deliveryCost, double total) {
        this.customerUsername = customerUsername;
        this.subtotalBeforeDiscount = subtotalBeforeDiscount;
        this.promotionDiscount = promotionDiscount;
        this.subtotalAfterDiscount = subtotalAfterDiscount;
        this.tax = tax;
        this.deliveryCost = deliveryCost;
        this.total = total;
    }

    public String getCustomerUsername() { return customerUsername; }
    public double getSubtotalBeforeDiscount() { return subtotalBeforeDiscount; }
    public double getPromotionDiscount() { return promotionDiscount; }
    public double getSubtotalAfterDiscount() { return subtotalAfterDiscount; }
    public double getTax() { return tax; }
    public double getDeliveryCost() { return deliveryCost; }
    public double getTotal() { return total; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "Receipt for %s:%n", customerUsername));
        sb.append(String.format(Locale.US, "  Subtotal (before discount): €%.2f%n", subtotalBeforeDiscount));
        if (promotionDiscount > 0.0) {
            sb.append(String.format(Locale.US, "  Promotions:                -€%.2f%n", promotionDiscount));
        }
        sb.append(String.format(Locale.US, "  Subtotal (after discount):  €%.2f%n", subtotalAfterDiscount));
        if (tax > 0.0) {
            sb.append(String.format(Locale.US, "  VAT:                        €%.2f%n", tax));
        }
        sb.append(String.format(Locale.US, "  Delivery:                   €%.2f%n", deliveryCost));
        sb.append(String.format(Locale.US, "  TOTAL:                      €%.2f", total));
        return sb.toString();
    }
}
