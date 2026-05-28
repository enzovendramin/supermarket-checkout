package supermarket.users;

public abstract class User {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String password;
    private final String role;

    protected User(String firstName, String lastName, String username, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public boolean checkPassword(String pwd) { return password.equals(pwd); }
    public String getRole() { return role; }
}
