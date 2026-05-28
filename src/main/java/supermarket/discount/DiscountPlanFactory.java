package supermarket.discount;

public class DiscountPlanFactory {
    public static DiscountPlan create(String name) {
        return switch (name.toLowerCase()) {
            case "normal"   -> new NormalPlan();
            case "prime"    -> new PrimePlan();
            case "platinum" -> new PlatinumPlan();
            default -> throw new IllegalArgumentException("Unknown plan: " + name);
        };
    }
}
