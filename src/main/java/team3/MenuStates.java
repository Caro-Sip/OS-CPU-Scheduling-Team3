package team3;

import team3.objs.Process;
import team3.objs.SimulationResult;
import team3.sim.CPUSimulator;
import team3.sim.ResultPrinter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;

/**
 * MenuStates enum the states
 */
public enum MenuStates {
    PROCESS_EDIT_MENU,
    LOAD_DEFAULT_PROCESSES,
    INPUT_CUSTOM_PROCESSES,
    EDIT_JSON_PROCESSES,
    MAIN_MENU,
    DISPLAY_PROCESSES,
    RUN_FCFS,
    RUN_SRT,
    RUN_SJF,
    RUN_RR,
    RUN_MLFQ,
    RUN_ALL,
    EXIT
}

class Menu {
    MenuStates currentState = MenuStates.MAIN_MENU;
    private static Menu instance = null;

    // Shared reference to currently loaded processes
    private List<Process> currentProcesses = null;
    // Map to hold simulation results for comparison
    private Map<String, SimulationResult> sessionResults = new LinkedHashMap<>();

    private Menu() {}

    public static Menu getInstance() {
        if (instance == null) {
            instance = new Menu();
        }
        return instance;
    }

    public void run(Scanner scanner) {
        // Ensure processes.json exists with default process details upon first-time execution
        File defaultFile = new File("processes.json");
        if (!defaultFile.exists()) {
            System.out.println("First-time execution detected. Generating default processes file...");
            List<Process> defaultList = App.createDefaultProcesses();
            App.saveProcesses(defaultList, "processes.json");
        }

        currentState = MenuStates.PROCESS_EDIT_MENU;

        while (currentState != MenuStates.EXIT) {
            switch (currentState) {
                case PROCESS_EDIT_MENU:
                    handleProcessEditMenu(scanner);
                    break;
                case LOAD_DEFAULT_PROCESSES:
                    handleLoadDefaultProcesses();
                    break;
                case INPUT_CUSTOM_PROCESSES:
                    handleInputCustomProcesses(scanner);
                    break;
                case EDIT_JSON_PROCESSES:
                    handleEditJsonProcesses(scanner);
                    break;
                case MAIN_MENU:
                    handleMainMenu(scanner);
                    break;
                case DISPLAY_PROCESSES:
                    handleDisplayProcesses();
                    break;
                case RUN_FCFS:
                    handleRunFCFS(scanner);
                    break;
                case RUN_SRT:
                    handleRunSRT(scanner);
                    break;
                case RUN_SJF:
                    handleRunSJF(scanner);
                    break;
                case RUN_RR:
                    handleRunRR(scanner);
                    break;
                case RUN_MLFQ:
                    handleRunMLFQ(scanner);
                    break;
                case RUN_ALL:
                    handleRunAll(scanner);
                    break;
                case EXIT:
                    break;
            }
        }
    }

    /**
     * Handlers for Process Edit / Configuration Menu
     */
    private void handleProcessEditMenu(Scanner scanner) {
        printProcessEditMenu();
        System.out.print("Enter your choice (1-3, 0 to exit): ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                currentState = MenuStates.LOAD_DEFAULT_PROCESSES;
                break;
            case "2":
                currentState = MenuStates.INPUT_CUSTOM_PROCESSES;
                break;
            case "3":
                currentState = MenuStates.EDIT_JSON_PROCESSES;
                break;
            case "0":
                currentState = MenuStates.EXIT;
                break;
            default:
                System.out.println("Invalid choice. Please choose from the menu options.");
                currentState = MenuStates.PROCESS_EDIT_MENU;
                break;
        }
    }

    private void handleLoadDefaultProcesses() {
        System.out.println("\n--- Loading Default Processes ---");
        List<Process> defaultList = App.createDefaultProcesses();
        App.saveProcesses(defaultList, "processes.json");
        currentProcesses = App.readProcesses("processes.json");
        if (currentProcesses != null) {
            sessionResults.clear(); // Clear previous runs since inputs changed
            App.printProcesses(currentProcesses);
            currentState = MenuStates.MAIN_MENU;
        } else {
            System.out.println("Error: Failed to load processes. Please try again.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
        }
    }

    private void handleInputCustomProcesses(Scanner scanner) {
        System.out.println("\n--- Creating Custom Processes ---");
        List<Process> customList = inputCustomProcesses(scanner);
        if (customList == null || customList.isEmpty()) {
            System.out.println("No processes created.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
            return;
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
        App.saveProcesses(customList, customFilename);

        // Read them back using BufferedReader
        currentProcesses = App.readProcesses(customFilename);
        if (currentProcesses != null) {
            sessionResults.clear(); // Clear previous runs since inputs changed
            App.printProcesses(currentProcesses);
            currentState = MenuStates.MAIN_MENU;
        } else {
            System.out.println("Error: Failed to load processes. Please try again.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
        }
    }

    private void handleEditJsonProcesses(Scanner scanner) {
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
            List<Process> templateList = App.createDefaultProcesses();
            App.saveProcesses(templateList, filePath);
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

        currentProcesses = App.readProcesses(filePath);
        if (currentProcesses != null) {
            sessionResults.clear(); // Clear previous runs since inputs changed
            App.printProcesses(currentProcesses);
            currentState = MenuStates.MAIN_MENU;
        } else {
            System.out.println("Error: Failed to load processes. Please try again.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
        }
    }

    /**
     * Handlers for the Main Scheduling Menu
     */
    private void handleMainMenu(Scanner scanner) {
        printMainMenu();
        System.out.print("Enter your choice (1-8, 0 to exit): ");
        String mainChoice = scanner.nextLine().trim();

        switch (mainChoice) {
            case "1":
                currentState = MenuStates.DISPLAY_PROCESSES;
                break;
            case "2":
                currentState = MenuStates.RUN_FCFS;
                break;
            case "3":
                currentState = MenuStates.RUN_SRT;
                break;
            case "4":
                currentState = MenuStates.RUN_SJF;
                break;
            case "5":
                currentState = MenuStates.RUN_RR;
                break;
            case "6":
                currentState = MenuStates.RUN_MLFQ;
                break;
            case "7":
                currentState = MenuStates.RUN_ALL;
                break;
            case "8":
                currentState = MenuStates.PROCESS_EDIT_MENU;
                break;
            case "0":
                currentState = MenuStates.EXIT;
                break;
            default:
                System.out.println("Invalid choice. Please choose from the menu options.");
                currentState = MenuStates.MAIN_MENU;
                break;
        }
    }

    private void handleDisplayProcesses() {
        if (currentProcesses != null) {
            App.printProcesses(currentProcesses);
            if (!sessionResults.isEmpty()) {
                ResultPrinter.printComparison(sessionResults);
            }
        } else {
            System.out.println("No processes loaded yet. Please configure processes first.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
            return;
        }
        currentState = MenuStates.MAIN_MENU;
    }

    private void handleRunFCFS(Scanner scanner) {
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
            currentState = MenuStates.PROCESS_EDIT_MENU;
            return;
        }
        currentState = MenuStates.MAIN_MENU;
    }

    private void handleRunSRT(Scanner scanner) {
        System.out.println("\nExecuting SRT (Shortest Remaining Time First) Scheduling...");
        if (currentProcesses != null) {
            try {
                team3.interfaces.Scheduler srt = new team3.algorithms.SRTF();
                CPUSimulator simulator = new CPUSimulator();
                SimulationResult result = simulator.run(currentProcesses, srt);
                ResultPrinter.print(result);
                sessionResults.put("SRT", result);
            } catch (Exception e) {
                System.out.println("Error running SRT simulation: " + e.getMessage());
            }
            System.out.println("\nPress [Enter] to return to the Main Menu...");
            scanner.nextLine();
        } else {
            System.out.println("No processes loaded yet. Please configure processes first.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
            return;
        }
        currentState = MenuStates.MAIN_MENU;
    }

    private void handleRunSJF(Scanner scanner) {
        System.out.println("\nExecuting SJF (Shortest Job First) Scheduling...");
        if (currentProcesses != null) {
            try {
                team3.interfaces.Scheduler sjf = new team3.algorithms.SJF();
                CPUSimulator simulator = new CPUSimulator();
                SimulationResult result = simulator.run(currentProcesses, sjf);
                ResultPrinter.print(result);
                sessionResults.put("SJF", result);
            } catch (Exception e) {
                System.out.println("Error running SJF simulation: " + e.getMessage());
            }
            System.out.println("\nPress [Enter] to return to the Main Menu...");
            scanner.nextLine();
        } else {
            System.out.println("No processes loaded yet. Please configure processes first.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
            return;
        }
        currentState = MenuStates.MAIN_MENU;
    }

    private void handleRunRR(Scanner scanner) {
        System.out.println("\nExecuting RR (Round Robin) Scheduling...");
        if (currentProcesses != null) {
            int quantum = getQuantumInput(scanner, 2);
            try {
                team3.interfaces.Scheduler rr = new team3.algorithms.RR(quantum);
                CPUSimulator simulator = new CPUSimulator();
                SimulationResult result = simulator.run(currentProcesses, rr);
                ResultPrinter.print(result);
                sessionResults.put("RR (q=" + quantum + ")", result);
            } catch (Exception e) {
                System.out.println("Error running RR simulation: " + e.getMessage());
            }
            System.out.println("\nPress [Enter] to return to the Main Menu...");
            scanner.nextLine();
        } else {
            System.out.println("No processes loaded yet. Please configure processes first.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
            return;
        }
        currentState = MenuStates.MAIN_MENU;
    }

    private void handleRunMLFQ(Scanner scanner) {
        System.out.println("\nExecuting MLFQ (Multi-Level Feedback Queue) Scheduling...");
        if (currentProcesses != null) {
            int numLevels = 3;
            System.out.print("Enter number of levels (default 3): ");
            String numLevelsInput = scanner.nextLine().trim();
            if (!numLevelsInput.isEmpty()) {
                try {
                    numLevels = Integer.parseInt(numLevelsInput);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Using default 3 levels.");
                }
            }
            if (numLevels <= 0) {
                System.out.println("Number of levels must be > 0. Using default 3.");
                numLevels = 3;
            }

            int[] quantums = new int[numLevels];
            for (int i = 0; i < numLevels; i++) {
                int defaultQ = (i == numLevels - 1) ? 0 : (int) Math.pow(2, i + 1); // 2, 4, etc. Last is FCFS (0)
                System.out.print("Enter quantum for level " + i + " (default " + defaultQ + ", 0 for FCFS): ");
                String qInput = scanner.nextLine().trim();
                int q = defaultQ;
                if (!qInput.isEmpty()) {
                    try {
                        q = Integer.parseInt(qInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Using default " + defaultQ);
                    }
                }
                if (q < 0) {
                    System.out.println("Quantum cannot be negative. Using default " + defaultQ);
                    q = defaultQ;
                }
                quantums[i] = q;
            }

            try {
                team3.interfaces.Scheduler mlfq = new team3.algorithms.MLFQ(quantums);
                CPUSimulator simulator = new CPUSimulator();
                SimulationResult result = simulator.run(currentProcesses, mlfq);
                ResultPrinter.print(result);

                StringBuilder sb = new StringBuilder("MLFQ (q=[");
                for (int i = 0; i < quantums.length; i++) {
                    sb.append(quantums[i]);
                    if (i < quantums.length - 1) sb.append(",");
                }
                sb.append("])");
                sessionResults.put(sb.toString(), result);
            } catch (Exception e) {
                System.out.println("Error running MLFQ simulation: " + e.getMessage());
            }
            System.out.println("\nPress [Enter] to return to the Main Menu...");
            scanner.nextLine();
        } else {
            System.out.println("No processes loaded yet. Please configure processes first.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
            return;
        }
        currentState = MenuStates.MAIN_MENU;
    }

    private void handleRunAll(Scanner scanner) {
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

            // 2. SRT (Shortest Remaining Time)
            try {
                team3.interfaces.Scheduler srt = new team3.algorithms.SRTF();
                CPUSimulator simulator = new CPUSimulator();
                SimulationResult result = simulator.run(currentProcesses, srt);
                sessionResults.put("SRT", result);
                System.out.println(" - SRT: Completed successfully");
            } catch (Exception e) {
                System.out.println(" - SRT: Failed (" + e.getMessage() + ")");
            }

            // 3. SJF (Shortest Job First)
            try {
                team3.interfaces.Scheduler sjf = new team3.algorithms.SJF();
                CPUSimulator simulator = new CPUSimulator();
                SimulationResult result = simulator.run(currentProcesses, sjf);
                sessionResults.put("SJF", result);
                System.out.println(" - SJF: Completed successfully");
            } catch (Exception e) {
                System.out.println(" - SJF: Failed (" + e.getMessage() + ")");
            }

            // 4. RR (Round Robin)
            try {
                int quantum = 2; // default quantum for Run All comparison
                team3.interfaces.Scheduler rr = new team3.algorithms.RR(quantum);
                CPUSimulator simulator = new CPUSimulator();
                SimulationResult result = simulator.run(currentProcesses, rr);
                sessionResults.put("RR (q=" + quantum + ")", result);
                System.out.println(" - RR: Completed successfully");
            } catch (Exception e) {
                System.out.println(" - RR: Failed (" + e.getMessage() + ")");
            }

            // 5. MLFQ (Multi-Level Feedback Queue)
            try {
                int[] quantums = new int[]{2, 4, 0}; // default [2, 4, FCFS]
                team3.interfaces.Scheduler mlfq = new team3.algorithms.MLFQ(quantums);
                CPUSimulator simulator = new CPUSimulator();
                SimulationResult result = simulator.run(currentProcesses, mlfq);
                sessionResults.put("MLFQ (q=[2,4,0])", result);
                System.out.println(" - MLFQ: Completed successfully");
            } catch (Exception e) {
                System.out.println(" - MLFQ: Failed (" + e.getMessage() + ")");
            }

            // Print comparative results if we have at least one successful run
            if (!sessionResults.isEmpty()) {
                ResultPrinter.printComparison(sessionResults);
            }
            System.out.println("\nPress [Enter] to return to the Main Menu...");
            scanner.nextLine();
        } else {
            System.out.println("No processes loaded yet. Please configure processes first.");
            currentState = MenuStates.PROCESS_EDIT_MENU;
            return;
        }
        currentState = MenuStates.MAIN_MENU;
    }

    private int getQuantumInput(Scanner scanner, int defaultVal) {
        while (true) {
            System.out.print("Enter Time Quantum for RR (default " + defaultVal + "): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultVal;
            }
            try {
                int val = Integer.parseInt(input);
                if (val > 0) {
                    return val;
                }
                System.out.println("Time quantum must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a positive integer.");
            }
        }
    }

    /**
     * Helper to read custom processes from the user
     */
    private List<Process> inputCustomProcesses(Scanner scanner) {
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

            Process p = new Process(pid, arrivalTime, burstTime, -1);
            customList.add(p);
        }
        return customList;
    }

    /**
     * Print the Processes Configuration / Edit Menu.
     */
    private void printProcessEditMenu() {
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
    private void printMainMenu() {
        System.out.println("\n==========================================");
        System.out.println("             MAIN MENU                    ");
        System.out.println("==========================================");
        System.out.println("1. Display the Process List");
        System.out.println("2. FCFS (First Come First Served)");
        System.out.println("3. SRT (Shortest Remaining Time)");
        System.out.println("4. SJF (Shortest Job First)");
        System.out.println("5. RR (Round Robin)");
        System.out.println("6. MLFQ (Multi-Level Feedback Queue)");
        System.out.println("7. Run All Algorithms");
        System.out.println("8. Revisit Processes Edit Menu");
        System.out.println("0. Exit");
        System.out.println("==========================================");
    }
}