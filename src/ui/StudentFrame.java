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
	JButton submitJobButton;
	JButton myJobsButton;

	public StudentFrame(AppData data, AccountService accountService, Student student) {

		this.data = data;
		this.accountService = accountService;
		this.student = student;

		setLayout(new GridLayout(0, 2, 10, 10));

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
		submitJobButton = new JButton("Submit GPU Job");
		myJobsButton = new JButton("My Jobs");

		// Add action listeners
		refreshButton.addActionListener(this);
		logoutButton.addActionListener(this);
		submitJobButton.addActionListener(this);
		myJobsButton.addActionListener(this);

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

		add(submitJobButton);
		add(myJobsButton);

		add(refreshButton);
		add(logoutButton);

		// Update balance
		updateBalance();

		// Window settings
		setTitle("GPU Utilization Management - Student");
		setSize(600, 450);
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

		if (e.getSource() == submitJobButton) {

			// Open job submission window
			new SubmitJobFrame();

		} else if (e.getSource() == myJobsButton) {

			// Open this student's jobs
			new MyJobsFrame(data, student);

		} else if (e.getSource() == refreshButton) {

			// Refresh GPU-minute balances
			updateBalance();

		} else if (e.getSource() == logoutButton) {

			// Return to login
			new LoginFrame(data, accountService);
			dispose();
		}
	}
}