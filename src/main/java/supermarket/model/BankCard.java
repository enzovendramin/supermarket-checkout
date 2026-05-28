package supermarket.model;

public class BankCard {
    private final String number;
    private final String pin;
    private double balance;

    public BankCard(String number, String pin, double balance) {
        this.number = number;
        this.pin = pin;
        this.balance = balance;
    }

    public String getNumber() { return number; }
    public double getBalance() { return balance; }

    /** PIN check is performed by the POS against the value stored on the card chip (spec 2.5). */
    public boolean pinMatches(String entered) { return pin.equals(entered); }

    public void debit(double amount) { this.balance -= amount; }
    public void credit(double amount) { this.balance += amount; }
}
