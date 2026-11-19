package model;

import java.util.*;

// everything the app keeps in memory. DataStore saves/loads it,
// AccountService and the Scheduler work on it. the time is kept in SimulationClock
public class AppData {

    private HashMap<String, Student> students = new HashMap<>(); // key = university id
    private ArrayList<AccessRequest> requests = new ArrayList<>();
    private ArrayList<Job> jobs = new ArrayList<>();
    private ArrayList<TimeGrant> grants = new ArrayList<>();
    private GPU[] gpus = new GPU[4];

    public AppData() {
        for (int i = 0; i < gpus.length; i++) {
            gpus[i] = new GPU(i + 1);
        }
    }

    public HashMap<String, Student> getStudents() {
        return students;
    }

    public ArrayList<AccessRequest> getRequests() {
        return requests;
    }

    public ArrayList<Job> getJobs() {
        return jobs;
    }

    public ArrayList<TimeGrant> getGrants() {
        return grants;
    }

    public GPU[] getGpus() {
        return gpus;
    }

    // next job id: J101, J102, ...
    public String nextJobId() {
        int highest = 100;
        for (Job job : jobs) {
            int number = Integer.parseInt(job.getJobId().substring(1));
            if (number > highest) {
                highest = number;
            }
        }
        return "J" + (highest + 1);
    }
}
