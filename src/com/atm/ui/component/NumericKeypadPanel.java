package com.atm.ui.component;

import com.atm.ui.UITheme;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable ATM on-screen numeric keypad component mimicking a physical ATM PIN pad.
 * Directly appends characters to an active JTextComponent (JPasswordField or JTextField).
 */
public class NumericKeypadPanel extends JPanel {
    private JTextComponent activeField;
    private final List<ModernButton> keyButtons = new ArrayList<>();
    private ActionListener enterActionListener;
    private final boolean includeDecimal;

    public NumericKeypadPanel(boolean includeDecimal) {
        this.includeDecimal = includeDecimal;
        setLayout(new GridLayout(4, 3, 10, 10));
        setOpaque(false);
        initKeypad();
    }

    public NumericKeypadPanel() {
        this(false);
    }

    public void setActiveField(JTextComponent field) {
        this.activeField = field;
    }

    public JTextComponent getActiveField() {
        return activeField;
    }

    public void setEnterActionListener(ActionListener listener) {
        this.enterActionListener = listener;
    }

    private void initKeypad() {
        // Digits 1-9
        for (int i = 1; i <= 9; i++) {
            final String digit = String.valueOf(i);
            ModernButton btn = createKeyButton(digit, ModernButton.Style.KEYPAD);
            btn.addActionListener(e -> appendText(digit));
            keyButtons.add(btn);
            add(btn);
        }

        // Bottom row: Left (Clear / Decimal), Center (0), Right (Backspace)
        if (includeDecimal) {
            ModernButton dotBtn = createKeyButton(".", ModernButton.Style.KEYPAD);
            dotBtn.addActionListener(e -> {
                if (activeField != null && !activeField.getText().contains(".")) {
                    appendText(".");
                }
            });
            keyButtons.add(dotBtn);
            add(dotBtn);
        } else {
            ModernButton clearBtn = createKeyButton("CLEAR", ModernButton.Style.WARNING);
            clearBtn.setFont(UITheme.FONT_BODY_BOLD);
            clearBtn.addActionListener(e -> clearText());
            keyButtons.add(clearBtn);
            add(clearBtn);
        }

        // Digit 0
        ModernButton zeroBtn = createKeyButton("0", ModernButton.Style.KEYPAD);
        zeroBtn.addActionListener(e -> appendText("0"));
        keyButtons.add(zeroBtn);
        add(zeroBtn);

        // Backspace
        ModernButton backBtn = createKeyButton("⌫", ModernButton.Style.DANGER);
        backBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
        backBtn.addActionListener(e -> backspaceText());
        keyButtons.add(backBtn);
        add(backBtn);
    }

    private ModernButton createKeyButton(String text, ModernButton.Style style) {
        ModernButton btn = new ModernButton(text, style);
        btn.setFont(UITheme.FONT_KEYPAD);
        btn.setPreferredSize(new Dimension(75, 55));
        btn.setCornerRadius(10);
        return btn;
    }

    public void appendText(String str) {
        if (activeField == null || !activeField.isEnabled()) {
            return;
        }
        if (activeField instanceof JPasswordField) {
            char[] current = ((JPasswordField) activeField).getPassword();
            if (current.length < 4) { // Typical 4-digit PIN limit
                activeField.setText(new String(current) + str);
            }
        } else {
            activeField.setText(activeField.getText() + str);
        }
        activeField.requestFocusInWindow();
    }

    public void clearText() {
        if (activeField != null && activeField.isEnabled()) {
            activeField.setText("");
            activeField.requestFocusInWindow();
        }
    }

    public void backspaceText() {
        if (activeField == null || !activeField.isEnabled()) {
            return;
        }
        String current = activeField.getText();
        if (current != null && !current.isEmpty()) {
            activeField.setText(current.substring(0, current.length() - 1));
        }
        activeField.requestFocusInWindow();
    }

    public void setKeypadEnabled(boolean enabled) {
        for (ModernButton btn : keyButtons) {
            btn.setEnabled(enabled);
        }
    }
}
