package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import logic.AccountService;
import logic.ValidationException;
import logic.Scheduler;

import model.Administrator;
import model.AppData;
import model.Student;
import model.User;

public class LoginFrame extends JFrame implements ActionListener {

    // Shared objects
    AppData data;
    AccountService accountService;
    Scheduler scheduler;

    // Text fields
    JTextField usernameField;
    JPasswordField passwordField;

    // Buttons
    JButton loginButton;
    JButton registerButton;

    public LoginFrame(AppData data, AccountService accountService, Scheduler scheduler) {

        this.data = data;
        this.accountService = accountService;
        this.scheduler = scheduler;

        GridLayout layout = new GridLayout(3, 2);
        setLayout(layout);

        // Create text fields
        usernameField = new JTextField();
        passwordField = new JPasswordField();

        // Create buttons
        loginButton = new JButton("Login");
        registerButton = new JButton("Request Account");

        // Add action listeners
        loginButton.addActionListener(this);
        registerButton.addActionListener(this);

        // Add components
        add(new JLabel("University ID / Username:"));
        add(usernameField);

        add(new JLabel("Password:"));
        add(passwordField);

        add(loginButton);
        add(registerButton);

        // Window settings
        setTitle("GPU Utilization Management - Login");
        setSize(550, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == loginButton) {

            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            try {

                User user = accountService.login(username, password);

                if (user instanceof Administrator) {

                    new AdminFrame(data, accountService, (Administrator) user).setVisible(true);

                    dispose();

                } else if (user instanceof Student) {

                    new StudentFrame(data, accountService, scheduler, (Student) user);

                    dispose();
                }

            } catch (ValidationException ex) {

                JOptionPane.showMessageDialog(this, ex.getMessage());
            }

        } else if (e.getSource() == registerButton) {

            new StudentRegistrationFrame(data, accountService, scheduler);
            dispose();
        }
    }
}