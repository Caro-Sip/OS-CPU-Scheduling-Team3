package team3.algorithms;

import java.util.ArrayList;
import team3.interfaces.Scheduler;
import team3.objs.Process;

public class SJF implements Scheduler {
    private Process currentProcess = null;
    private final ArrayList<Process> readyQueue = new ArrayList<>();

    @Override
    public void addProcess(Process p) {
        readyQueue.add(p);
    }

    @Override
    public Process nextProcess(int currentTime) {
        if (readyQueue.isEmpty()) {
            return null;
        }

        // If a process is already running, keep running it (non-preemptive)
        if (currentProcess != null && readyQueue.contains(currentProcess)) {
            return currentProcess;
        }

        // Otherwise, select the shortest job from the ready queue
        Process shortestJob = readyQueue.get(0);
        for (int i = 1; i < readyQueue.size(); i++) {
            Process p = readyQueue.get(i);
            if (p.burstTime < shortestJob.burstTime) {
                shortestJob = p;
            } else if (p.burstTime == shortestJob.burstTime) {
                // Tie breakers: arrival time, then PID
                if (p.arrivalTime < shortestJob.arrivalTime) {
                    shortestJob = p;
                } else if (p.arrivalTime == shortestJob.arrivalTime) {
                    if (p.pid < shortestJob.pid) {
                        shortestJob = p;
                    }
                }
            }
        }

        currentProcess = shortestJob;
        return currentProcess;
    }

    @Override
    public void recordTick(Process p) {
    }

    @Override
    public void removeProcess(Process p) {
        readyQueue.remove(p);
        if (p == currentProcess) {
            currentProcess = null;
        }
    }
}
