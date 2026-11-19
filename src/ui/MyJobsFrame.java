package ui;

import javax.swing.*;

import logic.Scheduler;
import model.Student;

public class MyJobsFrame extends JFrame {

	public MyJobsFrame(Scheduler scheduler, Student student) {

		// Display the student's jobs using the shared scheduler
		add(new MyJobsPanel(scheduler, student));

		// Window settings
		setTitle("GPU Utilization Management - My Jobs");
		setSize(950, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
}
