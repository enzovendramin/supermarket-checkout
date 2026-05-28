package supermarket.payment;

/** Possible outcomes of a POS payment attempt. */
public enum PaymentResult {
    SUCCESS,
    INSUFFICIENT_FUNDS,
    PIN_WRONG,
    AUTH_DENIED
}
