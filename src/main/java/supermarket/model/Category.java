package supermarket.model;

import supermarket.pricing.NoPricingPolicy;
import supermarket.pricing.PricingPolicy;

public class Category {
    private final String name;
    private PricingPolicy pricingPolicy;
    private double vatRate = 0.0; // VAT percentage applied to this category (0 = none)

    public Category(String name) {
        this.name = name;
        this.pricingPolicy = new NoPricingPolicy();
    }

    public String getName() { return name; }
    public PricingPolicy getPricingPolicy() { return pricingPolicy; }
    public void setPricingPolicy(PricingPolicy policy) { this.pricingPolicy = policy; }

    /** VAT rate as a percentage (e.g. 5.5 for 5.5%). */
    public double getVatRate() { return vatRate; }
    public void setVatRate(double vatRate) { this.vatRate = vatRate; }
}
