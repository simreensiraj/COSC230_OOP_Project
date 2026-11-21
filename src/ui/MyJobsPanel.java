package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import logic.Scheduler;
import model.Student;
import model.Job;
import model.JobStatus;
import model.TimeFormat;

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
        statusBox = new JComboBox<>(new String[] {"All", "QUEUED", "RUNNING", "COMPLETED", "CANCELLED"});
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
        String[] columns = {"Job ID", "Title", "Application", "Task Size", "Duration", "Status", "GPU", "Submitted", "Start", "Finish", "Queue Position", "Wait"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobsTable = new JTable(tableModel);
        jobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(new JScrollPane(jobsTable), BorderLayout.CENTER);
        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        cancelButton = new JButton("Cancel Selected Job");
        cancelButton.addActionListener(this);
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Refresh the student's jobs
    public void refreshJobs() {
        tableModel.setRowCount(0);
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusBox.getSelectedItem();
        // Show only this student's jobs
        for (Job job : scheduler.getJobsOf(student.getUniversityId())) {
            String jobId = job.getJobId();
            String title = job.getTitle();
            String application = job.getApplicationName();
            // Search by job ID, title, or application
            if (!searchText.isEmpty() && !jobId.toLowerCase().contains(searchText) && !title.toLowerCase().contains(searchText) && !application.toLowerCase().contains(searchText)) {
                continue;
            }
            // Filter by status
            if (!selectedStatus.equals("All") && !job.getStatus().toString().equals(selectedStatus)) {
                continue;
            }
            // GPU assignment
            String gpu = "Not Assigned";
            if (job.getAssignedGpuId() != 0) {
                gpu = "GPU" + job.getAssignedGpuId();
            }
            String start = TimeFormat.formatOrDash(job.getStartTime());
            String finish = TimeFormat.formatOrDash(job.getFinishTime());
            String queuePosition = "-";
            String wait = "-";
            // Queued jobs have predicted times
            if (job.getStatus() == JobStatus.QUEUED) {
                queuePosition = String.valueOf(scheduler.getQueuePosition(job));
                LocalDateTime predictedStart = scheduler.getPredictedStart(job);
                LocalDateTime predictedFinish = scheduler.getPredictedFinish(job);
                if (predictedStart != null) {
                    start = "\u2248 " + TimeFormat.format(predictedStart);
                }
                if (predictedFinish != null) {
                    finish = "\u2248 " + TimeFormat.format(predictedFinish);
                }
                wait = scheduler.getWaitMinutes(job) + " min";
            } else if (job.getStatus() == JobStatus.RUNNING) {
                // Actual start and expected finish
                LocalDateTime expectedFinish = job.getExpectedFinish();
                if (expectedFinish != null) {
                    finish = "\u2248 " + TimeFormat.format(expectedFinish);
                }
                if (job.getStartTime() != null) {
                    int waitMinutes = TimeFormat.minutesBetween(job.getSubmissionTime(), job.getStartTime());
                    wait = waitMinutes + " min";
                }
            } else if (job.getStartTime() != null) {
                // Completed or cancelled running jobs
                int waitMinutes = TimeFormat.minutesBetween(job.getSubmissionTime(), job.getStartTime());
                wait = waitMinutes + " min";
            }
            // Add job information to table
            Object[] row = {jobId, title, application, job.getTaskSize(), job.getEstimatedDuration() + " min", job.getStatus(), gpu, TimeFormat.formatOrDash(job.getSubmissionTime()), start, finish, queuePosition, wait};
            tableModel.addRow(row);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchButton || e.getSource() == statusBox) {
            refreshJobs();
        } else if (e.getSource() == refreshButton) {
            // Refresh without clearing search or status
            refreshJobs();
        } else if (e.getSource() == cancelButton) {
            int selectedRow = jobsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a job first.");
                return;
            }
            String jobId = tableModel.getValueAt(selectedRow, 0).toString();
            Job selectedJob = null;
            // Find selected job belonging to this student
            for (Job job : scheduler.getJobsOf(student.getUniversityId())) {
                if (job.getJobId().equals(jobId)) {
                    selectedJob = job;
                    break;
                }
            }
            if (selectedJob == null) {
                JOptionPane.showMessageDialog(this, "Job not found.");
                return;
            }
            // Only queued or running jobs can be cancelled
            if (selectedJob.getStatus() != JobStatus.QUEUED && selectedJob.getStatus() != JobStatus.RUNNING) {
                JOptionPane.showMessageDialog(this, "This job cannot be cancelled.");
                return;
            }
            // Check simulation clock
            if (!scheduler.getClock().isSet()) {
                JOptionPane.showMessageDialog(this, "Simulation clock is not set.");
                return;
            }
            // Calculate charge before cancellation
            int charge = 0;
            if (selectedJob.getStatus() == JobStatus.RUNNING) {
                charge = selectedJob.getMinutesRun(scheduler.getClock().getNow());
            }
            // Ask for cancellation reason
            String reason = JOptionPane.showInputDialog(this, "Enter cancellation reason:");
            if (reason == null) {
                return;
            }
            reason = reason.trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cancellation reason is required.");
                return;
            }
            // Confirm cancellation and show charge
            int confirm = JOptionPane.showConfirmDialog(this, "Cancel job " + jobId + "?" + "\nGPU minutes charged: " + charge + "\nReason: " + reason, "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                // Cancel through shared scheduler
                scheduler.cancel(selectedJob, student.getUniversityId(), reason);
                JOptionPane.showMessageDialog(this, "Job cancelled successfully.");
                // Update table after cancellation
                refreshJobs();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}
