package supermarket.delivery;

/**
 * Strategy for computing the (pre-plan-discount) home-delivery fee from the
 * order weight, the distance and the order value (R8). Making this an interface
 * lets the supermarket swap in new charging schemes without touching the rest
 * of the system.
 */
public interface DeliveryCalculator {
    /**
     * @throws DeliveryNotSupportedException if the order cannot be delivered.
     */
    double computeFee(double totalWeightKg, double distanceKm, double orderTotal);
}
