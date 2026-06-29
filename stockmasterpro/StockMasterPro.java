package stockmasterpro;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Main application window — login screen and inventory dashboard.
 *
 * Layout:
 *   rootLayout (CardLayout)
 *     ├── LOGIN  — login wrapper → login card
 *     └── DASH   — sidebar (CardLayout) | center panel (CardLayout)
 *                     ├── INIT  — nav buttons, search, stats
 *                     └── INPUT — stock in/out/new product form
 */
public class StockMasterPro {

    private static final int LOW_STOCK = 5;

    // ── Services ─────────────────────────────────────────────
    private final InventoryRepository repo    = new InventoryRepository();
    private final InventoryService    service = new InventoryService(repo);

    // ── Frame / root ─────────────────────────────────────────
    private JFrame     frame;
    private CardLayout rootLayout;
    private JPanel     mainContainer;

    // ── Login fields ─────────────────────────────────────────
    private StyledTextField     userField;
    private StyledPasswordField passField;
    private JLabel              loginError;

    // ── Dashboard layout ─────────────────────────────────────
    private JPanel     sidebar,       centerPanel;
    private CardLayout sideLayout,    centerLayout;
    private JPanel     initialPanel,  inputPanel;

    // ── Tables ───────────────────────────────────────────────
    private DefaultTableModel invModel,          histModel;
    private JTable            inventoryTable,    historyTable;
    private JScrollPane       tableScrollPane,   historyScrollPane;
    private TableColumn       dateColumn;
    private boolean           isDateColumnVisible = false;

    // ── Sidebar controls ─────────────────────────────────────
    private StyledTextField   nameInput, qtyInput, searchInput;
    private JComboBox<String> sortDropdown;
    private JLabel            actionTitle, actionSubtitle;
    private JLabel            statTotal,   statLow,  statOk;

    // ── Buttons ──────────────────────────────────────────────
    private PillButton viewStockBtn, newEntryBtn,   stockInBtn,  stockOutBtn,
                       deleteBtn,    historyBtn,     themeToggleBtn, logoutBtn,
                       confirmBtn,   cancelBtn;

    // ── State ────────────────────────────────────────────────
    private boolean isDarkMode  = true;
    private int     currentMode = 0; // 0 = stockIn, 1 = stockOut, 2 = newProduct

    // ─────────────────────────────────────────────────────────

    public StockMasterPro() {
        setupUI();
    }

    // ═══════════════════════════════════════════════════════════
    //  BOOTSTRAP
    // ═══════════════════════════════════════════════════════════

    private void setupUI() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        frame = new JFrame("StockMaster Pro");
        frame.setSize(1260, 820);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        rootLayout    = new CardLayout();
        mainContainer = new JPanel(rootLayout);

        mainContainer.add(buildLoginWrapper(),    "LOGIN");
        mainContainer.add(buildDashboardPanel(),  "DASH");

        frame.add(mainContainer);
        frame.setLocationRelativeTo(null);
        rootLayout.show(mainContainer, "LOGIN");
        frame.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════
    //  LOGIN
    // ═══════════════════════════════════════════════════════════

    private JPanel buildLoginWrapper() {
        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, DS.D_BG, getWidth(), getHeight(), new Color(20, 28, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE;
        wrapper.add(buildLoginCard(), gbc);
        return wrapper;
    }

    private JPanel buildLoginCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DS.D_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DS.RADIUS_LG, DS.RADIUS_LG);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(400, 480));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        // Icon
        JLabel icon = new JLabel("📦", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 40));
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 8, 0);
        card.add(icon, gbc);

        // Title
        JLabel title = new JLabel("StockMaster Pro", SwingConstants.CENTER);
        title.setFont(DS.FONT_TITLE); title.setForeground(DS.D_PRIMARY);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 4, 0);
        card.add(title, gbc);

        JLabel sub = new JLabel("Inventory Management System", SwingConstants.CENTER);
        sub.setFont(DS.FONT_SMALL); sub.setForeground(DS.D_SUBTEXT);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 30, 0);
        card.add(sub, gbc);

        // Username
        JLabel userLbl = new JLabel("USERNAME");
        userLbl.setFont(DS.FONT_LABEL); userLbl.setForeground(DS.D_SUBTEXT);
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 6, 0);
        card.add(userLbl, gbc);

        userField = new StyledTextField("Enter username", DS.D_BORDER, DS.D_PRIMARY);
        userField.setBackground(DS.D_CARD); userField.setForeground(DS.D_TEXT);
        userField.setPreferredSize(new Dimension(0, 44));
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 16, 0);
        card.add(userField, gbc);

        // Password
        JLabel passLbl = new JLabel("PASSWORD");
        passLbl.setFont(DS.FONT_LABEL); passLbl.setForeground(DS.D_SUBTEXT);
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 6, 0);
        card.add(passLbl, gbc);

        passField = new StyledPasswordField(DS.D_BORDER, DS.D_PRIMARY);
        passField.setBackground(DS.D_CARD); passField.setForeground(DS.D_TEXT);
        passField.setPreferredSize(new Dimension(0, 44));
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 10, 0);
        card.add(passField, gbc);

        // Error label
        loginError = new JLabel(" ");
        loginError.setFont(DS.FONT_SMALL); loginError.setForeground(DS.D_DANGER);
        loginError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 10, 0);
        card.add(loginError, gbc);

        // Sign in button
        PillButton loginBtn = new PillButton("SIGN IN",
            DS.D_PRIMARY, DS.D_PRIMARY.brighter(), Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(0, 46));
        loginBtn.setFont(DS.FONT_HEAD);
        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 0, 0);
        card.add(loginBtn, gbc);

        // Events
        loginBtn.addActionListener(e -> attemptLogin());
        userField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        || e.getKeyCode() == KeyEvent.VK_DOWN) passField.requestFocus();
            }
        });
        passField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
                if (e.getKeyCode() == KeyEvent.VK_UP)    userField.requestFocus();
            }
        });

        return card;
    }

    private void attemptLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        if (service.authenticate(user, pass)) {
            loginError.setText(" ");
            userField.setText(""); passField.setText("");
            applyTheme();
            refreshStats();
            refreshInventory("");
            rootLayout.show(mainContainer, "DASH");
            searchInput.requestFocus();
        } else {
            loginError.setText("Invalid username or password.");
            passField.setText("");
            passField.requestFocus();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  DASHBOARD
    // ═══════════════════════════════════════════════════════════

    private JPanel buildDashboardPanel() {
        JPanel dash = new JPanel(new BorderLayout(0, 0));
        dash.setOpaque(false);

        sidebar     = new JPanel(sideLayout = new CardLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));

        centerPanel = new JPanel(centerLayout = new CardLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        setupTables();
        buildSidebar();

        centerPanel.add(tableScrollPane,   "INVENTORY");
        centerPanel.add(historyScrollPane, "HISTORY");

        dash.add(sidebar,     BorderLayout.WEST);
        dash.add(centerPanel, BorderLayout.CENTER);
        return dash;
    }

    // ── Tables ───────────────────────────────────────────────

    private void setupTables() {
        // Inventory table
        invModel = new DefaultTableModel(
                new String[]{"ID", "PRODUCT NAME", "QTY", "STATUS", "DATE ADDED"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = new JTable(invModel);
        styleTable(inventoryTable);

        dateColumn = inventoryTable.getColumnModel().getColumn(4);
        inventoryTable.removeColumn(dateColumn);

        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(240);
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(80);

        inventoryTable.getColumnModel().getColumn(3).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                            t, v, sel, foc, row, col);
                    String val = v == null ? "" : v.toString();
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(DS.FONT_LABEL);
                    if ("LOW".equals(val)) {
                        lbl.setForeground(DS.D_DANGER);
                        lbl.setText("⚠ LOW");
                    } else {
                        lbl.setForeground(DS.D_SUCCESS);
                        lbl.setText("✓ OK");
                    }
                    return lbl;
                }
            });

        tableScrollPane = new JScrollPane(inventoryTable);
        styleScrollPane(tableScrollPane);

        inventoryTable.setFocusable(true);
        inventoryTable.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) searchInput.requestFocus();
            }
        });

        // History table
        histModel = new DefaultTableModel(
                new String[]{"ID", "ACTION", "PRODUCT NAME", "QTY CHANGE", "TIME"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(histModel);
        styleTable(historyTable);

        historyTable.getColumnModel().getColumn(1).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                            t, v, sel, foc, row, col);
                    lbl.setFont(DS.FONT_LABEL);
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    String val = v == null ? "" : v.toString();
                    switch (val) {
                        case "NEW": lbl.setForeground(DS.D_PRIMARY);  break;
                        case "ADD": lbl.setForeground(DS.D_SUCCESS);  break;
                        case "SUB": lbl.setForeground(DS.D_WARNING);  break;
                        case "DEL": lbl.setForeground(DS.D_DANGER);   break;
                        default:    lbl.setForeground(DS.D_TEXT);
                    }
                    return lbl;
                }
            });

        historyScrollPane = new JScrollPane(historyTable);
        styleScrollPane(historyScrollPane);
    }

    private void styleTable(JTable t) {
        t.setRowHeight(44);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFont(DS.FONT_BODY);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setOpaque(true);
        JTableHeader header = t.getTableHeader();
        header.setFont(DS.FONT_LABEL);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setPreferredSize(new Dimension(0, 40));
    }

    private void styleScrollPane(JScrollPane sp) {
        sp.setBorder(new RoundedBorder(DS.D_BORDER, DS.RADIUS_LG));
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
    }

    // ── Sidebar ──────────────────────────────────────────────

    private void buildSidebar() {
        buildInitialPanel();
        buildInputPanel();
        sidebar.add(initialPanel, "INIT");
        sidebar.add(inputPanel,   "INPUT");
    }

    private void buildInitialPanel() {
        initialPanel = new JPanel(new GridBagLayout());
        initialPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        // Header
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(24, 20, 16, 20));
        JLabel logo = new JLabel("📦 StockMaster"); logo.setFont(DS.FONT_HEAD);
        JLabel ver  = new JLabel("Pro v10");        ver.setFont(DS.FONT_SMALL);
        header.add(logo, BorderLayout.WEST);
        header.add(ver,  BorderLayout.EAST);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 0, 0);
        initialPanel.add(header, gbc);

        // Stats bar
        JPanel stats = new JPanel(new GridLayout(1, 3, 8, 0));
        stats.setOpaque(false);
        stats.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        statTotal = makeStat("0", "Total");
        statLow   = makeStat("0", "Low Stock");
        statOk    = makeStat("0", "OK");
        stats.add(wrapStat(statTotal, "Total",     DS.D_PRIMARY));
        stats.add(wrapStat(statLow,   "Low Stock", DS.D_DANGER));
        stats.add(wrapStat(statOk,    "OK",        DS.D_SUCCESS));
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 16, 0);
        initialPanel.add(stats, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 16, 12, 16);
        initialPanel.add(makeDivider(), gbc);

        // Sort
        initialPanel.add(makeSectionLabel("SORT & FILTER"),
            at(gbc, 3, 0, 20, 6, 20));

        sortDropdown = new JComboBox<>(new String[]{
            "Name (A–Z)", "Low Stock First", "High Stock First",
            "Newest First", "Oldest First"
        });
        sortDropdown.setFont(DS.FONT_BODY);
        sortDropdown.addActionListener(
            e -> { showInv(); refreshInventory(searchInput.getText().toUpperCase()); });
        gbc.gridy = 4; gbc.insets = new Insets(0, 16, 10, 16);
        initialPanel.add(sortDropdown, gbc);

        // Search
        searchInput = new StyledTextField("Search by name or ID…", DS.D_BORDER, DS.D_PRIMARY);
        searchInput.setPreferredSize(new Dimension(0, 42));
        searchInput.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { onSearch(); }
            public void removeUpdate(DocumentEvent e)  { onSearch(); }
            public void changedUpdate(DocumentEvent e) { onSearch(); }
            private void onSearch() {
                showInv();
                refreshInventory(searchInput.getText().toUpperCase());
            }
        });
        searchInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT && inventoryTable.getRowCount() > 0) {
                    inventoryTable.requestFocus();
                    inventoryTable.setRowSelectionInterval(0, 0);
                }
            }
        });
        gbc.gridy = 5; gbc.insets = new Insets(0, 16, 16, 16);
        initialPanel.add(searchInput, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(0, 16, 12, 16);
        initialPanel.add(makeDivider(), gbc);

        // Action buttons
        initialPanel.add(makeSectionLabel("INVENTORY ACTIONS"),
            at(gbc, 7, 0, 20, 8, 20));

        viewStockBtn = new PillButton("📋  View All Stock", DS.D_CARD, DS.D_BORDER, DS.D_TEXT);
        newEntryBtn  = new PillButton("＋  New Product",    DS.D_PRIMARY, DS.D_PRIMARY.brighter(), Color.WHITE);
        stockInBtn   = new PillButton("▲  Stock In (+)",    new Color(34, 100, 60),  DS.D_SUCCESS, DS.D_SUCCESS);
        stockOutBtn  = new PillButton("▼  Stock Out (−)",   new Color(120, 80, 0),   DS.D_WARNING, DS.D_WARNING);
        deleteBtn    = new PillButton("✕  Delete Selected", new Color(100, 28, 28),  DS.D_DANGER,  DS.D_DANGER);

        PillButton[] primary = {viewStockBtn, newEntryBtn, stockInBtn, stockOutBtn, deleteBtn};
        for (int i = 0; i < primary.length; i++) {
            primary[i].setPreferredSize(new Dimension(0, 42));
            gbc.gridy = 8 + i; gbc.insets = new Insets(0, 16, 6, 16);
            initialPanel.add(primary[i], gbc);
        }

        gbc.gridy = 13; gbc.insets = new Insets(8, 16, 8, 16);
        initialPanel.add(makeDivider(), gbc);

        historyBtn     = new PillButton("🕓  View History", DS.D_CARD, DS.D_BORDER, DS.D_SUBTEXT);
        themeToggleBtn = new PillButton("🌙  Dark Mode",    DS.D_CARD, DS.D_BORDER, DS.D_SUBTEXT);
        logoutBtn      = new PillButton("⎋   Logout",       DS.D_CARD, new Color(80, 28, 28), DS.D_DANGER);

        PillButton[] secondary = {historyBtn, themeToggleBtn, logoutBtn};
        for (int i = 0; i < secondary.length; i++) {
            secondary[i].setPreferredSize(new Dimension(0, 40));
            gbc.gridy = 14 + i; gbc.insets = new Insets(0, 16, 5, 16);
            initialPanel.add(secondary[i], gbc);
        }

        // Spacer
        JPanel spacer = new JPanel(); spacer.setOpaque(false);
        gbc.gridy = 17; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        initialPanel.add(spacer, gbc);

        wireInitialPanelHandlers();
    }

    private void wireInitialPanelHandlers() {
        viewStockBtn.addActionListener(e -> {
            searchInput.setText("");
            showInv();
            refreshInventory("");
        });
        newEntryBtn.addActionListener(e -> {
            showInv();
            currentMode = 2;
            actionTitle.setText("New Product");
            actionSubtitle.setText("Add a brand-new item to inventory");
            nameInput.setText(""); qtyInput.setText("");
            nameInput.setEditable(true);
            sideLayout.show(sidebar, "INPUT");
            nameInput.requestFocus();
        });
        stockInBtn.addActionListener(e -> {
            showInv();
            if (inventoryTable.getSelectedRow() == -1) {
                Toast.show(frame, "Please select a product first.", false); return;
            }
            currentMode = 0;
            actionTitle.setText("Stock In");
            actionSubtitle.setText("Add quantity to selected product");
            String sel = inventoryTable.getValueAt(inventoryTable.getSelectedRow(), 1).toString();
            nameInput.setText(sel); nameInput.setEditable(false);
            qtyInput.setText("");
            sideLayout.show(sidebar, "INPUT");
            qtyInput.requestFocus();
        });
        stockOutBtn.addActionListener(e -> {
            showInv();
            if (inventoryTable.getSelectedRow() == -1) {
                Toast.show(frame, "Please select a product first.", false); return;
            }
            currentMode = 1;
            actionTitle.setText("Stock Out");
            actionSubtitle.setText("Remove quantity from selected product");
            String sel = inventoryTable.getValueAt(inventoryTable.getSelectedRow(), 1).toString();
            nameInput.setText(sel); nameInput.setEditable(false);
            qtyInput.setText("");
            sideLayout.show(sidebar, "INPUT");
            qtyInput.requestFocus();
        });
        deleteBtn.addActionListener(e -> { showInv(); processDelete(); });
        historyBtn.addActionListener(e -> showHist());
        themeToggleBtn.addActionListener(e -> toggleTheme());
        logoutBtn.addActionListener(e -> {
            rootLayout.show(mainContainer, "LOGIN");
            userField.setText(""); passField.setText("");
        });

        applyNavKeys(viewStockBtn, newEntryBtn, stockInBtn, stockOutBtn,
            deleteBtn, historyBtn, themeToggleBtn, logoutBtn);
    }

    private void buildInputPanel() {
        inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        actionTitle = new JLabel("Action");
        actionTitle.setFont(DS.FONT_TITLE);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
        inputPanel.add(actionTitle, gbc);

        actionSubtitle = new JLabel("Description");
        actionSubtitle.setFont(DS.FONT_SMALL);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 24, 0);
        inputPanel.add(actionSubtitle, gbc);

        inputPanel.add(makeSectionLabel("PRODUCT NAME"), at(gbc, 2, 0, 0, 6, 0));

        nameInput = new StyledTextField("Product name", DS.D_BORDER, DS.D_PRIMARY);
        nameInput.setPreferredSize(new Dimension(0, 44));
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 16, 0);
        inputPanel.add(nameInput, gbc);

        inputPanel.add(makeSectionLabel("QUANTITY"), at(gbc, 4, 0, 0, 6, 0));

        qtyInput = new StyledTextField("Enter a number", DS.D_BORDER, DS.D_PRIMARY);
        qtyInput.setPreferredSize(new Dimension(0, 44));
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 24, 0);
        inputPanel.add(qtyInput, gbc);

        confirmBtn = new PillButton("CONFIRM", DS.D_PRIMARY, DS.D_PRIMARY.brighter(), Color.WHITE);
        confirmBtn.setPreferredSize(new Dimension(0, 46));
        confirmBtn.setFont(DS.FONT_HEAD);
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 10, 0);
        inputPanel.add(confirmBtn, gbc);

        cancelBtn = new PillButton("CANCEL", DS.D_CARD, DS.D_BORDER, DS.D_SUBTEXT);
        cancelBtn.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 0, 0);
        inputPanel.add(cancelBtn, gbc);

        JPanel spacer = new JPanel(); spacer.setOpaque(false);
        gbc.gridy = 8; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(spacer, gbc);

        confirmBtn.addActionListener(e -> processEntry());
        cancelBtn.addActionListener(e -> {
            sideLayout.show(sidebar, "INIT");
            searchInput.requestFocus();
        });
        qtyInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) processEntry();
                if (e.getKeyCode() == KeyEvent.VK_UP)    nameInput.requestFocus();
            }
        });
        nameInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) qtyInput.requestFocus();
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  BUSINESS LOGIC (UI layer)
    // ═══════════════════════════════════════════════════════════

    private void processEntry() {
        String name    = nameInput.getText().trim().toUpperCase();
        String qtyText = qtyInput.getText().trim();
        int qty;

        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException ex) {
            Toast.show(frame, "Quantity must be a valid number.", false);
            qtyInput.requestFocus();
            return;
        }

        String error;
        if      (currentMode == 2) error = service.addNewProduct(name, qty);
        else if (currentMode == 0) error = service.stockIn(name, qty);
        else                       error = service.stockOut(name, qty);

        if (error != null) {
            Toast.show(frame, error, false);
        } else {
            Toast.show(frame, "Operation successful!", true);
            sideLayout.show(sidebar, "INIT");
            refreshStats();
            refreshInventory(searchInput.getText());
            inventoryTable.repaint();
            searchInput.requestFocus();
        }
    }

    private void processDelete() {
        int row = inventoryTable.getSelectedRow();
        if (row == -1) {
            Toast.show(frame, "Please select a product to delete.", false);
            return;
        }
        int    id   = (int) inventoryTable.getValueAt(row, 0);
        String name = inventoryTable.getValueAt(row, 1).toString();

        int result = JOptionPane.showConfirmDialog(
            frame,
            "Are you sure you want to delete \"" + name + "\"?\nThis action cannot be undone.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION) {
            Product p = service.findById(id);
            if (p != null) {
                service.deleteProduct(p);
                Toast.show(frame, "\"" + name + "\" deleted.", true);
                refreshStats();
                refreshInventory(searchInput.getText());
                inventoryTable.repaint();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  REFRESH
    // ═══════════════════════════════════════════════════════════

    private void refreshInventory(String query) {
        String sort = (String) sortDropdown.getSelectedItem();
        List<Product> list = new ArrayList<>(service.getInventory()); // defensive copy

        if (sort != null) {
            if      (sort.contains("A–Z"))    list.sort(Comparator.comparing(Product::getName));
            else if (sort.contains("Low"))    list.sort(Comparator.comparingInt(Product::getQuantity));
            else if (sort.contains("High"))   list.sort((a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()));
            else if (sort.contains("Newest")) list.sort((a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
            else if (sort.contains("Oldest")) list.sort(Comparator.comparingLong(Product::getDateAdded));
        }

        boolean wantsDate = sort != null
            && (sort.contains("Newest") || sort.contains("Oldest"));
        if (wantsDate && !isDateColumnVisible) {
            inventoryTable.addColumn(dateColumn); isDateColumnVisible = true;
        } else if (!wantsDate && isDateColumnVisible) {
            inventoryTable.removeColumn(dateColumn); isDateColumnVisible = false;
        }

        invModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm");
        String q = query.toUpperCase();
        for (Product p : list) {
            if (q.isEmpty()
                    || p.getName().toUpperCase().contains(q)
                    || String.valueOf(p.getId()).contains(q)) {
                invModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getQuantity(),
                    p.getQuantity() <= LOW_STOCK ? "LOW" : "OK",
                    sdf.format(new Date(p.getDateAdded()))
                });
            }
        }
    }

    private void refreshStats() {
        List<Product> list = service.getInventory();
        long low = list.stream().filter(p -> p.getQuantity() <= LOW_STOCK).count();
        statTotal.setText(String.valueOf(list.size()));
        statLow.setText(String.valueOf(low));
        statOk.setText(String.valueOf(list.size() - low));
    }

    private void showInv()  { centerLayout.show(centerPanel, "INVENTORY"); }

    private void showHist() {
        histModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        List<HistoryRecord> hist = service.getHistory();
        for (int i = hist.size() - 1; i >= 0; i--) {
            HistoryRecord h = hist.get(i);
            histModel.addRow(new Object[]{
                h.getId(), h.getAction(), h.getName(),
                h.getChange(), sdf.format(new Date(h.getTime()))
            });
        }
        centerLayout.show(centerPanel, "HISTORY");
    }

    // ═══════════════════════════════════════════════════════════
    //  THEME
    // ═══════════════════════════════════════════════════════════

    private void applyTheme() {
        Color bg      = isDarkMode ? DS.D_BG      : DS.L_BG;
        Color surface = isDarkMode ? DS.D_SURFACE : DS.L_SURFACE;
        Color card    = isDarkMode ? DS.D_CARD    : DS.L_CARD;
        Color border  = isDarkMode ? DS.D_BORDER  : DS.L_BORDER;
        Color primary = isDarkMode ? DS.D_PRIMARY : DS.L_PRIMARY;
        Color text    = isDarkMode ? DS.D_TEXT    : DS.L_TEXT;
        Color subtext = isDarkMode ? DS.D_SUBTEXT : DS.L_SUBTEXT;

        if (sidebar != null)      sidebar.setBackground(surface);
        if (initialPanel != null) initialPanel.setBackground(surface);
        if (inputPanel != null)   inputPanel.setBackground(surface);
        if (centerPanel != null)  centerPanel.setBackground(bg);
        mainContainer.setBackground(bg);

        applyTableTheme(inventoryTable, tableScrollPane,   bg, text, subtext, primary, surface);
        applyTableTheme(historyTable,   historyScrollPane, bg, text, subtext, primary, surface);

        if (searchInput != null) { searchInput.setBackground(card); searchInput.setForeground(text); searchInput.setColors(border, primary); }
        if (nameInput   != null) { nameInput.setBackground(card);   nameInput.setForeground(text);   nameInput.setColors(border, primary); }
        if (qtyInput    != null) { qtyInput.setBackground(card);    qtyInput.setForeground(text);    qtyInput.setColors(border, primary); }
        if (actionTitle    != null) actionTitle.setForeground(text);
        if (actionSubtitle != null) actionSubtitle.setForeground(subtext);
        if (sortDropdown   != null) { sortDropdown.setBackground(card); sortDropdown.setForeground(text); }

        updateLabelsIn(initialPanel, text, subtext);
        updateLabelsIn(inputPanel,   text, subtext);
        frame.repaint();
    }

    private void applyTableTheme(JTable t, JScrollPane sp,
            Color bg, Color text, Color subtext, Color primary, Color surface) {
        if (t == null || sp == null) return;
        t.setBackground(bg);
        t.setForeground(text);
        t.setSelectionBackground(primary.darker());
        t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setBackground(surface);
        t.getTableHeader().setForeground(subtext);
        sp.getViewport().setBackground(bg);
        sp.setBackground(bg);
    }

    private void updateLabelsIn(JPanel panel, Color text, Color subtext) {
        if (panel == null) return;
        for (Component c : panel.getComponents()) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                lbl.setForeground(lbl.getFont().equals(DS.FONT_LABEL) ? subtext : text);
            }
        }
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        themeToggleBtn.setText(isDarkMode ? "☀  Light Mode" : "🌙  Dark Mode");
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════

    private JLabel makeStat(String val, String label) {
        JLabel l = new JLabel(val, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 20));
        return l;
    }

    private JPanel wrapStat(JLabel valLbl, String labelText, Color accent) {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DS.RADIUS, DS.RADIUS);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, DS.RADIUS, DS.RADIUS);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        valLbl.setForeground(accent);
        p.add(valLbl, gbc);

        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(DS.FONT_SMALL); lbl.setForeground(DS.D_SUBTEXT);
        gbc.gridy = 1;
        p.add(lbl, gbc);

        // Wire back to stat fields
        if ("Total".equals(labelText))      statTotal = valLbl;
        else if ("Low Stock".equals(labelText)) statLow = valLbl;
        else if ("OK".equals(labelText))    statOk  = valLbl;

        return p;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(DS.D_BORDER);
        return sep;
    }

    private JLabel makeSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(DS.FONT_LABEL); lbl.setForeground(DS.D_SUBTEXT);
        return lbl;
    }

    /** Utility: clone gbc with new row and insets, add component, return gbc. */
    private GridBagConstraints at(GridBagConstraints gbc,
            int row, int top, int left, int bottom, int right) {
        gbc.gridy  = row;
        gbc.insets = new Insets(top, left, bottom, right);
        return gbc;
    }

    private void applyNavKeys(JComponent... components) {
        for (int i = 0; i < components.length; i++) {
            final int idx = i;
            components[i].addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if      (e.getKeyCode() == KeyEvent.VK_DOWN  && idx < components.length - 1)
                        components[idx + 1].requestFocus();
                    else if (e.getKeyCode() == KeyEvent.VK_UP    && idx > 0)
                        components[idx - 1].requestFocus();
                    else if (e.getKeyCode() == KeyEvent.VK_RIGHT && inventoryTable.getRowCount() > 0) {
                        inventoryTable.requestFocus();
                        inventoryTable.setRowSelectionInterval(0, 0);
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER && components[idx] instanceof JButton)
                        ((JButton) components[idx]).doClick();
                }
            });
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═══════════════════════════════════════════════════════════

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StockMasterPro::new);
    }
}
