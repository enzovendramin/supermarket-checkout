package supermarket.payment;

import java.util.HashMap;
import java.util.Map;

import supermarket.model.BankCard;

/**
 * Simulates the bank's Transaction Authorisation System (spec 2.6).
 * Holds the registered client cards and authorises debits against them,
 * crediting the supermarket's account on success. Acts as a Facade hiding
 * the banking protocol from the rest of the system.
 */
public class TransactionAuthorisationSystem {
    private final Map<String, BankCard> registeredCards = new HashMap<>();
    private double shopAccount = 0.0;

    public void registerCard(BankCard card) {
        registeredCards.put(card.getNumber(), card);
    }

    public BankCard getCard(String cardNumber) {
        return registeredCards.get(cardNumber);
    }

    public boolean isRegistered(String cardNumber) {
        return registeredCards.containsKey(cardNumber);
    }

    /**
     * Authorises and performs a transaction for the given amount: debits the
     * card holder and credits the shop account. Assumes the card is registered.
     */
    public PaymentResult authorise(String cardNumber, double amount) {
        BankCard card = registeredCards.get(cardNumber);
        if (card == null) {
            return PaymentResult.AUTH_DENIED;
        }
        if (card.getBalance() < amount) {
            return PaymentResult.INSUFFICIENT_FUNDS;
        }
        card.debit(amount);
        shopAccount += amount;
        return PaymentResult.SUCCESS;
    }

    public double getShopAccount() { return shopAccount; }
}
