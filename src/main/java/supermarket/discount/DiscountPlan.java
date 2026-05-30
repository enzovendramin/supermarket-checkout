package supermarket.discount;

public interface DiscountPlan {
    double applyDiscount(double subtotal);
    double applyDeliveryDiscount(double deliveryCost);
    String getName();

    /** Annual subscription fee charged immediately when a customer subscribes (spec 2.3). */
    double getAnnualFee();
}
