package supermarket.loyalty;

/**
 * Loyalty rewards rule: customers earn points for the amount they spend.
 * Kept as a small service so the earning rate can be changed in one place.
 */
public class LoyaltyProgram {
    public static final int POINTS_PER_EURO = 1;

    /** Points earned for a given spend (one point per whole euro). */
    public int pointsFor(double amountSpent) {
        return (int) Math.floor(amountSpent) * POINTS_PER_EURO;
    }
}
