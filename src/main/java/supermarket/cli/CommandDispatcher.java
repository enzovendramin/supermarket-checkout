package supermarket.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import supermarket.delivery.DeliveryNotSupportedException;
import supermarket.delivery.OverbookingException;
import supermarket.inventory.Inventory;
import supermarket.model.Customer;
import supermarket.model.Item;
import supermarket.model.Receipt;
import supermarket.payment.PaymentResult;
import supermarket.register.CashRegister;
import supermarket.register.Supermarket;
import supermarket.users.User;

/**
 * Parses a CLUI line into a command and its (quote-aware) arguments and routes
 * it to the matching handler, enforcing role permissions and reporting all
 * usage/misuse errors without crashing the interpreter.
 */
public class CommandDispatcher {
    private final CommandContext context;
    private final PrintStream out;

    public CommandDispatcher(CommandContext context, PrintStream out) {
        this.context = context;
        this.out = out;
    }

    /** Tokenises a line on whitespace, keeping double-quoted segments together. */
    static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean hasToken = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                hasToken = true;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (hasToken) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    hasToken = false;
                }
            } else {
                current.append(c);
                hasToken = true;
            }
        }
        if (hasToken) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    /** Executes a single CLUI line, reporting errors instead of throwing. */
    public void execute(String rawLine) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        List<String> tokens = tokenize(line);
        String command = tokens.get(0);
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);
        try {
            dispatch(command, args);
        } catch (CommandException | IllegalArgumentException | IllegalStateException
                 | DeliveryNotSupportedException | OverbookingException e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void dispatch(String command, String[] args) {
        switch (command) {
            case "login"               -> login(args);
            case "logout"              -> logout(args);
            case "setup"               -> setup(args);
            case "registerCashier"     -> registerCashier(args);
            case "registerCustomer"    -> registerCustomer(args);
            case "addItem"             -> addItem(args);
            case "restock"             -> restock(args);
            case "setCategoryDiscount" -> setCategoryDiscount(args);
            case "subscribeToPlan"     -> subscribeToPlan(args);
            case "startCheckout"       -> startCheckout(args);
            case "scanItem"            -> scanItem(args);
            case "computeBill"         -> computeBill(args);
            case "requestDelivery"     -> requestDelivery(args);
            case "pay"                 -> pay(args);
            case "simulatePayment"     -> simulatePayment(args);
            case "showInventory"       -> showInventory(args);
            case "showRevenue"         -> showRevenue(args);
            case "runTest"             -> runTest(args);
            case "help"                -> help(args);
            case "exit", "quit"        -> context.stop();
            default -> out.println("Unknown command: " + command + " (type 'help')");
        }
    }

    // ---- Authentication ----

    private void login(String[] args) {
        requireArgs(args, 2, "login <username> <password>");
        User user = context.getMarket().getUser(args[0]);
        if (user == null || !user.checkPassword(args[1])) {
            throw new CommandException("Invalid credentials.");
        }
        context.setCurrentUser(user);
        out.printf("Logged in as %s (%s).%n", user.getUsername(), user.getRole());
    }

    private void logout(String[] args) {
        requireArgs(args, 0, "logout");
        User user = context.requireLogin();
        context.setCurrentUser(null);
        out.printf("Logged out %s.%n", user.getUsername());
    }

    // ---- Manager commands ----

    private void setup(String[] args) {
        requireArgs(args, 0, "setup");
        context.requireRole("manager");
        context.getMarket().setup();
        out.println("Default configuration loaded.");
    }

    private void registerCashier(String[] args) {
        requireArgs(args, 4, "registerCashier <firstName> <lastName> <username> <password>");
        context.requireRole("manager");
        context.getMarket().registerCashier(args[0], args[1], args[2], args[3]);
        out.printf("Cashier '%s' registered.%n", args[2]);
    }

    private void registerCustomer(String[] args) {
        requireArgs(args, 5, "registerCustomer <firstName> <lastName> <username> <address> <password>");
        context.requireRole("manager");
        context.getMarket().registerCustomer(args[0], args[1], args[2], args[3], args[4]);
        out.printf("Customer '%s' registered.%n", args[2]);
    }

    private void addItem(String[] args) {
        requireArgs(args, 5, "addItem <itemName> <categoryName> <unitPrice> <weight> <initialStock>");
        context.requireRole("manager");
        double price = parseDouble(args[2], "unitPrice");
        double weight = parseDouble(args[3], "weight");
        int stock = parseInt(args[4], "initialStock");
        context.getMarket().addItem(args[0], args[1], price, weight, stock);
        out.printf("Item '%s' added to category '%s' (stock %d).%n", args[0], args[1], stock);
    }

    private void restock(String[] args) {
        requireArgs(args, 2, "restock <itemName> <quantity>");
        context.requireRole("manager");
        int qty = parseInt(args[1], "quantity");
        context.getMarket().restock(args[0], qty);
        out.printf("Restocked '%s' by %d.%n", args[0], qty);
    }

    private void setCategoryDiscount(String[] args) {
        requireArgs(args, 2, "setCategoryDiscount <categoryName> <discountPercent>");
        context.requireRole("manager");
        double percent = parseDouble(args[1], "discountPercent");
        context.getMarket().setCategoryDiscount(args[0], percent);
        out.printf(Locale.US, "Applied %.1f%% discount to category '%s'.%n", percent, args[0]);
    }

    private void showInventory(String[] args) {
        requireArgs(args, 0, "showInventory");
        context.requireRole("manager");
        Supermarket market = context.getMarket();
        Inventory inventory = market.getInventory();
        out.println("Inventory:");
        market.getCatalogueItems().stream()
            .sorted(Comparator.comparing(Item::getName))
            .forEach(item -> out.printf("  %-12s %-22s stock=%-4d%s%n",
                item.getName(), item.getCategory().getName(),
                inventory.getStock(item), inventory.isLow(item) ? " [LOW STOCK]" : ""));
    }

    private void showRevenue(String[] args) {
        requireArgs(args, 0, "showRevenue");
        context.requireRole("manager");
        out.printf(Locale.US, "Total revenue: €%.2f%n", context.getMarket().getRevenue());
    }

    // ---- Customer commands ----

    private void subscribeToPlan(String[] args) {
        requireArgs(args, 1, "subscribeToPlan <planName>");
        User user = context.requireRole("customer");
        context.getMarket().subscribeToPlan((Customer) user, args[0]);
        out.printf("Subscribed to the '%s' plan.%n", args[0]);
    }

    private void requestDelivery(String[] args) {
        requireArgs(args, 1, "requestDelivery <address>");
        User user = context.requireRole("customer");
        context.getMarket().requestDelivery(user.getUsername(), args[0]);
        out.printf("Home delivery requested to '%s' for the next purchase.%n", args[0]);
    }

    // ---- Cashier commands ----

    private void startCheckout(String[] args) {
        requireArgs(args, 1, "startCheckout <customerUsername>");
        context.requireRole("cashier");
        context.getMarket().openCheckout(args[0]);
        out.printf("Checkout opened for customer '%s'.%n", args[0]);
    }

    private void scanItem(String[] args) {
        requireArgs(args, 2, "scanItem <itemName> <quantity>");
        context.requireRole("cashier");
        int qty = parseInt(args[1], "quantity");
        Item item = context.getMarket().requireItem(args[0]);
        context.getMarket().getCashRegister().scanItem(item, qty);
        out.printf(Locale.US, "Scanned %d x %s @ €%.2f.%n", qty, item.getName(), item.getUnitPrice());
    }

    private void computeBill(String[] args) {
        requireArgs(args, 0, "computeBill");
        context.requireRole("cashier");
        Receipt receipt = context.getMarket().getCashRegister().computeBill();
        out.println(receipt);
    }

    private void pay(String[] args) {
        requireArgs(args, 2, "pay <cardNumber> <pin>");
        context.requireRole("cashier");
        CashRegister register = context.getMarket().getCashRegister();
        PaymentResult result = register.pay(args[0], args[1]);
        if (result == PaymentResult.SUCCESS) {
            out.println("Payment approved.");
            out.println(register.getLastReceipt());
        } else {
            out.println("Payment refused: " + result + ".");
        }
    }

    private void simulatePayment(String[] args) {
        requireArgs(args, 1, "simulatePayment <SUCCESS|INSUFFICIENT_FUNDS|PIN_WRONG|AUTH_DENIED>");
        context.requireRole("cashier");
        PaymentResult outcome;
        try {
            outcome = PaymentResult.valueOf(args[0]);
        } catch (IllegalArgumentException e) {
            throw new CommandException("Unknown payment outcome: " + args[0]);
        }
        context.getMarket().getCashRegister().simulatePayment(outcome);
        out.printf("Next payment forced to %s.%n", outcome);
    }

    // ---- Generic commands ----

    private void runTest(String[] args) {
        requireArgs(args, 1, "runTest <testScenario-file>");
        Path path = Path.of(args[0]);
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new CommandException("Cannot read test file: " + args[0]);
        }
        out.println("--- Running " + args[0] + " ---");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            out.println("> " + trimmed);
            execute(trimmed);
        }
        out.println("--- Finished " + args[0] + " ---");
    }

    private void help(String[] args) {
        out.println("Available commands:");
        out.println("  login <username> <password>");
        out.println("  logout");
        out.println("  setup                                                     (manager)");
        out.println("  registerCashier <first> <last> <username> <password>      (manager)");
        out.println("  registerCustomer <first> <last> <username> <address> <pw> (manager)");
        out.println("  addItem <name> <category> <unitPrice> <weight> <stock>    (manager)");
        out.println("  restock <itemName> <quantity>                            (manager)");
        out.println("  setCategoryDiscount <categoryName> <percent>             (manager)");
        out.println("  showInventory                                            (manager)");
        out.println("  showRevenue                                              (manager)");
        out.println("  subscribeToPlan <planName>                               (customer)");
        out.println("  requestDelivery <address>                                (customer)");
        out.println("  startCheckout <customerUsername>                         (cashier)");
        out.println("  scanItem <itemName> <quantity>                           (cashier)");
        out.println("  computeBill                                              (cashier)");
        out.println("  pay <cardNumber> <pin>                                   (cashier)");
        out.println("  simulatePayment <outcome>                                (cashier)");
        out.println("  runTest <file>");
        out.println("  help");
        out.println("  exit");
    }

    // ---- Helpers ----

    private void requireArgs(String[] args, int expected, String usage) {
        if (args.length != expected) {
            throw new CommandException("Usage: " + usage);
        }
    }

    private double parseDouble(String value, String field) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid number for " + field + ": " + value);
        }
    }

    private int parseInt(String value, String field) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid integer for " + field + ": " + value);
        }
    }
}
