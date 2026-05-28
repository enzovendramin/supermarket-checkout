package supermarket.delivery;

/** Thrown when a delivery slot has no remaining vehicle capacity (R10). */
public class OverbookingException extends RuntimeException {
    public OverbookingException(TimeSlot slot) {
        super("Delivery slot " + slot.label() + " is fully booked.");
    }
}
