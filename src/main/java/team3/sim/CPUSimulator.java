package team3.sim;

import team3.interfaces.Scheduler;
import team3.objs.GanttElement;
import team3.objs.Process;
import team3.objs.SimulationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * CPUSimulator executes CPU scheduling algorithms using the tick loop.
 */
public class CPUSimulator {

    /**
     * Run the simulation for the given processes and scheduler.
     * 
     * @param processes the list of input processes
     * @param scheduler the scheduler algorithm to use
     * @return SimulationResult containing simulation logs and averages
     */
    public SimulationResult run(List<Process> processes, Scheduler scheduler) {
        // Deep-copy to ensure the original input list and objects are never mutated
        List<Process> copy = deepCopyProcesses(processes);
        // Pre-sort by arrival time to optimize arrival checking to O(1) per tick
        copy.sort((p1, p2) -> Integer.compare(p1.arrivalTime, p2.arrivalTime));

        List<Process> completed = new ArrayList<>();
        List<GanttElement> timeline = new ArrayList<>();
        CPU cpu = new CPU();

        int time = 0;
        // when the process start
        int activeStart = 0;

        // Run as long as not all processes have completed execution
        while (completed.size() < processes.size()) {
            // 1. Add newly arrived processes to the scheduler
            while (!copy.isEmpty() && copy.get(0).arrivalTime == time) {
                scheduler.addProcess(copy.remove(0));
            }

            // 2. Query scheduler for the next process to run at this clock tick
            Process next = scheduler.nextProcess(time);

            // 3. Check if the next process isn't the same as the previous one
            // if not the same, we switch the cpu current process
            if (next != cpu.currentProcess) {
                if (!cpu.isIdle()) {
                    // append the process to the gantt chart
                    timeline.add(new GanttElement(cpu.currentProcess.pid, activeStart, time));
                }

                if (next != null) {
                    cpu.assign(next, time);
                } else {
                    cpu.currentProcess = null;
                }
                activeStart = time;
            }

            // 4. Execute the active process for 1 tick
            if (!cpu.isIdle()) {
                cpu.run(); // decrement remaining time of the process
                scheduler.recordTick(cpu.currentProcess);

                // If process is finished, record stats and remove it
                if (cpu.currentProcess.remainingTime == 0) {
                    Process finished = cpu.currentProcess;
                    finished.finishTime = time + 1;
                    finished.turnaroundTime = finished.finishTime - finished.arrivalTime;
                    finished.waitingTime = finished.turnaroundTime - finished.burstTime;

                    completed.add(finished);
                    scheduler.removeProcess(finished);
                    timeline.add(new GanttElement(finished.pid, activeStart, time + 1));
                    cpu.currentProcess = null;
                }
            }
            time++;
        }

        // Calculate Average Waiting Time (AWT) and Average Turnaround Time (ATT)
        double totalWaiting = 0;
        double totalTurnaround = 0;
        for (Process p : completed) {
            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
        }
        double avgWaiting = completed.isEmpty() ? 0 : totalWaiting / completed.size();
        double avgTurnaround = completed.isEmpty() ? 0 : totalTurnaround / completed.size();

        return new SimulationResult(completed, timeline, avgWaiting, avgTurnaround);
    }

    /**
     * Create a deep copy of the processes using the 4-argument constructor.
     */
    private List<Process> deepCopyProcesses(List<Process> original) {
        List<Process> copy = new ArrayList<>();
        for (Process p : original) {
            copy.add(new Process(p.pid, p.arrivalTime, p.burstTime, p.priority));
        }
        return copy;
    }
}
