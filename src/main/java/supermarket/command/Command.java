package supermarket.command;

/**
 * A reversible action (Command pattern). Lets the cash register record checkout
 * actions so they can be undone and redone.
 */
public interface Command {
    void execute();
    void undo();
    /** Short human-readable description, used in CLUI feedback. */
    String describe();
}
