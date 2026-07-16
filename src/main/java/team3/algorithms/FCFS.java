package team3.algorithms;

import java.util.LinkedList;
import java.util.Queue;

import team3.interfaces.Scheduler;
import team3.objs.Process;

public class FCFS implements Scheduler {
    private final Queue<Process> readyQueue = new LinkedList<>();

    @Override
    public void addProcess(Process p) {
        // enqeue
        readyQueue.add(p);
    }

    @Override
    public Process nextProcess(int currentTime) {
        // peek
        return readyQueue.peek();
    }

    @Override
    public void recordTick(Process p) {
        // unsupported, fcfs is non-preemptive
    }

    @Override
    public void removeProcess(Process p) {
        readyQueue.poll();
    }
}
