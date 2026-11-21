
package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import logic.AccountService;
import model.AccessRequest;
import model.Administrator;
import model.AppData;
import model.RequestStatus;

public class RequestsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
    private final AppData data;
    private final AccountService accountService;
    private final Administrator admin;

    private final JTable requestTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> statusFilter;
    private final JSpinner minutesSpinner;

    private final ArrayList<AccessRequest> displayedRequests = new ArrayList<>();

    public RequestsPanel(AppData data, AccountService accountService,
            Administrator admin) {

        this.data = data;
        this.accountService = accountService;
        this.admin = admin;

        setLayout(new BorderLayout(10, 10));

        // Filter section
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.add(new JLabel("Registration Requests"));

        statusFilter = new JComboBox<>(
                new String[]{"Pending", "Approved", "Rejected", "All"});

        topPanel.add(new JLabel("Show:"));
        topPanel.add(statusFilter);

        add(topPanel, BorderLayout.NORTH);

        // Requests table
        String[] columns = {
            "University ID", "Name", "Email", "Phone", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);
        requestTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(requestTable), BorderLayout.CENTER);

        // Approval and rejection controls
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        bottomPanel.add(new JLabel("Initial GPU minutes:"));

        minutesSpinner = new JSpinner(
                new SpinnerNumberModel(
                        AccountService.DEFAULT_MINUTES, 1, 100000, 1));

        bottomPanel.add(minutesSpinner);

        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");

        bottomPanel.add(approveButton);
        bottomPanel.add(rejectButton);

        add(bottomPanel, BorderLayout.SOUTH);

        statusFilter.addActionListener(e -> refreshRequests());

        approveButton.addActionListener(e -> approveRequest());
        rejectButton.addActionListener(e -> rejectRequest());
    }

    public void refreshRequests() {

        tableModel.setRowCount(0);
        displayedRequests.clear();

        String selectedFilter = (String) statusFilter.getSelectedItem();

        for (AccessRequest request : data.getRequests()) {

            String status = request.getStatus().name();

            if (!"All".equals(selectedFilter)
                    && !status.equalsIgnoreCase(selectedFilter)) {
                continue;
            }

            displayedRequests.add(request);

            tableModel.addRow(new Object[]{
                request.getUniversityId(),
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                status
            });
        }
    }

    private AccessRequest getSelectedRequest() {

        int row = requestTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.");
            return null;
        }

        int modelRow = requestTable.convertRowIndexToModel(row);

        return displayedRequests.get(modelRow);
    }

    private void approveRequest() {

        AccessRequest request = getSelectedRequest();

        if (request == null) {
            return;
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            JOptionPane.showMessageDialog(this,
                    "This request has already been processed.");
            return;
        }

        int minutes = (Integer) minutesSpinner.getValue();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Approve " + request.getName()
                + " with " + minutes + " GPU minutes?",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            accountService.approve(admin, request, minutes);

            JOptionPane.showMessageDialog(this,
                    "Student approved successfully.");

            refreshRequests();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Approval Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectRequest() {

        AccessRequest request = getSelectedRequest();

        if (request == null) {
            return;
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            JOptionPane.showMessageDialog(this,
                    "This request has already been processed.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Reject the request from " + request.getName() + "?",
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            accountService.reject(admin, request);

            JOptionPane.showMessageDialog(this,
                    "Request rejected successfully.");

            refreshRequests();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Rejection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
