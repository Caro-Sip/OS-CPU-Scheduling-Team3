package team3.algorithms;

import java.util.ArrayList;
import team3.interfaces.Scheduler;
import team3.objs.Process;

public class SJF implements Scheduler {
    Process currentProcess;
    ArrayList<Process> readyQueue = new ArrayList<>();

    @Override
    public void addProcess(Process p) {
        // Enqueue process
        readyQueue.add(p);
    }

    @Override
    public Process nextProcess(int currentTime) {
        if (readyQueue.isEmpty()) {
            return null;
        }
        
        Process shortestJob = readyQueue.get(0);
        
        for (int i = 1; i < readyQueue.size(); i++) {
            Process p = readyQueue.get(i);
            if (p.burstTime < shortestJob.burstTime) {
                shortestJob = p;
            }
        }
        
        return shortestJob;
    }

    @Override
    public void recordTick(Process p) {
       
    }

    @Override
    public void removeProcess(Process p) {
        readyQueue.remove(p);
    }
}
