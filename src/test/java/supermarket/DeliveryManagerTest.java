package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import supermarket.delivery.DeliveryManager;
import supermarket.delivery.OverbookingException;
import supermarket.delivery.TimeSlot;

class DeliveryManagerTest {

    @Test
    void preventsOverbookingBeyondVehicleCapacity() {
        DeliveryManager manager = new DeliveryManager(2);
        TimeSlot slot = new TimeSlot(8); // 08:00-10:00, off-peak

        manager.book(slot);
        manager.book(slot);
        assertEquals(0, manager.remainingCapacity(slot));
        assertThrows(OverbookingException.class, () -> manager.book(slot));
    }

    @Test
    void peakSlotAppliesSurcharge() {
        DeliveryManager manager = new DeliveryManager(5);
        TimeSlot peak = new TimeSlot(18); // 18:00-20:00, peak
        assertEquals(15.0 * 1.5, manager.quote(15.0, peak, false), 1e-9);
    }

    @Test
    void ecoSlotWithNearbyTruckGetsDiscount() {
        DeliveryManager manager = new DeliveryManager(5);
        TimeSlot offPeak = new TimeSlot(10); // 10:00-12:00
        assertEquals(15.0 * 0.8, manager.quote(15.0, offPeak, true), 1e-9);
    }

    @Test
    void offPeakWithoutNearbyTruckIsBasePrice() {
        DeliveryManager manager = new DeliveryManager(5);
        TimeSlot offPeak = new TimeSlot(10);
        assertEquals(15.0, manager.quote(15.0, offPeak, false), 1e-9);
    }
}
