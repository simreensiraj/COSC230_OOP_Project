package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import model.Student;
import model.AppData;
import logic.AccountService;

public class StudentFrame extends JFrame implements ActionListener {

	// Shared objects
	AppData data;
	AccountService accountService;
	Student student;

	// Labels
	JLabel nameLabel;
	JLabel idLabel;
	JLabel allocatedLabel;
	JLabel usedLabel;
	JLabel reservedLabel;
	JLabel availableLabel;

	// Buttons
	JButton refreshButton;
	JButton logoutButton;

	public StudentFrame(AppData data, AccountService accountService, Student student) {

		this.data = data;
		this.accountService = accountService;
		this.student = student;

		setLayout(new GridLayout(8, 2));

		// Create labels
		nameLabel = new JLabel(student.getName());
		idLabel = new JLabel(student.getUniversityId());

		allocatedLabel = new JLabel();
		usedLabel = new JLabel();
		reservedLabel = new JLabel();
		availableLabel = new JLabel();

		// Create buttons
		refreshButton = new JButton("Refresh Balance");
		logoutButton = new JButton("Log Out");

		// Add action listeners
		refreshButton.addActionListener(this);
		logoutButton.addActionListener(this);

		// Add components
		add(new JLabel("Welcome:"));
		add(nameLabel);

		add(new JLabel("University ID:"));
		add(idLabel);

		add(new JLabel("Allocated GPU Minutes:"));
		add(allocatedLabel);

		add(new JLabel("Used GPU Minutes:"));
		add(usedLabel);

		add(new JLabel("Reserved GPU Minutes:"));
		add(reservedLabel);

		add(new JLabel("Available GPU Minutes:"));
		add(availableLabel);

		add(refreshButton);
		add(logoutButton);

		// Update balance
		updateBalance();

		// Window settings
		setTitle("GPU Utilization Management - Student");
		setSize(600, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void updateBalance() {

		allocatedLabel.setText(String.valueOf(student.getAllowance().getAllocated()));

		usedLabel.setText(String.valueOf(student.getAllowance().getUsed()));

		reservedLabel.setText(String.valueOf(student.getAllowance().getReserved()));

		availableLabel.setText(String.valueOf(student.getAllowance().getAvailable()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == refreshButton) {

			updateBalance();

		} else if (e.getSource() == logoutButton) {

			new LoginFrame(data, accountService);
			dispose();
		}
	}
}