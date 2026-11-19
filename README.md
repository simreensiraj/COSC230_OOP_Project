# COSC230_OOP_Project
COSC230 - OOP Lab PROJECT 

Team Members:

Mariam Alzaabi - 100066146

Simreen Siraj Shehzadi - 100068862

Alyazeya Alhammadi - 100060361

Maryam Alhefeiti - 100060410

## Opening the project

**Eclipse:** File → Import → General → Existing Projects into Workspace → pick this folder.

**VS Code:** install "Extension Pack for Java", then File → Open Folder.

Java 17 or newer.

## Layout

- `src/model` – User, Student, Administrator, AccessRequest, Job, GPU, TimeAllowance, TimeGrant, AppData
- `src/logic` – AccountService, SimulationClock, Scheduler
- `src/storage` – DataStore: saves/loads `data/*.csv`
- `src/ui` – Swing windows
- `test/tests/ModelStorageTest.java` – run as a Java application

## Using the model

- `AppData` holds students, requests, jobs, grants and the 4 GPUs. The time is in `SimulationClock`.
- Load: `AppData data = new DataStore().load(clock);` (fills the clock too). Save: `new DataStore().save(data, clock);`
- Submit: `allowance.reserve(duration)`, then add the job to `data.getJobs()`.
- Start: `job.start(gpuId, now)` and `gpu.assign(job)`.
- Complete: `job.complete(now)`, `allowance.release(duration)`, `allowance.charge(job.getMinutesRun(now))`, `gpu.free()`.
- Cancel: `job.cancel(by, reason, now)`, `allowance.release(duration)`, `allowance.charge(job.getMinutesRun(now))` (0 if it was queued).
- Accounts: `register(...)`, `login(...)`, `approve(admin, request, minutes)`, `reject(admin, request)`, `grantTime(...)` in `AccountService`.
- Errors the user should see come as `ValidationException`: show `e.getMessage()` (for a form, `e.getField()` says which field). This covers registration, login and a broken data file on `load()`.
