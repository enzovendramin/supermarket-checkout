package supermarket.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {
    private final List<CartEntry> entries = new ArrayList<>();

    public void addEntry(CartEntry entry) { entries.add(entry); }

    public List<CartEntry> getEntries() { return Collections.unmodifiableList(entries); }

    public void clear() { entries.clear(); }

    public double rawTotal() {
        return entries.stream().mapToDouble(CartEntry::subtotal).sum();
    }

    public double totalWeightKg() {
        return entries.stream().mapToDouble(CartEntry::totalWeightKg).sum();
    }
}
