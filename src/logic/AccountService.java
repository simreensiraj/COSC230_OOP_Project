package logic;
import java.time.*;
import java.util.*;
import model.*;

// registration, login, approving/rejecting requests and granting time
public class AccountService {
    public static final int DEFAULT_MINUTES = 100;
    private AppData data;
    private Administrator admin = new Administrator();

    public AccountService(AppData data) {
        this.data = data;
    }

    // checks the form and saves a PENDING request
    public AccessRequest register(String universityId, String name, String email, String phone, String password) throws ValidationException {
        if (universityId == null || universityId.trim().isEmpty()) {
            throw new ValidationException("universityId", "Enter your university ID.");
        }
        if (!universityId.trim().matches("\\d+")) {
            throw new ValidationException("universityId", "University ID must be digits only.");
        }
        if (data.getStudents().containsKey(universityId.trim())) {
            throw new ValidationException("universityId", "This ID already has an account.");
        }
        for (AccessRequest r : data.getRequests()) {
            if (r.getUniversityId().equals(universityId.trim()) && r.getStatus() == RequestStatus.PENDING) {
                throw new ValidationException("universityId", "A request for this ID is already pending.");
            }
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("name", "Enter your full name.");
        }
        if (email == null || !email.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ValidationException("email", "Enter a valid email address.");
        }
        if (phone == null || !phone.trim().matches("\\d{10}")) {
            throw new ValidationException("phone", "Enter a valid phone number, e.g. 0501234567.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("password", "Choose a password.");
        }
        AccessRequest request = new AccessRequest(universityId.trim(), name.trim(), email.trim(), phone.trim(), password);
        data.getRequests().add(request);
        return request;
    }

    // returns the admin or an approved student
    public User login(String username, String password) throws ValidationException {
        username = username.trim();
        ArrayList<User> users = new ArrayList<>();
        users.add(admin);
        users.addAll(data.getStudents().values());

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                if (user.checkPassword(password)) {
                    return user;
                }
                throw new ValidationException("Wrong password.");
            }
        }

        // no account yet, so check their latest request
        AccessRequest latest = null;
        for (AccessRequest r : data.getRequests()) {
            if (r.getUniversityId().equals(username)) {
                latest = r;
            }
        }
        if (latest == null) {
            throw new ValidationException("No account found. Request an account first.");
        }
        if (latest.getStatus() == RequestStatus.PENDING) {
            throw new ValidationException("Your registration request is still pending approval.");
        }
        throw new ValidationException("Your registration request was rejected.");
    }

    // admin only (needs an Administrator), blocks duplicate accounts
    public Student approve(Administrator by, AccessRequest request, int initialMinutes) {
        if (data.getStudents().containsKey(request.getUniversityId())) {
            throw new IllegalStateException("This ID already has an account.");
        }
        Student student = by.approve(request, initialMinutes);
        data.getStudents().put(student.getUniversityId(), student);
        return student;
    }

    public void reject(Administrator by, AccessRequest request) {
        by.reject(request);
    }

    public TimeGrant grantTime(Administrator by, Student student, int minutes, String reason, LocalDateTime now) {
        TimeGrant grant = by.grantTime(student, minutes, reason, now);
        data.getGrants().add(grant);
        return grant;
    }
}
