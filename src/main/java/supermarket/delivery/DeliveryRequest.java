package supermarket.delivery;

/**
 * A pending home-delivery request (R7): the destination address and its
 * distance from the supermarket. Created when a customer issues
 * {@code requestDelivery} and consumed by the next checkout.
 */
public class DeliveryRequest {
    private final String address;
    private final double distanceKm;

    public DeliveryRequest(String address, double distanceKm) {
        this.address = address;
        this.distanceKm = distanceKm;
    }

    public String getAddress() { return address; }
    public double getDistanceKm() { return distanceKm; }
}
