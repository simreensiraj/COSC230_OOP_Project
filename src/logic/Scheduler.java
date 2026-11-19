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


}
