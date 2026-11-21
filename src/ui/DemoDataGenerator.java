
package ui;

import java.io.IOException;
import java.util.ArrayList;

import logic.Scheduler;
import logic.SimulationClock;
import model.AccessRequest;
import model.Administrator;
import model.AppData;
import model.Job;
import model.JobStatus;
import model.Student;
import model.TaskSize;
import storage.DataStore;

public class DemoDataGenerator {

    public static void main(String[] args) {

        try {
            AppData data = new AppData();
            SimulationClock clock = new SimulationClock();

            clock.setStart(2026, 11, 21, 10, 0);

            Administrator admin = new Administrator();
            Scheduler scheduler = new Scheduler(clock, data);

            // Five approved students
            String[] names = {
                    "Ahmed Ali",
                    "Sara Hassan",
                    "Omar Khalid",
                    "Fatima Mohammed",
                    "Noora Salem"
            };

            String[] ids = {
                    "100011", "100012", "100013",
                    "100014", "100015"
            };

            for (int i = 0; i < ids.length; i++) {

                AccessRequest request = new AccessRequest(
                        ids[i],
                        names[i],
                        "demo" + (i + 1) + "@example.com",
                        "050000000" + (i + 1),
                        "Demo12345"
                );

                Student student = admin.approve(request, 500);

                data.getRequests().add(request);
                data.getStudents().put(ids[i], student);
            }

            // Two pending registration requests
            data.getRequests().add(new AccessRequest(
                    "100016", "Khalid Saeed",
                    "demo6@example.com",
                    "0500000006", "Demo12345"));

            data.getRequests().add(new AccessRequest(
                    "100017", "Maha Abdullah",
                    "demo7@example.com",
                    "0500000007", "Demo12345"));

            // One rejected registration request
            AccessRequest rejected = new AccessRequest(
                    "100018", "Yousef Rashid",
                    "demo8@example.com",
                    "0500000008", "Demo12345");

            admin.reject(rejected);
            data.getRequests().add(rejected);

            // Use the task sizes defined by the team
            TaskSize[] sizes = TaskSize.values();

            // Stage 1: Submit eight jobs
            for (int i = 0; i < 8; i++) {
                submitDemoJob(
                        scheduler, ids, sizes, i);
            }

            // Cancel two queued jobs
            ArrayList<Job> waiting =
                    new ArrayList<>(scheduler.getQueue());

            scheduler.cancel(waiting.get(0), "admin",
                    "Demo queued cancellation");

            scheduler.cancel(waiting.get(1), "admin",
                    "Demo queued cancellation");

            // Complete the remaining six jobs
            int steps = 0;

            while (scheduler.countByStatus(
                    JobStatus.COMPLETED) < 6) {

                scheduler.advanceMinute();
                steps++;

                if (steps > 10000) {
                    throw new IllegalStateException(
                            "Jobs did not finish as expected.");
                }
            }

            // Stage 2: Submit four jobs and cancel them
            for (int i = 8; i < 12; i++) {

                Job job = submitDemoJob(
                        scheduler, ids, sizes, i);

                scheduler.cancel(job, "admin",
                        "Demo running cancellation");
            }

            // Stage 3: Eight jobs remain active
            // Four running and four waiting
            for (int i = 12; i < 20; i++) {
                submitDemoJob(
                        scheduler, ids, sizes, i);
            }

            // Validate the demonstration scenario
            check(data.getStudents().size() == 5,
                    "Expected five students");

            check(data.getJobs().size() == 20,
                    "Expected twenty jobs");

            check(scheduler.countByStatus(
                    JobStatus.COMPLETED) == 6,
                    "Completed job count is incorrect");

            check(scheduler.countByStatus(
                    JobStatus.CANCELLED) == 6,
                    "Cancelled job count is incorrect");

            check(scheduler.countByStatus(
                    JobStatus.RUNNING) == 4,
                    "Running job count is incorrect");

            check(scheduler.countByStatus(
                    JobStatus.QUEUED) == 4,
                    "Queued job count is incorrect");

            // Store demonstration data in data/
            DataStore store = new DataStore("data");
            store.save(data, clock);

            // Confirm the files can be loaded again
            SimulationClock restoredClock =
                    new SimulationClock();

            AppData restored = store.load(restoredClock);

            check(restored.getStudents().size() == 5,
                    "Saved students could not be restored");

            check(restored.getJobs().size() == 20,
                    "Saved jobs could not be restored");

            System.out.println("DEMO DATA CREATED SUCCESSFULLY");
            System.out.println("Folder: data");
            System.out.println("Students: " +
                    restored.getStudents().size());
            System.out.println("Requests: " +
                    restored.getRequests().size());
            System.out.println("Jobs: " +
                    restored.getJobs().size());

            System.out.println("Completed: " +
                    scheduler.countByStatus(JobStatus.COMPLETED));

            System.out.println("Cancelled: " +
                    scheduler.countByStatus(JobStatus.CANCELLED));

            System.out.println("Running: " +
                    scheduler.countByStatus(JobStatus.RUNNING));

            System.out.println("Queued: " +
                    scheduler.countByStatus(JobStatus.QUEUED));

        } catch (Exception ex) {
            System.err.println(
                    "Demo data generation failed: " +
                    ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static Job submitDemoJob(
            Scheduler scheduler,
            String[] ids,
            TaskSize[] sizes,
            int index) {

        String studentId = ids[index % ids.length];

        TaskSize size = sizes[index % sizes.length];

        return scheduler.submit(
                studentId,
                "Demo GPU Job " + (index + 1),
                index % 2 == 0 ? "MATLAB" : "Python",
                "GPU computing demonstration task",
                size
        );
    }

    private static void check(
            boolean condition, String message) {

        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
