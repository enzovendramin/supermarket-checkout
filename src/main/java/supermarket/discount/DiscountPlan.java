package supermarket.discount;

public interface DiscountPlan {
    double applyDiscount(double subtotal);
    double applyDeliveryDiscount(double deliveryCost);
    String getName();
}
