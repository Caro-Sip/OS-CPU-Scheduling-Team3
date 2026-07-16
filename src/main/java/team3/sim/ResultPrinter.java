package team3.sim;

import team3.objs.Process;

import java.util.List;
import java.util.Map;

/**
 * ResultPrinter utility prints CPU simulation results and Gantt charts to the console.
 */
public class ResultPrinter {

    /**
     * Print the results of a single simulation run.
     * @param result the SimulationResult to display
     */
    public static void print(SimulationResult result) {
        if (result == null) {
            System.out.println("No simulation results available to print.");
            return;
        }

        System.out.println("\n==============================================================================================");
        System.out.println("                                   SIMULATION RUN RESULTS                                     ");
        System.out.println("==============================================================================================");
        System.out.printf("%-5s | %-12s | %-10s | %-8s | %-10s | %-11s | %-12s | %-15s\n", 
            "PID", "Arrival Time", "Burst Time", "Priority", "Start Time", "Finish Time", "Waiting Time", "Turnaround Time");
        System.out.println("----------------------------------------------------------------------------------------------");
        for (Process p : result.completedProcesses) {
            System.out.printf("%-5d | %-12d | %-10d | %-8d | %-10d | %-11d | %-12d | %-15d\n",
                p.pid, p.arrivalTime, p.burstTime, p.priority, p.startTime, p.finishTime, p.waitingTime, p.turnaroundTime);
        }
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.printf("Average Waiting Time: %.2f\n", result.averageWaitingTime);
        System.out.printf("Average Turnaround Time: %.2f\n", result.averageTurnaroundTime);
        System.out.println("==============================================================================================");

        printGanttChart(result.timeline);
    }

    /**
     * Print side-by-side comparison across multiple algorithm runs.
     * @param results Map of algorithm label keys to their respective SimulationResult values
     */
    public static void printComparison(Map<String, SimulationResult> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("No algorithm runs available in session to compare.");
            return;
        }

        System.out.println("\n=============================================================");
        System.out.println("                 ALGORITHM COMPARISON SUMMARY                ");
        System.out.println("=============================================================");
        System.out.printf("%-20s | %-20s | %-20s\n", "Algorithm", "Avg Waiting Time", "Avg Turnaround Time");
        System.out.println("-------------------------------------------------------------");
        for (Map.Entry<String, SimulationResult> entry : results.entrySet()) {
            SimulationResult res = entry.getValue();
            System.out.printf("%-20s | %-20.2f | %-20.2f\n", entry.getKey(), res.averageWaitingTime, res.averageTurnaroundTime);
        }
        System.out.println("=============================================================");
    }

    /**
     * Render Gantt Chart in proportional boxed ASCII format.
     */
    public static void printGanttChart(List<GanttElement> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            System.out.println("Gantt Chart: (No execution timeline recorded)");
            return;
        }

        StringBuilder topBorder = new StringBuilder();
        StringBuilder processLine = new StringBuilder();
        StringBuilder bottomBorder = new StringBuilder();
        StringBuilder timeLine = new StringBuilder();

        topBorder.append("+");
        processLine.append("|");
        bottomBorder.append("+");
        timeLine.append("0");

        for (GanttElement entry : timeline) {
            int duration = entry.endTime - entry.startTime;
            // Proportional box width
            int width = Math.max(5, duration * 2);
            String pStr = "P" + entry.pid;

            int totalPad = width - pStr.length();
            int leftPad = totalPad / 2;
            int rightPad = totalPad - leftPad;

            for (int i = 0; i < width; i++) {
                topBorder.append("-");
                bottomBorder.append("-");
            }
            topBorder.append("+");
            bottomBorder.append("+");

            for (int i = 0; i < leftPad; i++) processLine.append(" ");
            processLine.append(pStr);
            for (int i = 0; i < rightPad; i++) processLine.append(" ");
            processLine.append("|");

            // Time label alignment
            String endTimeStr = String.valueOf(entry.endTime);
            int spaceNeeded = topBorder.length() - 1 - timeLine.length() - endTimeStr.length();
            for (int i = 0; i < spaceNeeded; i++) {
                timeLine.append(" ");
            }
            timeLine.append(endTimeStr);
        }

        System.out.println("\nGantt Chart Timeline:");
        System.out.println(topBorder.toString());
        System.out.println(processLine.toString());
        System.out.println(bottomBorder.toString());
        System.out.println(timeLine.toString());
    }
}
