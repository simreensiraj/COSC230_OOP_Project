package tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import logic.AccountService;
import logic.Scheduler;
import logic.SimulationClock;
import logic.ValidationException;
import model.AccessRequest;
import model.Administrator;
import model.AppData;
import model.Job;
import model.JobStatus;
import model.RequestStatus;
import model.TaskSize;
import model.TimeAllowance;
import model.TimeFormat;
import storage.DataStore;

// the guide's 10:20 example run through the real Scheduler and AccountService
// (the same calls the windows make), plus cancellations, limits and validation
// run it as a java application, prints PASS / FAIL for each check
public class ScenarioTest {
    static int failed = 0;
    static AppData data = new AppData();
    static SimulationClock clock = new SimulationClock();
    static Scheduler s = new Scheduler(clock, data);
    static AccountService acc = new AccountService(data);
    static Administrator admin = new Administrator();

    public static void main(String[] args) throws Exception {
        setUp();
        beforeClockSet();
        guideExample();
        saveAndReload();
        predictions();
        cancellations();
        allowanceLimits();
        registrationAndLogin();
        System.out.println(failed == 0 ? "\nALL PASSED" : "\n" + failed + " FAILED");
    }

    // five approved students with 100 min, two pending requests, one rejected
    static void setUp() throws ValidationException {
        String[][] st = {{"100031025", "Ali Hassan"}, {"100052210", "Sara Ahmed"}, {"100044318", "Hamad Saeed"}, {"100039874", "Mariam Khalid"}, {"100047391", "Noura Salem"}};
        int n = 1;
        for (String[] r : st) {
            String email = r[1].toLowerCase().replace(' ', '.') + "@ku.ac.ae";
            acc.approve(admin, acc.register(r[0], r[1], email, "050123450" + n++, "gpu12345"), AccountService.DEFAULT_MINUTES);
        }
        acc.register("100061234", "Khalid Omar", "khalid.omar@ku.ac.ae", "0501234506", "gpu12345");
        acc.register("100063377", "Aisha Rashid", "aisha.rashid@ku.ac.ae", "0501234507", "gpu12345");
        acc.reject(admin, acc.register("100065555", "Fatima Ali", "fatima.ali@ku.ac.ae", "0501234508", "gpu12345"));
    }

    static void beforeClockSet() {
        System.out.println("-- before the clock is set");
        check("submit before the clock is set", "The simulation clock has not been set yet.", error(() -> s.submit("100031025", "x", "x", "x", TaskSize.SMALL)));
        check("+1 minute before the clock is set", false, s.advanceMinute());
        clock.setStart(2026, 11, 16, 10, 0);
        check("utilization at time zero", "N/A", s.getUtilization() < 0 ? "N/A" : s.getUtilization());
    }

    // jobs submitted at their times while pressing +1 minute, then compared at 10:20
    static void guideExample() {
        System.out.println("-- guide example at 10:20");
        s.submit("100031025", "Notebook warm-up", "Jupyter", "Check the environment", TaskSize.SMALL);
        s.submit("100052210", "Data cleaning", "Pandas", "Clean the survey data", TaskSize.MEDIUM);
        s.submit("100044318", "Genome alignment", "BLAST", "Align sample reads", TaskSize.MEDIUM);
        s.submit("100039874", "Weather model run", "WRF", "Two-day forecast", TaskSize.LARGE);
        goTo(10, 8);
        s.submit("100031025", "Model evaluation", "Jupyter", "Evaluate the trained model", TaskSize.LARGE);
        goTo(10, 10);
        s.submit("100052210", "CNN training", "PyTorch", "Train the image classifier", TaskSize.MEDIUM);
        goTo(10, 12);
        s.submit("100044318", "Protein folding", "AlphaFold", "Fold the test protein", TaskSize.LARGE);
        goTo(10, 16);
        s.submit("100047391", "Speech model tuning", "TensorFlow", "Fine-tune the speech model", TaskSize.MEDIUM);
        goTo(10, 18);
        s.submit("100052210", "Image resize", "OpenCV", "Resize the dataset images", TaskSize.SMALL);
        goTo(10, 19);
        s.submit("100044318", "Quick test", "BLAST", "Test run", TaskSize.SMALL);
        s.cancel(job("J110"), "100044318", "Submitted by mistake");
        goTo(10, 20);
        check("GPU1", "J105, 18 min left, ends 10:38", gpu(1));
        check("GPU2", "J106, 10 min left, ends 10:30", gpu(2));
        check("GPU3", "J107, 25 min left, ends 10:45", gpu(3));
        check("GPU4", "J104, 10 min left, ends 10:30", gpu(4));
        check("J108 in the queue", "#1, start 10:30, wait 10", queue(job("J108")));
        check("J109 in the queue", "#2, start 10:30, wait 10", queue(job("J109")));
        check("utilization", "96.3%", String.format(Locale.US, "%.1f%%", s.getUtilization()));
        check("Ali balance", "100/5/30/65", balance("100031025"));
        check("Sara balance", "100/15/20/65", balance("100052210"));
        check("Hamad balance", "100/15/30/55", balance("100044318"));
        check("J110 cancelled while queued", "CANCELLED, charged 0", job("J110").getStatus() + ", charged " + job("J110").getMinutesRun(clock.getNow()));
        check("running/queued/completed/cancelled, busy", "4/2/3/1, 4 of 4", s.countByStatus(JobStatus.RUNNING) + "/" + s.countByStatus(JobStatus.QUEUED) + "/" + s.countByStatus(JobStatus.COMPLETED) + "/" + s.countByStatus(JobStatus.CANCELLED) + ", " + s.getBusyGpuCount() + " of 4");
    }

    // save at 10:20, load into a new clock and AppData like a restart
    static void saveAndReload() throws Exception {
        System.out.println("-- save, close, reopen");
        Path folder = Files.createTempDirectory("gpu");
        new DataStore(folder.toString()).save(data, clock);
        SimulationClock c2 = new SimulationClock();
        AppData d2 = new DataStore(folder.toString()).load(c2);
        Scheduler s2 = new Scheduler(c2, d2);
        Job j105 = find(d2, "J105"), j108 = find(d2, "J108");
        check("clock after reload", "16/11/2026 10:20, 20 min elapsed", TimeFormat.format(c2.getNow()) + ", " + c2.getElapsedMinutes() + " min elapsed");
        check("J105 after reload", "GPU1, 18 min left", "GPU" + j105.getAssignedGpuId() + ", " + j105.getRemainingMinutes(c2.getNow()) + " min left");
        check("J108 wait after reload", 10, s2.getWaitMinutes(j108));
        TimeAllowance h = d2.getStudents().get("100044318").getAllowance();
        check("Hamad balance after reload", "100/15/30/55", h.getAllocated() + "/" + h.getUsed() + "/" + h.getReserved() + "/" + h.getAvailable());
        long pending = d2.getRequests().stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count();
        check("requests after reload (all / pending)", "8 / 2", d2.getRequests().size() + " / " + pending);
    }

    // the guide's worked prediction: J111 (Large) at 10:20 goes to GPU4 again
    static void predictions() {
        System.out.println("-- predictions");
        s.submit("100039874", "Climate downscaling", "WRF", "Downscale the forecast", TaskSize.LARGE);
        check("J108 / J109 / J111 waits", "10 / 10 / 15", waits());
        check("J111 predicted start", "10:35", hm(s.getPredictedStart(job("J111"))));
        s.advanceMinute();
        check("waits after +1 minute", "9 / 9 / 14", waits());
    }

    static void cancellations() {
        System.out.println("-- cancellations");
        check("Sara cancels Hamad's job", "Not allowed to cancel this job.", error(() -> s.cancel(job("J107"), "100052210", "test")));
        s.cancel(job("J107"), "admin", "GPU maintenance");
        check("admin cancels running J107 at 10:21 (Hamad charged 6)", "100/21/0/79", balance("100044318"));
        check("next job starts on the freed GPU3", "J108 on GPU3 at 10:21", "J108 on GPU" + job("J108").getAssignedGpuId() + " at " + hm(job("J108").getStartTime()));
        check("cancel a cancelled job again", "Only a queued or running job can be cancelled.", error(() -> s.cancel(job("J107"), "admin", "again")));
        check("restart a completed job", true, !error(() -> job("J101").start(1, clock.getNow())).equals("no error"));
    }

    // Ali has 65 available here
    static void allowanceLimits() {
        System.out.println("-- allowance limits");
        s.submit("100031025", "Long training", "PyTorch", "Full training run", TaskSize.VERY_LARGE);
        check("Ali submits 60 of 65", "100/5/90/5", balance("100031025"));
        s.submit("100031025", "Small check", "Jupyter", "Exact match", TaskSize.SMALL);
        check("Ali submits 5 of 5 (exact match)", "100/5/95/0", balance("100031025"));
        check("Ali submits with 0 available", true, !error(() -> s.submit("100031025", "One more", "Jupyter", "x", TaskSize.SMALL)).equals("no error"));
        acc.grantTime(admin, data.getStudents().get("100031025"), 20, "Extra runs for final report", clock.getNow());
        check("admin grants Ali 20 min", "120/5/95/20", balance("100031025"));
        check("grant without a reason", "A reason is required.", error(() -> acc.grantTime(admin, data.getStudents().get("100031025"), 5, " ", clock.getNow())));
    }

    static void registrationAndLogin() {
        System.out.println("-- registration and login");
        check("empty ID", "Enter your university ID.", invalid(() -> acc.register("", "A", "a@ku.ac.ae", "0501234567", "p")));
        check("letters in ID", "University ID must be digits only.", invalid(() -> acc.register("10A", "A", "a@ku.ac.ae", "0501234567", "p")));
        check("ID that has an account", "This ID already has an account.", invalid(() -> acc.register("100031025", "A", "a@ku.ac.ae", "0501234567", "p")));
        check("ID already pending", "A request for this ID is already pending.", invalid(() -> acc.register("100061234", "A", "a@ku.ac.ae", "0501234567", "p")));
        check("bad email", "Enter a valid email address.", invalid(() -> acc.register("100070000", "A", "a.ku.ac.ae", "0501234567", "p")));
        check("7-digit phone", "Enter a valid phone number, e.g. 0501234567.", invalid(() -> acc.register("100070000", "A", "a@ku.ac.ae", "0501234", "p")));
        check("wrong password", "Wrong password.", invalid(() -> acc.login("100031025", "nope")));
        check("login while pending", "Your registration request is still pending approval.", invalid(() -> acc.login("100061234", "gpu12345")));
        check("login after rejection", "Your registration request was rejected.", invalid(() -> acc.login("100065555", "gpu12345")));
        check("unknown ID", "No account found. Request an account first.", invalid(() -> acc.login("100099999", "x")));
        check("admin / gpu2026 signs in", "no error", invalid(() -> acc.login("admin", "gpu2026")));
        AccessRequest first = data.getRequests().get(0);
        check("approve the same request twice", true, !error(() -> acc.approve(admin, first, 100)).equals("no error"));
    }

    // ---- helpers
    static void goTo(int hour, int minute) {
        while (clock.getNow().getHour() * 60 + clock.getNow().getMinute() < hour * 60 + minute) {
            s.advanceMinute();
        }
    }

    static Job job(String id) {
        return find(data, id);
    }

    static Job find(AppData d, String id) {
        for (Job j : d.getJobs()) {
            if (j.getJobId().equals(id)) {
                return j;
            }
        }
        return null;
    }

    static String balance(String id) {
        TimeAllowance a = data.getStudents().get(id).getAllowance();
        return a.getAllocated() + "/" + a.getUsed() + "/" + a.getReserved() + "/" + a.getAvailable();
    }

    static String gpu(int id) {
        Job j = s.getGpus()[id - 1].getCurrentJob();
        return j == null ? "free" : j.getJobId() + ", " + j.getRemainingMinutes(clock.getNow()) + " min left, ends " + hm(j.getExpectedFinish());
    }

    static String queue(Job j) {
        return "#" + s.getQueuePosition(j) + ", start " + hm(s.getPredictedStart(j)) + ", wait " + s.getWaitMinutes(j);
    }

    static String waits() {
        return s.getWaitMinutes(job("J108")) + " / " + s.getWaitMinutes(job("J109")) + " / " + s.getWaitMinutes(job("J111"));
    }

    static String hm(LocalDateTime t) {
        return t == null ? "-" : String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    // message of the exception a call throws, or "no error"
    static String error(Runnable r) {
        try {
            r.run();
            return "no error";
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    interface Call {
        void run() throws ValidationException;
    }

    static String invalid(Call c) {
        try {
            c.run();
            return "no error";
        } catch (ValidationException e) {
            return e.getMessage();
        }
    }

    static void check(String name, Object expected, Object actual) {
        if (String.valueOf(expected).equals(String.valueOf(actual))) {
            System.out.println("PASS " + name + " (" + actual + ")");
        } else {
            failed++;
            System.out.println("FAIL " + name + ": expected " + expected + ", got " + actual);
        }
    }
}
