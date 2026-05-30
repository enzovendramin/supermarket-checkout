package supermarket.discount;

public class PlatinumPlan implements DiscountPlan {
    @Override
    public double applyDiscount(double subtotal) {
        return subtotal * 0.70;
    }

    @Override
    public double applyDeliveryDiscount(double deliveryCost) {
        return 0.0;
    }

    @Override
    public String getName() { return "platinum"; }

    @Override
    public double getAnnualFee() { return 200.0; }
}
