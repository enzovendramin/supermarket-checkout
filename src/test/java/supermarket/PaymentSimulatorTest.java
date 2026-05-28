package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import supermarket.payment.PaymentResult;
import supermarket.payment.PaymentSimulator;

class PaymentSimulatorTest {

    @Test
    void hasNoForcedOutcomeByDefault() {
        assertFalse(new PaymentSimulator().hasForcedOutcome());
    }

    @Test
    void forcedOutcomeIsConsumedExactlyOnce() {
        PaymentSimulator simulator = new PaymentSimulator();
        simulator.force(PaymentResult.AUTH_DENIED);
        assertTrue(simulator.hasForcedOutcome());

        assertEquals(PaymentResult.AUTH_DENIED, simulator.consume().orElseThrow());
        // One-shot: the outcome is cleared after being consumed.
        assertFalse(simulator.hasForcedOutcome());
        assertTrue(simulator.consume().isEmpty());
    }
}
