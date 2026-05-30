package supermarket.command;

import java.util.Locale;

import supermarket.model.Cart;
import supermarket.model.CartEntry;
import supermarket.model.Item;

/**
 * Adds a scanned item to the cart; undo removes that exact line again. Used to
 * let a cashier correct a mis-scan with {@code undo}/{@code redo}.
 */
public class ScanItemCommand implements Command {
    private final Cart cart;
    private final CartEntry entry;

    public ScanItemCommand(Cart cart, Item item, int quantity) {
        this.cart = cart;
        this.entry = new CartEntry(item, quantity);
    }

    @Override
    public void execute() {
        cart.addEntry(entry);
    }

    @Override
    public void undo() {
        cart.removeEntry(entry);
    }

    @Override
    public String describe() {
        return String.format(Locale.US, "scan %d x %s",
            entry.getQuantity(), entry.getItem().getName());
    }
}
