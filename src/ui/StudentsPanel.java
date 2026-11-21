
package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import logic.AccountService;
import logic.Scheduler;
import model.Administrator;
import model.AppData;
import model.Job;
import model.JobStatus;
import model.Student;
import model.TimeAllowance;

public class StudentsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AppData data;
    private final AccountService accountService;
    private final Administrator admin;
    private final Scheduler scheduler;

    private final JTextField searchField;
    private final JTable studentTable;
    private final JTable jobTable;

    private final DefaultTableModel studentModel;
    private final DefaultTableModel jobModel;

    private final ArrayList<Student> displayedStudents =
            new ArrayList<>();

    private final ArrayList<Job> displayedJobs =
            new ArrayList<>();

    public StudentsPanel(AppData data,
            AccountService accountService,
            Administrator admin,
            Scheduler scheduler) {

        this.data = data;
        this.accountService = accountService;
        this.admin = admin;
        this.scheduler = scheduler;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
                10, 10, 10, 10));

        // Search section
        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        searchPanel.add(new JLabel("Search University ID:"));

        searchField = new JTextField(18);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        add(searchPanel, BorderLayout.NORTH);

        // Students table
        studentModel = createModel(new String[]{
                "University ID", "Name", "Email", "Phone",
                "Allocated", "Used", "Reserved", "Available"
        });

        studentTable = new JTable(studentModel);
        studentTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        JPanel studentsSection = new JPanel(new BorderLayout());
        studentsSection.setBorder(
                BorderFactory.createTitledBorder("Students"));

        studentsSection.add(
                new JScrollPane(studentTable),
                BorderLayout.CENTER);

        // Student jobs table
        jobModel = createModel(new String[]{
                "Job ID", "Title", "Status",
                "GPU", "Duration"
        });

        jobTable = new JTable(jobModel);
        jobTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        JPanel jobsSection = new JPanel(new BorderLayout());
        jobsSection.setBorder(
                BorderFactory.createTitledBorder(
                        "Selected Student's Jobs"));

        jobsSection.add(
                new JScrollPane(jobTable),
                BorderLayout.CENTER);

        JPanel tables = new JPanel(new GridLayout(2, 1, 10, 10));
        tables.add(studentsSection);
        tables.add(jobsSection);

        add(tables, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        JButton grantButton =
                new JButton("Grant Extra Minutes");

        JButton cancelButton =
                new JButton("Cancel Selected Job");

        JButton refreshButton =
                new JButton("Refresh");

        actionPanel.add(grantButton);
        actionPanel.add(cancelButton);
        actionPanel.add(refreshButton);

        add(actionPanel, BorderLayout.SOUTH);

        // Search actions
        searchButton.addActionListener(
                e -> refreshStudents());

        searchField.addActionListener(
                e -> refreshStudents());

        showAllButton.addActionListener(e -> {
            searchField.setText("");
            refreshStudents();
        });

        // Display jobs when a student is selected
        studentTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        refreshJobs();
                    }
                });

        grantButton.addActionListener(
                e -> grantMinutes());

        cancelButton.addActionListener(
                e -> cancelJob());

        refreshButton.addActionListener(
                e -> refreshStudents());

        refreshStudents();
    }

    private DefaultTableModel createModel(String[] columns) {

        return new DefaultTableModel(columns, 0) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private Student getSelectedStudent() {

        int row = studentTable.getSelectedRow();

        if (row < 0 || row >= displayedStudents.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student first.");
            return null;
        }

        return displayedStudents.get(row);
    }

    public void refreshStudents() {

        String filter = searchField.getText().trim();

        String selectedId = null;
        int selectedRow = studentTable.getSelectedRow();

        if (selectedRow >= 0 &&
                selectedRow < displayedStudents.size()) {

            selectedId = displayedStudents.get(
                    selectedRow).getUniversityId();
        }

        displayedStudents.clear();
        studentModel.setRowCount(0);
        jobModel.setRowCount(0);
        displayedJobs.clear();

        ArrayList<Student> students = new ArrayList<>(
                data.getStudents().values());

        students.sort(Comparator.comparing(
                Student::getUniversityId));

        int rowToSelect = -1;

        for (Student student : students) {

            if (!student.getUniversityId().contains(filter)) {
                continue;
            }

            TimeAllowance a = student.getAllowance();

            displayedStudents.add(student);

            studentModel.addRow(new Object[]{
                    student.getUniversityId(),
                    student.getName(),
                    student.getEmail(),
                    student.getPhone(),
                    a.getAllocated(),
                    a.getUsed(),
                    a.getReserved(),
                    a.getAvailable()
            });

            if (student.getUniversityId().equals(selectedId)) {
                rowToSelect = displayedStudents.size() - 1;
            }
        }

        if (rowToSelect >= 0) {
            studentTable.setRowSelectionInterval(
                    rowToSelect, rowToSelect);
        } else {
            refreshJobs();
        }
    }

    private void refreshJobs() {

        displayedJobs.clear();
        jobModel.setRowCount(0);

        int row = studentTable.getSelectedRow();

        if (row < 0 || row >= displayedStudents.size()) {
            return;
        }

        Student student = displayedStudents.get(row);

        for (Job job : scheduler.getJobsOf(
                student.getUniversityId())) {

            displayedJobs.add(job);

            String gpu = job.getAssignedGpuId() == 0
                    ? "-"
                    : "GPU" + job.getAssignedGpuId();

            jobModel.addRow(new Object[]{
                    job.getJobId(),
                    job.getTitle(),
                    job.getStatus(),
                    gpu,
                    job.getEstimatedDuration() + " min"
            });
        }
    }

    private void grantMinutes() {

        Student student = getSelectedStudent();

        if (student == null) {
            return;
        }

        JSpinner minutesSpinner = new JSpinner(
                new SpinnerNumberModel(20, 1, 100000, 1));

        JTextField reasonField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));

        panel.add(new JLabel("Extra minutes:"));
        panel.add(minutesSpinner);

        panel.add(new JLabel("Reason:"));
        panel.add(reasonField);

        int choice = JOptionPane.showConfirmDialog(
                this, panel, "Grant GPU Minutes",
                JOptionPane.OK_CANCEL_OPTION);

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            minutesSpinner.commitEdit();

            int minutes = (Integer) minutesSpinner.getValue();
            String reason = reasonField.getText().trim();

            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "A reason is required.");
                return;
            }

            if (Main.clock == null || !Main.clock.isSet()) {
                JOptionPane.showMessageDialog(this,
                        "Set the simulation clock first.");
                return;
            }

            accountService.grantTime(
                    admin, student, minutes,
                    reason, Main.clock.getNow());

            Main.save();
            refreshStudents();

            JOptionPane.showMessageDialog(this,
                    "GPU minutes granted successfully.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Grant Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelJob() {

        Student student = getSelectedStudent();

        if (student == null) {
            return;
        }

        int row = jobTable.getSelectedRow();

        if (row < 0 || row >= displayedJobs.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a job first.");
            return;
        }

        Job job = displayedJobs.get(row);

        if (job.getStatus() != JobStatus.QUEUED &&
                job.getStatus() != JobStatus.RUNNING) {

            JOptionPane.showMessageDialog(this,
                    "Only queued or running jobs can be cancelled.");
            return;
        }

        String reason = JOptionPane.showInputDialog(
                this, "Enter cancellation reason:");

        if (reason == null) {
            return;
        }

        reason = reason.trim();

        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "A cancellation reason is required.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel job " + job.getJobId() + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            scheduler.cancel(job, "admin", reason);

            Main.save();
            refreshStudents();

            JOptionPane.showMessageDialog(this,
                    "Job cancelled successfully.");

        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Cancellation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
