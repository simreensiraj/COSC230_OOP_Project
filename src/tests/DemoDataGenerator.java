package tests;

import java.util.List;
import logic.AccountService;
import logic.Scheduler;
import logic.SimulationClock;
import model.AccessRequest;
import model.Administrator;
import model.AppData;
import model.Job;
import model.JobStatus;
import model.TaskSize;
import storage.DataStore;

// builds the demo data in data/ through the real classes. close the app first, it overwrites the files
// 5 approved students, 2 pending and 1 rejected request, 20 jobs: 6 completed, 6 cancelled, 4 running, 4 queued
public class DemoDataGenerator {
    public static void main(String[] args) throws Exception {
        AppData data = new AppData();
        SimulationClock clock = new SimulationClock();
        clock.setStart(2026, 11, 21, 10, 0);
        AccountService accounts = new AccountService(data);
        Scheduler scheduler = new Scheduler(clock, data);
        Administrator admin = new Administrator();
        // 5 approved students, 500 minutes each, password Demo12345
        String[] ids = {"100011", "100012", "100013", "100014", "100015"};
        String[] names = {"Ahmed Ali", "Sara Hassan", "Omar Khalid", "Fatima Mohammed", "Noora Salem"};
        for (int i = 0; i < ids.length; i++) {
            AccessRequest request = accounts.register(ids[i], names[i], "demo" + (i + 1) + "@example.com", "050000000" + (i + 1), "Demo12345");
            accounts.approve(admin, request, 500);
        }
        // 2 pending requests and 1 rejected
        accounts.register("100016", "Khalid Saeed", "demo6@example.com", "0500000006", "Demo12345");
        accounts.register("100017", "Maha Abdullah", "demo7@example.com", "0500000007", "Demo12345");
        AccessRequest rejected = accounts.register("100018", "Yousef Rashid", "demo8@example.com", "0500000008", "Demo12345");
        accounts.reject(admin, rejected);
        // 8 jobs: cancel the first 2 in the queue, then +1 minute until the other 6 complete
        for (int i = 0; i < 8; i++) {
            submit(scheduler, ids, i);
        }
        List<Job> queue = scheduler.getQueue();
        scheduler.cancel(queue.get(0), "admin", "Demo queued cancellation");
        scheduler.cancel(queue.get(1), "admin", "Demo queued cancellation");
        while (scheduler.countByStatus(JobStatus.COMPLETED) < 6) {
            scheduler.advanceMinute();
        }
        // 4 jobs that start on a free gpu and get cancelled
        for (int i = 8; i < 12; i++) {
            Job job = submit(scheduler, ids, i);
            scheduler.cancel(job, "admin", "Demo running cancellation");
        }
        // 8 more, so all 4 gpus are busy and 4 jobs wait
        for (int i = 12; i < 20; i++) {
            submit(scheduler, ids, i);
        }
        new DataStore("data").save(data, clock);
        System.out.println("saved to data/ - completed " + scheduler.countByStatus(JobStatus.COMPLETED) + ", cancelled " + scheduler.countByStatus(JobStatus.CANCELLED) + ", running " + scheduler.countByStatus(JobStatus.RUNNING) + ", queued " + scheduler.countByStatus(JobStatus.QUEUED));
    }

    // students and sizes take turns
    private static Job submit(Scheduler scheduler, String[] ids, int i) {
        TaskSize size = TaskSize.values()[i % 4];
        String application = i % 2 == 0 ? "MATLAB" : "Python";
        return scheduler.submit(ids[i % ids.length], "Demo GPU Job " + (i + 1), application, "GPU computing demonstration task", size);
    }
}
