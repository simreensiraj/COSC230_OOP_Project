
package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Locale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import logic.Scheduler;
import logic.SimulationClock;
import model.AppData;
import model.GPU;
import model.Job;
import model.JobStatus;
import model.Student;
import model.TimeAllowance;
import model.TimeFormat;

public class DashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AppData data;
    private final Scheduler scheduler;
    private final SimulationClock clock;

    private final JLabel summaryLabel;
    private final JLabel[] gpuLabels = new JLabel[4];

    private final DefaultTableModel waitingModel;
    private final DefaultTableModel completedModel;
    private final DefaultTableModel allowanceModel;

    public DashboardPanel(AppData data, Scheduler scheduler,
            SimulationClock clock) {

        this.data = data;
        this.scheduler = scheduler;
        this.clock = clock;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // System summary
        summaryLabel = new JLabel();
        summaryLabel.setBorder(
                BorderFactory.createTitledBorder("System Summary"));

        content.add(summaryLabel);

        // Four GPU status cards
        JPanel gpuPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        gpuPanel.setBorder(
                BorderFactory.createTitledBorder("GPU Status"));

        for (int i = 0; i < 4; i++) {
            gpuLabels[i] = new JLabel();
            gpuLabels[i].setVerticalAlignment(SwingConstants.TOP);
            gpuPanel.add(gpuLabels[i]);
        }

        content.add(gpuPanel);

        // Waiting jobs table
        waitingModel = createModel(new String[]{
                "Job ID", "Student", "Application", "Duration",
                "Position", "Predicted Start", "Predicted Finish",
                "Remaining Wait"
        });

        content.add(createTablePanel("Waiting Jobs", waitingModel));

        // Completed jobs table
        completedModel = createModel(new String[]{
                "Job ID", "Student", "GPU", "Submitted",
                "Started", "Finished", "Runtime",
                "Queue Wait", "Charged"
        });

        content.add(createTablePanel("Completed Jobs", completedModel));

        // Student allowances table
        allowanceModel = createModel(new String[]{
                "Student ID", "Allocated", "Used",
                "Reserved", "Available"
        });

        content.add(createTablePanel(
                "Student Allowances", allowanceModel));

        add(new JScrollPane(content), BorderLayout.CENTER);

        refreshDashboard();
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

    private JPanel createTablePanel(
            String title, DefaultTableModel model) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(850, 140));

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    public void refreshDashboard() {

        // Summary counts and utilization
        String utilization = clock.getElapsedMinutes() == 0
                ? "N/A"
                : String.format(Locale.US, "%.1f%%",
                        scheduler.getUtilization());

        summaryLabel.setText(
                "Queued: "
                + scheduler.countByStatus(JobStatus.QUEUED)
                + "     Running: "
                + scheduler.countByStatus(JobStatus.RUNNING)
                + "     Completed: "
                + scheduler.countByStatus(JobStatus.COMPLETED)
                + "     Cancelled: "
                + scheduler.countByStatus(JobStatus.CANCELLED)
                + "     Busy GPUs: "
                + scheduler.getBusyGpuCount() + "/4"
                + "     Utilization: " + utilization
        );

        // GPU status cards
        GPU[] gpus = scheduler.getGpus();

        for (int i = 0; i < gpuLabels.length; i++) {

            GPU gpu = gpus[i];
            Job job = gpu.getCurrentJob();

            if (gpu.isFree()) {

                gpuLabels[i].setText(
                        "<html><b>" + gpu.getName()
                        + "</b><br>Available"
                        + "<br>No current job</html>");

            } else {

                gpuLabels[i].setText(
                        "<html><b>" + gpu.getName()
                        + "</b><br>Busy"
                        + "<br>Job: " + job.getJobId()
                        + "<br>Remaining: "
                        + job.getRemainingMinutes(clock.getNow())
                        + " min"
                        + "<br>Finish: "
                        + TimeFormat.formatOrDash(job.getExpectedFinish())
                        + "</html>");
            }
        }

        // Waiting jobs
        waitingModel.setRowCount(0);

        for (Job job : scheduler.getQueue()) {

            waitingModel.addRow(new Object[]{
                    job.getJobId(),
                    job.getStudentId(),
                    job.getApplicationName(),
                    job.getEstimatedDuration(),
                    scheduler.getQueuePosition(job),
                    TimeFormat.formatOrDash(scheduler.getPredictedStart(job)),
                    TimeFormat.formatOrDash(scheduler.getPredictedFinish(job)),
                    scheduler.getWaitMinutes(job) + " min"
            });
        }

        // Completed jobs
        completedModel.setRowCount(0);

        for (Job job : scheduler.getAllJobs()) {

            if (job.getStatus() == JobStatus.COMPLETED) {

                int runtime = job.getMinutesRun(clock.getNow());

                int queueWait = TimeFormat.minutesBetween(
                        job.getSubmissionTime(), job.getStartTime());

                completedModel.addRow(new Object[]{
                        job.getJobId(),
                        job.getStudentId(),
                        "GPU" + job.getAssignedGpuId(),
                        TimeFormat.formatOrDash(job.getSubmissionTime()),
                        TimeFormat.formatOrDash(job.getStartTime()),
                        TimeFormat.formatOrDash(job.getFinishTime()),
                        runtime + " min",
                        queueWait + " min",
                        runtime + " min"
                });
            }
        }

        // Student GPU allowances
        allowanceModel.setRowCount(0);

        for (Student student : data.getStudents().values()) {

            TimeAllowance allowance = student.getAllowance();

            allowanceModel.addRow(new Object[]{
                    student.getUniversityId(),
                    allowance.getAllocated(),
                    allowance.getUsed(),
                    allowance.getReserved(),
                    allowance.getAvailable()
            });
        }
    }
}
