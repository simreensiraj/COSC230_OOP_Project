package model;

import java.time.*;

// one of the 4 gpus, runs one job at a time
public class GPU {

    private final int id;
    private Job currentJob; // null = free

    public GPU(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return "GPU" + id;
    }

    public Job getCurrentJob() {
        return currentJob;
    }

    public boolean isFree() {
        return currentJob == null;
    }

    public void assign(Job job) {
        if (!isFree()) {
            throw new IllegalStateException(getName() + " is busy.");
        }
        currentJob = job;
    }

    // after the job completes or gets cancelled
    public void free() {
        currentJob = null;
    }

    // when it can take the next job: now if free, otherwise when its job finishes
    // (predictions use copies of this, they never change the real gpu)
    public LocalDateTime getFreeAt(LocalDateTime now) {
        if (isFree()) {
            return now;
        }
        return currentJob.getExpectedFinish();
    }
}
