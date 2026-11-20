package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import logic.Scheduler;
import model.Student;
import model.TaskSize;
import model.Job;

public class SubmitJobFrame extends JFrame implements ActionListener {

	// Shared objects
	Scheduler scheduler;
	Student student;

	// Input fields
	JTextField titleField;
	JTextField applicationField;
	JTextArea descriptionArea;
	JComboBox<TaskSize> sizeBox;

	// Labels
	JLabel durationLabel;

	// Buttons
	JButton submitButton;
	JButton cancelButton;

	public SubmitJobFrame(Scheduler scheduler, Student student) {

		this.scheduler = scheduler;
		this.student = student;

		setTitle("GPU Utilization Management - Submit Job");
		setLayout(new GridLayout(6, 2, 10, 10));

		// Create input fields
		titleField = new JTextField();
		applicationField = new JTextField();

		descriptionArea = new JTextArea(3, 20);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);

		// Task size dropdown
		sizeBox = new JComboBox<TaskSize>(TaskSize.values());

		// Estimated duration
		durationLabel = new JLabel();

		// Create buttons
		submitButton = new JButton("Submit Job");
		cancelButton = new JButton("Cancel");

		// Add action listeners
		submitButton.addActionListener(this);
		cancelButton.addActionListener(this);
		sizeBox.addActionListener(this);

		// Add components
		add(new JLabel("Job Title:"));
		add(titleField);

		add(new JLabel("Application Name:"));
		add(applicationField);

		add(new JLabel("Description:"));
		add(new JScrollPane(descriptionArea));

		add(new JLabel("Task Size:"));
		add(sizeBox);

		add(new JLabel("Estimated Duration:"));
		add(durationLabel);

		add(submitButton);
		add(cancelButton);

		// Show initial duration
		updateDuration();

		// Window settings
		setSize(650, 450);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	public void updateDuration() {

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

			String title = titleField.getText().trim();
			String application = applicationField.getText().trim();
			String description = descriptionArea.getText().trim();

			// Validate required fields
			if (title.isEmpty() || application.isEmpty() || description.isEmpty()) {

				JOptionPane.showMessageDialog(this, "Please complete all fields.");
				return;
			}

			TaskSize selectedSize = (TaskSize) sizeBox.getSelectedItem();

			if (selectedSize == null) {

				JOptionPane.showMessageDialog(this, "Please select a task size.");
				return;
			}

			// Check that the simulation clock is set
			if (!scheduler.getClock().isSet()) {

				JOptionPane.showMessageDialog(this, "The simulation clock has not been set yet.");
				return;
			}

			// Check available GPU minutes
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

				dispose();

			} catch (IllegalArgumentException ex) {

				JOptionPane.showMessageDialog(this, ex.getMessage());

			} catch (IllegalStateException ex) {

				JOptionPane.showMessageDialog(this, ex.getMessage());
			}

		} else if (e.getSource() == cancelButton) {

			dispose();
		}
	}
}