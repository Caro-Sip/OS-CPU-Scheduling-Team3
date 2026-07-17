package team3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import team3.objs.Process;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * CLI Menu Application for CPU Scheduling
 */
public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Menu.getInstance().run(scanner);
        System.out.println("Exiting CPU Scheduler. Goodbye!");
        scanner.close();
    }

    /**
     * Create the default list of 4 processes as defined in the requirements.
     * Uses the 4-arg constructor.
     */
    public static List<Process> createDefaultProcesses() {
        List<Process> defaultProcesses = new ArrayList<>();
        defaultProcesses.add(new Process(1, 0, 5, -1));
        defaultProcesses.add(new Process(2, 1, 3, -1));
        defaultProcesses.add(new Process(3, 2, 8, -1));
        defaultProcesses.add(new Process(4, 3, 6, -1));
        return defaultProcesses;
    }

    /**
     * Save processes to JSON file using BufferedWriter and Jackson.
     */
    public static void saveProcesses(List<Process> processes, String filename) {
        File file = new File(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(writer, processes);
            System.out.println("Successfully saved processes to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving processes to file: " + e.getMessage());
        }
    }

    /**
     * Read processes from JSON file using BufferedReader and Jackson.
     */
    public static List<Process> readProcesses(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            ObjectMapper mapper = new ObjectMapper();
            List<Process> processes = mapper.readValue(reader, new TypeReference<List<Process>>() {});
            if (processes != null) {
                // Initialize/reset runtime attributes that are ignored in JSON
                for (Process p : processes) {
                    p.remainingTime = p.burstTime;
                    p.startTime = -1;
                    p.finishTime = -1;
                    p.waitingTime = 0;
                    p.turnaroundTime = 0;
                    p.responseTime = 0;
                }
            }
            return processes;
        } catch (IOException e) {
            System.err.println("Error reading processes from file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Print processes in a premium ASCII table format.
     */
    public static void printProcesses(List<Process> processes) {
        System.out.println("\nLoaded Process Details:");
        System.out.println("---------------------------------------------");
        System.out.printf("%-5s | %-12s | %-10s\n", "PID", "Arrival Time", "Burst Time");
        System.out.println("---------------------------------------------");
        for (Process p : processes) {
            System.out.printf("%-5d | %-12d | %-10d\n", 
                p.pid, p.arrivalTime, p.burstTime);
        }
        System.out.println("---------------------------------------------");
    }


}
