package supermarket.discount;

public class PrimePlan implements DiscountPlan {
    @Override
    public double applyDiscount(double subtotal) {
        return subtotal >= 50.0 ? subtotal * 0.80 : subtotal;
    }

    @Override
    public double applyDeliveryDiscount(double deliveryCost) {
        return deliveryCost * 0.50;
    }

    @Override
    public String getName() { return "prime"; }

    @Override
    public double getAnnualFee() { return 50.0; }
}
