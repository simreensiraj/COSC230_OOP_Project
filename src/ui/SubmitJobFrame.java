package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import model.TaskSize;

public class SubmitJobFrame extends JFrame implements ActionListener {

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

	public SubmitJobFrame() {

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

			// Temporary message until submission is connected
			JOptionPane.showMessageDialog(this, "Form validated. Job submission will be connected next.");

		} else if (e.getSource() == cancelButton) {

			dispose();
		}
	}

	// Temporary main method for testing
	public static void main(String[] args) {

		new SubmitJobFrame();
	}
}