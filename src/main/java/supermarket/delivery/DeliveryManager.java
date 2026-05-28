package supermarket.delivery;

import java.util.HashMap;
import java.util.Map;

/**
 * Smart logistics module (R10). Manages 2-hour delivery windows with a fixed
 * vehicle capacity per slot, prevents overbooking and applies dynamic pricing:
 * a surcharge for peak windows and a discount for "eco" slots where a truck is
 * already in the customer's vicinity.
 */
public class DeliveryManager {
    public static final double PEAK_SURCHARGE = 1.5; // +50% on peak windows
    public static final double ECO_DISCOUNT = 0.8;   // -20% when a truck is nearby

    private final int vehicleCapacityPerSlot;
    private final Map<TimeSlot, Integer> bookings = new HashMap<>();

    public DeliveryManager(int vehicleCapacityPerSlot) {
        if (vehicleCapacityPerSlot <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        this.vehicleCapacityPerSlot = vehicleCapacityPerSlot;
    }

    public int remainingCapacity(TimeSlot slot) {
        return vehicleCapacityPerSlot - bookings.getOrDefault(slot, 0);
    }

    public boolean hasCapacity(TimeSlot slot) {
        return remainingCapacity(slot) > 0;
    }

    /** Books one delivery into the slot, refusing if the slot is full (anti-overbooking). */
    public void book(TimeSlot slot) {
        if (!hasCapacity(slot)) {
            throw new OverbookingException(slot);
        }
        bookings.merge(slot, 1, Integer::sum);
    }

    /** Dynamic-pricing multiplier for a slot given whether a truck is nearby. */
    public double priceMultiplier(TimeSlot slot, boolean truckInVicinity) {
        double multiplier = 1.0;
        if (slot.isPeak()) {
            multiplier *= PEAK_SURCHARGE;
        }
        if (truckInVicinity) {
            multiplier *= ECO_DISCOUNT;
        }
        return multiplier;
    }

    /** Applies dynamic pricing to a base delivery fee. */
    public double quote(double baseFee, TimeSlot slot, boolean truckInVicinity) {
        return baseFee * priceMultiplier(slot, truckInVicinity);
    }
}
