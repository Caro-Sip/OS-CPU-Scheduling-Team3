package team3.sim;

import team3.objs.Process;

public class CPU {
    Process currentProcess;

    boolean isIdle() {
        return currentProcess == null;
    }

    void assign(Process p, int time) {
        this.currentProcess = p;
        if (currentProcess.startTime == -1)
            currentProcess.startTime = time;
    }

    void run() {
        currentProcess.remainingTime--;
    }
}
