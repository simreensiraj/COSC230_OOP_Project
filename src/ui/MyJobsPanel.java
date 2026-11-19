package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

import logic.Scheduler;
import model.Student;
import model.Job;
import model.JobStatus;

public class MyJobsPanel extends JPanel implements ActionListener {

    // Shared objects
    Scheduler scheduler;
    Student student;

    // Components
    JTextField searchField;
    JComboBox<String> statusBox;
    JButton searchButton;
    JButton refreshButton;
    JButton cancelButton;

    JTable jobsTable;
    DefaultTableModel tableModel;

    public MyJobsPanel(Scheduler scheduler, Student student) {

        this.scheduler = scheduler;
        this.student = student;

        setLayout(new BorderLayout(10, 10));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout());

        searchField = new JTextField(20);

        statusBox = new JComboBox<String>(
            new String[] {
                "All", "QUEUED", "RUNNING",
                "COMPLETED", "CANCELLED"
            }
        );

        searchButton = new JButton("Search");
        refreshButton = new JButton("Refresh");

        searchButton.addActionListener(this);
        refreshButton.addActionListener(this);
        statusBox.addActionListener(this);

        searchPanel.add(new JLabel("Search Jobs:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Status:"));
        searchPanel.add(statusBox);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        add(searchPanel, BorderLayout.NORTH);

        // Jobs table
        String[] columns = {
            "Job ID", "Title", "Application",
            "Task Size", "Duration", "Status", "GPU"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        jobsTable = new JTable(tableModel);
        jobsTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION);

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

        String searchText =
            searchField.getText().trim().toLowerCase();

        String selectedStatus =
            (String) statusBox.getSelectedItem();

        for (Job job :
                scheduler.getJobsOf(student.getUniversityId())) {

            String jobId = job.getJobId();
            String title = job.getTitle();
            String application = job.getApplicationName();

            // Search by ID, title, or application
            if (!searchText.isEmpty()
                    && !jobId.toLowerCase().contains(searchText)
                    && !title.toLowerCase().contains(searchText)
                    && !application.toLowerCase().contains(searchText)) {

                continue;
            }

            // Filter by job status
            if (!selectedStatus.equals("All")
                    && !job.getStatus().toString()
                        .equals(selectedStatus)) {

                continue;
            }

            String gpu = "Not Assigned";

            if (job.getAssignedGpuId() != 0) {
                gpu = String.valueOf(job.getAssignedGpuId());
            }

            Object[] row = {
                jobId,
                title,
                application,
                job.getTaskSize(),
                job.getEstimatedDuration() + " min",
                job.getStatus(),
                gpu
            };

            tableModel.addRow(row);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == searchButton
                || e.getSource() == statusBox) {

            refreshJobs();

        } else if (e.getSource() == refreshButton) {

            searchField.setText("");
            statusBox.setSelectedItem("All");
            refreshJobs();

        } else if (e.getSource() == cancelButton) {

            int selectedRow = jobsTable.getSelectedRow();

            if (selectedRow == -1) {

                JOptionPane.showMessageDialog(
                    this, "Please select a job first.");

                return;
            }

            String jobId = tableModel.getValueAt(
                selectedRow, 0).toString();

            Job selectedJob = null;

            // Find the selected student's job
            for (Job job :
                    scheduler.getJobsOf(student.getUniversityId())) {

                if (job.getJobId().equals(jobId)) {
                    selectedJob = job;
                    break;
                }
            }

            if (selectedJob == null) {

                JOptionPane.showMessageDialog(
                    this, "Job not found.");

                return;
            }

            // Only queued or running jobs can be cancelled
            if (selectedJob.getStatus() != JobStatus.QUEUED
                    && selectedJob.getStatus() != JobStatus.RUNNING) {

                JOptionPane.showMessageDialog(
                    this, "This job cannot be cancelled.");

                return;
            }

            // Ask for cancellation reason
            String reason = JOptionPane.showInputDialog(
                this, "Enter cancellation reason:");

            if (reason == null) {
                return;
            }

            reason = reason.trim();

            if (reason.isEmpty()) {

                JOptionPane.showMessageDialog(
                    this, "Cancellation reason is required.");

                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel job " + jobId + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {

                scheduler.cancel(
                    selectedJob,
                    student.getUniversityId(),
                    reason
                );

                JOptionPane.showMessageDialog(
                    this, "Job cancelled successfully.");

                refreshJobs();

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                    this, ex.getMessage());

            } catch (IllegalStateException ex) {

                JOptionPane.showMessageDialog(
                    this, ex.getMessage());
            }
        }
    }
}