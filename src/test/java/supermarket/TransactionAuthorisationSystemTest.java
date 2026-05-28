package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import supermarket.model.BankCard;
import supermarket.payment.PaymentResult;
import supermarket.payment.TransactionAuthorisationSystem;

class TransactionAuthorisationSystemTest {

    @Test
    void authorisesAndDebitsAndCreditsShopOnSuccess() {
        TransactionAuthorisationSystem tas = new TransactionAuthorisationSystem();
        BankCard card = new BankCard("4242424242424242", "1234", 100.0);
        tas.registerCard(card);

        assertEquals(PaymentResult.SUCCESS, tas.authorise("4242424242424242", 30.0));
        assertEquals(70.0, card.getBalance(), 1e-9);   // card debited
        assertEquals(30.0, tas.getShopAccount(), 1e-9); // shop credited
    }

    @Test
    void refusesWhenBalanceInsufficient() {
        TransactionAuthorisationSystem tas = new TransactionAuthorisationSystem();
        tas.registerCard(new BankCard("4242424242424242", "1234", 5.0));
        assertEquals(PaymentResult.INSUFFICIENT_FUNDS, tas.authorise("4242424242424242", 30.0));
        assertEquals(0.0, tas.getShopAccount(), 1e-9);
    }

    @Test
    void deniesUnregisteredCard() {
        TransactionAuthorisationSystem tas = new TransactionAuthorisationSystem();
        assertEquals(PaymentResult.AUTH_DENIED, tas.authorise("0000000000000000", 10.0));
    }
}
