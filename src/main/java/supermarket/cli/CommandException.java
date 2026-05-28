package supermarket.cli;

/** User-facing error for invalid CLUI usage (syntax errors, permissions, etc.). */
public class CommandException extends RuntimeException {
    public CommandException(String message) {
        super(message);
    }
}
