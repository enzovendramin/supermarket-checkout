package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import supermarket.command.CommandHistory;
import supermarket.model.Customer;
import supermarket.register.CashRegister;
import supermarket.register.Supermarket;

class CommandUndoRedoTest {

    private Supermarket market;
    private CashRegister register;

    @BeforeEach
    void setUp() {
        market = new Supermarket();
        market.addItem("milk", "dairy", 1.20, 1.00, 100);
        Customer eve = market.registerCustomer("Eve", "Stone", "eve", "addr", "pwd");
        register = market.getCashRegister();
        register.startCheckout(eve);
    }

    @Test
    void undoRemovesLastScanAndRedoRestoresIt() {
        register.scanItem(market.getItem("milk"), 2);
        register.scanItem(market.getItem("milk"), 3);
        assertEquals(2, register.getCart().getEntries().size());

        register.undoLastAction();
        assertEquals(1, register.getCart().getEntries().size());

        register.redoLastAction();
        assertEquals(2, register.getCart().getEntries().size());
    }

    @Test
    void undoWithNothingToUndoIsReported() {
        assertThrows(IllegalStateException.class, register::undoLastAction);
    }

    @Test
    void commandHistoryRedoWithEmptyStackThrows() {
        assertThrows(IllegalStateException.class, new CommandHistory()::redo);
    }
}
