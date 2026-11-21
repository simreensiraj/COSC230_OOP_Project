package model;

// the 4 job sizes, 1 workload unit = 1 gpu minute
public enum TaskSize {
    SMALL("Small", 5),
    MEDIUM("Medium", 15),
    LARGE("Large", 30),
    VERY_LARGE("Very large", 60);
    private final String label;
    private final int workloadUnits;

    TaskSize(String label, int workloadUnits) {
        this.label = label;
        this.workloadUnits = workloadUnits;
    }

    public int getWorkloadUnits() {
        return workloadUnits;
    }

    // duration in minutes is the same as the workload units
    public int getDuration() {
        return workloadUnits;
    }

    // what the combo box shows, e.g. "Large (30 units)"
    @Override
    public String toString() {
        return label + " (" + workloadUnits + " units)";
    }
}
