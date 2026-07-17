package team3.algorithms;

import team3.interfaces.Scheduler;
import team3.objs.Process;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MLFQ implements Scheduler {

    private final int[] quantums; // quantum per level; 0 = no limit (run-to-completion at that level)
    private final int numLevels;
    private final List<Deque<Process>> queues;

    private Process current;
    private int currentLevel = -1;
    private int ticksUsedAtLevel = 0;

    public MLFQ(int[] quantums) {
        this.quantums = quantums;
        this.numLevels = quantums.length;
        this.queues = new ArrayList<>();
        for (int i = 0; i < numLevels; i++) {
            queues.add(new ArrayDeque<>());
        }
    }

    @Override
    public void addProcess(Process p) {
        queues.get(0).add(p); // new arrivals always enter at the top priority level
    }

    @Override
    public Process nextProcess(int currentTime) {
        // Preempt current if a strictly higher-priority queue now has someone waiting
        if (current != null) {
            int higher = highestNonEmptyLevel();
            if (higher != -1 && higher < currentLevel) {
                queues.get(currentLevel).addFirst(current); // didn't use its quantum, keep its place
                current = null;
            }
        }

        if (current == null) {
            int level = highestNonEmptyLevel();
            if (level != -1) {
                current = queues.get(level).poll();
                currentLevel = level;
                ticksUsedAtLevel = 0;
            }
        }

        return current;
    }

    @Override
    public void recordTick(Process p) {
        if (p.remainingTime == 0) {
            return; // finished this tick; removeProcess handles cleanup
        }

        ticksUsedAtLevel++;
        int quantum = quantums[currentLevel];

        if (quantum > 0 && ticksUsedAtLevel >= quantum) {
            int nextLevel = Math.min(currentLevel + 1, numLevels - 1);
            queues.get(nextLevel).add(current); // demote, or requeue at last level
            current = null;
        }
    }

    @Override
    public void removeProcess(Process p) {
        current = null;
    }

    private int highestNonEmptyLevel() {
        for (int i = 0; i < numLevels; i++) {
            if (!queues.get(i).isEmpty()) return i;
        }
        return -1;
    }
}
