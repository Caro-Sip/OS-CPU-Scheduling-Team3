# CPU Scheduling Simulation - Team 3

## Project Objectives

- Comprehend the fundamental mechanics of CPU scheduling within an operating system.
- Implement a variety of core scheduling algorithms: First-Come, First-Served (FCFS), Shortest Job First (SJF), Shortest Remaining Time (SRT), Round Robin (RR), and Multilevel Feedback Queue (MLFQ).
- Simulate the execution behavior of these algorithms utilizing a set of user-defined processes.
- Present the scheduling results visually through Gantt charts, alongside key performance metrics including **Waiting Time**, **Turnaround Time**, and **Response Time**.
- Evaluate and compare the performance and efficiency of different scheduling strategies.

## Setup and Installation Instructions

This project is built using **Java** and uses **Maven** for dependency management and building. 

To compile the project within your local development environment and run it directly via the Java Virtual Machine (JVM):

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="team3.App"
```

For installation and execution via a standalone packaged JAR file:

```bash
mvn clean package
java -jar target/cpu-1.0-SNAPSHOT.jar
```

## Description of Implemented Algorithms

- [x] **First-Come, First-Served (FCFS):** A non-preemptive scheduling algorithm where processes are allocated to the CPU in the exact order of their arrival.
- [x] **Shortest Job First (SJF):** A non-preemptive scheduling approach that selects the waiting process with the smallest execution time to execute next.
- [ ] **Shortest Remaining Time (SRT):** A preemptive version of SJF in which the process with the smallest remaining burst time is scheduled next. Context switching occurs if a new process arrives with a shorter burst time.
- [x] **Round Robin (RR):** A preemptive scheduling algorithm that assigns a configurable fixed time quantum to each process in equal portions and in circular order.
- [x] **Multilevel Feedback Queue (MLFQ):** A complex scheduling algorithm that utilizes a basic 3-level queue architecture (Queue 1: RR q=2, Queue 2: RR q=4, Queue 3: FCFS). It features built-in promotion/demotion logic and an aging mechanism to prevent process starvation.

## Instructions on How to Run Each Scheduler

Because the application comprehensively integrates all the aforementioned algorithms into a single unified interface, executing the compiled JAR file will launch an interactive **Console-based Command-Line Interface (CLI)** menu. 

Through this menu, you can opt to utilize the default, pre-configured list of processes or input a custom set of processes (via console). Once the processes are loaded, simply select your desired algorithm from the prompt to execute the simulation and review the corresponding data.

## Sample Input/Output

### Sample Input
The default configuration initializes four processes:
- **P1**: Arrival Time = 0, Burst Time = 5
- **P2**: Arrival Time = 1, Burst Time = 3
- **P3**: Arrival Time = 2, Burst Time = 8
- **P4**: Arrival Time = 3, Burst Time = 6

*(Configured Quantum for RR/MLFQ is 2)*

### Sample Output (Simulation Results for FCFS)
```text
==============================================================================================
                                   SIMULATION RUN RESULTS                                     
==============================================================================================
PID   | Arrival Time | Burst Time | Start Time | Finish Time | Waiting Time | Turnaround Time
----------------------------------------------------------------------------------------------
1     | 0            | 5          | 0          | 5           | 0            | 5              
2     | 1            | 3          | 5          | 8           | 4            | 7              
3     | 2            | 8          | 8          | 16          | 6            | 14             
4     | 3            | 6          | 16         | 22          | 13           | 19             
----------------------------------------------------------------------------------------------
Average Waiting Time: 5.75
Average Turnaround Time: 11.25
==============================================================================================
```

## Screenshots or Gantt Chart Output

The simulation generates a proportional ASCII-based Gantt chart to visualize process execution over time. Below is a representative timeline output for the FCFS scheduling algorithm:

```text
Gantt Chart Timeline:
+----------+------+----------------+------------+
|    P1    |  P2  |       P3       |     P4     |
+----------+------+----------------+------------+
0         5      8               16           22
```
