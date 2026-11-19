package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

import model.AppData;
import model.Student;
import model.Job;

public class MyJobsPanel extends JPanel implements ActionListener {

	// Shared objects
	AppData data;
	Student student;

	// Components
	JTextField searchField;
	JButton searchButton;
	JButton refreshButton;
	JButton cancelButton;

	JTable jobsTable;
	DefaultTableModel tableModel;

	public MyJobsPanel(AppData data, Student student) {

		this.data = data;
		this.student = student;

		setLayout(new BorderLayout(10, 10));

		// Search panel
		JPanel searchPanel = new JPanel(new FlowLayout());

		searchField = new JTextField(20);
		searchButton = new JButton("Search");
		refreshButton = new JButton("Refresh");

		searchButton.addActionListener(this);
		refreshButton.addActionListener(this);

		searchPanel.add(new JLabel("Search Jobs:"));
		searchPanel.add(searchField);
		searchPanel.add(searchButton);
		searchPanel.add(refreshButton);

		add(searchPanel, BorderLayout.NORTH);

		// Jobs table
		String[] columns = { "Job ID", "Title", "Application", "Task Size", "Duration", "Status", "GPU" };

		tableModel = new DefaultTableModel(columns, 0) {

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		jobsTable = new JTable(tableModel);
		jobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(new JScrollPane(jobsTable), BorderLayout.CENTER);

		// Bottom panel
		JPanel bottomPanel = new JPanel(new FlowLayout());

		cancelButton = new JButton("Cancel Selected Job");
		cancelButton.addActionListener(this);

		bottomPanel.add(cancelButton);

		add(bottomPanel, BorderLayout.SOUTH);

		// Load student jobs
		refreshJobs();
	}

	public void refreshJobs() {

		tableModel.setRowCount(0);

		String searchText = searchField.getText().trim().toLowerCase();

		for (Job job : data.getJobs()) {

			// Only display this student's jobs
			if (job.getStudentId().equals(student.getUniversityId())) {

				String jobId = job.getJobId();
				String title = job.getTitle();
				String application = job.getApplicationName();

				// Search by ID, title, or application
				if (!searchText.isEmpty() && !jobId.toLowerCase().contains(searchText)
						&& !title.toLowerCase().contains(searchText)
						&& !application.toLowerCase().contains(searchText)) {

					continue;
				}

				String gpu = "Not Assigned";

				if (job.getAssignedGpuId() != 0) {
					gpu = String.valueOf(job.getAssignedGpuId());
				}

				Object[] row = { jobId, title, application, job.getTaskSize(), job.getEstimatedDuration() + " min",
						job.getStatus(), gpu };

				tableModel.addRow(row);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == searchButton) {

			refreshJobs();

		} else if (e.getSource() == refreshButton) {

			searchField.setText("");
			refreshJobs();

		} else if (e.getSource() == cancelButton) {

			int selectedRow = jobsTable.getSelectedRow();

			if (selectedRow == -1) {

				JOptionPane.showMessageDialog(this, "Please select a job first.");

				return;
			}

			JOptionPane.showMessageDialog(this, "Job cancellation will be connected next.");
		}
	}
}