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
	private JTabbedPane tabs;

	public AdminFrame(AppData data, AccountService accountService, Administrator admin) {

		setTitle("GPU Utilization Management - Administrator");
		setSize(1000, 650);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		// Top section
		JPanel topPanel = new JPanel(new BorderLayout());

		JLabel titleLabel = new JLabel("GPU Utilization Dashboard");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
		topPanel.add(titleLabel, BorderLayout.WEST);

		JPanel buttonPanel = new JPanel(new FlowLayout());

		clockLabel = new JLabel("Simulated time: Not set");

		JButton advanceButton = new JButton("+1 minute");
		JButton logoutButton = new JButton("Log out");

		// Connect to the simulated clock during integration
		advanceButton.setEnabled(false);

		// Return to login using the same shared objects
		logoutButton.addActionListener(e -> {
			new LoginFrame(data, accountService);
			dispose();
		});

		buttonPanel.add(clockLabel);
		buttonPanel.add(advanceButton);
		buttonPanel.add(logoutButton);

		topPanel.add(buttonPanel, BorderLayout.EAST);
		add(topPanel, BorderLayout.NORTH);

		// Administrator tabs
		tabs = new JTabbedPane();

		tabs.addTab("Dashboard", createPlaceholder("Dashboard"));

		tabs.addTab("Requests", new RequestsPanel(data, accountService, admin));

		tabs.addTab("Students", createPlaceholder("Students"));
		tabs.addTab("All Jobs", createPlaceholder("All Jobs"));

		add(tabs, BorderLayout.CENTER);
	}

	private JPanel createPlaceholder(String text) {

		JPanel panel = new JPanel(new BorderLayout());

		JLabel label = new JLabel(text + " - Coming soon", SwingConstants.CENTER);

		label.setFont(new Font("Arial", Font.PLAIN, 18));

		panel.add(label, BorderLayout.CENTER);

		return panel;
	}

	// Temporary standalone launcher for UI testing
	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {

			AppData data = new AppData();
			AccountService accountService = new AccountService(data);
			Administrator admin = new Administrator();

			AdminFrame frame = new AdminFrame(data, accountService, admin);

			frame.setVisible(true);
		});
	}
}