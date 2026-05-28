package supermarket.model;

import supermarket.pricing.NoPricingPolicy;
import supermarket.pricing.PricingPolicy;

public class Category {
    private final String name;
    private PricingPolicy pricingPolicy;

    public Category(String name) {
        this.name = name;
        this.pricingPolicy = new NoPricingPolicy();
    }

    public String getName() { return name; }
    public PricingPolicy getPricingPolicy() { return pricingPolicy; }
    public void setPricingPolicy(PricingPolicy policy) { this.pricingPolicy = policy; }
}
