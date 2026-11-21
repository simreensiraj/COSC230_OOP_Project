package model;

// a student's gpu minutes: available = allocated - used - reserved
// fields are private so only the methods below can change them, and none of them lets a value go below 0
public class TimeAllowance {
    private int allocated;
    private int used;
    private int reserved;

    public TimeAllowance(int allocated) {
        this(allocated, 0, 0);
    }

    // used when loading from the file
    public TimeAllowance(int allocated, int used, int reserved) {
        if (allocated < 0 || used < 0 || reserved < 0 || used + reserved > allocated) {
            throw new IllegalArgumentException("Invalid balances");
        }
        this.allocated = allocated;
        this.used = used;
        this.reserved = reserved;
    }

    public int getAllocated() {
        return allocated;
    }

    public int getUsed() {
        return used;
    }

    public int getReserved() {
        return reserved;
    }

    public int getAvailable() {
        return allocated - used - reserved;
    }

    // job accepted: hold its full duration (exact match is ok)
    public void reserve(int minutes) {
        if (minutes <= 0 || minutes > getAvailable()) {
            throw new IllegalArgumentException("Not enough GPU time");
        }
        reserved += minutes;
    }

    // job completed or cancelled: give the reservation back
    public void release(int minutes) {
        if (minutes <= 0 || minutes > reserved) {
            throw new IllegalArgumentException("Cannot release more than is reserved");
        }
        reserved -= minutes;
    }

    // minutes the job actually ran, waiting is never charged
    public void charge(int minutes) {
        if (minutes < 0 || minutes > getAvailable()) {
            throw new IllegalArgumentException("Invalid charge");
        }
        used += minutes;
    }

    // admin gives extra time
    public void grant(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Minutes must be a positive whole number");
        }
        allocated += minutes;
    }
}
