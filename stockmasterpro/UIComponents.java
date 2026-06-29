package stockmasterpro;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Reusable custom Swing components used across the application.
 *
 *  - RoundedBorder       — anti-aliased rounded border for scroll panes
 *  - PillButton          — custom painted button with hover/focus states
 *  - StyledTextField     — rounded text field with placeholder and focus ring
 *  - StyledPasswordField — same as StyledTextField but for passwords
 *  - Toast               — non-blocking floating notification
 */

// ── RoundedBorder ────────────────────────────────────────────
class RoundedBorder extends AbstractBorder {

    private final Color color;
    private final int   radius;

    RoundedBorder(Color c, int r) {
        color  = c;
        radius = r;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, radius, radius);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) { return new Insets(6, 12, 6, 12); }

    @Override
    public Insets getBorderInsets(Component c, Insets i) {
        i.set(6, 12, 6, 12);
        return i;
    }
}

// ── PillButton ───────────────────────────────────────────────
class PillButton extends JButton {

    private final Color baseColor;
    private final Color hoverColor;
    private boolean hovered = false;

    PillButton(String text, Color base, Color hover, Color textColor) {
        super(text);
        this.baseColor  = base;
        this.hoverColor = hover;

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(DS.FONT_LABEL);
        setForeground(textColor);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width, 38));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
        });
        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { repaint(); }
            public void focusLost(FocusEvent e)   { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = hovered ? hoverColor : baseColor;
        if (!isEnabled()) bg = bg.darker();
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), DS.RADIUS, DS.RADIUS);

        if (isFocusOwner()) {
            g2.setColor(DS.D_PRIMARY.brighter());
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, DS.RADIUS, DS.RADIUS);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

// ── StyledTextField ──────────────────────────────────────────
class StyledTextField extends JTextField {

    private final String placeholder;
    private Color  borderNormal;
    private Color  borderFocus;
    private boolean focused = false;

    StyledTextField(String placeholder, Color bNormal, Color bFocus) {
        this.placeholder  = placeholder;
        this.borderNormal = bNormal;
        this.borderFocus  = bFocus;

        setOpaque(false);
        setFont(DS.FONT_BODY);
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { focused = true;  repaint(); }
            public void focusLost(FocusEvent e)   { focused = false; repaint(); }
        });
    }

    public void setColors(Color bNormal, Color bFocus) {
        this.borderNormal = bNormal;
        this.borderFocus  = bFocus;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), DS.RADIUS, DS.RADIUS);
        g2.dispose();
        super.paintComponent(g);

        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setColor(new Color(130, 145, 172));
            g3.setFont(DS.FONT_BODY);
            FontMetrics fm = g3.getFontMetrics();
            g3.drawString(placeholder, 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g3.dispose();
        }
    }

    @Override
    public void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(focused ? borderFocus : borderNormal);
        g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, DS.RADIUS, DS.RADIUS);
        g2.dispose();
    }
}

// ── StyledPasswordField ──────────────────────────────────────
class StyledPasswordField extends JPasswordField {

    private Color   borderNormal;
    private Color   borderFocus;
    private boolean focused = false;

    StyledPasswordField(Color bNormal, Color bFocus) {
        this.borderNormal = bNormal;
        this.borderFocus  = bFocus;

        setOpaque(false);
        setFont(DS.FONT_BODY);
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { focused = true;  repaint(); }
            public void focusLost(FocusEvent e)   { focused = false; repaint(); }
        });
    }

    public void setColors(Color bNormal, Color bFocus) {
        this.borderNormal = bNormal;
        this.borderFocus  = bFocus;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), DS.RADIUS, DS.RADIUS);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(focused ? borderFocus : borderNormal);
        g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, DS.RADIUS, DS.RADIUS);
        g2.dispose();
    }
}

// ── Toast ────────────────────────────────────────────────────
class Toast extends JWindow {

    private Toast(Frame owner, String msg, Color bg, Color fg) {
        super(owner);
        JLabel label = new JLabel("  " + msg + "  ", SwingConstants.CENTER);
        label.setFont(DS.FONT_BODY);
        label.setForeground(fg);
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DS.RADIUS_LG, DS.RADIUS_LG);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.add(label);
        setBackground(new Color(0, 0, 0, 0));
        add(panel);
        pack();
    }

    /**
     * Shows a floating toast notification centred at the bottom of the screen.
     * Auto-dismisses after 2.5 seconds.
     *
     * @param owner   the parent frame (for positioning)
     * @param msg     message to display
     * @param success true = green (success), false = red (error)
     */
    public static void show(Frame owner, String msg, boolean success) {
        Color bg = success ? DS.D_SUCCESS : DS.D_DANGER;
        Toast t  = new Toast(owner, msg, bg, Color.WHITE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        t.setLocation((screen.width - t.getWidth()) / 2, screen.height - 120);
        t.setVisible(true);
        Timer timer = new Timer(2500, e -> t.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}
