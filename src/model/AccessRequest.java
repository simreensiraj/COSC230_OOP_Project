package model;

// a student's registration request (the 5 fields from the form + a status)
// approved and rejected requests stay as history
public class AccessRequest implements Storable {
    private String universityId;
    private String name;
    private String email;
    private String phone;
    private String password;
    private RequestStatus status;
    
    public AccessRequest(String universityId, String name, String email, String phone, String password) {
        this.universityId = universityId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.status = RequestStatus.PENDING;
    }

    public String getUniversityId() {
        return universityId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public RequestStatus getStatus() {
        return status;
    }

    // a request can only be processed once
    public void approve() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("This request was already processed.");
        }
        status = RequestStatus.APPROVED;
    }

    public void reject() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("This request was already processed.");
        }
        status = RequestStatus.REJECTED;
    }

    @Override
    public String toLine() {
        return Storable.join(universityId, name, email, phone, password, status.name());
    }

    public static AccessRequest fromLine(String line) {
        String[] v = Storable.split(line, 6);
        AccessRequest request = new AccessRequest(v[0], v[1], v[2], v[3], v[4]);
        request.status = RequestStatus.valueOf(v[5]);
        return request;
    }
}
