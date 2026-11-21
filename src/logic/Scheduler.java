package logic;

import java.time.LocalDateTime;
import java.util.*;
import model.*;

public class Scheduler {
    private final SimulationClock clock;
    private final Map<String, Student> students;
    private final GPU[] gpus;
    private final List<Job> jobs;
    private final AppData data;

    public Scheduler(SimulationClock clock, AppData data) {
        this.clock = clock;
        this.data = data;
        this.students = data.getStudents();
        this.jobs = data.getJobs(); // same list DataStore saves and loads
        this.gpus = data.getGpus(); // same 4 GPUs (AppData creates them)
    }

    private TimeAllowance allowanceOf(String studentId) {
        if (!students.containsKey(studentId)) {
            throw new IllegalArgumentException("Unknown student");
        }
        return students.get(studentId).getAllowance();
    }

    public Job submit(String studentId, String title, String applicationName, String description, TaskSize size) {
        if (!clock.isSet()) {
            throw new IllegalStateException("The simulation clock has not been set yet.");
        }
        // reserve first so a refused job leaves nothing behind
        allowanceOf(studentId).reserve(size.getDuration());
        String id = data.nextJobId();
        Job job = new Job(id, studentId, title, applicationName, description, size, clock.getNow());
        jobs.add(job);
        dispatch();
        return job;
    }

    private GPU findFreeGpu() {
        for (GPU g : gpus) {
            if (g.isFree()) {
                return g;
            }
        }
        return null;
    }

    public List<Job> getQueue() {
        List<Job> queue = new ArrayList<>();
        for (Job j : jobs) {
            if (j.getStatus() == JobStatus.QUEUED) {
                queue.add(j);
            }
        }
        return queue;
    }

    private void dispatch() {
        while (true) {
            List<Job> queue = getQueue();
            GPU gpu = findFreeGpu();
            if (queue.isEmpty() || gpu == null) {
                break;
            }
            Job job = queue.get(0);
            job.start(gpu.getId(), clock.getNow());
            gpu.assign(job);
        }
    }

    public int getQueuePosition(Job job) {
        List<Job> queue = getQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i) == job) {
                return i + 1;
            }
        }
        return 0;
    }

    private void settle(Job job, int minutesRun) {
        allowanceOf(job.getStudentId()).release(job.getEstimatedDuration());
        allowanceOf(job.getStudentId()).charge(minutesRun);
    }

    public boolean advanceMinute() {
        if (!clock.isSet()) {
            return false;
        }
        clock.advance();
        LocalDateTime now = clock.getNow();
        for (GPU gpu : gpus) {
            Job job = gpu.getCurrentJob();
            if (job != null && job.getRemainingMinutes(now) <= 0) {
                job.complete(now);
                settle(job, job.getMinutesRun(now));
                gpu.free();
            }
        }
        dispatch();
        return true;
    }

    public void cancel(Job job, String by, String reason) {
        if (!(by.equals("admin") || by.equals(job.getStudentId()))) {
            throw new IllegalArgumentException("Not allowed to cancel this job.");
        }
        boolean wasRunning = job.getStatus() == JobStatus.RUNNING;
        int gpuId = job.getAssignedGpuId();
        job.cancel(by, reason, clock.getNow());
        if (wasRunning) {
            gpus[gpuId - 1].free();
            settle(job, job.getMinutesRun(clock.getNow()));
        } else {
            allowanceOf(job.getStudentId()).release(job.getEstimatedDuration());
        }
        dispatch();
    }

    private Map<Job, LocalDateTime> predictStarts() {
        Map<Job, LocalDateTime> starts = new HashMap<>();
        if (!clock.isSet()) {
            return starts;
        }
        LocalDateTime[] freeAt = new LocalDateTime[4];
        for (int i = 0; i < 4; i++) {
            freeAt[i] = gpus[i].getFreeAt(clock.getNow());
        }
        for (Job job : getQueue()) {
            int g = 0;
            for (int i = 1; i < 4; i++) {
                if (freeAt[i].isBefore(freeAt[g])) {
                    g = i;
                }
            }
            starts.put(job, freeAt[g]);
            freeAt[g] = freeAt[g].plusMinutes(job.getEstimatedDuration());
        }
        return starts;
    }

    public LocalDateTime getPredictedStart(Job job) {
        return predictStarts().get(job);
    }

    public LocalDateTime getPredictedFinish(Job job) {
        LocalDateTime start = getPredictedStart(job);
        if (start == null) {
            return null;
        }
        return start.plusMinutes(job.getEstimatedDuration());
    }

    public int getWaitMinutes(Job job) {
        LocalDateTime start = getPredictedStart(job);
        if (start == null) {
            return 0;
        }
        return TimeFormat.minutesBetween(clock.getNow(), start);
    }

    public double getUtilization() {
        int elapsed = clock.getElapsedMinutes();
        if (elapsed == 0) {
            return -1;
        }
        int busy = 0;
        for (Job j : jobs) {
            busy += j.getMinutesRun(clock.getNow());
        }
        return busy * 100.0 / (4 * elapsed);
    }

    public List<Job> getJobsOf(String studentId) {
        List<Job> result = new ArrayList<>();
        for (Job j : jobs) {
            if (j.getStudentId().equals(studentId)) {
                result.add(j);
            }
        }
        return result;
    }

    public List<Job> getAllJobs() {
        return new ArrayList<>(jobs);
    }

    public GPU[] getGpus() {
        return gpus;
    }

    public int getBusyGpuCount() {
        int count = 0;
        for (GPU g : gpus) {
            if (!g.isFree()) {
                count++;
            }
        }
        return count;
    }

    public int countByStatus(JobStatus status) {
        int count = 0;
        for (Job j : jobs) {
            if (j.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public SimulationClock getClock() {
        return clock;
    }
}
