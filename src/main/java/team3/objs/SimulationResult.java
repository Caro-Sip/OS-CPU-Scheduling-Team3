package team3.objs;

import java.util.List;

/**
 * SimulationResult is a plain data container holding the outcome of a CPU simulation run.
 */
public class SimulationResult {
    public List<Process> completedProcesses;
    public List<GanttElement> timeline;
    public double averageWaitingTime;
    public double averageTurnaroundTime;

    public SimulationResult() {}

    public SimulationResult(List<Process> completedProcesses, List<GanttElement> timeline, 
                            double averageWaitingTime, double averageTurnaroundTime) {
        this.completedProcesses = completedProcesses;
        this.timeline = timeline;
        this.averageWaitingTime = averageWaitingTime;
        this.averageTurnaroundTime = averageTurnaroundTime;
    }
}
