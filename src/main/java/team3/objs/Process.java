package team3.objs;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Process
 * plain java objects representing a CPU process
 */
public class Process {
    public int pid;
    public int arrivalTime;
    public int burstTime;
    @JsonIgnore
    public int priority; // -1 = none

    @JsonIgnore
    public int remainingTime;
    @JsonIgnore
    public int startTime = -1; // set to time when cpu starts the process
    @JsonIgnore
    public int finishTime = -1; // set by the loop
    @JsonIgnore
    public int waitingTime;
    @JsonIgnore
    public int turnaroundTime;
    @JsonIgnore
    public int responseTime;

    /**
     * Default constructor required for Jackson JSON deserialization.
     */
    public Process() {
        this.startTime = -1;
        this.finishTime = -1;
        this.priority = -1;
    }

    /**
     * Constructor for the 4 core process attributes.
     */
    public Process(int pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.startTime = -1;
        this.finishTime = -1;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.responseTime = 0;
    }
}