package model;

import java.time.*;

// one submitted job. the student only types title, application, description and size,
// the system fills in the rest
// allowed moves: QUEUED -> RUNNING/CANCELLED, RUNNING -> COMPLETED/CANCELLED
// anything else throws, so a finished job can't restart or get charged twice
public class Job implements Storable {

    private String jobId;
    private String studentId;
    private String title;
    private String applicationName;
    private String description;
    private TaskSize taskSize;
    private int workloadUnits;
    private int estimatedDuration;
    private LocalDateTime submissionTime;
    private JobStatus status;
    private int assignedGpuId;          // 0 until it starts
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private String cancelReason;

    public Job(String jobId, String studentId, String title, String applicationName,
            String description, TaskSize taskSize, LocalDateTime submissionTime) {
        this.jobId = jobId;
        this.studentId = studentId;
        this.title = title;
        this.applicationName = applicationName;
        this.description = description;
        this.taskSize = taskSize;
        this.workloadUnits = taskSize.getWorkloadUnits();
        this.estimatedDuration = taskSize.getDuration();
        this.submissionTime = submissionTime;
        this.status = JobStatus.QUEUED;
    }

    public void start(int gpuId, LocalDateTime time) {
        if (status != JobStatus.QUEUED) {
            throw new IllegalStateException("Only a queued job can start.");
        }
        status = JobStatus.RUNNING;
        assignedGpuId = gpuId;
        startTime = time;
    }

    public void complete(LocalDateTime time) {
        if (status != JobStatus.RUNNING) {
            throw new IllegalStateException("Only a running job can complete.");
        }
        status = JobStatus.COMPLETED;
        finishTime = time;
    }

    // works for both student and admin cancels, reason is required
    public void cancel(String by, String reason, LocalDateTime time) {
        if (status != JobStatus.QUEUED && status != JobStatus.RUNNING) {
            throw new IllegalStateException("Only a queued or running job can be cancelled.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A reason is required.");
        }
        if (status == JobStatus.RUNNING) {
            finishTime = time;
        }
        status = JobStatus.CANCELLED;
        cancelledAt = time;
        cancelledBy = by;
        cancelReason = reason.trim();
    }

    // minutes it spent on a gpu (not waiting). this is also what gets charged
    public int getMinutesRun(LocalDateTime now) {
        if (startTime == null) {
            return 0;
        }
        if (status == JobStatus.RUNNING) {
            return TimeFormat.minutesBetween(startTime, now);
        }
        return TimeFormat.minutesBetween(startTime, finishTime);
    }

    public int getRemainingMinutes(LocalDateTime now) {
        if (status == JobStatus.QUEUED) {
            return estimatedDuration;
        }
        if (status == JobStatus.RUNNING) {
            return estimatedDuration - getMinutesRun(now);
        }
        return 0;
    }

    // start + duration, null if it hasn't started
    public LocalDateTime getExpectedFinish() {
        if (startTime == null) {
            return null;
        }
        return startTime.plusMinutes(estimatedDuration);
    }

    public String getJobId() {
        return jobId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getTitle() {
        return title;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getDescription() {
        return description;
    }

    public TaskSize getTaskSize() {
        return taskSize;
    }

    public int getWorkloadUnits() {
        return workloadUnits;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    public JobStatus getStatus() {
        return status;
    }

    public int getAssignedGpuId() {
        return assignedGpuId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    @Override
    public String toLine() {
        return Storable.join(jobId, studentId, title, applicationName, description,
                taskSize.name(), TimeFormat.format(submissionTime), status.name(),
                String.valueOf(assignedGpuId), TimeFormat.format(startTime),
                TimeFormat.format(finishTime), TimeFormat.format(cancelledAt),
                cancelledBy, cancelReason);
    }

    public static Job fromLine(String line) {
        String[] v = Storable.split(line, 14);
        Job job = new Job(v[0], v[1], v[2], v[3], v[4], TaskSize.valueOf(v[5]),
                TimeFormat.parse(v[6]));
        job.status = JobStatus.valueOf(v[7]);
        job.assignedGpuId = Integer.parseInt(v[8]);
        job.startTime = TimeFormat.parse(v[9]);
        job.finishTime = TimeFormat.parse(v[10]);
        job.cancelledAt = TimeFormat.parse(v[11]);
        if (!v[12].isEmpty()) {
            job.cancelledBy = v[12];
            job.cancelReason = v[13];
        }
        return job;
    }
}
