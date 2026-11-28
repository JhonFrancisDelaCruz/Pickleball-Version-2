package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class SettingsDialog extends JDialog {

    private final JSpinner waitingLimitSpinner;
    private final JSpinner maxCourtsSpinner;
    private final JCheckBox autoWaitingCheck;
    private final JComboBox<String> themeCombo;
    private boolean saved = false;

    private int waitingLimit;
    private int maxCourts;
    private boolean autoWaiting;
    private Theme.Mode themeMode;

    public SettingsDialog(Frame parent, int currentLimit, boolean currentAuto, Theme.Mode currentMode,
                          int currentMaxCourts, int currentCourtCount) {
        super(parent, "Settings", true);
        this.waitingLimit = currentLimit;
        this.maxCourts = currentMaxCourts;
        this.autoWaiting = currentAuto;
        this.themeMode = currentMode;

        setResizable(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel title = new JLabel("Settings");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BG);
        form.setBorder(new EmptyBorder(8, 0, 8, 0));

        // Waiting limit
        JLabel lblLimit = new JLabel("Default Waiting Limit");
        lblLimit.setFont(Theme.FONT_SMALL);
        lblLimit.setForeground(Theme.TEXT_SEC);
        lblLimit.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblLimit);
        form.add(Box.createVerticalStrut(4));

        waitingLimitSpinner = new JSpinner(new SpinnerNumberModel(currentLimit, 4, 100, 1));
        waitingLimitSpinner.setMaximumSize(new Dimension(120, 28));
        waitingLimitSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(waitingLimitSpinner);
        form.add(Box.createVerticalStrut(12));

        // Auto waiting
        autoWaitingCheck = new JCheckBox("Auto-set waiting limit (4 slots per extra court)");
        autoWaitingCheck.setSelected(currentAuto);
        autoWaitingCheck.setBackground(Theme.BG);
        autoWaitingCheck.setForeground(Theme.TEXT);
        autoWaitingCheck.setFont(Theme.FONT_BODY);
        autoWaitingCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoWaitingCheck.addActionListener(e -> waitingLimitSpinner.setEnabled(!autoWaitingCheck.isSelected()));
        waitingLimitSpinner.setEnabled(!currentAuto);
        form.add(autoWaitingCheck);
        form.add(Box.createVerticalStrut(12));

        JLabel lblCourts = new JLabel("Maximum Number of Courts");
        lblCourts.setFont(Theme.FONT_SMALL);
        lblCourts.setForeground(Theme.TEXT_SEC);
        lblCourts.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblCourts);
        form.add(Box.createVerticalStrut(4));

        maxCourtsSpinner = new JSpinner(new SpinnerNumberModel(
            Math.max(currentCourtCount, Math.min(currentMaxCourts, 50)), currentCourtCount, 50, 1));
        maxCourtsSpinner.setMaximumSize(new Dimension(120, 28));
        maxCourtsSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(maxCourtsSpinner);
        form.add(Box.createVerticalStrut(12));

        // Theme
        JLabel lblTheme = new JLabel("Theme");
        lblTheme.setFont(Theme.FONT_SMALL);
        lblTheme.setForeground(Theme.TEXT_SEC);
        lblTheme.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblTheme);
        form.add(Box.createVerticalStrut(4));

        themeCombo = new JComboBox<>(new String[]{"Light", "Dark"});
        themeCombo.setSelectedItem(currentMode == Theme.Mode.DARK ? "Dark" : "Light");
        themeCombo.setMaximumSize(new Dimension(160, 28));
        themeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(themeCombo);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(Theme.BG);

        JButton cancel = new JButton("Cancel");
        Theme.styleSecondaryButton(cancel);
        cancel.addActionListener(e -> dispose());

        JButton save = new JButton("Save");
        Theme.stylePrimaryButton(save);
        save.addActionListener(e -> {
            waitingLimit = (Integer) waitingLimitSpinner.getValue();
            maxCourts = (Integer) maxCourtsSpinner.getValue();
            autoWaiting = autoWaitingCheck.isSelected();
            themeMode = "Dark".equals(themeCombo.getSelectedItem()) ? Theme.Mode.DARK : Theme.Mode.LIGHT;
            saved = true;
            dispose();
        });

        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(320, 250));
        setLocationRelativeTo(parent);
    }

    public boolean isSaved() { return saved; }
    public int getWaitingLimit() { return waitingLimit; }
    public int getMaxCourts() { return maxCourts; }
    public boolean isAutoWaiting() { return autoWaiting; }
    public Theme.Mode getThemeMode() { return themeMode; }
}
