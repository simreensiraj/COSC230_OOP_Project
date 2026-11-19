package logic;
import model.*;

import java.time.LocalDateTime;
import java.util.*;

public class Scheduler {
private SimulationClock clock;
private Map<String, Student> students;
private GPU [] gpus;
private List<Job> jobs;
private int nextJobNumber;

public Scheduler (SimulationClock clock,Map<String, Student> students) {
	gpus = new GPU[4];
	gpus[0]=new GPU(1);
	gpus[1]= new GPU(2);
	gpus[2]=new GPU(3);
	gpus[3]=new GPU(4);
	this.clock = clock;
	this.students=students;
	jobs=new ArrayList<>();
	nextJobNumber=101;
}

private TimeAllowance allowanceOf(String studentId) {
	if (!students.containsKey(studentId)) {
	    throw new IllegalArgumentException("Unknown student");
	}
		
	return students.get(studentId).getAllowance();
}

public Job submit(String studentId, String title, String applicationName, String description, TaskSize size) {
	if (!clock.isSet())
		throw new IllegalStateException ("The simulation clock has not been set yet.");
	allowanceOf(studentId).reserve(size.getDuration());
	String id="J"+nextJobNumber;
	Job job= new Job(id, studentId, title, applicationName, description, size, clock.getNow());
	jobs.add(job);
	nextJobNumber++;
	dispatch();
	return job;
	//Why reserve first: if the student doesn’t have enough time, reserve throws an exception. 
	//If you had already added the job to the list by then, you’d leave a job in the system that was never accepted.
	//Reserving first means a rejected submission leaves nothing behind.
	
}
private GPU findFreeGpu() {
	for (GPU g:gpus) {
		if (g.isFree()) {
			return g;
		}
	}
	return null;
}

public List<Job> getQueue(){
	List<Job> queue = new ArrayList<>();
	 for (Job j:jobs) {
	        if (j.getStatus() == JobStatus.QUEUED) {
	            queue.add(j);
	        }
	    }
	    return queue;
	}
private void dispatch() {
	 while (true) {
	        List<Job> queue = getQueue();
	        GPU gpu = findFreeGpu();
	        if (queue.isEmpty() || gpu==null) {
	            break;
	        }
	        Job job = queue.get(0);
	        job.start(gpu.getId(), clock.getNow());
	        gpu.assign(job);
	    }
}

public int getQueuePosition(Job job) {
    List<Job> queue = getQueue();
    for (int i = 0; i < queue.size(); i++) {
        if (queue.get(i)==job) {
            return i+1;
        }
    }
    return 0;
}
}






