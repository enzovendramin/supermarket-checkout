package supermarket.pricing;

public interface PricingPolicy {
    double apply(double price);
    String getName();
}
