package team3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import team3.objs.Process;
import team3.sim.*;

import java.awt.Desktop;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * CLI Menu Application for CPU Scheduling
 */
public class App {

    // Shared reference to currently loaded processes
    private static List<Process> currentProcesses = null;
    // Map to hold simulation results for comparison
    private static Map<String, SimulationResult> sessionResults = new LinkedHashMap<>();

    public static void main(String[] args) {
        // Ensure processes.json exists with default process details upon first-time execution
        File defaultFile = new File("processes.json");
        if (!defaultFile.exists()) {
            System.out.println("First-time execution detected. Generating default processes file...");
            List<Process> defaultList = createDefaultProcesses();
            saveProcesses(defaultList, "processes.json");
        }

        Scanner scanner = new Scanner(System.in);
        MenuStates state = MenuStates.PROCESS_EDIT_MENU;

        while (state != MenuStates.EXIT) {
            switch (state) {
                case PROCESS_EDIT_MENU:
                    state = handleProcessEditMenu(scanner);
                    break;
                case MAIN_MENU:
                    state = handleMainMenu(scanner);
                    break;
                case EXIT:
                    break;
            }
        }

        System.out.println("Exiting CPU Scheduler. Goodbye!");
        scanner.close();
    }

    /**
     * Handlers for Process Edit / Configuration Menu
     */
    private static MenuStates handleProcessEditMenu(Scanner scanner) {
        printProcessEditMenu();
        System.out.print("Enter your choice (1-3, 0 to exit): ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                System.out.println("\n--- Loading Default Processes ---");
                List<Process> defaultList = createDefaultProcesses();
                saveProcesses(defaultList, "processes.json");
                currentProcesses = readProcesses("processes.json");
                if (currentProcesses != null) {
                    sessionResults.clear(); // Clear previous runs since inputs changed
                    printProcesses(currentProcesses);
                    return MenuStates.MAIN_MENU;
                } else {
                    System.out.println("Error: Failed to load processes. Please try again.");
                    return MenuStates.PROCESS_EDIT_MENU;
                }

            case "2":
                System.out.println("\n--- Creating Custom Processes ---");
                List<Process> customList = inputCustomProcesses(scanner);
                if (customList == null || customList.isEmpty()) {
                    System.out.println("No processes created.");
                    return MenuStates.PROCESS_EDIT_MENU;
                }

                System.out.print("Enter file name to save custom processes (default: custom_processes.json): ");
                String customFilename = scanner.nextLine().trim();
                if (customFilename.isEmpty()) {
                    customFilename = "custom_processes.json";
                }
                if (!customFilename.endsWith(".json")) {
                    customFilename += ".json";
                }

                // Write custom processes using BufferedWriter
                saveProcesses(customList, customFilename);

                // Read them back using BufferedReader
                currentProcesses = readProcesses(customFilename);
                if (currentProcesses != null) {
                    sessionResults.clear(); // Clear previous runs since inputs changed
                    printProcesses(currentProcesses);
                    return MenuStates.MAIN_MENU;
                } else {
                    System.out.println("Error: Failed to load processes. Please try again.");
                    return MenuStates.PROCESS_EDIT_MENU;
                }

            case "3":
                System.out.println("\n--- Read/Edit Processes from JSON File ---");
                System.out.print("Enter JSON file path to read/edit (default: processes.json): ");
                String filePath = scanner.nextLine().trim();
                if (filePath.isEmpty()) {
                    filePath = "processes.json";
                }
                if (!filePath.endsWith(".json")) {
                    filePath += ".json";
                }

                File fileToRead = new File(filePath);
                if (!fileToRead.exists()) {
                    System.out.println("File does not exist. Creating a template JSON file at: " + fileToRead.getAbsolutePath());
                    List<Process> templateList = createDefaultProcesses();
                    saveProcesses(templateList, filePath);
                }

                System.out.println("\n=======================================================");
                System.out.println("The program will open the default text editor for the file:");
                System.out.println(fileToRead.getAbsolutePath());
                System.out.println("=======================================================");
                System.out.println("Press [Enter] to launch the text editor...");
                scanner.nextLine(); // Wait for user to press Enter

                boolean opened = false;
                try {
                    String os = System.getProperty("os.name").toLowerCase();
                    ProcessBuilder pb;
                    if (os.contains("win")) {
                        pb = new ProcessBuilder("cmd", "/c", "start", "/wait", "", fileToRead.getAbsolutePath());
                    } else if (os.contains("mac")) {
                        pb = new ProcessBuilder("open", "-W", fileToRead.getAbsolutePath());
                    } else {
                        pb = new ProcessBuilder("xdg-open", fileToRead.getAbsolutePath());
                    }

                    // Discard standard output and error streams to prevent any cluttering logs
                    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    pb.redirectError(ProcessBuilder.Redirect.DISCARD);

                    System.out.println("Launching default text editor. Please edit and save the file.");
                    System.out.println("When you are finished, close the text editor window to proceed...");
                    java.lang.Process process = pb.start();
                    process.waitFor(); // Wait for the process to terminate
                    opened = true;
                } catch (Exception e) {
                    // Fallback to Desktop API if exec/wait fails
                }

                if (!opened) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            if (desktop.isSupported(Desktop.Action.EDIT)) {
                                try {
                                    desktop.edit(fileToRead);
                                    opened = true;
                                } catch (IOException e) {
                                    // Fallback to OPEN
                                }
                            }
                            if (!opened && desktop.isSupported(Desktop.Action.OPEN)) {
                                try {
                                    desktop.open(fileToRead);
                                    opened = true;
                                } catch (IOException e) {
                                    // Ignore
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                }

                if (opened) {
                    System.out.println("\nEditor session finished. Press [Enter] here to continue the program...");
                } else {
                    System.out.println("Could not launch default editor automatically.");
                    System.out.println("Please open the file manually: " + fileToRead.getAbsolutePath());
                    System.out.println("Once you have saved the file, press [Enter] here to continue the program...");
                }
                scanner.nextLine(); // Wait for user to press Enter

                currentProcesses = readProcesses(filePath);
                if (currentProcesses != null) {
                    sessionResults.clear(); // Clear previous runs since inputs changed
                    printProcesses(currentProcesses);
                    return MenuStates.MAIN_MENU;
                } else {
                    System.out.println("Error: Failed to load processes. Please try again.");
                    return MenuStates.PROCESS_EDIT_MENU;
                }

            case "0":
                return MenuStates.EXIT;

            default:
                System.out.println("Invalid choice. Please choose from the menu options.");
                return MenuStates.PROCESS_EDIT_MENU;
        }
    }

    /**
     * Handlers for the Main Scheduling Menu
     */
    private static MenuStates handleMainMenu(Scanner scanner) {
        printMainMenu();
        System.out.print("Enter your choice (1-8, 0 to exit): ");
        String mainChoice = scanner.nextLine().trim();

        switch (mainChoice) {
            case "1":
                if (currentProcesses != null) {
                    printProcesses(currentProcesses);
                    if (!sessionResults.isEmpty()) {
                        ResultPrinter.printComparison(sessionResults);
                    }
                } else {
                    System.out.println("No processes loaded yet. Please configure processes first.");
                    return MenuStates.PROCESS_EDIT_MENU;
                }
                return MenuStates.MAIN_MENU;

            case "2":
                System.out.println("\nExecuting FCFS (First Come First Served) Scheduling...");
                if (currentProcesses != null) {
                    try {
                        team3.interfaces.Scheduler fcfs = new team3.algorithms.FCFS();
                        CPUSimulator simulator = new CPUSimulator();
                        SimulationResult result = simulator.run(currentProcesses, fcfs);
                        ResultPrinter.print(result);
                        sessionResults.put("FCFS", result);
                    } catch (Exception e) {
                        System.out.println("Error running FCFS simulation: " + e.getMessage());
                    }
                    System.out.println("\nPress [Enter] to return to the Main Menu...");
                    scanner.nextLine();
                } else {
                    System.out.println("No processes loaded yet. Please configure processes first.");
                }
                return MenuStates.MAIN_MENU;

            case "3":
                System.out.println("\nExecuting SRT (Shortest Remaining Time) Scheduling...");
                // Stub for SRT
                System.out.println("[SRT Scheduling logic not yet connected]");
                System.out.println("\nPress [Enter] to return to the Main Menu...");
                scanner.nextLine();
                return MenuStates.MAIN_MENU;

            case "4":
                System.out.println("\nExecuting SRJ (Shortest Remaining Job) Scheduling...");
                // Stub for SRJ
                System.out.println("[SRJ Scheduling logic not yet connected]");
                System.out.println("\nPress [Enter] to return to the Main Menu...");
                scanner.nextLine();
                return MenuStates.MAIN_MENU;

            case "5":
                System.out.println("\nExecuting RR (Round Robin) Scheduling...");
                // Stub for RR
                System.out.println("[RR Scheduling logic not yet connected]");
                System.out.println("\nPress [Enter] to return to the Main Menu...");
                scanner.nextLine();
                return MenuStates.MAIN_MENU;

            case "6":
                System.out.println("\nExecuting MLFQ (Multi-Level Feedback Queue) Scheduling...");
                // Stub for MLFQ
                System.out.println("[MLFQ Scheduling logic not yet connected]");
                System.out.println("\nPress [Enter] to return to the Main Menu...");
                scanner.nextLine();
                return MenuStates.MAIN_MENU;

            case "7":
                System.out.println("\nRunning All Scheduler Algorithms...");
                if (currentProcesses != null) {
                    // 1. FCFS
                    try {
                        team3.interfaces.Scheduler fcfs = new team3.algorithms.FCFS();
                        CPUSimulator simulator = new CPUSimulator();
                        SimulationResult result = simulator.run(currentProcesses, fcfs);
                        sessionResults.put("FCFS", result);
                        System.out.println(" - FCFS: Completed successfully");
                    } catch (Exception e) {
                        System.out.println(" - FCFS: Failed (" + e.getMessage() + ")");
                    }

                    // 2. SRT (Shortest Remaining Time) - stub
                    System.out.println(" - SRT: Not yet implemented");

                    // 3. SRJ (Shortest Remaining Job) - stub
                    System.out.println(" - SRJ: Not yet implemented");

                    // 4. RR (Round Robin) - stub
                    System.out.println(" - RR: Not yet implemented");

                    // 5. MLFQ (Multi-Level Feedback Queue) - stub
                    System.out.println(" - MLFQ: Not yet implemented");

                    // Print comparative results if we have at least one successful run
                    if (!sessionResults.isEmpty()) {
                        ResultPrinter.printComparison(sessionResults);
                    }
                    System.out.println("\nPress [Enter] to return to the Main Menu...");
                    scanner.nextLine();
                } else {
                    System.out.println("No processes loaded yet. Please configure processes first.");
                }
                return MenuStates.MAIN_MENU;

            case "8":
                System.out.println("\nReturning to Processes Edit Menu...");
                return MenuStates.PROCESS_EDIT_MENU;

            case "0":
                return MenuStates.EXIT;

            default:
                System.out.println("Invalid choice. Please choose from the menu options.");
                return MenuStates.MAIN_MENU;
        }
    }

    /**
     * Helper to read custom processes from the user
     */
    private static List<Process> inputCustomProcesses(Scanner scanner) {
        int amount = 0;
        while (true) {
            System.out.print("Enter amount of processes: ");
            try {
                amount = Integer.parseInt(scanner.nextLine().trim());
                if (amount > 0) break;
                System.out.println("Amount must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }

        List<Process> customList = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            int pid = i + 1; // System-incremented PID
            System.out.println("\nEnter details for Process PID " + pid + ":");

            int arrivalTime = 0;
            while (true) {
                System.out.print("Arrival Time: ");
                try {
                    arrivalTime = Integer.parseInt(scanner.nextLine().trim());
                    if (arrivalTime >= 0) break;
                    System.out.println("Arrival Time must be >= 0.");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter an integer.");
                }
            }

            int burstTime = 0;
            while (true) {
                System.out.print("Burst Time: ");
                try {
                    burstTime = Integer.parseInt(scanner.nextLine().trim());
                    if (burstTime > 0) break;
                    System.out.println("Burst Time must be > 0.");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter an integer.");
                }
            }

            int priority = -1;
            System.out.print("Priority (default -1): ");
            String priorityInput = scanner.nextLine().trim();
            if (!priorityInput.isEmpty()) {
                try {
                    priority = Integer.parseInt(priorityInput);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid priority. Using default -1");
                }
            }

            Process p = new Process(pid, arrivalTime, burstTime, priority);
            customList.add(p);
        }
        return customList;
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
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-5s | %-12s | %-10s | %-8s\n", "PID", "Arrival Time", "Burst Time", "Priority");
        System.out.println("---------------------------------------------------------");
        for (Process p : processes) {
            System.out.printf("%-5d | %-12d | %-10d | %-8d\n", 
                p.pid, p.arrivalTime, p.burstTime, p.priority);
        }
        System.out.println("---------------------------------------------------------");
    }

    /**
     * Print the Processes Configuration / Edit Menu.
     */
    public static void printProcessEditMenu() {
        System.out.println("\n==========================================");
        System.out.println("      PROCESS CONFIGURATION MENU          ");
        System.out.println("==========================================");
        System.out.println("1. Load Default Processes (processes.json)");
        System.out.println("2. Input Custom Processes");
        System.out.println("3. Read/Edit Processes from a JSON File");
        System.out.println("0. Exit");
        System.out.println("==========================================");
    }

    /**
     * Print the Main Scheduling Menu.
     */
    public static void printMainMenu() {
        System.out.println("\n==========================================");
        System.out.println("             MAIN MENU                    ");
        System.out.println("==========================================");
        System.out.println("1. Display the Process List");
        System.out.println("2. FCFS (First Come First Served)");
        System.out.println("3. SRT (Shortest Remaining Time)");
        System.out.println("4. SRJ (Shortest Remaining Job)");
        System.out.println("5. RR (Round Robin)");
        System.out.println("6. MLFQ (Multi-Level Feedback Queue)");
        System.out.println("7. Run All Algorithms");
        System.out.println("8. Revisit Processes Edit Menu");
        System.out.println("0. Exit");
        System.out.println("==========================================");
    }
}
