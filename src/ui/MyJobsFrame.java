package ui;

import javax.swing.*;

import model.AppData;
import model.Student;

public class MyJobsFrame extends JFrame {

	public MyJobsFrame(AppData data, Student student) {

		setTitle("GPU Utilization Management - My Jobs");

		add(new MyJobsPanel(data, student));

		setSize(950, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
}
