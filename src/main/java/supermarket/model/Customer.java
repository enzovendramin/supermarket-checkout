package supermarket.model;

import supermarket.discount.DiscountPlan;
import supermarket.discount.NormalPlan;
import supermarket.users.User;

public class Customer extends User {
    private final String address;
    private int numericalId;
    private DiscountPlan discountPlan;
    private BankCard bankCard;
    private int loyaltyPoints = 0;

    public Customer(String firstName, String lastName, String username, String address, String password) {
        super(firstName, lastName, username, password, "customer");
        this.address = address;
        this.discountPlan = new NormalPlan();
    }

    public String getAddress() { return address; }

    /** Unique numerical ID characterising the customer (spec 2.3). */
    public int getNumericalId() { return numericalId; }
    public void setNumericalId(int numericalId) { this.numericalId = numericalId; }
    public DiscountPlan getDiscountPlan() { return discountPlan; }
    public void setDiscountPlan(DiscountPlan plan) { this.discountPlan = plan; }
    public BankCard getBankCard() { return bankCard; }
    public void setBankCard(BankCard card) { this.bankCard = card; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void addLoyaltyPoints(int points) { this.loyaltyPoints += points; }
}
