package stockmasterpro;

/**
 * Data model representing a single audit-log entry.
 * Actions: NEW, ADD, SUB, DEL
 */
public class HistoryRecord {
    private final int id;
    private final String action;
    private final String name;
    private final int change;
    private final long time;

    public HistoryRecord(int id, String action, String name, int change, long time) {
        this.id     = id;
        this.action = action;
        this.name   = name;
        this.change = change;
        this.time   = time;
    }

    public int    getId()        { return id; }
    public String getAction()    { return action; }
    public String getName()      { return name; }
    public int    getChange()    { return change; }
    public long   getTime()      { return time; }
}
