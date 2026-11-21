package ui;

import java.awt.GridLayout;
import java.awt.Window;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.*;

public class ClockSetupPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JSpinner dateSpinner;
    private final JSpinner timeSpinner;

    public ClockSetupPanel(Runnable onClockSet) {
        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        // Date picker and time picker
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        add(new JLabel("Date:"));
        add(dateSpinner);
        add(new JLabel("Time (24-hour):"));
        add(timeSpinner);
        JButton setButton = new JButton("Set Clock");
        add(new JLabel());
        add(setButton);
        setButton.addActionListener(e -> {
            try {
                dateSpinner.commitEdit();
                timeSpinner.commitEdit();
                // Date from one picker, time from the other
                LocalDateTime date = toLocalDateTime((Date) dateSpinner.getValue());
                LocalDateTime time = toLocalDateTime((Date) timeSpinner.getValue());
                boolean success = Main.clock.setStart(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), time.getHour(), time.getMinute());
                if (!success) {
                    JOptionPane.showMessageDialog(this, "Invalid date/time or clock already set.", "Clock Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Main.save();
                onClockSet.run();
                JOptionPane.showMessageDialog(this, "Simulation clock set successfully.");
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid date and time.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private LocalDateTime toLocalDateTime(Date value) {
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }
}
