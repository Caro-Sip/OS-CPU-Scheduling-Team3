package team3.sim;

/**
 * GanttElement represents a block of continuous execution of a process on the CPU.
 */
public class GanttElement {
    public int pid;
    public int startTime;
    public int endTime;

    public GanttElement() {}

    public GanttElement(int pid, int startTime, int endTime) {
        this.pid = pid;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
