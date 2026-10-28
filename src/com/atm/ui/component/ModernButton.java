package com.atm.ui.component;

import com.atm.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Polished, modern button with rounded corners, custom color palettes,
 * hover states, and clear visual response.
 */
public class ModernButton extends JButton {

    public enum Style {
        PRIMARY(UITheme.ACCENT_BLUE, UITheme.ACCENT_BLUE_HOVER, UITheme.TEXT_PRIMARY),
        SUCCESS(UITheme.ACCENT_GREEN, UITheme.ACCENT_GREEN_HOVER, UITheme.TEXT_PRIMARY),
        DANGER(UITheme.ACCENT_RED, UITheme.ACCENT_RED_HOVER, UITheme.TEXT_PRIMARY),
        WARNING(UITheme.ACCENT_AMBER, new Color(0xD9, 0x77, 0x06), UITheme.TEXT_PRIMARY),
        SECONDARY(UITheme.BG_CARD, UITheme.BG_CARD_LIGHT, UITheme.TEXT_PRIMARY),
        KEYPAD(UITheme.KEYPAD_BTN_BG, UITheme.KEYPAD_BTN_HOVER, UITheme.TEXT_PRIMARY),
        OUTLINE(new Color(0, 0, 0, 0), UITheme.BG_CARD, UITheme.TEXT_PRIMARY);

        final Color baseColor;
        final Color hoverColor;
        final Color textColor;

        Style(Color baseColor, Color hoverColor, Color textColor) {
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.textColor = textColor;
        }
    }

    private final Style buttonStyle;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 10;
    private Color customBaseColor;
    private Color customHoverColor;

    public ModernButton(String text, Style style) {
        super(text);
        this.buttonStyle = style;
        this.customBaseColor = style.baseColor;
        this.customHoverColor = style.hoverColor;

        initUI();
    }

    public ModernButton(String text, Color baseColor, Color hoverColor) {
        super(text);
        this.buttonStyle = Style.PRIMARY;
        this.customBaseColor = baseColor;
        this.customHoverColor = hoverColor;

        initUI();
    }

    private void initUI() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setFont(UITheme.FONT_BODY_BOLD);
        setForeground(buttonStyle.textColor);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color currentColor;
        if (!isEnabled()) {
            currentColor = new Color(0x33, 0x41, 0x55, 0x80); // Disabled transparent slate
        } else if (isPressed) {
            currentColor = customHoverColor.darker();
        } else if (isHovered) {
            currentColor = customHoverColor;
        } else {
            currentColor = customBaseColor;
        }

        // Fill rounded background
        g2.setColor(currentColor);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // If outline style, draw border
        if (buttonStyle == Style.OUTLINE) {
            g2.setColor(UITheme.BORDER_SUBTLE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
