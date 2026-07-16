package team3.interfaces;

import team3.objs.Process;

/**
 * Scheduler interface defines the common methods required by any CPU scheduling algorithm.
 */
public interface Scheduler {
    
    /**
     * Add a process to the scheduler's queue or pools.
     * @param p the Process to add
     */
    void addProcess(Process p);

    /**
     * Select and return the next process to run at the given simulation time.
     * Returns null if no process is ready/available.
     * @param currentTime the current clock time of the simulation
     * @return the Process to execute, or null if idle
     */
    Process nextProcess(int currentTime);

    /**
     * Record that the running process has executed for one time unit (tick).
     * Useful for preemptive algorithms like Round Robin or MLFQ to manage quantums.
     * @param p the currently running Process
     */
    void recordTick(Process p);

    /**
     * Remove a completed process from the scheduler's queue or pools.
     * @param p the completed Process to remove
     */
    void removeProcess(Process p);

}
