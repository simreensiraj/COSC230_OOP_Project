package tests;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import logic.AccountService;
import logic.SimulationClock;
import logic.ValidationException;
import model.AccessRequest;
import model.Administrator;
import model.AppData;
import model.GPU;
import model.Job;
import model.JobStatus;
import model.Student;
import model.TaskSize;
import model.TimeAllowance;
import model.TimeFormat;
import storage.DataStore;

// console tests for the model, accounts and storage. run it as a java application
// prints PASS / FAIL for each check
public class ModelStorageTest {
    static int failed = 0;

    public static void main(String[] args) throws Exception {
        workedExample();
        allowanceLimits();
        illegalMoves();
        accounts();
        saveAndReload();
        missingAndBrokenFiles();
        System.out.println(failed == 0 ? "\nALL PASSED" : "\n" + failed + " FAILED");
    }

    // the guide's worked example: ends at 100/27/60/13, then +20 gives 120/27/60/33
    static void workedExample() {
        System.out.println("-- worked example");
        LocalDateTime t = TimeFormat.parse("16/11/2026 10:00");
        Student s = new Student("100052210", "pw", "Sara", "sara@ku.ac.ae", "0501234567", new TimeAllowance(100));
        TimeAllowance a = s.getAllowance();
        Job jobA = new Job("J101", "100052210", "A", "app", "d", TaskSize.MEDIUM, t);
        a.reserve(15);
        Job jobB = new Job("J102", "100052210", "B", "app", "d", TaskSize.LARGE, t);
        a.reserve(30);
        check("after A and B", a, 100, 0, 45, 55);
        jobA.start(1, t);
        jobB.start(2, t);
        jobA.complete(t.plusMinutes(15));
        a.release(15);
        a.charge(jobA.getMinutesRun(t.plusMinutes(15)));
        check("job A completes", a, 100, 15, 30, 55);
        jobB.cancel("100052210", "wrong dataset", t.plusMinutes(12));
        a.release(30);
        a.charge(jobB.getMinutesRun(t.plusMinutes(12)));
        check("job B cancelled after 12 min", a, 100, 27, 0, 73);
        a.reserve(60);
        check("job C (60) accepted", a, 100, 27, 60, 13);
        try {
            a.reserve(30);
            fail("job D (30) should be rejected");
        } catch (IllegalArgumentException e) {
            check("job D (30) rejected", a, 100, 27, 60, 13);
        }
        new Administrator().grantTime(s, 20, "extra runs", t.plusMinutes(20));
        check("admin grants +20", a, 120, 27, 60, 33);
    }

    static void allowanceLimits() {
        System.out.println("-- allowance limits");
        TimeAllowance a = new TimeAllowance(15);
        a.reserve(15);
        check("exact match accepted", a, 15, 0, 15, 0);
        try {
            a.reserve(5);
            fail("available 0 should block new jobs");
        } catch (IllegalArgumentException e) {
            pass("available 0 blocks new jobs");
        }
    }

    static void illegalMoves() {
        System.out.println("-- illegal job moves");
        LocalDateTime t = TimeFormat.parse("16/11/2026 10:00");
        Job job = new Job("J101", "100031025", "t", "app", "d", TaskSize.SMALL, t);
        try {
            job.complete(t);
            fail("complete a queued job");
        } catch (IllegalStateException e) {
            pass("cannot complete a queued job");
        }
        job.start(1, t);
        job.complete(t.plusMinutes(5));
        try {
            job.start(2, t.plusMinutes(6));
            fail("restart a completed job");
        } catch (IllegalStateException e) {
            pass("cannot restart a completed job");
        }
        try {
            job.cancel("admin", "x", t.plusMinutes(6));
            fail("cancel a completed job");
        } catch (IllegalStateException e) {
            pass("cannot cancel a completed job");
        }
        GPU gpu = new GPU(1);
        gpu.assign(job);
        try {
            gpu.assign(new Job("J102", "100031025", "t", "app", "d", TaskSize.SMALL, t));
            fail("busy GPU takes a second job");
        } catch (IllegalStateException e) {
            pass("busy GPU refuses a second job");
        }
    }

    static void accounts() throws Exception {
        System.out.println("-- accounts");
        AppData data = new AppData();
        AccountService accounts = new AccountService(data);
        Administrator admin = new Administrator();
        try {
            accounts.register("100061477", "Omar", "omar.k@ku", "0501234567", "pw");
            fail("bad email accepted");
        } catch (ValidationException e) {
            pass("bad email rejected (" + e.getField() + ")");
        }
        AccessRequest omar = accounts.register("100061477", "Omar Khalid", "omar.k@ku.ac.ae", "0501234567", "pw");
        loginMessage(accounts, "100061477", "pw", "still pending approval");
        accounts.approve(admin, omar, AccountService.DEFAULT_MINUTES);
        try {
            accounts.approve(admin, omar, 100);
            fail("approved twice");
        } catch (IllegalStateException e) {
            pass("cannot approve twice");
        }
        if (accounts.login("100061477", "pw") instanceof Student) {
            pass("approved student can sign in");
        } else {
            fail("approved student can sign in");
        }
        if (accounts.login("admin", "gpu2026").isAdmin()) {
            pass("admin can sign in");
        } else {
            fail("admin can sign in");
        }
        AccessRequest lina = accounts.register("100063052", "Lina", "lina@ku.ac.ae", "0559876543", "x");
        accounts.reject(admin, lina);
        loginMessage(accounts, "100063052", "x", "rejected");
    }

    static void saveAndReload() throws Exception {
        System.out.println("-- save, close, reopen");
        AppData data = new AppData();
        LocalDateTime t = TimeFormat.parse("16/11/2026 10:00");
        SimulationClock clock = new SimulationClock();
        clock.setStart(2026, 11, 16, 10, 0);
        for (int i = 0; i < 20; i++) {
            clock.advance();
        }
        Student s = new Student("100031025", "pw", "Ali", "ali@ku.ac.ae", "0501234567", new TimeAllowance(100, 5, 30));
        data.getStudents().put(s.getUniversityId(), s);
        Job done = new Job("J101", "100031025", "t", "app", "has, a comma", TaskSize.SMALL, t);
        done.start(1, t);
        done.complete(t.plusMinutes(5));
        Job running = new Job("J105", "100031025", "t", "app", "d", TaskSize.LARGE, t.plusMinutes(8));
        running.start(1, t.plusMinutes(8));
        data.getGpus()[0].assign(running);
        data.getJobs().add(done);
        data.getJobs().add(running);
        String folder = Files.createTempDirectory("gpu").toString() + "/data";
        new DataStore(folder).save(data, clock);
        SimulationClock loadedClock = new SimulationClock();
        AppData loaded = new DataStore(folder).load(loadedClock);
        Student back = loaded.getStudents().get("100031025");
        check("balance restored", back.getAllowance(), 100, 5, 30, 65);
        same("clock restored", loadedClock.getNow(), t.plusMinutes(20));
        same("elapsed minutes restored", loadedClock.getElapsedMinutes(), 20);
        same("J105 back on GPU1", loaded.getGpus()[0].getCurrentJob().getJobId(), "J105");
        same("J105 has 18 min left", loaded.getGpus()[0].getCurrentJob().getRemainingMinutes(loadedClock.getNow()), 18);
        same("J101 still completed", loaded.getJobs().get(0).getStatus(), JobStatus.COMPLETED);
        same("description with comma", loaded.getJobs().get(0).getDescription(), "has, a comma");
        same("next job ID", loaded.nextJobId(), "J106");
    }

    static void missingAndBrokenFiles() throws Exception {
        System.out.println("-- missing and broken files");
        String empty = Files.createTempDirectory("gpu").toString() + "/nothing";
        SimulationClock clock = new SimulationClock();
        AppData data = new DataStore(empty).load(clock);
        same("missing files start empty", data.getJobs().size(), 0);
        same("clock not set yet", clock.isSet(), false);
        String folder = Files.createTempDirectory("gpu").toString();
        Files.write(Paths.get(folder, "students.csv"), List.of("100031025,pw,Ali,ali@ku.ac.ae,0501234567,100,0,0"));
        Files.write(Paths.get(folder, "jobs.csv"), List.of("J101,100031025,t,app,d,SMALL,16-11-26,QUEUED,0,,,,,"));
        try {
            new DataStore(folder).load(new SimulationClock());
            fail("broken date loaded");
        } catch (ValidationException e) {
            System.out.println("   message: " + e.getMessage().replace("\n", " "));
            if (e.getMessage().contains("jobs.csv (line 1: bad date \"16-11-26\")")) {
                pass("broken file gives file + line");
            } else {
                fail("broken file gives file + line");
            }
        }
    }

    // helpers
    static void check(String name, TimeAllowance a, int allocated, int used, int reserved, int available) {
        same(name, a.getAllocated() + "/" + a.getUsed() + "/" + a.getReserved() + "/" + a.getAvailable(), allocated + "/" + used + "/" + reserved + "/" + available);
    }

    static void loginMessage(AccountService accounts, String user, String pw, String expected) {
        try {
            accounts.login(user, pw);
            fail("login should fail: " + expected);
        } catch (ValidationException e) {
            if (e.getMessage().contains(expected)) {
                pass("login message: " + e.getMessage());
            } else {
                fail("login message: " + e.getMessage());
            }
        }
    }

    static void same(String name, Object actual, Object expected) {
        if (actual.equals(expected)) {
            pass(name + " (" + actual + ")");
        } else {
            fail(name + ": got " + actual + ", expected " + expected);
        }
    }

    static void pass(String name) {
        System.out.println("PASS " + name);
    }

    static void fail(String name) {
        System.out.println("FAIL " + name);
        failed++;
    }
}
