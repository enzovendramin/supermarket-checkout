package supermarket.payment;

import java.util.Optional;

/**
 * Holds a one-shot forced outcome used by {@code simulatePayment}. When set,
 * the next POS payment returns this outcome instead of performing the real
 * PIN/TAS checks, allowing reproducible unhappy-path scenarios without
 * manipulating the bank state.
 */
public class PaymentSimulator {
    private PaymentResult forcedOutcome = null;

    public void force(PaymentResult outcome) {
        this.forcedOutcome = outcome;
    }

    public boolean hasForcedOutcome() {
        return forcedOutcome != null;
    }

    /** Returns and clears the forced outcome (one-shot). */
    public Optional<PaymentResult> consume() {
        Optional<PaymentResult> out = Optional.ofNullable(forcedOutcome);
        forcedOutcome = null;
        return out;
    }
}
