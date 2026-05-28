package supermarket.pricing;

public class CategoryDiscount implements PricingPolicy {
    private final double percent;

    public CategoryDiscount(double percent) {
        this.percent = percent;
    }

    @Override
    public double apply(double price) {
        return price * (1.0 - percent / 100.0);
    }

    @Override
    public String getName() { return percent + "% discount"; }

    public double getPercent() { return percent; }
}
