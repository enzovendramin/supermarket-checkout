package supermarket.delivery;

/**
 * Default delivery charging scheme (R8):
 * <ul>
 *   <li>orders over 50 kg are refused;</li>
 *   <li>light orders (≤ 10 kg within 30 km) pay a flat fee;</li>
 *   <li>heavier orders (10–50 kg, or beyond 30 km) pay the flat fee plus a
 *       percentage of the order value.</li>
 * </ul>
 */
public class StandardDeliveryCalculator implements DeliveryCalculator {
    public static final double FLAT_FEE = 15.0;
    public static final double LIGHT_WEIGHT_KG = 10.0;
    public static final double MAX_FLAT_DISTANCE_KM = 30.0;
    public static final double MAX_WEIGHT_KG = 50.0;
    public static final double WEIGHT_SURCHARGE_RATE = 0.10; // 10% of order value

    @Override
    public double computeFee(double totalWeightKg, double distanceKm, double orderTotal) {
        if (totalWeightKg > MAX_WEIGHT_KG) {
            throw new DeliveryNotSupportedException(totalWeightKg);
        }
        if (totalWeightKg <= LIGHT_WEIGHT_KG && distanceKm <= MAX_FLAT_DISTANCE_KM) {
            return FLAT_FEE;
        }
        return FLAT_FEE + WEIGHT_SURCHARGE_RATE * orderTotal;
    }
}
