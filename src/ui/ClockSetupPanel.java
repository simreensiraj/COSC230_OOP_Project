
package ui;

import java.awt.GridLayout;
import java.awt.Window;
import java.text.ParseException;

import javax.swing.*;

public class ClockSetupPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JSpinner yearSpinner;
    private JSpinner monthSpinner;
    private JSpinner daySpinner;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;

    public ClockSetupPanel(Runnable onClockSet) {

        setLayout(new GridLayout(6, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        yearSpinner = new JSpinner(
                new SpinnerNumberModel(2026, 1, 9999, 1));

        monthSpinner = new JSpinner(
                new SpinnerNumberModel(11, 1, 12, 1));

        daySpinner = new JSpinner(
                new SpinnerNumberModel(19, 1, 31, 1));

        hourSpinner = new JSpinner(
                new SpinnerNumberModel(10, 0, 23, 1));

        minuteSpinner = new JSpinner(
                new SpinnerNumberModel(20, 0, 59, 1));

        add(new JLabel("Year:"));
        add(yearSpinner);

        add(new JLabel("Month:"));
        add(monthSpinner);

        add(new JLabel("Day:"));
        add(daySpinner);

        add(new JLabel("Hour:"));
        add(hourSpinner);

        add(new JLabel("Minute:"));
        add(minuteSpinner);

        JButton setButton = new JButton("Set Clock");
        add(new JLabel());
        add(setButton);

        setButton.addActionListener(e -> {

            try {
                yearSpinner.commitEdit();
                monthSpinner.commitEdit();
                daySpinner.commitEdit();
                hourSpinner.commitEdit();
                minuteSpinner.commitEdit();

                int year = (Integer) yearSpinner.getValue();
                int month = (Integer) monthSpinner.getValue();
                int day = (Integer) daySpinner.getValue();
                int hour = (Integer) hourSpinner.getValue();
                int minute = (Integer) minuteSpinner.getValue();

                boolean success = Main.clock.setStart(
                        year, month, day, hour, minute);

                if (!success) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid date/time or clock already set.",
                            "Clock Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Main.save();
                onClockSet.run();

                JOptionPane.showMessageDialog(this,
                        "Simulation clock set successfully.");

                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                }

            } catch (ParseException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid date and time values.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
