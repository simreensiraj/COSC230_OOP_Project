package storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import logic.SimulationClock;
import logic.ValidationException;
import model.AccessRequest;
import model.AppData;
import model.Job;
import model.JobStatus;
import model.Storable;
import model.Student;
import model.TimeGrant;

// saves and loads everything as text files in the data folder, one object per line
// gpu assignments are saved in jobs.csv (the gpu column of running jobs)
// a missing file just means nothing was saved yet
public class DataStore {
    private final String folder;

    public DataStore() {
        this("data");
    }

    public DataStore(String folder) {
        this.folder = folder;
    }

    public void save(AppData data, SimulationClock clock) throws IOException {
        Files.createDirectories(Paths.get(folder));
        writeFile("students.csv", new ArrayList<>(data.getStudents().values()));
        writeFile("requests.csv", new ArrayList<>(data.getRequests()));
        writeFile("jobs.csv", new ArrayList<>(data.getJobs()));
        writeFile("grants.csv", new ArrayList<>(data.getGrants()));
        // year,month,day,hour,minute,elapsedMinutes. empty file if the clock isn't set yet
        ArrayList<String> clockLine = new ArrayList<>();
        if (clock.isSet()) {
            clockLine.add(Storable.join(String.valueOf(clock.getYear()), String.valueOf(clock.getMonth()), String.valueOf(clock.getDay()), String.valueOf(clock.getHour()), String.valueOf(clock.getMinute()), String.valueOf(clock.getElapsedMinutes())));
        }
        Files.write(Paths.get(folder, "clock.csv"), clockLine);
    }

    // each item writes its own line with toLine(), whatever class it is
    private void writeFile(String fileName, ArrayList<Storable> items) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        for (Storable item : items) {
            lines.add(item.toLine());
        }
        Files.write(Paths.get(folder, fileName), lines);
    }

    // fills the given clock and returns everything else
    // a broken line stops loading with a clear message (file + line) instead of crashing
    public AppData load(SimulationClock clock) throws ValidationException {
        AppData data = new AppData();
        String fileName = "";
        int lineNumber = 0;
        try {
            fileName = "clock.csv";
            lineNumber = 0;
            for (String line : readFile(fileName)) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    String[] v = Storable.split(line, 6);
                    boolean ok = clock.restore(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2]), Integer.parseInt(v[3]), Integer.parseInt(v[4]), Integer.parseInt(v[5]));
                    if (!ok) {
                        throw new IllegalArgumentException("not a valid date/time");
                    }
                }
            }
            fileName = "students.csv";
            lineNumber = 0;
            for (String line : readFile(fileName)) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    Student student = Student.fromLine(line);
                    data.getStudents().put(student.getUniversityId(), student);
                }
            }
            fileName = "requests.csv";
            lineNumber = 0;
            for (String line : readFile(fileName)) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    data.getRequests().add(AccessRequest.fromLine(line));
                }
            }
            fileName = "grants.csv";
            lineNumber = 0;
            for (String line : readFile(fileName)) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    data.getGrants().add(TimeGrant.fromLine(line));
                }
            }
            fileName = "jobs.csv";
            lineNumber = 0;
            for (String line : readFile(fileName)) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    Job job = Job.fromLine(line);
                    if (!data.getStudents().containsKey(job.getStudentId())) {
                        throw new IllegalArgumentException("unknown student " + job.getStudentId());
                    }
                    data.getJobs().add(job);
                    if (job.getStatus() == JobStatus.RUNNING) {
                        data.getGpus()[job.getAssignedGpuId() - 1].assign(job); // back on its gpu
                    }
                }
            }
        } catch (Exception e) {
            // e.g. Could not read data/jobs.csv (line 7: bad date "16-11-26")
            throw new ValidationException("Could not read " + folder + "/" + fileName + " (line " + lineNumber + ": " + e.getMessage() + ").\nThe file was not loaded. Fix or remove it, then restart.");
        }
        return data;
    }

    // all lines of a file (so line numbers match), or none if it doesn't exist yet
    private List<String> readFile(String fileName) throws IOException {
        Path path = Paths.get(folder, fileName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(path);
    }
}
