
package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.*;

import logic.AccountService;
import model.Administrator;
import model.AppData;

public class AdminFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JLabel clockLabel;
    private JButton advanceButton;
    private JButton setupButton;
    private JTabbedPane tabs;

    private DashboardPanel dashboardPanel;
    private RequestsPanel requestsPanel;

    public AdminFrame(AppData data, AccountService accountService,
            Administrator admin) {

        setTitle("GPU Utilization Management - Administrator");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top section
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("GPU Utilization Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        clockLabel = new JLabel();
        setupButton = new JButton("Set Clock");
        advanceButton = new JButton("+1 minute");
        JButton logoutButton = new JButton("Log out");

        buttonPanel.add(clockLabel);
        buttonPanel.add(setupButton);
        buttonPanel.add(advanceButton);
        buttonPanel.add(logoutButton);

        topPanel.add(buttonPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Administrator tabs
        tabs = new JTabbedPane();

        // Dashboard connected to the shared scheduler and clock
        dashboardPanel = new DashboardPanel(
                data, Main.scheduler, Main.clock);

        tabs.addTab("Dashboard", dashboardPanel);

        // Registration requests
        requestsPanel = new RequestsPanel(
                data, accountService, admin);

        tabs.addTab("Requests", requestsPanel);

        // Remaining tabs
        tabs.addTab("Students", createPlaceholder("Students"));
        tabs.addTab("All Jobs", createPlaceholder("All Jobs"));

        add(tabs, BorderLayout.CENTER);

        // Set simulation clock
        setupButton.addActionListener(e -> showClockSetup());

        // Advance simulation one minute
        advanceButton.addActionListener(e -> {

            try {
                boolean advanced = Main.scheduler.advanceMinute();

                if (advanced) {
                    Main.save();
                    refreshAll();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Please set the simulation clock first.");
                }

            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Simulation Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Logout and return to Alyazeya's LoginFrame
        logoutButton.addActionListener(e -> {
            Main.save();
            dispose();

            new LoginFrame(data, accountService, Main.scheduler);
        });

        // Refresh tables when switching tabs
        tabs.addChangeListener(e -> {

            if (tabs.getSelectedComponent() == requestsPanel) {
                requestsPanel.refreshRequests();
            }

            if (tabs.getSelectedComponent() == dashboardPanel) {
                dashboardPanel.refreshDashboard();
            }
        });

        refreshAll();

        // First-time clock setup
        SwingUtilities.invokeLater(() -> {

            if (!Main.clock.isSet() && isDisplayable()) {
                showClockSetup();
            }
        });
    }

    private void showClockSetup() {

        if (Main.clock.isSet()) {
            return;
        }

        JDialog dialog = new JDialog(
                this, "Set Simulation Clock", true);

        dialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE);

        dialog.add(new ClockSetupPanel(this::refreshAll));

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void refreshAll() {

        if (Main.clock != null && Main.clock.isSet()) {

            clockLabel.setText("Simulated time: " + Main.clock);

            setupButton.setEnabled(false);
            advanceButton.setEnabled(true);

        } else {

            clockLabel.setText("Simulated time: Not set");

            setupButton.setEnabled(true);
            advanceButton.setEnabled(false);
        }

        requestsPanel.refreshRequests();
        dashboardPanel.refreshDashboard();
    }

    private JPanel createPlaceholder(String text) {

        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(
                text + " - Coming soon",
                SwingConstants.CENTER);

        label.setFont(new Font("Arial", Font.PLAIN, 18));

        panel.add(label, BorderLayout.CENTER);

        return panel;
    }
}
