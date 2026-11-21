package model;

// approved student account, username = university id
// each student has one TimeAllowance
public class Student extends User implements Storable {

    private String name;
    private String email;
    private String phone;
    private TimeAllowance allowance;

    public Student(String universityId, String password, String name, String email, String phone,TimeAllowance allowance) {
        super(universityId, password);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.allowance = allowance;
    }

    public String getUniversityId() {
        return getUsername();
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

    public TimeAllowance getAllowance() {
        return allowance;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    // id, password, name, email, phone, allocated, used, reserved
    @Override
    public String toLine() {
        return Storable.join(getUniversityId(), getPassword(), name, email, phone,
                String.valueOf(allowance.getAllocated()), String.valueOf(allowance.getUsed()),
                String.valueOf(allowance.getReserved()));
    }

    public static Student fromLine(String line) {
        String[] v = Storable.split(line, 8);
        TimeAllowance allowance = new TimeAllowance(Integer.parseInt(v[5]),
                Integer.parseInt(v[6]), Integer.parseInt(v[7]));
        return new Student(v[0], v[1], v[2], v[3], v[4], allowance);
    }
}
