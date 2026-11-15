
package ui;

import javax.swing.*;
import java.awt.*;

public class StudentRegistrationFrame extends JFrame {

	// Text fields
	JTextField idField;
	JTextField nameField;
	JTextField emailField;
	JTextField phoneField;
	JPasswordField passwordField;

	// Buttons
	JButton submitButton;
	JButton backButton;

	public StudentRegistrationFrame() {

		GridLayout layout = new GridLayout(6, 2);

		// Create text fields
		idField = new JTextField();
		nameField = new JTextField();
		emailField = new JTextField();
		phoneField = new JTextField();
		passwordField = new JPasswordField();

		// Create buttons
		submitButton = new JButton("Submit Request");
		backButton = new JButton("Back to Login");

		// Add components
		add(new JLabel("University ID:"));
		add(idField);

		add(new JLabel("Full Name:"));
		add(nameField);

		add(new JLabel("Email:"));
		add(emailField);

		add(new JLabel("Phone Number:"));
		add(phoneField);

		add(new JLabel("Password:"));
		add(passwordField);

		add(submitButton);
		add(backButton);

		// Window settings
		setLayout(layout);
		setTitle("GPU Utilization Management - Registration");
		setSize(550, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	// Temporary main method for testing
	public static void main(String[] args) {
		new StudentRegistrationFrame();
	}
}
