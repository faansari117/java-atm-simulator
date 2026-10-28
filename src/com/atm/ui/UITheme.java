package com.atm.ui;

import java.awt.*;

/**
 * Global design tokens, color palettes, and typography for the ATM simulation GUI.
 * Adheres to modern banking terminal aesthetics with high contrast and sleek dark slate motifs.
 */
public final class UITheme {

    private UITheme() {
        // Prevent instantiation
    }

    // Color Palette - Backgrounds
    public static final Color BG_DARK = new Color(0x0F, 0x17, 0x2A);          // Deep Slate 900
    public static final Color BG_PANEL = new Color(0x1E, 0x29, 0x3B);         // Slate 800
    public static final Color BG_CARD = new Color(0x33, 0x41, 0x55);          // Slate 700
    public static final Color BG_CARD_LIGHT = new Color(0x47, 0x55, 0x69);    // Slate 600
    public static final Color BG_INPUT = new Color(0x0F, 0x17, 0x2A);         // Deep Slate for inputs

    // Color Palette - Accents
    public static final Color ACCENT_BLUE = new Color(0x3B, 0x82, 0xF6);      // Electric Blue 500
    public static final Color ACCENT_BLUE_HOVER = new Color(0x25, 0x63, 0xEB);// Blue 600
    public static final Color ACCENT_CYAN = new Color(0x06, 0xB6, 0xD4);      // Cyan 500
    public static final Color ACCENT_GREEN = new Color(0x10, 0xB9, 0x81);     // Emerald 500
    public static final Color ACCENT_GREEN_HOVER = new Color(0x05, 0x96, 0x69);
    public static final Color ACCENT_AMBER = new Color(0xF5, 0x9E, 0x0B);     // Amber 500
    public static final Color ACCENT_RED = new Color(0xEF, 0x44, 0x44);       // Red 500
    public static final Color ACCENT_RED_HOVER = new Color(0xDC, 0x26, 0x26);
    public static final Color ACCENT_PURPLE = new Color(0x8B, 0x5C, 0xF6);    // Violet 500

    // Keypad specific colors
    public static final Color KEYPAD_BTN_BG = new Color(0x28, 0x35, 0x4E);
    public static final Color KEYPAD_BTN_HOVER = new Color(0x3B, 0x4D, 0x70);
    public static final Color KEYPAD_BTN_ACTION_BG = new Color(0x1E, 0x3A, 0x5F);

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);     // Slate 50
    public static final Color TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);   // Slate 400
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);       // Slate 500
    public static final Color TEXT_DARK = new Color(0x0F, 0x17, 0x2A);

    // Borders
    public static final Color BORDER_SUBTLE = new Color(0x47, 0x55, 0x69);
    public static final Color BORDER_ACTIVE = new Color(0x3B, 0x82, 0xF6);

    // Typography
    public static final Font FONT_BRAND = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_DISPLAY_AMOUNT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_PIN_INPUT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_KEYPAD = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_MONOSPACE = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_MONOSPACE_BOLD = new Font("Consolas", Font.BOLD, 13);
}
