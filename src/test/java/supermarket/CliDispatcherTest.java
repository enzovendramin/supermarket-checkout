package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import supermarket.cli.CommandContext;
import supermarket.cli.CommandDispatcher;
import supermarket.register.Supermarket;

class CliDispatcherTest {

    private Supermarket market;
    private CommandDispatcher dispatcher;
    private ByteArrayOutputStream buffer;

    @BeforeEach
    void setUp() {
        market = new Supermarket();
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandContext context = new CommandContext(market);
        dispatcher = new CommandDispatcher(context, out);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void runsTestScenario1EndToEnd() {
        dispatcher.execute("runTest testScenario1.txt");

        // Revenue = two sales (€30.74 + €40.00) + Alice's prime annual fee (€50.00).
        assertEquals(120.74, market.getRevenue(), 1e-9);
        // Steak fully sold out and flagged as low stock (R9 condition; the alert
        // itself is asserted in InventoryObserverTest via a recording observer).
        assertEquals(0, market.getInventory().getStock(market.getItem("steak")));
        assertTrue(market.getInventory().isLow(market.getItem("steak")));
        // The scenario reached the manager's final inspection.
        assertTrue(output().contains("Total revenue: €120.74"));
    }

    @Test
    void runsTestScenario2EndToEnd() {
        dispatcher.execute("runTest testScenario2.txt");

        // Two platinum sales (€6.916 + €47.32) + Dora's platinum annual fee (€200) = €254.236.
        assertEquals(254.236, market.getRevenue(), 1e-9);
        // Beef sold down to 2 then restocked by 20 -> 22.
        assertEquals(22, market.getInventory().getStock(market.getItem("beef")));
        // Unhappy payment paths and the >50 kg delivery refusal were reported.
        assertTrue(output().contains("Payment refused: PIN_WRONG"));
        assertTrue(output().contains("Payment refused: AUTH_DENIED"));
        assertTrue(output().contains("Home delivery refused"));
    }

    @Test
    void runsTestScenario3EndToEnd() {
        dispatcher.execute("runTest testScenario3.txt");

        // Bulk rice sale: 10 x €1.50 = €15.00 (no subscription, no delivery).
        assertEquals(15.0, market.getRevenue(), 1e-9);
        // R10: third booking of slot 08:00-10:00 is refused (capacity 2).
        assertTrue(output().contains("fully booked"));
        // R10 dynamic pricing: peak x1.5 -> €22.50; eco x0.8 -> €12.00.
        assertTrue(output().contains("€22.50"));
        assertTrue(output().contains("€12.00"));
    }

    @Test
    void tokenizerKeepsQuotedAddressTogether() {
        dispatcher.execute("login ceo 123456789");
        dispatcher.execute("registerCustomer Alice Martin alice \"12 rue de la Paix\" pwd");
        assertEquals("12 rue de la Paix", market.requireCustomer("alice").getAddress());
    }

    @Test
    void reportsPermissionDenied() {
        dispatcher.execute("login ceo 123456789");
        dispatcher.execute("registerCustomer Alice Martin alice addr pwd");
        dispatcher.execute("logout");
        dispatcher.execute("login alice pwd");
        dispatcher.execute("addItem milk dairy 1.0 1.0 10"); // customer cannot addItem
        assertTrue(output().contains("Permission denied"));
    }

    @Test
    void reportsUnknownItemOnScan() {
        dispatcher.execute("login ceo 123456789");
        dispatcher.execute("registerCashier Bob Dupont bob bobpwd");
        dispatcher.execute("registerCustomer Alice Martin alice addr pwd");
        dispatcher.execute("logout");
        dispatcher.execute("login bob bobpwd");
        dispatcher.execute("startCheckout alice");
        dispatcher.execute("scanItem ghostitem 1");
        assertTrue(output().contains("Unknown item: ghostitem"));
    }

    @Test
    void reportsSyntaxErrorOnBadArgs() {
        dispatcher.execute("login ceo"); // missing password
        assertTrue(output().contains("Usage: login"));
    }
}
