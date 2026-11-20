# GPU Utilization Management System

**COSC230 — Object-Oriented Programming Lab Project**

## Team Members

- Mariam Alzaabi — 100066146
- Simreen Siraj Shehzadi — 100068862
- Alyazeya Alhammadi — 100060361
- Maryam Alhefeiti — 100060410

## 1. Project Overview

The GPU Utilization Management System is a Java desktop application designed to manage shared GPU resources for university students.

The system provides student registration, administrator approval, GPU-minute allowances, job submission, automatic scheduling, cancellation, and utilization monitoring. Four GPUs are shared between approved students.

The application uses Java Swing for the graphical interface and CSV files for persistent storage.

## 2. Requirements

- Java JDK 17 or newer
- Eclipse IDE (recommended) or a compatible Java IDE
- No external database required

## 3. Opening the Project

**Eclipse:**

1. Open Eclipse.
2. Select File → Import.
3. Choose General → Existing Projects into Workspace.
4. Select the downloaded project folder.
5. Finish the import.

**VS Code:** Install the Extension Pack for Java, then open the project folder.

## 4. Running the Application

### Normal Mode

Run `src/ui/Main.java` as a Java Application.

Normal mode reads and saves application records in the `data/` folder.

### Demo Mode

Run `src/ui/DemoMain.java` as a Java Application.

Demo mode loads the prepared demonstration records from the `demo-data/` folder, separately from normal application data.

The included demonstration contains:

| Record | Quantity |
|---|---:|
| Approved students | 5 |
| Pending registration requests | 2 |
| Rejected registration requests | 1 |
| Completed jobs | 6 |
| Cancelled jobs | 6 |
| Running jobs | 4 |
| Queued jobs | 4 |
| Total jobs | 20 |

### Regenerating Demo Data

Run `src/ui/DemoDataGenerator.java` as a Java Application.

This generates a fresh demonstration dataset using the existing model and scheduling classes.

**Important:** Regenerating the data overwrites the existing CSV files in `demo-data/`. Close the demo application first and avoid regenerating if you need to preserve changes made during a demonstration.

## 5. Main Features

### Student Features

- Submit a registration request
- Log in after administrator approval
- View allocated, used, reserved, and available GPU minutes
- Submit GPU jobs with application details and task size
- View job history and scheduling information
- Search and filter personal jobs
- Cancel eligible jobs

### Administrator Features

- Set the simulation clock
- Advance simulated time one minute at a time
- Approve or reject registration requests
- Allocate initial GPU minutes during approval
- Monitor the four GPUs and their running jobs
- View completed and waiting jobs
- Monitor overall GPU utilization
- Search approved students
- Grant additional GPU minutes with a reason
- Cancel eligible student jobs with a reason
- View and filter all jobs by status

## 6. Scheduling and GPU Allowances

The system contains four GPU resources.

When a student submits a job, the Scheduler reserves the estimated GPU minutes and assigns the job to an available GPU. If all GPUs are occupied, the job waits in the queue.

The simulated clock controls job progress and completion. Completed jobs are charged for their execution time, while cancelled jobs release unused reserved minutes according to the scheduling logic.

Jobs may have the following statuses:

- `QUEUED`
- `RUNNING`
- `COMPLETED`
- `CANCELLED`

## 7. Project Structure

| Location | Description |
|---|---|
| `src/model/` | Students, administrator, jobs, GPUs, allowances, requests, and related data classes |
| `src/logic/` | Account management, scheduling, simulation clock, and validation |
| `src/storage/` | Saving and loading CSV data |
| `src/ui/` | Student and administrator Swing interfaces and application launchers |
| `test/tests/` | Available model and storage tests |
| `data/` | Local normal-mode application records |
| `demo-data/` | Prepared demonstration records |

The central `AppData` class holds students, requests, jobs, grants, and the four GPUs. `DataStore` saves and restores these records.

## 8. Data Storage

Application records are saved in CSV files:

- `students.csv`
- `requests.csv`
- `jobs.csv`
- `grants.csv`
- `clock.csv`

The application supports loading existing data when restarted. Normal mode and demo mode use separate folders to avoid mixing their records.

## 9. Testing and Demonstration

The administrator interface has been manually tested for:

- Registration request approval and rejection
- Student allowance management
- Granting additional GPU minutes
- Saving and restoring an allowance grant
- Running-job completion
- Administrator job cancellation
- Releasing reserved minutes after cancellation
- GPU status and utilization updates
- All Jobs search and status filters

The demo-data generator also validates record counts and confirms that the generated records can be loaded from CSV files.

## 10. GitHub Collaboration

The team uses GitHub for version control and collaborative development.

Repository: https://github.com/simreensiraj/COSC230_OOP_Project

Individual development contributions and updates can be reviewed under the repository's **Commits** and **Insights → Contributors** sections.