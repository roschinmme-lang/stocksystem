package stockmasterpro;

import java.awt.*;

/**
 * Design system constants — color palette and typography.
 * All UI classes reference this file for consistent theming.
 */
public class DS {

    private DS() {}

    // ── Dark palette ─────────────────────────────────────────
    public static final Color D_BG      = new Color(12,  14,  20);
    public static final Color D_SURFACE = new Color(20,  24,  35);
    public static final Color D_CARD    = new Color(28,  33,  48);
    public static final Color D_BORDER  = new Color(45,  52,  72);
    public static final Color D_PRIMARY = new Color(99,  179, 237);
    public static final Color D_SUCCESS = new Color(72,  199, 142);
    public static final Color D_WARNING = new Color(255, 184, 0);
    public static final Color D_DANGER  = new Color(252, 92,  101);
    public static final Color D_TEXT    = new Color(225, 232, 245);
    public static final Color D_SUBTEXT = new Color(130, 145, 172);

    // ── Light palette ────────────────────────────────────────
    public static final Color L_BG      = new Color(246, 248, 252);
    public static final Color L_SURFACE = new Color(255, 255, 255);
    public static final Color L_CARD    = new Color(240, 243, 250);
    public static final Color L_BORDER  = new Color(210, 218, 234);
    public static final Color L_PRIMARY = new Color(37,  117, 252);
    public static final Color L_SUCCESS = new Color(34,  166, 109);
    public static final Color L_WARNING = new Color(217, 144, 0);
    public static final Color L_DANGER  = new Color(220, 53,  69);
    public static final Color L_TEXT    = new Color(20,  28,  50);
    public static final Color L_SUBTEXT = new Color(100, 115, 142);

    // ── Shape ────────────────────────────────────────────────
    public static final int RADIUS    = 10;
    public static final int RADIUS_LG = 16;

    // ── Typography ───────────────────────────────────────────
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD,  22);
    public static final Font FONT_HEAD  = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FONT_BODY  = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO  = new Font("Monospaced", Font.BOLD, 12);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD,  11);
}
