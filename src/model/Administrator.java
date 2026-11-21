package model;

import java.time.*;

// the one fixed admin account
public class Administrator extends User {
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "gpu2026";

    public Administrator() {
        super(USERNAME, PASSWORD);
    }

    @Override
    public boolean isAdmin() {
        return true;
    }

    // approving creates the student account, username = university id
    public Student approve(AccessRequest request, int initialMinutes) {
        if (initialMinutes <= 0) {
            throw new IllegalArgumentException("Minutes must be a positive whole number");
        }
        request.approve();
        return new Student(request.getUniversityId(), request.getPassword(), request.getName(), request.getEmail(), request.getPhone(), new TimeAllowance(initialMinutes));
    }

    // rejecting only changes the status
    public void reject(AccessRequest request) {
        request.reject();
    }

    // extra minutes for a student, returns the record so it can be saved
    public TimeGrant grantTime(Student student, int minutes, String reason, LocalDateTime now) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A reason is required.");
        }
        student.getAllowance().grant(minutes);
        return new TimeGrant(student.getUniversityId(), minutes, reason.trim(), getUsername(), now);
    }
}
