package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import supermarket.delivery.TimeSlot;

class TimeSlotTest {

    @Test
    void labelDescribesTheTwoHourWindow() {
        assertEquals("08:00-10:00", new TimeSlot(8).label());
        assertEquals("22:00-24:00", new TimeSlot(22).label());
    }

    @Test
    void peakWindowsAreFlagged() {
        assertFalse(new TimeSlot(16).isPeak());
        assertTrue(new TimeSlot(17).isPeak());
        assertTrue(new TimeSlot(19).isPeak());
        assertFalse(new TimeSlot(20).isPeak());
    }

    @Test
    void slotsWithSameStartHourAreEqual() {
        assertEquals(new TimeSlot(8), new TimeSlot(8));
        assertEquals(new TimeSlot(8).hashCode(), new TimeSlot(8).hashCode());
        assertNotEquals(new TimeSlot(8), new TimeSlot(10));
    }

    @Test
    void invalidStartHourIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new TimeSlot(-1));
        assertThrows(IllegalArgumentException.class, () -> new TimeSlot(23)); // 23+2 > 24
    }
}
