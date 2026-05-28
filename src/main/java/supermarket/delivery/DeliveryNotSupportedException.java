package supermarket.delivery;

/** Thrown when a delivery cannot be supported, e.g. the order exceeds 50 kg (R8). */
public class DeliveryNotSupportedException extends RuntimeException {
    public DeliveryNotSupportedException(double weightKg) {
        super(String.format("Home delivery refused: order weight %.2f kg exceeds the 50 kg limit.", weightKg));
    }
}
