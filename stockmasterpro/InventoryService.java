package stockmasterpro;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * All business logic for inventory management.
 * Returns null on success, or an error message string on failure.
 * Has zero dependency on Swing — fully testable without a UI.
 */
public class InventoryService {

    private static final int BASE_ID             = 1000;
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final InventoryRepository    repo;
    private final List<Product>          inventory;
    private final List<HistoryRecord>    history;
    private final Map<String, Integer>   masterIdMap;

    // Stored SHA-256 hash of "1234".
    // In production: load from a secure config file instead of hardcoding.
    private static final String ADMIN_USER      = "admin";
    private static final String ADMIN_PASS_HASH =
        "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4";

    public InventoryService(InventoryRepository repo) {
        this.repo        = repo;
        this.inventory   = repo.loadInventory();
        this.masterIdMap = repo.loadIdMap();
        this.history     = repo.loadHistory();
    }

    // ── GETTERS ──────────────────────────────────────────────

    public List<Product>       getInventory()        { return inventory; }
    public List<HistoryRecord> getHistory()          { return history; }
    public int                 getLowStockThreshold(){ return LOW_STOCK_THRESHOLD; }

    // ── OPERATIONS ───────────────────────────────────────────

    /** Adds a brand-new product. Returns null on success, error string on failure. */
    public String addNewProduct(String name, int qty) {
        if (qty <= 0)          return "Quantity must be a positive number.";
        if (name.isBlank())    return "Product name cannot be empty.";
        if (findProduct(name) != null)
                               return "Product already exists. Use Stock In to add quantity.";

        int id = masterIdMap.getOrDefault(name, BASE_ID + masterIdMap.size());
        masterIdMap.put(name, id);
        Product p = new Product(id, name, qty, System.currentTimeMillis());
        inventory.add(p);
        logHistory(id, "NEW", name, qty);
        repo.saveInventory(inventory);
        repo.saveIdMap(masterIdMap);
        return null;
    }

    /** Adds quantity to an existing product. Returns null on success, error string on failure. */
    public String stockIn(String name, int qty) {
        if (qty <= 0) return "Quantity must be a positive number.";
        Product p = findProduct(name);
        if (p == null) return "Product not found.";
        p.setQuantity(p.getQuantity() + qty);
        logHistory(p.getId(), "ADD", name, qty);
        repo.saveInventory(inventory);
        return null;
    }

    /** Removes quantity from an existing product. Returns null on success, error string on failure. */
    public String stockOut(String name, int qty) {
        if (qty <= 0) return "Quantity must be a positive number.";
        Product p = findProduct(name);
        if (p == null) return "Product not found.";
        if (p.getQuantity() < qty)
            return "Insufficient stock. Available: " + p.getQuantity();
        p.setQuantity(p.getQuantity() - qty);
        logHistory(p.getId(), "SUB", name, qty);
        repo.saveInventory(inventory);
        return null;
    }

    /** Permanently removes a product and logs the deletion. */
    public void deleteProduct(Product p) {
        logHistory(p.getId(), "DEL", p.getName(), 0);
        inventory.remove(p);
        repo.saveInventory(inventory);
    }

    // ── LOOKUP ───────────────────────────────────────────────

    public Product findProduct(String name) {
        return inventory.stream()
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .findFirst().orElse(null);
    }

    public Product findById(int id) {
        return inventory.stream()
            .filter(p -> p.getId() == id)
            .findFirst().orElse(null);
    }

    // ── AUTH ─────────────────────────────────────────────────

    public boolean authenticate(String user, String pass) {
        return ADMIN_USER.equals(user)
            && ADMIN_PASS_HASH.equals(hashPassword(pass));
    }

    /** Returns the SHA-256 hex digest of a plain-text password. */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ── PRIVATE ──────────────────────────────────────────────

    private void logHistory(int id, String action, String name, int change) {
        HistoryRecord h = new HistoryRecord(id, action, name, change,
            System.currentTimeMillis());
        history.add(h);
        repo.appendHistory(h);
    }
}
