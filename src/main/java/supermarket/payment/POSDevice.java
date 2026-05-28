package supermarket.payment;

import supermarket.model.BankCard;

/**
 * Point-of-Sale terminal (spec 2.5). Reads the card, verifies the PIN against
 * the value stored on the card chip, then asks the TAS to authorise the debit.
 * A {@link PaymentSimulator} may force the outcome of the next payment.
 */
public class POSDevice {
    private final TransactionAuthorisationSystem tas;
    private final PaymentSimulator simulator = new PaymentSimulator();

    public POSDevice(TransactionAuthorisationSystem tas) {
        this.tas = tas;
    }

    /** Forces the outcome of the next {@link #pay} call. */
    public void simulateNextPayment(PaymentResult outcome) {
        simulator.force(outcome);
    }

    /**
     * Processes a payment for {@code amount}. If an outcome was forced via
     * {@code simulatePayment}, it is honoured (consuming it) without touching
     * the bank state. Otherwise the real flow runs: card lookup, PIN check,
     * then TAS authorisation.
     */
    public PaymentResult pay(String cardNumber, String pin, double amount) {
        if (simulator.hasForcedOutcome()) {
            return simulator.consume().orElse(PaymentResult.AUTH_DENIED);
        }

        BankCard card = tas.getCard(cardNumber);
        if (card == null) {
            // Card not known to the bank: cannot be authorised.
            return PaymentResult.AUTH_DENIED;
        }
        if (!card.pinMatches(pin)) {
            return PaymentResult.PIN_WRONG;
        }
        return tas.authorise(cardNumber, amount);
    }
}
