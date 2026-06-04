package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import supermarket.gui.DemoData;
import supermarket.model.Receipt;
import supermarket.payment.PaymentResult;
import supermarket.register.CashRegister;
import supermarket.register.Supermarket;

/**
 * Exercises exactly the domain calls the JavaFX GUI makes (bootstrap, scan,
 * undo/redo, computeBill, pay, low-stock), so the GUI's logic is verified even
 * though the widgets themselves need a display.
 */
class GuiDomainFlowTest {

    @Test
    void demoBootstrapPopulatesCatalogue() {
        Supermarket market = DemoData.build();
        assertNotNull(market.getItem("milk"));
        assertNotNull(market.getItem("steak"));
        assertNotNull(market.requireCustomer(DemoData.CUSTOMER));
    }

    @Test
    void scanUndoRedoPayAndLowStockFlow() {
        Supermarket market = DemoData.build();
        CashRegister register = market.getCashRegister();
        market.openCheckout(DemoData.CUSTOMER);

        register.scanItem(market.getItem("steak"), 2); // 2 x €12.50 = €25 (meat: no VAT/policy)
        assertEquals(25.00, register.computeBill().getTotal(), 1e-9);

        register.undoLastAction();
        assertEquals(0.00, register.computeBill().getTotal(), 1e-9);
        register.redoLastAction();
        assertEquals(25.00, register.computeBill().getTotal(), 1e-9);

        assertEquals(PaymentResult.SUCCESS, register.pay(DemoData.CARD, DemoData.PIN));
        assertEquals(25.00, market.getRevenue(), 1e-9);
        assertEquals(25, market.requireCustomer(DemoData.CUSTOMER).getLoyaltyPoints());
        // steak 5 - 2 = 3 -> at threshold -> low stock (drives the GUI highlight).
        assertTrue(market.getInventory().isLow(market.getItem("steak")));
    }

    @Test
    void dairyVatAppearsInTheBill() {
        Supermarket market = DemoData.build();
        CashRegister register = market.getCashRegister();
        market.openCheckout(DemoData.CUSTOMER);

        register.scanItem(market.getItem("milk"), 2); // 2 x €1.20 = €2.40, dairy VAT 5%
        Receipt bill = register.computeBill();
        assertEquals(0.12, bill.getTax(), 1e-9);   // 5% of €2.40
        assertEquals(2.52, bill.getTotal(), 1e-9);
    }
}
