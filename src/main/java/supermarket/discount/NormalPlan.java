package supermarket.discount;

public class NormalPlan implements DiscountPlan {
    @Override
    public double applyDiscount(double subtotal) { return subtotal; }

    @Override
    public double applyDeliveryDiscount(double deliveryCost) { return deliveryCost; }

    @Override
    public String getName() { return "normal"; }
}
