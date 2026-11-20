
package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import logic.Scheduler;
import model.Job;
import model.JobStatus;

public class AllJobsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private Scheduler scheduler;

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JTable jobsTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;

    public AllJobsPanel(Scheduler scheduler) {

        this.scheduler = scheduler;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
                10, 10, 10, 10));

        // Search and filter section
        JPanel filterPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        filterPanel.add(new JLabel("Search:"));

        searchField = new JTextField(18);
        filterPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        filterPanel.add(searchButton);

        filterPanel.add(new JLabel("Status:"));

        statusFilter = new JComboBox<>(new String[]{
                "ALL",
                "QUEUED",
                "RUNNING",
                "COMPLETED",
                "CANCELLED"
        });

        filterPanel.add(statusFilter);

        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear Filters");

        filterPanel.add(refreshButton);
        filterPanel.add(clearButton);

        add(filterPanel, BorderLayout.NORTH);

        // All Jobs table
        String[] columns = {
                "Job ID",
                "Student ID",
                "Title",
                "Application",
                "Task Size",
                "Duration (min)",
                "Status",
                "GPU",
                "Submitted",
                "Started",
                "Finished"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        jobsTable = new JTable(tableModel);

        jobsTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF);

        jobsTable.setRowHeight(25);

        jobsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(jobsTable);

        scrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "All Student GPU Jobs"));

        add(scrollPane, BorderLayout.CENTER);

        // Bottom summary
        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        countLabel = new JLabel();
        bottomPanel.add(countLabel);

        add(bottomPanel, BorderLayout.SOUTH);

        // Button actions
        searchButton.addActionListener(
                e -> refreshJobs());

        searchField.addActionListener(
                e -> refreshJobs());

        statusFilter.addActionListener(
                e -> refreshJobs());

        refreshButton.addActionListener(
                e -> refreshJobs());

        clearButton.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedItem("ALL");
            refreshJobs();
        });

        refreshJobs();
    }

    public void refreshJobs() {

        tableModel.setRowCount(0);

        String search = searchField.getText()
                .trim().toLowerCase();

        String selectedStatus =
                (String) statusFilter.getSelectedItem();

        ArrayList<Job> jobs = new ArrayList<>(
                scheduler.getAllJobs());

        jobs.sort(
                Comparator.comparing(
                        Job::getSubmissionTime,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()))
                .thenComparing(Job::getJobId));

        int displayedCount = 0;

        for (Job job : jobs) {

            // Filter by job status
            if (!"ALL".equals(selectedStatus)
                    && !job.getStatus().name()
                            .equals(selectedStatus)) {
                continue;
            }

            // Search by job ID, student ID,
            // title, or application name
            String searchableText = (
                    job.getJobId() + " " +
                    job.getStudentId() + " " +
                    job.getTitle() + " " +
                    job.getApplicationName()
                    ).toLowerCase();

            if (!searchableText.contains(search)) {
                continue;
            }

            String gpu = job.getAssignedGpuId() == 0
                    ? "-"
                    : "GPU" + job.getAssignedGpuId();

            tableModel.addRow(new Object[]{
                    job.getJobId(),
                    job.getStudentId(),
                    job.getTitle(),
                    job.getApplicationName(),
                    job.getTaskSize(),
                    job.getEstimatedDuration(),
                    job.getStatus(),
                    gpu,
                    formatTime(job.getSubmissionTime()),
                    formatTime(job.getStartTime()),
                    formatTime(job.getFinishTime())
            });

            displayedCount++;
        }

        countLabel.setText(
                "Showing " + displayedCount +
                " of " + jobs.size() + " jobs");
    }

    private String formatTime(
            java.time.LocalDateTime time) {

        if (time == null) {
            return "-";
        }

        return time.format(
                java.time.format.DateTimeFormatter
                        .ofPattern("dd/MM/yyyy HH:mm"));
    }
}
