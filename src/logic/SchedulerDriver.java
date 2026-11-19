package logic;

import java.util.HashMap;
import java.util.Map;

import model.Job;
import model.JobStatus;
import model.Student;
import model.TaskSize;
import model.TimeAllowance;

public class SchedulerDriver {

	    // prints PASS or FAIL so you don't have to compare by eye
	    private static void check(String what, Object actual, Object expected) {
	        boolean ok = actual.equals(expected);
	        System.out.println((ok ? "PASS  " : "FAIL  ") + what
	                + "  (got " + actual + ", expected " + expected + ")");
	    }

	    public static void main(String[] args) {
	        // clock at 16/11/2026 10:00
	        SimulationClock clock = new SimulationClock();
	        clock.setStart(2026, 11, 16, 10, 0);

	        // one student with 100 minutes
	        TimeAllowance allowance = new TimeAllowance(100);
	        Student sara = new Student("100052210", "pass123", "Sara Ahmed",
	                "sara@ku.ac.ae", "0501234567", allowance);
	        Map<String, Student> students = new HashMap<>();
	        students.put("100052210", sara);

	        Scheduler scheduler = new Scheduler(clock, students);

	        // 4 small jobs: each should start at once, on GPU1 to GPU4 in order
	        for (int i = 1; i <= 4; i++) {
	            Job j = scheduler.submit("100052210", "Job " + i, "PyTorch", "test", TaskSize.SMALL);
	            check(j.getJobId() + " status", j.getStatus(), JobStatus.RUNNING);
	            check(j.getJobId() + " gpu", j.getAssignedGpuId(), i);
	        }

	        // 5th job: all GPUs busy, so it must wait at position 1
	        Job fifth = scheduler.submit("100052210", "Job 5", "PyTorch", "test", TaskSize.SMALL);
	        check("J105 status", fifth.getStatus(), JobStatus.QUEUED);
	        check("J105 queue position", scheduler.getQueuePosition(fifth), 1);
	        check("J105 gpu (none yet)", fifth.getAssignedGpuId(), 0);

	        // 6th job: position 2, and the job ID counter should have kept going
	        Job sixth = scheduler.submit("100052210", "Job 6", "PyTorch", "test", TaskSize.LARGE);
	        check("J106 queue position", scheduler.getQueuePosition(sixth), 2);
	        check("queue size", scheduler.getQueue().size(), 2);

	        // balances: 4 + 1 small (5 each) = 25 reserved, plus a large (30) = 55
	        check("reserved", allowance.getReserved(), 55);
	        check("used", allowance.getUsed(), 0);
	        check("available", allowance.getAvailable(), 45);
	    }
	}


