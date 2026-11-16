package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame implements ActionListener {

	// Text fields
	JTextField usernameField;
	JPasswordField passwordField;

	// Buttons
	JButton loginButton;
	JButton registerButton;

	public LoginFrame() {

		GridLayout layout = new GridLayout(3, 2);

		// Create text fields
		usernameField = new JTextField();
		passwordField = new JPasswordField();

		// Create buttons
		loginButton = new JButton("Login");
		registerButton = new JButton("Request Account");

		// Add action listeners
		loginButton.addActionListener(this);
		registerButton.addActionListener(this);

		// Add components
		add(new JLabel("University ID / Username:"));
		add(usernameField);

		add(new JLabel("Password:"));
		add(passwordField);

		add(loginButton);
		add(registerButton);

		// Window settings
		setLayout(layout);
		setTitle("GPU Utilization Management - Login");
		setSize(550, 300);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == loginButton) {

			JOptionPane.showMessageDialog(this, "Login button clicked!");

		} else if (e.getSource() == registerButton) {

			new StudentRegistrationFrame();
			dispose();

		}
	}

	// Temporary main method for testing
	public static void main(String[] args) {
		new LoginFrame();
	}
}
