package supermarket.pricing;

public class NoPricingPolicy implements PricingPolicy {
    @Override
    public double apply(double price) { return price; }

    @Override
    public String getName() { return "none"; }
}
