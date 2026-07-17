package team3.algorithms;

import java.util.LinkedList;
import java.util.Queue;
import team3.interfaces.Scheduler;
import team3.objs.Process;

public class RR implements Scheduler {
    private final Queue<Process> readyQueue = new LinkedList<>();
    private final int quantum;
    private int timeSpentInCurrentQuantum = 0;
    private Process currentRunningProcess = null;

    public RR(int quantum) {
        this.quantum = quantum > 0 ? quantum : 2; // default to 2 if invalid
    }

    @Override
    public void addProcess(Process p) {
        readyQueue.add(p);
    }

    @Override
    public Process nextProcess(int currentTime) {
        Process next = readyQueue.peek();

        // If the process at the front is different from what we think is running,
        // it means we just context-switched or a new process has started.
        if (next != currentRunningProcess) {
            currentRunningProcess = next;
            timeSpentInCurrentQuantum = 0;
        }

        // If a process is running and has exhausted its quantum, preempt it
        if (currentRunningProcess != null && timeSpentInCurrentQuantum >= quantum) {
            readyQueue.poll();                     // Remove from front
            readyQueue.add(currentRunningProcess);  // Move to the back of the ready queue
            timeSpentInCurrentQuantum = 0;
            
            // Get the new process at the front of the ready queue
            next = readyQueue.peek();
            currentRunningProcess = next;
        }

        return next;
    }

    @Override
    public void recordTick(Process p) {
        // Record that the running process has executed for one time unit (tick).
        if (p == currentRunningProcess) {
            timeSpentInCurrentQuantum++;
        }
    }

    @Override
    public void removeProcess(Process p) {
        readyQueue.remove(p);
        if (p == currentRunningProcess) {
            currentRunningProcess = null;
            timeSpentInCurrentQuantum = 0;
        }
    }
}
