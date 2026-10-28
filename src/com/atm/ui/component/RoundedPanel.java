package com.atm.ui.component;

import javax.swing.*;
import java.awt.*;

/**
 * Custom JPanel providing anti-aliased rounded corners, customizable background
 * color, optional border, and drop-elevation styling.
 */
public class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;
    private Color borderColor;
    private int borderWidth;

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, null, 0);
    }

    public RoundedPanel(int radius, Color bgColor, Color borderColor, int borderWidth) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        setOpaque(false);
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw background
        if (backgroundColor != null) {
            g2.setColor(backgroundColor);
            g2.fillRoundRect(borderWidth / 2, borderWidth / 2,
                    width - borderWidth, height - borderWidth, cornerRadius, cornerRadius);
        }

        // Draw border if configured
        if (borderColor != null && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRoundRect(borderWidth / 2, borderWidth / 2,
                    width - borderWidth, height - borderWidth, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }
}
