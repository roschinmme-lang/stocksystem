package stockmasterpro;

import java.io.*;
import java.util.*;

/**
 * Handles all file I/O for inventory, history, and ID registry.
 * No business logic here — only reading and writing data files.
 *
 * File format: pipe-delimited (|) to support commas in product names.
 */
public class InventoryRepository {

    private static final String INV_FILE    = "inventory_data.txt";
    private static final String HIST_FILE   = "history_log.txt";
    private static final String ID_MAP_FILE = "id_registry.txt";

    // ── LOAD ─────────────────────────────────────────────────

    public List<Product> loadInventory() {
        List<Product> list = new ArrayList<>();
        File f = new File(INV_FILE);
        if (!f.exists()) return list;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 4) {
                    list.add(new Product(
                        Integer.parseInt(p[0].trim()),
                        p[1].trim(),
                        Integer.parseInt(p[2].trim()),
                        Long.parseLong(p[3].trim())
                    ));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public Map<String, Integer> loadIdMap() {
        Map<String, Integer> map = new HashMap<>();
        File f = new File(ID_MAP_FILE);
        if (!f.exists()) return map;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 2) map.put(p[0].trim(), Integer.parseInt(p[1].trim()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    public List<HistoryRecord> loadHistory() {
        List<HistoryRecord> list = new ArrayList<>();
        File f = new File(HIST_FILE);
        if (!f.exists()) return list;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 5) {
                    list.add(new HistoryRecord(
                        Integer.parseInt(p[0].trim()),
                        p[1].trim(),
                        p[2].trim(),
                        Integer.parseInt(p[3].trim()),
                        Long.parseLong(p[4].trim())
                    ));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // ── SAVE ─────────────────────────────────────────────────

    public void saveInventory(List<Product> list) {
        try (PrintWriter w = new PrintWriter(new FileWriter(INV_FILE))) {
            for (Product p : list)
                w.println(p.getId() + "|" + p.getName() + "|"
                        + p.getQuantity() + "|" + p.getDateAdded());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveIdMap(Map<String, Integer> map) {
        try (PrintWriter w = new PrintWriter(new FileWriter(ID_MAP_FILE))) {
            for (Map.Entry<String, Integer> e : map.entrySet())
                w.println(e.getKey() + "|" + e.getValue());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void appendHistory(HistoryRecord h) {
        try (PrintWriter w = new PrintWriter(new FileWriter(HIST_FILE, true))) {
            w.println(h.getId() + "|" + h.getAction() + "|"
                    + h.getName() + "|" + h.getChange() + "|" + h.getTime());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
