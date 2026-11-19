package ui;

import javax.swing.*;
import java.awt.event.*;

import logic.Scheduler;
import model.Student;

public class MyJobsFrame extends JFrame {

    // Shared objects
    Scheduler scheduler;
    Student student;

    // My Jobs panel
    MyJobsPanel myJobsPanel;

    public MyJobsFrame(Scheduler scheduler, Student student) {

        this.scheduler = scheduler;
        this.student = student;

        // Create My Jobs panel
        myJobsPanel = new MyJobsPanel(scheduler, student);

        add(myJobsPanel);

        // Refresh when the window becomes active
        addWindowFocusListener(new WindowAdapter() {

            @Override
            public void windowGainedFocus(WindowEvent e) {

                myJobsPanel.refreshJobs();
            }
        });

        // Window settings
        setTitle("GPU Utilization Management - My Jobs");
        setSize(1100, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
}