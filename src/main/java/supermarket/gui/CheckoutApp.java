package supermarket.gui;

import java.util.Locale;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import supermarket.model.CartEntry;
import supermarket.model.Item;
import supermarket.model.Receipt;
import supermarket.payment.PaymentResult;
import supermarket.register.CashRegister;
import supermarket.register.Supermarket;

/**
 * JavaFX desktop front-end for the checkout system. It is purely a presentation
 * layer: every action delegates to the same {@link Supermarket} domain core used
 * by the CLUI. Demonstrates the Command pattern (undo/redo) and the Observer
 * pattern (a live low-stock highlight fed by a {@code StockObserver}).
 */
public class CheckoutApp extends Application {

    private Supermarket market;
    private CashRegister register;

    private final ObservableList<Item> catalogueItems = FXCollections.observableArrayList();
    private final ObservableList<CartEntry> cartItems = FXCollections.observableArrayList();
    private TableView<Item> catalogueTable;

    private final Label subtotalLabel = new Label();
    private final Label promoLabel = new Label();
    private final Label vatLabel = new Label();
    private final Label totalLabel = new Label();
    private final Label revenueLabel = new Label();
    private final Label pointsLabel = new Label();
    private final TextArea log = new TextArea();

    @Override
    public void start(Stage stage) {
        market = DemoData.build();
        register = market.getCashRegister();

        // Observer pattern: a new GUI listener updates the screen on low stock (R9).
        market.getInventory().addObserver((item, stock, threshold) -> Platform.runLater(() -> {
            log("LOW STOCK: " + item.getName() + " - " + stock + " left (threshold " + threshold + ")");
            catalogueTable.refresh();
        }));

        market.openCheckout(DemoData.CUSTOMER);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setTop(buildHeader());
        root.setLeft(buildCataloguePane());
        root.setCenter(buildCartPane());
        root.setRight(buildBillPane());

        refreshAll();

        stage.setScene(new Scene(root, 980, 560));
        stage.setTitle("Supermarket Checkout");
        stage.show();
    }

    // ---- Header ----

    private HBox buildHeader() {
        Label title = new Label("Supermarket Checkout");
        title.setFont(Font.font(20));
        revenueLabel.setFont(Font.font(14));
        HBox header = new HBox(20, title, spacer(), revenueLabel);
        header.setPadding(new Insets(0, 0, 10, 0));
        return header;
    }

    // ---- Catalogue (left) ----

    private VBox buildCataloguePane() {
        catalogueTable = new TableView<>(catalogueItems);
        catalogueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        catalogueTable.getColumns().add(column("Item", i -> i.getName()));
        catalogueTable.getColumns().add(column("Category", i -> i.getCategory().getName()));
        catalogueTable.getColumns().add(column("Price", i -> money(i.getUnitPrice())));
        catalogueTable.getColumns().add(column("Stock",
            i -> Integer.toString(market.getInventory().getStock(i))));

        // Observer-driven low-stock highlight.
        catalogueTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle(market.getInventory().isLow(item) ? "-fx-background-color: #ffd0d0;" : "");
                }
            }
        });

        Spinner<Integer> qty = new Spinner<>(1, 99, 1);
        qty.setEditable(true);
        Button scan = new Button("Scan ->");
        scan.setOnAction(e -> {
            Item item = catalogueTable.getSelectionModel().getSelectedItem();
            if (item == null) {
                log("Select an item to scan.");
                return;
            }
            register.scanItem(item, qty.getValue());
            log("Scanned " + qty.getValue() + " x " + item.getName());
            refreshCartAndBill();
        });

        HBox controls = new HBox(8, new Label("Qty:"), qty, scan);
        VBox box = new VBox(8, new Label("Catalogue"), catalogueTable, controls);
        box.setPadding(new Insets(0, 10, 0, 0));
        box.setPrefWidth(420);
        VBox.setVgrow(catalogueTable, Priority.ALWAYS);
        return box;
    }

    // ---- Cart (center) ----

    private VBox buildCartPane() {
        TableView<CartEntry> cartTable = new TableView<>(cartItems);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cartTable.getColumns().add(column("Item", e -> e.getItem().getName()));
        cartTable.getColumns().add(column("Qty", e -> Integer.toString(e.getQuantity())));
        cartTable.getColumns().add(column("Subtotal", e -> money(e.subtotal())));

        Button undo = new Button("Undo");
        undo.setOnAction(e -> runSafely(() -> log("Undone: " + register.undoLastAction())));
        Button redo = new Button("Redo");
        redo.setOnAction(e -> runSafely(() -> log("Redone: " + register.redoLastAction())));

        HBox controls = new HBox(8, undo, redo);
        VBox box = new VBox(8, new Label("Cart"), cartTable, controls);
        box.setPadding(new Insets(0, 10, 0, 10));
        VBox.setVgrow(cartTable, Priority.ALWAYS);
        return box;
    }

    // ---- Bill + payment (right) ----

    private VBox buildBillPane() {
        ComboBox<String> outcome = new ComboBox<>(FXCollections.observableArrayList(
            "REAL (use card)", "SUCCESS", "INSUFFICIENT_FUNDS", "PIN_WRONG", "AUTH_DENIED"));
        outcome.getSelectionModel().selectFirst();

        Button pay = new Button("Pay");
        pay.setOnAction(e -> pay(outcome.getValue()));

        totalLabel.setFont(Font.font(16));
        log.setEditable(false);
        log.setPrefRowCount(8);

        VBox box = new VBox(6,
            new Label("Bill"),
            subtotalLabel, promoLabel, vatLabel, totalLabel,
            new Label("Simulate outcome:"), outcome, pay,
            pointsLabel,
            new Label("Events:"), log);
        box.setPadding(new Insets(0, 0, 0, 10));
        box.setPrefWidth(300);
        VBox.setVgrow(log, Priority.ALWAYS);
        return box;
    }

    private void pay(String outcome) {
        if (cartItems.isEmpty()) {
            log("Cart is empty.");
            return;
        }
        if (!outcome.startsWith("REAL")) {
            register.simulatePayment(PaymentResult.valueOf(outcome));
        }
        PaymentResult result;
        try {
            result = register.pay(DemoData.CARD, DemoData.PIN);
        } catch (RuntimeException ex) {
            log("Error: " + ex.getMessage());
            return;
        }
        if (result == PaymentResult.SUCCESS) {
            log("Payment approved. Earned " + register.getLastPointsEarned() + " points.");
            market.openCheckout(DemoData.CUSTOMER); // ready for the next sale
            refreshAll();
        } else {
            log("Payment refused: " + result);
        }
    }

    // ---- Refresh helpers ----

    private void refreshAll() {
        catalogueItems.setAll(market.getCatalogueItems());
        catalogueTable.refresh();
        refreshCartAndBill();
        revenueLabel.setText("Revenue: " + money(market.getRevenue()));
        pointsLabel.setText("Customer points: " + market.requireCustomer(DemoData.CUSTOMER).getLoyaltyPoints());
    }

    private void refreshCartAndBill() {
        cartItems.setAll(register.getCart().getEntries());
        Receipt bill = register.computeBill();
        subtotalLabel.setText("Subtotal: " + money(bill.getSubtotalAfterDiscount()));
        promoLabel.setText("Promotions: -" + money(bill.getPromotionDiscount()));
        vatLabel.setText("VAT: " + money(bill.getTax()));
        totalLabel.setText("TOTAL: " + money(bill.getTotal()));
    }

    // ---- Small utilities ----

    private void runSafely(Runnable action) {
        try {
            action.run();
            refreshCartAndBill();
        } catch (RuntimeException ex) {
            log(ex.getMessage());
        }
    }

    private void log(String message) {
        log.appendText(message + "\n");
    }

    private static String money(double amount) {
        return String.format(Locale.US, "€%.2f", amount);
    }

    private static <T> TableColumn<T, String> column(String title, java.util.function.Function<T, String> value) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new ReadOnlyStringWrapper(value.apply(c.getValue())));
        return col;
    }

    private static javafx.scene.Node spacer() {
        javafx.scene.layout.Region region = new javafx.scene.layout.Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
