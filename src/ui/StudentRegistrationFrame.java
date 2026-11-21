package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import logic.AccountService;
import logic.ValidationException;
import logic.Scheduler;
import model.AppData;

public class StudentRegistrationFrame extends JFrame implements ActionListener {

	// Text fields
	JTextField idField;
	JTextField nameField;
	JTextField emailField;
	JTextField phoneField;
	JPasswordField passwordField;

	// Buttons
	JButton submitButton;
	JButton backButton;

	// Shared objects
	AppData data;
	AccountService accountService;
	Scheduler scheduler;

	public StudentRegistrationFrame(AppData data, AccountService accountService, Scheduler scheduler) {

		this.data = data;
		this.accountService = accountService;
		this.scheduler = scheduler;

		GridLayout layout = new GridLayout(6, 2);
		setLayout(layout);

		// Create text fields
		idField = new JTextField();
		nameField = new JTextField();
		emailField = new JTextField();
		phoneField = new JTextField();
		passwordField = new JPasswordField();

		// Create buttons
		submitButton = new JButton("Submit Request");
		backButton = new JButton("Back to Login");

		// Add action listeners
		submitButton.addActionListener(this);
		backButton.addActionListener(this);

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
		setTitle("GPU Utilization Management - Registration");
		setSize(550, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == submitButton) {

			try {

				accountService.register(idField.getText(), nameField.getText(), emailField.getText(),
						phoneField.getText(), new String(passwordField.getPassword()));

				JOptionPane.showMessageDialog(this, "Registration request submitted successfully!");

			} catch (ValidationException ex) {

				JOptionPane.showMessageDialog(this, ex.getMessage());
			}

		} else if (e.getSource() == backButton) {

			new LoginFrame(data, accountService, scheduler);
			dispose();
		}
	}
}
