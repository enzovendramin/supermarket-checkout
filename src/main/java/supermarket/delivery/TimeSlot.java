package supermarket.delivery;

import java.util.Objects;

/**
 * A 2-hour home-delivery window identified by its start hour (R10).
 * For example, {@code new TimeSlot(8)} represents the 08:00–10:00 window.
 */
public class TimeSlot {
    public static final int WINDOW_HOURS = 2;
    private static final int PEAK_START = 17; // evening peak: 17:00–20:00
    private static final int PEAK_END = 20;

    private final int startHour;

    public TimeSlot(int startHour) {
        if (startHour < 0 || startHour + WINDOW_HOURS > 24) {
            throw new IllegalArgumentException("Invalid slot start hour: " + startHour);
        }
        this.startHour = startHour;
    }

    public int getStartHour() { return startHour; }

    /** Peak windows attract a dynamic-pricing surcharge. */
    public boolean isPeak() {
        return startHour >= PEAK_START && startHour < PEAK_END;
    }

    public String label() {
        return String.format("%02d:00-%02d:00", startHour, startHour + WINDOW_HOURS);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof TimeSlot other) && other.startHour == this.startHour;
    }

    @Override
    public int hashCode() { return Objects.hash(startHour); }

    @Override
    public String toString() { return label(); }
}
