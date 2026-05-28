package supermarket.cli;

import supermarket.register.Supermarket;
import supermarket.users.User;

/**
 * Session state for the CLUI: the underlying {@link Supermarket} and the
 * currently logged-in user. Provides role guards used by the dispatcher.
 */
public class CommandContext {
    private final Supermarket market;
    private User currentUser;
    private boolean running = true;

    public CommandContext(Supermarket market) {
        this.market = market;
    }

    public Supermarket getMarket() { return market; }
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public boolean isRunning() { return running; }
    public void stop() { this.running = false; }

    public User requireLogin() {
        if (currentUser == null) {
            throw new CommandException("You must be logged in to run this command.");
        }
        return currentUser;
    }

    /** Ensures a user with the given role is logged in, returning that user. */
    public User requireRole(String role) {
        User user = requireLogin();
        if (!user.getRole().equals(role)) {
            throw new CommandException(
                "Permission denied: this command requires the '" + role + "' role.");
        }
        return user;
    }
}
