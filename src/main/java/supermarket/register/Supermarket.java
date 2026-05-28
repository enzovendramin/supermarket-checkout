package supermarket.register;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import supermarket.delivery.DeliveryCalculator;
import supermarket.delivery.DeliveryRequest;
import supermarket.delivery.StandardDeliveryCalculator;
import supermarket.discount.DiscountPlan;
import supermarket.discount.DiscountPlanFactory;
import supermarket.inventory.Inventory;
import supermarket.inventory.ManagerNotifier;
import supermarket.inventory.SupplierNotifier;
import supermarket.model.BankCard;
import supermarket.model.Category;
import supermarket.model.Customer;
import supermarket.model.Item;
import supermarket.pricing.CategoryDiscount;
import supermarket.users.Cashier;
import supermarket.users.Manager;
import supermarket.users.User;
import supermarket.payment.POSDevice;
import supermarket.payment.TransactionAuthorisationSystem;

/**
 * Core of the Supermarket checkout system: owns the catalogue, the item
 * categories, the registered users, the live inventory, the bank's TAS and the
 * cash register (with its POS). Provides the operations exposed by the CLUI.
 */
public class Supermarket {
    /** Stub distance until a real geocoding service is plugged in; keeps scenarios deterministic. */
    public static final double DEFAULT_DISTANCE_KM = 10.0;

    private final Map<String, Category> categories = new HashMap<>();
    private final Map<String, Item> catalogue = new HashMap<>();
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, DeliveryRequest> pendingDeliveries = new HashMap<>();

    private final Inventory inventory = new Inventory();
    private final TransactionAuthorisationSystem tas = new TransactionAuthorisationSystem();
    private final POSDevice pos = new POSDevice(tas);
    private final DeliveryCalculator deliveryCalculator = new StandardDeliveryCalculator();
    private final CashRegister cashRegister = new CashRegister(inventory, pos, deliveryCalculator);

    private int cardCounter = 0;

    public Supermarket() {
        // R9: wire the Observer notifiers to the inventory.
        inventory.addObserver(new ManagerNotifier());
        inventory.addObserver(new SupplierNotifier());

        // The CEO manager is assumed to exist (spec 3.1).
        users.put("ceo", new Manager("Super", "Visor", "ceo", "123456789"));
    }

    /**
     * Loads the default configuration (setup command): the three mandatory item
     * categories (R2b), a default cashier and customer, and a pre-registered
     * test bank card. Idempotent so it can be re-run safely.
     */
    public void setup() {
        getOrCreateCategory("fruit-and-vegetables");
        getOrCreateCategory("dairy");
        getOrCreateCategory("meat");

        if (getUser("cashier") == null) {
            registerCashier("Default", "Cashier", "cashier", "cashier");
        }
        if (getUser("customer") == null) {
            registerCustomer("Default", "Customer", "customer", "1 Main Street", "customer");
        }
        if (!tas.isRegistered("4242424242424242")) {
            tas.registerCard(new BankCard("4242424242424242", "1234", 1_000.0));
        }
    }

    // ---- Catalogue & categories ----

    /** Returns the category, creating it on the fly if unknown (R6b). */
    public Category getOrCreateCategory(String name) {
        return categories.computeIfAbsent(name, Category::new);
    }

    public Category getCategory(String name) {
        return categories.get(name);
    }

    /** Adds a sellable item to the catalogue and the inventory (addItem command). */
    public Item addItem(String name, String categoryName, double unitPrice,
                        double weightKg, int initialStock) {
        Category category = getOrCreateCategory(categoryName);
        Item item = new Item(name, category, unitPrice, weightKg);
        catalogue.put(name, item);
        inventory.addItem(item, initialStock);
        return item;
    }

    public Item getItem(String name) {
        return catalogue.get(name);
    }

    public Collection<Item> getCatalogueItems() {
        return catalogue.values();
    }

    public Item requireItem(String name) {
        Item item = catalogue.get(name);
        if (item == null) {
            throw new IllegalArgumentException("Unknown item: " + name);
        }
        return item;
    }

    public void restock(String itemName, int quantity) {
        inventory.restock(requireItem(itemName), quantity);
    }

    /** Applies a category-level pricing policy (R6). */
    public void setCategoryDiscount(String categoryName, double percent) {
        Category category = categories.get(categoryName);
        if (category == null) {
            throw new IllegalArgumentException("Unknown category: " + categoryName);
        }
        category.setPricingPolicy(new CategoryDiscount(percent));
    }

    // ---- Users ----

    public void registerCashier(String firstName, String lastName, String username, String password) {
        users.put(username, new Cashier(firstName, lastName, username, password));
    }

    public Customer registerCustomer(String firstName, String lastName, String username,
                                     String address, String password) {
        Customer customer = new Customer(firstName, lastName, username, address, password);
        // Each new customer gets an auto-generated, pre-funded test bank card.
        BankCard card = new BankCard(generateCardNumber(), "0000", 1_000.0);
        customer.setBankCard(card);
        tas.registerCard(card);
        users.put(username, customer);
        return customer;
    }

    public void subscribeToPlan(Customer customer, String planName) {
        DiscountPlan plan = DiscountPlanFactory.create(planName);
        customer.setDiscountPlan(plan);
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public Customer requireCustomer(String username) {
        User user = users.get(username);
        if (!(user instanceof Customer customer)) {
            throw new IllegalArgumentException("Unknown customer: " + username);
        }
        return customer;
    }

    // ---- Delivery & checkout ----

    /**
     * Registers a pending home-delivery request for a customer (R7). The request
     * is applied to the cart of the next checkout opened for that customer.
     */
    public void requestDelivery(String customerUsername, String address) {
        requireCustomer(customerUsername); // validates the customer exists
        pendingDeliveries.put(customerUsername, new DeliveryRequest(address, DEFAULT_DISTANCE_KM));
    }

    /**
     * Opens a checkout for the given customer on the cash register, consuming any
     * pending delivery request so the delivery fee is included in the bill.
     */
    public void openCheckout(String customerUsername) {
        Customer customer = requireCustomer(customerUsername);
        DeliveryRequest delivery = pendingDeliveries.remove(customerUsername);
        cashRegister.startCheckout(customer, delivery);
    }

    // ---- Components ----

    public Inventory getInventory() { return inventory; }
    public TransactionAuthorisationSystem getTas() { return tas; }
    public CashRegister getCashRegister() { return cashRegister; }
    public double getRevenue() { return cashRegister.getTotalRevenue(); }

    private String generateCardNumber() {
        return String.format("%016d", ++cardCounter);
    }
}
