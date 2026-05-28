package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import supermarket.delivery.DeliveryNotSupportedException;
import supermarket.delivery.StandardDeliveryCalculator;

class DeliveryCalculatorTest {

    private final StandardDeliveryCalculator calc = new StandardDeliveryCalculator();

    @Test
    void lightCloseOrderPaysFlatFee() {
        // 5 kg, 10 km -> flat €15 regardless of order value.
        assertEquals(15.0, calc.computeFee(5.0, 10.0, 100.0), 1e-9);
    }

    @Test
    void heavyOrderPaysFlatFeePlusPercentage() {
        // 25 kg -> €15 + 10% of €200 = €35.
        assertEquals(15.0 + 0.10 * 200.0, calc.computeFee(25.0, 10.0, 200.0), 1e-9);
    }

    @Test
    void lightButFarOrderPaysFlatFeePlusPercentage() {
        // 5 kg but 50 km (beyond 30 km) -> flat + percentage.
        assertEquals(15.0 + 0.10 * 100.0, calc.computeFee(5.0, 50.0, 100.0), 1e-9);
    }

    @Test
    void orderOver50KgIsRefused() {
        assertThrows(DeliveryNotSupportedException.class,
            () -> calc.computeFee(60.0, 10.0, 100.0));
    }
}
