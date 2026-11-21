package model;

import java.time.LocalDateTime;

// record of extra time the admin gave: who, when (simulated time) and why
public class TimeGrant implements Storable {

    private final String studentId;
    private final int minutes;
    private final String reason;
    private final String grantedBy;
    private final LocalDateTime grantedAt;

    public TimeGrant(String studentId, int minutes, String reason, String grantedBy,
            LocalDateTime grantedAt) {
        this.studentId = studentId;
        this.minutes = minutes;
        this.reason = reason;
        this.grantedBy = grantedBy;
        this.grantedAt = grantedAt;
    }

    public String getStudentId() {
        return studentId;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getReason() {
        return reason;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    @Override
    public String toLine() {
        return Storable.join(studentId, String.valueOf(minutes), reason, grantedBy,
                TimeFormat.format(grantedAt));
    }

    public static TimeGrant fromLine(String line) {
        String[] v = Storable.split(line, 5);
        return new TimeGrant(v[0], Integer.parseInt(v[1]), v[2], v[3], TimeFormat.parse(v[4]));
    }
}
