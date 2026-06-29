package stockmasterpro;

/**
 * Data model representing a single inventory product.
 */
public class Product {
    private final int id;
    private String name;
    private int quantity;
    private final long dateAdded;

    public Product(int id, String name, int quantity, long dateAdded) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.dateAdded = dateAdded;
    }

    public int    getId()          { return id; }
    public String getName()        { return name; }
    public int    getQuantity()    { return quantity; }
    public long   getDateAdded()   { return dateAdded; }

    public void setName(String n)  { this.name = n; }
    public void setQuantity(int q) { this.quantity = q; }
}
