package supermarket.users;

public class Cashier extends User {
    public Cashier(String firstName, String lastName, String username, String password) {
        super(firstName, lastName, username, password, "cashier");
    }
}
