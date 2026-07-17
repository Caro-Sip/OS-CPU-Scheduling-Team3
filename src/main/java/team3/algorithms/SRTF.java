package team3.algorithms;

import java.util.ArrayList;
import team3.interfaces.Scheduler;
import team3.objs.Process;

public class SRTF implements Scheduler {
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

        Process shortestRemainingJob = readyQueue.get(0);

        for (int i = 1; i < readyQueue.size(); i++) {
            Process p = readyQueue.get(i);
            // Preemptive selection based on shortest remaining time
            if (p.remainingTime < shortestRemainingJob.remainingTime) {
                shortestRemainingJob = p;
            } else if (p.remainingTime == shortestRemainingJob.remainingTime) {
                // Tie breakers
                if (p.arrivalTime < shortestRemainingJob.arrivalTime) {
                    shortestRemainingJob = p;
                } else if (p.arrivalTime == shortestRemainingJob.arrivalTime) {
                    if (p.pid < shortestRemainingJob.pid) {
                        shortestRemainingJob = p;
                    }
                }
            }
        }

        return shortestRemainingJob;
    }

    @Override
    public void recordTick(Process p) {
    }

    @Override
    public void removeProcess(Process p) {
        readyQueue.remove(p);
    }
}
