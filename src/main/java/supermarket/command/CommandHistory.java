package supermarket.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Invoker of the Command pattern: runs commands and keeps undo/redo stacks so
 * the most recent reversible actions can be undone and redone.
 */
public class CommandHistory {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /** Executes a command and records it, clearing the redo history. */
    public void run(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    /** Undoes the most recent command; returns its description. */
    public String undo() {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("Nothing to undo.");
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return command.describe();
    }

    /** Re-executes the most recently undone command; returns its description. */
    public String redo() {
        if (redoStack.isEmpty()) {
            throw new IllegalStateException("Nothing to redo.");
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        return command.describe();
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}
