package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import model.Student;
import model.AppData;
import model.Job;
import model.TaskSize;
import model.TimeFormat;
import logic.AccountService;
import logic.Scheduler;

// The student window: a "Home and Submit" tab and a "My Jobs" tab, like the guide's mockup
public class StudentFrame extends JFrame implements ActionListener {

	// Shared objects
	AppData data;
	AccountService accountService;
	Scheduler scheduler;
	Student student;

	// Labels
	JLabel clockLabel;
	JLabel allocatedLabel;
	JLabel usedLabel;
	JLabel reservedLabel;
	JLabel availableLabel;
	JLabel durationLabel;

	// Job form
	JTextField titleField;
	JTextField applicationField;
	JTextArea descriptionArea;
	JComboBox<TaskSize> sizeBox;

	// Buttons
	JButton submitButton;
	JButton refreshButton;
	JButton logoutButton;

	// Tabs
	JTabbedPane tabs;
	MyJobsPanel myJobsPanel;

	public StudentFrame(AppData data, AccountService accountService, Scheduler scheduler, Student student) {

		this.data = data;
		this.accountService = accountService;
		this.scheduler = scheduler;
		this.student = student;

		setLayout(new BorderLayout(10, 10));

		// Top: who is logged in, simulated time, refresh and log out
		JPanel namePanel = new JPanel(new GridLayout(2, 1));
		namePanel.add(new JLabel("Welcome, " + student.getName()));
		namePanel.add(new JLabel("Student · " + student.getUniversityId()));

		clockLabel = new JLabel();
		refreshButton = new JButton("Refresh");
		logoutButton = new JButton("Log Out");

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rightPanel.add(clockLabel);
		rightPanel.add(refreshButton);
		rightPanel.add(logoutButton);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		topPanel.add(namePanel, BorderLayout.WEST);
		topPanel.add(rightPanel, BorderLayout.EAST);
		add(topPanel, BorderLayout.NORTH);

		// Home tab, part 1: GPU minute balances
		allocatedLabel = new JLabel();
		usedLabel = new JLabel();
		reservedLabel = new JLabel();
		availableLabel = new JLabel();

		JPanel balancePanel = new JPanel(new GridLayout(2, 4, 10, 5));
		balancePanel.setBorder(BorderFactory.createTitledBorder("GPU Minutes"));
		balancePanel.add(new JLabel("Allocated"));
		balancePanel.add(new JLabel("Used"));
		balancePanel.add(new JLabel("Reserved"));
		balancePanel.add(new JLabel("Available"));
		balancePanel.add(allocatedLabel);
		balancePanel.add(usedLabel);
		balancePanel.add(reservedLabel);
		balancePanel.add(availableLabel);

		// Home tab, part 2: the job form
		titleField = new JTextField();
		applicationField = new JTextField();

		descriptionArea = new JTextArea(3, 20);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);

		sizeBox = new JComboBox<>(TaskSize.values());
		durationLabel = new JLabel();
		submitButton = new JButton("Submit Job");

		JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
		formPanel.setBorder(BorderFactory.createTitledBorder("Submit a New Job"));
		formPanel.add(new JLabel("Job Title:"));
		formPanel.add(titleField);
		formPanel.add(new JLabel("Application Name:"));
		formPanel.add(applicationField);
		formPanel.add(new JLabel("Description:"));
		formPanel.add(new JScrollPane(descriptionArea));
		formPanel.add(new JLabel("Task Size:"));
		formPanel.add(sizeBox);
		formPanel.add(new JLabel("Estimated Duration:"));
		formPanel.add(durationLabel);
		formPanel.add(new JLabel());
		formPanel.add(submitButton);

		JPanel homePanel = new JPanel(new BorderLayout(10, 10));
		homePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		homePanel.add(balancePanel, BorderLayout.NORTH);
		homePanel.add(formPanel, BorderLayout.CENTER);

		// My Jobs tab
		myJobsPanel = new MyJobsPanel(scheduler, student);

		tabs = new JTabbedPane();
		tabs.addTab("Home and Submit", homePanel);
		tabs.addTab("My Jobs", myJobsPanel);
		add(tabs, BorderLayout.CENTER);

		// Add action listeners
		submitButton.addActionListener(this);
		refreshButton.addActionListener(this);
		logoutButton.addActionListener(this);
		sizeBox.addActionListener(this);

		// Refresh when another tab is picked or the window comes to the front
		tabs.addChangeListener(e -> refresh());

		addWindowFocusListener(new WindowAdapter() {

			@Override
			public void windowGainedFocus(WindowEvent e) {
				refresh();
			}
		});

		updateDuration();
		refresh();

		// Window settings
		setTitle("GPU Utilization Management - Student");
		setSize(1000, 520);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// Update balances, simulated time and the jobs table
	private void refresh() {

		allocatedLabel.setText(String.valueOf(student.getAllowance().getAllocated()));
		usedLabel.setText(String.valueOf(student.getAllowance().getUsed()));
		reservedLabel.setText(String.valueOf(student.getAllowance().getReserved()));
		availableLabel.setText(String.valueOf(student.getAllowance().getAvailable()));

		if (scheduler.getClock().isSet()) {
			clockLabel.setText("Simulated time: " + TimeFormat.format(scheduler.getClock().getNow()));
		} else {
			clockLabel.setText("Simulated time: Not Set");
		}

		myJobsPanel.refreshJobs();
	}

	// Duration comes from the task size
	private void updateDuration() {

		TaskSize selectedSize = (TaskSize) sizeBox.getSelectedItem();

		if (selectedSize != null) {
			durationLabel.setText(selectedSize.getDuration() + " minutes");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == sizeBox) {

			updateDuration();

		} else if (e.getSource() == submitButton) {

			submitJob();

		} else if (e.getSource() == refreshButton) {

			refresh();

		} else if (e.getSource() == logoutButton) {

			// Return to login with shared objects
			new LoginFrame(data, accountService, scheduler).setVisible(true);
			dispose();
		}
	}

	private void submitJob() {

		String title = titleField.getText().trim();
		String application = applicationField.getText().trim();
		String description = descriptionArea.getText().trim();
		TaskSize selectedSize = (TaskSize) sizeBox.getSelectedItem();

		// Validate required fields
		if (title.isEmpty() || application.isEmpty() || description.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please complete all fields.");
			return;
		}

		if (selectedSize == null) {
			JOptionPane.showMessageDialog(this, "Please select a task size.");
			return;
		}

		if (!scheduler.getClock().isSet()) {
			JOptionPane.showMessageDialog(this, "The simulation clock has not been set yet.");
			return;
		}

		// Check available GPU minutes before asking to confirm
		int duration = selectedSize.getDuration();
		int available = student.getAllowance().getAvailable();

		if (duration > available) {
			JOptionPane.showMessageDialog(this, "Insufficient GPU minutes." + "\nRequired: " + duration + " minutes"
					+ "\nAvailable: " + available + " minutes");
			return;
		}

		// Confirm job details before submission
		int confirm = JOptionPane.showConfirmDialog(this,
				"Job Title: " + title + "\nApplication: " + application + "\nTask Size: " + selectedSize
						+ "\nEstimated Duration: " + duration + " minutes" + "\nAvailable GPU Minutes: " + available
						+ "\n\nDo you want to submit this job?",
				"Confirm Job Submission", JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		try {

			// Submit through the shared scheduler
			Job job = scheduler.submit(student.getUniversityId(), title, application, description, selectedSize);

			JOptionPane.showMessageDialog(this,
					"Job submitted successfully!" + "\nJob ID: " + job.getJobId() + "\nStatus: " + job.getStatus());

			titleField.setText("");
			applicationField.setText("");
			descriptionArea.setText("");
			refresh();

		} catch (IllegalArgumentException | IllegalStateException ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage());
		}
	}
}
