package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import supermarket.model.BankCard;

class BankCardTest {

    @Test
    void pinMatchesOnlyForCorrectPin() {
        BankCard card = new BankCard("4242424242424242", "1234", 100.0);
        assertTrue(card.pinMatches("1234"));
        assertFalse(card.pinMatches("0000"));
    }

    @Test
    void debitAndCreditAdjustBalance() {
        BankCard card = new BankCard("4242424242424242", "1234", 100.0);
        card.debit(30.0);
        assertEquals(70.0, card.getBalance(), 1e-9);
        card.credit(10.0);
        assertEquals(80.0, card.getBalance(), 1e-9);
    }
}
