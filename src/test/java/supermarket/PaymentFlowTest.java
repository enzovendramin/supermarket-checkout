package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import supermarket.model.BankCard;
import supermarket.model.Customer;
import supermarket.payment.PaymentResult;
import supermarket.register.CashRegister;
import supermarket.register.Supermarket;

class PaymentFlowTest {

    private Supermarket marketWithCheckout() {
        Supermarket market = new Supermarket();
        market.addItem("steak", "meat", 12.50, 0.50, 5);
        Customer alice = market.registerCustomer("Alice", "Martin", "alice", "addr", "pwd");
        // Give Alice a known card registered with the bank.
        BankCard card = new BankCard("4242424242424242", "1234", 100.0);
        alice.setBankCard(card);
        market.getTas().registerCard(card);

        CashRegister register = market.getCashRegister();
        register.startCheckout(alice);
        register.scanItem(market.getItem("steak"), 1);
        return market;
    }

    @Test
    void forcedInsufficientFundsThenSuccessfulRetry() {
        Supermarket market = marketWithCheckout();
        CashRegister register = market.getCashRegister();

        register.simulatePayment(PaymentResult.INSUFFICIENT_FUNDS);
        assertEquals(PaymentResult.INSUFFICIENT_FUNDS, register.pay("4242424242424242", "1234"));
        // Checkout stays open after a failed payment.
        assertEquals(true, register.isCheckoutOpen());

        register.simulatePayment(PaymentResult.SUCCESS);
        assertEquals(PaymentResult.SUCCESS, register.pay("4242424242424242", "1234"));
        assertEquals(false, register.isCheckoutOpen());
        assertEquals(12.50, register.getTotalRevenue(), 1e-9);
        // Stock decremented after the sale.
        assertEquals(4, market.getInventory().getStock(market.getItem("steak")));
    }

    @Test
    void realFlowRejectsWrongPin() {
        Supermarket market = marketWithCheckout();
        CashRegister register = market.getCashRegister();
        assertEquals(PaymentResult.PIN_WRONG, register.pay("4242424242424242", "0000"));
    }

    @Test
    void realFlowRejectsUnregisteredCard() {
        Supermarket market = marketWithCheckout();
        CashRegister register = market.getCashRegister();
        assertEquals(PaymentResult.AUTH_DENIED, register.pay("0000000000000000", "1234"));
    }

    @Test
    void realFlowRejectsInsufficientBalance() {
        Supermarket market = new Supermarket();
        market.addItem("steak", "meat", 12.50, 0.50, 5);
        Customer alice = market.registerCustomer("Alice", "Martin", "alice", "addr", "pwd");
        BankCard poorCard = new BankCard("1111222233334444", "1234", 5.0);
        alice.setBankCard(poorCard);
        market.getTas().registerCard(poorCard);

        CashRegister register = market.getCashRegister();
        register.startCheckout(alice);
        register.scanItem(market.getItem("steak"), 1); // 12.50 > 5.00 balance
        assertEquals(PaymentResult.INSUFFICIENT_FUNDS, register.pay("1111222233334444", "1234"));
    }
}
